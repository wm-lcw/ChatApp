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
    private static Thread outToClientThread;
    private static ThreadPoolExecutor tpe;

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
                    System.out.println("startService--activityThread size :"+tpe.getActiveCount());
                    System.out.println("Server: waiting...");
                    //这里会阻塞，直到有客户的连接
                    client = serverSocket.accept();
                    System.out.println(client);
                    System.out.println("Server: Receiving...");
                    try {
                        //开启输入线程，用于控制发送消息到客户端、控制关闭客户
                        sendMessageAndControlStop();
                        System.out.println("connectClient--activityThread size :"+tpe.getActiveCount());
                        while (true) {
                            if (client.isClosed()) {
                                //若客户连接已关闭，就退出循环，不再接收当前客户的消息
                                System.out.println("client is close");
                                break;
                            }
                            clientIn = new BufferedReader(
                                    new InputStreamReader(client.getInputStream()));
                            String str = clientIn.readLine();
                            if (str == null || "".equals(str) || "null".equals(str)) {
                                //读取到的消息为空，证明该客户已断开连接
                                closeClient();
                                break;
                            }
                            System.out.println("Server: Received: '" + str + "'");
                        }
                    } catch (Exception e) {
                        System.out.println("Client: Error");
                        e.printStackTrace();
                    } finally {
                        closeClient();
                        System.out.println("Client: disconnect.");
                    }
                }
            } catch (Exception e) {
                System.out.println("Server: Error");
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
                outToClientThread = Thread.currentThread();
                clientOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);
                System.out.println("clientOut " + clientOut + "   outThread : " + outToClientThread);
                while (true) {
                    //若客户端已关闭，则退出循环，关闭控制台输入监听
                    if (client.isClosed()) {
                        System.out.println("sendMessageAndControlStop - client is close");
                        break;
                    }
                    String str = scanner.nextLine();
                    if ("stop".equals(str)) {
                        System.out.println("input stop");
                        closeClient();
                        break;
                    } else {
                        clientOut.println(str);
                        System.out.println("Server : Send : " + str);
                        clientOut.flush();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }


    public static void closeClient(){
        try {
            clientIn.close();
            clientOut.close();
            client.close();
            if (!outToClientThread.isInterrupted()){
                //当前活动线程数
                outToClientThread.interrupt();
                System.out.println("outThread is interrupt :"+outToClientThread.isInterrupted());
                System.out.println("closeClient activityThread size :"+tpe.getActiveCount());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
