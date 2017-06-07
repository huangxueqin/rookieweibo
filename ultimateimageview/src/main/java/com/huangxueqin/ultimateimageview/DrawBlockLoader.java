package com.huangxueqin.ultimateimageview;

import android.content.Context;
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

import com.huangxueqin.ultimateimageview.factory.ImageBlockSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by huangxueqin on 2017/4/6.
 */

public class DrawBlockLoader implements Handler.Callback {
    private static final int INVALID_SAMPLE_RATE = 0;

    // used in decode thread
    private static final int MSG_DECODE_INFO = 1;
    private static final int MSG_DECODE_BLOCK = 3;

    // used in main thread
    private static final int MSG_LOAD_FAIL = 11;
    private static final int MSG_SIZE_READY = 12;
    private static final int MSG_BLOCK_READY = 14;

    private static HandlerThread sBlockLoaderThread;
    static {
        sBlockLoaderThread = new HandlerThread("block-loader-thread");
        sBlockLoaderThread.start();
    }

    private Context mContext;
    private ImageBlockTarget mTarget;
    private ImageBlockSource mSource;
    private final int BLOCK_SIZE;
    private final int CACHE_PIXELS;

    private Handler mLoadHandler;
    private Handler mMainHandler;

    private int mImageWidth;
    private int mImageHeight;
    private volatile BitmapRegionDecoder mDecoder;

    private volatile Rect mCachedDrawRect;
    private volatile BlockCache mCache;
    private volatile BlockCache mBackupCache;

    public DrawBlockLoader(Context context,
                           ImageBlockTarget target,
                           ImageBlockSource source,
                           final int blockSize,
                           final int cachePixels) {
        mContext = context;
        mTarget = target;
        mSource = source;
        BLOCK_SIZE = blockSize;
        CACHE_PIXELS = cachePixels;

        mMainHandler = new Handler(Looper.getMainLooper(), this);
        mLoadHandler = new Handler(sBlockLoaderThread.getLooper(), this);

        mCachedDrawRect = new Rect();
        mCache = new BlockCache();
        mBackupCache = new BlockCache();
    }

    private int findNearestSampleRate(float sampleRateF) {
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

    private void updateCachedDrawRect(Rect imageRect) {
        final int left = Math.max(0, imageRect.left-CACHE_PIXELS);
        final int top = Math.max(0, imageRect.top-CACHE_PIXELS);
        final int right = Math.min(mImageWidth, imageRect.right+CACHE_PIXELS);
        final int bottom = Math.min(mImageHeight, imageRect.bottom+CACHE_PIXELS);
        mCachedDrawRect.set(left, top, right, bottom);
    }

    private void updateCache(final int sr) {
        if (sr == mCache.sr) { return; }

        if (mCache.sr == INVALID_SAMPLE_RATE) {
            mCache.sr = sr;
        } else {
            if (mCache.size() > 0 || sr == mBackupCache.sr) {
                BlockCache temp = mBackupCache;
                mBackupCache = mCache;
                mCache = temp;
            }
            if (sr != mCache.sr) {
                mCache.clean();
                mCache.sr = sr;
            }
        }
    }

    public List<DrawBlock> getDrawData(float sampleRate, Rect imageRect) {
        if (mDecoder == null) {
            mLoadHandler.sendEmptyMessage(MSG_DECODE_INFO);
            return Collections.EMPTY_LIST;
        }

        updateCachedDrawRect(imageRect);
        updateCache(findNearestSampleRate(sampleRate));

        final List<DrawBlock> drawBlocks = new ArrayList<>();
        final List<Integer> missingBlocks = new LinkedList<>();
        final int rowStart = imageRect.top/BLOCK_SIZE;
        final int rowEnd = imageRect.bottom/BLOCK_SIZE + (imageRect.bottom%BLOCK_SIZE != 0 ? 1 : 0);
        final int colStart = imageRect.left/BLOCK_SIZE;
        final int colEnd = imageRect.right/BLOCK_SIZE + (imageRect.right%BLOCK_SIZE != 0 ? 1 : 0);

        for (int i = rowStart; i< rowEnd; i++) {
            for (int j = colStart; j < colEnd; j++) {
                final int position = PosUtil.makePos(i, j);
                Bitmap image = mCache.get(position);
                float sr = mCache.sr;
                if (image == null) {
                    missingBlocks.add(position);
                    image = mBackupCache.get(position);
                    sr = mBackupCache.sr;
                }

                if (image != null) {
                    final int left = j * BLOCK_SIZE;
                    final int top = i * BLOCK_SIZE;
                    final int width = Math.min(BLOCK_SIZE, mImageWidth-left);
                    final int height = Math.min(BLOCK_SIZE, mImageHeight-top);

                    Rect srcRect = new Rect(0, 0, round(width/sr), round(height/sr));
                    Rect imgRect = new Rect(left, top, left+width, top+height);
                    drawBlocks.add(new DrawBlock(image, srcRect, imgRect));
                }
            }
        }

        loadMissingBlocks(missingBlocks);

        // load extra caches
        final List<Integer> missingCachedBlocks = new LinkedList<>();
        final int cachedRowStart = mCachedDrawRect.top/BLOCK_SIZE;
        final int cachedRowEnd = mCachedDrawRect.bottom/BLOCK_SIZE + (mCachedDrawRect.bottom%BLOCK_SIZE != 0 ? 1 : 0);
        final int cachedColStart = mCachedDrawRect.left/BLOCK_SIZE;
        final int cachedColEnd = mCachedDrawRect.right/BLOCK_SIZE + (mCachedDrawRect.right%BLOCK_SIZE != 0 ? 1 : 0);;

        for (int i = cachedRowStart; i < rowStart; i++) {
            for(int j=cachedColStart; j < cachedColEnd; j++) {
                final int position = PosUtil.makePos(i, j);
                if (mCache.get(position) == null) {
                    missingCachedBlocks.add(position);
                }
            }
        }

        for (int i = rowStart; i < rowEnd; i++) {
            for (int j = cachedColStart; j < colStart; j++) {
                final int position = PosUtil.makePos(i, j);
                if (mCache.get(position) == null) {
                    missingCachedBlocks.add(position);
                }
            }
        }

        for (int i = rowEnd; i < cachedRowEnd; i++) {
            for (int j = cachedColStart; j < cachedColEnd; j++) {
                final int position = PosUtil.makePos(i, j);
                if (mCache.get(position) == null) {
                    missingCachedBlocks.add(position);
                }
            }
        }

        for (int i = rowStart; i < rowEnd; i++) {
            for (int j = colEnd; j < cachedColEnd; j++) {
                final int position = PosUtil.makePos(i, j);
                if (mCache.get(position) == null) {
                    missingCachedBlocks.add(position);
                }
            }
        }

        loadMissingBlocks(missingCachedBlocks);

        return drawBlocks;
    }

    private void loadMissingBlocks(final List<Integer> missingBlocks) {
        for (Integer position : missingBlocks) {
            Log.d("TAG", "col = " + PosUtil.getCol(position) + ", row = " + PosUtil.getRow(position));
        }
        if (missingBlocks.size() > 0) {
            mLoadHandler.obtainMessage(MSG_DECODE_BLOCK, mCache.sr, -1, missingBlocks).sendToTarget();
        }
    }

    private Rect mTempDecodeRegion = new Rect();
    private Rect getDecodeRegion(final int row, final int col) {
        mTempDecodeRegion.set(
                col * BLOCK_SIZE,
                row * BLOCK_SIZE,
                Math.min((col+1)*BLOCK_SIZE, mImageWidth),
                Math.min((row+1)*BLOCK_SIZE, mImageHeight));
        return mTempDecodeRegion;
    }

    private boolean decodeBlock(int position, final int sampleRate) {
        if (mDecoder == null) {
            return false;
        }

        if (mCache.get(position) != null && mCache.sr == sampleRate) {
            return false;
        }

        final int row = PosUtil.getRow(position);
        final int col = PosUtil.getCol(position);

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleRate;
        Bitmap bitmap = mDecoder.decodeRegion(getDecodeRegion(row, col), options);

        if (bitmap != null && mCache.sr == sampleRate) {
            mCache.put(position, bitmap);
            mBackupCache.remove(position);
            return true;
        }

        return false;
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            // running on load thread
            case MSG_DECODE_INFO:
                mDecoder = mSource.make();
                if (mDecoder == null) {
                    mMainHandler.sendEmptyMessage(MSG_LOAD_FAIL);
                    break;
                }
                mImageWidth = mDecoder.getWidth();
                mImageHeight = mDecoder.getHeight();
                mMainHandler.sendEmptyMessage(MSG_SIZE_READY);
                break;
            case MSG_DECODE_BLOCK:
                final int sr = msg.arg1;
                if (sr != mCache.sr) {
                    break;
                }
                final List<Integer> blocks = (List<Integer>) msg.obj;

                Log.d("TAG", "start decode, block size: " + blocks.size() + "time: " + SystemClock.uptimeMillis());

                boolean newBitmapDecoded = false;
                for (int i = 0; i < blocks.size() && mCache.sr == sr; i++) {
                    if (decodeBlock(blocks.get(i), sr)) {
                        newBitmapDecoded = true;
                    }
                }

                Log.d("TAG", "DecodeFinish: " + SystemClock.uptimeMillis());

                if (newBitmapDecoded) {
                    mCache.trim(mCachedDrawRect);
                    if (sr == mCache.sr) {
                        mMainHandler.sendEmptyMessage(MSG_BLOCK_READY);
                    }
                }
                break;

            // running on main thread
            case MSG_SIZE_READY:
                mTarget.onImageSizeReady(mImageWidth, mImageHeight);
                break;
            case MSG_BLOCK_READY:
                Log.d("TAG", "MSG_BLOCK_READY: " + SystemClock.uptimeMillis());
                mTarget.onDecodeBlockSuccess();
                break;
        }
        return true;
    }

    public void finalize() {
        Log.d("TAG", "finalized()");
        mLoadHandler.removeCallbacksAndMessages(null);
        if (mDecoder != null) {
            mDecoder.recycle();
            mDecoder = null;
        }

        mCache.clean();
        mBackupCache.clean();
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

    private class BlockCache {
        int sr;
        ConcurrentHashMap<Integer, Bitmap> cache;

        public BlockCache() {
            sr = INVALID_SAMPLE_RATE;
            cache = new ConcurrentHashMap<>();
        }

        public Bitmap get(final Integer pos) {
            return cache.get(pos);
        }

        public void put(final Integer pos, final Bitmap b) {
            cache.put(pos, b);
        }

        public Bitmap remove(final Integer pos) {
            return cache.remove(pos);
        }

        public int size() {
            return cache.size();
        }

        public void clean() {
            cache.clear();
        }

        public void trim(Rect r) {
            for (Iterator<Integer> iterator = cache.keySet().iterator(); iterator.hasNext(); ) {
                final int position = iterator.next();
                final int row = PosUtil.getRow(position);
                final int col = PosUtil.getCol(position);
                if (!getDecodeRegion(row, col).intersect(r)) {
                    iterator.remove();
                }
            }
        }
    }

    private static class PosUtil {
        public static int COL_MASK = (1<<16)-1;

        public static int makePos(int row, int col) {
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
