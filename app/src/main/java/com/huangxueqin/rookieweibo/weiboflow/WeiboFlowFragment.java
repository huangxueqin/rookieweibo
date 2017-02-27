package com.huangxueqin.rookieweibo.weiboflow;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.huangxueqin.rookieweibo.AppConfiguration;
import com.huangxueqin.rookieweibo.BaseFragment;
import com.huangxueqin.rookieweibo.R;
import com.huangxueqin.rookieweibo.RecyclerViewLoadingListener;
import com.huangxueqin.rookieweibo.WeiboAPIWrapper;
import com.huangxueqin.rookieweibo.auth.AuthConstants;
import com.huangxueqin.rookieweibo.itemdecoration.LinearLayoutPaddingDecoration;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.openapi.StatusesAPI;
import com.sina.weibo.sdk.openapi.models.Status;
import com.sina.weibo.sdk.openapi.models.StatusList;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by huangxueqin on 2017/2/22.
 */

public class WeiboFlowFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final int DEFAULT_RETRY = 2;

    @BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.weibo_flow_list) RecyclerView mWeiboFlowList;

    StatusesAPI mStatusAPI;
    WeiboFlowAdapter mFlowAdapter;

    StatusAPIWrapper mStatusRequest;
    int mCurrentPage = 0;
    boolean mLoadingComplete = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStatusAPI = new StatusesAPI(getContext(), AuthConstants.APP_KEY, getAccessToken());
        mFlowAdapter = new WeiboFlowAdapter(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_weibo_flow, container, false);
        ButterKnife.bind(this, root);

        final int paddingBetweenItem = getResources().getDimensionPixelSize(R.dimen.weibo_flow_list_padding_between_item);
        final int paddingLeft = getResources().getDimensionPixelSize(R.dimen.weibo_flow_list_padding_left);
        final int paddingTop = getResources().getDimensionPixelSize(R.dimen.weibo_flow_list_padding_top);
        mWeiboFlowList.setLayoutManager(new LinearLayoutManager(getContext()));
        mWeiboFlowList.addItemDecoration(new LinearLayoutPaddingDecoration(paddingLeft, paddingTop, paddingBetweenItem));
        mWeiboFlowList.setAdapter(mFlowAdapter);
        mWeiboFlowList.addOnScrollListener(mLoadingListener);

        mSwipeRefreshLayout.setOnRefreshListener(this);
        return root;
    }

    @Override
    protected void prepareDataAndInit() {
        mSwipeRefreshLayout.setRefreshing(true);
        refreshFlow();
    }

    @Override
    public void onRefresh() {
        refreshFlow();
    }

    private void refreshFlow() {
        if (mStatusRequest != null && mStatusRequest.isValid()) {
            if (mStatusRequest.isTypeRefresh()) {
                return;
            }
            else {
                mStatusRequest.cancel();
            }
        }
        mStatusRequest = new StatusAPIWrapper(1);
        mStatusRequest.execute();
    }

    private void nextFlow() {
        if (mStatusRequest!= null && mStatusRequest.isValid()) return;
        mStatusRequest = new StatusAPIWrapper(mCurrentPage+1);
        mStatusRequest.execute();
    }

    private void updateUIOnEndOfStatus() {
        mFlowAdapter.setDataComplete();
    }

    private void updateUIAfterRefresh() {
        mSwipeRefreshLayout.setRefreshing(false);
        mFlowAdapter.setDataInComplete();
    }

    private void updateUIAfterRefreshFail() {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void updateUIAfterLoading() {

    }

    private void updateUIAfterLoadingFail() {
        mWeiboFlowList.scrollToPosition(mFlowAdapter.getItemCount()-1);
    }

    private RecyclerViewLoadingListener mLoadingListener = new RecyclerViewLoadingListener() {
        @Override
        public boolean allowLoading() {
            return !mLoadingComplete && (mStatusRequest == null ||  !mStatusRequest.isValid());
        }

        @Override
        public void performLoadingAction() {
            nextFlow();
        }
    };

    private class StatusAPIWrapper extends WeiboAPIWrapper {
        int RETRY;
        final int PAGE;
        boolean mFinish;

        public StatusAPIWrapper(int page) {
            this(page, DEFAULT_RETRY);
        }

        public StatusAPIWrapper(int page, int retry) {
            RETRY = retry;
            PAGE = page;
            mFinish = true;
        }

        public boolean isTypeRefresh() {
            return PAGE == 1;
        }

        @Override
        public boolean isValid() {
            return mFinish && super.isValid();
        }

        @Override
        public void execute() {
            mStatusAPI.friendsTimeline(0, 0, AppConfiguration.Status.COUNT, PAGE,
                    false, StatusesAPI.FEATURE_ALL, false, this);
        }

        @Override
        protected void onRequestException(WeiboException e) {
            RETRY -= 1;
            if (RETRY > 0) {
                execute();
            } else {
                if (isTypeRefresh()) {
                    updateUIAfterRefreshFail();
                } else {
                    updateUIAfterLoadingFail();
                }
                mFinish = false;
            }
        }

        @Override
        protected void onRequestComplete(String s) {
            StatusList statusList = StatusList.parse(s);
            ArrayList<Status> statuses = statusList.statusList;
            if (statuses != null) {
                if (isTypeRefresh()) {
                    mFlowAdapter.refresh(statuses);
                } else {
                    mFlowAdapter.append(statuses);
                }
            }
            if (isTypeRefresh()) {
                mLoadingComplete = false;
                updateUIAfterRefresh();
            }
            if (statuses == null || statuses.size() < AppConfiguration.Status.COUNT) {
                mLoadingComplete = true;
                updateUIOnEndOfStatus();
            }

            mCurrentPage = PAGE;
            mFinish = false;
        }
    }

    private void D(String msg) {
        Log.d("TAG", msg);
    }

}
