package com.huangxueqin.rookieweibo.ui.repost;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.huangxueqin.rookieweibo.common.list.EndlessAdapter;

/**
 * Created by huangxueqin on 2017/3/31.
 */

public class RepostListAdapter extends EndlessAdapter<RepostListAdapter.RepostHolder, EndlessAdapter.FooterHolder> {



    @Override
    public int getContentItemCount() {
        return 0;
    }

    @Override
    public int getContentItemViewType(int position) {
        return 0;
    }

    @Override
    public RepostHolder onCreateContentViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindContentViewHolder(RepostHolder holder, int position) {

    }

    @Override
    public FooterHolder onCreateFooterHolder(ViewGroup parent) {
        return null;
    }

    @Override
    public void onBindFooterHolder(FooterHolder holder, boolean dataComplete) {

    }

    public static class RepostHolder extends RecyclerView.ViewHolder {

        public RepostHolder(View itemView) {
            super(itemView);
        }
    }
}
