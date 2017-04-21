package com.huangxueqin.rookieweibo;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.huangxueqin.rookieweibo.auth.AuthConstants;
import com.huangxueqin.rookieweibo.cons.Cons;
import com.huangxueqin.rookieweibo.ui.emoji.EmojiFragment;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.legacy.StatusesAPI;
import com.sina.weibo.sdk.openapi.models.Status;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by huangxueqin on 2017/4/18.
 */

public class RepostActivity extends BaseActivity {
    private static final int EMOTION_PANEL = 0x100;

    @BindView(R.id.root_view)
    ViewGroup mContentView;

    @BindView(R.id.input_view)
    ViewGroup mInputView;
    @BindView(R.id.editor)
    EditText mContentEditor;
    @BindView(R.id.show_emotion)
    View mEmotionPickerButton;

    @BindView(R.id.bottom_panel)
    ViewGroup mBottomPanel;

    @BindView(R.id.toolbar)
    ViewGroup mToolbar;
    @BindView(R.id.menu_send)
    View mSendButton;

    Status mStatus;
    StatusesAPI mStatusesAPI;
    int mTextCount = 0;

    // used for handle toggling bottom panel
    int mWindowVisibleBottom = -1;
    int mInputViewTop;
    int mBottomPanelHeight;

    boolean mKeyboardVisible;
    boolean mBottomPanelVisible;

    EmojiFragment mEmojiFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repost);
        ButterKnife.bind(this);
        mSendButton.setOnClickListener(mToolbarActionListener);
        mEmotionPickerButton.setOnClickListener(mToolbarActionListener);

        final String statusStr = getIntent().getStringExtra(Cons.IntentKey.STATUS);
        mStatus = new Gson().fromJson(statusStr, Status.class);
        mStatusesAPI = new StatusesAPI(this, AuthConstants.APP_KEY, mAccessToken);

        initViews();

        mContentView.getViewTreeObserver().addOnGlobalLayoutListener(mKeyboardObserver);
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

    private ViewTreeObserver.OnGlobalLayoutListener mKeyboardObserver = new ViewTreeObserver.OnGlobalLayoutListener() {

        private Rect rect = new Rect();

        @Override
        public void onGlobalLayout() {
            mContentView.getWindowVisibleDisplayFrame(rect);
            final int curVisibleBottom = rect.bottom;
            final int oldVisibleBottom = mWindowVisibleBottom;

            if (curVisibleBottom == oldVisibleBottom) {
                return;
            }

            if (mWindowVisibleBottom == -1) {
                mWindowVisibleBottom = curVisibleBottom;
                mInputViewTop = mInputView.getTop();
                return;
            }

            mKeyboardVisible = curVisibleBottom < oldVisibleBottom;
            if (curVisibleBottom < oldVisibleBottom) {
                mBottomPanelHeight = oldVisibleBottom - curVisibleBottom;
                toggleEmotionPicker(false);
                adjustInputViewHeight();
            } else {
                toggleEmotionPicker(true);
            }
            mWindowVisibleBottom = curVisibleBottom;
        }
    };

    private void adjustInputViewHeight() {
        final ViewGroup.LayoutParams lp = mInputView.getLayoutParams();
        lp.height = mContentView.getHeight()-mInputViewTop;
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
            case R.id.show_emotion:
                toggleEmotionPicker(!mEmotionPickerButton.isSelected());
                break;
        }
    }

    private void toggleEmotionPicker(boolean open) {
        mEmotionPickerButton.setSelected(open);
        if (!open) {
            toggleBottomPanel(false);
            showKeyboard(InputMethodManager.SHOW_IMPLICIT);
        } else {
            toggleBottomPanel(true);
            // show emotion picker
            if (mEmojiFragment == null) {
                mEmojiFragment = new EmojiFragment();
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.bottom_panel, mEmojiFragment)
                        .commit();
            }
        }
    }

    private void toggleBottomPanel(boolean open) {
        if (mBottomPanelVisible == open) {
            return;
        }
        mBottomPanelVisible = open;
        if (open) {
            if (mKeyboardVisible) {
                hideKeyboard();
            } else {
                adjustInputViewBottomForBottomPanel(true);
            }
        }
        mBottomPanel.setVisibility(open ? View.VISIBLE : View.GONE);
    }

    private void adjustInputViewBottomForBottomPanel(boolean open) {
        final ViewGroup.LayoutParams lp = mInputView.getLayoutParams();
        lp.height = mContentView.getHeight() - mInputViewTop - (open ? mBottomPanelHeight : 0);
        mInputView.setLayoutParams(lp);
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
