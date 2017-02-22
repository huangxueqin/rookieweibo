package com.huangxueqin.rookieweibo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by huangxueqin on 2017/2/22.
 */

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected View.OnClickListener mToolbarActionListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.back:
                    onToolbarBackPress();
                    break;
                case R.id.close:
                    onToolbarClosePress();
                    break;
                default:
                    onToolbarButtonPress();
                    break;
            }
        }
    };

    protected void onToolbarButtonPress() {
        finish();
    }

    protected void onToolbarClosePress() {
        finish();
    }

    protected void onToolbarBackPress() {

    }
}
