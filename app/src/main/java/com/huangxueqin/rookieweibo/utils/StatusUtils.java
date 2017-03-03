package com.huangxueqin.rookieweibo.utils;

import android.text.SpannableStringBuilder;
import android.util.LruCache;
import com.sina.weibo.sdk.openapi.models.Status;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by huangxueqin on 2017/3/1.
 */

public class StatusUtils {
    private static final int MAX_CACHE_SIZE = 200;

    private static class CachedValue {
        String[] middlePics;
        String[] largePics;
    }

    private static final LruCache<String, CachedValue> CACHE = new LruCache<>(MAX_CACHE_SIZE);
    private static final SimpleDateFormat STATUS_TIME_FORMATTER = new SimpleDateFormat("EEE MMM d HH:mm:ss Z yyyy", Locale.US);
    private static final SimpleDateFormat SIMPLE_FORMATTER = new SimpleDateFormat("HH:mm");
    private static final SimpleDateFormat FULL_FORMATTER = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");

    private static Object queryFromCache(String id, String fieldName) {
        CachedValue cv = CACHE.get(id);
        if (cv == null) return null;
        try {
            Field field = cv.getClass().getField(fieldName);
            field.setAccessible(true);
            field.get(cv);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void saveToCache(String id, Object obj, String fieldName) {
        CachedValue cv = CACHE.get(id);
        if (cv == null) {
            cv = new CachedValue();
            CACHE.put(id, cv);
        }
        try {
            Field field = cv.getClass().getField(fieldName);
            field.setAccessible(true);
            field.set(cv, obj);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static String[] getThumbnailPics(Status status) {
        if (status.pic_urls == null) return null;
        return status.pic_urls.toArray(new String[0]);
    }



    public static String[] getMiddlePics(Status status) {
        if (status.pic_urls == null) return null;
        String[] pics = (String[]) queryFromCache(status.id, "middlePics");
        if (pics == null) {
            pics = new String[status.pic_urls.size()];
            for (int i = 0; i < status.pic_urls.size(); i++) {
                pics[i] = status.pic_urls.get(i).replace("thumbnail", "bmiddle");
            }
            saveToCache(status.id, pics, "middlePics");
        }
        return pics;
    }

    public static String[] getLargePics(Status status) {
        if (status.pic_urls == null) return null;
        String[] pics = (String[]) queryFromCache(status.id, "largePics");
        if (pics == null) {
            pics = new String[status.pic_urls.size()];
            for (int i = 0; i < status.pic_urls.size(); i++) {
                pics[i] = status.pic_urls.get(i).replace("thumbnail", "large");
            }
            saveToCache(status.id, pics, "largePics");
        }
        return pics;
    }

    public static String parseCreateTime(Status status) {
        try {
            Date now = new Date();
            Date date = STATUS_TIME_FORMATTER.parse(status.created_at);
            long diffMills = now.getTime() - date.getTime();
            long diffMinutes = diffMills / (1000 * 60);
            if (diffMinutes < 1) {
                return "刚刚";
            } else if (diffMinutes < 60) {
                return diffMinutes + "分钟前";
            } else {
                if (DateUtils.sameDay(now, date)) {
                    return "今天 " + SIMPLE_FORMATTER.format(date);
                } else {
                    return FULL_FORMATTER.format(date);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }
}
