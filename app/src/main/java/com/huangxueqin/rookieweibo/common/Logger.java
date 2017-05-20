package com.huangxueqin.rookieweibo.common;

import android.util.Log;

import com.huangxueqin.rookieweibo.BuildConfig;

/**
 * Created by huangxueqin on 2017/3/30.
 */

public class Logger {
    private final static boolean ENABLE_LOG = BuildConfig.DEBUG;


    public static void d(String tag, String msg) {
        if (ENABLE_LOG) {
            Log.d(tag, msg);
        }
    }
}
