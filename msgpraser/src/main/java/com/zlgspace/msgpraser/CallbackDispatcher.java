package com.zlgspace.msgpraser;




import com.zlgspace.msgpraser.base.CallbackMsg;
import com.zlgspace.msgpraser.base.IMsgParserAdapter;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


final class CallbackDispatcher {
    //<MsgID,Method>
    protected HashMap<String, List<Executer>>  mExecuters = new HashMap<>();

    CallbackDispatcher(){
    }

    public void addExecuter(Executer executer){
        if(executer==null)
            return;
        synchronized (mExecuters) {
            List<Executer> executerList = mExecuters.get(executer.getMsgId());
            if(executerList == null) {
                executerList = new ArrayList<>();
                mExecuters.put(executer.getMsgId(),executerList);
            }
            executerList.add(executer);
        }
    }

    public void rmvExecuter(Executer executer){
        if(executer == null)
            return;
        synchronized (mExecuters) {
            List<Executer> executerList = mExecuters.get(executer.getMsgId());
            if(executerList==null||executerList.isEmpty())
                return;
            executerList.remove(executer);
        }
    }

    public void dispatch(CallbackMsg cbkMsg, IMsgParserAdapter adapter){
        synchronized (mExecuters){
            List<Executer> executers = mExecuters.get(cbkMsg.getMsgHead());
            if(executers==null||executers.isEmpty()) {
                return;
            }
            Class parameterTypes[] = executers.get(0).getMethod().getParameterTypes();
            if(parameterTypes!=null&&parameterTypes.length>1)
                throw new InvalidParameterException("callback method most one parameter");
            Class clz = null;
            if(parameterTypes!=null)
                clz = parameterTypes.length==0?null:parameterTypes[0];
            Object rstObj = null;
            if(clz!=null)
                rstObj = adapter.parser(cbkMsg.getMsgBody(),clz);

            final Object rst = rstObj;

            adapter.dispatchRstToMainThread(() -> {
                for(Executer exe:executers){
                    exe.execut(rst);
                }
            });
        }
    }
}
