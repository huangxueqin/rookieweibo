package com.huangxueqin.rookieweibo.ui.status;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.gson.Gson;
import com.huangxueqin.rookieweibo.AppConfiguration;
import com.huangxueqin.rookieweibo.GalleryActivity;
import com.huangxueqin.rookieweibo.RepostActivity;
import com.huangxueqin.rookieweibo.WeiboActivity;
import com.huangxueqin.rookieweibo.WeiboUserActivity;
import com.huangxueqin.rookieweibo.cons.Cons;
import com.sina.weibo.sdk.openapi.models.Status;
import com.sina.weibo.sdk.openapi.models.User;

/**
 * Created by huangxueqin on 2017/3/12.
 */

public final class StatusActionHelper {

    public static void goUserPage(Context context, User user) {
        Intent intent = new Intent(context, WeiboUserActivity.class);
        intent.putExtra(Cons.IntentKey.USER, new Gson().toJson(user));
        context.startActivity(intent);
    }

    public static void goStatusPage(Context context, Status status) {
        final String statusStr = new Gson().toJson(status);
        Intent statusIntent = new Intent(context, WeiboActivity.class);
        statusIntent.putExtra(Cons.IntentKey.STATUS, statusStr);
        context.startActivity(statusIntent);
    }

    public static void goGallery(Context context, String[] images, int index) {
        Intent intent = new Intent(context, GalleryActivity.class);
        intent.putExtra(Cons.IntentKey.IMAGE_LIST, images);
        intent.putExtra(Cons.IntentKey.SELECT_INDEX, index);
        context.startActivity(intent);
    }

    public static void attitude(Context context, Status status) {

    }

    public static void repost(Context context, Status status) {
        Intent repost = new Intent(context, RepostActivity.class);
        repost.putExtra(Cons.IntentKey.STATUS, new Gson().toJson(status));
        context.startActivity(repost);
    }

    public static void comment(Context context, Status status) {

    }
}
