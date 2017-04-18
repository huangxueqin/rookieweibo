package com.huangxueqin.rookieweibo;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.huangxueqin.rookieweibo.cons.Cons;
import com.sina.weibo.sdk.openapi.models.Status;

/**
 * Created by huangxueqin on 2017/4/18.
 */

public class RepostActivity extends BaseActivity {

    Status mStatus;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String statusStr = getIntent().getStringExtra(Cons.IntentKey.STATUS);
        mStatus = new Gson().fromJson(statusStr, Status.class);


    }
}
