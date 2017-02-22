package com.huangxueqin.rookieweibo;

/**
 * Created by huangxueqin on 2017/2/22.
 */

public class AppConfiguration {
    public interface Main {
        String[] TabNavTitles = {"首页", "消息", "发现", "我的"};
        int TabFrontPage = 0;
        int TabUser = 1;

        int PrimaryTabNdx = Main.TabFrontPage;
    }
}
