package com.huangxueqin.rookieweibo.weibodetail;

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

import com.huangxueqin.rookieweibo.R;

import java.lang.ref.WeakReference;

/**
 * Created by huangxueqin on 2017/3/13.
 */

public class HeaderScrollingBehavior extends CoordinatorLayout.Behavior<View> {

    final int headerCollapseHeight;
    int headerHeight;

    WeakReference<View> headerView;
    Scroller scroller;

    public HeaderScrollingBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        scroller = new Scroller(context);
        headerCollapseHeight = context.getResources().getDimensionPixelSize(R.dimen.toolbar_height);
    }

    private View getHeaderView() {
        return headerView.get();
    }

    private void setHeaderView(View view) {
        headerView = new WeakReference<View>(view);
    }

    private int getMinHeaderTranslationY() {
        return headerCollapseHeight - headerHeight;
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        if (dependency.getId() == R.id.scroll_header) {
            setHeaderView(dependency);
            return true;
        }
        return super.layoutDependsOn(parent, child, dependency);
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, View child, int layoutDirection) {
        Log.d("TAG", "onLayoutChild");

        headerHeight = getHeaderView().getMeasuredHeight();
        final int parentHeight = parent.getMeasuredHeight();
        final int parentWidth = parent.getMeasuredWidth();
        child.layout(0, headerHeight, parentWidth, parentHeight+headerHeight-headerCollapseHeight);
        return true;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        Log.d("TAG", "onDependentViewChanged...");

        child.setTranslationY(getHeaderView().getTranslationY());
        return true;
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, View child, View directTargetChild, View target, int nestedScrollAxes) {
        Log.d("TAG", "onStartNestedScroll");

        return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedScrollAccepted(CoordinatorLayout coordinatorLayout, View child, View directTargetChild, View target, int nestedScrollAxes) {
        Log.d("TAG", "onNestedScrollAccepted");

        if (!scroller.isFinished()) {
            scroller.abortAnimation();
        }
        super.onNestedScrollAccepted(coordinatorLayout, child, directTargetChild, target, nestedScrollAxes);
    }

    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, View child, View target, int dx, int dy, int[] consumed) {
        Log.d("TAG", "dy = " + dy + ", consumed[0] = " + consumed[0] + ", consumed[1] = " + consumed[1]);

        if (dy < 0) return;

        final float minTranslationY = getMinHeaderTranslationY();
        final float headerTranslationY = getHeaderView().getTranslationY();
        if (headerTranslationY > minTranslationY) {
            final float deltaTranslationY = headerTranslationY-minTranslationY;
            consumed[1] = (int) Math.min(dy, deltaTranslationY);
            getHeaderView().setTranslationY(headerTranslationY - consumed[1]);
        }
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, View child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        Log.d("TAG", "onNestedScroll");

        if (dyUnconsumed > 0) return;

        final float newTranslationY = getHeaderView().getTranslationY() - dyUnconsumed;
        getHeaderView().setTranslationY(Math.min(0, newTranslationY));
    }

    @Override
    public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, View child, View target) {
        Log.d("TAG", "onStopNestedScroll");
//        Log.d("TAG", "target is " + target.toString());
//        onUserStopDrag(child, 800);
    }

    @Override
    public boolean onNestedPreFling(CoordinatorLayout coordinatorLayout, View child, View target, float velocityX, float velocityY) {
        Log.d("TAG", "onNestedPreFling");
        return onUserStopDrag(child, velocityY);
    }

    private boolean onUserStopDrag(final View view, float velocity) {
        final float currentTranslationY = getHeaderView().getTranslationY();
        final float minTranslationY = getMinHeaderTranslationY();

        if (currentTranslationY == 0 || currentTranslationY == minTranslationY) {
            return false;
        }

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
                    getHeaderView().setTranslationY(scroller.getCurrY());
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
