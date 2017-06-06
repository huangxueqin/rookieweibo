package com.huangxueqin.rookieweibo.cons;

import java.util.regex.Pattern;

/**
 * Created by huangxueqin on 2017/3/1.
 */

public class WeiboPattern {
    public static final String REGEX_TOPIC = "#[\\p{Print}\\p{InCJKUnifiedIdeographs}&&[^#]]+#";
    public static final String REGEX_URL = "http://[a-zA-Z0-9+&@#/%?=~_\\\\-|!:,\\\\.;]*[a-zA-Z0-9+&@#/%=~_|]";
    public static final String REGEX_AT = "@[\\w\\p{InCJKUnifiedIdeographs}-]{1,26}";
    public static final String REGEX_EMOTICON = "\\[[0-9a-zA-Z\\u4e00-\\u9fa5]+\\]";

    public static final Pattern PATTERN_TOPIC = Pattern.compile(REGEX_TOPIC);
    public static final Pattern PATTERN_URL = Pattern.compile(REGEX_URL);
    public static final Pattern PATTER_AT = Pattern.compile(REGEX_AT);
    public static final Pattern PATTER_EMOTICON = Pattern.compile(REGEX_EMOTICON);

    public static final String SCHEME_TOPIC = "topic:";
    public static final String SCHEME_URL = "url:";
    public static final String SCHEME_AT = "at:";
}
