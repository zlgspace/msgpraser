package com.zlgspace.msgpraser;

/**
 * Created by zl on 2020/8/19.
 */
public class MsgIdDescription {
    private String msgId;
    private Class bindEntity;
    private boolean hasParams;
    private boolean intercept;

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public Class getBindEntity() {
        return bindEntity;
    }

    public void setBindEntity(Class bindEntity) {
        this.bindEntity = bindEntity;
    }

    public boolean isHasParams() {
        return hasParams;
    }

    public void setHasParams(boolean hasParams) {
        this.hasParams = hasParams;
    }

    public boolean isIntercept() {
        return intercept;
    }

    public void setIntercept(boolean intercept) {
        this.intercept = intercept;
    }
}
