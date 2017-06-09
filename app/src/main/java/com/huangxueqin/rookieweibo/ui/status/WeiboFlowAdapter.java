package com.huangxueqin.rookieweibo.ui.status;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.huangxueqin.rookieweibo.R;
import com.huangxueqin.rookieweibo.common.StatusUtils;
import com.huangxueqin.rookieweibo.interfaces.StatusLinkHandler;
import com.huangxueqin.rookieweibo.interfaces.StatusListener;
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
    StatusListener mStatusListener;
    StatusLinkHandler mLinkHandler;

    public WeiboFlowAdapter(Context context) {
        mContext = context;
        mStatusList = new ArrayList<>();
    }

    public void setStatusActionListener(StatusListener listener) {
        mStatusListener = listener;
    }

    public void setLinkHandler(StatusLinkHandler handler) {
        mLinkHandler = handler;
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
            final int statusType = StatusUtils.getStatusType(status);
            return VT.Card | (statusType << 16);
        }
    }

    @Override
    public int getItemCount() {
        return mStatusList.size() + 1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        if (viewType == VT.Footer) {
            final View itemView = inflater.inflate(R.layout.view_default_list_footer, parent, false);
            return new LoadingViewHolder(itemView);
        } else {
            final View itemView = inflater.inflate(R.layout.view_list_status_item, parent, false);
            return new StatusHolder(itemView, (viewType >> 16), mStatusListener, mLinkHandler);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final int viewType = getItemViewType(position);
        if (viewType == VT.Footer) {
            onBindFooter((LoadingViewHolder) holder, position);
        } else {
            onBindStatus((StatusHolder) holder, position);
        }
    }

    private void onBindStatus(StatusHolder holder, int position) {
        Status status = mStatusList.get(position);
        holder.setStatus(status);
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
