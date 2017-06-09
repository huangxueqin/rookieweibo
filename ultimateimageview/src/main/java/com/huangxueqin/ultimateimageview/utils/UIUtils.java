package com.huangxueqin.ultimateimageview.utils;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * Created by huangxueqin on 2017/6/9.
 */

public class UIUtils {
    public static Size getScreenSize(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return new Size(metrics.widthPixels, metrics.heightPixels);
    }
}
