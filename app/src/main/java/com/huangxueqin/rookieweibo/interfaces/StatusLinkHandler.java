package com.huangxueqin.rookieweibo.interfaces;

import com.huangxueqin.rookieweibo.ui.widget.StatusTextView;

/**
 * Created by huangxueqin on 2017/3/13.
 */

public interface StatusLinkHandler {
    void handleURL(StatusTextView view, String url);
    void handleAt(StatusTextView view, String at);
    void handleTopic(StatusTextView view, String topic);
}
