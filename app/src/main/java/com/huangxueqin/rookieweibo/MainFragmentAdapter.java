package com.huangxueqin.rookieweibo;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.huangxueqin.rookieweibo.slide_tab_layout.SlideTabLayout;

/**
 * Created by huangxueqin on 2017/2/22.
 */

public class MainFragmentAdapter extends FragmentPagerAdapter implements SlideTabLayout.Callback {

    MainFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case AppConfiguration.Main.TabFrontPage:
                return new FrontPageFragment();
            case AppConfiguration.Main.TabUser:
                return new UserFragment();
        }
        return new BlankFragment();
    }

    @Override
    public int getCount() {
        return AppConfiguration.Main.TabNavTitles.length;
    }

    @Override
    public int getTabCount() {
        return AppConfiguration.Main.TabNavTitles.length;
    }

    @Override
    public String getTabTitle(int tabNdx) {
        return AppConfiguration.Main.TabNavTitles[tabNdx];
    }
}
