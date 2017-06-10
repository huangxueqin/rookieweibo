package com.huangxueqin.rookieweibo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.huangxueqin.rookieweibo.auth.AuthConstants;
import com.huangxueqin.rookieweibo.common.ToolbarAlphaBehavior;
import com.huangxueqin.rookieweibo.common.list.EndlessAdapter;
import com.huangxueqin.rookieweibo.cons.Cons;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.StatusesAPI;
import com.sina.weibo.sdk.openapi.UsersAPI;
import com.sina.weibo.sdk.openapi.models.User;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by huangxueqin on 2017/6/10.
 */

public class WeiboUserActivity extends BaseActivity {

    @BindView(R.id.user_info_list)
    RecyclerView mUserInfoList;
    @BindView(R.id.toolbar)
    View mToolbar;

    User mUser;
    UsersAPI mUserApi;
    StatusesAPI mStatusApi;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weibo_user);
        ButterKnife.bind(this);
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mToolbar.getLayoutParams();
        params.setBehavior(new ToolbarAlphaBehavior(mUserInfoList, 600));
        mToolbar.setLayoutParams(params);
        mUserInfoList.setAdapter(new SimpleAdapter());

        mUserApi = new UsersAPI(this, AuthConstants.APP_KEY, mAccessToken);
        mStatusApi = new StatusesAPI(this, AuthConstants.APP_KEY, mAccessToken);

        String username = getIntent().getStringExtra(Cons.IntentKey.USER_NAME);
        long uid = getIntent().getLongExtra(Cons.IntentKey.USER_ID, -1);
        retrieveUserInfo(uid, username);
    }

    private void retrieveUserInfo(long uid, String username) {
        RequestListener listener = new RequestListener() {
            @Override
            public void onComplete(String s) {

            }

            @Override
            public void onWeiboException(WeiboException e) {

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

    private class SimpleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView textView = new TextView(WeiboUserActivity.this);
            textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100));
            textView.setTextSize(30);
            return new VH(textView);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            TextView tv = (TextView) holder.itemView;
            tv.setText("This is " + position + " line");
        }

        @Override
        public int getItemCount() {
            return 50;
        }

        class VH extends RecyclerView.ViewHolder {

            public VH(View itemView) {
                super(itemView);
            }
        }
    }
}
