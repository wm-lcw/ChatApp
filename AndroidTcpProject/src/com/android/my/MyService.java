package com.android.my;

import javafx.stage.Screen;

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

    /**
     * 创建线程池
     */
    private static ExecutorService threadPool = new ThreadPoolExecutor(2,
            5,
            2L,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(3),
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.DiscardOldestPolicy());


    public static void main(String[] args) {
        System.out.println("hello world");
        scanner = new Scanner(System.in);

        startService();
    }

    /**
    * 开启服务
    * */
    public static void startService() {
        threadPool.execute(() -> {
            try {
                System.out.println("Server: Connecting...");
                ServerSocket serverSocket = new ServerSocket(SERVERPORT);
                while (true) {
                    client = serverSocket.accept();
                    System.out.println("Server: Receiving...");
                    try {
                        toControlStop();
                        while (true) {
                            if (!clientConnect) {
                                System.out.println("close");
                                break;
                            }
                            BufferedReader in = new BufferedReader(
                                    new InputStreamReader(client.getInputStream()));
                            String str = in.readLine();
                            System.out.println("Server: Received: '" + str + "'");
                        }
                    } catch (Exception e) {
                        System.out.println("Server: Error");
                        e.printStackTrace();
                    } finally {
                        client.close();
                        System.out.println("Server: Done.");
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
    public static void toControlStop() {
        threadPool.execute(() -> {
            try {
                PrintWriter mClientOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);

                while (true) {
                    String str = scanner.nextLine();
                    if ("stop".equals(str)) {
                        System.out.println("input stop");
                        clientConnect = false;
                        break;
                    } else {
                        mClientOut.println(str);
                        mClientOut.flush();
                    }
                }
            } catch (IOException e) {

            }
        });
    }

}
