package com.huangxueqin.ultimateimageview;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.huangxueqin.ultimateimageview.utils.MathUtils;
import com.huangxueqin.ultimateimageview.utils.RectPool;

import java.io.File;

/**
 * Created by huangxueqin on 2017/6/7.
 */

public class RwImageView extends android.support.v7.widget.AppCompatImageView implements LargeImageDrawable.ImageSourceCallback {

    private static final String TAG = "RwImageView";

    private GestureDetector mGestureDetector;
    private Matrix mTempMatrix = new Matrix();
    private float[] mTempValues = new float[9];

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
        setScaleType(ScaleType.MATRIX);
        setClickable(true);
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

    public void setLargeImage(File imageFile) {
        LargeImageDrawable d = new LargeImageDrawable(getContext(), imageFile);
        setImageDrawable(d);
        d.setImageSourceCallback(this);
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
            offsetDrawable(-distanceX, -distanceY);
            return true;
        }
    };

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

    @Override
    public void onImageSizeReady(int width, int height) {
        mDrawableWidth = width;
        mDrawableHeight = height;
        Log.d("TAG", "size ready, width = " + width + ", height = " + height);
        if (sizeReady()) {
            initImageMatrix();
        }
    }
}
