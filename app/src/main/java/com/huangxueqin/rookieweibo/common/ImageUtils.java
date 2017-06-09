package com.huangxueqin.rookieweibo.common;

/**
 * Created by huangxueqin on 2017/6/9.
 */

public class ImageUtils {
    public static boolean isLongImage(int width, int height) {
        return height >= 2*width;
    }
}
