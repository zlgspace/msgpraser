package com.zlgspace.msgpraser;

/**
 * Created by zl on 2020/8/19.
 */
public interface Dispatcher {
    boolean dispatch(String msgId,Object body);
}
