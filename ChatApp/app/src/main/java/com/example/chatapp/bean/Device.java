package com.example.chatapp.bean;

/**
 * @ClassName: Device
 * @Description: 局域网中的设备
 * @Author: wm
 * @CreateDate: 2023/3/6
 * @UpdateUser: updater
 * @UpdateDate: 2023/3/6
 * @UpdateRemark: 更新内容
 * @Version: 1.0
 */
public class Device {

    private String ip;

    private int port;

    private String uuid;

    public Device(String ip, int port, String uuid) {
        super();
        this.ip = ip;
        this.port = port;
        this.uuid = uuid;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

}
