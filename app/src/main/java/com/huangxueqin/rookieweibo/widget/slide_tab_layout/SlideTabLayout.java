package com.huangxueqin.rookieweibo.widget.slide_tab_layout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.huangxueqin.rookieweibo.R;

/**
 * Created by huangxueqin on 2017/1/23.
 */

public class SlideTabLayout extends HorizontalScrollView implements ViewPager.OnPageChangeListener {
    private static final String TAG = SlideTabLayout.class.getSimpleName();

    // cons for attr "tabGravity"
    public static final int GRAVITY_LEFT = 1;
    public static final int GRAVITY_RIGHT = 2;
    public static final int GRAVITY_CENTER = 3;
    public enum TabGravity {
        LEFT(1),
        RIGHT(2),
        CENTER(3);

        private final int id;

        TabGravity(int id) {
            this.id = id;
        }

        public int getValue() {
            return id;
        }
    }

    // cons for attr "tabMode"
    public static final int MODE_FIT_WIDTH = 1;
    public static final int MODE_FIT_SCREEN = 2;
    public static final int MODE_FIX_WIDTH = 3;
    public enum TabMode {
        FIT_WIDTH(1),
        FIT_SCREEN(2),
        FIX_WIDTH(3);

        private final int id;

        TabMode(int id) {
            this.id = id;
        }

        public int getValue() {
            return id;
        }
    }

    // cons for attr "tabStyle"
    public static final int STYLE_PLAIN_TEXT = 1;
    public static final int STYLE_PLAIN_ICON = 2;
    public static final int STYLE_ICON_TEXT = 3;
    public static final int STYLE_CUSTOM = 4;
    public enum TabStyle {
        PLAIN_TEXT(1),
        PLAIN_ICON(2),
        ICON_TEXT(3),
        CUSTOM(4);

        private final int id;

        TabStyle(int id) {
            this.id = id;
        }

        public int getValue() {
            return id;
        }
    }

    private static final float INDICATOR_HEIGHT = 1.5f;
    private static final float INDICATOR_MARGIN_BOTTOM = 5;

    private LayoutInflater mTabCellInflater;
    private LinearLayout mTabParent;
    private int mTabGravity;
    private int mTabMode;
    private int mTabStyle;
    // used in FIX_WIDTH mode
    private int mTabWidth;

    private Adapter mAdapter;
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
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SlideTabLayout);
        mTabGravity = ta.getInt(R.styleable.SlideTabLayout_tabGravity, GRAVITY_CENTER);
        mTabMode = ta.getInt(R.styleable.SlideTabLayout_tabMode, MODE_FIT_SCREEN);
        mTabStyle = ta.getInt(R.styleable.SlideTabLayout_tabStyle, STYLE_PLAIN_TEXT);
        mTabWidth = ta.getDimensionPixelSize(R.styleable.SlideTabLayout_tabWidth, 0);
        ta.recycle();

        mTabCellInflater = LayoutInflater.from(context);
        // create {@link mTabParent}
        mTabParent = new LinearLayout(context);
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        if (mTabGravity == GRAVITY_LEFT) {
            lp.gravity = Gravity.LEFT;
        } else if (mTabGravity == GRAVITY_RIGHT) {
            lp.gravity = Gravity.RIGHT;
        } else {
            lp.gravity = Gravity.CENTER;
        }
        addView(mTabParent, lp);

        initDimension(context);
        initScrollIndicatorPaint();
    }

    public void setTabGravity(int gravity) {
        if (mTabGravity == gravity) return;
        LayoutParams lp = (LayoutParams) mTabParent.getLayoutParams();
        mTabGravity = gravity;
        switch (mTabGravity) {
            case GRAVITY_LEFT:
                lp.gravity = Gravity.LEFT;
                break;
            case GRAVITY_RIGHT:
                lp.gravity = Gravity.RIGHT;
                break;
            case GRAVITY_CENTER:
                lp.gravity = Gravity.CENTER;
                break;
        }
        mTabParent.setLayoutParams(lp);
    }

    public void setTabMode(int tabMode) {
        if (mTabMode == tabMode) return;
        mTabMode = tabMode;
        for (int i = 0; i < mTabParent.getChildCount(); i++) {
            View cell = mTabParent.getChildAt(i);
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) cell.getLayoutParams();
            if (tabMode == MODE_FIT_SCREEN) {
                lp.width = 0;
                lp.weight = 1;
            } else if (tabMode == MODE_FIX_WIDTH) {
                lp.width = mTabWidth;
            } else {
                lp.width = ViewPager.LayoutParams.WRAP_CONTENT;
            }
            cell.setLayoutParams(lp);
        }
    }

    public void setAdapter(Adapter adapter) {
        if (mAdapter == adapter) return;
        mAdapter = adapter;


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
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mTabWidth == 0) {
            for (int i = 0; i < mTabParent.getChildCount(); i++) {
                mTabWidth = Math.max(mTabWidth, getChildAt(i).getMeasuredWidth());
            }
        }
    }

    private void drawIndicator(Canvas canvas) {
        if (mTabParent.getChildCount() <= 0) {
            return;
        }

        final float h = getHeight();
        final float tabW = (mTabParent.getWidth()) / mCallback.getTabCount();
        final float tabStart = (getWidth() - mTabParent.getWidth()) / 2;
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
            SlideTabCell prevPrimaryItem = (SlideTabCell) mTabParent.getChildAt(mCurrentPosition);
            SlideTabCell currPrimaryItem = (SlideTabCell) mTabParent.getChildAt(position);
            prevPrimaryItem.setSelected(false);
            currPrimaryItem.setSelected(true);
            mCurrentPosition = position;
        }
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
        if (mCallback != null) {
            installTabs();
        }
    }

    public void setTabSelectListener(TabSelectListener listener) {
        mListener = listener;
    }

    private void installTabs() {
        mTabParent.removeAllViews();
        final int tabCount = mCallback.getTabCount();
        for (int i = 0; i < tabCount; i++) {
            SlideTabCell tabItem = (SlideTabCell) mTabCellInflater.inflate(R.layout.view_slide_tab_cell, null);
            tabItem.setSelected(mCurrentPosition == i);
            tabItem.setTitle(mCallback.getTabTitle(i));
            tabItem.index = i;
            tabItem.setOnClickListener(mTabClickListener);
            mTabParent.addView(tabItem,
                    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
    }

    private OnClickListener mTabClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            SlideTabCell tabItem = (SlideTabCell) v;
            SlideTabCell currentTabItem = (SlideTabCell) mTabParent.getChildAt(mCurrentPosition);
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

    public static abstract class Adapter {
        public abstract int getTabCount();

        public String getTabTitle(int tabNdx) {
            return null;
        }

        public int getTabIcon(int tabNdx) {
            return -1;
        }

        public Drawable getTabIconDrawable(int tabNdx) {
            return null;
        }

        public View getTabView(int tabNdx) {
            return null;
        }
    }

    public interface TabSelectListener {
        void onTabSelected(int tabNdx);
    }
}
