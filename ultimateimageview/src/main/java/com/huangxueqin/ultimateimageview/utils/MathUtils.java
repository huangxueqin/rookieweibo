package com.huangxueqin.ultimateimageview.utils;

import static java.lang.Math.ceil;
import static java.lang.Math.floor;
import static java.lang.Math.round;

/**
 * Created by huangxueqin on 2017/6/7.
 */

public class MathUtils {

    int ceilTo(double a) {
        return (int) ceil(a);
    }

    int floorTo(double a) {
        return (int) floor(a);
    }

    int roundTo(double a) {
        return (int) round(a);
    }
}
