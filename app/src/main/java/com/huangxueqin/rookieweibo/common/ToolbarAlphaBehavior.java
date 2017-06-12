package com.huangxueqin.rookieweibo.common;

import android.graphics.Color;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by huangxueqin on 2017/6/10.
 */

public class ToolbarAlphaBehavior extends CoordinatorLayout.Behavior<View> {
    private static final String TAG = "ToolbarAlphaBehavior";

    private int mLimit;
    private RecyclerView mTrackingView;

    public ToolbarAlphaBehavior(RecyclerView trackingView, int limit) {
        mTrackingView = trackingView;
        mLimit = limit;
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return dependency == mTrackingView;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        int offset = mTrackingView.computeVerticalScrollOffset();
        return false;
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, View child, View directTargetChild, View target, int nestedScrollAxes) {
        Logger.d(TAG, "onStartNestedScroll");
        return (nestedScrollAxes & CoordinatorLayout.SCROLL_AXIS_VERTICAL) != 0 && target == mTrackingView;
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, View child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        Logger.d(TAG, "onNestedScroll: child = " + child);
        Logger.d(TAG, "onNestedScroll: target = " + target);
        Logger.d(TAG, "onNestedScroll: dxConsumed = " + dxConsumed + ", dyConsumed = " + dyConsumed);
        Logger.d(TAG, "onNestedScroll: dxUnConsumed = " + dxUnconsumed + ", dyUnConsumed = " + dyUnconsumed);

        final int offset = mTrackingView.computeVerticalScrollOffset();
        Logger.d(TAG, "offset = " + offset);
        final float alpha;
        if (offset <= 0) {
            alpha = 1;
        } else if (offset >= mLimit) {
            alpha = 0;
        } else {
            alpha = 1 - offset / (float)mLimit;
        }
        child.setBackgroundColor(Color.argb((int)(255 * alpha), 0xff, 0xff, 0xff));
    }
}
