package com.huangxueqin.rookieweibo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import butterknife.BindView;
import butterknife.Optional;

/**
 * Created by huangxueqin on 2017/2/22.
 */

public abstract class BaseActivity extends AppCompatActivity {

    @Nullable
    @BindView(R.id.back)
    View mBackButton;

    @Nullable
    @BindView(R.id.close)
    View mCloseButton;

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (mBackButton != null) {
            mBackButton.setOnClickListener(mToolbarActionListener);
        }
        if (mCloseButton != null) {
            mCloseButton.setOnClickListener(mToolbarActionListener);
        }
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
    }

    protected void onToolbarClosePress() {
        finish();
    }

    protected void onToolbarBackPress() {
        finish();
    }
}
