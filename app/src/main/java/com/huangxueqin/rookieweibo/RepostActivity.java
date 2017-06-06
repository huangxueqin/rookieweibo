package com.huangxueqin.rookieweibo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.huangxueqin.rookieweibo.auth.AuthConstants;
import com.huangxueqin.rookieweibo.common.EmoticonStringFormatter;
import com.huangxueqin.rookieweibo.common.KeyboardPanelActivity;
import com.huangxueqin.rookieweibo.cons.Cons;
import com.huangxueqin.rookieweibo.ui.emoji.Emoticon;
import com.huangxueqin.rookieweibo.ui.emoji.EmoticonFragment;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.legacy.StatusesAPI;
import com.sina.weibo.sdk.openapi.models.Status;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by huangxueqin on 2017/4/18.
 */

public class RepostActivity extends KeyboardPanelActivity implements EmoticonFragment.OnEmoticonClickListener {

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
        mBtnEmotionPick.setOnClickListener(mEmoticonPickerListener);

        final String statusStr = getIntent().getStringExtra(Cons.IntentKey.STATUS);
        mStatus = new Gson().fromJson(statusStr, Status.class);
        mStatusesAPI = new StatusesAPI(this, AuthConstants.APP_KEY, mAccessToken);

        initViews();
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
            mContentEditor.setText(EmoticonStringFormatter.format(this, "//@" + mStatus.user.screen_name + "：" + mStatus.text, mContentEditor.getTextSize()));
        }
        mContentEditor.setSelection(0);
    }

    @Override
    protected void onToolbarButtonPress(View v) {
        if (v.getId() == R.id.menu_send) {
            doRepost();
        }
    }

    @Override
    public void onClick(View v, Emoticon e) {
        int insertPos = mContentEditor.getSelectionStart();
        String emoticonStr = "["+e.getName()+"]";
        StringBuilder sb = new StringBuilder(mContentEditor.getText());
        sb.insert(insertPos, emoticonStr);
        mContentEditor.setText(EmoticonStringFormatter.format(this, sb, mContentEditor.getTextSize()));
        mContentEditor.setSelection(insertPos+emoticonStr.length());
    }

    private View.OnClickListener mEmoticonPickerListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mBtnEmotionPick.isSelected()) {
                mBtnEmotionPick.setSelected(false);
                hideBottomPanel(true);
                showKeyboard(mContentEditor);
            } else {
                mBtnEmotionPick.setSelected(true);
                showBottomPanel();
                if (mEmojiFragment == null) {
                    mEmojiFragment = EmoticonFragment.newInstance();
                    mEmojiFragment.setOnEmoticonClickListener(RepostActivity.this);
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.bottom_panel, mEmojiFragment)
                            .commit();
                }
            }
        }
    };

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

    // KeyboardPanelActivity interfaces
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
    protected void onBottomPanelShow() {
        mBtnEmotionPick.setSelected(true);
    }

    @Override
    protected void onBottomPanelDismiss() {
        mBtnEmotionPick.setSelected(false);
    }

    @Override
    protected void onKeyboardShow() {
        mBtnEmotionPick.setSelected(false);
    }

    @Override
    protected void onKeyboardDismiss() {
        mBtnEmotionPick.setSelected(isBottomPanelOpen());
    }
}
