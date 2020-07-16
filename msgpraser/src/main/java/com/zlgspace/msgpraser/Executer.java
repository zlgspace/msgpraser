package com.zlgspace.msgpraser;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;

class Executer {

    private Object mObj;

    private String mMsgId;

    private Method mMethod;

    private final boolean hasParams;

    Executer(Object obj,Method method,String msgId){

        Class paramTypes[] = method.getParameterTypes();
        if(paramTypes==null||paramTypes.length==0)
            hasParams = false;
        else
            hasParams = true;

        System.out.println("Executer:method = "+method.getName()+",mMsgId = "+msgId+",hasParams = "+hasParams);

        if(hasParams&&paramTypes.length>1)
            throw new InvalidParameterException("error: "+method.getName()+" method most one parameter!");

        setObj(obj);
        setMethod(method);
        setMsgId(msgId);
    }

    public Object getObj() {
        return mObj;
    }

    public void setObj(Object mObj) {
        this.mObj = mObj;
    }

    public String getMsgId() {
        return mMsgId;
    }

    public void setMsgId(String mMsgId) {
        this.mMsgId = mMsgId;
    }

    public Method getMethod() {
        return mMethod;
    }

    public void setMethod(Method mMethod) {
        this.mMethod = mMethod;
    }

    public void execut(Object ... obj){
        if(mObj==null) {
            throw new InvalidParameterException("Executer mObj not be null");
        }

        if(mMethod == null){
            throw new InvalidParameterException("Executer mMethod not be null");
        }
        try {
            if(hasParams)
                mMethod.invoke(mObj,obj);
            else
                mMethod.invoke(mObj);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return mMsgId+"-"+mMethod.getName();
    }
}
