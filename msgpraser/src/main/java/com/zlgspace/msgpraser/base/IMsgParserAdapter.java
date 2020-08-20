package com.zlgspace.msgpraser.base;

public interface IMsgParserAdapter<T,W> {
    CallbackMsg preParser(T msg);
    Object parser(W msg, Class clz);
    boolean dispatchRstToMainThread(Runnable runable);
}
