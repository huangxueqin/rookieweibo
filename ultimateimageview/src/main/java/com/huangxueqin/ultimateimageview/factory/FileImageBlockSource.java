package com.huangxueqin.ultimateimageview.factory;

import android.graphics.BitmapRegionDecoder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by huangxueqin on 2017/4/16.
 */

public class FileImageBlockSource implements ImageBlockSource {

    private final File imageFile;

    public FileImageBlockSource(File file) {
        imageFile = file;
    }

    @Override
    public BitmapRegionDecoder make() {
        BitmapRegionDecoder decoder = null;
        try {
            decoder = BitmapRegionDecoder.newInstance(new FileInputStream(imageFile), false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return decoder;
    }
}
