package com.huangxueqin.rookieweibo.slide_tab_layout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.huangxueqin.rookieweibo.R;

/**
 * Created by huangxueqin on 2017/1/23.
 */

public class SlideTabLayout extends HorizontalScrollView implements ViewPager.OnPageChangeListener {
    private static final String TAG = SlideTabLayout.class.getSimpleName();

    private static final float INDICATOR_HEIGHT = 1.5f;
    private static final float INDICATOR_MARGIN_BOTTOM = 5;

    private LinearLayout mTabContainer;
    private LayoutInflater mTabItemInflater;

    private Callback mCallback;
    private TabSelectListener mListener;
    private int mCurrentPosition = 0;

    private Paint mIndicatorPaint;
    private float mIndicatorHeight;
    private float mIndicatorMarginBottom;

    private boolean mIdle = true;
    private int mScrollPosition;
    private float mScrollPositionOffset;

    public SlideTabLayout(Context context) {
        this(context, null);
    }

    public SlideTabLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initDimension(context);
        mTabItemInflater = LayoutInflater.from(context);
        initScrollIndicatorPaint();
    }

    private void initDimension(Context context) {
        float density = context.getResources().getDisplayMetrics().density;
        mIndicatorHeight = density * INDICATOR_HEIGHT;
        mIndicatorMarginBottom = density * INDICATOR_MARGIN_BOTTOM;
    }

    private void initScrollIndicatorPaint() {
        mIndicatorPaint = new Paint();
        mIndicatorPaint.setAntiAlias(true);
        mIndicatorPaint.setStyle(Paint.Style.STROKE);
        mIndicatorPaint.setStrokeWidth(mIndicatorHeight);
        mIndicatorPaint.setStrokeCap(Paint.Cap.ROUND);
        mIndicatorPaint.setColor(getResources().getColor(R.color.slide_tab_title));
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTabContainer = (LinearLayout) findViewById(R.id.tab_container);
        if (mTabContainer == null) {
            mTabContainer = new LinearLayout(getContext());
            mTabContainer.setOrientation(LinearLayout.HORIZONTAL);
            addView(mTabContainer,
                    new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT, Gravity.CENTER));
        }
    }

    private void drawIndicator(Canvas canvas) {
        if (mTabContainer.getChildCount() <= 0) {
            return;
        }

        final float h = getHeight();
        final float tabW = (mTabContainer.getWidth()) / mCallback.getTabCount();
        final float tabStart = (getWidth() - mTabContainer.getWidth()) / 2;
        final float indicatorY =  h - mIndicatorMarginBottom;
        final float indicatorW = tabW / 4;

        float indicatorStartX;
        float indicatorEndX;
        if (mIdle) {
            indicatorStartX = tabStart + (tabW-indicatorW)/2 + tabW * mCurrentPosition;
            indicatorEndX = indicatorStartX + indicatorW;
        } else {
            indicatorStartX = tabStart + (tabW-indicatorW)/2 +
                    tabW * (mScrollPosition + (mCurrentPosition>mScrollPosition ? 1 : 0));
            indicatorEndX = indicatorStartX + indicatorW;

            final float newIndicatorW = indicatorW + (tabW/2-indicatorW)*(0.5f-Math.abs(mScrollPositionOffset-0.5f)) / 0.5f;
            // adjust startX and endX
            final float offsetX = 3/4.0f*tabW * (0.5f-Math.abs(mScrollPositionOffset-0.5f));
            if (mCurrentPosition <= mScrollPosition) {
                if (mScrollPositionOffset <= 0.5) {
                    indicatorStartX += offsetX;
                    indicatorEndX = indicatorStartX + newIndicatorW;
                } else {
                    indicatorEndX += tabW - offsetX;
                    indicatorStartX = indicatorEndX - newIndicatorW;
                }
            } else {
                if (mScrollPositionOffset < 0.5) {
                    indicatorStartX += offsetX - tabW;
                    indicatorEndX = indicatorStartX + newIndicatorW;
                } else {
                    indicatorEndX -= offsetX;
                    indicatorStartX = indicatorEndX - newIndicatorW;
                }
            }
        }

        canvas.drawLine(indicatorStartX,
                indicatorY,
                indicatorEndX,
                indicatorY,
                mIndicatorPaint);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        drawIndicator(canvas);
    }

    public void setCurrentItem(int position) {
        if (mCurrentPosition != position) {
            SlideTabCell prevPrimaryItem = (SlideTabCell) mTabContainer.getChildAt(mCurrentPosition);
            SlideTabCell currPrimaryItem = (SlideTabCell) mTabContainer.getChildAt(position);
            prevPrimaryItem.setSelected(false);
            currPrimaryItem.setSelected(true);
            mCurrentPosition = position;
        }
    }

    public void attach(Callback callback) {
        mCallback = callback;
        if (mCallback != null) {
            installTabs();
        }
    }

    public void setTabSelectListener(TabSelectListener listener) {
        mListener = listener;
    }

    private void installTabs() {
        mTabContainer.removeAllViews();
        final int tabCount = mCallback.getTabCount();
        for (int i = 0; i < tabCount; i++) {
            SlideTabCell tabItem = (SlideTabCell) mTabItemInflater.inflate(R.layout.view_slide_tab_cell, null);
            tabItem.setSelected(mCurrentPosition == i);
            tabItem.setTitle(mCallback.getTabTitle(i));
            tabItem.index = i;
            tabItem.setOnClickListener(mTabClickListener);
            mTabContainer.addView(tabItem,
                    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
    }

    private OnClickListener mTabClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            SlideTabCell tabItem = (SlideTabCell) v;
            SlideTabCell currentTabItem = (SlideTabCell) mTabContainer.getChildAt(mCurrentPosition);
            if (tabItem != currentTabItem) {
                mCurrentPosition = tabItem.index;
                currentTabItem.setSelected(false);
                tabItem.setSelected(true);
                if (mListener != null) {
                    mListener.onTabSelected(mCurrentPosition);
                }
            }
        }
    };

    /**
     * implementation of {@link ViewPager.OnPageChangeListener}
     */
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        mScrollPosition = position;
        mScrollPositionOffset = positionOffset;
        invalidate();
    }

    @Override
    public void onPageSelected(int position) {
        mCurrentPosition = position;
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        mIdle = state == ViewPager.SCROLL_STATE_IDLE;
        if (mIdle) {
            invalidate();
        }
    }

    public interface Callback {
        int getTabCount();
        String getTabTitle(int tabNdx);
    }

    public interface TabSelectListener {
        void onTabSelected(int tabNdx);
    }
}
