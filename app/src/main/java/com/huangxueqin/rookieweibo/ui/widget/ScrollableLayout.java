package com.huangxueqin.rookieweibo.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.support.v4.view.NestedScrollingParent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.Scroller;

import com.huangxueqin.rookieweibo.R;

/**
 * Created by huangxueqin on 2017/3/25.
 */

public class ScrollableLayout extends FrameLayout implements NestedScrollingParent {
    private static final int INVALID_POINTER = -1;

    private boolean mIsBeingDragged;
    private boolean mTouchInNestedChild;
    private int mTouchSlop;
    private int mLastMotionY;
    private int mActivePointerId = INVALID_POINTER;
    private VelocityTracker mVelocityTracker;

    private int mNestedChildId;
    private View mNestedChild;

    private int mCurrentOffset;
    private int mMinOffset;

    private Scroller mScroller;
    private FlingRunnable mFlingRunnable;
    private FlingTracker mFlingTracker;

    private ScrollableListener mScrollableListener;

    private Rect mTempRect = new Rect();

    public ScrollableLayout(@NonNull Context context) {
        this(context, null);
    }

    public ScrollableLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrollableLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (attrs != null) {
            initAttrs(context, attrs);
        }

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mCurrentOffset = 0;
        mMinOffset = 0;

        mScroller = new Scroller(context);
        mFlingTracker = new FlingTracker(context);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ScrollableLayout);
        mNestedChildId = ta.getResourceId(R.styleable.ScrollableLayout_nested_child_id, 0);
        ta.recycle();
    }

    public void setScrollableListener(ScrollableListener listener) {
        mScrollableListener = listener;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (mNestedChildId > 0) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (child.getId() == mNestedChildId) {
                    mNestedChild = child;
                    break;
                }
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int count = getChildCount();
        int totalContentHeight = 0;
        for (int i = 0; i < count; i++) {
            totalContentHeight += getChildAt(i).getMeasuredHeight();
        }
        mMinOffset = Math.min(0, getMeasuredHeight()-totalContentHeight);

        D("parent height = " + getMeasuredHeight());
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            D("child " + i + " height = " + child.getMeasuredHeight());
        }
        D("mMinOffset = " + mMinOffset);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int count = getChildCount();
        int childTop = getPaddingTop();
        for (int i = 0; i < count; i++) {
            View v = getChildAt(i);
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) v.getLayoutParams();
            final int marginLeft = lp.leftMargin;
            final int gravity = lp.gravity;
            final int childLeft;
            if (gravity == Gravity.CENTER_HORIZONTAL) {
                childLeft = left + (right-left-v.getMeasuredWidth())/2;
            } else {
                childLeft = left + marginLeft;
            }
            v.layout(childLeft,
                    childTop,
                    childLeft+v.getMeasuredWidth(),
                    childTop+v.getMeasuredHeight());
            childTop += v.getMeasuredHeight();
        }
    }

    /**
     * check if this scrollableLayout can scroll vertically,
     * scroll_down means offset becomes smaller
     * scroll_up means offset becomes larger
     * @param direction >0 scroll Down, < 0 scroll up
     * @return
     */
    @Override
    public boolean canScrollVertically(int direction) {
        if (mMinOffset != 0) {
            if (direction > 0) {
                return mCurrentOffset > mMinOffset;
            }
            if (direction < 0) {
                return mCurrentOffset < 0;
            }
        }
        return false;
    }

    public boolean canScrollUp() {
        return canScrollVertically(-1);
    }

    public boolean canScrollDown() {
        return canScrollVertically(1);
    }

    /**
     * new offset = mCurrentOffset + dy
     * @param dy
     */
    private void offsetContent(int dy) {
        if (mMinOffset != 0) {
            int deltaOffset = 0;
            if (dy > 0 && canScrollUp()) {
                deltaOffset = Math.min(dy, -mCurrentOffset);
            } else if (dy < 0 && canScrollDown()) {
                deltaOffset = Math.max(dy, mMinOffset-mCurrentOffset);
            }
            if (deltaOffset != 0) {
                final int count = getChildCount();
                for (int i = 0; i < count; i++) {
                    getChildAt(i).offsetTopAndBottom(deltaOffset);
                }
                mCurrentOffset += deltaOffset;

                D("offset content by: " + deltaOffset);
            }
        }
    }

    private void ensureVelocityTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getActionMasked();

        if (mTouchInNestedChild) {
            return false;
        }

        if (action == MotionEvent.ACTION_MOVE && mIsBeingDragged) {
            return true;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mIsBeingDragged = false;
                final int x = (int) ev.getX();
                final int y = (int) ev.getY();

                if (mNestedChild != null) {
                    mNestedChild.getHitRect(mTempRect);
                    if (mTempRect.contains(x, y)) {
                        mTouchInNestedChild = true;
                        break;
                    }
                }

                mLastMotionY = y;
                mActivePointerId = ev.getPointerId(0);
                ensureVelocityTracker();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                final int activePointerId = mActivePointerId;
                if (activePointerId == INVALID_POINTER) {
                    break;
                }
                final int pointerIndex = ev.findPointerIndex(activePointerId);
                if (pointerIndex == -1) {
                    break;
                }

                final int y = (int) ev.getY(pointerIndex);

                final int yDiff = Math.abs(y - mLastMotionY);
                if (yDiff > mTouchSlop) {
                    mIsBeingDragged = true;
                    mLastMotionY = y;
                }
                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mIsBeingDragged = false;
                mTouchInNestedChild = false;
                mActivePointerId = INVALID_POINTER;
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
        }

        if (mVelocityTracker != null) {
            mVelocityTracker.addMovement(ev);
        }

        return mIsBeingDragged;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                final int y = (int) event.getY();
                mLastMotionY = y;
                mActivePointerId = event.getPointerId(0);
                ensureVelocityTracker();
                break;
            }
            case MotionEvent.ACTION_MOVE:
                final int activePointerId = mActivePointerId;
                if (activePointerId == INVALID_POINTER) {
                    return false;
                }
                final int activePointerIndex = event.findPointerIndex(activePointerId);
                if (activePointerIndex == -1) {
                    return false;
                }
                final int y = (int) event.getY(activePointerIndex);
                int dy = y-mLastMotionY;
                if (!mIsBeingDragged && Math.abs(dy) > mTouchSlop) {
                    mIsBeingDragged = true;
                    if (dy > 0) {
                        dy -= mTouchSlop;
                    } else {
                        dy += mTouchSlop;
                    }
                }

                if (mIsBeingDragged) {
                    mLastMotionY = y;
                    offsetContent(dy);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mVelocityTracker != null) {
                    mVelocityTracker.addMovement(event);
                    mVelocityTracker.computeCurrentVelocity(1000);
                    float yVel = mVelocityTracker.getYVelocity(mActivePointerId);
                    fling(yVel);
                }
            case MotionEvent.ACTION_CANCEL:
                mTouchInNestedChild = false;
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
        }

        if (mVelocityTracker != null) {
            mVelocityTracker.addMovement(event);
        }

        return true;
    }

    private void fling(float velocityY) {

        if (mFlingRunnable != null) {
            removeCallbacks(mFlingRunnable);
            mFlingRunnable = null;
        }

        mScroller.fling(0, 0, 0, Math.round(velocityY), 0, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);

        if (mScroller.computeScrollOffset()) {
            mFlingRunnable = new FlingRunnable();
            postOnAnimation(mFlingRunnable);
        }
    }

    /* NestedScrollingParent interfaces, in these methods, dy > 0 means offset became smaller */

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        D("onStartNestedScroll");
        return (nestedScrollAxes & SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes) {
        D("onNestedScrollAccepted");
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }

        mFlingTracker.unRegisterFromTarget();
    }

    @Override
    public void onStopNestedScroll(View child) {
        D("onStopNestedScroll");
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        D("target: " + target.toString() + ", dyConsumed = " + dyConsumed + ", dyUnconsumed = " + dyUnconsumed);
        D("mCurrentOffset = " + mCurrentOffset);
        if (dyUnconsumed < 0 && canScrollUp()) {
            offsetContent(-dyUnconsumed);
        }
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        D("target: " + target.toString() + ", dy = " + dy);
        D("mCurrentOffset = " + mCurrentOffset);
        if (dy > 0 && canScrollDown()) {
            final int consumedY = Math.min(dy, mCurrentOffset-mMinOffset);
            D("consumedY = " + consumedY);
            if (consumedY != 0) {
                offsetContent(-consumedY);
            }
            consumed[1] = consumedY;
        }
    }

    @Override
    public boolean onNestedFling(final View target, final float velocityX, float velocityY, boolean consumed) {
        if (!consumed || !target.canScrollVertically(-1)) {
            fling(-velocityY);
        } else if (velocityY < 0 && canScrollUp()) {
            mFlingTracker.registerToTarget(target, -velocityY);
        }
        return super.onNestedFling(target, velocityX, velocityY, consumed);
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        if (canScrollDown() && velocityY > 0) {
            fling(-velocityY);
            return true;
        }
        return super.onNestedPreFling(target, velocityX, velocityY);
    }



    private class FlingTracker implements ViewTreeObserver.OnScrollChangedListener {
        private final Scroller mTrackerScroller;

        private View mTarget;

        public FlingTracker(Context context) {
            mTrackerScroller = new Scroller(context);
        }

        @Override
        public void onScrollChanged() {
            if (!mTarget.canScrollVertically(-1)) {
                if (mTrackerScroller.computeScrollOffset()) {
                    final float velY = mTrackerScroller.getCurrVelocity();
                    fling(velY);
                    unRegisterFromTarget();
                }
            }
        }

        public void registerToTarget(View target, float velocity) {
            if (mTarget != null) {
                mTarget.getViewTreeObserver().removeOnScrollChangedListener(this);
                mTrackerScroller.abortAnimation();
            }
            mTarget = target;
            mTarget.getViewTreeObserver().addOnScrollChangedListener(this);
            mTrackerScroller.fling(0, 0, 0, Math.round(velocity), 0, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
        }

        public void unRegisterFromTarget() {
            if (mTarget != null) {
                mTarget.getViewTreeObserver().removeOnScrollChangedListener(this);
            }
            if (!mTrackerScroller.isFinished()) {
                mTrackerScroller.abortAnimation();
            }
        }
    }

    private class FlingRunnable implements Runnable {
        private int mLastY = 0;

        @Override
        public void run() {
            if (mScroller.computeScrollOffset()) {
                final int currY = mScroller.getCurrY();
                final int dy = currY-mLastY;
                if (dy < 0 && canScrollDown() || dy > 0 && canScrollUp()) {
                    offsetContent(dy);
                    postOnAnimation(this);
                } else {
                    if (dy < 0 && mScrollableListener != null) {
                        mScrollableListener.onFlingToBottom((dy > 0 ? -1 : 1) * mScroller.getCurrVelocity());
                    }
                }
                mLastY = currY;
            }
        }
    }

    public interface ScrollableListener {
        void onFlingToBottom(float velocityY);
    }

    private void D(String msg) {
        Log.d("TAG", msg);
    }
}
