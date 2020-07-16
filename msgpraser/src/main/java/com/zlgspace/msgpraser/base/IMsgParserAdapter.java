package com.zlgspace.msgpraser.base;

public interface IMsgParserAdapter<T,W> {
    CallbackMsg preParser(T msg);
    Object parser(W msg, Class clz);
    /**
     *因为是java module,所以没法办法持有Android Handler,因此这里添加了这个接口
     * 在这里需要使用者自己选择执行到异步线程还是主线程
     * 如果是主线程的话可以使用handler
     */
    boolean dispatchRstToMainThread(Runnable runable);
}
