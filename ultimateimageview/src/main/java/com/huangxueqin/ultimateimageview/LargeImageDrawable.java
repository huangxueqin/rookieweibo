package com.huangxueqin.ultimateimageview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;

import com.huangxueqin.ultimateimageview.factory.FileImageBlockSource;

import java.io.File;
import java.util.List;

/**
 * Created by huangxueqin on 2017/6/7.
 */

public class LargeImageDrawable extends Drawable implements ImageBlockTarget {

    private float mSample;
    private Rect mDrawBounds = new Rect();
    private ImageSourceCallback mImageSourceCallback;
    private int mIntrinsicWidth = -1;
    private int mIntrinsicHeight = -1;
    private DrawBlockLoader mBlockLoader;

    public LargeImageDrawable(Context context, File imageFile) {
        FileImageBlockSource source = new FileImageBlockSource(imageFile);
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        final int blockSize = Math.min(metrics.widthPixels, metrics.heightPixels)/2;
        mBlockLoader = new DrawBlockLoader(context,
                this,
                source,
                blockSize,
                blockSize/2);
        // drive to get image size
        mBlockLoader.getDrawData(1, new Rect(0, 0, 1, 1));
    }

    public void setDrawBounds(Rect bounds) {
        mDrawBounds.set(bounds);
    }

    public void setSample(float sample) {
        Log.d("TAG", "sample = " + sample);
        mSample = sample;
    }

    public void setImageSourceCallback(ImageSourceCallback callback) {
        mImageSourceCallback = callback;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (mDrawBounds.width() > 0 && mDrawBounds.height() > 0) {
            List<DrawBlock> blocks = mBlockLoader.getDrawData(mSample, mDrawBounds);
            for (DrawBlock block : blocks) {
                canvas.drawBitmap(block.bitmap, block.srcRect, block.imgRect, null);
            }
        }
    }

    @Override
    public void setAlpha(@IntRange(from = 0, to = 255) int alpha) {

    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public int getIntrinsicWidth() {
        return mIntrinsicWidth;
    }

    @Override
    public int getIntrinsicHeight() {
        return mIntrinsicHeight;
    }

    @Override
    public void onLoadFail() {

    }

    @Override
    public void onDecodeBlockSuccess() {
        invalidateSelf();
    }

    @Override
    public void onImageSizeReady(int width, int height) {
        mIntrinsicWidth = width;
        mIntrinsicHeight = height;
        if (mImageSourceCallback != null) {
            mImageSourceCallback.onImageSizeReady(width, height);
        }
    }

    public interface ImageSourceCallback {
        void onImageSizeReady(int width, int height);
    }
}
