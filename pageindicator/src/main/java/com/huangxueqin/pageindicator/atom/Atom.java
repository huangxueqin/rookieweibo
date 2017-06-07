package com.huangxueqin.pageindicator.atom;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by huangxueqin on 2017/4/23.
 */

public abstract class Atom extends Drawable {

    private int position;
    private int alpha;
    private Matrix drawMatrix;

    public Atom(int position) {
        this.position = position;
        this.alpha = 255;
        this.drawMatrix = new Matrix();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {

    }

    @Override
    public void setAlpha(@IntRange(from = 0, to = 255) int alpha) {
        this.alpha = alpha;
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
}
