package com.huangxueqin.test;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.huangxueqin.ultimateimageview.RwImageView;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final RwImageView imageView = (RwImageView) findViewById(R.id.image);
        final ImageView imageView2 = (ImageView) findViewById(R.id.imageView2);
//        imageView.setVisibility(View.GONE);
        imageView2.setVisibility(View.GONE);
        Glide.with(this)
//                .load("http://static.cnbetacdn.com/thumb/article/2017/0607/ee9dd19a2ef6346.jpg")
                .load("http://wx4.sinaimg.cn/mw1024/718878b5ly1fgcxdq8czhj20u08ss7wi.jpg")
                .override(540, 80)
                .listener(mGlideRequestListener)
                .into(imageView);
//                .load("http://static.cnbetacdn.com/thumb/article/2017/0607/ee9dd19a2ef6346.jpg")
//                .into(imageView);
//                .downloadOnly(new SimpleTarget<File>() {
//                    @Override
//                    public void onResourceReady(File resource, GlideAnimation<? super File> glideAnimation) {
////                        imageView.setLargeImage(resource);
//                        imageView.setImageURI(Uri.fromFile(resource));
//                    }
//                });
    }

    private RequestListener<String, GlideDrawable> mGlideRequestListener = new RequestListener<String, GlideDrawable>() {
        @Override
        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
            return false;
        }

        @Override
        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
            int drawableWidth = resource.getIntrinsicWidth();
            int drawableHeight = resource.getIntrinsicHeight();
            Log.d("TAG", "drawable size: " + drawableWidth + ", " + drawableHeight);
            return false;
        }
    };
}
