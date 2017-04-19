package com.huangxueqin.rookieweibo;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.huangxueqin.rookieweibo.auth.AuthConstants;
import com.huangxueqin.rookieweibo.cons.Cons;
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
    private static final int PANEL_CLOSE = 0x100;
    private static final int PANEL_EMOTION = 0x101;

    @BindView(R.id.root_view)
    ViewGroup mContentView;

    @BindView(R.id.input_view)
    ViewGroup mInputView;
    @BindView(R.id.editor)
    EditText mContentEditor;
    @BindView(R.id.show_emotion)
    View mShowEmotionButton;

    @BindView(R.id.bottom_panel)
    ViewGroup mBottomPanel;

    @BindView(R.id.toolbar)
    ViewGroup mToolbar;
    @BindView(R.id.menu_send)
    View mSendButton;

    Status mStatus;
    StatusesAPI mStatusesAPI;
    int mTextCount = 0;
    int mCurrentPanelType = PANEL_CLOSE;

    int mLastContentBottom = -1;
    Rect tempRect = new Rect();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repost);
        ButterKnife.bind(this);
        mSendButton.setOnClickListener(mToolbarActionListener);
        mShowEmotionButton.setOnClickListener(mToolbarActionListener);

        final String statusStr = getIntent().getStringExtra(Cons.IntentKey.STATUS);
        mStatus = new Gson().fromJson(statusStr, Status.class);
        mStatusesAPI = new StatusesAPI(this, AuthConstants.APP_KEY, mAccessToken);

        initViews();

        observeKeyboardPop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showKeyboard(InputMethodManager.SHOW_IMPLICIT);
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
    }

    // register keyboard show/hide listener
    private void observeKeyboardPop() {
        mContentView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mContentView.getWindowVisibleDisplayFrame(tempRect);
                if (mLastContentBottom == -1) {
                    mLastContentBottom = tempRect.bottom;
                } else {
                    if (mLastContentBottom != tempRect.bottom) {
                        mLastContentBottom = tempRect.bottom;
                        if (mCurrentPanelType == PANEL_CLOSE) {
                            final int height = mContentView.getHeight() - mToolbar.getHeight();
                            Log.d("TAG", "height = " + height);
                            ViewGroup.LayoutParams params = mInputView.getLayoutParams();
                            params.height = height;
                            mInputView.setLayoutParams(params);
                        }
                    }
                }
            }
        });
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
        final int id = v.getId();
        if (id == R.id.menu_send) {
            doRepost();
        } else if (id == R.id.show_emotion) {
            if (mCurrentPanelType == PANEL_EMOTION) {
                // hide panel
                mShowEmotionButton.setSelected(false);
                hideBottomPanel();
            } else {
                mShowEmotionButton.setSelected(true);
                showBottomPanel(PANEL_EMOTION);
            }
        }
    }

    private void hideBottomPanel() {
        mCurrentPanelType = PANEL_CLOSE;
        mBottomPanel.setVisibility(View.GONE);
    }

    private void showBottomPanel(int panelType) {
        final int oldPanelType = mCurrentPanelType;
        mCurrentPanelType = panelType;
        if (oldPanelType == PANEL_CLOSE) {
            hideKeyboard();
            mBottomPanel.setVisibility(View.VISIBLE);
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
