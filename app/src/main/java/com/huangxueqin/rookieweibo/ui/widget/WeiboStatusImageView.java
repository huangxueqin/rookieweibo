package com.huangxueqin.rookieweibo.ui.widget;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by huangxueqin on 2017/6/9.
 * This customized image view to display status image thumbnails correctly
 */

public class WeiboStatusImageView extends android.support.v7.widget.AppCompatImageView {

    private int dw = -1;
    private int dh = -1;
    private Matrix matrix = new Matrix();

    public WeiboStatusImageView(Context context) {
        this(context, null);
    }

    public WeiboStatusImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WeiboStatusImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setScaleType(ScaleType.MATRIX);
    }

    private boolean sizeReady() {
        return getMeasuredHeight() > 0 && getMeasuredWidth() > 0;
    }

    private void updateImageMatrix() {
        final int w = getMeasuredWidth();
        final int h = getMeasuredHeight();

        if (dw/(float)dh >= w/(float)h) { // width first
            final float scale = h / (float)dh;
            final float left = (w - dw*scale)/2;
            matrix.setScale(scale, scale);
            matrix.postTranslate(left, 0);
        } else {
            final float scale = w / (float)dw;
            matrix.setScale(scale, scale);
        }
        setImageMatrix(matrix);
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        super.setImageDrawable(drawable);
        if (drawable != null) {
            dw = drawable.getIntrinsicWidth();
            dh = drawable.getIntrinsicHeight();

            if (sizeReady()) {
                updateImageMatrix();
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (dw > 0 && dh > 0) {
            updateImageMatrix();
        }
    }
}
