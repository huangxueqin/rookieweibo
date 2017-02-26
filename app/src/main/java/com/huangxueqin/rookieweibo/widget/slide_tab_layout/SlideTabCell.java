package com.huangxueqin.rookieweibo.widget.slide_tab_layout;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.huangxueqin.rookieweibo.R;

/**
 * Created by huangxueqin on 2017/2/1.
 */

public class SlideTabCell extends FrameLayout {
    int index;
    private TextView mTitle;

    @TargetApi(21)
    public SlideTabCell(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public SlideTabCell(Context context) {
        super(context);
    }

    public SlideTabCell(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SlideTabCell(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setTitle(String title) {
        mTitle.setText(title);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTitle = (TextView) findViewById(R.id.title);
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        mTitle.setSelected(selected);
    }
}
