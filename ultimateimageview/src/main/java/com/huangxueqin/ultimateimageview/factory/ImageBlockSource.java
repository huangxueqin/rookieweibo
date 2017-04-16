package com.huangxueqin.ultimateimageview.factory;

import android.graphics.BitmapRegionDecoder;

/**
 * Created by huangxueqin on 2017/4/16.
 */

public interface ImageBlockSource {
    BitmapRegionDecoder make();
}
