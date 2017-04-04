package com.huangxueqin.rookieweibo.ui.message;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.huangxueqin.rookieweibo.BaseFragment;
import com.huangxueqin.rookieweibo.R;
import com.huangxueqin.rookieweibo.common.list.LinearLineDecoration;
import com.huangxueqin.rookieweibo.common.list.LinearPaddingDecoration;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by huangxueqin on 2017/4/1.
 */

public class MessageFragment extends BaseFragment {

    @BindView(R.id.message_list)
    RecyclerView mMessageList;

    MessageListAdapter mAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new MessageListAdapter(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_message, container, false);
        ButterKnife.bind(this, root);

        mMessageList.setAdapter(mAdapter);
        // item decoration
        final int sepColor = ContextCompat.getColor(getContext(), R.color.grey1);
        final int sepLeftPadding = (int) (getResources().getDisplayMetrics().density * 77);
        mMessageList.addItemDecoration(new LinearLineDecoration(sepColor, sepLeftPadding, 0));
        return root;
    }

    @Override
    protected void prepareDataAndInit() {
        super.prepareDataAndInit();
    }
}
