package com.example.chatapp.service;

import static com.example.chatapp.activity.ToClientActivity.MSG_RECEIVE;
import static com.example.chatapp.activity.ToClientActivity.MSG_SOCKET_CLOSE;
import static com.example.chatapp.activity.ToClientActivity.MSG_SOCKET_CONNECT;

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
import java.net.ServerSocket;
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
 * @UpdateRemark: 更新内容
 * @Version: 1.0
 */
public class ServerChatService extends Service {

    private Context mContext;
    private Handler mHandler;
    private static Socket client;
    private String ipAddress;
    private int socketPort;
    private boolean socketConnectState = false;

    private BufferedReader serverIn;
    private PrintWriter serverOut;
    private ServerSocket serverSocket;
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

    public void startListen(int port) {
        if (client != null) {
            return;
        }
        socketPort = port;
        threadPool.execute(() -> {
            try {
                listenThread = Thread.currentThread();
                serverSocket = new ServerSocket(socketPort);
                ChatAppLog.debug(""+serverSocket);
                ChatAppLog.debug(""+listenThread);
                tpe = (ThreadPoolExecutor) threadPool;
                ChatAppLog.debug("activityThread size :" + tpe.getActiveCount());
                //这里会阻塞，直到有客户的连接
                client = serverSocket.accept();
                ChatAppLog.debug("" + client);

                //获取服务端输入输出流
                serverOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);
                serverIn = new BufferedReader(new InputStreamReader(client.getInputStream()));
                ChatAppLog.debug("serverIn " + serverIn);
                ChatAppLog.debug("serverOut " + serverOut);

                //获取客户端IP地址
                String clientIp = client.getInetAddress().toString();
                ChatAppLog.debug(clientIp);

                //客户连接成功之后发送消息给Activity
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("clientIp", clientIp);
                message.what = MSG_SOCKET_CONNECT;
                message.setData(bundle);
                mHandler.sendMessage(message);

                //开启接收信息线程，用于接收客户端的消息
                receiverMessageFromClient();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void receiverMessageFromClient() {
        threadPool.execute(() -> {
            try {
                String receiverMessage = "";
                while (true) {
                    if (client == null || client.isClosed()) {
                        //若客户连接已关闭，就退出循环，不再接收当前客户的消息
                        ChatAppLog.debug("receiverMessageFromClient - client is close");
                        closeClient();
                        break;
                    }
                    //这里会阻塞线程
                    receiverMessage = serverIn.readLine();
                    if (receiverMessage == null || "".equals(receiverMessage) || "null".equals(receiverMessage)) {
                        ChatAppLog.debug("------receive : " + receiverMessage);
                        //读取到的消息为空，证明该客户已断开连接
                        closeClient();
                        break;
                    }
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("receiveMessage", receiverMessage);
                    message.what = MSG_RECEIVE;
                    message.setData(bundle);
                    mHandler.sendMessage(message);
                    ChatAppLog.debug("Server: Received: '" + receiverMessage + "'");
                }
            } catch (Exception e) {
                ChatAppLog.error(e.getMessage());
                closeClient();
            }
        });
    }

    public void sendMessageToClient(String message) {
        if (!"".equals(message)) {
            threadPool.execute(() -> {
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
            //加上判断，若socket已断开，就不再重复执行以下操作
            if (client == null || client.isClosed()) {
                return;
            }
            if (serverIn != null) {
                client.shutdownOutput();
                client.shutdownInput();
                serverIn.close();
            }
            if (serverOut != null) {
                serverOut.close();
            }
            if (client != null) {
                client.close();
            }
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void stopListen() {
        try {
            if (listenThread == null){
                return;
            }
            if (!listenThread.isInterrupted()) {
                listenThread.interrupt();
                serverSocket.close();
                serverSocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}