package com.huangxueqin.rookieweibo.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.view.View;
import android.widget.Toast;

import com.huangxueqin.rookieweibo.BrowserActivity;
import com.huangxueqin.rookieweibo.R;
import com.huangxueqin.rookieweibo.cons.WeiboPattern;
import com.huangxueqin.rookieweibo.interfaces.WeiboLinkHandler;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.sina.weibo.sdk.openapi.models.Status;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by huangxueqin on 2017/3/1.
 */

public class StatusUtils {
    static class StatusCache {
        String[] thumbnailPics;
        String[] middlePics;
        String[] largePics;
        SpannableStringBuilder statusText;
        SpannableStringBuilder rtStatusText;
    }

    private static final HashMap<String, StatusCache> CACHE = new HashMap<>();
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("EEE MMM d HH:mm:ss Z yyyy", Locale.US);
    private static final SimpleDateFormat SIMPLE_READABLE_FORMATTER = new SimpleDateFormat("HH:mm");
    private static final SimpleDateFormat READABLE_FORMATTER = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");

    private static final WeiboLinkHandler LINK_HANDLER = new WeiboLinkHandler() {
        @Override
        public void handleTopic(Context context, String topic) {

        }

        @Override
        public void handleURL(Context context, String url) {
            Intent browser = new Intent(context, BrowserActivity.class);
            browser.putExtra("content-url", url);
            context.startActivity(browser);
        }

        @Override
        public void handleAT(Context context, String user) {

        }
    };

    public static String[] getThumbnailPics(Status status) {
        if (status.pic_urls == null || status.pic_urls.size() == 0) return null;

        final String id = status.id;
        StatusCache cache = CACHE.get(id);
        if (cache != null && cache.thumbnailPics != null) {
            return cache.thumbnailPics;
        }
        String[] thumbnails = new String[status.pic_urls.size()];
        for (int i = 0; i < status.pic_urls.size(); i++) {
            thumbnails[i] = status.pic_urls.get(i);
        }
        if (cache == null) {
            cache = new StatusCache();
        }
        cache.thumbnailPics = thumbnails;
        CACHE.put(id, cache);
        return thumbnails;
    }

    public static String[] getMiddlePics(Status status) {
        if (status.pic_urls == null || status.pic_urls.size() == 0) return null;

        final String id = status.id;
        StatusCache cache = CACHE.get(id);
        if (cache != null && cache.middlePics != null) {
            return cache.middlePics;
        }
        String[] middlePics = new String[status.pic_urls.size()];
        for (int i = 0; i < status.pic_urls.size(); i++) {
            middlePics[i] = status.pic_urls.get(i).replace("thumbnail", "bmiddle");
        }
        if (cache == null) {
            cache = new StatusCache();
        }
        cache.middlePics = middlePics;
        CACHE.put(id, cache);
        return middlePics;
    }

    public static String[] getLargePics(Status status) {
        if (status.pic_urls == null || status.pic_urls.size() == 0) return null;

        final String id = status.id;
        StatusCache cache = CACHE.get(id);
        if (cache != null && cache.largePics != null) {
            return cache.largePics;
        }
        String[] largePics = new String[status.pic_urls.size()];
        for (int i = 0; i < status.pic_urls.size(); i++) {
            largePics[i] = status.pic_urls.get(i).replace("thumbnail", "large");
        }
        if (cache == null) {
            cache = new StatusCache();
        }
        cache.largePics = largePics;
        CACHE.put(id, cache);
        return largePics;
    }

    public static String getReadableDate(Status status) {
        try {
            Date now = new Date();
            Date date = DATE_FORMATTER.parse(status.created_at);
            long diffMills = now.getTime() - date.getTime();
            long diffMinutes = diffMills / (1000 * 60);
            if (diffMinutes < 1) {
                return "刚刚";
            } else if (diffMinutes < 60) {
                return diffMinutes + "分钟前";
            } else {
                if (DateUtils.sameDay(now, date)) {
                    return "今天 " + SIMPLE_READABLE_FORMATTER.format(date);
                } else {
                    return READABLE_FORMATTER.format(date);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static SpannableStringBuilder getFormattedStatusText(Context context, Status status) {
        final String id = status.id;
        StatusCache cache = CACHE.get(id);
        if (cache != null && cache.statusText != null) {
            return cache.statusText;
        }
        SpannableStringBuilder ssb = formatWeiboStatusText(context, status.text);
        if (cache == null) {
            cache = new StatusCache();
        }
        cache.statusText = ssb;
        CACHE.put(id, cache);
        return ssb;
    }

    public static SpannableStringBuilder getFormattedRtStatusText(Context context, Status status) {
        final String id = status.id;
        StatusCache cache = CACHE.get(id);
        if (cache != null && cache.rtStatusText != null) {
            return cache.rtStatusText;
        }
        SpannableStringBuilder ssb = formatWeiboStatusText(context, "@"+status.user.screen_name+": "+status.text);
        if (cache == null) {
            cache = new StatusCache();
        }
        cache.rtStatusText = ssb;
        CACHE.put(id, cache);
        return ssb;
    }

    private static SpannableStringBuilder formatWeiboStatusText(final Context context, String source) {
        final int textSize = context.getResources().getDimensionPixelSize(R.dimen.weibo_flow_list_status_text);
        final int spanColor = context.getResources().getColor(R.color.grey2);
        SpannableStringBuilder ssb = new SpannableStringBuilder(source);

        Linkify.addLinks(ssb, WeiboPattern.PATTERN_TOPIC, WeiboPattern.SCHEME_TOPIC);
        Linkify.addLinks(ssb, WeiboPattern.PATTERN_URL, WeiboPattern.SCHEME_URL);
        Linkify.addLinks(ssb, WeiboPattern.PATTER_AT, WeiboPattern.SCHEME_AT);

        final URLSpan[] urlSpans = ssb.getSpans(0, ssb.length(), URLSpan.class);
        for (final URLSpan span : urlSpans) {
            ClickableSpan clickableSpan = new WeiboClickableSpan(span, spanColor);
            final int start = ssb.getSpanStart(span);
            final int end = ssb.getSpanEnd(span);
            ssb.removeSpan(span);
            if (span.getURL().startsWith(WeiboPattern.SCHEME_URL)) {
                Drawable linkIcon = new IconicsDrawable(context)
                        .icon(GoogleMaterial.Icon.gmd_link)
                        .color(spanColor)
                        .sizePx(textSize);
                ImageSpan imageSpan = new ImageSpan(linkIcon, ImageSpan.ALIGN_BASELINE);
                SpannableStringBuilder replaced = new SpannableStringBuilder("  网页链接");
                replaced.setSpan(imageSpan, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ssb.replace(start, end, replaced);
                ssb.setSpan(clickableSpan, start, start+replaced.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                ssb.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return ssb;
    }

    static class WeiboClickableSpan extends ClickableSpan {
        private final int spanColor;
        private final String scheme;
        private final String content;

        public WeiboClickableSpan (URLSpan urlSpan, int spanColor) {
            this.spanColor = spanColor;
            String url = urlSpan.getURL();
            if (url.startsWith(WeiboPattern.SCHEME_URL)) {
                scheme = WeiboPattern.SCHEME_URL;
            } else if (url.startsWith(WeiboPattern.SCHEME_AT)) {
                scheme = WeiboPattern.SCHEME_AT;
            } else {
                scheme = WeiboPattern.SCHEME_TOPIC;
            }
            content = url.substring(scheme.length());
        }

        @Override
        public void onClick(View widget) {
            if (scheme.equals(WeiboPattern.SCHEME_URL)) {
                final String url = content;
                LINK_HANDLER.handleURL(widget.getContext(), url);
            } else if (scheme.equals(WeiboPattern.SCHEME_TOPIC)) {
                final String topic = content.substring(1, content.length()-1);
                LINK_HANDLER.handleTopic(widget.getContext(), topic);
            } else {
                final String username = content.substring(1);
                LINK_HANDLER.handleAT(widget.getContext(), username);
            }
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setColor(spanColor);
            ds.setUnderlineText(false);
        }
    }

}
