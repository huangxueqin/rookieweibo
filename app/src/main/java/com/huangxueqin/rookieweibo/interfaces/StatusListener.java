package com.huangxueqin.rookieweibo.interfaces;

/**
 * Created by huangxueqin on 2017/3/12.
 */

public interface StatusListener {
    /**
     * @param action one of {@link com.huangxueqin.rookieweibo.cons.Cons.StatusAction}
     * @param args
     */
    void performAction(int action, Object... args);
}
