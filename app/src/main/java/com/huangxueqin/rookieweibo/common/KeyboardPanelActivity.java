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
    private boolean mBottomPanelOpen;
    private int mBottomPanelHeight = -1;

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

    // interfaces
    protected abstract int getContentViewId();
    protected abstract int getPanelViewId();
    protected abstract int getRootViewId();
    protected abstract int getLayoutId();
    protected abstract void onKeyboardShow();
    protected abstract void onKeyboardDismiss();
    protected abstract void onBottomPanelShow();
    protected abstract void onBottomPanelDismiss();

    protected int getDefaultPanelHeight() {
        return 600;
    }

    private int getPanelHeight() {
        return mBottomPanelHeight > 0 ? mBottomPanelHeight : getDefaultPanelHeight();
    }

    private void handleKeyboardShow(int keyboardHeight) {
        mKeyboardOpen = true;
        mBottomPanelOpen = false;
        mBottomPanelHeight = keyboardHeight;
        mPanelViewLP.height = 0;
        mPanelView.setLayoutParams(mPanelViewLP);
        mContentViewLP.height = mRootView.getHeight()-mContentView.getTop();
        mContentView.setLayoutParams(mContentViewLP);
        onKeyboardShow();
    }

    private void handleKeyboardDismiss() {
        mKeyboardOpen = false;
        if (mBottomPanelOpen) {
            mContentViewLP.height = mRootView.getHeight() - mContentView.getTop() - getPanelHeight();
            mContentView.setLayoutParams(mContentViewLP);
            mPanelViewLP.height = getPanelHeight();
            mPanelView.setLayoutParams(mPanelViewLP);
        } else {
            mContentViewLP.height = mRootView.getHeight() - mContentView.getTop();
            mContentView.setLayoutParams(mContentViewLP);
        }
        onKeyboardDismiss();
    }

    protected void showBottomPanel() {
        mBottomPanelOpen = true;
        if (mKeyboardOpen) {
            hideKeyboard();
        } else {
            mPanelViewLP.height = getPanelHeight();
            mPanelView.setLayoutParams(mPanelViewLP);
            mContentViewLP.height = mRootView.getHeight() - mContentView.getTop() - getPanelHeight();
            mContentView.setLayoutParams(mContentViewLP);
            onBottomPanelShow();
        }
    }

    protected void hideBottomPanel(boolean keyboardWillShow) {
        mBottomPanelOpen = false;
        if (!keyboardWillShow) {
            mPanelViewLP.height = 0;
            mPanelView.setLayoutParams(mPanelViewLP);
            mContentViewLP.height = mRootView.getHeight() - mContentView.getTop();
            mContentView.setLayoutParams(mContentViewLP);
            onBottomPanelDismiss();
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

    protected boolean isBottomPanelOpen() {
        return mBottomPanelOpen;
    }

    protected boolean isKeyboardOpen() {
        return mKeyboardOpen;
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
                    handleKeyboardDismiss();
                } else if (curWindowBottom < oldWindowBottom) {
                    // keyboard show
                    handleKeyboardShow(oldWindowBottom-curWindowBottom);
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
