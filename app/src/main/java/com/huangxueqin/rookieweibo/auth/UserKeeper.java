package com.huangxueqin.rookieweibo.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.sina.weibo.sdk.openapi.models.User;

/**
 * Created by huangxueqin on 2017/2/22.
 */

public class UserKeeper {
    private static final String PREFERENCE_NAME = "com_weibo_sdk_android_user";
    private static final String KEY_USER = "user_str";

    public static void writeUser(Context context, String userStr) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_USER, userStr).commit();
    }

    public static User readUser(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        String s = sp.getString(KEY_USER, "");
        return User.parse(s);
    }

    public static void clear(Context context) {
        context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .commit();
    }
}
