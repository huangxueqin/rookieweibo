package com.huangxueqin.rookieweibo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.huangxueqin.rookieweibo.cons.Cons;
import com.huangxueqin.rookieweibo.cons.ST;
import com.huangxueqin.rookieweibo.utils.StatusUtils;
import com.huangxueqin.rookieweibo.widget.StatusTextView;
import com.huangxueqin.rookieweibo.widget.WeiboImageGrid;
import com.sina.weibo.sdk.openapi.models.Status;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by huangxueqin on 2017/3/3.
 */

public class WeiboActivity extends BaseActivity {

    @BindView(R.id.title)
    TextView mToolbarTitle;

    @BindView(R.id.weibo_content)
    ViewGroup mStatusContent;

    @BindView(R.id.user_avatar)
    ImageView mUserAvatar;
    @BindView(R.id.user_name)
    TextView mUserName;
    @BindView(R.id.weibo_create_time)
    TextView mWeiboCreateTime;


    @BindView(R.id.status_text)
    StatusTextView mStatusText;
    @BindView(R.id.status_decorator)
    ViewGroup mDecoContent;
    @BindView(R.id.deco_image_grid)
    WeiboImageGrid mDecoImages;
    @BindView(R.id.deco_retweet)
    ViewGroup mRetweetContent;
    @BindView(R.id.deco_rt_image_grid)
    WeiboImageGrid mDecoRtImages;
    @BindView(R.id.deco_rt_status_text)
    StatusTextView mDecoRtStatusText;

    private Status mStatus;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weibo);
        ButterKnife.bind(this);

        String statusStr = getIntent().getStringExtra(Cons.IntentKey.STATUS);
        mStatus = new Gson().fromJson(statusStr, Status.class);

        setupViews();
    }

    private void setupViews() {
        // setup toolbar views
        mToolbarTitle.setText(mStatus.user.screen_name);

        // setup status content
        Glide.with(this).load(mStatus.user.avatar_hd).into(mUserAvatar);
        mUserName.setText(mStatus.user.screen_name);
        mWeiboCreateTime.setText(StatusUtils.parseCreateTime(mStatus));
        mStatusText.setText(mStatus.text);
        final int statusType = StatusUtils.getStatusType(mStatus);
        if (statusType == ST.SIMPLE) {
            mDecoContent.setVisibility(View.GONE);
        } else if (statusType == ST.IMAGE) {
            mRetweetContent.setVisibility(View.GONE);
            mDecoImages.setImage(StatusUtils.getLargePics(mStatus));
        } else if (statusType == ST.RT_SIMPLE) {
            mDecoImages.setVisibility(View.GONE);
            mDecoRtImages.setVisibility(View.GONE);
            mDecoRtStatusText.setText(mStatus.retweeted_status.text);
        } else if (statusType == ST.RT_IMAGE) {
            mDecoImages.setVisibility(View.GONE);
            mDecoRtStatusText.setText(mStatus.retweeted_status.text);
            mDecoRtImages.setImage(StatusUtils.getLargePics(mStatus.retweeted_status));
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }


}
