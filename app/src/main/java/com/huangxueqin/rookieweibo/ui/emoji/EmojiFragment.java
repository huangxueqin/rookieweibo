package com.huangxueqin.rookieweibo.ui.emoji;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pools;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.huangxueqin.rookieweibo.BaseFragment;
import com.huangxueqin.rookieweibo.R;
import com.huangxueqin.rookieweibo.ui.widget.PageIndicator;

import butterknife.BindView;

/**
 * Created by huangxueqin on 2017/4/20.
 */

public class EmojiFragment extends BaseFragment {

    @BindView(R.id.emoji_list)
    ViewPager mEmojiList;
    @BindView(R.id.indicator)
    PageIndicator mIndicator;

    LayoutInflater mInflater;

    EmojiManager mEmojiHelper;
    Pair<String, Integer>[] mEmojiTable;
    int mColCount;
    int mRowCount;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEmojiHelper = EmojiManager.getInstance();
        mEmojiTable = mEmojiHelper.getEmojiTable();
        mColCount = 7;
        mRowCount = 3;
        mInflater = LayoutInflater.from(getContext());
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_emoji;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mEmojiList.setAdapter(new EmojiAdapter());
        mIndicator.setPageCount(4);
    }

    private class EmojiAdapter extends PagerAdapter {
        private Pools.SimplePool<EmojiGridView> mPagePool = new Pools.SimplePool<>(3);

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Log.d("TAG", "position = " + position);
            Log.d("TAG", "mEmojiTable.length = " + mEmojiTable.length);
            EmojiGridView view = obtainEmojiGridView();
            final int start = position*mColCount*mRowCount;
            final int count = Math.min(mColCount*mRowCount, mEmojiTable.length-start);
            Log.d("TAG", "start = " + start + ", count = " + count);

            view.setEmojiRes(mEmojiTable, start, count);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            EmojiGridView view = (EmojiGridView) object;
            container.removeView(view);
            recycleEmojiGridView(view);
        }

        @Override
        public int getCount() {
            final int groupCount = mColCount*mRowCount;
            final int totalCount = mEmojiTable.length;
            return (totalCount+groupCount-1) / groupCount;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        private EmojiGridView obtainEmojiGridView() {
            EmojiGridView view = mPagePool.acquire();
            if (view == null) {
                view = new EmojiGridView(getContext(), mRowCount, mColCount, 96);
                view.setPadding(20, 80, 20, 80);
                view.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
            }
            return view;
        }

        private void recycleEmojiGridView(EmojiGridView view) {
            mPagePool.release(view);
        }
    }
}