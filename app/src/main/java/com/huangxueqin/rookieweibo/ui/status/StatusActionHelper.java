package com.huangxueqin.rookieweibo.ui.status;

import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;
import com.huangxueqin.rookieweibo.GalleryActivity;
import com.huangxueqin.rookieweibo.WeiboActivity;
import com.huangxueqin.rookieweibo.cons.Cons;
import com.sina.weibo.sdk.openapi.models.Status;
import com.sina.weibo.sdk.openapi.models.User;

/**
 * Created by huangxueqin on 2017/3/12.
 */

public final class StatusActionHelper {

    public static void goUserPage(Context context, User user) {

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

    }

    public static void comment(Context context, Status status) {

    }
}
