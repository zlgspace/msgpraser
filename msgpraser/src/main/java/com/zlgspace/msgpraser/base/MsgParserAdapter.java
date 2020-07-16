package com.zlgspace.msgpraser.base;

public abstract class MsgParserAdapter<T,M> implements IMsgParserAdapter<T,M>{

    @Override
    public boolean dispatchRstToMainThread(Runnable runable) {
        runable.run();
        return false;
    }
}
