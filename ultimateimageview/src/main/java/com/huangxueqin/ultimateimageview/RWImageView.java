package com.huangxueqin.ultimateimageview;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Scroller;

import com.huangxueqin.ultimateimageview.utils.MathUtils;
import com.huangxueqin.ultimateimageview.utils.RectPool;

import java.io.File;

/**
 * Created by huangxueqin on 2017/6/7.
 * currently can only support nested in horizontal view pager
 */

public class RWImageView extends android.support.v7.widget.AppCompatImageView {

    private static final String TAG = "RwImageView";

    private static final int INVALID_POINTER_ID = -1;

    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleGestureDetector;
    private Matrix mTempMatrix = new Matrix();
    private float[] mTempValues = new float[9];

    // touch events
    private int mActivePointerId = INVALID_POINTER_ID;
    private int mLastMotionX;
    private int mLastMotionY;
    private boolean mIsBeingDraggedX;
    private boolean mIsBeingDraggedY;
    private boolean mIsScaling;
    private int mTouchSlop;

    private Scroller mScroller;
    private FlingRunnable mFlingRunnable;


    public RWImageView(Context context) {
        this(context, null);
    }

    public RWImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RWImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mGestureDetector = new GestureDetector(context, mGestureListener);
        mScaleGestureDetector = new ScaleGestureDetector(context, mScaleGestureListener);
        setOnTouchListener(mOnTouchListener);
        setScaleType(ScaleType.MATRIX);
        setClickable(true);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mScroller = new Scroller(context);
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        super.setImageDrawable(drawable);
        if (drawable == null) {
            return;
        }
        mDrawableWidth = drawable.getIntrinsicWidth();
        mDrawableHeight = drawable.getIntrinsicHeight();
        if (sizeReady() && mDrawableWidth > 0 && mDrawableHeight > 0) {
            initImageMatrix();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mDrawableWidth > 0 && mDrawableHeight > 0) {
            initImageMatrix();
        }
    }

    private boolean sizeReady() {
        return getMeasuredWidth() > 0 && getMeasuredHeight() > 0;
    }

    public void setLargeImage(File imageFile, int width, int height) {
        LargeImageDrawable d = new LargeImageDrawable(getContext(), imageFile, width, height);
        setImageDrawable(d);
    }

    private void getDrawableDisplayBounds(RectF rectf) {
        rectf.set(0, 0, mDrawableWidth, mDrawableHeight);
        Matrix matrix = getImageMatrix();
        matrix.mapRect(rectf);
    }

    // visible bounds that need be drawing in View
    private void getDrawableDrawBounds(Rect rect) {
        final RectF drawableRect = mRectPool.acquireRectF(0, 0, mDrawableWidth, mDrawableHeight);
        final RectF viewRect = mRectPool.acquireRectF(0, 0, getMeasuredWidth(), getMeasuredHeight());

        // map view rect to drawable coordinates
        getImageMatrix().invert(mTempMatrix);
        mTempMatrix.mapRect(viewRect);

        drawableRect.intersect(viewRect);
        MathUtils.roundTo(drawableRect, rect);

        mRectPool.release(drawableRect);
        mRectPool.release(viewRect);
    }

    // Those five method below determine if the drawable can be drag
    private RectF mTempRectF = new RectF();

    private boolean canDragVertically (int directionY) {
        getDrawableDisplayBounds(mTempRectF);
        if (directionY > 0) {
            return MathUtils.roundTo(mTempRectF.top) < 0;
        } else {
            return MathUtils.roundTo(mTempRectF.bottom) > getMeasuredHeight();
        }
    }

    private boolean canDragHorizontally(int directionX) {
        getDrawableDisplayBounds(mTempRectF);
        if (directionX > 0) {
            return MathUtils.roundTo(mTempRectF.left) < 0;
        } else {
            return MathUtils.roundTo(mTempRectF.right) > getMeasuredWidth();
        }
    }

    private boolean canDragVertically() {
        getDrawableDisplayBounds(mTempRectF);
        return mTempRectF.height() > getMeasuredHeight();
    }

    private boolean canDragHorizontally() {
        getDrawableDisplayBounds(mTempRectF);
        return mTempRectF.width() > getMeasuredWidth();
    }

    private boolean canDrag() {
        getDrawableDisplayBounds(mTempRectF);
        return mTempRectF.width() > getMeasuredWidth() || mTempRectF.height() > getMeasuredHeight();
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

    private void handleNestedScroll(View v, MotionEvent event) {
        final int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = event.getPointerId(0);
                mLastMotionX = (int) event.getX();
                mLastMotionY = (int) event.getY();
                if (canDrag()) {
                    if (v.getParent() != null) {
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                    }
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (v.getParent() != null) {
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
                if (!mIsScaling && detectNestedScroll(-xDiff)) {
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
            case MotionEvent.ACTION_POINTER_UP:

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsBeingDraggedX = false;
                mIsBeingDraggedY = false;
                mActivePointerId = -1;
                break;
        }
    }

    private OnTouchListener mOnTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            Log.d(TAG, "onTouchListener: event = " + event);

            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                if (mFlingRunnable != null) {
                    mScroller.abortAnimation();
                    removeCallbacks(mFlingRunnable);
                    mFlingRunnable = null;
                }
            }

            mScaleGestureDetector.onTouchEvent(event);

            if (mIsScaling) {
                return true;
            }

            handleNestedScroll(v, event);
            return mGestureDetector.onTouchEvent(event);
        }
    };

    private GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            Log.d(TAG, "onScroll distanceX = " + distanceX + ", distanceY = " + distanceY);
            if (!mIsScaling) {
                offsetDrawable(-distanceX, -distanceY);
            }
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.d(TAG, "onDoubleTapped");
            final float tapX = e.getX();
            final float tapY = e.getY();
            final float currentScale = getCurrentScale();
            final float targetScale = determineTargetScale(currentScale);
            zoomDrawable(targetScale, tapX, tapY, ZOOM_DURATION, true);
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Log.d(TAG, "onSingleTapUp");
            return super.onSingleTapUp(e);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.d(TAG, "onFling running");
            Log.d(TAG, "onFling, velocityX = " + velocityX + ", velocityY = " + velocityY);

            if (!canDragVertically((int) velocityY) && !canDragHorizontally((int) velocityX)) {
                return false;
            }

            if (mFlingRunnable != null) {
                removeCallbacks(mFlingRunnable);
                mFlingRunnable = null;
            }
            mScroller.fling(0, 0, (int) velocityX, (int) velocityY, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
            if (mScroller.computeScrollOffset()) {
                mFlingRunnable = new FlingRunnable();
                postOnAnimation(mFlingRunnable);
            }
            return true;
        }
    };

    private ScaleGestureDetector.OnScaleGestureListener mScaleGestureListener = new ScaleGestureDetector.OnScaleGestureListener() {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            Log.d(TAG, "onScale");
            if (!mIsScaling) {
                return false;
            }
            final float scale = getCurrentScale() * mScaleGestureDetector.getScaleFactor();
            Log.d(TAG, "target scale = " + scale);
            zoomDrawable(scale, mScaleGestureDetector.getFocusX(), mScaleGestureDetector.getFocusY(), ZOOM_DURATION, false);
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            Log.d(TAG, "onScaleBegin");
            mIsScaling = true;
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            Log.d(TAG, "onScaleEnd");
            mIsScaling = false;
        }
    };


    // Handle drawable Transform
    private static final int ZOOM_DURATION = 200;

    private int mDrawableWidth;
    private int mDrawableHeight;

    private float mBaseScale;
    private float mBaseTransX;
    private float mBaseTransY;
    private float mMaxScale;

    private Matrix mBaseMatrix = new Matrix();
    private Matrix mSuppMatrix = new Matrix();

    private RectPool mRectPool = new RectPool(5);

    private void initImageMatrix() {
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();
        mBaseScale = width / (float) mDrawableWidth;
        mBaseTransX = 0;
        if (mDrawableWidth/(float)width > mDrawableHeight/(float)height) {
            mBaseTransY = (height-mDrawableHeight*mBaseScale)/2;
            mMaxScale = height / (float) mDrawableHeight;
        } else {
            mBaseTransY = 0;
            mMaxScale = 1.5f * mBaseScale;
        }

        mBaseMatrix.setScale(mBaseScale, mBaseScale);
        mBaseMatrix.postTranslate(mBaseTransX, mBaseTransY);
        mSuppMatrix.reset();

        updateDrawableMatrix();
    }

    private void updateDrawableMatrix() {
        mTempMatrix.set(mBaseMatrix);
        mTempMatrix.postConcat(mSuppMatrix);
        setImageMatrix(mTempMatrix);

        Drawable d = getDrawable();
        if (d instanceof LargeImageDrawable) {
            LargeImageDrawable ld = (LargeImageDrawable) d;
            Rect drawBounds = mRectPool.acquireRect();
            getDrawableDrawBounds(drawBounds);
            ld.setDrawBounds(drawBounds);
            mRectPool.release(drawBounds);

            Matrix mat = getImageMatrix();
            mat.getValues(mTempValues);
            ld.setSample(1/mTempValues[Matrix.MSCALE_X]);
        }
        invalidate();
    }

    private void setDrawableTranslation(float scale, float tx, float ty) {
        final float suppScale = scale / mBaseScale;
        mSuppMatrix.setScale(suppScale, suppScale);
        mSuppMatrix.postTranslate(tx - mBaseTransX * suppScale, ty - mBaseTransY * suppScale);
        updateDrawableMatrix();
    }

    // Translate
    private void offsetDrawable(float tx, float ty) {
        RectF displayRect = mRectPool.acquireRectF();
        getDrawableDisplayBounds(displayRect);

        float txMin = Math.min(0, getMeasuredWidth()-displayRect.right);
        float txMax = Math.max(0, -displayRect.left);
        float targetTx = Math.max(txMin, Math.min(tx, txMax));

        float tyMin = Math.min(0, getMeasuredHeight()-displayRect.bottom);
        float tyMax = Math.max(0, -displayRect.top);
        float targetTy = Math.max(tyMin, Math.min(ty, tyMax));

        mSuppMatrix.postTranslate(targetTx, targetTy);
        updateDrawableMatrix();

        mRectPool.release(displayRect);
    }

    // ZOOM
    private float getCurrentScale() {
        Matrix matrix = getImageMatrix();
        matrix.getValues(mTempValues);
        return mTempValues[Matrix.MSCALE_X];
    }

    private float determineTargetScale(float currentScale) {
        if (Math.abs(currentScale-mBaseScale) < 0.01) {
            currentScale = mBaseScale;
        } else if (Math.abs(currentScale-mMaxScale) < 0.01) {
            currentScale = mMaxScale;
        }
        if (currentScale < mBaseScale || currentScale >= mMaxScale) {
            return mBaseScale;
        } else {
            return mMaxScale;
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
            this.startTime = SystemClock.uptimeMillis();
        }

        @Override
        public void run() {
            final float t = Math.min(1f, 1f * (SystemClock.uptimeMillis() - startTime) / duration);
            final float currScale = fromScale + t * (toScale - fromScale);
            final float currOffsetX = fromOffsetX + t * (toOffsetX - fromOffsetX);
            final float currOffsetY = fromOffsetY + t * (toOffsetY - fromOffsetY);

            setDrawableTranslation(currScale, currOffsetX, currOffsetY);

            if (t < 1f) {
                postOnAnimation(this);
            }
        }
    }

    private void zoomDrawable(float toScale, float anchorX, float anchorY, int duration, boolean animated) {
        getDrawableDisplayBounds(mTempRectF);
        final float curScale = getCurrentScale();
        final float toDw = mDrawableWidth * toScale;
        final float toDh = mDrawableHeight * toScale;

        float targetTx = (getMeasuredWidth() - toDw) / 2;
        if (toDw > getMeasuredWidth() && anchorX >= 0) {
            float delta = anchorX - mTempRectF.left;
            targetTx = Math.max(getMeasuredWidth()-toDw, Math.min(0, anchorX-delta*toScale/curScale));
        }

        float targetTy = (getMeasuredHeight() - toDh) / 2;
        if (toDh > getMeasuredHeight() && anchorY >= 0) {
            float delta = anchorY - mTempRectF.top;
            targetTy = Math.max(getMeasuredHeight()-toDh, Math.min(0, anchorY - delta*toScale/curScale));
        }

        if (animated) {
            post(new ZoomRunnable(toScale, targetTx, targetTy, duration));
        } else {
            setDrawableTranslation(toScale, targetTx, targetTy);
        }
    }

    // Fling
    private class FlingRunnable implements Runnable {
        private int mLastX = 0;
        private int mLastY = 0;

        @Override
        public void run() {
            Log.d(TAG, "start computer mScroller");
            if (mScroller.computeScrollOffset()) {
                Log.d(TAG, "fling, currY = " + mScroller.getCurrY());
                final int currY = mScroller.getCurrY();
                final int dy = currY-mLastY;
                final int currX = mScroller.getCurrX();
                final int dx = currX-mLastX;

                offsetDrawable(dx, dy);
                postOnAnimation(this);

                mLastY = currY;
                mLastX = currX;
            }
        }
    }
}
