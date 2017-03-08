package com.huangxueqin.rookieweibo.weiboflow;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.huangxueqin.rookieweibo.R;
import com.huangxueqin.rookieweibo.cons.ST;
import com.huangxueqin.rookieweibo.weiboViewModel.WeiboLinkHandler;
import com.huangxueqin.rookieweibo.utils.StatusUtils;
import com.huangxueqin.rookieweibo.weiboViewModel.WeiboActionListener;
import com.huangxueqin.rookieweibo.weiboflow.extra.ImageExtra;
import com.huangxueqin.rookieweibo.weiboflow.extra.RtImageExtra;
import com.huangxueqin.rookieweibo.weiboflow.extra.RtSimpleExtra;
import com.huangxueqin.rookieweibo.weiboflow.extra.ViewExtra;
import com.huangxueqin.rookieweibo.widget.StatusTextView;
import com.sina.weibo.sdk.openapi.models.Status;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by huangxueqin on 2017/3/3.
 */

public class StatusCardHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    @BindView(R.id.weibo_card_content)
    View weiboContentView;

    // header
    @BindView(R.id.user_avatar)
    ImageView userAvatar;
    @BindView(R.id.user_name)
    TextView userName;
    @BindView(R.id.weibo_create_time)
    TextView createTime;
    @BindView(R.id.weibo_opt_menu)
    View optMenu;

    // body
    @BindView(R.id.status_text)
    StatusTextView statusText;
    @BindView(R.id.status_decorator)
    FrameLayout extraContainer;

    // footer
    @BindView(R.id.weibo_like_num)
    TextView attitudesCount;
    @BindView(R.id.weibo_forward_num)
    TextView repostsCount;
    @BindView(R.id.weibo_comment_num)
    TextView commentsCount;

    private final Context mContext;
    private final ViewExtra mViewExtra;
    private Status mCurrentStatus;
    private WeiboActionListener mStatusActionListener;
    private WeiboLinkHandler mLinkHandler;

    public StatusCardHolder(View itemView, Context context, int statusType) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        mContext = context;
        LayoutInflater inflater = LayoutInflater.from(context);
        final int layoutId;
        switch (statusType) {
            case ST.IMAGE:
                layoutId = R.layout.view_weibo_status_extra_image;
                mViewExtra = new ImageExtra(inflater.inflate(layoutId, extraContainer, false));
                break;
            case ST.RT_SIMPLE:
                layoutId = R.layout.view_weibo_status_extra_rt_simple;
                mViewExtra = new RtSimpleExtra(inflater.inflate(layoutId, extraContainer, false));
                break;
            case ST.RT_IMAGE:
                layoutId = R.layout.view_weibo_status_extra_rt_image;
                mViewExtra = new RtImageExtra(inflater.inflate(layoutId, extraContainer, false));
                break;
            default:
                layoutId = 0;
                mViewExtra = null;
                break;
        }
        if (layoutId <= 0) {
            extraContainer.setVisibility(View.GONE);
        } else {
            mViewExtra.setOnClickListener(this);
            extraContainer.addView(mViewExtra.itemView);
        }
    }

    public void setStatusActionListener(WeiboActionListener listener) {
        mStatusActionListener = listener;
    }

    public void setLinkHandler(WeiboLinkHandler linkHandler) {
        if (mLinkHandler != linkHandler) {
            mLinkHandler = linkHandler;
            statusText.setLinkHandler(linkHandler);
            if (mViewExtra != null) {
                mViewExtra.setLinkHandler(linkHandler);
            }
        }
    }

    public void setup(Status status) {
        if (mCurrentStatus != null && mCurrentStatus.id.equals(status.id)) return;
        mCurrentStatus = status;
        Glide.with(mContext).load(status.user.avatar_hd).into(userAvatar);
        userName.setText(status.user.screen_name);
        createTime.setText(StatusUtils.parseCreateTime(status));
        statusText.setText(status.text);
        attitudesCount.setText("" + status.attitudes_count);
        repostsCount.setText("" + status.reposts_count);
        commentsCount.setText("" + status.comments_count);
        if (mViewExtra != null) {
            mViewExtra.setup(status);
        }
    }

    @OnClick({R.id.weibo_card_content,
            R.id.user_avatar,
            R.id.weibo_opt_menu,
            R.id.status_decorator,
            R.id.weibo_like_num,
            R.id.weibo_forward_num,
            R.id.weibo_comment_num,
            })
    @Override
    public void onClick(View v) {
        if (mStatusActionListener == null || mCurrentStatus == null) return;
        String id = mCurrentStatus.id;
        Status status = mCurrentStatus;
        int action = -1;
        switch (v.getId()) {
            case R.id.weibo_card_content:
                action = WeiboActionListener.ACTION_STATUS;
                break;
            case R.id.user_avatar:
                action = WeiboActionListener.ACTION_USER;
                break;
            case R.id.weibo_opt_menu:
                action = WeiboActionListener.ACTION_OPT_MENU;
                break;
            case R.id.weibo_comment_num:
                action = WeiboActionListener.ACTION_COMMENTS;
                break;
            case R.id.weibo_forward_num:
                action = WeiboActionListener.ACTION_REPOSTS;
                break;
            case R.id.weibo_like_num:
                action = WeiboActionListener.ACTION_ATTITUDES;
                break;
            case R.id.status_decorator:
                if (mCurrentStatus.retweeted_status != null) {
                    id = mCurrentStatus.retweeted_status.id;
                    status = mCurrentStatus.retweeted_status;
                    action = WeiboActionListener.ACTION_STATUS;
                }
                break;
            case R.id.deco_image_grid:
                action = WeiboActionListener.ACTION_IMAGES;
                break;
            case R.id.deco_rt_image_grid:
                id = mCurrentStatus.retweeted_status.id;
                status = mCurrentStatus.retweeted_status;
                action = WeiboActionListener.ACTION_IMAGES;
                break;
        }
        if (action >= 0) {
            mStatusActionListener.onStatusAction(v, status, action);
        }
    }
}
