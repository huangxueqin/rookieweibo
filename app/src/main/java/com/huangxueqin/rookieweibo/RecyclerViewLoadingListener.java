package com.huangxueqin.rookieweibo;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Created by huangxueqin on 2017/2/24.
 */

public abstract class RecyclerViewLoadingListener extends RecyclerView.OnScrollListener {
    @Override
    public final void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        if (dy > 0 && allowLoading()) {
            RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();
            final int visibleItemCount = lm.getChildCount();
            final int totalItemCount = lm.getItemCount();
            if (lm instanceof LinearLayoutManager) {
                LinearLayoutManager llm = (LinearLayoutManager) lm;
                final int startPosition = llm.findFirstVisibleItemPosition();
                if (startPosition + visibleItemCount >= totalItemCount) {
                    performLoadingAction();
                }
            }
        }
    }

    public boolean allowLoading() {
        return true;
    }

    public abstract void performLoadingAction();
}
