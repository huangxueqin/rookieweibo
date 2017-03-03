package com.huangxueqin.rookieweibo.weiboflow.extra;

import android.view.View;

import com.huangxueqin.rookieweibo.utils.StatusUtils;
import com.huangxueqin.rookieweibo.widget.WeiboImageGrid;
import com.sina.weibo.sdk.openapi.models.Status;

/**
 * Created by huangxueqin on 2017/3/3.
 */

public class ImageExtra extends ViewExtra {
    public ImageExtra(View itemView) {
        super(itemView);
    }

    @Override
    public void setOnClickListener(View.OnClickListener listener) {
        itemView.setOnClickListener(listener);
    }

    @Override
    public void setup(Status status) {
        WeiboImageGrid imageGrid = (WeiboImageGrid) itemView;
        imageGrid.setImage(StatusUtils.getMiddlePics(status));
    }
}
