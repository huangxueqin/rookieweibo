package com.huangxueqin.rookieweibo.common;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewTreeObserver;

import com.huangxueqin.rookieweibo.RepostActivity;

/**
 * Created by huangxueqin on 2017/6/2.
 */

public class KeyboardObserver implements ViewTreeObserver.OnGlobalLayoutListener{

    View attachedView;
    KeyboardStateListener mListener;
    Rect rect = new Rect();

    int windowBottom = -1;

    public KeyboardObserver(@NonNull View attachedView) {
        this.attachedView = attachedView;
    }

    public void setKeyboardStateListener(KeyboardStateListener listener) {
        mListener = listener;
    }

    @Override
    public void onGlobalLayout() {
        attachedView.getWindowVisibleDisplayFrame(rect);
        if (windowBottom == -1) {
            windowBottom = rect.bottom;
            return;
        }

        final int curWindowBottom = rect.bottom;
        final int lastWindowBottom = windowBottom;

        if (curWindowBottom < lastWindowBottom) {
            // keyboard showing
            if (mListener != null) {
                mListener.onKeyboardShow(lastWindowBottom-curWindowBottom);
            }
        }

        if (curWindowBottom > lastWindowBottom) {
            if (mListener != null) {
                mListener.onKeyboardDismiss();
            }
        }

        windowBottom = curWindowBottom;
    }

    public void startObserve() {
        attachedView.getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    public void stopObserve() {
        attachedView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
    }

    public interface KeyboardStateListener {
        void onKeyboardShow(int keyboardHeight);
        void onKeyboardDismiss();
    }
}
