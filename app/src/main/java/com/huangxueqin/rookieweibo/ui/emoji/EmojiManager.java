package com.huangxueqin.rookieweibo.ui.emoji;

import android.util.Pair;

import com.huangxueqin.rookieweibo.R;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by huangxueqin on 2017/4/20.
 */

public class EmojiManager {
    private static class EmojiManagerLoader {
        private final static Pair<String, Integer>[] EMOJI_TABLE = new Pair[] {
                new Pair("爱你", R.mipmap.d_aini),
                new Pair("奥特曼", R.mipmap.d_aoteman),
                new Pair("拜拜", R.mipmap.d_baibai),
                new Pair("抱抱", R.mipmap.d_baobao),
                new Pair("悲伤", R.mipmap.d_beishang),
                new Pair("鄙视", R.mipmap.d_bishi),
                new Pair("闭嘴", R.mipmap.d_bizui),
                new Pair("馋嘴", R.mipmap.d_chanzui),
                new Pair("吃惊", R.mipmap.d_chijing),
                new Pair("哈欠", R.mipmap.d_dahaqi),
                new Pair("打脸", R.mipmap.d_dalian),
                new Pair("顶", R.mipmap.d_ding),
                new Pair("doge", R.mipmap.d_doge),
                new Pair("二哈", R.mipmap.d_erha),
                new Pair("肥皂", R.mipmap.d_feizao),
                new Pair("感冒", R.mipmap.d_ganmao),
                new Pair("鼓掌", R.mipmap.d_guzhang),
                new Pair("哈哈", R.mipmap.d_haha),
                new Pair("害羞", R.mipmap.d_haixiu),
                new Pair("汗", R.mipmap.d_han),
                new Pair("呵呵", R.mipmap.d_hehe),
                new Pair("哼", R.mipmap.d_heng),
                new Pair("黑线", R.mipmap.d_heixian),
                new Pair("坏笑", R.mipmap.d_huaixiao),
                new Pair("花心", R.mipmap.d_huaxin),
                new Pair("挤眼", R.mipmap.d_jiyan),
                new Pair("可爱", R.mipmap.d_keai),
                new Pair("可怜", R.mipmap.d_kelian),
                new Pair("哭", R.mipmap.d_ku),
                new Pair("骷髅", R.mipmap.d_kulou),
                new Pair("困", R.mipmap.d_kun),
                new Pair("懒得理你", R.mipmap.d_landelini),
                new Pair("浪", R.mipmap.d_lang),
                new Pair("累", R.mipmap.d_lei),
                new Pair("喵", R.mipmap.d_miao),
                new Pair("男孩儿", R.mipmap.d_nanhaier),
                new Pair("怒", R.mipmap.d_nu),
                new Pair("怒骂", R.mipmap.d_numa),
                new Pair("女孩儿", R.mipmap.d_nvhaier),
                new Pair("钱", R.mipmap.d_qian),
                new Pair("亲亲", R.mipmap.d_qinqin),
                new Pair("傻眼", R.mipmap.d_shayan),
                new Pair("生病", R.mipmap.d_shengbing),
                new Pair("神兽", R.mipmap.d_shenshou),
                new Pair("失望", R.mipmap.d_shiwang),
                new Pair("帅", R.mipmap.d_shuai),
                new Pair("睡觉", R.mipmap.d_shuijiao),
                new Pair("思考", R.mipmap.d_sikao),
                new Pair("太开心", R.mipmap.d_taikaixin),
                new Pair("摊手", R.mipmap.d_tanshou),
                new Pair("甜", R.mipmap.d_tian),
                new Pair("偷笑", R.mipmap.d_touxiao),
                new Pair("吐", R.mipmap.d_tu),
                new Pair("兔子", R.mipmap.d_tuzi),
                new Pair("挖鼻屎", R.mipmap.d_wabishi),
                new Pair("委屈", R.mipmap.d_weiqu),
                new Pair("污", R.mipmap.d_wu),
                new Pair("笑cry", R.mipmap.d_xiaoku),
                new Pair("熊猫", R.mipmap.d_xiongmao),
                new Pair("嘻嘻", R.mipmap.d_xixi),
                new Pair("嘘", R.mipmap.d_xu),
                new Pair("阴险", R.mipmap.d_yinxian),
                new Pair("疑问", R.mipmap.d_yiwen),
                new Pair("右哼哼", R.mipmap.d_youhengheng),
                new Pair("晕", R.mipmap.d_yun),
                new Pair("抓狂", R.mipmap.d_zhuakuang),
                new Pair("猪头", R.mipmap.d_zhutou),
                new Pair("最右", R.mipmap.d_zuiyou),
                new Pair("左哼哼", R.mipmap.d_zuohengheng)
        };

        final static EmojiManager INSTANCE = new EmojiManager();
    }

    private Map<String, Integer> mEmojiMap;

    private EmojiManager() {
        if (EmojiManagerLoader.INSTANCE != null) {
            throw new IllegalStateException("INSTANCE Already Exist...");
        }

        HashMap<String, Integer> map = new HashMap<>(EmojiManagerLoader.EMOJI_TABLE.length);
        for (Pair<String, Integer> pair : EmojiManagerLoader.EMOJI_TABLE) {
            map.put(pair.first, pair.second);
        }
        mEmojiMap = Collections.unmodifiableMap(map);
    }

    public static EmojiManager getInstance() {
        return EmojiManagerLoader.INSTANCE;
    }

    public Pair[] getEmojiTable() {
        return Arrays.copyOfRange(EmojiManagerLoader.EMOJI_TABLE, 0, EmojiManagerLoader.EMOJI_TABLE.length);
    }
}
