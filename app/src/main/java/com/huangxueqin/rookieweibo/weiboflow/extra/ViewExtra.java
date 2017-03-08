package com.huangxueqin.rookieweibo.weiboflow.extra;

import android.view.View;

import com.huangxueqin.rookieweibo.weiboViewModel.WeiboLinkHandler;
import com.sina.weibo.sdk.openapi.models.Status;

/**
 * Created by huangxueqin on 2017/3/3.
 */

public abstract class ViewExtra {

    public View itemView;

    public ViewExtra(View itemView) {
        this.itemView = itemView;
    }

    public void setOnClickListener(View.OnClickListener listener) {}
    public void setLinkHandler(WeiboLinkHandler linkHandler) {}

    public abstract void setup(Status status);
}
