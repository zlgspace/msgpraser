package com.zlgspace.msgpraser.demo.parser.bean;

import com.google.gson.JsonObject;

public class MessageBean {

    String head;

    JsonObject body;

    public MessageBean(String head, JsonObject body) {
        this.head = head;
        this.body = body;
    }

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
    }

    public JsonObject getBody() {
        return body;
    }

    public void setBody(JsonObject body) {
        this.body = body;
    }
}
