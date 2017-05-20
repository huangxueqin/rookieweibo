package com.huangxueqin.rookieweibo.ui.emoji;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.util.Pools;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;

import com.huangxueqin.rookieweibo.BaseFragment;
import com.huangxueqin.rookieweibo.R;
import com.huangxueqin.rookieweibo.common.Logger;
import com.huangxueqin.rookieweibo.ui.widget.PageIndicator;

import butterknife.BindView;

/**
 * Created by huangxueqin on 2017/4/20.
 */

public class EmojiPanelFragment extends BaseFragment {

    private static final int COLS_SMALL = 7;
    private static final int ROWS_SMALL = 3;
    private static final int EMJ_PANEL_PADDING = 10;

    @BindView(R.id.emoji_pager)
    ViewPager mEmojiPager;
    @BindView(R.id.indicator)
    PageIndicator mIndicator;

    private LayoutInflater mInflater;
    private int mEmojiPanelPadding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInflater = LayoutInflater.from(getContext());
        mEmojiPanelPadding = (int) getResources().getDisplayMetrics().density * EMJ_PANEL_PADDING;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_emoji;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mEmojiPager.setAdapter(new EmojiPanelAdapter());
        mIndicator.setViewPager(mEmojiPager);
    }

    private class EmojiPanelAdapter extends PagerAdapter {
        private Pools.SimplePool<GridLayout> mPagePool = new Pools.SimplePool<>(3);

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            GridLayout view = obtainEmojiGridView(position);
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            container.addView(view, lp);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            GridLayout gridLayout = (GridLayout) object;
            container.removeView(gridLayout);
            recycleEmojiGridView(gridLayout);
        }

        @Override
        public int getCount() {
            final int groupCount = COLS_SMALL*ROWS_SMALL;
            final int totalCount = EmojiManager.EMOJI_TABLE.length;
            return (totalCount+groupCount-1) / groupCount;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        private GridLayout obtainEmojiGridView(int position) {
            GridLayout gridLayout = mPagePool.acquire();
            if (gridLayout == null) {
                gridLayout = new GridLayout(getContext());
                gridLayout.setRowCount(ROWS_SMALL);
                gridLayout.setColumnCount(COLS_SMALL);
                gridLayout.setPadding(mEmojiPanelPadding, mEmojiPanelPadding, mEmojiPanelPadding, mEmojiPanelPadding);
                for (int row = 0; row < ROWS_SMALL; row++) {
                    for (int col = 0; col < COLS_SMALL; col++) {
                        View emojiView = mInflater.inflate(R.layout.view_small_emoji_item, null);
                        emojiView.setTag(new EmojiViewHolder(emojiView, col, row));
                        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
                        lp.rowSpec = GridLayout.spec(row, 1, 1f);
                        lp.columnSpec = GridLayout.spec(col, 1, 1f);
                        gridLayout.addView(emojiView, lp);
                    }
                }
            }

            int count = COLS_SMALL * ROWS_SMALL;
            for (int i = 0; i < count; i++) {
                View emojiView = gridLayout.getChildAt(i);
                EmojiViewHolder holder = (EmojiViewHolder) emojiView.getTag();
                int col = holder.col;
                int row = holder.row;
                int offset = position * COLS_SMALL * ROWS_SMALL + row*COLS_SMALL + col;
                if (offset < EmojiManager.EMOJI_TABLE.length) {
                    emojiView.setVisibility(View.VISIBLE);
                    holder.icon.setImageResource(EmojiManager.EMOJI_TABLE[offset].second);
                } else {
                    emojiView.setVisibility(View.INVISIBLE);
                }
            }
            return gridLayout;
        }

        private void recycleEmojiGridView(GridLayout gridLayout) {
            mPagePool.release(gridLayout);
        }

        class EmojiViewHolder {
            View itemView;
            ImageView icon;
            int col;
            int row;

            public EmojiViewHolder(View itemView, int col, int row) {
                this.itemView = itemView;
                this.col = col;
                this.row = row;
                this.icon = (ImageView) itemView.findViewById(R.id.emoji);
            }
        }
    }
}
