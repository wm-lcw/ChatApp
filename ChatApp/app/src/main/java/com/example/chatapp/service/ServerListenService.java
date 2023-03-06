package com.example.chatapp.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcelable;

import com.example.chatapp.utils.ChatAppLog;
import com.example.chatapp.utils.Constant;
import com.example.chatapp.utils.DeviceSearchResponser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName: ServerListenService
 * @Description: 服务端监听服务
 * @Author: wm
 * @CreateDate: 2023/3/4
 * @UpdateUser: updater
 * @UpdateDate: 2023/3/4
 * @UpdateRemark: 更新内容
 * @Version: 1.0
 */
public class ServerListenService extends Service {

    private Context mContext;
    private Handler mHandler;
    private ServerSocket serverSocket;
    private static Socket client;
    private boolean isListing = false;


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

    private IBinder serverBinder = new ServerListenService.ServerBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return serverBinder;
    }


    public class ServerBinder extends Binder {
        public ServerListenService getService(Context context, Handler handler) {
            mContext = context;
            mHandler = handler;
            return ServerListenService.this;
        }
    }

    /**
     *  @version V1.0
     *  @Title startListen
     *  @author wm
     *  @createTime 2023/3/4 16:57
     *  @description 开启监听
     *  @param
     *  @return
     */
    public void startListen(int port) {
        ChatAppLog.debug("start listing");
        if (isListing) {
            ChatAppLog.debug("Server is listing...");
            return;
        }
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //开启设备查找反馈
        DeviceSearchResponser.open();
        threadPool.execute(() -> {
            try {
//                while(true){
                    isListing = true;
                    listenThread = Thread.currentThread();
                    ChatAppLog.debug("" + serverSocket);
                    ChatAppLog.debug("" + listenThread);
                    tpe = (ThreadPoolExecutor) threadPool;
                    //这里会阻塞，直到有客户的连接
                    client = serverSocket.accept();
                    ChatAppLog.debug("" + client);
                    Message msg = new Message();
                    msg.what = Constant.MSG_SOCKET_NEW_CLIENT;
                    mHandler.sendMessage(msg);
//                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     *  @version V1.0
     *  @Title getNewClient
     *  @author wm
     *  @createTime 2023/3/4 19:16
     *  @description 获取最新的客户Socket
     *  @param
     *  @return 
     */
    public Socket getNewClient(){
        return client;
    }

    /**
     *  @version V1.0
     *  @Title stopListen
     *  @author wm
     *  @createTime 2023/3/4 16:56
     *  @description 停止监听
     *  @param
     *  @return 
     */
    public void stopListen() {
        ChatAppLog.debug();
        if (listenThread == null) {
            return;
        }
        //关闭设备查找反馈
        DeviceSearchResponser.close();
        if (!listenThread.isInterrupted()) {
            listenThread.interrupt();
        }
    }

    /**
     *  @version V1.0
     *  @Title closeSocket
     *  @author wm
     *  @createTime 2023/3/4 16:56
     *  @description 关闭Socket
     *  @param
     *  @return
     */
    public void closeSocket(){
        try {
            if (listenThread == null) {
                return;
            }
            if (!listenThread.isInterrupted()) {
                listenThread.interrupt();
                client.close();
                serverSocket.close();
                serverSocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        closeSocket();
    }
}