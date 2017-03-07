package com.huangxueqin.rookieweibo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.huangxueqin.rookieweibo.cons.Cons;
import com.huangxueqin.rookieweibo.widget.StatusTextView;
import com.sina.weibo.sdk.openapi.models.Status;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by huangxueqin on 2017/3/3.
 */

public class WeiboActivity extends BaseActivity {

    @BindView(R.id.user_avatar)
    ImageView mUserAvatar;
    @BindView(R.id.status_text)
    StatusTextView mStatusText;

    private Status mStatus;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weibo);
        ButterKnife.bind(this);
        String statusStr = getIntent().getStringExtra(Cons.IntentKey.STATUS);
        mStatus = new Gson().fromJson(statusStr, Status.class);

        Glide.with(this).load(mStatus.user.avatar_hd).into(mUserAvatar);
        mStatusText.setText(mStatus.text);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }
}
