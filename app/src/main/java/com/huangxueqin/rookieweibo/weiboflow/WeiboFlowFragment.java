package com.huangxueqin.rookieweibo.weiboflow;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.huangxueqin.rookieweibo.BaseFragment;
import com.huangxueqin.rookieweibo.R;
import com.huangxueqin.rookieweibo.auth.Constants;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.StatusesAPI;
import com.sina.weibo.sdk.openapi.models.StatusList;

/**
 * Created by huangxueqin on 2017/2/22.
 */

public class WeiboFlowFragment extends BaseFragment {

    StatusesAPI mStatusAPI;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStatusAPI = new StatusesAPI(getContext(), Constants.APP_KEY, getAccessToken());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_front_page, container, false);
        return root;
    }

    @Override
    protected void prepareDataAndInit() {
        mStatusAPI.friendsTimeline(0, 0, 10, 1, false, 0, false, new RequestListener() {
            @Override
            public void onComplete(String s) {

            }

            @Override
            public void onWeiboException(WeiboException e) {

            }
        });
    }


}
