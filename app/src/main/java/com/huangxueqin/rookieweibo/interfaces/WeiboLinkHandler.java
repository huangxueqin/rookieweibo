package com.huangxueqin.rookieweibo.interfaces;

import android.content.Context;

/**
 * Created by huangxueqin on 2017/3/2.
 */

public interface WeiboLinkHandler {
    void handleTopic(Context context, String topic);
    void handleURL(Context context, String url);
    void handleAT(Context context, String user);
}
