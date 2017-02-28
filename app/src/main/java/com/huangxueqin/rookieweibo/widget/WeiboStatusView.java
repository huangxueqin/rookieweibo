package com.huangxueqin.rookieweibo.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.huangxueqin.rookieweibo.R;
import com.huangxueqin.rookieweibo.StatusType;
import com.sina.weibo.sdk.openapi.models.Status;

/**
 * Created by huangxueqin on 2017/2/26.
 */

public class WeiboStatusView extends LinearLayout {

    private final int mStatusType;
    private View mTypedContent;
    private TextView mStatusText;

    private Status mStatus;


    public WeiboStatusView(Context context, int statusType) {
        super(context);
        mStatusType = statusType;

        mStatusText = createStatusText(context);

        if (mStatusType == StatusType.SIMPLE) {

        }
    }

    private TextView createStatusText(Context context) {
        TextView text = new TextView(context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
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
        if (mStatusType == TYPE_IMAGE) {
            String[] imageUrls = new String[status.pic_urls.size()];
            status.pic_urls.toArray(imageUrls);
            mImageGrid.setImage(imageUrls);
        } else if (mStatusType == TYPE_RETWEET) {
            mRetweetStatusView.setStatus(status.retweeted_status);
        }
        mStatus = status;
    }

    public static WeiboStatusView get(Context context, int statusType) {

    }

    public static WeiboStatusView get(Context context, int type, int retweetType, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        final WeiboStatusView statusView = (WeiboStatusView) inflater.inflate(R.layout.view_weibo_status_content, parent, false);
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
            retweetView.mStatusType = retweetType;
            retweetView.setCompactMode();
            extraContent.addView(retweetView, lp);
            statusView.mRetweetStatusView = retweetView;
        }
        statusView.mStatusType = type;
        return statusView;
    }
}
