package com.huangxueqin.ultimateimageview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.util.Pools;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;

import java.io.File;
import java.util.List;

/**
 * Created by huangxueqin on 2017/4/6.
 */

public class UltimateImageView extends View implements LoadListener {
    private static final int ZOOM_DURATION = 200;

    private ImageLoader mImageLoader;

    private int mDrawableWidth;
    private int mDrawableHeight;

    private float mBaseScale;
    private float mBaseTranX;
    private float mBaseTranY;
    private float mMaxScale;

    private Matrix mBaseMatrix = new Matrix();
    private Matrix mSuppMatrix = new Matrix();
    private Matrix mImageMatrix = new Matrix();

    private RectCache mRectCache = new RectCache();
    private float[] mMatrixValues = new float[9];
    private Matrix mTempMatrix = new Matrix();

    // gesture detect
    private GestureHandler mGestureHandler;

    public UltimateImageView(Context context) {
        this(context, null);
    }

    public UltimateImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UltimateImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setClickable(true);

        mImageLoader = new ImageLoader(context);
        mImageLoader.setLoadListener(this);

        mGestureHandler = new GestureHandler(context);
        setOnTouchListener(mGestureHandler);
    }

    public void setImage(File imageFile) {
        mImageLoader.setImage(imageFile.getAbsolutePath());
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // init image matrix...
        if (drawableSizeReady() && viewSizeReady()) {
            final int width = getMeasuredWidth();
            final int height = getMeasuredHeight();
            mBaseScale = width / (float) mDrawableWidth;
            mBaseTranX = 0;
            if (mDrawableWidth/(float)width > mDrawableHeight/(float)height) {
                mBaseTranY = (height-mDrawableHeight*mBaseScale)/2;
                mMaxScale = height / (float) mDrawableHeight;
            } else {
                mBaseTranY = 0;
                mMaxScale = 1.5f * mBaseScale;
            }

            mBaseMatrix.setScale(mBaseScale, mBaseScale);
            mBaseMatrix.postTranslate(mBaseTranX, mBaseTranY);
            mSuppMatrix.reset();

            updateImageMatrix();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!viewSizeReady()) {
            return;
        }

        Log.d("TAG", "onDraw: " + SystemClock.uptimeMillis());

        final Rect drawArea = mRectCache.obtainRect();
        getImageDrawRect(drawArea);
        mImageMatrix.getValues(mMatrixValues);
        List<DrawBlock> drawDatas = mImageLoader.getDrawData(1/mMatrixValues[Matrix.MSCALE_X], drawArea);
        final RectF dst = mRectCache.obtainRectF();
        for (DrawBlock data : drawDatas) {
            dst.set(data.imgRect);
            mImageMatrix.mapRect(dst);
            canvas.drawBitmap(data.bitmap, data.srcRect, dst, null);
        }
        mRectCache.recycle(drawArea);
        mRectCache.recycle(dst);
    }

    private void updateImageMatrix() {
        mTempMatrix.set(mBaseMatrix);
        mTempMatrix.postConcat(mSuppMatrix);
        if (!mImageMatrix.equals(mTempMatrix)) {
            mImageMatrix.set(mTempMatrix);
            invalidate();
        }
    }

    private void setImageTranslation(float scale, float tx, float ty) {
        final float suppScale = scale / mBaseScale;
        mSuppMatrix.setScale(suppScale, suppScale);
        mSuppMatrix.postTranslate(tx - mBaseTranX * suppScale, ty - mBaseTranY * suppScale);
        updateImageMatrix();
    }

    private void transitDrawable(float dx, float dy) {
        final RectF dispRectF = mRectCache.obtainRectF();
        getDisplayRect(dispRectF);

        final float dxMin = Math.min(0, getWidth() - dispRectF.right);
        final float dxMax = Math.max(0, -dispRectF.left);
        final float tx = Math.max(dxMin, Math.min(dxMax, -dx));

        final float dyMin = Math.min(0, getHeight() - dispRectF.bottom);
        final float dyMax = Math.max(0, -dispRectF.top);
        final float ty = Math.max(dyMin, Math.min(dyMax, -dy));

        mSuppMatrix.postTranslate(tx, ty);
        updateImageMatrix();

        mRectCache.recycle(dispRectF);
    }

    private boolean canDragVertically(int directionY) {
        final RectF rectF = mRectCache.obtainRectF();
        getDisplayRect(rectF);
        try {
            if (directionY > 0) {
                return Math.round(rectF.top) < 0;
            } else {
                return Math.round(rectF.bottom) > getHeight();
            }
        } finally {
            mRectCache.recycle(rectF);
        }
    }

    private boolean canDragHorizontally() {
        final RectF rectF = mRectCache.obtainRectF();
        getDisplayRect(rectF);
        try {
            return Math.round(rectF.width()) > getWidth();
        } finally {
            mRectCache.recycle(rectF);
        }
    }

    private boolean canDragHorizontally(int directionX) {
        final RectF rectF = mRectCache.obtainRectF();
        getDisplayRect(rectF);
        try {
            if (directionX > 0) {
                return Math.round(rectF.left) < 0;
            } else {
                return Math.round(rectF.right) > getWidth();
            }
        } finally {
            mRectCache.recycle(rectF);
        }
    }

    private boolean canDragVertically() {
        final RectF rectF = mRectCache.obtainRectF();
        getDisplayRect(rectF);
        try {
            return Math.round(rectF.height()) > getHeight();
        } finally {
            mRectCache.recycle(rectF);
        }
    }

    private boolean canDrag() {
        final RectF rectF = mRectCache.obtainRectF();
        getDisplayRect(rectF);
        try {
            return Math.round(rectF.height()) > getHeight() ||
                    Math.round(rectF.width()) > getWidth();
        } finally {
            mRectCache.recycle(rectF);
        }
    }

    private boolean viewSizeReady() {
        return getMeasuredWidth() > 0 && getMeasuredHeight() > 0;
    }

    private boolean drawableSizeReady() {
        return mDrawableWidth > 0 && mDrawableHeight > 0;
    }

    private void getDisplayRect(RectF rectF) {
        rectF.set(0, 0, mDrawableWidth, mDrawableHeight);
        mImageMatrix.mapRect(rectF);
    }

    private void getImageDrawRect(Rect rect) {
        final RectF imageRectF = mRectCache.obtainRectF(0, 0, mDrawableWidth, mDrawableHeight);
        final RectF viewRectF = mRectCache.obtainRectF(0, 0, getWidth(), getHeight());
        final Matrix invertedMatrix = mTempMatrix;
        mImageMatrix.invert(invertedMatrix);
        invertedMatrix.mapRect(viewRectF);

        imageRectF.intersect(viewRectF);

        rect.set(Math.round(imageRectF.left),
                Math.round(imageRectF.top),
                Math.round(imageRectF.right),
                Math.round(imageRectF.bottom));

        // release temp rectFs
        mRectCache.recycle(viewRectF);
        mRectCache.recycle(imageRectF);
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
        RectF rect = mRectCache.obtainRectF();
        getDisplayRect(rect);

        mImageMatrix.getValues(mMatrixValues);
        final float currScale = mMatrixValues[Matrix.MSCALE_X];

        final int zoomedW = Math.round(mDrawableWidth * targetScale);
        final int zoomedH = Math.round(mDrawableHeight * targetScale);

        // compute tx and ty
        float targetTx = (getWidth() - zoomedW) / 2;
        if (anchorX >= 0 && zoomedW > getWidth()) {
            final float tAnchorX = anchorX - rect.left;
            targetTx = Math.max(getWidth()-zoomedW, Math.min(0, anchorX - tAnchorX * targetScale/currScale));
        }

        float targetTy = (getHeight() - zoomedH) / 2;
        if (anchorY >= 0 && zoomedH > getHeight()) {
            final float tAnchorY = anchorY - rect.top;
            targetTy = Math.max(getHeight()-zoomedH, Math.min(0, anchorY - tAnchorY * targetScale/currScale));
        }

        // start translation
        if (animated) {
            post(new ZoomRunnable(targetScale, targetTx, targetTy, duration));
        } else {
            setImageTranslation(targetScale, targetTx, targetTy);
        }
    }

    private class ZoomRunnable implements Runnable {
        private final float fromScale, toScale;
        private final float fromOffsetX, toOffsetX;
        private final float fromOffsetY, toOffsetY;
        private final int duration;
        private final long startTime;

        public ZoomRunnable(float toScale, float toOffsetX, float toOffsetY, int duration) {
            mImageMatrix.getValues(mMatrixValues);
            this.fromScale = mMatrixValues[Matrix.MSCALE_X];
            this.fromOffsetX = mMatrixValues[Matrix.MTRANS_X];
            this.fromOffsetY = mMatrixValues[Matrix.MTRANS_Y];
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

            setImageTranslation(currScale, currOffsetX, currOffsetY);

            if (t < 1f) {
                postOnAnimation(this);
            }
        }
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
            mImageMatrix.getValues(mMatrixValues);

            float currScale = mMatrixValues[Matrix.MSCALE_X];
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
            transitDrawable(distanceX, distanceY);
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

    @Override
    public void onLoadFail() {

    }

    @Override
    public void onDecodeThumbnailSuccess() {
        Log.d("TAG", "onDecodeThumbnailSuccess: " + SystemClock.uptimeMillis());
        invalidate();
    }

    @Override
    public void onDecodeBlockSuccess() {
        Log.d("TAG", "onDecodeBlockSuccess: " + SystemClock.uptimeMillis());
        invalidate();
    }

    @Override
    public void onImageSizeReady(int width, int height) {
        Log.d("TAG", "onImageSizeReady: " + SystemClock.uptimeMillis());
        final int oldWidth = mDrawableWidth;
        final int oldHeight = mDrawableHeight;

        mDrawableWidth = width;
        mDrawableHeight = height;

        if (mDrawableWidth != oldWidth || mDrawableHeight != oldHeight) {
            requestLayout();
        }
        invalidate();
    }

    private static class RectCache {
        private Pools.SimplePool<Rect> mRectPool = new Pools.SimplePool<>(5);
        private Pools.SimplePool<RectF> mRectFPool = new Pools.SimplePool<>(5);

        public Rect obtainRect() {
            Rect rect = mRectPool.acquire();
            if (rect == null) {
                rect = new Rect();
            }
            return rect;
        }

        private Rect obtainRect(int left, int top, int right, int bottom) {
            Rect rect = obtainRect();
            rect.set(left, top, right, bottom);
            return rect;
        }

        public RectF obtainRectF() {
            RectF rectF = mRectFPool.acquire();
            if (rectF == null) {
                rectF = new RectF();
            }
            return rectF;
        }

        private RectF obtainRectF(float left, float top, float right, float bottom) {
            RectF rectF = obtainRectF();
            rectF.set(left, top, right, bottom);
            return rectF;
        }

        public void recycle(Rect rect) {
            mRectPool.release(rect);
        }

        public void recycle(RectF rectF) {
            mRectFPool.release(rectF);
        }
    }
}
