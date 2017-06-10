package com.huangxueqin.rookieweibo;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.huangxueqin.rookieweibo.auth.AuthConstants;
import com.huangxueqin.rookieweibo.common.EmoticonStringFormatter;
import com.huangxueqin.rookieweibo.common.KeyboardPanelActivity;
import com.huangxueqin.rookieweibo.ui.emoji.Emoticon;
import com.huangxueqin.rookieweibo.ui.emoji.EmoticonFragment;
import com.huangxueqin.rookieweibo.ui.widget.WeiboStatusImageView;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.legacy.StatusesAPI;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by huangxueqin on 2017/6/10.
 */

public class PubStatusActivity extends KeyboardPanelActivity {
    private static final int REQUEST_PICK_IMAGE = 100;

    @BindView(R.id.content_editor)
    EditText mContentEditor;
    @BindView(R.id.emoji_pick_button)
    View mEmojiPickButton;
    @BindView(R.id.image_pick_button)
    View mImagePickButton;
    @BindView(R.id.send_status_button)
    View mSendStatusButton;
    @BindView(R.id.update_image_area)
    ViewGroup mUpdateImageArea;
    @BindView(R.id.upload_image)
    WeiboStatusImageView mUploadImage;
    @BindView(R.id.delete_image)
    View mDeleteImageButton;

    StatusesAPI mStatusApi;
    EmoticonFragment mEmoticonFragment;

    Uri mSelectedImageUri;
    ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        mStatusApi = new StatusesAPI(this, AuthConstants.APP_KEY, mAccessToken);

        mImagePickButton.setOnClickListener(mToolbarActionListener);
        mEmojiPickButton.setOnClickListener(mToolbarActionListener);
        mSendStatusButton.setOnClickListener(mToolbarActionListener);
        mDeleteImageButton.setOnClickListener(mToolbarActionListener);
        initBottomPanel();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isKeyboardOpen()) {
                    showKeyboard(mContentEditor);
                    mContentEditor.setSelection(0);
                }
            }
        }, 500);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mUpdateImageArea.setVisibility(mSelectedImageUri == null ? View.GONE : View.VISIBLE);
        if (mSelectedImageUri != null) {
            Glide.with(this).load(mSelectedImageUri).into(mUploadImage);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_PICK_IMAGE:
                if (resultCode == RESULT_OK) {
                    mSelectedImageUri = data.getData();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }

    }

    @Override
    protected void onToolbarButtonPress(View v) {
        if (v.getId() == R.id.emoji_pick_button) {
            if (mEmojiPickButton.isSelected()) {
                mEmojiPickButton.setSelected(false);
                hideBottomPanel(true);
                showKeyboard(mContentEditor);
            } else {
                mEmojiPickButton.setSelected(true);
                showBottomPanel();
            }
        } else if (v.getId() == R.id.image_pick_button) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_PICK_IMAGE);
        } else if (v.getId() == R.id.send_status_button) {
            String content = mContentEditor.getText().toString();
            if (TextUtils.isEmpty(content)) {
                Toast.makeText(this, "请输入微博内容", Toast.LENGTH_SHORT).show();
            } else {
                doPublish();
            }
        } else if (v.getId() == R.id.delete_image) {
            mSelectedImageUri = null;
            mUploadImage.setImageDrawable(null);
            mUpdateImageArea.setVisibility(View.GONE);
        }
    }

    private void doPublish() {
        final String content = mContentEditor.getText().toString();
        final RequestListener listener =  new RequestListener() {
            @Override
            public void onComplete(String s) {
                mProgressDialog.dismiss();
                setResult(RESULT_OK);
                PubStatusActivity.this.finish();
            }

            @Override
            public void onWeiboException(WeiboException e) {
                mProgressDialog.dismiss();
                Toast.makeText(PubStatusActivity.this, "发送失败，请重试", Toast.LENGTH_SHORT).show();
            }
        };
        mProgressDialog = ProgressDialog.show(this, null, "正在发送...");
        if (mSelectedImageUri == null) {
            mStatusApi.update(content, "0.0", "0.0", listener);
        } else {
            Glide.with(this).load(mSelectedImageUri).asBitmap().into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                    mStatusApi.upload(content, resource, "0.0", "0.0", listener);
                }

                @Override
                public void onLoadFailed(Exception e, Drawable errorDrawable) {
                    super.onLoadFailed(e, errorDrawable);
                    mProgressDialog.dismiss();
                    Toast.makeText(PubStatusActivity.this, "发送失败，请重试", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void initBottomPanel() {
        mEmoticonFragment = EmoticonFragment.newInstance();
        mEmoticonFragment.setOnEmoticonClickListener(mOnEmoticonClickListener);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.bottom_panel, mEmoticonFragment)
                .commit();
    }

    private EmoticonFragment.OnEmoticonClickListener mOnEmoticonClickListener =
            new EmoticonFragment.OnEmoticonClickListener() {
                @Override
                public void onClick(View v, Emoticon e) {
                    int insertPos = mContentEditor.getSelectionStart();
                    String emoticonStr = "["+e.getName()+"]";
                    StringBuilder sb = new StringBuilder(mContentEditor.getText());
                    sb.insert(insertPos, emoticonStr);
                    mContentEditor.setText(EmoticonStringFormatter.format(PubStatusActivity.this,
                            sb, mContentEditor.getTextSize()));
                    mContentEditor.setSelection(insertPos+emoticonStr.length());
                }
            };

    // KeyboardPanelActivity Apis
    @Override
    protected int getContentViewId() {
        return R.id.content_view;
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
        return R.layout.activity_pub_status;
    }

    @Override
    protected void onKeyboardShow() {
        mEmojiPickButton.setSelected(false);
    }

    @Override
    protected void onKeyboardDismiss() {
        mEmojiPickButton.setSelected(isBottomPanelOpen());
    }

    @Override
    protected void onBottomPanelShow() {
        mEmojiPickButton.setSelected(true);
    }

    @Override
    protected void onBottomPanelDismiss() {
        mEmojiPickButton.setSelected(false);
    }
}
