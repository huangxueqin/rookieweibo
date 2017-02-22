package com.huangxueqin.rookieweibo;

import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.huangxueqin.rookieweibo.slide_tab_layout.SlideTabLayout;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener, SlideTabLayout.TabSelectListener  {

    @BindView(R.id.slide_tab_nav) SlideTabLayout mSlideTabNav;
    @BindView(R.id.view_pager) ViewPager mFragmentContainer;

    MainFragmentAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mAdapter = new MainFragmentAdapter(getSupportFragmentManager());
        mFragmentContainer.setAdapter(mAdapter);
        mFragmentContainer.setOffscreenPageLimit(mAdapter.getCount()+1);
        mFragmentContainer.addOnPageChangeListener(this);
        mSlideTabNav.attach(mAdapter);
        mSlideTabNav.setTabSelectListener(this);
        mSlideTabNav.setCurrentItem(AppConfiguration.Main.PrimaryTabNdx);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        mSlideTabNav.onPageScrolled(position, positionOffset, positionOffsetPixels);
    }

    @Override
    public void onPageSelected(int position) {
        mSlideTabNav.onPageSelected(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        mSlideTabNav.onPageScrollStateChanged(state);
    }

    @Override
    public void onTabSelected(int tabNdx) {
        mFragmentContainer.setCurrentItem(tabNdx);
    }
}
