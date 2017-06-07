package com.huangxueqin.ultimateimageview.utils;

import android.graphics.Rect;
import android.graphics.RectF;

/**
 * Created by huangxueqin on 2017/6/7.
 */

public class RectPool {
    private final Rect[] mRects;
    private final RectF[] mRectFs;

    private int mRectPoolSize;
    private int mRectFPoolSize;

    public RectPool(int maxPoolSize) {
        if (maxPoolSize <= 0) {
            throw new IllegalArgumentException("The max pool size must be > 0");
        }
        mRects = new Rect[maxPoolSize];
        mRectFs = new RectF[maxPoolSize];
        mRectPoolSize = 0;
    }

    public Rect acquireRect() {
        if (mRectPoolSize > 0) {
            int last = mRectPoolSize-1;
            Rect r = mRects[last];
            mRects[last] = null;
            mRectPoolSize--;
            return r;
        } else {
            return new Rect();
        }
    }

    public Rect acquireRect(int left, int top, int right, int bottom) {
        Rect r = acquireRect();
        r.set(left, top, right, bottom);
        return r;
    }

    public RectF acquireRectF() {
        if (mRectFPoolSize > 0) {
            int last = mRectFPoolSize-1;
            RectF r = mRectFs[last];
            mRectFs[last] = null;
            mRectFPoolSize--;
            return r;
        } else {
            return new RectF();
        }
    }

    public RectF acquireRectF(float left, float top, float right, float bottom) {
        RectF rf = acquireRectF();
        rf.set(left, top, right, bottom);
        return rf;
    }

    public void release(Rect r) {
        if (isInPool(r)) {
            throw new IllegalStateException("Already in the pool!");
        }
        if (mRectPoolSize < mRects.length) {
            mRects[mRectPoolSize] = r;
            mRectPoolSize++;
        }
    }

    public void release(RectF rf) {
        if (isInPool(rf)) {
            throw new IllegalStateException("Already in the pool!");
        }
        if (mRectFPoolSize < mRectFs.length) {
            mRectFs[mRectFPoolSize] = rf;
            mRectFPoolSize++;
        }
    }

    private boolean isInPool(Rect r) {
        for (int i = 0; i < mRectPoolSize; i++) {
            if (mRects[i] == r) {
                return true;
            }
        }
        return false;
    }

    private boolean isInPool(RectF rf) {
        for (int i = 0; i < mRectFPoolSize; i++) {
            if (mRectFs[i] == rf) {
                return true;
            }
        }
        return false;
    }
}
