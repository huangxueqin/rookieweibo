package com.huangxueqin.rookieweibo.ui.repost;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.huangxueqin.rookieweibo.LceFragment;
import com.huangxueqin.rookieweibo.common.Logger;
import com.huangxueqin.rookieweibo.cons.Cons;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.legacy.StatusesAPI;

/**
 * Created by huangxueqin on 2017/3/31.
 */

public class RepostListFragment extends LceFragment {
    private static final int COUNT_EACH_REQUEST = 20;

    private String mStatusId;
    private StatusesAPI mStatusAPI;
    private int mCurrentPage;

    public static RepostListFragment newInstance(String statusId) {
        RepostListFragment fragment = new RepostListFragment();
        Bundle args = new Bundle();
        args.putString(Cons.IntentKey.STATUS_ID, statusId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return 0;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mStatusId = args.getString(Cons.IntentKey.STATUS_ID);
        mStatusAPI = getLegacyStatusAPI();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return new RecyclerView(getContext());
    }

    @Override
    protected void prepareDataAndInit() {
        super.prepareDataAndInit();
        mCurrentPage = 0;
        loadNextPage();
    }

    private void loadNextPage() {
        loadRepostList(mCurrentPage+1);
    }

    private void loadRepostList(final int page) {
        loadRepostList(page, 2);
    }

    private void loadRepostList(final int page, final int retryTimes) {
        mStatusAPI.repostTimeline(Long.parseLong(mStatusId), 0, 0, COUNT_EACH_REQUEST, 1, 0, new RequestListener() {
            @Override
            public void onComplete(String s) {
                Logger.d("TAG", s);
            }

            @Override
            public void onWeiboException(WeiboException e) {

            }
        });
    }
}
