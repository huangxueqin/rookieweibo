package com.huangxueqin.rookieweibo.ui.emoji;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by huangxueqin on 2017/4/20.
 */

public class EmojiGridView extends ViewGroup {

    private int mRowCount;
    private int mColumnCount;
    private int mChildWidth;
    private int mChildHeight;

    public EmojiGridView(Context context, int rowCount, int columnCount) {
        super(context);
        mRowCount = rowCount;
        mColumnCount = columnCount;

        if (mRowCount <= 0 || mColumnCount <= 0) {
            throw new IllegalArgumentException("rowCount and columnCount must greater than 0");
        }

        for (int i = 0; i < rowCount*columnCount; i++) {
            ImageView emImage = new ImageView(context);
            emImage.setScaleType(ImageView.ScaleType.CENTER);
            addView(emImage);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int vertAvailableSpace = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
        final int horiAvailableSpace = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        mChildWidth = horiAvailableSpace / mColumnCount;
        mChildHeight = vertAvailableSpace / mRowCount;
        for (int i = 0; i < mRowCount*mColumnCount; i++) {
            getChildAt(i).measure(MeasureSpec.makeMeasureSpec(mChildWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(mChildHeight, MeasureSpec.EXACTLY));
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }

    private class LayoutParams extends ViewGroup.LayoutParams {
        int row;
        int col;

        public LayoutParams(int row, int col) {
            super(WRAP_CONTENT, WRAP_CONTENT);

        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }
}
