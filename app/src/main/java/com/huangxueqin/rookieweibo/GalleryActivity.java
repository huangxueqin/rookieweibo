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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.huangxueqin.rookieweibo.common.ImageUtils;
import com.huangxueqin.rookieweibo.common.Logger;
import com.huangxueqin.rookieweibo.common.Size;
import com.huangxueqin.rookieweibo.cons.Cons;
import com.huangxueqin.ultimateimageview.RwImageView;
import com.huangxueqin.ultimateimageview.UltimateImageView;
import com.lsjwzh.widget.recyclerviewpager.RecyclerViewPager;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by huangxueqin on 2017/3/2.
 */

public class GalleryActivity extends BaseActivity {
    private static final String TAG = "GalleryActivity";

    String[] mImageUrls;

    @BindView(R.id.image_list)
    RecyclerViewPager mImageList;

    private Map<ImageView, String> mDisplayMap = new ConcurrentHashMap<>();
    private Map<String, Size> mImageSizeMap = new ConcurrentHashMap<>();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent bundle = getIntent();
        mImageUrls = bundle.getStringArrayExtra(Cons.IntentKey.IMAGE_LIST);
        setContentView(R.layout.activity_gallery);
        ButterKnife.bind(this);

        mImageList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mImageList.setAdapter(new ImageAdapter());
        mImageList.scrollToPosition(bundle.getIntExtra(Cons.IntentKey.SELECT_INDEX, 0));
    }

    private class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(GalleryActivity.this);
            View itemView = inflater.inflate(R.layout.view_page_image, parent, false);
            return new ViewHolder(itemView);
        }

        private void showGif(RwImageView imageView, String url) {
            Glide.with(GalleryActivity.this)
                    .load(url)
                    .asGif()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(imageView);
        }

        private void showStaticImage(final RwImageView imageView, final String url) {
            Glide.with(GalleryActivity.this)
                    .load(url)
                    .downloadOnly(new SimpleTarget<File>() {
                        @Override
                        public void onResourceReady(File resource, GlideAnimation<? super File> glideAnimation) {
                            Size imageSize = mImageSizeMap.get(url);
                            if (imageSize == null) {
                                Logger.d(TAG, "decode image size from disk...");
                                BitmapFactory.Options op = new BitmapFactory.Options();
                                op.inJustDecodeBounds = true;
                                BitmapFactory.decodeFile(resource.getAbsolutePath(), op);
                                imageSize = new Size(op.outWidth, op.outHeight);
                                mImageSizeMap.put(url, imageSize);
                            }
                            Logger.d(TAG, "image: " + url + ", width = " + imageSize.width + ", height = " + imageSize.height);
                            if (url.equals(mDisplayMap.get(imageView))) {
                                final int imgW = imageSize.width;
                                final int imgH = imageSize.height;
                                if (ImageUtils.isLongImage(imgW, imgH)) {
                                    imageView.setLargeImage(resource, imgW, imgH);
                                } else {
                                    Glide.with(GalleryActivity.this).load(url).into(imageView);
                                }
                            }
                        }
                    });
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final String imageURL = mImageUrls[position];
            final RwImageView imageView = holder.image;
            mDisplayMap.put(imageView, imageURL);
            if (imageURL.endsWith(".gif")) {
                Logger.d(TAG, "position " + position + " is a gif");
                showGif(imageView, imageURL);
            } else {
                Logger.d(TAG, "position " + position + " is a static image");
                showStaticImage(imageView, imageURL);
            }

//            Glide.with(GalleryActivity.this).load(imageURL)
//                    .downloadOnly(new SimpleTarget<File>() {
//                        @Override
//                        public void onResourceReady(File resource, GlideAnimation<? super File> glideAnimation) {
//                            holder.image.setImage(resource);
//                        }
//                    });
        }

        @Override
        public int getItemCount() {
            return mImageUrls.length;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            RwImageView image;
//            UltimateImageView image;
            public ViewHolder(View itemView) {
                super(itemView);
                image = (RwImageView) itemView.findViewById(R.id.image);
//                image = (UltimateImageView) itemView.findViewById(R.id.image);
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
    protected void onToolbarClosePress() {
        super.onToolbarClosePress();
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);
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
//                                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
//                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                );
    }

    private void exitImmersiveMode() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
}
