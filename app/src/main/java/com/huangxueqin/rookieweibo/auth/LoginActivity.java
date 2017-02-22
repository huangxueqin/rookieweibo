package com.huangxueqin.rookieweibo.auth;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.BoolRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.huangxueqin.rookieweibo.BaseActivity;
import com.huangxueqin.rookieweibo.MainActivity;
import com.huangxueqin.rookieweibo.R;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.widget.LoginButton;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by huangxueqin on 2017/2/22.
 */

public class LoginActivity extends BaseActivity {

    @BindView(R.id.close) View mToolbarClose;
    @BindView(R.id.login_button) LoginButton mLoginButton;

    Boolean mAccessTokenGot = false;

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
        if (mAccessTokenGot) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                    Intent main = new Intent(LoginActivity.this, MainActivity.class);
                    main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(main);
                    overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);
                }
            });
        }
    }

    WeiboAuthListener mAuthListener = new WeiboAuthListener() {
        @Override
        public void onComplete(Bundle bundle) {
            Oauth2AccessToken accessToken = Oauth2AccessToken.parseAccessToken(bundle);
            if (accessToken.isSessionValid()) {
                mAccessTokenGot = true;
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
