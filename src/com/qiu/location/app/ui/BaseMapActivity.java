package com.qiu.location.app.ui;

import android.os.Bundle;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.fallenpanda.location.bean.MyLocation;
import com.fallenpanda.location.manager.MainLocationListener;
import com.fallenpanda.location.manager.MainLocationManager;
import com.fallenpanda.map.MyMapView;
import com.qiu.location.app.R;

import butterknife.Bind;

/**
 * 地图 - 基类
 * ============================================================================
 * 版权所有 2015
 *
 * @author fallenpanda
 * @version 1.0 2015/10/26
 * ============================================================================
 */
public class BaseMapActivity extends BaseActivity implements MainLocationListener, BaiduMap.OnMapStatusChangeListener {

    //-----------------------------------------------
    //地图
    protected static final float DEFAULT_ZOOMLEVEL = 17;//默认标注移动缩放级别

    @Bind(R.id.m_view_map)
    protected MyMapView mViewMap;

    protected BaiduMap mBaiduMap;//地图
    protected UiSettings mUiSettings;//地图设置
    //-----------------------------------------------
    //定位
    protected MainLocationManager mainLocationManager;//定位服务
    //-----------------------------------------------
    //Poi查询
    protected PoiSearch mPoiSearch;//Poi检索服务
    protected SuggestionSearch mSuggestionSearch;//建议查询
    protected GeoCoder mGeoCoder;//位置解析

    protected static final int MAX_SIZE = 10;//每页数
    protected int mPageNum = 0;//页

    protected static final int MAX_RADIUS = 10;//周边检索 - 检索半径
    protected LatLng mSearchPoint;//周边检索 - 中心位置

    protected String mSearchCity;//城市内检索 - 城市
    protected String mSearchAddress;//城市内检索 - 关键字
    //-----------------------------------------------

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPoiSearch!=null)
            mPoiSearch.destroy();
        if (mSuggestionSearch!=null)
            mSuggestionSearch.destroy();
        if (mGeoCoder!=null)
            mGeoCoder.destroy();
        mViewMap.onDestroy();
        mainLocationManager.stopBaiduLocation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mViewMap.onResume();
        mainLocationManager.registerListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mViewMap.onPause();
        mainLocationManager.unregisterListener(this);
    }

    /**
     * 是否需要初始化Poi检索服务
     */
    public boolean isNeedPoiSearch() {
        return false;
    }

    /**
     * 是否需要初始化建议查询
     */
    public boolean isNeedSuggestionSearch() {
        return false;
    }

    /**
     * 是否需要初始化位置解析
     */
    public boolean isNeedGeoCoder() {
        return false;
    }

    /**
     * 初始化地图参数
     */
    protected void initMapView() {

        mainLocationManager = MainLocationManager.getInstance();
        if (isNeedPoiSearch()) {
            mPoiSearch = PoiSearch.newInstance();
            mPoiSearch.setOnGetPoiSearchResultListener(new MyOnGetPoiSearchResultListener());
        }
        if (isNeedSuggestionSearch()) {
            mSuggestionSearch = SuggestionSearch.newInstance();
            mSuggestionSearch.setOnGetSuggestionResultListener(new MyOnGetSuggestionResultListener());
        }
        if (isNeedGeoCoder()) {
            mGeoCoder = GeoCoder.newInstance();
            mGeoCoder.setOnGetGeoCodeResultListener(new MyOnGetGeoCoderResultListener());
        }

        mBaiduMap = mViewMap.getMap();

        //设置地图初始化时的地图状态， 默认地图中心点为北京天安门，缩放级别为 12.0f
//        mBaiduMap.getMapStatus(mapStatus);
        //设置地图模式，默认普通地图
        //普通地图
//        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        //卫星地图
//        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
        //设置地图状态监听者
//        mBaiduMap.setOnMapStatusChangeListener(mapStatusChangeListener);

        mViewMap.setOnMapStatusChangeListener(this);

        //地图设置
        mUiSettings = mBaiduMap.getUiSettings();
        //设置是否允许指南针，默认允许。
//        mUiSettings.setCompassEnabled(false);
        //设置是否允许所有手势操作，默认允许
//        mUiSettings.setAllGesturesEnabled(false);
        //设置是否允许俯视手势，默认允许
        mUiSettings.setOverlookingGesturesEnabled(false);
        //设置是否允许旋转手势，默认允许
        mUiSettings.setRotateGesturesEnabled(false);
        //设置是否允许拖拽手势，默认允许
//        mUiSettings.setScrollGesturesEnabled(false);
        //设置是否允许缩放手势，默认允许
//        mUiSettings.setZoomGesturesEnabled(false);

    }

    //-----------------------------------------------
    //定位回调
    @Override
    public void onLocationChanged(MyLocation location) {

    }

    @Override
    public void onLocationFail(String provider, String message) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
    //-----------------------------------------------
    //地图状态回调
    @Override
    public void onMapStatusChangeStart(MapStatus mapStatus) {

    }

    @Override
    public void onMapStatusChange(MapStatus mapStatus) {

    }

    @Override
    public void onMapStatusChangeFinish(MapStatus mapStatus) {

    }
    //-----------------------------------------------
    //地图操作
    /**
     * 移动地图中心点
     * 不会触发 onMapStatusChangeFinish()
     */
    protected void moveTo(LatLng point) {
        //定义地图状态
        MapStatus mMapStatus = new MapStatus.Builder()
                .target(point)
                .zoom(mViewMap.getCurZoomLevel() < DEFAULT_ZOOMLEVEL ? DEFAULT_ZOOMLEVEL : mViewMap.getCurZoomLevel())
                .build();
        //定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
        //改变地图状态
        mBaiduMap.setMapStatus(mMapStatusUpdate);
    }
    /**
     * 移动地图中心点（动画）
     * 会触发 onMapStatusChangeFinish()
     */
    protected void moveToWithAnimate(LatLng point) {
        //定义地图状态
        MapStatus mMapStatus = new MapStatus.Builder()
                .target(point)
                .zoom(mViewMap.getCurZoomLevel() < DEFAULT_ZOOMLEVEL ? DEFAULT_ZOOMLEVEL : mViewMap.getCurZoomLevel())
                .build();
        //定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
        //改变地图状态
        mBaiduMap.animateMapStatus(mMapStatusUpdate);
    }
    //-----------------------------------------------
    //Poi检索
    /**
     * 发起周边检索请求
     */
    protected void poiNearbySearch(LatLng location, String keyword,int pageNum) {
        if (mPoiSearch == null) return;
        System.out.println("poiNearbySearch()-> " + location.toString() + "|" + keyword + "|" + MAX_RADIUS + "|" + pageNum);
        mPoiSearch.searchNearby((new PoiNearbySearchOption())
                .location(location)
                .keyword(keyword)
                .radius(MAX_RADIUS)
                .pageCapacity(MAX_SIZE)
                .pageNum(pageNum));
    }

    /**
     * 发起城市内检索请求
     */
    protected void poiCitySearch(String city, String keyword,int pageNum) {
        if (mPoiSearch == null) return;
        System.out.println("poiCitySearch()-> " + city + "|" + keyword + "|" + pageNum);
        mPoiSearch.searchInCity((new PoiCitySearchOption())
                .city(city)
                .keyword(keyword)
                .pageCapacity(MAX_SIZE)
                .pageNum(pageNum));
    }

    private class MyOnGetPoiSearchResultListener implements OnGetPoiSearchResultListener {

        @Override
        public void onGetPoiResult(PoiResult result) {
            //获取POI检索结果
            if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                //检索失败
                onGetPoiSearchFail();
            } else {
                //检索成功
                onGetPoiSearchSuccess(result);
            }
            onGetPoiSearchFinish();
        }

        @Override
        public void onGetPoiDetailResult(PoiDetailResult result) {
            //获取Place详情页检索结果
            if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                //检索失败
                onGetPoiDetailSearchFail();
            } else {
                //检索成功
                onGetPoiDetailSearchSuccess(result);
            }
            onGetPoiDetailSearchFinish();
        }

    }

    protected void onGetPoiSearchSuccess(PoiResult result) {
        System.out.println("检索成功");
    }

    protected void onGetPoiSearchFail() {
        System.out.println("检索失败");
    }

    protected void onGetPoiSearchFinish() {
        System.out.println("检索完成");
    }

    protected void onGetPoiDetailSearchSuccess(PoiDetailResult result) {
        System.out.println("检索成功");
    }

    protected void onGetPoiDetailSearchFail() {
        System.out.println("检索失败");
    }

    protected void onGetPoiDetailSearchFinish() {
        System.out.println("检索完成");
    }
    //-----------------------------------------------
    //建议查询
    /**
     * 发起城市内建议查询请求
     */
    protected void suggestionCitySearch(String city, String keyword) {
        if (mSuggestionSearch == null) return;
        System.out.println("suggestionCitySearch()-> " + city + "|" + keyword);
        mSuggestionSearch.requestSuggestion((new SuggestionSearchOption())
                .city(city)
                .keyword(keyword));
    }

    private class MyOnGetSuggestionResultListener implements OnGetSuggestionResultListener {

        @Override
        public void onGetSuggestionResult(SuggestionResult result) {
            //获取在线建议查询结果
            if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR || result.getAllSuggestions() == null) {
                //检索失败
                onSuggestionSearchFail();
            } else {
                //检索成功
                onSuggestionSearchSuccess(result);
            }
            onSuggestionSearchFinish();
        }

    }

    protected void onSuggestionSearchSuccess(SuggestionResult result) {
        System.out.println("检索成功");
    }

    protected void onSuggestionSearchFail() {
        System.out.println("检索失败");
    }

    protected void onSuggestionSearchFinish() {
        System.out.println("检索完成");
    }
    //-----------------------------------------------
    //地理位置反解析
    /**
     * 发起坐标反解析请求
     */
    protected void reverseGeoCode(LatLng location) {
        if (mGeoCoder == null) return;
        System.out.println("reverseGeoCode()-> " + location);
        mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption()
                .location(location));
    }

    /**
     * 发起坐标反解析请求
     */
    protected void geoCode(String city, String address) {
        if (mGeoCoder == null) return;
        System.out.println("geoCode()-> " + city + "|" + address);
        mGeoCoder.geocode(new GeoCodeOption()
                .city(city)
                .address(address));
    }

    private class MyOnGetGeoCoderResultListener implements OnGetGeoCoderResultListener {

        public void onGetGeoCodeResult(GeoCodeResult result) {
            //获取地址反解析结果
            if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                //没有检索到结果
                onGeoCodeFail();
            } else {
                //获取地理编码结果
                onGeoCodeSuccess(result);
            }
            onGeoCodeFinish();
        }

        @Override
        public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
            //获取坐标反解析结果
            if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                //没有找到检索结果
                onReverseGeoCodeFail();
            } else {
                //获取地址结果
                onReverseGeoCodeSuccess(result);
            }
            onReverseGeoCodeFinish();
        }

    }

    protected void onGeoCodeSuccess(GeoCodeResult result) {
        System.out.println("解析成功");
    }

    protected void onGeoCodeFail() {
        System.out.println("解析失败");
    }

    protected void onGeoCodeFinish() {
        System.out.println("解析完成");
    }

    protected void onReverseGeoCodeSuccess(ReverseGeoCodeResult result) {
        System.out.println("解析成功");
    }

    protected void onReverseGeoCodeFail() {
        System.out.println("解析失败");
    }

    protected void onReverseGeoCodeFinish() {
        System.out.println("解析完成");
    }

}
