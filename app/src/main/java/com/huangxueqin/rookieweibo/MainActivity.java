package com.huangxueqin.rookieweibo;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.View;

import com.huangxueqin.rookieweibo.interfaces.IFragmentCallback;
import com.huangxueqin.rookieweibo.ui.message.MessageFragment;
import com.huangxueqin.rookieweibo.ui.profile.ProfileFragment;
import com.huangxueqin.rookieweibo.ui.widget.SlideTabLayout;
import com.huangxueqin.rookieweibo.ui.status.WeiboFlowFragment;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.openapi.models.User;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity implements
        ViewPager.OnPageChangeListener,
        SlideTabLayout.TabSelectListener,
        IFragmentCallback {

    private static final int REQUEST_PUB_STATUS = 100;

    @BindView(R.id.slide_tab_nav) SlideTabLayout mSlideTabNav;
    @BindView(R.id.view_pager) ViewPager mFragmentPager;
    @BindView(R.id.send_button) View mSendButton;

    Fragment[] mFragments;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        createMainFragments();
        initFragmentPager();

        mSlideTabNav.setAdapter(mSlideTabAdapter);
        mSlideTabNav.setTabSelectListener(this);
        mSlideTabNav.setCurrentItem(AppConfiguration.Main.PrimaryTabNdx);

        mSendButton.setOnClickListener(mToolbarActionListener);
    }

    private void createMainFragments() {
        mFragments = new Fragment[AppConfiguration.Main.TabCount];
        for (int i = 0; i < mFragments.length; i++) {
            final BaseFragment fragment;
            if (i == AppConfiguration.Main.TabWeiboFlow) {
                fragment = new WeiboFlowFragment();
            } else if (i == AppConfiguration.Main.TabUserCenter) {
                fragment = new ProfileFragment();
            } else  {
                fragment = new MessageFragment();
            }
            mFragments[i] = fragment;
        }
    }

    private void initFragmentPager() {
        mFragmentPager.setOffscreenPageLimit(mFragments.length+1);
        mFragmentPager.addOnPageChangeListener(this);
        mFragmentPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return mFragments[position];
            }
            @Override
            public int getCount() {
                return mFragments.length;
            }
        });
    }

    @Override
    protected void onToolbarButtonPress(View v) {
        if (v.getId() == R.id.send_button) {
            Intent intent = new Intent(this, PubStatusActivity.class);
            startActivityForResult(intent, REQUEST_PUB_STATUS);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PUB_STATUS) {
            if (resultCode == RESULT_OK) {
                final int currentItem = mFragmentPager.getCurrentItem();
                if (currentItem == AppConfiguration.Main.TabWeiboFlow) {
                    WeiboFlowFragment fragment = (WeiboFlowFragment) mFragments[currentItem];
                    fragment.setNeedRefresh();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    // SlideTabLayout
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

    private SlideTabLayout.Adapter mSlideTabAdapter = new SlideTabLayout.Adapter() {
        @Override
        public int getTabCount() {
            return mFragments.length;
        }

        @Override
        public String getTabTitle(int tabNdx) {
            return AppConfiguration.Main.TabNavTitles[tabNdx];
        }
    };

    @Override
    public void onTabSelected(int tabNdx) {
        mFragmentPager.setCurrentItem(tabNdx);
    }

    // pub interfaces
    @Override
    public User getUser() {
        return mUser;
    }

    @Override
    public Oauth2AccessToken getAccessToken() {
        return mAccessToken;
    }


}
