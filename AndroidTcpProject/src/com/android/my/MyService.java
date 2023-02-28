package com.android.my;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.Channels;
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
    private static Thread receiverThread, sendThread;

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
                //取消循环监听客户的连接
//                while (true) {
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
//                }
            } catch (Exception e) {
                System.out.println("Server: Error");
                e.printStackTrace();
            }
        });
    }

    /**
     *
     */
    public static void receiverMessageFromClient() {
        threadPool.execute(() -> {
            try {
                receiverThread = Thread.currentThread();
                clientIn = new BufferedReader(
                        new InputStreamReader(client.getInputStream()));
                System.out.println("clientIn " + clientIn);
                System.out.println("receiverMessageFromClient--activityThread size :" + tpe.getActiveCount());
                while (true) {
                    if (client == null || client.isClosed()) {
                        //若客户连接已关闭，就退出循环，不再接收当前客户的消息
                        System.out.println("receiverMessageFromClient - client is close");
                        closeClient();
                        break;
                    }
                    //当服务端主动关闭客户连接时，这里会阻塞线程
                    receiverMessage = clientIn.readLine();
                    if (receiverMessage == null || "".equals(receiverMessage) || "null".equals(receiverMessage)) {
                        System.out.println("------receive : " + receiverMessage);
                        //读取到的消息为空，证明该客户已断开连接，此时输入框仍阻塞，需要手动输入回车结束
                        closeClient();
                        break;
                    }
                    System.out.println("Server: Received: '" + receiverMessage + "'");
                }
            } catch (Exception e) {
                System.out.println("Client Received: Error");
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
                scanner = new Scanner(System.in);
                sendThread = Thread.currentThread();
                clientOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);
                System.out.println("clientOut " + clientOut);
                while (true) {
                    //若客户端已关闭，则退出循环，关闭控制台输入监听
                    if (client == null || client.isClosed()) {
                        System.out.println("sendMessageAndControlStop - client is close");
                        closeClient();
                        break;
                    }

                    //当客户端主动断开连接时，这里会阻塞
                    sendMessage = scanner.nextLine();

                    if ("".equals(sendMessage)) {
                        break;
                    } else {
                        if (client != null) {
                            clientOut.println(sendMessage);
                            System.out.println("Server : Send : " + sendMessage);
                            clientOut.flush();
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
            if (client.isClosed()) {
                return;
            }
            scanner.close();
            if (clientIn != null) {
                client.shutdownOutput();
                client.shutdownInput();
                clientIn.close();
            }
            if (clientOut != null) {
                clientOut.close();
            }
            if (client != null) {
                client.close();
            }
            System.out.println("scanner " + scanner);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
