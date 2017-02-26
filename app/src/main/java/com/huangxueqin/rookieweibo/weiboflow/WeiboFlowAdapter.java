package com.huangxueqin.rookieweibo.weiboflow;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.huangxueqin.rookieweibo.R;
import com.huangxueqin.rookieweibo.widget.WeiboStatusView;
import com.sina.weibo.sdk.openapi.models.Status;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by huangxueqin on 2017/2/24.
 */

public class WeiboFlowAdapter extends RecyclerView.Adapter<WeiboFlowAdapter.WeiboFlowHolder> {
    public static final int TYPE_STATUS_SIMPLE = 100;
    public static final int TYPE_STATUS_IMAGE = 101;
    public static final int TYPE_STATUS_VIDEO = 102;
    public static final int TYPE_STATUS_MUSIC = 103;
    public static final int TYPE_STATUS_RETWEET = 104;
    private static final int TYPE_FOOTER = 1000;

    public static final int FOOTER_TYPE_NONE = 0;
    public static final int FOOTER_TYPE_LOADING = 1;
    public static final int FOOTER_TYPE_COMPLETE = 2;

    Context mContext;
    ArrayList<Status> mStatusList;
    int mFooterType = FOOTER_TYPE_NONE;

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

    public void setFooterType(int footerType) {
        mFooterType = footerType;
        notifyDataSetChanged();
    }



    @Override
    public int getItemViewType(int position) {
        if (position >= mStatusList.size()) return TYPE_FOOTER;

        final Status status = mStatusList.get(position);
        if (status.retweeted_status != null) {
            return TYPE_STATUS_RETWEET;
        } else if (!TextUtils.isEmpty(status.original_pic)) {
            return TYPE_STATUS_IMAGE;
        } else {
            return TYPE_STATUS_SIMPLE;
        }
    }

    @Override
    public int getItemCount() {
        return mStatusList.size() + 1;
    }

    private boolean isStatusType(int type) {
        return type >= TYPE_STATUS_SIMPLE && type <= TYPE_STATUS_RETWEET;
    }

    @Override
    public WeiboFlowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (isStatusType(viewType)) {
            return new WeiboFlowHolder(WeiboStatusCard.get(mContext, parent, viewType), viewType);
        } else {
            return new WeiboFlowHolder(LayoutInflater.from(mContext)
                    .inflate(R.layout.view_list_item_weibo_flow_footer, parent, false), viewType);
        }
    }

    @Override
    public void onBindViewHolder(WeiboFlowHolder holder, int position) {
        final int viewType = getItemViewType(position);
        if (isStatusType(viewType)) {
            onBindStatus(holder, position, viewType);
        } else if (viewType == TYPE_FOOTER) {
            onBindFooter(holder, position);
        }
    }

    private void onBindStatus(WeiboFlowHolder holder, int position, int type) {
        Status status = mStatusList.get(position);
        holder.statusView.setStatus(status);
    }

    private void onBindFooter(WeiboFlowHolder holder, int position) {
        holder.loadingView.setVisibility(mFooterType == FOOTER_TYPE_LOADING ? View.VISIBLE : View.GONE);
        holder.noMoreView.setVisibility(mFooterType == FOOTER_TYPE_COMPLETE ? View.VISIBLE : View.GONE);
    }

    public class WeiboFlowHolder extends RecyclerView.ViewHolder {
        View loadingView;
        View noMoreView;
        WeiboStatusView statusView;

        public WeiboFlowHolder(View itemView, int itemType) {
            super(itemView);
            if (itemType == TYPE_FOOTER) {
                loadingView = itemView.findViewById(R.id.loading_view);
                noMoreView = itemView.findViewById(R.id.no_more_prompt);
            } else {
                statusView = ((WeiboStatusCard)itemView).mStatusView;
            }
        }
    }

    private void D(String msg) {
        Log.d("TAG", msg);
    }
}
