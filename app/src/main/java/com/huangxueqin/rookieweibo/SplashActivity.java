package com.huangxueqin.rookieweibo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.huangxueqin.rookieweibo.auth.AccessTokenKeeper;
import com.huangxueqin.rookieweibo.auth.LoginActivity;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;

/**
 * Created by huangxueqin on 2017/2/22.
 */

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            final Context context = SplashActivity.this;
            @Override
            public void run() {
                Intent targetIntent;
                Oauth2AccessToken token = AccessTokenKeeper.readAccessToken(context);
                if (TextUtils.isEmpty(token.getToken())) {
                    targetIntent = new Intent(context, LoginActivity.class);
                } else {
                    targetIntent = new Intent(context, MainActivity.class);
                }
                targetIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(targetIntent);
                overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);
            }
        }, 1500);
    }


}
