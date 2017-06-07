package com.huangxueqin.pageindicator;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by huangxueqin on 2017/4/23.
 */

public class PageIndicator extends View {

    public PageIndicator(Context context) {
        this(context, null);
    }

    public PageIndicator(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PageIndicator(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public interface IndicatorListener {
        void onPageScrollStateChanged(int state);
        void onPageScrolled(int position, float positionOffset);
        void onPageSelected(int position);
    }
}
