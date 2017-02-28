package com.huangxueqin.rookieweibo.weiboflow;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.huangxueqin.rookieweibo.R;
import com.huangxueqin.rookieweibo.ST;
import com.huangxueqin.rookieweibo.StatusUtils;
import com.huangxueqin.rookieweibo.widget.WeiboStatusView;
import com.sina.weibo.sdk.openapi.models.Status;

import java.util.ArrayList;

/**
 * Created by huangxueqin on 2017/2/24.
 */

public class WeiboFlowAdapter extends RecyclerView.Adapter<WeiboFlowAdapter.WeiboFlowHolder> {
    interface FT {
        int NONE = 0;
        int LOADING = 1;
        int COMPLETE = 2;
    }

    interface VT {
        int Footer = 1;
        int Card = 2;
    }

    Context mContext;
    ArrayList<Status> mStatusList;
    int mFooterType = FT.NONE;

    public WeiboFlowAdapter(Context context) {
        mContext = context;
        mStatusList = new ArrayList<>();
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
            return VT.Card | (ST.getStatusType(mStatusList.get(position)) << 16);
        }
    }

    @Override
    public int getItemCount() {
        return mStatusList.size() + 1;
    }

    @Override
    public WeiboFlowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView;
        if (viewType == VT.Footer) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            itemView = inflater.inflate(R.layout.view_list_item_weibo_flow_footer, parent, false);
        } else {
            final int statusType = (viewType >> 16);
            itemView = WeiboStatusCard.get(mContext, parent, statusType);
        }
        return new WeiboFlowHolder(itemView, viewType);
    }

    @Override
    public void onBindViewHolder(WeiboFlowHolder holder, int position) {
        final int viewType = getItemViewType(position);
        if (viewType == VT.Footer) {
            onBindFooter(holder, position);
        } else {
            onBindStatus(holder, position, viewType);
        }
    }

    private void onBindStatus(WeiboFlowHolder holder, int position, int type) {
        Status status = mStatusList.get(position);
        holder.statusView.setStatus(status);
    }

    private void onBindFooter(WeiboFlowHolder holder, int position) {
        holder.loadingView.setVisibility(mFooterType == FT.LOADING ? View.VISIBLE : View.GONE);
        holder.noMoreView.setVisibility(mFooterType == FT.COMPLETE ? View.VISIBLE : View.GONE);
    }

    public class WeiboFlowHolder extends RecyclerView.ViewHolder {
        View loadingView;
        View noMoreView;

        WeiboStatusView statusView;

        public WeiboFlowHolder(View itemView, int itemType) {
            super(itemView);
            if (itemType == VT.Footer) {
                loadingView = itemView.findViewById(R.id.loading_view);
                noMoreView = itemView.findViewById(R.id.no_more_prompt);
            } else {
                WeiboStatusCard statusCard = (WeiboStatusCard) itemView;
                statusView = statusCard.getStatusView();
            }
        }
    }

    private void D(String msg) {
        Log.d("TAG", msg);
    }
}
