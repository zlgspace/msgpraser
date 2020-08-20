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

public class MsgParser {

    private static IMsgParserAdapter mMsgParserAdapter;
    private static final Map<Class<?>, Constructor<? extends MsgTargetBroker>> BINDINGS = new LinkedHashMap<>();
    private static final Map<Class<?>, MsgTargetBroker> BIND_INSTANCES = new LinkedHashMap<>();
    private static MsgDispatcher mMsgDispatcher = new MsgDispatcher();
    private static ExecutorService executorService = Executors.newSingleThreadExecutor();


    public static void init( IMsgParserAdapter adapter){
        mMsgParserAdapter = adapter;
    }

    public static void register(Object object){
        if(object==null)return;
        if(BIND_INSTANCES.containsKey(object.getClass()))
            return;
        Constructor<? extends MsgTargetBroker> constructor = findBindingConstructorForClass(object.getClass());
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
        BIND_INSTANCES.put(object.getClass(),msgTargetBroker);
        mMsgDispatcher.bind(msgTargetBroker);
    }

    public static void unRegister(Object object){
        if(!BIND_INSTANCES.containsKey(object.getClass()))
            return;
        mMsgDispatcher.unbind(BIND_INSTANCES.remove(object.getClass()));
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


    private static Constructor<? extends MsgTargetBroker> findBindingConstructorForClass(Class<?> cls) {
        Constructor<? extends MsgTargetBroker> bindingCtor = BINDINGS.get(cls);
        if (bindingCtor != null || BINDINGS.containsKey(cls)) {
            return bindingCtor;
        }
        String clsName = cls.getName();
        if (clsName.startsWith("android.") || clsName.startsWith("java.")
                || clsName.startsWith("androidx.")) {
            return null;
        }
        try {
            Class<?> bindingClass = cls.getClassLoader().loadClass(clsName + "_CbBroker");
            //noinspection unchecked
            bindingCtor = (Constructor<? extends MsgTargetBroker>) bindingClass.getConstructor(cls);
        } catch (ClassNotFoundException e) {
            bindingCtor = findBindingConstructorForClass(cls.getSuperclass());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Unable to find binding constructor for " + clsName, e);
        }
        BINDINGS.put(cls, bindingCtor);
        return bindingCtor;
    }


}
