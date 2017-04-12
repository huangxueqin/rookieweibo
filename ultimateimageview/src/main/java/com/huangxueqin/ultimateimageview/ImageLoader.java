package com.huangxueqin.ultimateimageview;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by huangxueqin on 2017/4/6.
 */

public class ImageLoader implements Handler.Callback {
    private static final int MSG_DECODE_INFO = 1;
    private static final int MSG_DECODE_THUMBNAIL = 2;
    private static final int MSG_DECODE_BLOCK = 3;

    private static final int MSG_LOAD_FAIL = 11;
    private static final int MSG_SIZE_READY = 12;
    private static final int MSG_THUMBNAIL_READY = 13;
    private static final int MSG_BLOCK_READY = 14;


    private final int BLOCK_SIZE;
    private final int EXTRA_CACHE_PIXELS;

    private Context mContext;
    private HandlerThread mLoadThread;
    private Handler mLoadHandler;
    private Handler mMainHandler;
    private LoadListener mLoadListener;

    private volatile String mCurrentFile;
    private volatile LoadSource mLoadSource;

    private volatile Rect mCacheRect = new Rect();
    private volatile ConcurrentHashMap<Integer, Bitmap> mCache = new ConcurrentHashMap<>();
    private Set<Integer> mLoadingBlocks = Collections.newSetFromMap(new ConcurrentHashMap<Integer, Boolean>());

    public ImageLoader(Context context) {
        mContext = context;
        mMainHandler = new Handler(Looper.getMainLooper(), this);

        Resources res = context.getResources();
        BLOCK_SIZE = ceil(res.getDisplayMetrics().widthPixels/2f);
        EXTRA_CACHE_PIXELS = ceil(res.getDisplayMetrics().density*200);
    }

    public void setLoadListener(LoadListener loadListener) {
        mLoadListener = loadListener;
    }

    public void setImage(String path) {
        if (mLoadSource != null) {
            if (mLoadSource.decoder != null) {
                mLoadSource.decoder.recycle();
            }
            mLoadSource = null;
        }

        if (mLoadHandler != null) {
            mLoadHandler.removeCallbacksAndMessages(null);
        }

        mCurrentFile = path;
        mCache.clear();
        mLoadingBlocks.clear();
    }

    private int getNearestSampleRate(float sampleRateF) {
        if (sampleRateF <= 1) {
            return 1;
        }
        int sampleRate = 1;
        while (sampleRate*2 <= sampleRateF) {
            sampleRate *= 2;
        }

        if (sampleRateF-sampleRate < sampleRate*2-sampleRateF) {
            return sampleRate;
        } else {
            return sampleRate*2;
        }
    }

    private void ensureLoadThread() {
        if (mLoadThread == null) {
            mLoadThread = new HandlerThread("image-loader-thread");
            mLoadThread.start();
        }

        if (mLoadHandler == null) {
            mLoadHandler = new Handler(mLoadThread.getLooper(), this);
        }
    }

    private void updateCacheRect(Rect imageRect) {
        final int left = Math.max(0, imageRect.left-EXTRA_CACHE_PIXELS);
        final int top = Math.max(0, imageRect.top-EXTRA_CACHE_PIXELS);
        final int right = Math.min(mLoadSource.imageWidth, imageRect.right+EXTRA_CACHE_PIXELS);
        final int bottom = Math.min(mLoadSource.imageHeight, imageRect.bottom+EXTRA_CACHE_PIXELS);
        mCacheRect.set(left, top, right, bottom);
    }

    public List<DrawBlock> getDrawData(float sampleRateF, Rect imageRect) {

        if (mCurrentFile == null) {
            return Collections.EMPTY_LIST;
        }

        ensureLoadThread();

        if (mLoadSource == null || mLoadSource.imageWidth == 0 || mLoadSource.imageHeight == 0) {
            mLoadHandler.sendEmptyMessage(MSG_DECODE_INFO);
            return Collections.EMPTY_LIST;
        }

//        final Bitmap thumbImage = mLoadSource.thumbImage;
//        final int thumbRate = mLoadSource.tSampleRate;
//        if (thumbImage == null) {
//            mLoadHandler.sendEmptyMessage(MSG_DECODE_THUMBNAIL);
//        } else if (sampleRateF >= thumbRate) {
//            Rect imgRect = new Rect(imageRect);
//            Rect srcRect = new Rect(round(1f*imgRect.left/thumbRate),
//                    round(1f*imgRect.top/thumbRate),
//                    round(1f*imgRect.right/thumbRate),
//                    round(1f*imgRect.bottom/thumbRate));
//            drawBlocks.add(new DrawBlock(thumbImage, srcRect, imgRect));
//            return drawBlocks;
//        }

        updateCacheRect(imageRect);

        final List<DrawBlock> drawBlocks = new ArrayList<>();
        final List<Integer> missingBlocks = new LinkedList<>();
        final int rowStart = floor(imageRect.top/(float)BLOCK_SIZE);
        final int rowEnd = ceil(imageRect.bottom/(float)BLOCK_SIZE);
        final int colStart = floor(imageRect.left/(float)BLOCK_SIZE);
        final int colEnd = ceil(imageRect.right/(float)BLOCK_SIZE);

        for (int i = rowStart; i< rowEnd; i++) {
            for (int j = colStart; j < colEnd; j++) {
                final int position = Position.makePosition(i, j);
                final Bitmap image = mCache.get(position);
                if (image == null) {
                    missingBlocks.add(position);
                } else {
                    final int left = j * BLOCK_SIZE;
                    final int top = i * BLOCK_SIZE;
                    final int width = Math.min(BLOCK_SIZE, mLoadSource.imageWidth-left);
                    final int height = Math.min(BLOCK_SIZE, mLoadSource.imageHeight-top);
                    Rect srcRect = new Rect(0, 0, width, height);
                    Rect imgRect = new Rect(left, top, left+width, top+height);
                    drawBlocks.add(new DrawBlock(image, srcRect, imgRect));
                }
            }
        }

        loadMissingBlocks(missingBlocks);

        // load extra caches
        final List<Integer> missingCachedBlocks = new LinkedList<>();
        final int cachedRowStart = floor(1f*mCacheRect.top/BLOCK_SIZE);
        final int cachedRowEnd = ceil(1f*mCacheRect.bottom/BLOCK_SIZE);
        final int cachedColStart = floor(1f*mCacheRect.left/BLOCK_SIZE);
        final int cachedColEnd = ceil(1f*mCacheRect.right/BLOCK_SIZE);

        for (int i = cachedRowStart; i < rowStart; i++) {
            for(int j=cachedColStart; j < cachedColEnd; j++) {
                final int position = Position.makePosition(i, j);
                if (mCache.get(position) == null) {
                    missingCachedBlocks.add(position);
                }
            }
        }

        for (int i = rowStart; i < rowEnd; i++) {
            for (int j = cachedColStart; j < colStart; j++) {
                final int position = Position.makePosition(i, j);
                if (mCache.get(position) == null) {
                    missingCachedBlocks.add(position);
                }
            }
        }

        for (int i = rowEnd; i < cachedRowEnd; i++) {
            for (int j = cachedColStart; j < cachedColEnd; j++) {
                final int position = Position.makePosition(i, j);
                if (mCache.get(position) == null) {
                    missingCachedBlocks.add(position);
                }
            }
        }

        for (int i = rowStart; i < rowEnd; i++) {
            for (int j = colEnd; j < cachedColEnd; j++) {
                final int position = Position.makePosition(i, j);
                if (mCache.get(position) == null) {
                    missingCachedBlocks.add(position);
                }
            }
        }

        loadMissingBlocks(missingCachedBlocks);

        return drawBlocks;
    }

    private void loadMissingBlocks(final List<Integer> missingBlocks) {
        Iterator<Integer> iter = missingBlocks.iterator();
        while (iter.hasNext()) {
            final Integer pos = iter.next();
            if (mLoadingBlocks.contains(pos)) {
                iter.remove();
            } else {
                mLoadingBlocks.add(pos);
            }
        }
        if (missingBlocks.size() > 0) {
            mLoadHandler.obtainMessage(MSG_DECODE_BLOCK, missingBlocks).sendToTarget();
        }
    }

    private void decodeThumbnail() {
        final BitmapRegionDecoder decoder = mLoadSource.decoder;
        final int imageWidth = mLoadSource.imageWidth;
        final int imageHeight = mLoadSource.imageHeight;
        final int screenWidth = mContext.getResources().getDisplayMetrics().widthPixels;
        final int screenHeight = mContext.getResources().getDisplayMetrics().heightPixels;

        float rate = Math.max(imageWidth/(float)screenWidth, imageHeight/(float)screenHeight);
        int nearestSampleRate = getNearestSampleRate(rate);
        if (nearestSampleRate < rate) {
            nearestSampleRate *= 2;
        }
        mLoadSource.tSampleRate = nearestSampleRate;
        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        decodeOptions.inSampleSize = nearestSampleRate;
        mLoadSource.thumbImage = decoder.decodeRegion(new Rect(0, 0, imageWidth, imageHeight), decodeOptions);
        if (mLoadSource.thumbImage != null) {
            mMainHandler.sendEmptyMessage(MSG_THUMBNAIL_READY);
        }
    }

    private Rect mTempDecodeRegion = new Rect();
    private Rect getDecodeRegion(final int row, final int col) {
        final int left = col * BLOCK_SIZE;
        final int top = row * BLOCK_SIZE;
        mTempDecodeRegion.set(left, top, left+BLOCK_SIZE, top+BLOCK_SIZE);
        return mTempDecodeRegion;
    }

    private boolean decodeBlock(int position) {
        if (mCache.get(position) != null) {
            return false;
        }

        final int row = Position.getRow(position);
        final int col = Position.getCol(position);
        Bitmap bitmap = mLoadSource.decoder.decodeRegion(getDecodeRegion(row, col), null);

        if (bitmap != null) {
            mCache.put(position, bitmap);
        }

        return bitmap != null;
    }

    private void cleanCache() {
        for (Iterator<Integer> iterator = mCache.keySet().iterator(); iterator.hasNext(); ) {
            final int position = iterator.next();
            final int row = Position.getRow(position);
            final int col = Position.getCol(position);
            if (!getDecodeRegion(row, col).intersect(mCacheRect)) {
                iterator.remove();
            }
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            // running on load thread
            case MSG_DECODE_INFO:
                if (mCurrentFile != null && mLoadSource == null) {
                    try {
                        final BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(mCurrentFile, false);
                        final int imageWidth = decoder.getWidth();
                        final int imageHeight = decoder.getHeight();
                        final int rowCount = ceil(imageHeight/(float)BLOCK_SIZE);
                        final int colCount = ceil(imageWidth/(float)BLOCK_SIZE);

                        mLoadSource = new LoadSource();
                        mLoadSource.decoder = decoder;
                        mLoadSource.imageWidth = imageWidth;
                        mLoadSource.imageHeight = imageHeight;
                        mLoadSource.blockRows = rowCount;
                        mLoadSource.blockCols = colCount;
                        mLoadSource.cachedImages = new Bitmap[rowCount][colCount];

                        mMainHandler.sendEmptyMessage(MSG_SIZE_READY);
                    } catch (IOException e) {
                        e.printStackTrace();
                        mMainHandler.sendEmptyMessage(MSG_LOAD_FAIL);
                    }
                }
                break;
            case MSG_DECODE_THUMBNAIL:
                Log.d("TAG", "start decode thumbnail");
                decodeThumbnail();
                break;
            case MSG_DECODE_BLOCK:
                final List<Integer> blocks = (List<Integer>) msg.obj;

                Log.d("TAG", "start decode, block size: " + blocks.size() + "time: " + SystemClock.uptimeMillis());

                boolean newBitmapDecoded = false;
                for (Integer position : blocks) {
                    if (decodeBlock(position)) {
                        newBitmapDecoded = true;
                    }
                    mLoadingBlocks.remove(position);
                }

                Log.d("TAG", "DecodeFinish: " + SystemClock.uptimeMillis());

                if (newBitmapDecoded) {
                    mMainHandler.sendEmptyMessage(MSG_BLOCK_READY);
                    cleanCache();
                }
                break;

            // running on main thread
            case MSG_SIZE_READY:
                if (mLoadListener != null) {
                    mLoadListener.onImageSizeReady(mLoadSource.imageWidth, mLoadSource.imageHeight);
                }
                break;
            case MSG_THUMBNAIL_READY:
                if (mLoadListener != null) {
                    mLoadListener.onDecodeBlockSuccess();
                }
                break;
            case MSG_BLOCK_READY:
                if (mLoadListener != null) {
                    Log.d("TAG", "MSG_BLOCK_READY: " + SystemClock.uptimeMillis());
                    mLoadListener.onDecodeBlockSuccess();
                }
                break;

        }
        return true;
    }

    private static int ceil(float f) {
        return Math.round(f+0.5f);
    }

    private static int floor(float f) {
        return Math.round(f-0.5f);
    }

    private static int round(float f) {
        return Math.round(f);
    }

    private class LoadSource {
        BitmapRegionDecoder decoder;
        int imageWidth;
        int imageHeight;

        int tSampleRate;
        Bitmap thumbImage;

        int blockRows;
        int blockCols;

        Bitmap[][] cachedImages;
    }

    public static class Position {
        public static int COL_MASK = (1 << 16)-1;

        public static int makePosition(int row, int col) {
            return (row << 16) | col;
        }

        public static int getRow(int position) {
            return position >> 16;
        }

        public static int getCol(int position) {
            return position & COL_MASK;
        }
    }
}
