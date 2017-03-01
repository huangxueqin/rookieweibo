package com.huangxueqin.rookieweibo.cons;

import android.text.TextUtils;

import com.sina.weibo.sdk.openapi.models.Status;

/**
 * Created by huangxueqin on 2017/2/28.
 */

public class ST {
    public final static int NONE = 0;
    public final static int SIMPLE = 1;
    public final static int IMAGE = 2;
    public final static int MUSIC = 3;
    public final static int VIDEO = 4;
    public final static int RT_SIMPLE = 5;
    public final static int RT_IMAGE = 6;
    public final static int RT_MUSIC = 7;
    public final static int RT_VIDEO = 8;

    public static int getTypeForStatus(Status status) {
        if (!TextUtils.isEmpty(status.original_pic)) {
            return IMAGE;
        }
        if (status.retweeted_status != null) {
            final int rtType = getTypeForStatus(status.retweeted_status);
            if (rtType == IMAGE) {
                return RT_IMAGE;
            }
            return RT_SIMPLE;
        }
        return SIMPLE;
    }
}
