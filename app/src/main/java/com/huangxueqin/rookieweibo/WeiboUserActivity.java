package com.huangxueqin.rookieweibo;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.huangxueqin.rookieweibo.auth.AuthConstants;
import com.huangxueqin.rookieweibo.common.Logger;
import com.huangxueqin.rookieweibo.common.StatusUtils;
import com.huangxueqin.rookieweibo.common.list.EndlessAdapter;
import com.huangxueqin.rookieweibo.common.list.LoadingListener;
import com.huangxueqin.rookieweibo.cons.Cons;
import com.huangxueqin.rookieweibo.ui.status.StatusHolder;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.StatusesAPI;
import com.sina.weibo.sdk.openapi.UsersAPI;
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

    User mUser;
    UsersAPI mUserApi;
    StatusesAPI mStatusApi;
    UserListAdapter mAdapter;
    UserStatusApi mUserStatusApi;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weibo_user);
        ButterKnife.bind(this);

        mUserApi = new UsersAPI(this, AuthConstants.APP_KEY, mAccessToken);
        mStatusApi = new StatusesAPI(this, AuthConstants.APP_KEY, mAccessToken);

        String userStr = getIntent().getStringExtra(Cons.IntentKey.USER);
        if (userStr != null) {
            mUser = new Gson().fromJson(userStr, User.class);
        }

        mAdapter = new UserListAdapter(this);
        mUserInfoList.setAdapter(mAdapter);
        mUserInfoList.addItemDecoration(new UserListDecoration(this));
        mUserInfoList.addOnScrollListener(mUserListLoadingListener);
        mUserStatusApi = new UserStatusApi();

        if (mUser == null) {
            String username = getIntent().getStringExtra(Cons.IntentKey.USER_NAME);
            retrieveUserInfo(-1, username);
        } else {
            initViewsByUser(mUser);
        }
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
        mUser = user;
        mAdapter.setUser(user);
        mTitle.setText(user.screen_name + "的主页");
        mUserStatusApi.execute();
    }

    private class UserListDecoration extends  RecyclerView.ItemDecoration {
        final int paddingLeft;
        final int paddingTop;
        final int paddingBetweenItem;

        public UserListDecoration(Context context) {
            paddingLeft = getResources().getDimensionPixelSize(R.dimen.weibo_flow_list_padding_left);
            paddingTop = getResources().getDimensionPixelSize(R.dimen.weibo_flow_list_padding_top);
            paddingBetweenItem = getResources().getDimensionPixelSize(R.dimen.weibo_flow_list_padding_between_item);
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            LinearLayoutManager llm = (LinearLayoutManager) parent.getLayoutManager();
            final int position = llm.getPosition(view);
            final int lastPosition = llm.getItemCount()-1;
            if (position == 0 || position == 1) {
                outRect.set(0, 0, 0, 0);
                return;
            }
            if (position == 2) {
                outRect.set(paddingLeft, paddingTop, paddingLeft, paddingBetweenItem/2);
            } else if (position == lastPosition) {
                outRect.set(paddingLeft, paddingBetweenItem/2, paddingLeft, paddingTop);
            } else {
                outRect.set(paddingLeft, paddingBetweenItem/2, paddingLeft, paddingBetweenItem/2);
            }
        }
    };

    private LoadingListener mUserListLoadingListener = new LoadingListener() {
        @Override
        public void performLoadingAction() {
            mUserStatusApi.execute();
        }

        @Override
        public boolean allowLoading() {
            return super.allowLoading() && !mUserStatusApi.isExecuting() && !mUserStatusApi.isComplete;
        }
    };

    private class UserStatusApi extends WeiboRequestApiWrapper {

        private static final int COUNT = AppConfiguration.Status.COUNT;
        private static final boolean BASE_APP = AppConfiguration.Status.BASE_APP;
        private static final int FEATURE = StatusesAPI.FEATURE_ALL;

        int page = 0;
        boolean isComplete;

        @Override
        public void execute() {
            super.execute();
            mStatusApi.friendsTimeline(0, 0, COUNT, page+1, BASE_APP, FEATURE, false, this);
        }

        @Override
        protected void onRequestComplete(String s) {
            super.onRequestComplete(s);
            List<Status> statusList = StatusList.parse(s).statusList;
            if (statusList != null) {
                mAdapter.appendStatus(statusList);
            }
            if (statusList == null || statusList.size() == 0) {
                isComplete = true;
                mAdapter.setDataComplete(true);
            }
            page += 1;
        }

        @Override
        protected void onRequestException(WeiboException e) {
            super.onRequestException(e);
        }
    }

    private class UserListAdapter extends EndlessAdapter<RecyclerView.ViewHolder, EndlessAdapter.FooterHolder> {
        private static final int TYPE_HEAD = 1;
        private static final int TYPE_USER_DATA = 2;
        private static final int TYPE_STATUS = 3;

        private Context mContext;
        private User mUser;
        private List<Status> mStatusList;

        public UserListAdapter(Context context) {
            mContext = context;
            mStatusList = new ArrayList<>();
        }

        public void setUser(User user) {
            mUser = user;
            notifyDataSetChanged();
        }

        public void appendStatus(List<Status> statuses) {
            mStatusList.addAll(statuses);
            notifyDataSetChanged();
        }

        @Override
        public int getContentItemCount() {
            return 2 + (mStatusList != null ? mStatusList.size() : 0);
        }

        @Override
        public int getContentItemViewType(int position) {
            if (position == 0) {
                return TYPE_HEAD;
            } else if (position == 1) {
                return TYPE_USER_DATA;
            } else  {
                return TYPE_STATUS | (StatusUtils.getStatusType(mStatusList.get(position-2)) << 16);
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateContentViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_HEAD) {
                View v = LayoutInflater.from(mContext).inflate(R.layout.view_user_list_head, parent, false);
                return new HeadHolder(v);
            } else if (viewType == TYPE_USER_DATA) {
                View v = LayoutInflater.from(mContext).inflate(R.layout.view_user_data, parent, false);
                return new UserDataHolder(v);
            } else {
                View v = LayoutInflater.from(mContext).inflate(R.layout.view_list_status_item, parent, false);
                return new StatusHolder(v, viewType>>16, null, null);
            }
        }

        @Override
        public void onBindContentViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (position == 0) {
                ((HeadHolder)holder).bind(mContext, mUser);
            } else if (position == 1) {
                ((UserDataHolder)holder).bind(mContext, mUser);
            } else {
                ((StatusHolder)holder).setStatus(mStatusList.get(position-2));
            }
        }

        @Override
        public FooterHolder onCreateFooterHolder(ViewGroup parent) {
            return FooterHolder.createInstance(mContext, parent);
        }

        @Override
        public void onBindFooterHolder(FooterHolder holder, boolean dataComplete) {
            holder.bind(mUserStatusApi.isComplete);
        }
    }

    public static class HeadHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.user_bg) ImageView userBgImage;
        @BindView(R.id.user_avatar) ImageView userAvatarImage;
        @BindView(R.id.user_name) TextView usernameText;
        public HeadHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(Context context, User user) {
            if (user != null) {
                Glide.with(context).load(user.cover_image_phone).placeholder(R.drawable.weibo_bg).into(userBgImage);
                Glide.with(context).load(user.avatar_large).into(userAvatarImage);
                usernameText.setText(user.screen_name);
            }
        }
    }

    public static class UserDataHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.weibo_num) TextView weiboNumText;
        @BindView(R.id.watch_num) TextView watchNumText;
        @BindView(R.id.fans_num) TextView fansNumText;

        public UserDataHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(Context context, User user) {
            if (user != null) {
                weiboNumText.setText(StatusUtils.parseNum(user.statuses_count));
                watchNumText.setText(StatusUtils.parseNum(user.friends_count));
                fansNumText.setText(StatusUtils.parseNum(user.followers_count));
            }
        }
    }
}
