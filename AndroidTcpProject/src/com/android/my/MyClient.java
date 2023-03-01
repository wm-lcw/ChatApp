package com.android.my;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.*;

/**
 * @author wm
 */
public class MyClient {
    public static final int SERVERPORT = 3333;
    private static Scanner scanner;
    private static Socket mSocket;
    private static PrintWriter mSocketOut;
    private static BufferedReader mSocketIn;

    private static ThreadPoolExecutor tpe;

    public static String receiverMessage, sendMessage;

    /**
     * 创建线程池
     */
    private static ExecutorService threadPool = new ThreadPoolExecutor(4,
            8,
            2L,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(4),
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.DiscardOldestPolicy());


    public static void main(String[] args) {

        tpe = (ThreadPoolExecutor) threadPool;

        //连接服务
        connectService();
    }

    /**
     * 连接服务
     */
    public static void connectService() {
        threadPool.execute(() -> {
            try {
                //10.138.11.251
                //192.168.42.65
                mSocket = new Socket("10.138.11.251", 3333);
                System.out.println("connectService--activityThread size :" + tpe.getActiveCount());
                System.out.println(mSocket);
                //开启输入线程，用于控制发送消息到服务端
                sendMessageAndControlStop();
                //开启接收信息线程，用于接收服务端的消息
                receiverMessageFromServer();
                System.out.println("end--activityThread size :" + tpe.getActiveCount());
            } catch (Exception e) {
                System.out.println("connectService: Error");
                e.printStackTrace();
            }
        });
    }

    /**
     * 接收客户端的消息
     */
    public static void receiverMessageFromServer() {
        threadPool.execute(() -> {
            try {
                mSocketIn = new BufferedReader(
                        new InputStreamReader(mSocket.getInputStream()));
                System.out.println("mSocketIn " + mSocketIn);
                System.out.println("receiverMessageFromClient--activityThread size :" + tpe.getActiveCount());
                while (true) {
                    if (mSocket == null || mSocket.isClosed()) {
                        //若客户连接已关闭，就退出循环
                        System.out.println("receiverMessageFromClient - mSocket is close");
                        closeClient();
                        break;
                    }
                    //当客户端主动关闭客户连接时，这里会阻塞线程
                    receiverMessage = mSocketIn.readLine();
                    if (receiverMessage == null || "".equals(receiverMessage) || "null".equals(receiverMessage)) {
                        System.out.println("------receive : " + receiverMessage);
                        //读取到的消息为空，证明服务器已断开连接，此时输入框仍阻塞，需要手动输入回车结束
                        closeClient();
                        break;
                    }
                    System.out.println("Received: '" + receiverMessage + "'");
                }
            } catch (Exception e) {
                System.out.println("Client Received: Error");
                e.printStackTrace();
            }
        });
    }

    /**
     * 向服务端发送的消息
     */
    public static void sendMessageAndControlStop() {
        threadPool.execute(() -> {
            try {
                scanner = new Scanner(System.in);
                mSocketOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream())), true);
                System.out.println("mSocketOut " + mSocketOut);
                while (true) {
                    //若服务端已关闭，则退出循环，关闭控制台输入监听
                    if (mSocket == null || mSocket.isClosed()) {
                        System.out.println("sendMessageAndControlStop - mSocket is close");
                        closeClient();
                        break;
                    }

                    //当客户端主动断开连接时，这里会阻塞
                    sendMessage = scanner.nextLine();

                    if ("".equals(sendMessage)) {
                        break;
                    } else {
                        if (mSocket != null) {
                            mSocketOut.println(sendMessage);
                            System.out.println("Client : Send : " + sendMessage);
                            mSocketOut.flush();
                        }
                        //将stop消息发送到客户端，便于客户端判断连接状态
                        if ("stop".equals(sendMessage)) {
                            System.out.println("input stop");
                            closeClient();
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Client Send: Error");
                e.printStackTrace();
            }
        });
    }


    /**
     * 关闭客户连接时，需要清空一些变量
     */
    public static void closeClient() {
        try {
            //加上判断，若socket已断开，就不再重复执行以下操作
            if (mSocket.isClosed()) {
                return;
            }
            scanner.close();
            if (mSocketIn != null) {
                mSocket.shutdownOutput();
                mSocket.shutdownInput();
                mSocketIn.close();
            }
            if (mSocketOut != null) {
                mSocketOut.close();
            }
            if (mSocket != null) {
                mSocket.close();
            }
            System.out.println("scanner " + scanner);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
