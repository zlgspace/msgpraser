package com.zlgspace.msgpraser;



import com.zlgspace.msgpraser.annotation.CallbackMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ClzParser {

    public static Class getClass(String fullClzName){
        Class clz = null;
        try {
            clz = Class.forName(fullClzName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return clz;
    }


    public static Object loadObj(String clzName){
        try {
            Class<?> objClz = ClzParser.class.getClassLoader().loadClass(clzName);
            Constructor ctor = objClz.getConstructor(new Class[]{});
            return ctor.newInstance(new Object(){});
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Executer> findExecuter(Object object){
        Method methods[] = object.getClass().getDeclaredMethods();
        List<Executer> executers = new ArrayList<>();
        for(Method method : methods){
            Annotation annotation = method.getAnnotation(CallbackMethod.class);
            if(annotation!=null){
                String callbackMsgId = ((CallbackMethod)annotation).value();
                if(callbackMsgId==null||callbackMsgId.trim().length()==0){
                    callbackMsgId = method.getName();
                }
                Executer executer = new Executer(object,method,callbackMsgId);
                executers.add(executer);
            }
        }
        return executers;
    }
}
