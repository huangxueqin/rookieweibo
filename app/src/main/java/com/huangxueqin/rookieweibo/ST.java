package com.huangxueqin.rookieweibo;

import android.text.TextUtils;

import com.sina.weibo.sdk.openapi.models.Status;

/**
 * Created by huangxueqin on 2017/2/28.
 */

public class ST {
    public static final int NONE = 0;
    public static final int SIMPLE = 1;
    public static final int IMAGE = 2;
    public static final int MUSIC = 3;
    public static final int VIDEO = 4;
    public static final int RT_SIMPLE = 5;
    public static final int RT_IMAGE = 6;
    public static final int RT_MUSIC = 7;
    public static final int RT_VIDEO = 8;

    public static int getStatusType(Status status) {
        if (!TextUtils.isEmpty(status.original_pic)) {
            return IMAGE;
        }
        if (status.retweeted_status != null) {
            if (getStatusType(status.retweeted_status) == IMAGE) {
                return RT_IMAGE;
            }
            return RT_SIMPLE;
        }
        return SIMPLE;
    }
}
