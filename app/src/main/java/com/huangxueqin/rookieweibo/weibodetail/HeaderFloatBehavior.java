package com.huangxueqin.rookieweibo.weibodetail;

import android.content.Context;
import android.graphics.Rect;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.huangxueqin.rookieweibo.R;

/**
 * Created by huangxueqin on 2017/3/15.
 */

public class HeaderFloatBehavior extends CoordinatorLayout.Behavior<View> {

    final int collapseHeight;
    final int touchSlop;
    boolean isDragging = false;
    float lastDownY;
    float lastDownX;
    Rect rect = new Rect();

    public HeaderFloatBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        collapseHeight = context.getResources().getDimensionPixelSize(R.dimen.toolbar_height);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, View child, MotionEvent ev) {
        Log.d("TAG", "onInterceptTouchEvent "+ ev);
        final float x = ev.getX();
        final float y = ev.getY();

        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastDownX = x;
                lastDownY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d("TAG", "y = " + y + ", lastDownY = " + lastDownY);
                child.getHitRect(rect);
                if (rect.contains((int)lastDownX, (int)lastDownY)) {
                    if (Math.abs(y - lastDownY) >= touchSlop) {
                        isDragging = true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isDragging = false;
                break;
        }
        return isDragging;
    }

    @Override
    public boolean onTouchEvent(CoordinatorLayout parent, View child, MotionEvent ev) {
        final float x = ev.getX();
        final float y = ev.getY();

        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_MOVE:
                child.setTranslationY(y - lastDownY);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isDragging = false;
                break;
        }
        return isDragging;
    }
}
