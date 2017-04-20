package com.huangxueqin.rookieweibo.ui.emoji;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;

import com.huangxueqin.rookieweibo.R;
import com.huangxueqin.rookieweibo.common.utils.L;
import com.lsjwzh.widget.recyclerviewpager.RecyclerViewPager;

/**
 * Created by huangxueqin on 2017/4/20.
 */

public class EmojiFragment extends Fragment {

    RecyclerViewPager mEmojiList;
    LayoutInflater mInflater;

    EmojiHelper mEmojiHelper;
    Pair<String, Integer>[] mEmojiTable;
    int mColCount;
    int mRowCount;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEmojiHelper = EmojiHelper.getInstance();
        mEmojiTable = mEmojiHelper.getEmojiTable();
        mColCount = 7;
        mRowCount = 3;

        mInflater = LayoutInflater.from(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_emoji, container, false);
        mEmojiList = (RecyclerViewPager) rootView.findViewById(R.id.emoji_list);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mEmojiList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mEmojiList.setAdapter(new EmojiAdapter());
    }

    private class EmojiAdapter extends RecyclerView.Adapter<ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            L.d("TAG", "onCreateViewHolder...");
            return new ViewHolder(createEmojiGrid());
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            L.d("TAG", "onBindViewHolder...");
            final int countPerPage = mColCount*mRowCount;
            GridLayout emojiGrid = (GridLayout) holder.itemView;
            final int offset = position * countPerPage;
            for (int i = 0; i < countPerPage; i++) {
                final int emjIndex = i + offset;
                ImageView emojiView = (ImageView) emojiGrid.getChildAt(i);
                if (emjIndex >= mEmojiTable.length) {
                    emojiView.setEnabled(false);
                    emojiView.setImageDrawable(null);
                } else {
                    emojiView.setEnabled(true);
                    emojiView.setImageResource(mEmojiTable[emjIndex].second);
                    emojiView.setTag(emjIndex);
                }
            }
            emojiGrid.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
        }

        @Override
        public int getItemCount() {
            L.d("TAG", "getItemCount...");
            final int countPerPage = mColCount*mRowCount;
            return mEmojiTable.length/countPerPage + (mEmojiTable.length % countPerPage == 0 ? 0 : 1);
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    private GridLayout createEmojiGrid() {
        GridLayout grid = (GridLayout) mInflater.inflate(R.layout.view_emoji_grid, null);
        grid.setColumnCount(7);
        grid.setRowCount(3);

        for (int i = 0; i < 21; i++) {
            ImageView emjView = (ImageView) mInflater.inflate(R.layout.view_emoji_icon, null);
            grid.addView(emjView);
            emjView.setOnClickListener(mEmjClickListener);
        }

        return grid;
    }

    View.OnClickListener mEmjClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final int index = (Integer)v.getTag();
            View parent = (ViewGroup) v.getParent();
            Log.d("TAG", "grid width = " + parent.getWidth());
            Log.d("TAG", "grid height = " + parent.getHeight());
        }
    };
}
