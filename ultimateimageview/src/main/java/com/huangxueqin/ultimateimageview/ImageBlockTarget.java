package com.huangxueqin.ultimateimageview;

/**
 * Created by huangxueqin on 2017/4/6.
 */

public interface ImageBlockTarget {
    void onLoadFail();
    void onDecodeBlockSuccess();
    void onImageSizeReady(final int width, final int height);
}
