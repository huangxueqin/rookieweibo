package com.huangxueqin.rookieweibo.weiboflow;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.huangxueqin.rookieweibo.R;
import com.huangxueqin.rookieweibo.widget.WeiboStatusView;

/**
 * Created by huangxueqin on 2017/2/26.
 */

public class WeiboStatusCard extends CardView {

    WeiboStatusView mStatusView;

    public WeiboStatusCard(Context context) {
        this(context, null);
    }

    public WeiboStatusCard(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WeiboStatusCard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public static WeiboStatusCard get(Context context, ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        WeiboStatusCard card = (WeiboStatusCard) inflater.inflate(R.layout.view_list_item_weibo_status_card,
                parent, false);
        WeiboStatusView statusView = WeiboStatusView.get(context, listItemTypeToStatusType(viewType), card);
        card.addView(statusView);
        card.mStatusView = statusView;
        return card;
    }

    public static int listItemTypeToStatusType(int viewType) {
        switch (viewType) {
            case WeiboFlowAdapter.TYPE_STATUS_IMAGE:
                return WeiboStatusView.TYPE_IMAGE;
            case WeiboFlowAdapter.TYPE_STATUS_MUSIC:
                return WeiboStatusView.TYPE_MUSIC;
            case WeiboFlowAdapter.TYPE_STATUS_VIDEO:
                return WeiboStatusView.TYPE_VIDEO;
            case WeiboFlowAdapter.TYPE_STATUS_RETWEET:
                return WeiboStatusView.TYPE_RETWEET;
            case WeiboFlowAdapter.TYPE_STATUS_SIMPLE:
                return WeiboStatusView.TYPE_SIMPLE;
        }
        throw new IllegalArgumentException("invalid list item type with raw value " + viewType);
    }
}
