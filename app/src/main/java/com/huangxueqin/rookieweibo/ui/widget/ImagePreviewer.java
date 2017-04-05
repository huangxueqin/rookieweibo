package com.huangxueqin.rookieweibo.ui.widget;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;

import com.huangxueqin.rookieweibo.common.utils.L;

/**
 * Created by huangxueqin on 2017/3/26.
 */

public class ImagePreviewer extends android.support.v7.widget.AppCompatImageView {

    private static final int ZOOM_DURATION = 200;

    private boolean mPreviewEnabled;

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

    private ScaleType savedScaleType;
    private OnClickListener mOnClickListener;

    // gesture detect
    private GestureHandler mGestureHandler;

    private SnapDelegate mSnapDelegate;

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
        setClickable(true);
        savedScaleType = getScaleType();

        mGestureHandler = new GestureHandler(context);
    }

    public void setSnapDelegate(SnapDelegate delegate) {
        mSnapDelegate = delegate;
    }

    private int getMaxAllowedBitmapSize() {
        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        return Math.max(metrics.widthPixels, metrics.heightPixels);
    }

    private boolean allowPreview() {
        Drawable d = getDrawable();
        return d != null && d.getIntrinsicWidth() > 0 && d.getIntrinsicHeight() > 0;
    }

    private void enablePreview() {
        mPreviewEnabled = true;
        setScaleType(ScaleType.MATRIX);
        setOnTouchListener(mGestureHandler);
        super.setOnClickListener(null);
    }

    private void disablePreview() {
        mPreviewEnabled = false;
        setScaleType(savedScaleType);
        setOnTouchListener(null);
        if (mOnClickListener != null) {
            super.setOnClickListener(mOnClickListener);
        }
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
            if (!mPreviewEnabled) {
                enablePreview();
            }
            if (getWidth() > 0 && getHeight() > 0) {
                initTranslation(getWidth(), getHeight());
            }
        } else if (mPreviewEnabled) {
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

    private void setImageTranslation(float scale, float tx, float ty) {
        final float suppScale = scale / mBaseScale;
        mSuppMatrix.setScale(suppScale, suppScale);
        mSuppMatrix.postTranslate(tx - mBaseOffsetX * suppScale, ty - mBaseOffsetY * suppScale);
        updateImageMatrix();
    }

    public float[] getImageTranslateValues() {
        getImageMatrix().getValues(mTempValues);
        return mTempValues;
    }

    /**
     * update image matrix by size of drawable and this view
     *
     * @param w
     * @param h
     */
    private void initTranslation(int w, int h) {
        final float drawableRatio = mDrawableWidth / (float) mDrawableHeight;
        final float viewRatio = w / (float) h;

        mBaseScale = w / (float) mDrawableWidth;
        mMinScale = Math.min(1, mBaseScale * 0.6f);
        mMaxScale = drawableRatio >= viewRatio ? (h / (float) mDrawableHeight) : mBaseScale * 1.4f;

        mBaseOffsetX = 0;
        if (drawableRatio >= viewRatio) {
            mBaseOffsetY = (h - mDrawableHeight * mBaseScale) / 2;
        } else {
            mBaseOffsetY = 0;
        }

        mBaseMatrix.reset();
        mBaseMatrix.postScale(mBaseScale, mBaseScale);
        mBaseMatrix.postTranslate(mBaseOffsetX, mBaseOffsetY);

        mSuppMatrix.reset();

        updateImageMatrix();
    }

    private void getDisplayRect(RectF rectF) {
        final Drawable d = getDrawable();
        rectF.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        Matrix matrix = getImageMatrix();
        matrix.mapRect(rectF);
    }

    private void getRawDisplayRect(RectF rectF) {
        final int width = mSnapDelegate == null ? getDrawable().getIntrinsicWidth() : mSnapDelegate.getRawWidth(this);
        final int height = mSnapDelegate == null ? getDrawable().getIntrinsicHeight() : mSnapDelegate.getRawHeight(this);
        rectF.set(0, 0, width, height);
        getImageMatrix().mapRect(rectF);
    }

    private void zoomImage(float targetScale, boolean animated) {
        zoomImage(targetScale, -1, -1, ZOOM_DURATION, animated);
    }

    private void zoomImage(float targetScale, float anchorX, float anchorY, boolean animated) {
        zoomImage(targetScale, anchorX, anchorY, ZOOM_DURATION, animated);
    }

    /**
     * zoom the image drawable
     *
     * @param targetScale target scale
     * @param anchorX     zoom anchorX in view
     * @param anchorY     zoom anchorY in view
     * @param duration    zoom duration
     * @param animated
     */
    private void zoomImage(float targetScale, float anchorX, float anchorY, int duration, boolean animated) {
        RectF rect = mTempRectF;
        getDisplayRect(rect);

        final float currScale = getImageTranslateValues()[Matrix.MSCALE_X];

        final int zoomedW = Math.round(mDrawableWidth * targetScale);
        final int zoomedH = Math.round(mDrawableHeight * targetScale);
        L.d("TAG", "drawableWidth = " + mDrawableWidth + ", drawableHeight = " + mDrawableHeight);
        L.d("TAG", "zoomedW = " + zoomedW + ", zoomedH = " + zoomedH);
        // compute tx and ty
        float targetTx = (getWidth() - zoomedW) / 2;
        if (anchorX >= 0 && zoomedW > getWidth()) {
            final float tAnchorX = anchorX - rect.left;
            targetTx = Math.max(getWidth()-zoomedW, Math.min(0, anchorX - tAnchorX * targetScale/currScale));
            L.d("TAG", "anchorX = " + anchorX + ", scale = " + targetScale + ", tAnchorX = " + tAnchorX);
        }

        float targetTy = (getHeight() - zoomedH) / 2;
        if (anchorY >= 0 && zoomedH > getHeight()) {
            final float tAnchorY = anchorY - rect.top;
            targetTy = Math.max(getHeight()-zoomedH, Math.min(0, anchorY - tAnchorY * targetScale/currScale));
            L.d("TAG", "anchorY = " + anchorY + ", scale = " + targetScale + ", tAnchorY = " + tAnchorY);
        }

        L.d("TAG", "targetTx = " + targetTx + ", targetTy = " + targetTy);
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

        getRawDisplayRect(mTempRectF);
        if (directionY > 0) {
            return Math.round(mTempRectF.top) < 0;
        } else {
            return Math.round(mTempRectF.bottom) > getHeight();
        }
    }

    private boolean canDragHorizontally() {
        if (!mPreviewEnabled) {
            return false;
        }
        getRawDisplayRect(mTempRectF);
        return Math.round(mTempRectF.width()) > getWidth();
    }

    private boolean canDragHorizontally(int directionX) {
        if (!mPreviewEnabled) {
            return false;
        }
        getRawDisplayRect(mTempRectF);
        if (directionX > 0) {
            return Math.round(mTempRectF.left) < 0;
        } else {
            return Math.round(mTempRectF.right) > getWidth();
        }
    }

    private boolean canDragVertically() {
        if (!mPreviewEnabled) {
            return false;
        }
        getRawDisplayRect(mTempRectF);
        return Math.round(mTempRectF.height()) > getHeight();
    }

    private boolean canDrag() {
        if (!mPreviewEnabled) {
            return false;
        }
        getRawDisplayRect(mTempRectF);
        return Math.round(mTempRectF.height()) > getHeight() ||
                Math.round(mTempRectF.width()) > getWidth();
    }

    private void transitDrawable(float dx, float dy, float[] consumed) {
        getDisplayRect(mTempRectF);

        final float dxMin = Math.min(0, getWidth() - mTempRectF.right);
        final float dxMax = Math.max(0, -mTempRectF.left);
        final float tx = Math.max(dxMin, Math.min(dxMax, -dx));

        final float dyMin = Math.min(0, getHeight() - mTempRectF.bottom);
        final float dyMax = Math.max(0, -mTempRectF.top);
        final float ty = Math.max(dyMin, Math.min(dyMax, -dy));

        consumed[0] = -tx;
        consumed[1] = -ty;
        mSuppMatrix.postTranslate(tx, ty);
        updateImageMatrix();
    }

    // Handle Gestures
    private class GestureHandler extends GestureDetector.SimpleOnGestureListener implements OnTouchListener {

        private boolean mIsBeingDraggedX;
        private boolean mIsBeingDraggedY;

        private int mTouchSlop;
        private int mActivePointerId = -1;
        private int mLastMotionX;
        private int mLastMotionY;

        private GestureDetector mGestureDetector;

        public GestureHandler(Context context) {
            mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
            mGestureDetector = new GestureDetector(context, this);
            mGestureDetector.setOnDoubleTapListener(this);
        }

        private float decideTargetScale() {
            final Matrix mat = getImageMatrix();
            mat.getValues(mTempValues);

            float currScale = mTempValues[Matrix.MSCALE_X];
            if (Math.abs(currScale-mBaseScale) < 0.01) {
                currScale = mBaseScale;
            }
            if (Math.abs(currScale-mMaxScale) < 0.01) {
                currScale = mMaxScale;
            }

            if (currScale < mBaseScale) {
                return mBaseScale;
            } else if (currScale < mMaxScale) {
                return mMaxScale;
            } else {
                return mBaseScale;
            }
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            float[] consumed = {0, 0};
            transitDrawable(distanceX, distanceY, consumed);
            if (mSnapDelegate != null) {
                float unConsumedX = distanceX - consumed[0];
                float unConsumedY = distanceY - consumed[1];
                L.d("TAG", "unConsumedX = " + unConsumedX + ", unConsumedY = " + unConsumedY);
                float currScale = getImageTranslateValues()[Matrix.MSCALE_X];
                mSnapDelegate.offsetRegion(ImagePreviewer.this, unConsumedX/currScale, unConsumedY/currScale);
            }
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            final float tapX = e.getX();
            final float tapY = e.getY();
            zoomImage(decideTargetScale(), tapX, tapY, true);
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

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            final int action = event.getActionMasked();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mActivePointerId = event.getPointerId(0);
                    mLastMotionX = (int) event.getX();
                    mLastMotionY = (int) event.getY();
                    if (v.getParent() != null && canDrag()) {
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    final int pointerId = mActivePointerId;
                    if (pointerId == -1) {
                        break;
                    }

                    final int pointerIndex = event.findPointerIndex(pointerId);
                    if (pointerIndex == -1) {
                        break;
                    }

                    final int x = (int) event.getX(pointerIndex);
                    final int y = (int) event.getY(pointerIndex);
                    final int xDiff = x - mLastMotionX;
                    final int yDiff = y - mLastMotionY;

                    if (detectNestedScroll(-xDiff)) {
                        if (mIsBeingDraggedX && !canDragHorizontally(xDiff)) {
                            if (!canDragVertically(yDiff) || !mIsBeingDraggedY) {
                                v.getParent().requestDisallowInterceptTouchEvent(false);
                            }
                        }
                    }

                    if (Math.abs(xDiff) > mTouchSlop && !mIsBeingDraggedX) {
                        mIsBeingDraggedX = true;
                    }
                    if (Math.abs(yDiff) > mTouchSlop && !mIsBeingDraggedY) {
                        mIsBeingDraggedY = true;
                    }

                    if (mIsBeingDraggedX) {
                        mLastMotionX = x;
                    }
                    if (mIsBeingDraggedY) {
                        mLastMotionY = y;
                    }

                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    mIsBeingDraggedX = false;
                    mIsBeingDraggedY = false;
                    mActivePointerId = -1;
                    break;
            }

            return mGestureDetector.onTouchEvent(event);
        }
    }

    private boolean detectNestedScroll(int direction) {
        ViewParent parent = getParent();
        while (parent != null && parent instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) parent;
            if (ViewCompat.canScrollHorizontally(vg, direction)) {
                return true;
            }
            parent = vg.getParent();
        }
        return false;
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
            final float t = Math.min(1f, 1f * (android.os.SystemClock.uptimeMillis() - startTime) / duration);
            final float currScale = fromScale + t * (toScale - fromScale);
            final float currOffsetX = fromOffsetX + t * (toOffsetX - fromOffsetX);
            final float currOffsetY = fromOffsetY + t * (toOffsetY - fromOffsetY);

            setImageTranslation(currScale, currOffsetX, currOffsetY);

            if (t < 1f) {
                postOnAnimation(this);
            }
        }
    }

    public interface SnapDelegate {
        int getRawHeight(ImageView imageView);
        int getRawWidth(ImageView imageView);
        void offsetRegion(ImageView imageView, float dx, float dy);
    }
}
