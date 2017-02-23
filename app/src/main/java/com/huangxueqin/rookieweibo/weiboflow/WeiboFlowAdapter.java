package com.huangxueqin.rookieweibo.weiboflow;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by huangxueqin on 2017/2/24.
 */

public class WeiboFlowAdapter extends RecyclerView.Adapter<WeiboFlowAdapter.WeiboFlowHolder> {

    @Override
    public WeiboFlowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(WeiboFlowHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class WeiboFlowHolder extends RecyclerView.ViewHolder {

        public WeiboFlowHolder(View itemView) {
            super(itemView);
        }
    }
}
