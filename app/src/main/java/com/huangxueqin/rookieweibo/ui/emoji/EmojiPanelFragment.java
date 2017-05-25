package com.huangxueqin.rookieweibo.ui.emoji;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.util.Pools;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    @BindView(R.id.emoji_pager)
    ViewPager mEmojiPager;
    @BindView(R.id.indicator)
    PageIndicator mIndicator;

    private LayoutInflater mInflater;
    private int mCols;
    private int mRows;

    public static EmojiPanelFragment newInstance() {
        return newInstance(COLS_SMALL, ROWS_SMALL);
    }

    public static EmojiPanelFragment newInstance(int cols, int rows) {
        Bundle argument = new Bundle();
        argument.putInt("cols", cols);
        argument.putInt("rows", rows);
        EmojiPanelFragment fragment = new EmojiPanelFragment();
        fragment.setArguments(argument);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mCols = args.getInt("cols");
        mRows = args.getInt("rows");
        mInflater = LayoutInflater.from(getContext());
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

        private Pools.SimplePool<GridLayout> mViewPool = new Pools.SimplePool<>(3);
        private ViewGroup.LayoutParams LP = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            GridLayout gridLayout =  obtainEmojiGridView();
            setupEmojiGridView(gridLayout, position);
            container.addView(gridLayout, LP);
            return gridLayout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            GridLayout gridLayout = (GridLayout) object;
            container.removeView(gridLayout);
            recycleEmojiGridView(gridLayout);
        }

        @Override
        public int getCount() {
            final int groupCount = mCols*mRows;
            final int totalCount = DefaultEmoji.EMOJI_TABLE.length;
            return (totalCount+groupCount-1) / groupCount;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        private void setupEmojiGridView(GridLayout gridLayout, int position) {
            int count = mRows * mCols;
            for (int i = 0; i < count; i++) {
                View itemView = gridLayout.getChildAt(i);
                EmojiViewHolder holder = (EmojiViewHolder) itemView.getTag();
                int col = holder.col;
                int row = holder.row;
                int resIndex = position * count + row * mCols + col;
                if (resIndex < DefaultEmoji.EMOJI_TABLE.length) {
                    holder.itemView.setVisibility(View.VISIBLE);
                    holder.emojiIcon.setImageResource(DefaultEmoji.EMOJI_TABLE[resIndex].second);
                } else {
//                    holder.itemView.setVisibility(View.INVISIBLE);
                    holder.emojiIcon.setImageResource(0);
                }
            }
        }

        private GridLayout obtainEmojiGridView() {
            GridLayout gridLayout = mViewPool.acquire();
            if (gridLayout == null) {
                Context context = getContext();
                gridLayout = new GridLayout(context);
                gridLayout.setRowCount(mRows);
                gridLayout.setColumnCount(mCols);
                for (int row = 0; row < mRows; row++) {
                    for (int col = 0; col < mCols; col++) {
                        View emojiView = mInflater.inflate(R.layout.view_small_emoji_item, null);
                        emojiView.setTag(new EmojiViewHolder(emojiView, col, row));
                        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
                        lp.rowSpec = GridLayout.spec(row, 1, 1f);
                        lp.columnSpec = GridLayout.spec(col, 1, 1f);
                        lp.setGravity(Gravity.FILL);
                        gridLayout.addView(emojiView, lp);
                    }
                }
            }
            return gridLayout;
        }

        private void recycleEmojiGridView(GridLayout gridLayout) {
            mViewPool.release(gridLayout);
        }

        class EmojiViewHolder {
            View itemView;
            ImageView emojiIcon;
            int col;
            int row;

            public EmojiViewHolder(View itemView, int col, int row) {
                this.itemView = itemView;
                this.emojiIcon = (ImageView) itemView.findViewById(R.id.emoji);
                this.col = col;
                this.row = row;

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Logger.d("TAG", "itemView height = " + v.getHeight() + ", width = " + v.getWidth());
                    }
                });
            }
        }
    }
}
