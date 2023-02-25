package com.example.chatapp.service;

import static com.example.chatapp.activity.ToClientActivity.MSG_RECEIVE;
import static com.example.chatapp.activity.ToClientActivity.MSG_SOCKET_CONNECT;
import static com.example.chatapp.activity.ToClientActivity.MSG_SOCKET_CONNECT_FAIL;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.example.chatapp.utils.ChatAppLog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName: ClientChatService
 * @Description: 客户端聊天Service
 * @Author: wm
 * @CreateDate: 2023/2/25
 * @UpdateUser: updater
 * @UpdateDate: 2023/2/25
 * @UpdateRemark: 更新内容
 * @Version: 1.0
 */
public class ClientChatService extends Service {

    private Context mContext;
    private Handler mHandler;
    private Socket mSocket;
    private String ipAddress;
    private int socketPort;
    /**
     * 从socket获取输入输出流
     */
    BufferedReader mClientIn;
    PrintWriter mClientOut;
    /**
     * 创建线程池
     */
    ExecutorService threadPool = new ThreadPoolExecutor(4,
            6,
            2L,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(4),
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.DiscardOldestPolicy());

    private IBinder clientBinder = new ClientBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return clientBinder;
    }

    public class ClientBinder extends Binder {
        public ClientChatService getService(Context context, Handler handler) {
            mContext = context;
            mHandler = handler;
            return ClientChatService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * @param
     * @return
     * @version V1.0
     * @Title initSocket
     * @author wm
     * @createTime 2023/2/25 11:47
     * @description 初始化socket
     */
    public void connectSocket(String ip, int port) {
        threadPool.execute(() -> {
            try {
                if (mSocket == null) {
                    this.ipAddress = ip;
                    this.socketPort = port;
                    mSocket = new Socket(ipAddress, socketPort);

                    //获取输入输出流
                    mClientIn = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                    mClientOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream())), true);
                    //正常连接，开启消息接收线程
                    startReceiverMessage();
                    //回传消息给Activity，刷新聊天页面
                    mHandler.sendEmptyMessage(MSG_SOCKET_CONNECT);
                }
            } catch (IOException e) {
                ChatAppLog.error(e.toString());
                //连接错误，关闭所有流和socket
                closeConnection();
                //回传消息给Activity，提示连接失败
                mHandler.sendEmptyMessage(MSG_SOCKET_CONNECT_FAIL);
                return;
            }
        });

    }

    /**
     * @param
     * @return
     * @version V1.0
     * @Title sendMessageToService
     * @author wm
     * @createTime 2023/2/25 11:41
     * @description 给服务端发送消息
     */
    public void sendMessageToService(String getInputMessage) {
        if (!"".equals(getInputMessage)) {
            threadPool.execute(() -> {
                mClientOut.println(getInputMessage);
                mClientOut.flush();
            });
        }
    }


    /**
     * @param
     * @return
     * @version V1.0
     * @Title startReceiverMessage
     * @author wm
     * @createTime 2023/2/25 11:38
     * @description 接收服务端的信息
     */
    private void startReceiverMessage() {
        threadPool.execute(() -> {
            ChatAppLog.debug();
            try {
                while (true) {
                    String str = mClientIn.readLine();
                    if (str != null && !"".equals(str)) {
                        //接收到服务端的消息，发送到Activity，更新到聊天框中
                        Message message = new Message();
                        Bundle bundle = new Bundle();
                        bundle.putString("receiverMessage", str);
                        message.what = MSG_RECEIVE;
                        message.setData(bundle);
                        mHandler.sendMessage(message);
                    }
                }
            } catch (Exception e) {
                ChatAppLog.error(e.toString());
            }
        });
    }

    /**
     * @param
     * @return
     * @version V1.0
     * @Title closeConnection
     * @author wm
     * @createTime 2023/2/25 14:25
     * @description 断开连接时关闭所有的流跟socket
     */
    public void closeConnection() {
        try {
            if (mClientOut != null) {
                mClientOut.close(); //关闭输出流
                mClientOut = null;
            }
            if (mClientIn != null) {
                mClientIn.close(); //关闭输入流
                mClientIn = null;
            }
            if (mSocket != null) {
                mSocket.close();  //关闭socket
                mSocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}