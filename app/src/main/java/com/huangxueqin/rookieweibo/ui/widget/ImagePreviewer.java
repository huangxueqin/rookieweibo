package com.huangxueqin.rookieweibo.ui.widget;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
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

    private static final int ZOOM_DURATION = 200;

    private boolean mPreviewEnabled;

    private boolean mIsBeingDragged;

    private int mTouchSlop;
    private int mActivePointerId = -1;
    private int mLastMotionX;
    private int mLastMotionY;

    private int mDrawableWidth;
    private int mDrawableHeight;

    private float mBaseScale;
    private float mMaxScale;
    private float mMinScale;
    private float mBaseOffsetX;
    private float mBaseOffsetY;

    private Matrix mBaseMatrix = new Matrix();
    private Matrix mSuppMatrix = new Matrix();
    private Matrix mDrawMatrix = new Matrix();
    private Matrix mDisplayMatrix = new Matrix();

    private ScaleType savedScaleType;
    private OnClickListener mOnClickListener;

    // gesture detect
    private GestureDetector mGestureDetector;
    private OnTouchListener mOnTouchListener;

    private RectF mTempRectF = new RectF();
    private float[] mTempValues = new float[9];

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

        SimpleGestureDetectorListener gestureListener = new SimpleGestureDetectorListener();
        mGestureDetector = new GestureDetector(context, gestureListener);
        mGestureDetector.setOnDoubleTapListener(gestureListener);
        mOnTouchListener = new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mGestureDetector.onTouchEvent(event);
            }
        };
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

        if (allowPreview()) {
            mDrawableWidth = drawable.getIntrinsicWidth();
            mDrawableHeight = drawable.getIntrinsicHeight();
            enablePreview();
            if (getWidth() > 0 && getHeight() > 0) {
                initTranslation(getWidth(), getHeight());
            }
        } else {
            disablePreview();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (mPreviewEnabled) {
            initTranslation(w, h);
        }
    }

    private void updateImageMatrix() {
        mDrawMatrix.set(mBaseMatrix);
        mDrawMatrix.postConcat(mSuppMatrix);
        setImageMatrix(mDrawMatrix);
    }

    /**
     * update image matrix by size of drawable and this view
     * @param w
     * @param h
     */
    private void initTranslation(int w, int h) {
        final float drawableRatio = mDrawableWidth / (float)mDrawableHeight;
        final float viewRatio = w / (float)h;

        mBaseScale = w / (float)mDrawableWidth;
        mMinScale = Math.min(1, mBaseScale*0.6f);
        mMaxScale = drawableRatio >= viewRatio ? (h / (float)mDrawableHeight) : mBaseScale*1.4f;

        mBaseOffsetX = 0;
        if (drawableRatio >= viewRatio) {
            mBaseOffsetY = (h-mDrawableHeight*mBaseScale) / 2;
        } else {
            mBaseOffsetY = 0;
        }

        mBaseMatrix.reset();
        mBaseMatrix.postScale(mBaseScale, mBaseScale);
        mBaseMatrix.postTranslate(mBaseOffsetX, mBaseOffsetY);

        mSuppMatrix.reset();

        updateImageMatrix();
    }

    private boolean allowPreview() {
        Drawable d = getDrawable();
        return d != null && d.getIntrinsicWidth() > 0 && d.getIntrinsicHeight() > 0;
    }

    private void enablePreview() {
        Log.d("TAG", "enablePreview");
        mPreviewEnabled = true;
        setScaleType(ScaleType.MATRIX);
        setOnTouchListener(mOnTouchListener);
    }

    private void disablePreview() {
        Log.d("TAG", "disablePreview");
        mPreviewEnabled = false;
        setScaleType(savedScaleType);
        setOnTouchListener(null);
//        super.setOnClickListener(mOnClickListener);
    }

    private void getDisplayRect(RectF rectF) {
        final Drawable d = getDrawable();
        rectF.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        Matrix matrix = getImageMatrix();
        matrix.mapRect(rectF);
    }

    private void setImageTranslation(float scale, float tx, float ty) {
        final float suppScale = scale / mBaseScale;
        mSuppMatrix.setScale(suppScale, suppScale);
        mSuppMatrix.postTranslate(tx-mBaseOffsetX*suppScale, ty-mBaseOffsetY*suppScale);
        updateImageMatrix();
    }

    private void zoomImage(float targetScale, boolean animated) {
        zoomImage(targetScale, -1, -1, ZOOM_DURATION, animated);
    }

    private void zoomImage(float targetScale, float anchorX, float anchorY, boolean animated) {
        zoomImage(targetScale, anchorX, anchorY, ZOOM_DURATION, animated);
    }

    /**
     * zoom the image drawable
     * @param targetScale target scale
     * @param anchorX zoom anchorX in view
     * @param anchorY zoom anchorY in view
     * @param duration zoom duration
     * @param animated
     */
    private void zoomImage(float targetScale, float anchorX, float anchorY, int duration, boolean animated) {
        RectF rect = mTempRectF;
        getDisplayRect(rect);

        final int zoomedW = Math.round(mDrawableWidth*targetScale);
        final int zoomedH = Math.round(mDrawableHeight*targetScale);

        // compute tx and ty
        float targetTx = (getWidth()-zoomedW)/2;
        if (anchorX >= 0) {
            final float tAnchorX = anchorX - rect.left;
            targetTx = Math.min(0, anchorX - tAnchorX*targetScale);
            if (zoomedW <= getWidth()) {
                targetTx = (getWidth()-zoomedW)/2;
            } else {
                targetTx += Math.max(0, getWidth()-zoomedW+tAnchorX);
            }
        }

        float targetTy = (getHeight()-zoomedH)/2;
        if (anchorY >= 0) {
            final float tAnchorY = anchorY - rect.top;
            targetTy = Math.min(0, anchorY - tAnchorY * targetScale);
            if (zoomedH <= getHeight()) {
                targetTy = (getHeight() - zoomedH) / 2;
            } else {
                targetTy += Math.max(0, getHeight() - zoomedH + tAnchorY);
            }
        }

        // start translation
        if (animated) {
            post(new ZoomRunnable(targetScale, targetTx, targetTy, duration));
        } else {
            setImageTranslation(targetScale, targetTx, targetTy);
        }
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

        final float dxMin = Math.min(0, getWidth()-mTempRectF.right);
        final float dxMax = Math.max(0, -mTempRectF.left);
        dx = Math.max(dxMin, Math.min(dxMax, dx));

        final float dyMin = Math.min(0, getHeight()-mTempRectF.bottom);
        final float dyMax = Math.max(0, -mTempRectF.top);
        dy = Math.max(dyMin, Math.min(dyMax, dy));

        mSuppMatrix.postTranslate(dx, dy);
        updateImageMatrix();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d("TAG", "onTouchEvent " + event);
        boolean result = super.onTouchEvent(event);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                result = true;
                break;
        }

        return result;
    }

    // Handle Gestures
    private class SimpleGestureDetectorListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            offsetDrawable(-distanceX, -distanceY);
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            final float tapX = e.getX();
            final float tapY = e.getY();

            getImageMatrix().getValues(mTempValues);
            final float currScale = mTempValues[Matrix.MSCALE_X];
            float targetScale = mBaseScale;
            if (currScale >= mBaseScale && currScale < mMaxScale) {
                targetScale = mMaxScale;
            }
            Log.d("TAG", "mCurrScale = " + currScale + ", targetScale = " + targetScale);
            zoomImage(targetScale, tapX, tapY, true);
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (mOnClickListener != null) {
                mOnClickListener.onClick(ImagePreviewer.this);
                return true;
            }
            return super.onSingleTapConfirmed(e);
        }
    }

    private class ZoomRunnable implements Runnable {
        private final float fromScale, toScale;
        private final float fromOffsetX, toOffsetX;
        private final float fromOffsetY, toOffsetY;
        private final int duration;
        private final long startTime;

        public ZoomRunnable(float toScale, float toOffsetX, float toOffsetY, int duration) {
            getImageMatrix().getValues(mTempValues);
            this.fromScale = mTempValues[Matrix.MSCALE_X];
            this.fromOffsetX = mTempValues[Matrix.MTRANS_X];
            this.fromOffsetY = mTempValues[Matrix.MTRANS_Y];
            this.toScale = toScale;
            this.toOffsetX = toOffsetX;
            this.toOffsetY = toOffsetY;
            this.duration = duration;
            this.startTime = android.os.SystemClock.uptimeMillis();
        }

        @Override
        public void run() {
            final float t = Math.min(1f, 1f * (android.os.SystemClock.uptimeMillis()-startTime)/duration);
            final float currScale = fromScale + t * (toScale - fromScale);
            final float currOffsetX = fromOffsetX + t * (toOffsetX - fromOffsetX);
            final float currOffsetY = fromOffsetY + t * (toOffsetY - fromOffsetY);

            setImageTranslation(currScale, currOffsetX, currOffsetY);

            if (t < 1f) {
                postOnAnimation(this);
            }
        }
    }

//    // drag image
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
}
