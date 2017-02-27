package com.huangxueqin.rookieweibo.weiboflow;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.huangxueqin.rookieweibo.R;
import com.huangxueqin.rookieweibo.widget.WeiboStatusView;

import butterknife.BindView;

/**
 * Created by huangxueqin on 2017/2/26.
 */

public class WeiboStatusCard extends CardView {

    @BindView(R.id.weibo_card_header) ViewGroup mCardHeader;
    @BindView(R.id.weibo_card_footer) ViewGroup mCardFooter;
    @BindView(R.id.weibo_card_content) ViewGroup mCardContent;


    public WeiboStatusCard(Context context) {
        this(context, null);
    }

    public WeiboStatusCard(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WeiboStatusCard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public WeiboStatusView getStatusView() {
        return mStatusView;
    }

    public static WeiboStatusCard get(Context context, ViewGroup parent, int statusType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        WeiboStatusCard card = new WeiboStatusCard(context, null, R.style.WeiboFlowCard);

        WeiboStatusView statusView = WeiboStatusView.get(context, listItemTypeToStatusType(st1),
                listItemTypeToStatusType(st2), card);
        card.addView(statusView);
        card.mStatusView = statusView;
        return card;
    }

    public static int listItemTypeToStatusType(int viewType) {
        switch (viewType) {
            case WeiboFlowAdapter.TYPE_IMAGE:
                return WeiboStatusView.TYPE_IMAGE;
            case WeiboFlowAdapter.TYPE_MUSIC:
                return WeiboStatusView.TYPE_MUSIC;
            case WeiboFlowAdapter.TYPE_VIDEO:
                return WeiboStatusView.TYPE_VIDEO;
            case WeiboFlowAdapter.TYPE_RETWEET:
                return WeiboStatusView.TYPE_RETWEET;
            case WeiboFlowAdapter.TYPE_SIMPLE:
                return WeiboStatusView.TYPE_SIMPLE;
        }
        return viewType;
    }
}
