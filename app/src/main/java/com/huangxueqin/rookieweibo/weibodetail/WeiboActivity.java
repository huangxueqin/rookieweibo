package com.huangxueqin.rookieweibo.weibodetail;

import android.os.Bundle;
import android.provider.SyncStateContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.huangxueqin.rookieweibo.BaseActivity;
import com.huangxueqin.rookieweibo.BlankFragment;
import com.huangxueqin.rookieweibo.R;
import com.huangxueqin.rookieweibo.auth.AccessTokenKeeper;
import com.huangxueqin.rookieweibo.auth.AuthConstants;
import com.huangxueqin.rookieweibo.common.list.LinearLineDecoration;
import com.huangxueqin.rookieweibo.ui.comments.CommentFragment;
import com.huangxueqin.rookieweibo.cons.Cons;
import com.huangxueqin.rookieweibo.cons.StatusType;
import com.huangxueqin.rookieweibo.common.utils.StatusUtils;
import com.huangxueqin.rookieweibo.ui.comments.CommentListAdapter;
import com.huangxueqin.rookieweibo.ui.widget.StatusTextView;
import com.huangxueqin.rookieweibo.ui.widget.WeiboImageGrid;
import com.huangxueqin.rookieweibo.ui.widget.SlideTabLayout;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.CommentsAPI;
import com.sina.weibo.sdk.openapi.models.CommentList;
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
    @Nullable @BindView(R.id.image_grid)
    WeiboImageGrid mDecoImages;
    @Nullable @BindView(R.id.rt_image_grid)
    WeiboImageGrid mDecoRtImages;
    @Nullable @BindView(R.id.rt_content_text)
    StatusTextView mDecoRtStatusText;

    @Nullable
    @BindView(R.id.slide_tabs)
    SlideTabLayout mSlideTabs;

    @Nullable
    @BindView(R.id.view_pager)
    ViewPager mViewPager;

//    @Nullable
//    @BindView(R.id.comment_list)
//    RecyclerView mCommentList;

    private Status mStatus;
    private CommentsAPI mCommentAPI;
    private CommentListAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weibo);

        String statusStr = getIntent().getStringExtra(Cons.IntentKey.STATUS);
        mStatus = new Gson().fromJson(statusStr, Status.class);


        final int statusType = StatusUtils.getStatusType(mStatus);
        int viewStubId = -1;
        if (statusType == StatusType.IMAGE) {
            viewStubId = R.id.stub_status_content_image;
        } else if (statusType == StatusType.RT_SIMPLE) {
            viewStubId = R.id.stub_status_conent_simple_retweet;
        } else if (statusType == StatusType.RT_IMAGE) {
            viewStubId = R.id.stub_status_content_image_retweet;
        }
        if (viewStubId > 0) {
            ViewStub stub = (ViewStub) findViewById(viewStubId);
            stub.setVisibility(View.VISIBLE);
        }
        ButterKnife.bind(this);

        setupViews();

//        mAdapter = new CommentListAdapter(this);
//        mCommentList.addItemDecoration(new LinearLineDecoration(getResources().getColor(R.color.comment_list_line_sep)));
//        mCommentList.setAdapter(mAdapter);
//
//        mCommentAPI = new CommentsAPI(this, AuthConstants.APP_KEY, AccessTokenKeeper.readAccessToken(this));
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                mCommentAPI.show(Long.parseLong(mStatus.id), 0, 0, 20, 1, 0, new RequestListener() {
//                    @Override
//                    public void onComplete(String s) {
//                        CommentList commentList = CommentList.parse(s);
//                        if (commentList != null && commentList.commentList != null) {
//                            mAdapter.appendComment(commentList.commentList);
//                        }
//                        if (commentList.commentList == null) {
//                            mAdapter.setDataComplete(true);
//                        }
//                    }
//
//                    @Override
//                    public void onWeiboException(WeiboException e) {
//
//                    }
//                });
//            }
//        }).start();

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
        if (statusType == StatusType.IMAGE) {
            mDecoImages.setImage(StatusUtils.getLargePics(mStatus));
        } else if (statusType == StatusType.RT_SIMPLE) {
            mDecoRtStatusText.setText(mStatus.retweeted_status.text);
        } else if (statusType == StatusType.RT_IMAGE) {
            mDecoRtStatusText.setText(mStatus.retweeted_status.text);
            mDecoRtImages.setImage(StatusUtils.getLargePics(mStatus.retweeted_status));
        }

        mSlideTabs.setAdapter(mSlideTabAdapter);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                if (position == 0) {
                    CommentFragment fragment = new CommentFragment();
                    fragment.mStatus = mStatus;
                    return fragment;
                } else {
                    return new BlankFragment();
                }
            }

            @Override
            public int getCount() {
                return 3;
            }
        });
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
