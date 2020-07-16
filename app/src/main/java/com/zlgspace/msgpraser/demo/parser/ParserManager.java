package com.zlgspace.msgpraser.demo.parser;


import com.zlgspace.msgpraser.MsgParser;

/**
 * 对MsgParser进行了代理封装
 */
public class ParserManager {

    //这里创建了一个callback 用来模拟收到消息
    private static DemoCallback callback = msg -> MsgParser.parser(msg);

    static{//必须在使用前设置消息解析器
        MsgParser.init(new ParserAdapter());
    }


    public static DemoCallback getCallback() {
        return callback;
    }

    public static void register(Object obj) {
        MsgParser.register(obj);
    }

    public static void unRegister(Object obj) {
        MsgParser.unRegister(obj);
    }
}

