package com.huangxueqin.rookieweibo.common.list;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.huangxueqin.rookieweibo.R;

/**
 * Created by huangxueqin on 2017/3/12.
 */

public abstract class EndlessAdapter<VH extends RecyclerView.ViewHolder,
        FH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    protected static final int TYPE_FOOTER = 0x89464174;  // random number

    private boolean mDataCompleted = false;

    public void setDataComplete(boolean complete) {
        if (mDataCompleted != complete) {
            mDataCompleted = complete;
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        return getContentItemCount() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position >= getContentItemCount()) {
            return TYPE_FOOTER;
        } else {
            return getContentItemViewType(position);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_FOOTER) {
            return onCreateFooterHolder(parent);
        } else {
            return onCreateContentViewHolder(parent, viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_FOOTER) {
            onBindFooterHolder((FH) holder, mDataCompleted);
        } else {
            onBindContentViewHolder((VH) holder, position);
        }
    }

    public abstract int getContentItemCount();

    public abstract int getContentItemViewType(int position);

    public abstract VH onCreateContentViewHolder(ViewGroup parent, int viewType);

    public abstract void onBindContentViewHolder(VH holder, int position);

    public abstract FH onCreateFooterHolder(ViewGroup parent);

    public abstract void onBindFooterHolder(FH holder, boolean dataComplete);


    public static class FooterHolder extends RecyclerView.ViewHolder {
        private View loadMoreView;
        private View completeView;

        private FooterHolder(View itemView) {
            super(itemView);
            loadMoreView = itemView.findViewById(R.id.loading_view);
            completeView = itemView.findViewById(R.id.no_more_prompt);
        }

        public static FooterHolder createInstance(Context context, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(context);
            return new FooterHolder(inflater.inflate(R.layout.view_default_list_footer, parent, false));
        }

        public void bind(boolean complete) {
            loadMoreView.setVisibility(complete ? View.GONE : View.VISIBLE);
            completeView.setVisibility(complete ? View.VISIBLE : View.GONE);
        }
    }
}
