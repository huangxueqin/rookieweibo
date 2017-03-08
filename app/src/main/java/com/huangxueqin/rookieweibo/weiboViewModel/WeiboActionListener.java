package com.huangxueqin.rookieweibo.weiboViewModel;

import android.view.View;

import com.sina.weibo.sdk.openapi.models.Status;
import com.sina.weibo.sdk.openapi.models.User;

/**
 * Created by huangxueqin on 2017/3/3.
 */

public interface WeiboActionListener {
    int ACTION_USER = 0;
    int ACTION_STATUS = 1;
    int ACTION_OPT_MENU = 2;
    int ACTION_IMAGES = 3;
    int ACTION_ATTITUDES = 4;
    int ACTION_REPOSTS = 5;
    int ACTION_COMMENTS = 6;
    int ACTION_RETWEET = 7;

    void onStatusAction(View view, Status status, int action);
}
