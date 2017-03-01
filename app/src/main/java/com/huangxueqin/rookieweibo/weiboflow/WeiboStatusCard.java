package com.huangxueqin.rookieweibo.weiboflow;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.huangxueqin.rookieweibo.R;
import com.huangxueqin.rookieweibo.ST;
import com.huangxueqin.rookieweibo.widget.WeiboImageGrid;
import com.sina.weibo.sdk.openapi.models.Status;

import org.w3c.dom.Text;

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
    View mOptMenu;

    // body
    @BindView(R.id.status_text)
    TextView mStatusText;
    @BindView(R.id.status_extra_content)
    FrameLayout mStatusExtraContainer;

    // footer
    @BindView(R.id.weibo_like_num)
    TextView mLikeNum;
    @BindView(R.id.weibo_forward_num)
    TextView mForwardNum;
    @BindView(R.id.weibo_comment_num)
    TextView mCommentNum;

    private int mStatusType;
    private ExtraHolder mHolder;
    private Status mStatus;

    public WeiboStatusCard(Context context) {
        super(context, null);
    }

    public WeiboStatusCard(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public WeiboStatusCard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    public void setStatus(Status status) {
        mStatus = status;
        // header
        mUserName.setText(status.user.screen_name);
        mCreateTime.setText(status.created_at);
        Glide.with(getContext()).load(status.user.avatar_large).into(mUserAvatar);
        // body
        mStatusText.setText(status.text);
        if (mHolder != null) {
            mHolder.setStatus(status.retweeted_status == null ? status : status.retweeted_status);
        }

        // footer
        mLikeNum.setText("" + status.attitudes_count);
        mForwardNum.setText("" + status.reposts_count);
        mCommentNum.setText("" + status.comments_count);
    }

    public static WeiboStatusCard get(Context context, ViewGroup parent, int statusType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        WeiboStatusCard card = (WeiboStatusCard) inflater.inflate(R.layout.view_weibo_status_card, parent, false);
        card.mStatusType = statusType;
        if (statusType != ST.SIMPLE) {
            View extraContentView = getExtraContentView(context, card.mStatusExtraContainer, statusType);
            card.mStatusExtraContainer.addView(extraContentView);
            card.mHolder = ExtraHolder.get(extraContentView, statusType);
        } else {
            card.mStatusExtraContainer.setVisibility(View.GONE);
        }
        return card;
    }

    private static View getExtraContentView(Context context, ViewGroup parent, int statusType) {
        final int layoutId;
        switch (statusType) {
            case ST.IMAGE:
                layoutId = R.layout.view_weibo_status_extra_image;
                break;
            case ST.RT_SIMPLE:
                layoutId = R.layout.view_weibo_status_extra_rt_simple;
                break;
            case ST.RT_IMAGE:
                layoutId = R.layout.view_weibo_status_extra_rt_image;
                break;
            default:
                layoutId = 0;
        }

        if (layoutId > 0) {
            return LayoutInflater.from(context).inflate(layoutId, parent, false);
        }
        return null;
    }

    private static abstract class ExtraHolder {
        abstract public void setStatus(Status status);

        public static ExtraHolder get(View view, int statusType) {
            switch (statusType) {
                case ST.IMAGE:
                    return new ImageExtraHolder(view);
                case ST.RT_SIMPLE:
                    return new RtSimpleExtraHolder(view);
                case ST.RT_IMAGE:
                    return new RtImageExtraHolder(view);
            }
            return null;
        }
    }

    private static class ImageExtraHolder extends ExtraHolder {
        WeiboImageGrid imageGrid;
        public ImageExtraHolder(View view) {
            imageGrid = (WeiboImageGrid) view;
        }

        @Override
        public void setStatus(Status status) {
            String[] imageUrls = new String[status.pic_urls.size()];
            for (int i = 0; i < status.pic_urls.size(); i++) {
                String str = status.pic_urls.get(i);
                imageUrls[i] = str.replace("thumbnail", "large");
            }
            imageGrid.setImage(imageUrls);
        }
    }

    private static class RtSimpleExtraHolder extends ExtraHolder {
        View container;
        TextView statusText;
        public RtSimpleExtraHolder(View view) {
            container = view;
            statusText = (TextView) view.findViewById(R.id.extra_rt_status_text);
        }

        @Override
        public void setStatus(Status status) {
            statusText.setText("@" + status.user.screen_name + "：" + status.text);
        }
    }

    private static class RtImageExtraHolder extends RtSimpleExtraHolder {
        WeiboImageGrid imageGrid;
        public RtImageExtraHolder(View view) {
            super(view);
            imageGrid = (WeiboImageGrid) view.findViewById(R.id.extra_rt_image_grid);
        }

        @Override
        public void setStatus(Status status) {
            super.setStatus(status);
            String[] imageUrls = new String[status.pic_urls.size()];
            for (int i = 0; i < status.pic_urls.size(); i++) {
                String str = status.pic_urls.get(i);
                imageUrls[i] = str.replace("thumbnail", "large");
            }
            imageGrid.setImage(imageUrls);
        }
    }


}
