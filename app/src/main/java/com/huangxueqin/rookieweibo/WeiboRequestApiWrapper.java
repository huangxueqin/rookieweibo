package com.huangxueqin.rookieweibo;

import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;

import java.lang.ref.WeakReference;

/**
 * Created by huangxueqin on 2017/2/24.
 */

public abstract class WeiboRequestApiWrapper implements RequestListener {
    private boolean isExecuting;
    private boolean mCancel;

    @Override
    public void onComplete(String s) {
        if (!mCancel) {
            onRequestComplete(s);
        }
    }

    @Override
    public void onWeiboException(WeiboException e) {
        if (mCancel) {
            onRequestException(e);
        }
    }

    public void cancel() {
        mCancel = true;
    }

    public boolean isValid() {
        return mCancel == false;
    }

    public boolean isExecuting() {
        return isExecuting;
    }

    public void execute() {
        isExecuting = true;
    }

    protected void onRequestException(WeiboException e) {
        isExecuting = false;
    }
    protected void onRequestComplete(String s) {
        isExecuting = false;
    }

    protected void onRequestCancel() {
        isExecuting = false;
    }
}
