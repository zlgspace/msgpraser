package com.zlgspace.msgpraser.demo;

import android.os.Bundle;
import android.util.Log;

import com.zlgspace.msgpraser.annotation.CallbackMethod;
import com.zlgspace.msgpraser.demo.parser.ParserManager;
import com.zlgspace.msgpraser.demo.parser.bean.ABean;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";

    private String msgA = "{" +
            "\"head\":\"A\"," +
            "\"body\":{\"name\":\"张三\"}" +
            "}";

    private String msgB = "{" +
            "\"head\":\"B\"," +
            "\"body\":{\"name\":\"李三\"}" +
            "}";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //这里是通过按钮模拟发送消息
        findViewById(R.id.mABtn).setOnClickListener(view -> {
            ParserManager.getCallback().callback(msgA);
//            MsgParser.sendMsg(A,null);
        });

        findViewById(R.id.mBBtn).setOnClickListener(view -> {
            ParserManager.getCallback().callback(msgB);
//            MsgParser.sendMsg(B,null);
        });

    }

    //必须要通过@CallbackMethod注解标记函数，才能被正确回调，这里是以函数名称为消息号进行回调的示例
    @CallbackMethod
    public void A(ABean a) {
        Log.d(TAG,"rcv A msg");
    }

    //这里是对回调函数绑定了消息号，进行回调示例
    @CallbackMethod("B")
    public void confStart(){
        Log.d(TAG,"rcv B msg");
    }
}
