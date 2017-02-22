package com.huangxueqin.rookieweibo.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.BoolRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.github.ybq.android.spinkit.SpinKitView;
import com.huangxueqin.rookieweibo.BaseActivity;
import com.huangxueqin.rookieweibo.MainActivity;
import com.huangxueqin.rookieweibo.R;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.UsersAPI;
import com.sina.weibo.sdk.openapi.models.User;
import com.sina.weibo.sdk.widget.LoginButton;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by huangxueqin on 2017/2/22.
 */

public class LoginActivity extends BaseActivity {

    @BindView(R.id.close) View mToolbarClose;
    @BindView(R.id.login_button) LoginButton mLoginButton;
    @BindView(R.id.dim_view) View mDimVim;

    Oauth2AccessToken mAccessToken;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        mToolbarClose.setOnClickListener(mToolbarActionListener);
        mLoginButton.setWeiboAuthInfo(new AuthInfo(this, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE)
                , mAuthListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAccessToken != null) {
            mDimVim.setVisibility(View.VISIBLE);
            mLoginButton.setVisibility(View.GONE);
            requestUserAsync();
        }
    }

    private void requestUserAsync() {
        UsersAPI usersAPI = new UsersAPI(this, Constants.APP_KEY, mAccessToken);
        long uid = Long.parseLong(mAccessToken.getUid());
        usersAPI.show(uid, new RequestListener() {
            @Override
            public void onComplete(String s) {
                UserKeeper.writeUser(LoginActivity.this, s);
                startMainActivity();
            }

            @Override
            public void onWeiboException(WeiboException e) {
                mAccessToken = null;
                AccessTokenKeeper.clear(LoginActivity.this);
                mLoginButton.setVisibility(View.VISIBLE);
                mDimVim.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "获取用户信息失败,请重试", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startMainActivity() {
        Intent main = new Intent(LoginActivity.this, MainActivity.class);
        main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(main);
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);
    }

    WeiboAuthListener mAuthListener = new WeiboAuthListener() {
        @Override
        public void onComplete(Bundle bundle) {
            Oauth2AccessToken accessToken = Oauth2AccessToken.parseAccessToken(bundle);
            if (accessToken.isSessionValid()) {
                mAccessToken = accessToken;
                AccessTokenKeeper.writeAccessToken(LoginActivity.this, accessToken);
            }
        }

        @Override
        public void onWeiboException(WeiboException e) {
        }

        @Override
        public void onCancel() {
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mLoginButton != null) {
            mLoginButton.onActivityResult(resultCode, resultCode, data);
        }
    }
}
