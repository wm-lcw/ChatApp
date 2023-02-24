package com.android.my;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.*;

/**
 * @author wm
 */
public class MyService {
    public static final int SERVERPORT = 3333;
    private static Scanner scanner;
    private static boolean clientConnect = true;
    private static Socket client;
    private static PrintWriter clientOut;
    private static BufferedReader clientIn;

    public static ConcurrentHashMap<String, Thread> consoleThreadMap = new ConcurrentHashMap<String, Thread>();
    private static ThreadPoolExecutor tpe;
    private static Thread receiverThread,sendThread;

    public static String receiverMessage,sendMessage;

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
        scanner = new Scanner(System.in);
        tpe = (ThreadPoolExecutor) threadPool;
        startService();
    }

    /**
     * 开启服务
     */
    public static void startService() {
        threadPool.execute(() -> {
            try {
                System.out.println("Server: start");
                ServerSocket serverSocket = new ServerSocket(SERVERPORT);
                //循环监听客户的连接
                while (true) {
                    System.out.println("startService--activityThread size :" + tpe.getActiveCount());
                    //这里会阻塞，直到有客户的连接
                    client = serverSocket.accept();
                    System.out.println(client);
                    System.out.println("Server: Receiving...");
                    //开启输入线程，用于控制发送消息到客户端、控制关闭客户
                    sendMessageAndControlStop();
                    //开启接收信息线程，用于接收客户端的消息
                    receiverMessageFromClient();
                    System.out.println("endService--activityThread size :" + tpe.getActiveCount());
                }
            } catch (Exception e) {
                System.out.println("Server: Error");
                e.printStackTrace();
            }
        });
    }

    /**
     *
    * */
    public static void receiverMessageFromClient() {
        threadPool.execute(() -> {
            try {
                receiverThread = Thread.currentThread();
                clientIn = new BufferedReader(
                        new InputStreamReader(client.getInputStream()));
                System.out.println("connectClient--activityThread size :" + tpe.getActiveCount());
                while (true) {
                    if (client.isClosed()) {
                        //若客户连接已关闭，就退出循环，不再接收当前客户的消息
                        System.out.println("receiverMessageFromClient - client is close");
                        closeClient();
                        break;
                    }
                    //当服务端主动关闭客户连接时，这里会阻塞线程
                    receiverMessage = clientIn.readLine();
                    if (receiverMessage == null || "".equals(receiverMessage) || "null".equals(receiverMessage)) {
                        //读取到的消息为空，证明该客户已断开连接
                        closeClient();
                        break;
                    }
                    System.out.println("Server: Received: '" + receiverMessage + "'");
                }
            } catch (Exception e) {
                System.out.println("Client: Error");
                e.printStackTrace();
            }
        });
    }

    /**
     * 控制停止客户端发送的消息
     */
    public static void sendMessageAndControlStop() {
        threadPool.execute(() -> {
            try {
                sendThread = Thread.currentThread();
                clientOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);
                System.out.println("clientOut " + clientOut );
                while (true) {
                    //若客户端已关闭，则退出循环，关闭控制台输入监听
                    if (client.isClosed()) {
                        System.out.println("sendMessageAndControlStop - client is close");
                        closeClient();
                        break;
                    }
                    //当客户端主动断开连接时，这里会阻塞
                    sendMessage = scanner.nextLine();
                    if ("stop".equals(sendMessage)) {
                        System.out.println("input stop");
                        closeClient();
                        break;
                    } else {
                        clientOut.println(sendMessage);
                        System.out.println("Server : Send : " + sendMessage);
                        clientOut.flush();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }


    /**
     * 关闭客户连接时，需要清空一些变量
     * */
    public static void closeClient() {
        try {
            clientIn.close();
            clientOut.close();
            client.close();
            clientIn = null;
            clientOut = null;
            client = null;
            //需要在这里关闭两个线程，输入消息线程会阻塞---
            sendThread.interrupt();
            receiverThread.interrupt();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
