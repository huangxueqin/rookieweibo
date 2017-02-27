package com.huangxueqin.rookieweibo.weiboflow;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.huangxueqin.rookieweibo.R;
import com.huangxueqin.rookieweibo.StatusUtils;
import com.huangxueqin.rookieweibo.widget.WeiboStatusView;
import com.sina.weibo.sdk.openapi.models.Status;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by huangxueqin on 2017/2/24.
 */

public class WeiboFlowAdapter extends RecyclerView.Adapter<WeiboFlowAdapter.WeiboFlowHolder> {
    interface FooterType {
        int NONE = 0;
        int LOADING = 1;
        int COMPLETE = 2;
    }

    private static final int VIEW_TYPE_FOOTER = 0xBABEBABE;

    Context mContext;
    ArrayList<Status> mStatusList;
    int mFooterType = FooterType.NONE;

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
        mFooterType = FooterType.COMPLETE;
        notifyDataSetChanged();
    }

    public void setDataInComplete() {
        mFooterType = FooterType.LOADING;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (position >= mStatusList.size()) {
            return VIEW_TYPE_FOOTER;
        } else {
            return StatusUtils.getType(mStatusList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return mStatusList.size() + 1;
    }

    @Override
    public WeiboFlowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView;
        if (viewType == VIEW_TYPE_FOOTER) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            itemView = inflater.inflate(R.layout.view_list_item_weibo_flow_footer, parent, false);
        } else {
            itemView = WeiboStatusCard.get(mContext, parent, viewType);
        }
        return new WeiboFlowHolder(itemView, viewType);
    }

    @Override
    public void onBindViewHolder(WeiboFlowHolder holder, int position) {
        final int viewType = getItemViewType(position);
        if (viewType == VIEW_TYPE_FOOTER) {
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
        holder.loadingView.setVisibility(mFooterType == FooterType.LOADING ? View.VISIBLE : View.GONE);
        holder.noMoreView.setVisibility(mFooterType == FooterType.COMPLETE ? View.VISIBLE : View.GONE);
    }

    public class WeiboFlowHolder extends RecyclerView.ViewHolder {
        View loadingView;
        View noMoreView;

        WeiboStatusView statusView;

        public WeiboFlowHolder(View itemView, int itemType) {
            super(itemView);
            if (itemType == VIEW_TYPE_FOOTER) {
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
