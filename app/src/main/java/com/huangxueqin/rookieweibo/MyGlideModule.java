package com.huangxueqin.rookieweibo;

import android.content.Context;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.engine.cache.MemoryCache;
import com.bumptech.glide.module.GlideModule;

/**
 * Created by huangxueqin on 2017/4/18.
 */

public class MyGlideModule implements GlideModule {
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        builder.setMemoryCache(WeiboApplication.getAppImageCache(context));
        Log.d("TAG", "glide module set ok");
    }

    @Override
    public void registerComponents(Context context, Glide glide) {

    }
}
