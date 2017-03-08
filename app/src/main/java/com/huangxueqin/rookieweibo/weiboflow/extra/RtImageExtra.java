package com.huangxueqin.rookieweibo.weiboflow.extra;

import android.view.View;

import com.huangxueqin.rookieweibo.R;
import com.huangxueqin.rookieweibo.utils.StatusUtils;
import com.huangxueqin.rookieweibo.widget.WeiboImageGrid;
import com.sina.weibo.sdk.openapi.models.Status;

/**
 * Created by huangxueqin on 2017/3/3.
 */

public class RtImageExtra extends RtSimpleExtra {
    WeiboImageGrid imageGrid;

    public RtImageExtra(View itemView) {
        super(itemView);
        imageGrid = (WeiboImageGrid) itemView.findViewById(R.id.deco_rt_image_grid);
    }

    @Override
    public void setOnClickListener(View.OnClickListener listener) {
        imageGrid.setOnClickListener(listener);
    }

    @Override
    public void setup(Status status) {
        super.setup(status);
        imageGrid.setImage(StatusUtils.getMiddlePics(status.retweeted_status));
    }
}
