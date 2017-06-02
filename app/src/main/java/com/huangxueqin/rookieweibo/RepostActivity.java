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

public class RepostActivity extends BaseActivity implements KeyboardObserver.KeyboardStateListener {

    @BindView(R.id.root_view)
    ViewGroup mContentView;

    @BindView(R.id.input_view)
    ViewGroup mInputView;
    @BindView(R.id.content_editor)
    EditText mContentEditor;
    @BindView(R.id.emotion_picker)
    View mBtnEmotionPick;

    @BindView(R.id.bottom_panel)
    ViewGroup mBottomPanel;

    // toolbar views
    @BindView(R.id.toolbar)
    ViewGroup mToolbar;
    @BindView(R.id.menu_send)
    View mSendButton;

    Status mStatus;
    StatusesAPI mStatusesAPI;
    int mTextCount = 0;

    // used for handle toggling bottom panel
    int mBottomPanelHeight;

    boolean mIsKeyboardShowing;
    boolean mIsBottomPanelShowing;

    EmoticonFragment mEmojiFragment;

    KeyboardObserver mKeyboardObserver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repost);
        ButterKnife.bind(this);

        mSendButton.setOnClickListener(mToolbarActionListener);
        mBtnEmotionPick.setOnClickListener(mToolbarActionListener);

        final String statusStr = getIntent().getStringExtra(Cons.IntentKey.STATUS);
        mStatus = new Gson().fromJson(statusStr, Status.class);
        mStatusesAPI = new StatusesAPI(this, AuthConstants.APP_KEY, mAccessToken);

        initViews();

        mKeyboardObserver = new KeyboardObserver(mContentView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mKeyboardObserver.setKeyboardStateListener(this);
        mKeyboardObserver.startObserve();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mKeyboardObserver.stopObserve();
        mKeyboardObserver.setKeyboardStateListener(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showKeyboard(InputMethodManager.SHOW_IMPLICIT);
                mContentEditor.setSelection(0);
            }
        });
    }

    private void initViews() {
        if (mStatus.retweeted_status != null) {
            final String initContent = "//@" + mStatus.user.screen_name + ":" + mStatus.text;
            mTextCount = initContent.length();
            mContentEditor.setText(initContent);
            mContentEditor.setSelection(0);
        }

        mBottomPanel.setVisibility(View.GONE);
        mBottomPanelHeight = 600;
    }

    @Override
    public void onKeyboardShow(int keyboardHeight) {
        mIsKeyboardShowing = true;
        mBottomPanelHeight = keyboardHeight;
        adjustInputViewHeight(mContentView.getHeight()-mInputView.getTop());
    }

    @Override
    public void onKeyboardDismiss() {
        mIsKeyboardShowing = false;
        if (!mIsBottomPanelShowing) {
            restoreInputViewHeight();
        }
    }

    private void adjustInputViewHeight(int height) {
        ViewGroup.LayoutParams lp = mInputView.getLayoutParams();
        lp.height = height;
        mInputView.setLayoutParams(lp);
    }

    private void restoreInputViewHeight() {
        ViewGroup.LayoutParams lp = mInputView.getLayoutParams();
        lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
        mInputView.setLayoutParams(lp);
    }

    private void showKeyboard(final int flags) {
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            mContentEditor.requestFocus();
            imm.showSoftInput(mContentEditor, flags);
        }
    }

    private void hideKeyboard() {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(mContentEditor.getWindowToken(), 0);
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
        if (!open) {
            toggleBottomPanel(false);
            showKeyboard(InputMethodManager.SHOW_IMPLICIT);
        } else {
            toggleBottomPanel(true);
            // show emotion picker
            if (mEmojiFragment == null) {
                mEmojiFragment = EmoticonFragment.newInstance();
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.bottom_panel, mEmojiFragment)
                        .commit();
            }
        }
    }

    private void toggleBottomPanel(boolean open) {
        if (mIsBottomPanelShowing == open) {
            return;
        }
        mIsBottomPanelShowing = open;
        if (open) {
            if (mIsKeyboardShowing) {
                hideKeyboard();
            } else {
                adjustInputViewHeight(mContentView.getHeight()-mInputView.getTop()-mBottomPanelHeight);
            }
        }
        mBottomPanel.setVisibility(open ? View.VISIBLE : View.INVISIBLE);
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
