package com.huangxueqin.rookieweibo.ui.status;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.huangxueqin.rookieweibo.AppConfiguration;
import com.huangxueqin.rookieweibo.BrowserActivity;
import com.huangxueqin.rookieweibo.LceFragment;
import com.huangxueqin.rookieweibo.R;
import com.huangxueqin.rookieweibo.RepostActivity;
import com.huangxueqin.rookieweibo.common.list.LoadingListener;
import com.huangxueqin.rookieweibo.WeiboApiWrapper;
import com.huangxueqin.rookieweibo.cons.Cons;
import com.huangxueqin.rookieweibo.cons.StatusAction;
import com.huangxueqin.rookieweibo.interfaces.StatusLinkHandler;
import com.huangxueqin.rookieweibo.interfaces.StatusListener;
import com.huangxueqin.rookieweibo.common.list.LinearPaddingDecoration;
import com.huangxueqin.rookieweibo.ui.widget.StatusTextView;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.openapi.StatusesAPI;
import com.sina.weibo.sdk.openapi.models.Status;
import com.sina.weibo.sdk.openapi.models.StatusList;
import com.sina.weibo.sdk.openapi.models.User;

import java.util.ArrayList;

import butterknife.BindView;

import static android.app.Activity.RESULT_OK;

/**
 * Created by huangxueqin on 2017/2/22.
 */

public class WeiboFlowFragment extends LceFragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final int REQUEST_REPOST = 0x1000;


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
        mStatusAPI = new StatusesAPI(getContext(), mAppKey, mToken);
        mFlowAdapter = new WeiboFlowAdapter(getContext());
        mFlowAdapter.setStatusActionListener(mStatusListener);
        mFlowAdapter.setLinkHandler(mLinkHandler);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_weibo_flow;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final int paddingBetweenItem = getResources().getDimensionPixelSize(R.dimen.weibo_flow_list_padding_between_item);
        final int paddingLeft = getResources().getDimensionPixelSize(R.dimen.weibo_flow_list_padding_left);
        final int paddingTop = getResources().getDimensionPixelSize(R.dimen.weibo_flow_list_padding_top);
        mWeiboFlowList.setLayoutManager(new LinearLayoutManager(getContext()));
        mWeiboFlowList.addItemDecoration(new LinearPaddingDecoration(paddingLeft, paddingTop, paddingBetweenItem));
        mWeiboFlowList.setAdapter(mFlowAdapter);
        mWeiboFlowList.addOnScrollListener(mLoadingListener);
        mSwipeRefreshLayout.setOnRefreshListener(this);
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
        mWeiboFlowList.scrollToPosition(0);
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

    private LoadingListener mLoadingListener = new LoadingListener() {
        @Override
        public boolean allowLoading() {
            return !mLoadingComplete && (mStatusRequest == null ||  !mStatusRequest.isValid());
        }

        @Override
        public void performLoadingAction() {
            nextFlow();
        }
    };

    private class StatusAPIWrapper extends WeiboApiWrapper {
        int RETRY;
        final int PAGE;
        boolean mFinish;

        public StatusAPIWrapper(int page) {
            this(page, 2);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_REPOST:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(getContext(), "转发成功", Toast.LENGTH_SHORT).show();
                    mSwipeRefreshLayout.setRefreshing(true);
                    refreshFlow();
                    return;
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private StatusListener mStatusListener = new StatusListener() {
        @Override
        public void performAction(int action, Object... args) {
            switch (action) {
                case StatusAction.GO_GALLERY:
                    final String[] images = (String[]) args[0];
                    final int index = (Integer) args[1];
                    StatusActionHelper.goGallery(getContext(), images, index);
                    break;
                case StatusAction.GO_STATUS: {
                    final Status status = (Status) args[0];
                    StatusActionHelper.goStatusPage(getContext(), status);
                }
                    break;
                case StatusAction.GO_USER:
                    final User user = (User) args[0];
                    StatusActionHelper.goUserPage(getContext(), user);
                    break;
                case StatusAction.ATTITUDE: {
                    final Status status = (Status) args[0];
                    StatusActionHelper.attitude(getContext(), status);
                    break;
                }
                case StatusAction.COMMENT: {
                    final Status status = (Status) args[0];
                    StatusActionHelper.comment(getContext(), status);
                    break;
                }
                case StatusAction.REPOST: {
                    final Status status = (Status) args[0];
                    Intent intent = new Intent(getContext(), RepostActivity.class);
                    intent.putExtra(Cons.IntentKey.STATUS, new Gson().toJson(status));
                    startActivityForResult(intent, REQUEST_REPOST);
                    break;
                }
            }
        }
    };

    private StatusLinkHandler mLinkHandler = new StatusLinkHandler() {
        @Override
        public void handleURL(StatusTextView view, String url) {
            Intent intent = new Intent(getContext(), BrowserActivity.class);
            intent.putExtra(Cons.IntentKey.URL, url);
            startActivity(intent);
        }

        @Override
        public void handleAt(StatusTextView view, String at) {

        }

        @Override
        public void handleTopic(StatusTextView view, String topic) {

        }
    };

    private void D(String msg) {
        Log.d("TAG", msg);
    }

}
