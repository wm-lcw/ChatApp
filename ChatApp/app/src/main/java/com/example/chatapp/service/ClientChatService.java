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
    private boolean socketConnectState = false;

    private BufferedReader mClientIn;
    private PrintWriter mClientOut;

    private Thread monitorThread;
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
                    mHandler.sendEmptyMessage(Constant.MSG_SOCKET_CONNECT);
                    //设置连接状态
                    socketConnectState = true;
                }
            } catch (IOException e) {
                ChatAppLog.error(e.toString());
                //连接错误，关闭所有流和socket
                closeConnection();
                //回传消息给Activity，提示连接失败
                mHandler.sendEmptyMessage(Constant.MSG_SOCKET_CONNECT_FAIL);
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
                    String str = "";
                    /**
                     * 使用这种方法会一直在读空内容，result都是0，只有内容不为空时才显示到屏幕上
                     * */
//                    byte[] inputData = new byte[1024];
//                    int result = mSocket.getInputStream().read(inputData, 0, mSocket.getInputStream().available());
//                    ChatAppLog.debug("receive " + result);
//                    if (result > 0){
//                        str = new String(inputData);
//                        ChatAppLog.debug(str);
//                    }

                    str = mClientIn.readLine();
                    if (str == null || "".equals(str)) {
                        mHandler.sendEmptyMessage(Constant.MSG_SOCKET_CLOSE);
                        break;
                    }

                    if ("stop".equals(str)) {
                        ChatAppLog.debug("receive stop");
                        //若接收到的是“stop”，表示是服务端终止了会话
                        mHandler.sendEmptyMessage(Constant.MSG_SOCKET_CLOSE);
                        break;
                    }
                    //接收到服务端的消息，发送到Activity，更新到聊天框中
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("receiverMessage", str);
                    message.what = Constant.MSG_RECEIVE;
                    message.setData(bundle);
                    mHandler.sendMessage(message);
                }
            } catch (Exception e) {
                ChatAppLog.error(e.toString());
                mHandler.sendEmptyMessage(Constant.MSG_SOCKET_CLOSE);
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
            //设置连接状态
            socketConnectState = false;
            //只要关闭一个流，socket就会被关闭
            ChatAppLog.debug("isClose : " + mSocket.isClosed());
            if (mClientOut != null) {
                mClientOut.close(); //关闭输出流
                mClientOut = null;
            }

            if (mClientIn != null) {
                mClientIn.close(); //关闭输入流
                mClientIn = null;
            }
            if (mSocket == null || mSocket.isClosed()) {
                //加上判断，若socket还没初始化或已断开，就不再重复执行以下操作
                mSocket = null;
                return;
            }
            if (mSocket != null) {
                mSocket.close();  //关闭socket
                mSocket = null;
            }
        } catch (IOException e) {
            ChatAppLog.error(e.getMessage());
        }

    }

    /**
     * @param
     * @return
     * @version V1.0
     * @Title monitorClientConnect
     * @author wm
     * @createTime 2023/2/28 13:53
     * @description 开启线程监听连接状态
     */
    public void monitorClientConnect() {
        threadPool.execute(() -> {
            monitorThread = Thread.currentThread();
            while (true) {
                ChatAppLog.debug("isClose : " + mSocket.isClosed());
                if (mSocket.isClosed()) {
                    closeConnection();
                    Message message = new Message();
                    message.what = Constant.MSG_SOCKET_CLOSE;
                    mHandler.sendMessage(message);
                    break;
                }

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    /**
     * @param
     * @return
     * @version V1.0
     * @Title closeMonitorThread
     * @author wm
     * @createTime 2023/2/28 13:56
     * @description 关闭监听线程
     */
    public void closeMonitorThread() {
        try {
            monitorThread.interrupt();
        } catch (Exception e) {
            e.printStackTrace();
            ChatAppLog.error(e.getMessage());
        }
    }
}