package com.huangxueqin.test;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.huangxueqin.ultimateimageview.RwImageView;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final RwImageView imageView = (RwImageView) findViewById(R.id.image);
        Glide.with(this)
                .load("http://wx4.sinaimg.cn/mw1024/718878b5ly1fgcxdq8czhj20u08ss7wi.jpg")
//                .load("http://static.cnbetacdn.com/thumb/article/2017/0607/ee9dd19a2ef6346.jpg")
//                .into(imageView);
                .downloadOnly(new SimpleTarget<File>() {
                    @Override
                    public void onResourceReady(File resource, GlideAnimation<? super File> glideAnimation) {
                        imageView.setLargeImage(resource);
                    }
                });
    }
}
