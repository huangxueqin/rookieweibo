package com.huangxueqin.rookieweibo.common;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.huangxueqin.rookieweibo.cons.WeiboPattern;
import com.huangxueqin.rookieweibo.ui.emoji.Emoticon;
import com.huangxueqin.rookieweibo.ui.emoji.EmoticonManager;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;

/**
 * Created by huangxueqin on 2017/6/6.
 */

public class EmoticonStringFormatter {
    private static HashMap<Integer, Bitmap> sEmoticonMap = new HashMap<>();

    public static SpannableStringBuilder format(Context context, CharSequence chars, float textSize) {
        final Resources res = context.getResources();
        SpannableStringBuilder ssb = new SpannableStringBuilder(chars);
        Matcher matcher = WeiboPattern.PATTER_EMOTICON.matcher(ssb);
        while (matcher.find()) {
            final String key = matcher.group();
            final int start = matcher.start();
            final Emoticon e = EmoticonManager.getInstance().getEmoticon(key.substring(1, key.length()-1));
            if (e != null) {
                final int eSize = (int) (textSize * 13 / 10);
                Bitmap eImg = sEmoticonMap.get(e.getResId());
                if (eImg != null) {
                    ImageSpan span = new ImageSpan(context, eImg);
                    ssb.setSpan(span, start, start + key.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else {
                    Glide.with(context).load(e.getResId()).asBitmap().into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            sEmoticonMap.put(e.getResId(), Bitmap.createScaledBitmap(resource, eSize, eSize, true));
                        }
                    });
                }
            }
        }
        return ssb;
    }
}
