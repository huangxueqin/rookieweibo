package com.huangxueqin.rookieweibo.common.list;

import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by huangxueqin on 2017/2/24.
 */

public class LinearPaddingDecoration extends RecyclerView.ItemDecoration {
    Rect paddingAround;
    int paddingBetweenItem;

    public LinearPaddingDecoration(int left, int top, int right, int bottom, int betweenItem) {
        paddingAround = new Rect(left, top, right, bottom);
        paddingBetweenItem = betweenItem;
    }

    public LinearPaddingDecoration(int left, int top, int betweenItem) {
        this(left, top, left, top, betweenItem);
    }

    public LinearPaddingDecoration(int betweenItem) {
        this(0, betweenItem/2, 0, betweenItem/2, betweenItem);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.left = paddingAround.left;
        outRect.right = paddingAround.right;
        LinearLayoutManager llm = (LinearLayoutManager) parent.getLayoutManager();
        final int position = llm.getPosition(view);
        final int lastPosition = llm.getItemCount();
        if (position != 0 && position != lastPosition) {
            outRect.top = outRect.bottom = paddingBetweenItem/2;
        } else if (position == 0) {
            outRect.top = paddingAround.top;
            outRect.bottom = paddingBetweenItem/2;
        } else {
            outRect.top = paddingBetweenItem/2;
            outRect.bottom = paddingBetweenItem/2;
        }
    }
}
