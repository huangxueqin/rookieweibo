package com.huangxueqin.rookieweibo.weiboflow;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.huangxueqin.rookieweibo.R;
import com.sina.weibo.sdk.openapi.models.Status;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by huangxueqin on 2017/2/24.
 */

public class WeiboFlowAdapter extends RecyclerView.Adapter<WeiboFlowAdapter.WeiboFlowHolder> {

    ArrayList<Status> mStatusList;
    Context mContext;

    public WeiboFlowAdapter(Context context) {
        mContext = context;
        mStatusList = new ArrayList<>();
    }

    public void append(ArrayList<Status> statuses) {
        mStatusList.addAll(statuses);
        notifyDataSetChanged();
    }

    public void replace(ArrayList<Status> statuses) {
        mStatusList.clear();
        mStatusList.addAll(statuses);
        notifyDataSetChanged();
    }

    @Override
    public WeiboFlowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View itemView = inflater.inflate(R.layout.view_weibo_flow_list_item, parent, false);
        return new WeiboFlowHolder(itemView);
    }

    @Override
    public void onBindViewHolder(WeiboFlowHolder holder, int position) {
        holder.contentText.setText(mStatusList.get(position).text);
    }

    @Override
    public int getItemCount() {
        return mStatusList.size();
    }

    public class WeiboFlowHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.content) TextView contentText;

        public WeiboFlowHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
