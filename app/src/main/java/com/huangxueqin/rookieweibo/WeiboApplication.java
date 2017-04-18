package com.huangxueqin.rookieweibo;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.util.LruCache;

/**
 * Created by huangxueqin on 2017/4/18.
 */

public class WeiboApplication extends Application {
    LruResourceCache mAppImageCache;

    @Override
    public void onCreate() {
        super.onCreate();

        mAppImageCache = new LruResourceCache((int) computeMaxImageCacheSize());
    }

    private long computeMaxImageCacheSize() {
        Log.d("TAG", "maxMemeory = " + Runtime.getRuntime().maxMemory());
        return Runtime.getRuntime().maxMemory()/8;
    }

    public static LruResourceCache getAppImageCache(Context context) {
        return ((WeiboApplication)context.getApplicationContext()).mAppImageCache;
    }
}
