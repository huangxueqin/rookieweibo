package com.huangxueqin.rookieweibo;

import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;

import java.lang.ref.WeakReference;

/**
 * Created by huangxueqin on 2017/2/24.
 */

public abstract class WeiboApiWrapper implements RequestListener {
    private boolean mCancel;

    @Override
    public void onComplete(String s) {
        if (mCancel) {
            onRequestCancel();
        } else {
            onRequestComplete(s);
        }
    }

    @Override
    public void onWeiboException(WeiboException e) {
        if (mCancel) {
            onRequestCancel();
        } else {
            onRequestException(e);
        }
    }

    public void cancel() {
        mCancel = true;
    }

    public boolean isValid() {
        return mCancel == false;
    }

    abstract public void execute();
    protected void onRequestCancel() {}
    protected void onRequestException(WeiboException e) {}
    protected void onRequestComplete(String s) {}
}
