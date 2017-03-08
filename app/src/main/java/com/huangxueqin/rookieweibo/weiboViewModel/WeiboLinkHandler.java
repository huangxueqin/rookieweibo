package com.huangxueqin.rookieweibo.weiboViewModel;

import android.content.Context;

/**
 * Created by huangxueqin on 2017/3/2.
 */

public interface WeiboLinkHandler {
    void handleTopic(String topic);
    void handleURL(String url);
    void handleAT(String user);
}
