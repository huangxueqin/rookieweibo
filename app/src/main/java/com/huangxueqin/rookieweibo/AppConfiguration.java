package com.huangxueqin.rookieweibo;

/**
 * Created by huangxueqin on 2017/2/22.
 */

public class AppConfiguration {
    public interface Main {
        String[] TabNavTitles = {"首页", "消息", "我的"};
        int TabCount = TabNavTitles.length;
        int TabWeiboFlow = 0;
        int TabMessage = 1;
        int TabUserCenter = 2;

        int PrimaryTabNdx = Main.TabWeiboFlow;
    }

    public interface Status {
        int COUNT = 20;
        boolean BASE_APP = false;
    }

    public interface Comment {
        int COUNT = 20;
    }
}
