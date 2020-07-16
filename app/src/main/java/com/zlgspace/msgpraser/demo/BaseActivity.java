package com.zlgspace.msgpraser.demo;

import android.os.Bundle;

import com.zlgspace.msgpraser.demo.parser.ParserManager;


public class BaseActivity extends CallbackAdapterActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ParserManager.register(this);
    }


    @Override
    protected void onStop() {
        super.onStop();
        ParserManager.unRegister(this);
    }
}
