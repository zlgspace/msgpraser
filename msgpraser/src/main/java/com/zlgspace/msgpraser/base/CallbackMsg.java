package com.zlgspace.msgpraser.base;

public class CallbackMsg {
    private String mMsgHead;
    private String mMsgBody;

    public String getMsgHead() {
        return mMsgHead;
    }

    public void setMsgHead(String msgHead) {
        this.mMsgHead = msgHead;
    }

    public String getMsgBody() {
        return mMsgBody;
    }

    public void setMsgBody(String msgBody) {
        this.mMsgBody = msgBody;
    }

    @Override
    public String toString() {
        return "["+mMsgHead+","+mMsgBody+"]";
    }
}
