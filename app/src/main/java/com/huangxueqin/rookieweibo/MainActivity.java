package com.huangxueqin.rookieweibo;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.huangxueqin.rookieweibo.auth.AccessTokenKeeper;
import com.huangxueqin.rookieweibo.auth.UserKeeper;
import com.huangxueqin.rookieweibo.interfaces.IFragmentCallback;
import com.huangxueqin.rookieweibo.widget.SlideTabLayout;
import com.huangxueqin.rookieweibo.ui.status.WeiboFlowFragment;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.openapi.models.User;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements
        ViewPager.OnPageChangeListener,
        SlideTabLayout.TabSelectListener,
        IFragmentCallback {

    @BindView(R.id.slide_tab_nav) SlideTabLayout mSlideTabNav;
    @BindView(R.id.view_pager) ViewPager mFragmentPager;

    Fragment[] mFragments;
    User mUser;
    Oauth2AccessToken mAccessToken;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mUser = UserKeeper.readUser(this);
        mAccessToken = AccessTokenKeeper.readAccessToken(this);

        createMainFragments();
        initFragmentPager();

        mSlideTabNav.setAdapter(mSlideTabAdapter);
        mSlideTabNav.setTabSelectListener(this);
        mSlideTabNav.setCurrentItem(AppConfiguration.Main.PrimaryTabNdx);
    }

    private void createMainFragments() {
        mFragments = new Fragment[AppConfiguration.Main.TabCount];
        for (int i = 0; i < mFragments.length; i++) {
            final BaseFragment fragment;
            if (i == AppConfiguration.Main.TabWeiboFlow) {
                fragment = new WeiboFlowFragment();
            } else if (i == AppConfiguration.Main.TabUserCenter) {
                fragment = new UserCenterFragment();
            } else {
                fragment = new BlankFragment();
            }
            fragment.setCallback(this);
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
        mFragmentPager.setCurrentItem(tabNdx);
    }

    @Override
    public User getUser() {
        return mUser;
    }

    @Override
    public Oauth2AccessToken getAccessToken() {
        return mAccessToken;
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
}
