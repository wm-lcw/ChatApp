package com.example.chatapp.utils;

/**
 * @author wm
 * @Classname Constant
 * @Description 常量辅助类
 * @Version 1.0.0
 * @Date 2023/3/2 18:13
 * @Created by wm
 */
public class Constant {
    /**
     * 发送消息
     */
    public static final int MSG_SEND = 1;

    /**
     * 接收消息
     */
    public static final int MSG_RECEIVE = 2;

    /**
     * TCP通信已连接
     */
    public static final int MSG_SOCKET_CONNECT = 3;

    /**
     * TCP通信连接失败
     */
    public static final int MSG_SOCKET_CONNECT_FAIL = 4;

    /**
     * TCP通信已关闭
     */
    public static final int MSG_SOCKET_CLOSE = 5;

    /**
     * TCP服务端正在监听
     */
    public static final int MSG_SOCKET_LISTING = 6;

    /**
     * TCP服务端停止监听
     */
    public static final int MSG_SOCKET_STOP_LISTING = 7;

    /**
     * TCP服务端接收到新的客户链接
     */
    public static final int MSG_SOCKET_NEW_CLIENT = 8;

    /**
     * 撤回消息
     */
    public static final int MSG_SOCKET_REVERT_MESSAGE = 22;

    /**
     * 删除消息
     */
    public static final int MSG_SOCKET_DELETE_MESSAGE = 23;

    /**
     * 复制
     */
    public static final int MSG_SOCKET_COPY_MESSAGE = 24;

    /**
     * 撤回消息的特殊字符
     */
    public static final String MSG__REVERT_MESSAGE_ACTION = "revert.message.action";

    /**
     * 匹配自然数的正则表达式，用于检查撤回的position是否符合下标的规范（自然数）
     */
    public static final String MATCH_POSITION_FORMAT_REGEX = "^(0|[1-9][0-9]*)$";

    /**
     * TCP通信的端口
     */
    public static final int TCP_PORT = 3333;

    /**
     * 用于设备搜索的端口
     */
    public static final int DEVICE_SEARCH_PORT = 8100;
    /**
     * 用于接收命令的端口
     */
    public static final int COMMAND_RECEIVE_PORT = 60001;
    /**
     * 设备搜索次数
     */
    public static final int SEARCH_DEVICE_TIMES = 3;
    /**
     * 搜索的最大设备数量
     */
    public static final int SEARCH_DEVICE_MAX = 250;
    /**
     * 接收超时时间
     */
    public static final int RECEIVE_TIME_OUT = 1000;

    /**
     * udp数据包前缀
     */
    public static final int PACKET_PREFIX = '$';
    /**
     * udp数据包类型：搜索类型
     */
    public static final int PACKET_TYPE_SEARCH_DEVICE_REQ = 0x10;
    /**
     * udp数据包类型：搜索应答类型
     */
    public static final int PACKET_TYPE_SEARCH_DEVICE_RSP = 0x11;
}
