package com.huangxueqin.rookieweibo.interfaces;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.openapi.models.User;

/**
 * Created by huangxueqin on 2017/2/23.
 */

public interface IFragmentCallback {
    User getUser();
    Oauth2AccessToken getAccessToken();
}
