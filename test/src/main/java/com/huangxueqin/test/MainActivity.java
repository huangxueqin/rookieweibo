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
                .load("http://wx2.sinaimg.cn/mw690/718878b5ly1fgcpj1fchmj20u08qdqv6.jpg")
                .downloadOnly(new SimpleTarget<File>() {
                    @Override
                    public void onResourceReady(File resource, GlideAnimation<? super File> glideAnimation) {
                        imageView.setLargeImage(resource);
                    }
                });
    }
}
