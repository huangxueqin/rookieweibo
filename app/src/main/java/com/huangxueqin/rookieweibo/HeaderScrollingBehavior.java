package com.huangxueqin.rookieweibo;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Scroller;

/**
 * Created by huangxueqin on 2017/3/13.
 */

public class HeaderScrollingBehavior extends CoordinatorLayout.Behavior<RecyclerView> {

    View headerView;
    int headerBottom;

    Scroller scroller;

    public HeaderScrollingBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        scroller = new Scroller(context);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, RecyclerView child, View dependency) {
        if (dependency.getId() == R.id.weibo_content) {
            this.headerView = dependency;
            return true;
        }
        return super.layoutDependsOn(parent, child, dependency);
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, RecyclerView child, int layoutDirection) {
        headerBottom = headerView.getBottom();
        child.layout(0, headerBottom, parent.getMeasuredWidth(), parent.getMeasuredHeight()+headerBottom);
        return true;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, RecyclerView child, View dependency) {
        Log.d("TAG", "onDependentViewChanged...");
        child.setTranslationY(headerView.getTranslationY());
        return true;
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, RecyclerView child, View directTargetChild, View target, int nestedScrollAxes) {
        Log.d("TAG", "onStartNestedScroll");
        Log.d("TAG", "directTargetChild = " + directTargetChild.toString());
        Log.d("TAG", "target = " + target.toString());
        Log.d("TAG", "nestedScrollAxes & VERTICAL = " + (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) + ", " +
                "nestedScrollAxes & Horizontal = " + (nestedScrollAxes & ViewCompat.SCROLL_AXIS_HORIZONTAL));
        if (target instanceof RecyclerView && (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0) {
            return true;
        }
        return false;
    }

    @Override
    public void onNestedScrollAccepted(CoordinatorLayout coordinatorLayout, RecyclerView child, View directTargetChild, View target, int nestedScrollAxes) {
        Log.d("TAG", "onNestedScrollAccepted");
        if (!scroller.isFinished()) {
            scroller.abortAnimation();
        }
    }

    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, RecyclerView child, View target, int dx, int dy, int[] consumed) {
        Log.d("TAG", "dy = " + dy + ", consumed[0] = " + consumed[0] + ", consumed[1] = " + consumed[1]);
        if (dy < 0) return;
        float minTranslationY = -headerBottom;
        float newTranslationY = headerView.getTranslationY() - dy;
        if (newTranslationY <= minTranslationY) {
            consumed[1] = (int) (dy-(minTranslationY-newTranslationY));
            newTranslationY = minTranslationY;
        } else {
            consumed[1] = dy;
        }
        headerView.setTranslationY(newTranslationY);
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, RecyclerView child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        Log.d("TAG", "onNestedScroll");
        if (dyUnconsumed > 0) return;
        final float newTranslationY = headerView.getTranslationY() - dyUnconsumed;
        headerView.setTranslationY(Math.min(0, newTranslationY));
    }

    @Override
    public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, RecyclerView child, View target) {
        Log.d("TAG", "onStopNestedScroll");
        Log.d("TAG", "target is " + target.toString());
        onUserStopDrag(child, 800);
    }

    @Override
    public boolean onNestedPreFling(CoordinatorLayout coordinatorLayout, RecyclerView child, View target, float velocityX, float velocityY) {
        Log.d("TAG", "onNestedPreFling");
        return onUserStopDrag(child, velocityY);
    }

    private boolean onUserStopDrag(final View view, float velocity) {
        final float currentTranslationY = headerView.getTranslationY();
        final float minTranslationY = -headerBottom;
        final float targetTranslationY;
        if (velocity <= 800) {
            targetTranslationY = (currentTranslationY < minTranslationY/2) ? minTranslationY : 0;
            velocity = 800;
        } else {
            targetTranslationY = (velocity > 0) ? minTranslationY : 0;
        }

        scroller.startScroll(0, (int) (currentTranslationY), 0, (int) (targetTranslationY-currentTranslationY), (int) (100000/Math.abs(velocity)));
        view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (scroller.computeScrollOffset()) {
                    headerView.setTranslationY(scroller.getCurrY());
                    view.postInvalidate();
                    return true;
                } else {
                    view.getViewTreeObserver().removeOnPreDrawListener(this);
                    return false;
                }
            }
        });
        view.invalidate();
        return true;
    }
}
