package com.android.my;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author wm
 */
public class MyService implements Runnable{
    public static final int SERVERPORT = 3333;

    public static void main(String[] args) {
        System.out.println("hello world");
        Thread desktopServerThread = new Thread(new MyService());
        desktopServerThread.start();
    }

    @Override
    public void run() {
        try {
            System.out.println("Server: Connecting...");
            ServerSocket serverSocket = new ServerSocket(SERVERPORT);
            while (true) {
                Socket client = serverSocket.accept();
                System.out.println("Server: Receiving...");
                try {
                    while(true){
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
    }
}
