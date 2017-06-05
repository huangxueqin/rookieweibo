package com.huangxueqin.rookieweibo;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.huangxueqin.rookieweibo.auth.AuthConstants;
import com.huangxueqin.rookieweibo.common.KeyboardPanelActivity;
import com.huangxueqin.rookieweibo.cons.Cons;
import com.huangxueqin.rookieweibo.ui.emoji.EmoticonFragment;
import com.huangxueqin.rookieweibo.common.KeyboardObserver;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.legacy.StatusesAPI;
import com.sina.weibo.sdk.openapi.models.Status;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by huangxueqin on 2017/4/18.
 */

public class RepostActivity extends KeyboardPanelActivity {

    @BindView(R.id.content_editor)
    EditText mContentEditor;
    @BindView(R.id.emotion_picker)
    View mBtnEmotionPick;

    // toolbar views
    @BindView(R.id.toolbar)
    ViewGroup mToolbar;
    @BindView(R.id.menu_send)
    View mSendButton;

    Status mStatus;
    StatusesAPI mStatusesAPI;
    EmoticonFragment mEmojiFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        mSendButton.setOnClickListener(mToolbarActionListener);
        mBtnEmotionPick.setOnClickListener(mToolbarActionListener);

        final String statusStr = getIntent().getStringExtra(Cons.IntentKey.STATUS);
        mStatus = new Gson().fromJson(statusStr, Status.class);
        mStatusesAPI = new StatusesAPI(this, AuthConstants.APP_KEY, mAccessToken);

        initViews();
    }

    @Override
    protected int getContentViewId() {
        return R.id.input_view;
    }

    @Override
    protected int getPanelViewId() {
        return R.id.bottom_panel;
    }

    @Override
    protected int getRootViewId() {
        return R.id.root_view;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_repost;
    }

    @Override
    protected void onKeyboardShow(int keyboardHeight) {
        super.onKeyboardShow(keyboardHeight);
        if (isBottomPanelOpen()) {
            mBtnEmotionPick.setSelected(false);
        }
    }

    @Override
    protected void onKeyboardDismiss() {
        super.onKeyboardDismiss();
        if (isBottomPanelOpen()) {
            mBtnEmotionPick.setSelected(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showKeyboard(mContentEditor);
                mContentEditor.setSelection(0);
            }
        });
    }

    private void initViews() {
        if (mStatus.retweeted_status != null) {
            final String initContent = "//@" + mStatus.user.screen_name + ":" + mStatus.text;
            mContentEditor.setText(initContent);
            mContentEditor.setSelection(0);
        }
    }

    @Override
    protected void onToolbarButtonPress(View v) {
        super.onToolbarButtonPress(v);
        switch (v.getId()) {
            case R.id.menu_send:
                doRepost();
                break;
            case R.id.emotion_picker:
                toggleEmotionPicker(!mBtnEmotionPick.isSelected());
                break;
        }
    }

    private void toggleEmotionPicker(boolean open) {
        mBtnEmotionPick.setSelected(open);
        if (open) {
            if (mEmojiFragment == null) {
                mEmojiFragment = EmoticonFragment.newInstance();
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.bottom_panel, mEmojiFragment)
                        .commit();
            }
            openBottomPanel(mContentEditor);
        } else {
            closeBottomPanel();
        }
    }

    private void doRepost() {
        final long statusId = Long.parseLong(mStatus.id);
        final String statusText = mContentEditor.getText().toString();

        mStatusesAPI.repost(statusId,
                TextUtils.isEmpty(statusText) ? "转发微博" : statusText,
                0,
                new RequestListener() {
                    @Override
                    public void onComplete(String s) {
                        setResult(RESULT_OK);
                        RepostActivity.this.finish();
                    }

                    @Override
                    public void onWeiboException(WeiboException e) {
                        Toast.makeText(RepostActivity.this, "转发失败，请重试", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
