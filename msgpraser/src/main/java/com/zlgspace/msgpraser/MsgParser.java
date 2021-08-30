package com.zlgspace.msgpraser;




import com.zlgspace.msgpraser.base.CallbackMsg;
import com.zlgspace.msgpraser.base.IMsgParserAdapter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import sun.rmi.runtime.Log;

public class MsgParser {

    private static IMsgParserAdapter mMsgParserAdapter;
//    private static final Map<Class<?>, Constructor<? extends MsgTargetBroker>> BINDINGS = new LinkedHashMap<>();
//    private static final Map<Class<?>, MsgTargetBroker> BIND_INSTANCES = new LinkedHashMap<>();
    private static final Map<String, Constructor<? extends MsgTargetBroker>> BINDINGS = new LinkedHashMap<>();
    private static final Map<String, MsgTargetBroker> BIND_INSTANCES = new LinkedHashMap<>();
    private static MsgDispatcher mMsgDispatcher = new MsgDispatcher();
    private static ExecutorService executorService = Executors.newSingleThreadExecutor();


    public static void init( IMsgParserAdapter adapter){
        mMsgParserAdapter = adapter;
    }

    public static void register(Object object){
        if(object==null)return;
        String key = findKeyByObj(object);
        if(BIND_INSTANCES.containsKey(key))
            return;
        Constructor<? extends MsgTargetBroker> constructor = findBindingConstructorForClass(object);
        if(constructor == null)
            return;
        MsgTargetBroker msgTargetBroker = null;
        try {
            msgTargetBroker = constructor.newInstance(object);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        if(msgTargetBroker==null)
            return;
        BIND_INSTANCES.put(key,msgTargetBroker);
        mMsgDispatcher.bind(msgTargetBroker);
        System.out.println("register->BIND_INSTANCES SIZE:"+BIND_INSTANCES.size());
    }

    public static void unRegister(Object object){
        String key = findKeyByObj(object);
        if(!BIND_INSTANCES.containsKey(key))
            return;
        mMsgDispatcher.unbind(BIND_INSTANCES.remove(key));
        System.out.println("unRegister->BIND_INSTANCES SIZE:"+BIND_INSTANCES.size());
    }

    public static void sendMsg(String msgId,Object msgBody){
        if(mMsgParserAdapter==null)
            return;
        executorService.execute(() -> mMsgDispatcher.realDispatch(msgId,msgBody,mMsgParserAdapter));
    }

    public static void sendEmptyMsg(String msgId){
        if(mMsgParserAdapter==null)
            return;
        executorService.execute(() -> mMsgDispatcher.realDispatch(msgId,null,mMsgParserAdapter));
    }

    public static<T> void parser(T t){
        if(mMsgParserAdapter == null)
            return;
        CallbackMsg cbkMsg = mMsgParserAdapter.preParser(t);
        executorService.execute(() -> mMsgDispatcher.dispatch(cbkMsg,mMsgParserAdapter));
    }


    private static Constructor<? extends MsgTargetBroker> findBindingConstructorForClass(Object obj) {
        Class cls = obj.getClass();
        String key = findKeyByObj(obj);
        Constructor<? extends MsgTargetBroker> bindingCtor = BINDINGS.get(key);
        if (bindingCtor != null || BINDINGS.containsKey(key)) {
            return bindingCtor;
        }
        String clsName = cls.getName();
        if (clsName.startsWith("android.") || clsName.startsWith("java.")
                || clsName.startsWith("androidx.")) {
            return null;
        }
        try {
            Class<?> bindingClass = cls.getClassLoader().loadClass(clsName + "_CbBroker");
            if(bindingClass==null)
                return null;
            //noinspection unchecked
            bindingCtor = (Constructor<? extends MsgTargetBroker>) bindingClass.getConstructor(cls);
        } catch (ClassNotFoundException e) {
            bindingCtor = findBindingConstructorForClass(cls.getSuperclass());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Unable to find binding constructor for " + clsName, e);
        }
        BINDINGS.put(key, bindingCtor);
        return bindingCtor;
    }


    private static String findKeyByObj(Object object){
        String key = object.getClass().getName()+"_"+object.hashCode();
        return key;
    }
}
