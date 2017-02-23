package com.huangxueqin.rookieweibo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import com.huangxueqin.rookieweibo.interfaces.IFragmentCallback;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.openapi.models.User;

/**
 * Created by huangxueqin on 2017/2/23.
 */

public class BaseFragment extends Fragment {
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
        return mCallback.getUser();
    }

    protected Oauth2AccessToken getAccessToken() {
        return mCallback.getAccessToken();
    }
}
