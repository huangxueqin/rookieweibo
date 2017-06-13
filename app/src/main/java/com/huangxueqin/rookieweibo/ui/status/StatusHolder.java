package com.huangxueqin.rookieweibo.ui.status;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.huangxueqin.rookieweibo.R;
import com.huangxueqin.rookieweibo.cons.StatusAction;
import com.huangxueqin.rookieweibo.cons.StatusType;
import com.huangxueqin.rookieweibo.common.StatusUtils;
import com.huangxueqin.rookieweibo.interfaces.StatusLinkHandler;
import com.huangxueqin.rookieweibo.interfaces.StatusListener;
import com.huangxueqin.rookieweibo.ui.widget.StatusTextView;
import com.huangxueqin.rookieweibo.ui.widget.WeiboImageGrid;
import com.sina.weibo.sdk.openapi.models.Status;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;

/**
 * Created by huangxueqin on 2017/3/12.
 */

public class StatusHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    // user info
    @BindView(R.id.user_avatar)
    ImageView userAvatar;
    @BindView(R.id.user_name)
    TextView userName;
    @BindView(R.id.weibo_create_time)
    TextView createTime;
    @BindView(R.id.status_opt_menu)
    View optMenu;

    // status card footer
    @BindView(R.id.attitudes_count)
    TextView attitudesCount;
    @BindView(R.id.reposts_count)
    TextView repostsCount;
    @BindView(R.id.comments_count)
    TextView commentsCount;

    // status body
    @BindView(R.id.status_content_text)
    StatusTextView statusText;
    @Nullable @BindView(R.id.image_grid)
    WeiboImageGrid statusImages;
    @Nullable @BindView(R.id.rt_content_text)
    StatusTextView rtStatusText;
    @Nullable @BindView(R.id.rt_image_grid)
    WeiboImageGrid rtStatusImages;

    final int statusType;
    final StatusListener listener;
    final StatusLinkHandler handler;
    Status status;

    public StatusHolder(View itemView,
                        int statusType,
                        StatusListener listener,
                        StatusLinkHandler handler) {
        super(itemView);

        this.statusType = statusType;
        this.listener = listener;
        this.handler = handler;

        int viewStubId = -1;
        if (statusType == StatusType.IMAGE) {
            viewStubId = R.id.stub_status_content_image;
        } else if (statusType == StatusType.RT_SIMPLE) {
            viewStubId = R.id.stub_status_conent_simple_retweet;
        } else if (statusType == StatusType.RT_IMAGE) {
            viewStubId = R.id.stub_status_content_image_retweet;
        }
        if (viewStubId > 0) {
            ViewStub stub = (ViewStub) itemView.findViewById(viewStubId);
            stub.setVisibility(View.VISIBLE);
        }
        ButterKnife.bind(this, itemView);

        statusText.setLinkHandler(handler);
        if (rtStatusText != null) {
            rtStatusText.setLinkHandler(handler);
        }
    }

    public void setStatus(Status status) {
        this.status = status;
        Glide.with(itemView.getContext()).load(status.user.avatar_hd).into(userAvatar);
        userName.setText(status.user.screen_name);
        createTime.setText(StatusUtils.parseCreateTime(status.created_at));
        attitudesCount.setText(String.valueOf(status.attitudes_count));
        repostsCount.setText(String.valueOf(status.reposts_count));
        commentsCount.setText(String.valueOf(status.comments_count));
        statusText.setText(status.text);
        if (statusType == StatusType.IMAGE) {
            statusImages.setImage(StatusUtils.getMiddlePics(status));
        } else if (statusType == StatusType.RT_SIMPLE) {
            rtStatusText.setText(status.retweeted_status.text);
        } else if (statusType == StatusType.RT_IMAGE) {
            rtStatusText.setText(status.retweeted_status.text);
            rtStatusImages.setImage(StatusUtils.getMiddlePics(status.retweeted_status));
        }
    }

    @Optional
    @OnClick ({
            R.id.user_avatar,R.id.user_name,
            R.id.status_content, R.id.status_content_text, R.id.rt_status_content, R.id.rt_content_text,
            R.id.image_grid, R.id.rt_image_grid,
            R.id.attitudes_count,
            R.id.comments_count,
            R.id.reposts_count,
            R.id.status_opt_menu,
    })
    @Override
    public void onClick(View v) {
        if (status == null || listener == null) return;

        switch (v.getId()) {
            case R.id.user_avatar:
            case R.id.user_name:
                listener.performAction(StatusAction.GO_USER, status.user);
                break;
            case R.id.status_content:
            case R.id.status_content_text:
                listener.performAction(StatusAction.GO_STATUS, status);
                break;
            case R.id.rt_status_content:
            case R.id.rt_content_text:
                listener.performAction(StatusAction.GO_STATUS, status.retweeted_status);
                break;
            case R.id.image_grid:
                listener.performAction(StatusAction.GO_GALLERY,
                        StatusUtils.getLargePics(status),
                        ((WeiboImageGrid)v).getLastClickChildIndex());
                break;
            case R.id.rt_image_grid:
                listener.performAction(StatusAction.GO_GALLERY,
                        StatusUtils.getLargePics(status.retweeted_status),
                        ((WeiboImageGrid)v).getLastClickChildIndex());
                break;
            case R.id.comments_count:
                listener.performAction(StatusAction.COMMENT, status);
                break;
            case R.id.reposts_count:
                listener.performAction(StatusAction.REPOST, status);
                break;
            case R.id.attitudes_count:
                listener.performAction(StatusAction.ATTITUDE, status);
                break;
        }
    }
}
