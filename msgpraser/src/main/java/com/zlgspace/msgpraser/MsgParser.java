package com.zlgspace.msgpraser;




import com.zlgspace.msgpraser.base.CallbackMsg;
import com.zlgspace.msgpraser.base.IMsgParserAdapter;

import java.util.HashMap;
import java.util.List;

public class MsgParser {

    private static IMsgParserAdapter mMsgParserAdapter;

    static CallbackDispatcher mDispatcher;

    private static HashMap<Object, List<Executer>> mExecuters = new HashMap<>();

    public static void init( IMsgParserAdapter adapter){
        mMsgParserAdapter = adapter;
        mDispatcher = new CallbackDispatcher();
    }


    public static void register(Object object){
        if(object==null)return;
        if(mExecuters.containsKey(object))
            return;
        List<Executer> list = ClzParser.findExecuter(object);
        mExecuters.put(object,list);
        for(Executer exe:list){
            mDispatcher.addExecuter(exe);
        }
    }

    public static void unRegister(Object object){
        List<Executer> exeList = mExecuters.remove(object);
        if(exeList==null||exeList.isEmpty())
            return;
        for(Executer exe:exeList){
            mDispatcher.rmvExecuter(exe);
        }
        exeList.clear();
    }

    public static<T> void parser(T t){
        if(mMsgParserAdapter == null||mDispatcher==null)
            return;
        CallbackMsg cbkMsg = mMsgParserAdapter.preParser(t);
        mDispatcher.dispatch(cbkMsg,mMsgParserAdapter);
    }
}
