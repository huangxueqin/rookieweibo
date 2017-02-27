package com.huangxueqin.rookieweibo;

import android.text.TextUtils;

import com.sina.weibo.sdk.openapi.models.Status;

/**
 * Created by huangxueqin on 2017/2/27.
 */

public class StatusType {
    public static final int NONE = 0x0;
    public static final int SIMPLE = 0x1;
    public static final int IMAGE = 0x2;
    public static final int VIDEO = 0x3;
    public static final int MUSIC = 0x4;
    public static final int RETWEET = 0x5;

    private static final int SHIFT = 4;
    private static final int MASK = 0xF;

    public static int getType(Status status) {
        if (!TextUtils.isEmpty(status.original_pic)) {
            return IMAGE;
        }
        if (status.retweeted_status != null) {
            return RETWEET | (getType(status.retweeted_status) << SHIFT);
        }
        return SIMPLE;
    }

    public static int getBaseType(int statusType) {
        return statusType & MASK;
    }

    public static int getRetweetStatusType(int statusType) {
        return (statusType >> SHIFT) & MASK;
    }
}
