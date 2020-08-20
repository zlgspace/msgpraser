package com.zlgspace.msgpraser;


/**
 * Created by zl on 2020/8/19.
 */
public abstract class MsgTargetBroker implements Unbinder,Dispatcher{

    protected MsgIdDescription[] rcvMsgIds;

    public MsgIdDescription[] getRcvMsgIds(){
        return rcvMsgIds;
    }

    public MsgIdDescription getMsgIdDescriptionById(String id){
        if(rcvMsgIds==null||rcvMsgIds.length==0)
            return null;
        for(MsgIdDescription msgIdD:rcvMsgIds){
            if(msgIdD.getMsgId().equals(id))
                return msgIdD;
        }
        return null;
    }

    public boolean containsMsg(String msg){
        if(rcvMsgIds==null||rcvMsgIds.length==0)
            return false;
        for(MsgIdDescription msgIdD:rcvMsgIds){
            if(msgIdD.getMsgId().equals(msg))
                return true;
        }
        return false;
    }
}
