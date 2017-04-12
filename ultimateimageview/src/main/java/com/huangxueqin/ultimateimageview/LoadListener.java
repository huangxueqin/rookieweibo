package com.huangxueqin.ultimateimageview;

/**
 * Created by huangxueqin on 2017/4/6.
 */

public interface LoadListener {
    void onLoadFail();
    void onDecodeThumbnailSuccess();
    void onDecodeBlockSuccess();
    void onImageSizeReady(final int width, final int height);
}
