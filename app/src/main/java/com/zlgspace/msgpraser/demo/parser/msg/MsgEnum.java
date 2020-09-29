package com.zlgspace.msgpraser.demo.parser.msg;


import com.zlgspace.msgpraser.annotation.BindEntity;
import com.zlgspace.msgpraser.annotation.MessageDescription;
import com.zlgspace.msgpraser.demo.parser.bean.ABean;
import com.zlgspace.msgpraser.demo.parser.bean.BBean;
import com.zlgspace.msgpraser.demo.parser.bean.CBean;
import com.zlgspace.msgpraser.demo.parser.bean.DBean;
import com.zlgspace.msgpraser.demo.parser.bean.EBean;

/**
 * 消息描述类，这里是一个枚举
 * 这个类主要是描述消息与实体类的关系，目前一个消息只能对应一个实体类
 */
//@MessageDescription
public enum MsgEnum {
    @BindEntity(ABean.class) A,
    @BindEntity(BBean.class) B,
    @BindEntity(CBean.class) C,
    @BindEntity(DBean.class) D,
    @BindEntity(EBean.class) E,
    @BindEntity F,

//    @BindEntity(ABean.class)
//    String A = "A";
//    @BindEntity(BBean.class)
//    String B = "B";
//    @BindEntity(CBean.class)
//    String C = "C";
//    @BindEntity(DBean.class)
//    String D = "D";
//    @BindEntity(EBean.class)
//    String E = "E";

}
