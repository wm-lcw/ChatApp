package com.example.chatapp.bean;

/**
 * @author wm
 * @Classname Msg
 * @Description 消息bean类，content表示消息的内容，type表示消息的类型，消息类型有两个值，0代表收到的信息，1代表发送的信息
 * @Version 1.0.0
 * @Date 2023/3/1 17:56
 * @Created by wm
 */
public class Msg {
    public static final int TYPE_RECEIVED = 0;
    public static final int TYPE_SEND = 1;

    private String content;
    private int type;

    public Msg(String content, int type) {
        this.content = content;
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public int getType() {
        return type;
    }
}
