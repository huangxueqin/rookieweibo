package com.huangxueqin.rookieweibo.common;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;

import com.huangxueqin.rookieweibo.BaseActivity;
import com.huangxueqin.rookieweibo.R;

/**
 * Created by huangxueqin on 2017/6/5.
 */

public abstract class KeyboardPanelActivity extends BaseActivity {

    protected View mRootView;
    protected View mContentView;
    protected View mPanelView;
    protected ViewGroup.LayoutParams mContentViewLP;
    protected ViewGroup.LayoutParams mPanelViewLP;

    private KeyboardStatusObserver mKeyboardObserver;
    private boolean mKeyboardOpen;
    private boolean mKeyboardShouldOpen;
    private boolean mBottomPanelOpen;
    private int mBottomPanelHeight = -1;

    private View mLastInputViewForBottomPanel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        mContentView = findViewById(getContentViewId());
        mPanelView = findViewById(getPanelViewId());
        mContentViewLP = mContentView.getLayoutParams();
        mPanelViewLP = mPanelView.getLayoutParams();
        mRootView = findViewById(getRootViewId());
        mKeyboardObserver = new KeyboardStatusObserver(mRootView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mKeyboardObserver.startObserve();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mKeyboardObserver.stopObserve();
    }

    protected abstract int getContentViewId();
    protected abstract int getPanelViewId();
    protected abstract int getRootViewId();
    protected abstract int getLayoutId();

    protected int getDefaultPanelHeight() {
        return 600;
    }

    private int getPanelHeight() {
        return mBottomPanelHeight > 0 ? mBottomPanelHeight : getDefaultPanelHeight();
    }

    protected void onKeyboardShow(int keyboardHeight) {
        mKeyboardOpen = true;
        mBottomPanelHeight = keyboardHeight;
        mPanelViewLP.height = 0;
        mPanelView.setLayoutParams(mPanelViewLP);
        mContentViewLP.height = mRootView.getHeight()-mContentView.getTop();
        mContentView.setLayoutParams(mContentViewLP);
    }

    protected void onKeyboardDismiss() {
        mKeyboardOpen = false;
        if (mBottomPanelOpen) {
            mPanelViewLP.height = getPanelHeight();
            mPanelView.setLayoutParams(mPanelViewLP);
            mContentViewLP.height = mRootView.getHeight()-mContentView.getTop()-getPanelHeight();
            mContentView.setLayoutParams(mContentViewLP);
        } else {
            mContentViewLP.height = mRootView.getHeight()-mContentView.getTop();
            mContentView.setLayoutParams(mContentViewLP);
        }
    }

    protected void openBottomPanel(View view) {
        mBottomPanelOpen = true;
        if (mKeyboardOpen) {
            mKeyboardShouldOpen = true;
            mLastInputViewForBottomPanel = view;
            hideKeyboard();
        } else {
            mPanelViewLP.height = getPanelHeight();
            mPanelView.setLayoutParams(mPanelViewLP);
            mContentViewLP.height = mRootView.getHeight() - mContentView.getTop() - getPanelHeight();
            mContentView.setLayoutParams(mContentViewLP);
        }
    }

    protected boolean isBottomPanelOpen() {
        return mBottomPanelOpen;
    }

    protected void closeBottomPanel() {
        mBottomPanelOpen = false;
        if (mKeyboardShouldOpen) {
            showKeyboard(mLastInputViewForBottomPanel);
            mKeyboardShouldOpen = false;
            mLastInputViewForBottomPanel = null;
        } else {
            mPanelViewLP.height = 0;
            mPanelView.setLayoutParams(mPanelViewLP);
            mContentViewLP.height = mRootView.getHeight() - mContentView.getTop();
            mContentView.setLayoutParams(mContentViewLP);
        }
    }

    protected void showKeyboard(View view) {
        if (view == null) {
            view = mContentView;
        }
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, 0);
        }
    }

    protected void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(mContentView.getWindowToken(), 0);
        }
    }

    private class KeyboardStatusObserver implements ViewTreeObserver.OnGlobalLayoutListener {
        View attachedView;
        int  attachedWindowBottom = -1;
        Rect rect = new Rect();

        public KeyboardStatusObserver(View view) {
            attachedView = view;
        }

        @Override
        public void onGlobalLayout() {
            attachedView.getWindowVisibleDisplayFrame(rect);
            if (attachedWindowBottom == -1) {
                attachedWindowBottom = rect.bottom;
                return;
            }
            final int curWindowBottom = rect.bottom;
            final int oldWindowBottom = attachedWindowBottom;
            if (Math.abs(curWindowBottom-oldWindowBottom) > 100) {
                if (curWindowBottom > oldWindowBottom) {
                    // keyboard dismiss
                    onKeyboardDismiss();
                } else if (curWindowBottom < oldWindowBottom) {
                    // keyboard show
                    onKeyboardShow(oldWindowBottom-curWindowBottom);
                }
            }
            attachedWindowBottom = curWindowBottom;
        }

        public void startObserve() {
            attachedView.getViewTreeObserver().addOnGlobalLayoutListener(this);
        }

        public void stopObserve() {
            attachedView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }
    }
}
