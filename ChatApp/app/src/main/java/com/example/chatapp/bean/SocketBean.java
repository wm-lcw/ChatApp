package com.example.chatapp.bean;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * @author wm
 * @Classname SocketBean
 * @Description socketBean类
 * @Version 1.0.0
 * @Date 2023/3/5 22:09
 * @Created by wm
 */
public class SocketBean {
    private Socket socket;
    private String ip;

    public SocketBean(Socket socket, String ip) {
        this.socket = socket;
        this.ip = ip;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
