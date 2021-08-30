package com.zlgspace.msgpraser;

import com.zlgspace.msgpraser.base.CallbackMsg;
import com.zlgspace.msgpraser.base.IMsgParserAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

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
            //清除已经为空的消息列表
            Set<String> keySet = new HashSet<String>(bindMsgTargetMap.keySet());
            for(String key:keySet){
                if(bindMsgTargetMap.get(key).isEmpty()){
                    bindMsgTargetMap.remove(key);
                }
            }
        }
        System.out.println("bindMsgTargetMap size:"+bindMsgTargetMap.size());
    }

    public void dispatch( CallbackMsg cbkMsg, IMsgParserAdapter adapter){
        String msgHead = cbkMsg.getMsgHead();
        Object msgBody = cbkMsg.getMsgBody();
        synchronized (lock) {
            ArrayList<MsgTargetBroker> list = bindMsgTargetMap.get(msgHead);
            if (list == null || list.isEmpty())
                return;
            MsgIdDescription msgIdD = list.get(0).getMsgIdDescriptionById(msgHead);
            if(msgIdD==null)
                return;
            Object rspObj = null;
            if(msgBody!=null)
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
