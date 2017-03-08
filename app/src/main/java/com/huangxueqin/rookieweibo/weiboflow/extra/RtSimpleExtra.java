package com.huangxueqin.rookieweibo.weiboflow.extra;

import android.view.View;

import com.huangxueqin.rookieweibo.R;
import com.huangxueqin.rookieweibo.weiboViewModel.WeiboLinkHandler;
import com.huangxueqin.rookieweibo.widget.StatusTextView;
import com.sina.weibo.sdk.openapi.models.Status;

/**
 * Created by huangxueqin on 2017/3/3.
 */

public class RtSimpleExtra extends ViewExtra {
    StatusTextView statusText;

    public RtSimpleExtra(View itemView) {
        super(itemView);
        statusText = (StatusTextView) itemView.findViewById(R.id.deco_rt_status_text);
    }

    @Override
    public void setup(Status status) {
        final Status rtStatus = status.retweeted_status;
        String str = (rtStatus.user == null ? "" : "@"+rtStatus.user.screen_name+": ") +
                rtStatus.text;
        statusText.setText(str);
    }

    @Override
    public void setLinkHandler(WeiboLinkHandler linkHandler) {
        statusText.setLinkHandler(linkHandler);
    }
}
