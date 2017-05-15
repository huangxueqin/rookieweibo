package com.huangxueqin.rookieweibo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.huangxueqin.rookieweibo.auth.AccessTokenKeeper;
import com.huangxueqin.rookieweibo.auth.AuthConstants;
import com.huangxueqin.rookieweibo.auth.UserKeeper;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.openapi.CommentsAPI;
import com.sina.weibo.sdk.openapi.StatusesAPI;
import com.sina.weibo.sdk.openapi.models.User;

/**
 * Created by huangxueqin on 2017/5/15.
 * Loading-Content-Error
 */

public abstract class LceFragment extends BaseFragment {
    protected User mUser;
    protected Oauth2AccessToken mToken;
    protected String mAppKey;

    private boolean mLoadingActionPending = true;

    protected void prepareDataAndInit() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUser = UserKeeper.readUser(getContext());
        mToken = AccessTokenKeeper.readAccessToken(getContext());
        mAppKey = AuthConstants.APP_KEY;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mLoadingActionPending && getUserVisibleHint()) {
            mLoadingActionPending = false;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    prepareDataAndInit();
                }
            });
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (mLoadingActionPending && isVisibleToUser && getView() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    prepareDataAndInit();
                }
            });
        }
    }


    protected CommentsAPI createCommentsAPI() {
        return new CommentsAPI(getContext(), mAppKey, mToken);
    }

    protected StatusesAPI getStatusAPI() {
        return new StatusesAPI(getContext(), mAppKey, mToken);
    }

    protected com.sina.weibo.sdk.openapi.legacy.StatusesAPI getLegacyStatusAPI() {
        return new com.sina.weibo.sdk.openapi.legacy.StatusesAPI(getContext(), mAppKey, mToken);
    }
}
