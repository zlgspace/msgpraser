package com.zlgspace.msgpraser;

import com.zlgspace.msgpraser.base.CallbackMsg;
import com.zlgspace.msgpraser.base.IMsgParserAdapter;

import java.util.ArrayList;
import java.util.HashMap;

class MsgDispatcher{

    private static final Object lock = new Object();

    private HashMap<String, ArrayList<MsgTargetBroker>> bindMsgTargetMap = new HashMap<>();


    public void bind(MsgTargetBroker mtb){
        MsgIdDescription msgIds[] = mtb.getRcvMsgIds();
        if(msgIds==null||msgIds.length==0)
            return;
        synchronized (lock){
            for(MsgIdDescription msgIdD:msgIds){
               String id =  msgIdD.getMsgId();
                if(!bindMsgTargetMap.containsKey(id))
                    bindMsgTargetMap.put(id,new ArrayList<MsgTargetBroker>());

                ArrayList<MsgTargetBroker> list = bindMsgTargetMap.get(id);

                if(list.contains(mtb))
                    continue;

                if(msgIdD.isIntercept()){
                    list.add(0,mtb);
                }else {
                    list.add(mtb);
                }
            }
        }
    }

    public void unbind(MsgTargetBroker mtb){
        MsgIdDescription msgIds[] = mtb.getRcvMsgIds();
        if(msgIds==null||msgIds.length==0)
            return;
        synchronized (lock){
            for(MsgIdDescription msgIdD:msgIds){
                String id = msgIdD.getMsgId();
                if(!bindMsgTargetMap.containsKey(id))
                    continue;
                bindMsgTargetMap.get(id).remove(mtb);
            }
        }
    }

    public void dispatch( CallbackMsg cbkMsg, IMsgParserAdapter adapter){
        String msgHead = cbkMsg.getMsgHead();
        String msgBody = cbkMsg.getMsgBody();
        synchronized (lock) {
            ArrayList<MsgTargetBroker> list = bindMsgTargetMap.get(msgHead);
            if (list == null || list.isEmpty())
                return;
            MsgIdDescription msgIdD = list.get(0).getMsgIdDescriptionById(msgHead);
            if(msgIdD==null)
                return;
            Object rspObj = null;
            if(msgBody!=null&&msgBody.length()>0)
                rspObj = adapter.parser(msgBody, msgIdD.getBindEntity());

            final Object rspTempObj = rspObj;

            adapter.dispatchRstToMainThread(() -> {
                for(MsgTargetBroker mtb:list){
                    if(mtb.dispatch(msgHead,rspTempObj)){
                        return;
                    }
                }
            });
        }
    }

    public void realDispatch(String msgId,Object body,IMsgParserAdapter adapter){
        ArrayList<MsgTargetBroker> list = bindMsgTargetMap.get(msgId);
        if (list == null || list.isEmpty())
            return;
        MsgIdDescription msgIdD = list.get(0).getMsgIdDescriptionById(msgId);
        if(msgIdD==null)
            return;
        adapter.dispatchRstToMainThread(() -> {
            for(MsgTargetBroker mtb:list){
                if(mtb.dispatch(msgId,body)){
                    return;
                }
            }
        });
    }
}
