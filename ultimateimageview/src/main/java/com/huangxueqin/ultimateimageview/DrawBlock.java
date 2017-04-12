package com.huangxueqin.ultimateimageview;

import android.graphics.Bitmap;
import android.graphics.Rect;

/**
 * Created by huangxueqin on 2017/4/8.
 */

public class DrawBlock {
    // Bitmap to be drawn
    Bitmap bitmap;
    // The subset of the bitmap to be drawn
    Rect srcRect;
    // The rectangle that the drawing subset of the bitmap fit in source image
    Rect imgRect;

    public DrawBlock(Bitmap bitmap, Rect srcRect, Rect imgRect) {
        this.bitmap = bitmap;
        this.srcRect = srcRect;
        this.imgRect = imgRect;
    }
}
