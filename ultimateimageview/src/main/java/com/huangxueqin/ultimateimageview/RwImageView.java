package com.huangxueqin.ultimateimageview;

import android.annotation.TargetApi;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.io.File;

/**
 * Created by huangxueqin on 2017/6/7.
 */

public class RwImageView extends android.support.v7.widget.AppCompatImageView {

    private static final String TAG = "RwImageView";

    private GestureDetector mGestureDetector;

    public RwImageView(Context context) {
        this(context, null);
    }

    public RwImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RwImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mGestureDetector = new GestureDetector(context, mGestureListener);
        setOnTouchListener(mOnTouchListener);
    }

    public void setLargeImage(File imageFile) {

    }

    private OnTouchListener mOnTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return mGestureDetector.onTouchEvent(event);
        }
    };

    private GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            Log.d(TAG, "onScroll distanceX = " + distanceX + ", distanceY = " + distanceY);
            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    };


}
