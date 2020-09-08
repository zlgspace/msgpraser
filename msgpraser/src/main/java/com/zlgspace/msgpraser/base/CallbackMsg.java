package com.zlgspace.msgpraser.base;

public class CallbackMsg<M> {
    private String mMsgHead;
    private M mMsgBody;

    public String getMsgHead() {
        return mMsgHead;
    }

    public void setMsgHead(String msgHead) {
        this.mMsgHead = msgHead;
    }

    public M getMsgBody() {
        return mMsgBody;
    }

    public void setMsgBody(M msgBody) {
        this.mMsgBody = msgBody;
    }

    @Override
    public String toString() {
        return "["+mMsgHead+","+mMsgBody+"]";
    }
}
