package com.qiu.location.app.ui.main.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.mapapi.search.core.PoiInfo;
import com.qiu.location.app.R;
import com.qiu.location.app.base.RecycleBaseAdapter;
import com.qiu.location.app.utils.StringUtils;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * poi信息列表
 * ============================================================================
 * 版权所有 2015
 *
 * @author fallenpanda
 * @version 1.0 2015/10/21
 * ============================================================================
 */
public class PoiInfoAdapter extends RecycleBaseAdapter<PoiInfo> {

    @Override
    protected View onCreateItemView(ViewGroup parent, int viewType) {
        return getLayoutInflater(parent.getContext()).inflate(
                R.layout.list_cell_text, null);
    }

    @Override
    protected RecycleBaseAdapter.ViewHolder onCreateItemViewHolder(View view, int viewType) {
        return new ViewHolder(viewType,view);
    }

    @Override
    protected void onBindItemViewHolder(RecycleBaseAdapter.ViewHolder holder, int position) {
        super.onBindItemViewHolder(holder, position);
        ViewHolder vh = (ViewHolder)holder;

        final PoiInfo item = _data.get(position);

        vh.mTvTextName.setText(item.name);

        if(StringUtils.isNotEmpty(item.address)){
            vh.mTvTextRemark.setText(item.address);
        }

    }

    private static class ViewHolder extends RecycleBaseAdapter.ViewHolder {
        public TextView mTvTextName, mTvTextRemark;

        public ViewHolder(int viewType, View view) {
            super(viewType,view);
            mTvTextName = ((TextView)view.findViewById(R.id.text_listitem_name));
            mTvTextRemark = ((TextView)view.findViewById(R.id.text_listitem_remark));
        }
    }

}
