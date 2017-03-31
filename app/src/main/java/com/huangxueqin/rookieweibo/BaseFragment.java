package com.huangxueqin.rookieweibo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import com.huangxueqin.rookieweibo.auth.AccessTokenKeeper;
import com.huangxueqin.rookieweibo.auth.AuthConstants;
import com.huangxueqin.rookieweibo.auth.UserKeeper;
import com.huangxueqin.rookieweibo.interfaces.IFragmentCallback;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.CommentsAPI;
import com.sina.weibo.sdk.openapi.StatusesAPI;
import com.sina.weibo.sdk.openapi.models.Status;
import com.sina.weibo.sdk.openapi.models.User;

/**
 * Created by huangxueqin on 2017/2/23.
 */

public class BaseFragment extends Fragment implements RequestListener {

    public static final int DEFAULT_RETRY = 2;

    private IFragmentCallback mCallback;
    private boolean mDataPreparationPending = true;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mDataPreparationPending && getUserVisibleHint()) {
            mDataPreparationPending = false;
            startPrepareData();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && getView() != null && mDataPreparationPending) {
            mDataPreparationPending = false;
            startPrepareData();
        }
    }

    private void startPrepareData() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                prepareDataAndInit();
            }
        });
    }

    protected void prepareDataAndInit() {

    }

    public void setCallback(IFragmentCallback callback) {
        mCallback = callback;
    }

    protected User getUser() {
        if (mCallback != null) {
            return mCallback.getUser();
        } else {
            return UserKeeper.readUser(getContext());
        }
    }

    protected String getAppKey() {
        return AuthConstants.APP_KEY;
    }

    protected Oauth2AccessToken getAccessToken() {
        if (mCallback != null) {
            return mCallback.getAccessToken();
        } else {
            return AccessTokenKeeper.readAccessToken(getContext());
        }
    }

    protected CommentsAPI createCommentsAPI() {
        return new CommentsAPI(getContext(), getAppKey(), getAccessToken());
    }

    protected StatusesAPI getStatusAPI() {
        return new StatusesAPI(getContext(), getAppKey(), getAccessToken());
    }

    protected com.sina.weibo.sdk.openapi.legacy.StatusesAPI getLegacyStatusAPI() {
        return new com.sina.weibo.sdk.openapi.legacy.StatusesAPI(getContext(), getAppKey(), getAccessToken());
    }

    @Override
    public void onComplete(String s) {

    }

    @Override
    public void onWeiboException(WeiboException e) {

    }
}
