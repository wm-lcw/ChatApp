package com.example.chatapp.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.example.chatapp.utils.ChatAppLog;
import com.example.chatapp.utils.Constant;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName: ServerChatService
 * @Description: 服务端service
 * @Author: wm
 * @CreateDate: 2023/3/1
 * @UpdateUser: updater
 * @UpdateDate: 2023/3/1
 * @UpdateRemark: 重写服务端连接客户端的逻辑
 * @Version: 1.0
 */
public class ServerChatService extends Service {

    private Context mContext;
    private Handler mHandler;
    private Socket client;
    private String ipAddress;
    private int socketPort;
    private boolean socketConnectState = false;

    private BufferedReader serverIn;
    private PrintWriter serverOut;
    /**
     * 创建线程池
     */
    private ExecutorService threadPool = new ThreadPoolExecutor(4,
            6,
            2L,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(4),
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.DiscardOldestPolicy());
    private static ThreadPoolExecutor tpe;
    private Thread listenThread;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    private IBinder serverBinder = new ServerChatService.ServerBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return serverBinder;
    }

    public class ServerBinder extends Binder {
        public ServerChatService getService(Context context, Handler handler) {
            mContext = context;
            mHandler = handler;
            return ServerChatService.this;
        }
    }

    /**
     * @param
     * @return
     * @version V1.0
     * @Title connectSocket
     * @author wm
     * @createTime 2023/3/6 14:35
     * @description 开始跟客户端连接
     */
    public void connectSocket(Socket clientSocket, PrintWriter serverOut, BufferedReader serverIn) {
        try {
            this.client = clientSocket;
            this.serverOut = serverOut;
            this.serverIn = serverIn;
            if (client == null || serverOut == null || serverIn == null) {
                mHandler.sendEmptyMessage(Constant.MSG_SOCKET_CONNECT_FAIL);
                return;
            }
            //获取客户端输入输出流
            ChatAppLog.debug("serverIn " + serverIn);
            ChatAppLog.debug("serverOut " + serverOut);

            //开启接收信息线程，用于接收客户端的消息
            receiverMessageFromClient();

            //客户连接成功之后发送消息给Activity
            mHandler.sendEmptyMessage(Constant.MSG_SOCKET_CONNECT);
        } catch (Exception e) {
            ChatAppLog.error(e.getMessage());
            mHandler.sendEmptyMessage(Constant.MSG_SOCKET_CONNECT_FAIL);
        }


    }

    /**
     * @param
     * @return
     * @version V1.0
     * @Title receiverMessageFromClient
     * @author wm
     * @createTime 2023/3/6 14:34
     * @description 接收客户端的消息
     */
    public void receiverMessageFromClient() {
        threadPool.execute(() -> {
            try {
                String receiverMessage = "";
                while (true) {
                    if (client == null || client.isClosed()) {
                        //若客户连接已关闭，就退出循环，不再接收当前客户的消息
                        ChatAppLog.debug("receiverMessageFromClient - client is close");
                        mHandler.sendEmptyMessage(Constant.MSG_SOCKET_CLOSE);
                        break;
                    }
                    //这里会阻塞线程
                    receiverMessage = serverIn.readLine();
                    if (receiverMessage == null || "".equals(receiverMessage) || "null".equals(receiverMessage)) {
                        ChatAppLog.debug("------receive : " + receiverMessage);
                        //读取到的消息为空，证明该客户已断开连接
                        mHandler.sendEmptyMessage(Constant.MSG_SOCKET_CLOSE);
                        break;
                    }
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("receiveMessage", receiverMessage);
                    message.what = Constant.MSG_RECEIVE;
                    message.setData(bundle);
                    mHandler.sendMessage(message);
                    ChatAppLog.debug("Server: Received: '" + receiverMessage + "'");
                }
            } catch (Exception e) {
                ChatAppLog.error(e.getMessage());
                mHandler.sendEmptyMessage(Constant.MSG_SOCKET_CLOSE);
            }
        });
    }

    /**
     * @param
     * @return
     * @version V1.0
     * @Title sendMessageToClient
     * @author wm
     * @createTime 2023/3/6 14:34
     * @description 发送信息给客户端
     */
    public void sendMessageToClient(String message) {
        if (!"".equals(message)) {
            if (client == null || client.isClosed()) {
                return;
            }
            threadPool.execute(() -> {
                ChatAppLog.debug("sendTest---" + message);
                serverOut.println(message);
                serverOut.flush();
            });
        }
    }

    /**
     * 关闭客户连接时，需要清空一些变量
     */
    public void closeClient() {
        try {
            if (serverIn != null) {
                //客户端socket 已经关闭的情况，服务器端socket 调用shutdownOutput/shutdownInput 则会出现这个错误
                //java.io.IOException: shutdown failed: ENOTCONN (Transport endpoint is not connected)
//                client.shutdownOutput();
//                client.shutdownInput();
                serverIn.close();
                serverIn = null;
            }
            if (serverOut != null) {
                serverOut.close();
                serverOut = null;
            }
            //加上判断，若socket已断开，就不再重复执行以下操作
            if (client == null || client.isClosed()) {
                client = null;
                return;
            }
            if (client != null) {
                client.close();
                client = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}