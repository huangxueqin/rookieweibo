package com.huangxueqin.rookieweibo.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

import com.huangxueqin.rookieweibo.R;

import java.lang.ref.WeakReference;

/**
 * Created by huangxueqin on 2017/4/25.
 */

public class PageIndicator extends View {
    private static final int INVALID_DIMENSION = -1;

    static final int DEFAULT_SELECTED_COLOR = 0x80ffffff;
    static final int DEFAULT_UNSELECTED_COLOR = 0xffffffff;

    int mPageCount;
    int mPosition;
    float mPositionOffset;

    int mPicWidth;
    int mPicHeight;
    int mPicSpacing;

    int mUnSelectedColor;
    int mSelectedColor;

    Paint mPaint;

    public PageIndicator(Context context) {
        this(context, null);
    }

    public PageIndicator(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PageIndicator(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttributes(context, attrs);

        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
    }

    private void initAttributes(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.PageIndicator);

            int picSize = ta.getDimensionPixelSize(R.styleable.PageIndicator_pic_size, INVALID_DIMENSION);
            if (picSize > 0) {
                mPicWidth = mPicHeight = picSize;
            } else {
                mPicWidth = ta.getDimensionPixelSize(R.styleable.PageIndicator_pic_width, INVALID_DIMENSION);
                mPicHeight = ta.getDimensionPixelOffset(R.styleable.PageIndicator_pic_height, INVALID_DIMENSION);
            }
            mPicSpacing = ta.getDimensionPixelSize(R.styleable.PageIndicator_pic_spacing, mPicWidth);

            mUnSelectedColor = ta.getColor(R.styleable.PageIndicator_indicatorColor, DEFAULT_SELECTED_COLOR);
            mSelectedColor = ta.getColor(R.styleable.PageIndicator_indicatorTintColor, DEFAULT_UNSELECTED_COLOR);
            ta.recycle();
        }
    }

    public void setPageCount(int count) {
        if (mPageCount != count) {
            mPageCount = count;
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mPicHeight < 0 || mPicWidth < 0) {
            throw new IllegalStateException("Must Specify the size of PageIndicator cells");
        }

        final int picWidth = mPicWidth;
        final int picHeight = mPicHeight;
        final int picSpacing = mPicSpacing;

        int dw = getPaddingLeft() + getPaddingRight() + (picWidth + picSpacing)*mPageCount - mPicSpacing;
        int dh = getPaddingTop() + getPaddingRight() + picHeight;

        final int measureWidth = resolveSizeAndState(dw, widthMeasureSpec, 0);
        final int measureHeight = resolveSizeAndState(dh, heightMeasureSpec, 0);

        setMeasuredDimension(measureWidth, measureHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (int i = 0; i < mPageCount; i++) {
            drawCircle(canvas, i);
        }
    }

    private void drawCircle(Canvas canvas, int index) {
        int w = getWidth();
        int h = getHeight();

        int intrinsicWidth = (mPicWidth+mPicSpacing) * mPageCount - mPicSpacing;
        int intrinsicHeight = mPicWidth;

        float radius = mPicWidth / 2f;

        mPaint.setColor(mUnSelectedColor);
        float cx = (w-intrinsicWidth)/2f + (mPicWidth+mPicSpacing) * index + radius/2;
        float cy = (h-intrinsicWidth)/2f + radius/2;
        canvas.drawCircle(cx, cy, radius, mPaint);

        if (index == mPosition) {
            mPaint.setColor(mSelectedColor);
            float cx2 = cx + (mPicWidth+mPicSpacing)*mPositionOffset;
            float cy2 = cy;
            canvas.drawCircle(cx2, cy2, radius, mPaint);
        }
    }

    public void onPageScrolled(int position, float offset) {
        mPosition = position;
        mPositionOffset = offset;
        invalidate();
    }

    public void setViewPager(final ViewPager viewPager) {
        final DataSetObserver observer = new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                setPageCount(viewPager.getAdapter().getCount());
            }
        };

        if (viewPager.getAdapter() != null) {
            viewPager.getAdapter().registerDataSetObserver(observer);
        }

        viewPager.addOnAdapterChangeListener(new ViewPager.OnAdapterChangeListener() {
            @Override
            public void onAdapterChanged(@NonNull ViewPager viewPager, @Nullable PagerAdapter oldAdapter, @Nullable PagerAdapter newAdapter) {
                if (oldAdapter != null) {
                    oldAdapter.unregisterDataSetObserver(observer);
                }
                if (newAdapter != null) {
                    newAdapter.registerDataSetObserver(observer);
                }
            }
        });

        viewPager.addOnPageChangeListener(new PageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                PageIndicator.this.onPageScrolled(position, positionOffset);
            }
        });
    }

    public static abstract class PageChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageSelected(int position) {}

        @Override
        public void onPageScrollStateChanged(int state) {}
    }
}
