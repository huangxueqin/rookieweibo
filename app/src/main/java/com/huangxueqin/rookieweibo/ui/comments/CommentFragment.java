package com.huangxueqin.rookieweibo.ui.comments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.huangxueqin.rookieweibo.BaseFragment;
import com.huangxueqin.rookieweibo.R;
import com.huangxueqin.rookieweibo.auth.AccessTokenKeeper;
import com.huangxueqin.rookieweibo.auth.AuthConstants;
import com.huangxueqin.rookieweibo.common.list.LinearLineDecoration;
import com.huangxueqin.rookieweibo.ui.comments.CommentListAdapter;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.CommentsAPI;
import com.sina.weibo.sdk.openapi.models.CommentList;
import com.sina.weibo.sdk.openapi.models.Status;

/**
 * Created by huangxueqin on 2017/3/12.
 */

public class CommentFragment extends BaseFragment {
    private static final int DEFAULT_RETRY = 2;

    private RecyclerView mCommentListView;
    private CommentListAdapter mCommentListAdapter;

    public Status mStatus;
    private CommentsAPI mCommentAPI;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCommentAPI = new CommentsAPI(getContext(), AuthConstants.APP_KEY, AccessTokenKeeper.readAccessToken(getContext()));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comment, container, false);
        mCommentListView = (RecyclerView) view.findViewById(R.id.comment_list_view);
        mCommentListView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mCommentListAdapter = new CommentListAdapter(getContext());
        mCommentListView.addItemDecoration(new LinearLineDecoration(getResources().getColor(R.color.comment_list_line_sep)));
        mCommentListView.setAdapter(mCommentListAdapter);
        return view;
    }

    @Override
    protected void prepareDataAndInit() {
        super.prepareDataAndInit();
        mCommentAPI.show(Long.parseLong(mStatus.id), 0, 0, 20, 1, 0, new RequestListener() {
            @Override
            public void onComplete(String s) {
                CommentList commentList = CommentList.parse(s);
                if (commentList != null && commentList.commentList != null) {
                    mCommentListAdapter.appendComment(commentList.commentList);
                }
                if (commentList.commentList == null) {
                    mCommentListAdapter.setDataComplete(true);
                }
                Log.d("TAG", "list height = " + mCommentListView.getMeasuredHeight());
            }

            @Override
            public void onWeiboException(WeiboException e) {

            }
        });
    }
}
