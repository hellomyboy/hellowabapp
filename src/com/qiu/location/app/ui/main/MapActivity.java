package com.qiu.location.app.ui.main;

import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.fallenpanda.location.bean.MyLocation;
import com.fallenpanda.location.utils.StringUtils;
import com.qiu.location.app.R;
import com.qiu.location.app.base.RecycleBaseAdapter;
import com.qiu.location.app.ui.BaseMapActivity;
import com.qiu.location.app.ui.main.adapter.PoiInfoAdapter;
import com.qiu.location.app.ui.main.adapter.QuerySuggestionsAdapter;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 地图Demo
 * ============================================================================
 * 版权所有 2015 。
 *
 * @author fallenpanda
 * @version 1.0 2015/10/20
 * ============================================================================
 */
public class MapActivity extends BaseMapActivity implements RecycleBaseAdapter.OnItemClickListener {

    @Bind(R.id.m_actionbar)
    Toolbar mActionbar;
    @Bind(R.id.m_tv_location)
    TextView mTvLocation;
    @Bind(R.id.m_listview)
    RecyclerView mListview;

    private MenuItem mMenuSearchView;
    private MenuItem mMenuSearch;
    private MenuItem mMenuSure;

    private LinearLayoutManager mLayoutManager;
    private PoiInfoAdapter poiInfoAdapter;

    private SearchView mSearchView;
    private String mSearchText;

    //-----------------------------------------------
    //Poi查询
    private CursorAdapter suggestAdapter;
    private MatrixCursor suggestCursor = new MatrixCursor(QuerySuggestionsAdapter.COLUMNS);

    private static final int STATE_NONE = 0;
    private static final int STATE_REFRESH = 1;
    private static final int STATE_LOADMORE = 2;
    private int mState = STATE_NONE;//页面状态

    private static final int TYPE_NEARBY = 0;//周边检索
    private static final int TYPE_CITY = 1;//城市内检索
    private int mType = TYPE_NEARBY;//检索类型
    //-----------------------------------------------
    //百度相关
    private Marker otherMarker;//其他标记点

    private LatLng curPoint;//当前中心坐标
    //-----------------------------------------------

    @Override
    public boolean isNeedPoiSearch() {
        return true;
    }

    @Override
    public boolean isNeedSuggestionSearch() {
        return true;
    }

    @Override
    public boolean isNeedGeoCoder() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        ButterKnife.bind(this);

        setSupportActionBar(mActionbar);

        initMapView();

        mViewMap.setOnLocButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnLocClick();
            }
        });

        //初始化列表
        mListview.setOnScrollListener(mScrollListener);

        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mListview.setLayoutManager(mLayoutManager);
        mListview.setHasFixedSize(true);
        mListview.setItemAnimator(new DefaultItemAnimator());

        poiInfoAdapter = new PoiInfoAdapter();
        poiInfoAdapter.setOnItemClickListener(this);
        mListview.setAdapter(poiInfoAdapter);

        //定位
        onBtnLocClick();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_map_search, menu);

        mMenuSearchView = menu.findItem(R.id.main_menu_searchview);
        mSearchView = (SearchView) MenuItemCompat.getActionView(mMenuSearchView);
        mSearchView.setQueryHint("搜索位置...");

        suggestAdapter = new QuerySuggestionsAdapter(this, suggestCursor);
        mSearchView.setSuggestionsAdapter(suggestAdapter);

        mMenuSearch = menu.findItem(R.id.main_menu_search);
        mMenuSure = menu.findItem(R.id.main_menu_sure);

        MenuItemCompat.setOnActionExpandListener(mMenuSearchView, actionExpandListener);

        mSearchView.setOnQueryTextListener(queryTextListener);
        mSearchView.setOnSuggestionListener(suggestionListener);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.main_menu_search:
                searchByAddress(mSearchText);
                return true;
            case R.id.main_menu_sure:
                if (mTvLocation.getTag() == null) return true;

                MyLocation location = (MyLocation) mTvLocation.getTag();
                String address = location.getmAddress() +"\n经度: "+location.getmLongitude()+"\n维度: "+location.getmLatitude();
                Toast.makeText(this, address, Toast.LENGTH_LONG).show();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onBtnLocClick() {
        mainLocationManager.getBaiduLocationOnce(true);
    }

    /**
     * 列表滚动事件
     */
    private RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
            int totalItemCount = mLayoutManager.getItemCount();
            if (lastVisibleItem >= totalItemCount - 4 && dy > 0) {
                if (mState == STATE_NONE && poiInfoAdapter != null
                        && poiInfoAdapter.getDataSize() > 0) {
                    loadMore();
                }
            }
        }
    };

    /**
     * 列表点击事件
     */
    @Override
    public void onItemClick(View view) {
        onItemClick(view, mListview.getChildPosition(view));
    }

    protected void onItemClick(View view, int position) {
        PoiInfo item = poiInfoAdapter.getItem(position);
        if (item != null) {
            //定义坐标点
            LatLng point = new LatLng(item.location.latitude, item.location.longitude);
            showMarker(point);
            moveToWithAnimate(point);
        }
    }

    /**
     * SearchView 显示影藏事件
     */
    private MenuItemCompat.OnActionExpandListener actionExpandListener = new MenuItemCompat.OnActionExpandListener() {
        @Override
        public boolean onMenuItemActionExpand(MenuItem item) {
            mMenuSearch.setVisible(true);
            mMenuSure.setVisible(false);
            mListview.setVisibility(View.GONE);
            return true;
        }

        @Override
        public boolean onMenuItemActionCollapse(MenuItem item) {
            mMenuSearch.setVisible(false);
            mMenuSure.setVisible(true);
            mListview.setVisibility(View.VISIBLE);
            return true;
        }
    };

    /**
     * SearchView 输入提交事件
     */
    private SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            searchByAddress(query);
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            mSearchText = newText;
            if (StringUtils.isNotEmpty(newText))
                suggestionCitySearch(mSearchCity, newText);
            return false;
        }
    };

    /**
     * SearchView 联想选择事件
     */
    private SearchView.OnSuggestionListener suggestionListener = new SearchView.OnSuggestionListener() {
        @Override
        public boolean onSuggestionSelect(int position) {
            Cursor cursor = (Cursor) mSearchView.getSuggestionsAdapter().getItem(position);
            String feedName = cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));
            mSearchView.setQuery(feedName, false);
            return true;
        }

        @Override
        public boolean onSuggestionClick(int position) {
            Cursor cursor = (Cursor) mSearchView.getSuggestionsAdapter().getItem(position);
            String feedName = cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));
            mSearchView.setQuery(feedName, false);
            return true;
        }
    };

    /**
     * 改变地图中心位置
     */
    private void onPointChanged(LatLng point) {
        curPoint = point;
        if (point != null)
            reverseGeoCode(point);
    }

    /**
     * 显示其他标记点
     */
    private void showMarker(LatLng point) {
        if (otherMarker != null) otherMarker.remove();
        //构建Marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_ar_tag);
        //构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions()
                .animateType(MarkerOptions.MarkerAnimateType.grow)
                .position(point)
                .icon(bitmap);
        //在地图上添加Marker，并显示
        otherMarker = (Marker) (mBaiduMap.addOverlay(option));
    }

    @Override
    public void onMapStatusChangeFinish(MapStatus mapStatus) {
        super.onMapStatusChangeFinish(mapStatus);
        System.out.println("onMapStatusChangeFinish-> " + mapStatus.toString());
        if (curPoint == null || (curPoint.latitude!=mapStatus.target.latitude && curPoint.longitude!=mapStatus.target.longitude))
            onPointChanged(mapStatus.target);
    }

    @Override
    public void onLocationChanged(MyLocation location) {
        super.onLocationChanged(location);
        if (location == null)
            return;
        //城市
        mSearchCity = location.getmCity();
        //定义坐标点
        LatLng point = new LatLng(location.getmLatitude(), location.getmLongitude());

        //标记当前位置
        //开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        //构造定位数据
        MyLocationData locData = new MyLocationData.Builder()
                .latitude(location.getmLatitude())
                .longitude(location.getmLongitude())
                .build();
        //设置定位数据
        mBaiduMap.setMyLocationData(locData);

        //移动地图中心
        moveTo(point);
        onPointChanged(point);
    }

    @Override
    public void onLocationFail(String provider, String message) {
        super.onLocationFail(provider, message);
    }

    /**
     * 地址检索
     */
    private void searchByAddress(String address) {
        if (StringUtils.isNotEmpty(address)) {
            //隐藏输入键盘
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
            //关闭SearchView
            mSearchView.clearFocus();
            MenuItemCompat.collapseActionView(mMenuSearchView);

            searchCity(address);
        }
    }

    /**
     * 初始化周边检索
     */
    public void searchNearby(String address) {
        mType = TYPE_NEARBY;
        mState = STATE_REFRESH;

        mPageNum = 0;
        mSearchPoint = curPoint;
        mSearchAddress = address;

        poiNearbySearch(curPoint, mSearchAddress, mPageNum);
    }

    /**
     * 初始化城市内检索
     */
    public void searchCity(String address) {
        mType = TYPE_CITY;
        mState = STATE_REFRESH;

        mPageNum = 0;
        mSearchAddress = address;

        poiCitySearch(mSearchCity, mSearchAddress, mPageNum);
    }

    /**
     * 加载更多
     */
    private void loadMore() {
        if (mType == TYPE_NEARBY || mType == TYPE_CITY) {
            if (mState == STATE_NONE) {
                if (poiInfoAdapter.getState() == RecycleBaseAdapter.STATE_LOAD_MORE) {
                    mState = STATE_LOADMORE;
                    mPageNum++;
                    if (mType == TYPE_NEARBY)
                        poiNearbySearch(mSearchPoint, mSearchAddress, mPageNum);
                    else if (mType == TYPE_CITY)
                        poiCitySearch(mSearchCity, mSearchAddress, mPageNum);
                }
            }
        }
    }

    @Override
    protected void onGetPoiSearchSuccess(PoiResult result) {
        System.out.println("检索成功");
        List<PoiInfo> dataList = result.getAllPoi();
        if (mState == STATE_REFRESH)
            poiInfoAdapter.clear();
        poiInfoAdapter.addData(dataList);
        if (dataList.size() == 0 && mState == STATE_REFRESH) {
            poiInfoAdapter.setState(RecycleBaseAdapter.STATE_NO_MORE);
        } else if (dataList.size() < 10) {
            poiInfoAdapter.setState(RecycleBaseAdapter.STATE_NO_MORE);
        } else if (result.getCurrentPageNum() == result.getTotalPageNum()) {
            poiInfoAdapter.setState(RecycleBaseAdapter.STATE_NO_MORE);
        } else {
            poiInfoAdapter.setState(RecycleBaseAdapter.STATE_LOAD_MORE);
        }
    }

    @Override
    protected void onGetPoiSearchFail() {
        System.out.println("检索失败");
        if (mState == STATE_REFRESH)
            poiInfoAdapter.clear();
        poiInfoAdapter.setState(RecycleBaseAdapter.STATE_NETWORK_ERROR);
    }

    @Override
    protected void onGetPoiSearchFinish() {
        mState = STATE_NONE;
    }

    @Override
    protected void onSuggestionSearchSuccess(SuggestionResult result) {
        suggestCursor = new MatrixCursor(QuerySuggestionsAdapter.COLUMNS);
        for (int i = 0; i < result.getAllSuggestions().size(); i++) {
            SuggestionResult.SuggestionInfo info = result.getAllSuggestions().get(i);
            suggestCursor.addRow(new Object[]{i, info.key});
        }
        suggestAdapter.changeCursor(suggestCursor);
    }

    @Override
    protected void onSuggestionSearchFail() {
        suggestCursor = new MatrixCursor(QuerySuggestionsAdapter.COLUMNS);
        suggestAdapter.changeCursor(suggestCursor);
    }

    @Override
    protected void onReverseGeoCodeSuccess(ReverseGeoCodeResult result) {
        System.out.println(result.getAddressDetail().province+result.getAddressDetail().city+result.getAddressDetail().district);
        System.out.println(result.getLocation().toString());
        mSearchCity = result.getAddressDetail().city;
        mTvLocation.setText(result.getAddress());
        MyLocation location = new MyLocation(result);
        mTvLocation.setTag(location);
    }

    @Override
    protected void onReverseGeoCodeFail() {
        Toast.makeText(this, "获取地址信息失败", Toast.LENGTH_LONG).show();
    }

}
