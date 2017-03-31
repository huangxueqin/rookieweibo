package com.huangxueqin.rookieweibo.ui.comments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.huangxueqin.rookieweibo.AppConfiguration;
import com.huangxueqin.rookieweibo.BaseFragment;
import com.huangxueqin.rookieweibo.R;
import com.huangxueqin.rookieweibo.common.list.LinearLineDecoration;
import com.huangxueqin.rookieweibo.common.list.LoadingListener;
import com.huangxueqin.rookieweibo.common.utils.UIUtils;
import com.huangxueqin.rookieweibo.cons.Cons;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.CommentsAPI;
import com.sina.weibo.sdk.openapi.models.CommentList;
import com.sina.weibo.sdk.openapi.models.Status;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by huangxueqin on 2017/3/12.
 */

public class CommentFragment extends BaseFragment {
    private static final int COUNT_EACH_REQUEST = AppConfiguration.Comment.COUNT;

    @BindView(R.id.comment_list_view)
    RecyclerView mCommentList;

    public String mStatusId;
    private CommentsAPI mCommentAPI;
    private CommentListAdapter mCommentAdapter;

    private int mCurrentPage;
    private boolean mAllCommentLoaded;
    private boolean mIsLoading;

    public static CommentFragment newInstance(String statusId) {
        CommentFragment fragment = new CommentFragment();
        Bundle args = new Bundle();
        args.putString(Cons.IntentKey.STATUS_ID, statusId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        mStatusId = args.getString(Cons.IntentKey.STATUS_ID);
        mCommentAPI = createCommentsAPI();
        mCommentAdapter = new CommentListAdapter(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comment, container, false);
        ButterKnife.bind(this, view);

        // init comment list view
        final int sepColor = UIUtils.getColor(getContext(), R.color.comment_list_line_sep);
        final RecyclerView.ItemDecoration decoration = new LinearLineDecoration(sepColor);
        mCommentList.addItemDecoration(decoration);
        mCommentList.setAdapter(mCommentAdapter);
        mCommentList.addOnScrollListener(mLoadingListener);

        return view;
    }

    @Override
    protected void prepareDataAndInit() {
        super.prepareDataAndInit();
        mCurrentPage = 0;
        loadComments(mCurrentPage+1);
    }

    private LoadingListener mLoadingListener = new LoadingListener() {

        @Override
        public boolean allowLoading() {
            return !mAllCommentLoaded && !mIsLoading;
        }

        @Override
        public void performLoadingAction() {
            loadComments(mCurrentPage+1);
        }
    };

    private void loadComments(int page) {
        loadComments(page, DEFAULT_RETRY);
    }

    private void loadComments(final int page, final int retryTime) {
        if (retryTime <= 0) return;
        mIsLoading = true;
        mCommentAPI.show(Long.parseLong(mStatusId), 0, 0, COUNT_EACH_REQUEST, page, 0, new RequestListener() {
            @Override
            public void onComplete(String s) {
                mIsLoading = false;
                loadCommentSuccess(page, CommentList.parse(s));
            }

            @Override
            public void onWeiboException(WeiboException e) {
                if (retryTime > 1) {
                    loadComments(page, retryTime-1);
                } else {
                    mIsLoading = false;
                    loadCommentFail(page);
                }
            }
        });
    }

    private void loadCommentSuccess(int targetPage, CommentList commentList) {
        if (targetPage != mCurrentPage+1) return;

        if (commentList != null && commentList.commentList != null) {
            mCommentAdapter.appendComment(commentList.commentList);
        }

        if (commentList == null ||
                commentList.commentList == null ||
                commentList.commentList.size() < COUNT_EACH_REQUEST) {
            mAllCommentLoaded = true;
            mCommentAdapter.setDataComplete(true);
        }

        if (commentList != null && commentList.commentList != null) {
            mCurrentPage += 1;
        }
    }

    private void loadCommentFail(int targetPage) {
        if (targetPage != mCurrentPage+1) return;

        mCommentList.scrollToPosition(mCommentAdapter.getItemCount()-2);
    }
}
