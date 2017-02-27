package com.huangxueqin.rookieweibo.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.huangxueqin.rookieweibo.R;
import com.huangxueqin.rookieweibo.weiboflow.WeiboStatusCard;
import com.sina.weibo.sdk.openapi.models.Status;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by huangxueqin on 2017/2/26.
 */

public class WeiboStatusView extends LinearLayout {
    public static final int TYPE_SIMPLE = 100;
    public static final int TYPE_IMAGE = 101;
    public static final int TYPE_VIDEO = 102;
    public static final int TYPE_MUSIC = 103;
    public static final int TYPE_RETWEET = 104;

    @BindView(R.id.weibo_card_header)
    ViewGroup mWeiboCardHeader;
    @BindView(R.id.user_avatar)
    ImageView mUserAvatar;
    @BindView(R.id.user_name)
    TextView mUserName;
    @BindView(R.id.weibo_create_time)
    TextView mCreateTime;
    @BindView(R.id.weibo_opt_menu)
    View mOptMenu;
    @BindView(R.id.weibo_text)
    TextView mWeiboText;
    @BindView(R.id.weibo_extra_content)
    FrameLayout mExtraContent;
    @BindView(R.id.weibo_card_footer)
    ViewGroup mWeiboCardFooter;
    @BindView(R.id.weibo_like_num)
    TextView mLikeNum;
    @BindView(R.id.weibo_comment_num)
    TextView mCommentNum;
    @BindView(R.id.weibo_forward_num)
    TextView mForwardNum;

    WeiboImageGrid mImageGrid;
    WeiboStatusView mRetweetStatusView;

    private int mType = -1;
    private boolean mIsCompactMode;
    private Status mStatus;

    public WeiboStatusView(Context context) {
        this(context, null);
    }

    public WeiboStatusView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WeiboStatusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.WeiboStatusView);
        mIsCompactMode = ta.getBoolean(R.styleable.WeiboStatusView_compactMode, false);
        ta.recycle();

        if (mIsCompactMode) {
            mWeiboCardHeader.setVisibility(View.GONE);
            mWeiboCardFooter.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    public void setStatus(Status status) {
        if (mStatus != null && mStatus.id.equals(status.id)) return;
        if (mIsCompactMode) {
            mWeiboText.setText("@" + status.user.screen_name + ":" + status.text);
        } else {
            mWeiboText.setText(status.text);
        }
        mUserName.setText(status.user.screen_name);
        mCreateTime.setText(status.created_at);
        Glide.with(getContext()).load(status.user.avatar_large).into(mUserAvatar);
        if (mType == TYPE_IMAGE) {
            String[] imageUrls = new String[status.pic_urls.size()];
            status.pic_urls.toArray(imageUrls);
            mImageGrid.setImage(imageUrls);
        } else if (mType == TYPE_RETWEET) {
            mRetweetStatusView.setStatus(status.retweeted_status);
        }
        mStatus = status;
    }

    public static WeiboStatusView get(Context context, int statusType) {

    }

    public static WeiboStatusView get(Context context, int type, int retweetType, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        final WeiboStatusView statusView = (WeiboStatusView) inflater.inflate(R.layout.view_weibo_status, parent, false);
        FrameLayout extraContent = statusView.mExtraContent;
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        if (type == TYPE_SIMPLE) {
            extraContent.setVisibility(View.GONE);
        } else if (type == TYPE_IMAGE) {
            WeiboImageGrid imageGrid = new WeiboImageGrid(context);
            extraContent.addView(imageGrid, lp);
            statusView.mImageGrid = imageGrid;
        } else if (type == TYPE_RETWEET) {
            WeiboStatusView retweetView = WeiboStatusView.get(context, retweetType, -1, extraContent);
            retweetView.mType = retweetType;
            retweetView.setCompactMode();
            extraContent.addView(retweetView, lp);
            statusView.mRetweetStatusView = retweetView;
        }
        statusView.mType = type;
        return statusView;
    }
}
