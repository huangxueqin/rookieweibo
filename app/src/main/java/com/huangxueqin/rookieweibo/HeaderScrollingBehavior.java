package com.huangxueqin.rookieweibo;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by huangxueqin on 2017/3/13.
 */

public class HeaderScrollingBehavior extends CoordinatorLayout.Behavior<LinearLayout> {

    private boolean isHeadHide = false;
    private boolean isAnimating = false;
    private final int SCROOL_VALUE = 50;
    private int childHeight;
    private final int animationDuration = 500;

    private View dependency;

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, LinearLayout child, View dependency) {
        if (dependency.getId() == R.id.weibo_content) {
            this.dependency = dependency;
            return true;
        }
        return false;
    }

    public HeaderScrollingBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }



    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, LinearLayout child, int layoutDirection) {
        child.layout(0, dependency.getMeasuredHeight(), parent.getMeasuredWidth(), parent.getMeasuredHeight());
        return true;
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, LinearLayout child, View directTargetChild, View target, int nestedScrollAxes) {
        if (target.getId() == R.id.scroll_body) {
            if (childHeight == 0) {
                childHeight = child.getHeight();
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, LinearLayout child, View target, int dx, int dy, int[] consumed) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed);
        if (isAnimating) {
            return;
        }
        if (dy > SCROOL_VALUE && !isHeadHide) {
            hide(child, target);
        } else if (dy < -SCROOL_VALUE && isHeadHide) {
            show(child, target);
        }
    }

    public void hide(final View child, final View target) {
        isHeadHide = true;
        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setIntValues(0, childHeight);
        valueAnimator.setDuration(animationDuration);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (child.getBottom() > 0) {
                    int value = (int) animation.getAnimatedValue();
                    isAnimating = value != childHeight;
                    child.layout(child.getLeft(), -value, child.getRight(), -value + childHeight);
                    target.layout(target.getLeft(), -value + childHeight, target.getRight(), target.getBottom());
                }
            }
        });
        valueAnimator.start();
    }

    public void show(final View child, final View target) {
        isHeadHide = false;
        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setIntValues(0, childHeight);
        valueAnimator.setDuration(animationDuration);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (child.getBottom() < childHeight) {
                    int value = (int) animation.getAnimatedValue();
                    isAnimating = value != childHeight;
                    child.layout(child.getLeft(), value - childHeight, child.getRight(), value);
                    target.layout(target.getLeft(), value, target.getRight(), target.getBottom());
                }
            }
        });
        valueAnimator.start();
    }
}
