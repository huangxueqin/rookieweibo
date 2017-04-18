package com.huangxueqin.rookieweibo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.util.LruCache;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.huangxueqin.rookieweibo.cons.Cons;
import com.huangxueqin.rookieweibo.ui.widget.ImagePreviewer;
import com.huangxueqin.ultimateimageview.UltimateImageView;
import com.lsjwzh.widget.recyclerviewpager.RecyclerViewPager;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by huangxueqin on 2017/3/2.
 */

public class GalleryActivity extends BaseActivity implements ImagePreviewer.SnapDelegate {
    String[] mImageUrls;
    int mSelectedIndex;

    @BindView(R.id.image_list)
    RecyclerViewPager mImageList;

    LruCache<String, Bitmap> mImageCache;
    HashMap<ImageView, String> mLoadMap;
    HashMap<String, DecoderOptions> mDecoderOpts;
    int mMaxBitmapSize;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent bundle = getIntent();
        mImageUrls = bundle.getStringArrayExtra(Cons.IntentKey.IMAGE_LIST);
        mSelectedIndex = bundle.getIntExtra(Cons.IntentKey.SELECT_INDEX, 0);
        setContentView(R.layout.activity_gallery);
        ButterKnife.bind(this);
        mImageList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mImageList.setAdapter(new ImageAdapter());
        mImageList.scrollToPosition(mSelectedIndex);

        final int maxMemory = (int) Runtime.getRuntime().maxMemory()/1024;
        mImageCache = new LruCache<String, Bitmap>(maxMemory/8) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount() / 1024;
            }
        };
        mLoadMap = new HashMap<>();
        mDecoderOpts = new HashMap<>();

        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        mMaxBitmapSize = Math.max(metrics.widthPixels, metrics.heightPixels);
    }

    private static int computeSampling(final int srcSize, final int dstSize) {
        int sampling = 1;
        while (srcSize / (sampling+1) >= dstSize) {
            sampling += 1;
        }
        return sampling;
    }

    private void setImageUrl(final String url, final UltimateImageView imageView) {
        Glide.with(GalleryActivity.this)
                .load(url)
                .downloadOnly(new SimpleTarget<File>() {
                    @Override
                    public void onResourceReady(File resource, GlideAnimation<? super File> glideAnimation) {
                        imageView.setImage(resource);
                    }
                });
    }

    @Override
    public int getRawHeight(ImageView imageView) {
        final String url = mLoadMap.get(imageView);
        DecoderOptions ops = mDecoderOpts.get(url);
        return ops.rawHeight;
    }

    @Override
    public int getRawWidth(ImageView imageView) {
        final String url = mLoadMap.get(imageView);
        DecoderOptions ops = mDecoderOpts.get(url);
        return ops.rawWidth;
    }

    @Override
    public void offsetRegion(ImageView imageView, float dx, float dy) {
        final String url = mLoadMap.get(imageView);
        DecoderOptions ops = mDecoderOpts.get(url);
        ops.region.offset(0, (int) dy);
        Bitmap image = ops.decoder.decodeRegion(ops.region, ops.options);
        if (image != null) {
            imageView.setImageBitmap(image);
            mImageCache.put(url, image);
        }
    }

    private class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(GalleryActivity.this);
            View itemView = inflater.inflate(R.layout.view_page_image, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final String imageURL = mImageUrls[position];
            final boolean isGif = imageURL.endsWith(".gif");
            holder.gifView.setVisibility(isGif ? View.VISIBLE : View.GONE);
            holder.image.setVisibility(isGif ? View.GONE : View.VISIBLE);
            if (isGif) {
                Glide.with(GalleryActivity.this).load(imageURL).asGif().into(holder.gifView);
            } else {
                setImageUrl(imageURL, holder.image);
            }
        }

        @Override
        public int getItemCount() {
            return mImageUrls.length;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            UltimateImageView image;
            ImageView gifView;
            public ViewHolder(View itemView) {
                super(itemView);
                image = (UltimateImageView) itemView.findViewById(R.id.image);
                gifView = (ImageView) itemView.findViewById(R.id.gif_image);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        enterImmersiveMode();
    }

    @Override
    protected void onPause() {
        super.onPause();
        exitImmersiveMode();
    }



    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            enterImmersiveMode();
        }
    }

    private void enterImmersiveMode() {
        getWindow().getDecorView()
                .setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private void exitImmersiveMode() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    private static class DecoderOptions {
        final int rawWidth;
        final int rawHeight;
        BitmapRegionDecoder decoder;
        Rect region;
        BitmapFactory.Options options;

        public DecoderOptions(final int rawWidth, final int rawHeight) {
            this.rawWidth = rawWidth;
            this.rawHeight = rawHeight;
        }
    }
}
