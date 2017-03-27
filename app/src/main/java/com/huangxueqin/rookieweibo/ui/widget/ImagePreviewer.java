package com.huangxueqin.rookieweibo.ui.widget;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * Created by huangxueqin on 2017/3/26.
 */

public class ImagePreviewer extends android.support.v7.widget.AppCompatImageView {

    private boolean mPreviewEnabled;

    private boolean mIsBeingDragged;

    private int mTouchSlop;
    private int mActivePointerId = -1;
    private int mLastMotionX;
    private int mLastMotionY;

    private int mDrawableWidth;
    private int mDrawableHeight;

    private float mBaseScale;
    private float mBaseOffsetX;
    private float mBaseOffsetY;

    private float mLargerScale;

    private Matrix mBaseMatrix = new Matrix();
    private Matrix mSuppMatrix = new Matrix();
    private Matrix mDrawMatrix = new Matrix();
    private Matrix mDisplayMatrix = new Matrix();

    private ScaleType savedScaleType;
    private OnClickListener mOnClickListener;
    private GestureDetector mGestureDetector;
    private SimpleGestureDetectorListener mGestureListener;

    private RectF mTempRectF = new RectF();

    public ImagePreviewer(Context context) {
        this(context, null);
    }

    public ImagePreviewer(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImagePreviewer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        savedScaleType = getScaleType();

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mGestureListener = new SimpleGestureDetectorListener();
        mGestureDetector = new GestureDetector(context, mGestureListener);
        mGestureDetector.setOnDoubleTapListener(mGestureListener);
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        if (!isClickable()) {
            setClickable(true);
        }
        mOnClickListener = l;
        if (!mPreviewEnabled) {
            super.setOnClickListener(l);
        }
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        super.setImageDrawable(drawable);

        if (drawableFineToPreview()) {
            mDrawableWidth = drawable.getIntrinsicWidth();
            mDrawableHeight = drawable.getIntrinsicHeight();
            enablePreview();

            if (getWidth() > 0 && getHeight() > 0) {
                resetImageMatrix(getWidth(), getHeight());
            }
        } else {
            disablePreview();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (mPreviewEnabled) {
            resetImageMatrix(w, h);
        }
    }

    private boolean drawableFineToPreview() {
        Drawable d = getDrawable();
        return d != null && d.getIntrinsicWidth() > 0 && d.getIntrinsicHeight() > 0;
    }

    private void enablePreview() {
        mPreviewEnabled = true;
        setScaleType(ScaleType.MATRIX);
        super.setOnClickListener(mDoubleClickListener);
    }

    private void disablePreview() {
        mPreviewEnabled = false;
        setScaleType(savedScaleType);
        super.setOnClickListener(mOnClickListener);
    }

    /**
     * update image matrix by size of drawable and this view
     * @param w
     * @param h
     */
    private void resetImageMatrix(int w, int h) {

        final float drawableRatio = mDrawableWidth / (float)mDrawableHeight;
        final float viewRatio = w / (float)h;

        if (drawableRatio > viewRatio) {
            mBaseScale = w / (float)mDrawableWidth;
            mLargerScale = h / (float)mDrawableHeight;
        } else if (drawableRatio < viewRatio) {
            mBaseScale = h / (float)mDrawableHeight;
            mLargerScale = w / (float)mDrawableWidth;
        } else {
            mLargerScale = 1.5f * (mBaseScale = w / (float)mDrawableWidth);
        }

        mBaseOffsetX = (w-mDrawableWidth*mBaseScale) / 2;
        mBaseOffsetY = (h-mDrawableHeight*mBaseScale) / 2;

        mBaseMatrix.reset();
        mBaseMatrix.postScale(mBaseScale, mBaseScale);
        mBaseMatrix.postTranslate(mBaseOffsetX, mBaseOffsetY);

        mSuppMatrix.reset();

        updateImageMatrix();
    }

    private void updateImageMatrix() {
        mDrawMatrix.set(mBaseMatrix);
        mDrawMatrix.postConcat(mSuppMatrix);
        setImageMatrix(mDrawMatrix);
    }

    private void getDisplayRect(RectF rectF) {
        Drawable d = getDrawable();
        rectF.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        Matrix matrix = getMatrix();
        matrix.mapRect(rectF);
    }

    private boolean canDragVertically(int directionY) {
        if (!mPreviewEnabled) {
            return false;
        }

        getDisplayRect(mTempRectF);
        if (directionY > 0) {
            return mTempRectF.top < 0;
        } else {
            return mTempRectF.bottom > getHeight();
        }
    }

    private boolean canDragHorizontally(int directionX) {
        if (!mPreviewEnabled) {
            return false;
        }
        getDisplayRect(mTempRectF);
        if (directionX > 0) {
            return mTempRectF.left < 0;
        } else {
            return mTempRectF.right > getWidth();
        }
    }

    private void offsetDrawable(float dx, float dy) {
        getDisplayRect(mTempRectF);

        dx = Math.max(-mTempRectF.left, Math.min(getWidth()-mTempRectF.width(), dx));
        dy = Math.max(-mTempRectF.top, Math.min(getHeight()-mTempRectF.height(), dy));

        mSuppMatrix.postTranslate(dx, dy);
        updateImageMatrix();
    }

    // Handle Gestures
    private class SimpleGestureDetectorListener extends GestureDetector.SimpleOnGestureListener {

    }


//    // drag image
//
//    private boolean handleMoveAction(MotionEvent ev) {
//        final int activePointerId = mActivePointerId;
//        if (activePointerId == -1) {
//            return false;
//        }
//        final int activePointerIndex = ev.findPointerIndex(activePointerId);
//        if (activePointerIndex == -1) {
//            return false;
//        }
//
//        final int x = (int) ev.getX(activePointerIndex);
//        final int y = (int) ev.getY(activePointerIndex);
//
//        int dx = x - mLastMotionX;
//        int dy = y - mLastMotionY;
//
//        Log.d("TAG", "dx = " + dx + ", dy = " + dy + ", touchSlop = " + mTouchSlop);
//
//        if (!mIsBeingDragged && (Math.abs(dy) > mTouchSlop || Math.abs(dx) > mTouchSlop)) {
//            mIsBeingDragged = true;
//            if (Math.abs(dy) > mTouchSlop) {
//                if (dy > 0) {
//                    dy -= mTouchSlop;
//                } else {
//                    dy += mTouchSlop;
//                }
//            }
//
//            if (Math.abs(dx) > mTouchSlop) {
//                if (dx > 0) {
//                    dx -= mTouchSlop;
//                } else {
//                    dx += mTouchSlop;
//                }
//            }
//        }
//
//        boolean canDrag = false;
//
//        if (mIsBeingDragged) {
//            mLastMotionX = x;
//            mLastMotionY = y;
//
//            if (dx > 0 && !canDragHorizontally(1) || dx < 0 && !canDragHorizontally(-1)) {
//                dx = 0;
//            }
//
//            if (dy > 0 && !canDragVertically(1) || dy < 0 && !canDragVertically(-1)) {
//                dy = 0;
//            }
//
//            if (dx != 0 || dy != 0) {
//                canDrag = true;
//                offsetDrawable(dx, dy);
//            } else {
//                getParent().requestDisallowInterceptTouchEvent(false);
//            }
//        }
//
//        return mIsBeingDragged && canDrag;
//    }
//
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        boolean result = super.onTouchEvent(event);
//
//        if (!mPreviewEnabled) {
//            return result;
//        }
//
//        final int action = event.getActionMasked();
//        switch (action) {
//            case MotionEvent.ACTION_DOWN:
//                mLastMotionX = (int) event.getX();
//                mLastMotionY = (int) event.getY();
//                mActivePointerId = event.getPointerId(0);
//                break;
//            case MotionEvent.ACTION_MOVE:
//                getParent().requestDisallowInterceptTouchEvent(true);
//                boolean handled = handleMoveAction(event);
//                if (mIsBeingDragged) {
//                    setPressed(false);
//                }
//                if (handled) {
//                    return true;
//                }
//                break;
//            case MotionEvent.ACTION_UP:
//            case MotionEvent.ACTION_CANCEL:
//                mActivePointerId = -1;
//                mIsBeingDragged = false;
//                break;
//        }
//
//        Log.d("TAG", "result = " + result);
//        return result;
//    }
//
//    // scale image
//
//    private void onDoubleClicked() {
//        if (!mPreviewEnabled) {
//            return;
//        }
//
//        final float targetScale;
//        if (mCurrScale < mBaseScale) {
//            targetScale = mBaseScale;
//        } else if (mCurrScale < mLargerScale) {
//            targetScale = mLargerScale;
//        } else {
//            targetScale = mBaseScale;
//        }
//
//        mCurrScale = targetScale;
//        mCurrOffsetX = (getWidth() - mDrawableWidth*mCurrScale)/2;
//        mCurrOffsetY = (getHeight() - mDrawableHeight*mCurrScale)/2;
//
//        resetImageMatrix();
//    }
//
//    private class DoubleClickListener implements View.OnClickListener {
//        private final static int TIME_OUT = 500;
//        private Handler mHandler;
//        private boolean mClicked = false;
//
//        public DoubleClickListener() {
//            mHandler = new Handler(Looper.getMainLooper());
//        }
//
//        @Override
//        public void onClick(View v) {
//            Log.d("TAG", "onClick");
//            if (!mClicked) {
//                mClicked = true;
//                mHandler.postDelayed(mReset, TIME_OUT);
//                return;
//            }
//            mClicked = false;
//            mHandler.removeCallbacks(mReset);
//            onDoubleClicked();
//        }
//
//        private Runnable mReset = new Runnable() {
//            @Override
//            public void run() {
//                if (mClicked && mOnClickListener != null) {
//                    mOnClickListener.onClick(ImagePreviewer.this);
//                }
//                mClicked = false;
//            }
//        };
//    }


}
