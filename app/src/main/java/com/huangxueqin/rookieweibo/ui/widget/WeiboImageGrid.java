package com.huangxueqin.rookieweibo.ui.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

/**
 * Created by huangxueqin on 2017/2/24.
 */

public class WeiboImageGrid extends ViewGroup {
    private static final int MAX_IMAGE_COUNT = 9;
    private static final int IMAGE_SPACING_DP = 5;
    private static final float RATIO = 0.7f;

    private String[] mImageUrls;
    private int mImageSpacing;
    private int mImageWidth;
    private int mImageHeight;

    private int mLastTouchDownX = -1;
    private int mLastTouchDownY = -1;
    private Rect mTempRect = new Rect();

    public WeiboImageGrid(Context context) {
        this(context, null);
    }

    public WeiboImageGrid(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WeiboImageGrid(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mImageSpacing = (int) (getResources().getDisplayMetrics().density * IMAGE_SPACING_DP);
        for (int i = 0; i < MAX_IMAGE_COUNT; i++) {
            ImageView im = new ImageView(context);
            im.setScaleType(ImageView.ScaleType.CENTER_CROP);
            im.setVisibility(GONE);
            addView(im);
        }
    }

    public void setImage(String[] imageUrls) {
        mImageUrls = imageUrls;
        for (int i = 0; i < MAX_IMAGE_COUNT; i++) {
            ImageView im = (ImageView) getChildAt(i);
            im.setVisibility(i < imageUrls.length ? VISIBLE : GONE);
            if (i < imageUrls.length) {
                Glide.with(getContext()).load(imageUrls[i]).into(im);
            }
        }
    }

    private int makeExactSpec(int size) {
        return MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        D("on measure running");
        if (mImageUrls == null || mImageUrls.length == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            final int width = MeasureSpec.getSize(widthMeasureSpec);
            if (mImageUrls.length == 1) {
                mImageWidth = width;
                mImageHeight = (int) (width * RATIO);
            } else if (mImageUrls.length == 2) {
                mImageWidth = (width-mImageSpacing) / 2;
                mImageHeight = (int) (width * RATIO);
            } else {
                mImageWidth = (width-2*mImageSpacing) / 3;
                mImageHeight = mImageWidth;
            }
            for (int i = 0; i < getChildCount(); i++) {
                final View child = getChildAt(i);
                if (i < mImageUrls.length) {
                    child.measure(makeExactSpec(mImageWidth), makeExactSpec(mImageHeight));
                } else {
                    child.measure(0, 0);
                }
            }

            final int height;
            if (mImageUrls.length == 1 || mImageUrls.length == 2) {
                height = (int) (width * RATIO);
            } else {
                final int rows = (mImageUrls.length-1)/3 + 1;
                height = rows * (mImageHeight+mImageSpacing) - mImageSpacing;
            }
            setMeasuredDimension(width, height);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mImageUrls != null) {
            for (int i = 0; i < mImageUrls.length; i++) {
                ImageView im = (ImageView) getChildAt(i);
                final int curRow = i / 3;
                final int curCol = i - 3 * curRow;
                final int left = curCol * mImageWidth + curCol * mImageSpacing;
                final int top = curRow * mImageHeight + curRow * mImageSpacing;
                im.layout(left, top, left + mImageWidth, top + mImageHeight);
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            mLastTouchDownX = (int) ev.getX();
            mLastTouchDownY = (int) ev.getY();
        }
        return super.onInterceptTouchEvent(ev);
    }

    public int getLastClickChildIndex() {
        if (mLastTouchDownY < 0 || mLastTouchDownX < 0) {
            return 0;
        }
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            child.getHitRect(mTempRect);
            if (mTempRect.contains(mLastTouchDownX, mLastTouchDownY)) {
                return i;
            }
        }
        return 0;
    }

    private void D(String msg) {
        Log.d("TAG", msg);
    }
}
