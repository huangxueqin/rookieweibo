package com.huangxueqin.rookieweibo.weiboflow;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.huangxueqin.rookieweibo.R;
import com.huangxueqin.rookieweibo.ST;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by huangxueqin on 2017/2/26.
 */

public class WeiboStatusCard extends CardView {

    // header
    @BindView(R.id.user_avatar)
    ImageView mUserAvatar;
    @BindView(R.id.user_name)
    TextView mUserName;
    @BindView(R.id.weibo_create_time)
    TextView mCreateTime;
    @BindView(R.id.weibo_opt_menu)
    TextView mOptMenu;

    // body
    @BindView(R.id.weibo_text)
    TextView mStatusText;
    @BindView(R.id.weibo_extra_content)
    FrameLayout mExtraContentContainer;
    View mExtraContentView;

    // footer
    @BindView(R.id.weibo_like_num)
    TextView mLikeText;
    @BindView(R.id.weibo_forward_num)
    TextView mForwardCount;
    @BindView(R.id.weibo_comment_num)
    TextView mCommentCount;

    private int mStatusType;

    private WeiboStatusCard(Context context) {
        this(context, null);
    }

    private WeiboStatusCard(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    private WeiboStatusCard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    public static WeiboStatusCard get(Context context, ViewGroup parent, int statusType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        WeiboStatusCard card = (WeiboStatusCard) inflater.inflate(R.layout.view_weibo_status_card, parent, false);
        card.mStatusType = statusType;
        View extraContentView = extraContentViewForType(context, card.mExtraContentContainer, statusType);
        if (extraContentView != null) {
            card.mExtraContentContainer.addView(extraContentView);
        } else {
            card.mExtraContentContainer.setVisibility(View.GONE);
        }
        card.mExtraContentView = extraContentView;
        return card;
    }

    private static View extraContentViewForType(Context context, ViewGroup parent, int statusType) {
        final int layoutId;
        switch (statusType) {
            case ST.IMAGE:
                layoutId = R.layout.view_weibo_status_extra_image;
                break;
            default:
                layoutId = -1;
        }

        View extraView = null;
        if (layoutId > 0) {
            extraView = LayoutInflater.from(context).inflate(layoutId, parent, false);
        }
        return extraView;
    }

    private static abstract class ExtraContentHolder {
        View contentView;
        ExtraContentHolder(View contentView) {
            this.contentView = contentView;
        }
    }

    private static class ImageContentHolder extends ExtraContentHolder {

        ImageContentHolder(View contentView) {
            super(contentView);
        }
    }
}
