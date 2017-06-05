package com.huangxueqin.rookieweibo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.huangxueqin.rookieweibo.auth.AccessTokenKeeper;
import com.huangxueqin.rookieweibo.auth.UserKeeper;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.openapi.models.User;

import butterknife.BindView;
import butterknife.ButterKnife;
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

    protected User mUser;
    protected Oauth2AccessToken mAccessToken;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUser = UserKeeper.readUser(this);
        mAccessToken = AccessTokenKeeper.readAccessToken(this);
    }

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
                    onToolbarButtonPress(v);
                    break;
            }
        }
    };

    protected void onToolbarButtonPress(View v) {
    }

    protected void onToolbarClosePress() {
        finish();
    }

    protected void onToolbarBackPress() {
        finish();
    }
}
