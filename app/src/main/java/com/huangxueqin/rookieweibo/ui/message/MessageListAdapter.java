package com.huangxueqin.rookieweibo.ui.message;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.huangxueqin.rookieweibo.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by huangxueqin on 2017/4/4.
 */

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.ViewHolder> {

    private Context mContext;

    public MessageListAdapter(Context context) {
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View itemView = inflater.inflate(R.layout.view_message_list_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (position == 0) {
            holder.icon.setImageResource(R.mipmap.messagescenter_at);
            holder.title.setText(R.string.message_at);
        } else if (position == 1) {
            holder.icon.setImageResource(R.mipmap.messagescenter_comments);
            holder.title.setText(R.string.message_comments);
        } else if (position == 2) {
            holder.icon.setImageResource(R.mipmap.messagescenter_good);
            holder.title.setText(R.string.message_like);
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.icon)
        ImageView icon;
        @BindView(R.id.title)
        TextView title;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
