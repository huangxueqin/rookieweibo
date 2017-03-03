package com.huangxueqin.rookieweibo.weiboflow;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.huangxueqin.rookieweibo.R;
import com.huangxueqin.rookieweibo.cons.ST;
import com.huangxueqin.rookieweibo.interfaces.WeiboLinkHandler;
import com.sina.weibo.sdk.openapi.models.Status;

import java.util.ArrayList;

/**
 * Created by huangxueqin on 2017/2/24.
 */

public class WeiboFlowAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    interface FT {
        int NONE = 0;
        int LOADING = 1;
        int COMPLETE = 2;
    }

    interface VT {
        int Footer = 0;
        int Card = 1;
    }

    Context mContext;
    ArrayList<Status> mStatusList;
    int mFooterType = FT.NONE;
    WeiboLinkHandler mLinkHandler;
    StatusActionListener mStatusActionListener;

    public WeiboFlowAdapter(Context context) {
        mContext = context;
        mStatusList = new ArrayList<>();
    }

    public void setLinkHandler(WeiboLinkHandler linkHandler) {
        mLinkHandler = linkHandler;
    }

    public void setStatusActionListener(StatusActionListener listener) {
        mStatusActionListener = listener;
    }

    public void append(ArrayList<Status> statuses) {
        mStatusList.addAll(statuses);
        notifyDataSetChanged();
    }

    public void refresh(ArrayList<Status> statuses) {
        mStatusList.clear();
        mStatusList.addAll(statuses);
        notifyDataSetChanged();
    }

    public void setDataComplete() {
        mFooterType = FT.COMPLETE;
        notifyDataSetChanged();
    }

    public void setDataInComplete() {
        mFooterType = FT.LOADING;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (position >= mStatusList.size()) {
            return VT.Footer;
        } else {
            final Status status = mStatusList.get(position);
            final int statusType = ST.getTypeForStatus(status);
            return VT.Card | (statusType << 16);
        }
    }

    @Override
    public int getItemCount() {
        return mStatusList.size() + 1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        if (viewType == VT.Footer) {
            itemView = inflater.inflate(R.layout.view_list_item_weibo_flow_footer, parent, false);
            return new LoadingViewHolder(itemView);
        } else {
            final int statusType = viewType >> 16;
            itemView = inflater.inflate(R.layout.view_weibo_status_card, parent, false);
            return new StatusCardHolder(itemView, mContext, statusType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final int viewType = getItemViewType(position);
        if (viewType == VT.Footer) {
            onBindFooter((LoadingViewHolder) holder, position);
        } else {
            onBindStatus((StatusCardHolder) holder, position);
        }
    }

    private void onBindStatus(StatusCardHolder holder, int position) {
        Status status = mStatusList.get(position);
        holder.setLinkHandler(mLinkHandler);
        holder.setStatusActionListener(mStatusActionListener);
        holder.setup(status);
    }

    private void onBindFooter(LoadingViewHolder holder, int position) {
        holder.loadingView.setVisibility(mFooterType == FT.LOADING ? View.VISIBLE : View.GONE);
        holder.noMoreView.setVisibility(mFooterType == FT.COMPLETE ? View.VISIBLE : View.GONE);
    }

    static class LoadingViewHolder extends RecyclerView.ViewHolder {
        View loadingView;
        View noMoreView;

        public LoadingViewHolder(View itemView) {
            super(itemView);
            loadingView = itemView.findViewById(R.id.loading_view);
            noMoreView = itemView.findViewById(R.id.no_more_prompt);
        }
    }

    private void D(String msg) {
        Log.d("TAG", msg);
    }
}
