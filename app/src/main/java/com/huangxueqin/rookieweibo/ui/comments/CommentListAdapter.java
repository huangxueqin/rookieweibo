package com.huangxueqin.rookieweibo.ui.comments;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.huangxueqin.rookieweibo.common.list.EndlessAdapter;
import com.huangxueqin.rookieweibo.R;
import com.huangxueqin.rookieweibo.common.utils.StatusUtils;
import com.huangxueqin.rookieweibo.ui.widget.StatusTextView;
import com.sina.weibo.sdk.openapi.models.Comment;

import java.util.ArrayList;

/**
 * Created by huangxueqin on 2017/3/12.
 */

public class CommentListAdapter extends EndlessAdapter<CommentListAdapter.CommentHolder, EndlessAdapter.FooterHolder> {
    private static final int TYPE_COMMENT = 1;

    Context mContext;
    LayoutInflater mInflater;
    ArrayList<Comment> mComments;

    public CommentListAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mComments = new ArrayList<>();
    }

    public void appendComment(ArrayList<Comment> comments) {
        mComments.addAll(comments);
        notifyDataSetChanged();
    }

    @Override
    public int getContentItemCount() {
        return mComments.size();
    }

    @Override
    public int getContentItemViewType(int position) {
        return TYPE_COMMENT;
    }

    @Override
    public CommentHolder onCreateContentViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.view_list_comment_item, parent, false);
        return new CommentHolder(itemView);
    }

    @Override
    public void onBindContentViewHolder(CommentHolder holder, int position) {
        Comment comment = mComments.get(position);
        holder.userName.setText(comment.user.screen_name);
        Glide.with(mContext).load(comment.user.avatar_hd).into(holder.userAvatar);
        holder.createTime.setText(StatusUtils.parseCreateTime(comment.created_at));
        holder.commentText.setText(comment.text);
    }

    @Override
    public FooterHolder onCreateFooterHolder(ViewGroup parent) {
        return FooterHolder.createInstance(mContext, parent);
    }

    @Override
    public void onBindFooterHolder(FooterHolder holder, boolean dataComplete) {
        holder.bind(dataComplete);
    }

    public static class CommentHolder extends RecyclerView.ViewHolder {
        ImageView userAvatar;
        TextView userName;
        TextView createTime;
        StatusTextView commentText;

        public CommentHolder(View itemView) {
            super(itemView);
            userAvatar = (ImageView) itemView.findViewById(R.id.user_avatar);
            userName = (TextView) itemView.findViewById(R.id.user_name);
            createTime = (TextView) itemView.findViewById(R.id.weibo_create_time);
            commentText = (StatusTextView) itemView.findViewById(R.id.comment_text);
        }
    }
}
