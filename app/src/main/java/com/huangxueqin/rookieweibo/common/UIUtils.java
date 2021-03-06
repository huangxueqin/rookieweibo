package com.huangxueqin.rookieweibo.common;

import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;

/**
 * Created by huangxueqin on 2017/3/31.
 */

public class UIUtils {

    public static int getColor(Context context, int resId) {
        return ContextCompat.getColor(context, resId);
    }

    public static Size getScreenSize(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return new Size(metrics.widthPixels, metrics.heightPixels);
    }
}
