package com.huangxueqin.rookieweibo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.huangxueqin.rookieweibo.auth.AuthConstants;
import com.huangxueqin.rookieweibo.common.Logger;
import com.huangxueqin.rookieweibo.common.ToolbarAlphaBehavior;
import com.huangxueqin.rookieweibo.common.list.LoadingListener;
import com.huangxueqin.rookieweibo.cons.Cons;
import com.huangxueqin.rookieweibo.ui.status.WeiboFlowAdapter;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.UsersAPI;
import com.sina.weibo.sdk.openapi.legacy.StatusesAPI;
import com.sina.weibo.sdk.openapi.models.Status;
import com.sina.weibo.sdk.openapi.models.StatusList;
import com.sina.weibo.sdk.openapi.models.User;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by huangxueqin on 2017/6/10.
 */

public class WeiboUserActivity extends BaseActivity {

    private static final String TAG = "WeiboUserActivity";

    @BindView(R.id.status_list)
    RecyclerView mUserInfoList;
    @BindView(R.id.toolbar)
    View mToolbar;
    @BindView(R.id.header_view)
    View mHeaderView;
    @BindView(R.id.user_bg)
    ImageView mUserBgView;
    @BindView(R.id.user_avatar)
    ImageView mUserAvatarView;
    @BindView(R.id.user_name)
    TextView mUsernameText;

    User mUser;
    UsersAPI mUserApi;
    StatusesAPI mStatusApi;
    WeiboFlowAdapter mWeiboFlowAdapter;
    boolean mDataComplete;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weibo_user);
        ButterKnife.bind(this);
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mToolbar.getLayoutParams();
        params.setBehavior(new ToolbarAlphaBehavior(mUserInfoList, 600));
        mToolbar.setLayoutParams(params);

        mUserApi = new UsersAPI(this, AuthConstants.APP_KEY, mAccessToken);
        mStatusApi = new StatusesAPI(this, AuthConstants.APP_KEY, mAccessToken);

        String username = getIntent().getStringExtra(Cons.IntentKey.USER_NAME);
        long uid = getIntent().getLongExtra(Cons.IntentKey.USER_ID, -1);
        retrieveUserInfo(uid, username);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mUserStatusApi.cancel();
    }

    private void retrieveUserInfo(long uid, String username) {
        RequestListener listener = new RequestListener() {
            @Override
            public void onComplete(String s) {
                Logger.d(TAG, s);
                mUser = User.parse(s);
                initViewsByUser(mUser);
            }

            @Override
            public void onWeiboException(WeiboException e) {
                Toast.makeText(WeiboUserActivity.this, "加载用户信息失败", Toast.LENGTH_SHORT).show();
            }
        };

        if (uid != -1) {
            mUserApi.show(uid, listener);
        } else if (!TextUtils.isEmpty(username)) {
            mUserApi.show(username, listener);
        } else {
            throw new IllegalArgumentException("user id or user name should be supplied to retrieve user info");
        }
    }

    private void initViewsByUser(User user) {
        Glide.with(this).load(R.drawable.weibo_bg).into(mUserBgView);
        Glide.with(this).load(user.avatar_hd).into(mUserAvatarView);
        mUsernameText.setText(user.screen_name);
        mWeiboFlowAdapter = new WeiboFlowAdapter(this);
        mUserInfoList.setAdapter(mWeiboFlowAdapter);
        mUserInfoList.addOnScrollListener(new LoadingListener() {
            @Override
            public boolean allowLoading() {
                return !mDataComplete && !mUserStatusApi.isExecuting() && mUserStatusApi.isValid();
            }

            @Override
            public void performLoadingAction() {
                mUserStatusApi.execute();
            }
        });

        mUserStatusApi.execute();
    }

    private WeiboApiWrapper mUserStatusApi = new WeiboApiWrapper() {
        boolean mIsLoading;
        int currentPage = 1;
        @Override
        public void execute() {
            super.execute();
            mStatusApi.userTimeline(mUser.screen_name, 0, 0, 20, currentPage, false, 0, true, this);
        }

        @Override
        public void onComplete(String s) {
            super.onComplete(s);
            currentPage += 1;
            ArrayList<Status> statuses = StatusList.parse(s).statusList;
            if (statuses != null) {
                mWeiboFlowAdapter.append(statuses);
            }
            if (statuses == null || statuses.size() < 20) {
                mWeiboFlowAdapter.setDataComplete();
                mDataComplete = true;
            }
        }
    };
}
