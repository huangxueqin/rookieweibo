package com.huangxueqin.rookieweibo.common;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;

import com.huangxueqin.rookieweibo.cons.WeiboPattern;
import com.huangxueqin.rookieweibo.ui.emoji.Emoticon;
import com.huangxueqin.rookieweibo.ui.emoji.EmoticonManager;

import java.util.regex.Matcher;

/**
 * Created by huangxueqin on 2017/6/6.
 */

public class EmoticonStringFormatter {
    public static SpannableStringBuilder format(Context context, CharSequence chars, float textSize) {
        Resources res = context.getResources();
        SpannableStringBuilder ssb = new SpannableStringBuilder(chars);
        Matcher matcher = WeiboPattern.PATTER_EMOTICON.matcher(ssb);
        while (matcher.find()) {
            final String key = matcher.group();
            final int start = matcher.start();
            final Emoticon e = EmoticonManager.getInstance().getEmoticon(key.substring(1, key.length()-1));
            if (e != null) {
                final int eSize = (int) (textSize*13/10);
                Bitmap eImg = BitmapFactory.decodeResource(res, e.getResId());
                Bitmap eScaledImg = Bitmap.createScaledBitmap(eImg, eSize, eSize, true);
                ImageSpan span = new ImageSpan(context, eScaledImg);
                ssb.setSpan(span, start, start+key.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return ssb;
    }
}
