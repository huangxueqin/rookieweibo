package com.huangxueqin.rookieweibo.common.list;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by huangxueqin on 2017/3/13.
 */

public class LinearLineDecoration extends RecyclerView.ItemDecoration {

    private Paint mPaint;
    private int mPaddingLeft;
    private int mPaddingRight;

    public LinearLineDecoration(int color) {
        this(color, 0);
    }

    public LinearLineDecoration(int color, int padding) {
        this(color, padding, padding);
    }

    public LinearLineDecoration(int color, int paddingLeft, int paddingRight) {
        mPaint = new Paint();
        mPaint.setColor(color);
        mPaddingLeft = paddingLeft;
        mPaddingRight = paddingRight;
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        final LinearLayoutManager llm = (LinearLayoutManager) parent.getLayoutManager();
        final int count = llm.getChildCount();
        final int totalItemCount = llm.getItemCount();
        final int firstPosition = llm.findFirstVisibleItemPosition();
        for (int i = 0; i < count; i++) {
            if (firstPosition+i < totalItemCount-1) {
                View item = parent.getChildAt(i);
                c.drawLine(mPaddingLeft,
                        item.getBottom(),
                        parent.getWidth() - mPaddingRight,
                        item.getBottom(),
                        mPaint);
            }
        }
    }
}
