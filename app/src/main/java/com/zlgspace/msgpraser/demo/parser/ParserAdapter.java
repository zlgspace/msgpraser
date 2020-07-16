package com.zlgspace.msgpraser.demo.parser;


import com.zlgspace.msgpraser.base.CallbackMsg;
import com.zlgspace.msgpraser.base.IMsgParserAdapter;
import com.zlgspace.msgpraser.base.MsgParserAdapter;
import com.zlgspace.msgpraser.demo.parser.bean.MessageBean;

public class ParserAdapter extends MsgParserAdapter<String,String> {
    //初始解析，解析消息号和初步的消息体
    @Override
    public CallbackMsg preParser(String msg) {
        MessageBean messageBean = GsonUtils.fromJson(msg, MessageBean.class);
        CallbackMsg callbackMsg = new CallbackMsg();
        callbackMsg.setMsgHead(messageBean.getHead());
        callbackMsg.setMsgBody(messageBean.getBody().toString());
        return callbackMsg;
    }

    //真正的解析，对消息体进行进一步解析
    @Override
    public Object parser(String msg, Class clz) {
        return  GsonUtils.fromJson(msg,clz);
    }
}
