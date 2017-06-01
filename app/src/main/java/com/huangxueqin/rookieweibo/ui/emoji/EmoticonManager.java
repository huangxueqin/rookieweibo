package com.huangxueqin.rookieweibo.ui.emoji;

import android.support.annotation.DrawableRes;
import android.util.Pair;

import com.huangxueqin.rookieweibo.R;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by huangxueqin on 2017/4/20.
 */

public class EmoticonManager {

    private final static Emoticon[] DEFAULT_EMOJI = new Emoticon[] {
            new Emoticon("爱你", R.mipmap.d_aini),
            new Emoticon("奥特曼", R.mipmap.d_aoteman),
            new Emoticon("拜拜", R.mipmap.d_baibai),
            new Emoticon("抱抱", R.mipmap.d_baobao),
            new Emoticon("悲伤", R.mipmap.d_beishang),
            new Emoticon("鄙视", R.mipmap.d_bishi),
            new Emoticon("闭嘴", R.mipmap.d_bizui),
            new Emoticon("馋嘴", R.mipmap.d_chanzui),
            new Emoticon("吃惊", R.mipmap.d_chijing),
            new Emoticon("哈欠", R.mipmap.d_dahaqi),
            new Emoticon("打脸", R.mipmap.d_dalian),
            new Emoticon("顶", R.mipmap.d_ding),
            new Emoticon("doge", R.mipmap.d_doge),
            new Emoticon("二哈", R.mipmap.d_erha),
            new Emoticon("肥皂", R.mipmap.d_feizao),
            new Emoticon("感冒", R.mipmap.d_ganmao),
            new Emoticon("鼓掌", R.mipmap.d_guzhang),
            new Emoticon("哈哈", R.mipmap.d_haha),
            new Emoticon("害羞", R.mipmap.d_haixiu),
            new Emoticon("汗", R.mipmap.d_han),
            new Emoticon("呵呵", R.mipmap.d_hehe),
            new Emoticon("哼", R.mipmap.d_heng),
            new Emoticon("黑线", R.mipmap.d_heixian),
            new Emoticon("坏笑", R.mipmap.d_huaixiao),
            new Emoticon("花心", R.mipmap.d_huaxin),
            new Emoticon("挤眼", R.mipmap.d_jiyan),
            new Emoticon("可爱", R.mipmap.d_keai),
            new Emoticon("可怜", R.mipmap.d_kelian),
            new Emoticon("哭", R.mipmap.d_ku),
            new Emoticon("骷髅", R.mipmap.d_kulou),
            new Emoticon("困", R.mipmap.d_kun),
            new Emoticon("懒得理你", R.mipmap.d_landelini),
            new Emoticon("浪", R.mipmap.d_lang),
            new Emoticon("累", R.mipmap.d_lei),
            new Emoticon("喵", R.mipmap.d_miao),
            new Emoticon("男孩儿", R.mipmap.d_nanhaier),
            new Emoticon("怒", R.mipmap.d_nu),
            new Emoticon("怒骂", R.mipmap.d_numa),
            new Emoticon("女孩儿", R.mipmap.d_nvhaier),
            new Emoticon("钱", R.mipmap.d_qian),
            new Emoticon("亲亲", R.mipmap.d_qinqin),
            new Emoticon("傻眼", R.mipmap.d_shayan),
            new Emoticon("生病", R.mipmap.d_shengbing),
            new Emoticon("神兽", R.mipmap.d_shenshou),
            new Emoticon("失望", R.mipmap.d_shiwang),
            new Emoticon("帅", R.mipmap.d_shuai),
            new Emoticon("睡觉", R.mipmap.d_shuijiao),
            new Emoticon("思考", R.mipmap.d_sikao),
            new Emoticon("太开心", R.mipmap.d_taikaixin),
            new Emoticon("摊手", R.mipmap.d_tanshou),
            new Emoticon("甜", R.mipmap.d_tian),
            new Emoticon("偷笑", R.mipmap.d_touxiao),
            new Emoticon("兔子", R.mipmap.d_tuzi),
            new Emoticon("挖鼻屎", R.mipmap.d_wabishi),
            new Emoticon("委屈", R.mipmap.d_weiqu),
            new Emoticon("污", R.mipmap.d_wu),
            new Emoticon("笑cry", R.mipmap.d_xiaoku),
            new Emoticon("熊猫", R.mipmap.d_xiongmao),
            new Emoticon("嘻嘻", R.mipmap.d_xixi),
            new Emoticon("嘘", R.mipmap.d_xu),
            new Emoticon("阴险", R.mipmap.d_yinxian),
            new Emoticon("疑问", R.mipmap.d_yiwen),
            new Emoticon("右哼哼", R.mipmap.d_youhengheng),
            new Emoticon("晕", R.mipmap.d_yun),
            new Emoticon("抓狂", R.mipmap.d_zhuakuang),
            new Emoticon("猪头", R.mipmap.d_zhutou),
            new Emoticon("最右", R.mipmap.d_zuiyou),
            new Emoticon("左哼哼", R.mipmap.d_zuohengheng)
    };

    private static Map<String, Emoticon> DEFAULT_EMOJI_MAP;

    static {
        Map<String, Emoticon> map = new HashMap<>(DEFAULT_EMOJI.length);
        for (int i = 0; i < DEFAULT_EMOJI.length; i++) {
            Emoticon e = DEFAULT_EMOJI[i];
            map.put(e.name, e);
        }
        DEFAULT_EMOJI_MAP = Collections.unmodifiableMap(map);
    }

    private static EmoticonManager sInstance = null;

    public static synchronized EmoticonManager getInstance() {
        if (sInstance == null) {
            sInstance = new EmoticonManager();
        }
        return sInstance;
    }

    private EmoticonManager() {
        if (sInstance != null) {
            throw new IllegalStateException("can not invoke constructor by user");
        }
    }

    public Emoticon getEmoticon(String name) {
        return DEFAULT_EMOJI_MAP.get(name);
    }

    public Emoticon[] getDefaultEmoticons() {
        return DEFAULT_EMOJI;
    }
}
