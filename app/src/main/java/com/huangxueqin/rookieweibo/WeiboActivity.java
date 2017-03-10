package com.huangxueqin.rookieweibo;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.huangxueqin.rookieweibo.cons.Cons;
import com.huangxueqin.rookieweibo.cons.ST;
import com.huangxueqin.rookieweibo.utils.StatusUtils;
import com.huangxueqin.rookieweibo.widget.StatusTextView;
import com.huangxueqin.rookieweibo.widget.WeiboImageGrid;
import com.huangxueqin.rookieweibo.widget.SlideTabLayout;
import com.sina.weibo.sdk.openapi.models.Status;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by huangxueqin on 2017/3/3.
 */

public class WeiboActivity extends BaseActivity {

    @BindView(R.id.title)
    TextView mToolbarTitle;

    @BindView(R.id.scrollview)
    ScrollView mScrollView;

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
    @Nullable @BindView(R.id.deco_image_grid)
    WeiboImageGrid mDecoImages;
    @Nullable @BindView(R.id.deco_rt_image_grid)
    WeiboImageGrid mDecoRtImages;
    @Nullable @BindView(R.id.deco_rt_status_text)
    StatusTextView mDecoRtStatusText;

    @BindView(R.id.slide_tabs)
    SlideTabLayout mSlideTabs;

    @BindView(R.id.view_pager)
    ViewPager mViewPager;

    private Status mStatus;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weibo);

        String statusStr = getIntent().getStringExtra(Cons.IntentKey.STATUS);
        mStatus = new Gson().fromJson(statusStr, Status.class);

        final int statusType = StatusUtils.getStatusType(mStatus);
        int viewStubId = -1;
        if (statusType == ST.IMAGE) {
            viewStubId = R.id.stub_deco_image_grid;
        } else if (statusType == ST.RT_SIMPLE) {
            viewStubId = R.id.stub_deco_rt_simple;
        } else if (statusType == ST.RT_IMAGE) {
            viewStubId = R.id.stub_deco_rt_image;
        }
        if (viewStubId > 0) {
            ViewStub stub = (ViewStub) findViewById(viewStubId);
            stub.setVisibility(View.VISIBLE);
        }
        ButterKnife.bind(this);

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
        if (statusType == ST.IMAGE) {
            mDecoImages.setImage(StatusUtils.getLargePics(mStatus));
        } else if (statusType == ST.RT_SIMPLE) {
            mDecoRtStatusText.setText(mStatus.retweeted_status.text);
        } else if (statusType == ST.RT_IMAGE) {
            mDecoRtStatusText.setText(mStatus.retweeted_status.text);
            mDecoRtImages.setImage(StatusUtils.getLargePics(mStatus.retweeted_status));
        }

        mSlideTabs.setAdapter(mSlideTabAdapter);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    private SlideTabLayout.Adapter mSlideTabAdapter = new SlideTabLayout.Adapter() {
        @Override
        public int getTabCount() {
            return 3;
        }

        @Override
        public String getTabTitle(int tabNdx) {
            if (tabNdx == 0) {
                return "评论" + mStatus.comments_count;
            } else if (tabNdx == 1) {
                return "转发" + mStatus.reposts_count;
            } else {
                return "点赞" + mStatus.attitudes_count;
            }
        }

        @Override
        public int getTabIcon(int tabNdx) {
            if (tabNdx == 0) {
                return R.mipmap.ic_item_comment_gray;
            } else if (tabNdx == 1) {
                return R.mipmap.ic_item_forward_gray;
            } else {
                return R.mipmap.ic_like_empty_gray;
            }
        }
    };

}
