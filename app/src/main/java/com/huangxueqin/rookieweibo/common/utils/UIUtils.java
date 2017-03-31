package com.huangxueqin.rookieweibo.common.utils;

import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;

/**
 * Created by huangxueqin on 2017/3/31.
 */

public class UIUtils {

    public static int getColor(Context context, int resId) {
        return ContextCompat.getColor(context, resId);
    }
}
