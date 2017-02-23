package com.huangxueqin.rookieweibo.weiboflow;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by huangxueqin on 2017/2/22.
 */

public class WeiboFlowFragment extends BaseFragment {

    @BindView(R.id.weibo_flow_list) RecyclerView mWeiboFlowList;

    StatusesAPI mStatusAPI;
    WeiboFlowAdapter mFlowAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStatusAPI = new StatusesAPI(getContext(), Constants.APP_KEY, getAccessToken());
        mFlowAdapter = new WeiboFlowAdapter(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_weibo_flow, container, false);
        ButterKnife.bind(this, root);
        mWeiboFlowList.setLayoutManager(new LinearLayoutManager(getContext()));
        mWeiboFlowList.setAdapter(mFlowAdapter);
        return root;
    }

    @Override
    protected void prepareDataAndInit() {
        mStatusAPI.friendsTimeline(0, 0, 10, 1, false, 0, false, new RequestListener() {
            @Override
            public void onComplete(String s) {
                StatusList list = StatusList.parse(s);
                mFlowAdapter.replace(list.statusList);
            }

            @Override
            public void onWeiboException(WeiboException e) {

            }
        });
    }


}
