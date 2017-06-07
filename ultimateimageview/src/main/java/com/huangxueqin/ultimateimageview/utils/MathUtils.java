package com.huangxueqin.ultimateimageview.utils;

import android.graphics.Rect;
import android.graphics.RectF;

import static java.lang.Math.ceil;
import static java.lang.Math.floor;
import static java.lang.Math.round;

/**
 * Created by huangxueqin on 2017/6/7.
 */

public class MathUtils {

    public static int ceilTo(double a) {
        return (int) ceil(a);
    }

    public static int floorTo(double a) {
        return (int) floor(a);
    }

    public static int roundTo(double a) {
        return (int) round(a);
    }

    public static void roundTo(RectF src, Rect dst) {
        dst.set(roundTo(src.left),
                roundTo(src.top),
                roundTo(src.right),
                roundTo(src.bottom));
    }
}
