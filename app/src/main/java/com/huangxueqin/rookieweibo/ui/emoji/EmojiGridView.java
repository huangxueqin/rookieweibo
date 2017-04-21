package com.huangxueqin.rookieweibo.ui.emoji;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by huangxueqin on 2017/4/20.
 */

public class EmojiGridView extends ViewGroup {

    public interface OnClickListener {
        void onClickAt(int row, int pos);
    }

    private int mRowCount;
    private int mColumnCount;
    private int mEmojiSize;

    private OnClickListener mOnClickListener;

    public EmojiGridView(Context context,
                         int rowCount,
                         int columnCount,
                         int emojiSize) {
        super(context);
        mRowCount = rowCount;
        mColumnCount = columnCount;
        mEmojiSize = emojiSize;

        if (mRowCount <= 0 || mColumnCount <= 0 || mEmojiSize <= 0) {
            throw new IllegalArgumentException("rowCount and columnCount must greater than 0");
        }

        for (int i = 0; i < mRowCount; i++) {
            for (int j = 0; j < mColumnCount; j++) {
                ImageView child = createEmojiView(context, i, j);
                child.setOnClickListener(mEmojiClickListener);
                addView(child);
            }
        }
    }

    public void setOnClickListener(@Nullable OnClickListener l) {
        mOnClickListener = l;
    }

    public void setEmojiRes(final Pair<String, Integer> res[], int start, int count) {
        for (int i = 0; i < mRowCount; i++) {
            for (int j = 0; j < mColumnCount; j++) {
                final int index = i*mColumnCount+j;
                final ImageView child = (ImageView) getChildAt(index);
                final boolean enable = index < count;
                child.setEnabled(enable);
                child.setImageResource(enable ? res[start+index].second : android.R.color.transparent);
            }
        }
    }

    private ImageView createEmojiView(Context context, int row, int col) {
        ImageView image = new ImageView(context);
        image.setScaleType(ImageView.ScaleType.CENTER);
        image.setLayoutParams(new LayoutParams(row, col));
        return image;
    }

    private View.OnClickListener mEmojiClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mOnClickListener != null) {
                LayoutParams lp = (LayoutParams) v.getLayoutParams();
                final int row = lp.row;
                final int col = lp.col;
                mOnClickListener.onClickAt(row, col);
            }
        }
    };

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // measure child
        for (int i = 0; i < mRowCount*mColumnCount; i++) {
            getChildAt(i).measure(MeasureSpec.makeMeasureSpec(mEmojiSize, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(mEmojiSize, MeasureSpec.EXACTLY));
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();
        final int widthUsed = getPaddingLeft() + getPaddingRight();
        final int heightUsed = getPaddingTop() + getPaddingBottom();
        final float verticalSpacing = 1f*(height-heightUsed-mEmojiSize*mRowCount) / (mRowCount-1);
        final float horizontalSpacing = 1f*(width-widthUsed-mEmojiSize*mColumnCount) / (mColumnCount-1);

        float top = getPaddingBottom();
        for (int i = 0; i < mRowCount; i++) {
            float left = getPaddingLeft();
            for (int j = 0; j < mColumnCount; j++) {
                final int index = i*mColumnCount+j;
                final View child = getChildAt(index);
                final int cl = (int) left;
                final int ct = (int) top;
                child.layout(cl, ct, cl+mEmojiSize, ct+mEmojiSize);
                left = left+mEmojiSize+horizontalSpacing;
            }
            top = top+mEmojiSize+verticalSpacing;
        }
    }

    private class LayoutParams extends ViewGroup.LayoutParams {
        int row;
        int col;

        public LayoutParams(int row, int col) {
            super(WRAP_CONTENT, WRAP_CONTENT);
            this.row = row;
            this.col = col;
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }
}
