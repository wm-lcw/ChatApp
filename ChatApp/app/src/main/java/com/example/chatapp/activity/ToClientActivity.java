package com.example.chatapp.activity;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.chatapp.R;
import com.example.chatapp.base.BasicActivity;
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
 * @ClassName: ToClientActivity
 * @Description: 客户端登录界面/聊天界面
 * @Author: wm
 * @CreateDate: 2023/2/21
 * @UpdateUser: updater
 * @UpdateDate: 2023/2/21
 * @UpdateRemark: 更新内容
 * @Version: 1.0
 */
public class ToClientActivity extends BasicActivity {

    private Context mContext;
    private EditText etIpDress, etPort, etInputMessage;
    private Button btConnect, btSendMessage;
    private LinearLayout llRequestUi;
    private RelativeLayout rlChatUi;
    private TextView tvChatRecord;
    private String inPutIp, inPutPort;
    private String TCP_IP;
    private int TCP_PORT;

    private boolean isConnect = false;

    /**
     * 创建线程池
     */
    ExecutorService threadPool = new ThreadPoolExecutor(3,
            5,
            2L,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(3),
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.DiscardOldestPolicy());

    private Socket mSocket;
    /**
     * 从socket获取输入输出流
     */
    BufferedReader mClientIn;
    PrintWriter mClientOut;

    private static final int MSG_SEND = 1;
    private static final int MSG_RECEIVE = 2;
    private static final int MSG_SOCKET_CONNECT = 3;
    private static final int MSG_SOCKET_CONNECT_FAIL = 4;
    private static final int MSG_SOCKET_CLOSE = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_to_client;
    }

    /**
     * @param
     * @return
     * @version V1.0
     * @Title initData
     * @author wm
     * @createTime 2023/2/21 20:32
     * @description 初始化布局、数据
     */
    private void initData() {
        mContext = this;
        etIpDress = findViewById(R.id.et_ip);
        etPort = findViewById(R.id.et_port);
        btConnect = findViewById(R.id.bt_start);
        btConnect.setOnClickListener(mListen);

        llRequestUi = findViewById(R.id.ll_request_ui);
        rlChatUi = findViewById(R.id.rl_chat_ui);
        tvChatRecord = findViewById(R.id.tv_chat_record);
        etInputMessage = findViewById(R.id.et_input_message);
        btSendMessage = findViewById(R.id.bt_send_message);
        btSendMessage.setOnClickListener(mListen);
    }

    View.OnClickListener mListen = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view == btConnect) {
                inPutIp = etIpDress.getText().toString();
                inPutPort = etPort.getText().toString();
                if (inPutIp == null || "".equals(inPutIp) || "".equals(inPutPort)) {
                    showToash("please check IP or PORT format !!!");
                } else {
                    TCP_IP = inPutIp;
                    TCP_PORT = Integer.parseInt(inPutPort);
                    //暂时将服务器和端口写死
                    TCP_IP = "192.168.141.65";
                    TCP_PORT = 3333;
                    ChatAppLog.debug("ip:" + TCP_IP + ";  port:" + TCP_PORT);
                    toConnectService();
                }
            } else if (view == btSendMessage) {
                ChatAppLog.debug();
                mHandler.sendEmptyMessage(MSG_SEND);
            }


        }
    };

    /**
     * @param
     * @return
     * @version V1.0
     * @Title toConnectService
     * @author wm
     * @createTime 2023/2/21 20:32
     * @description 连接服务器
     */
    private void toConnectService() {
        threadPool.execute(() -> {
            try {
                //指定ip地址和端口号
                mSocket = new Socket(TCP_IP, TCP_PORT);
                //获取输出流、输入流
                mClientIn = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                mClientOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream())), true);
                startReceiverMessage();
                mHandler.sendEmptyMessage(MSG_SOCKET_CONNECT);
            } catch (Exception e) {
                e.printStackTrace();
                ChatAppLog.error(e.toString());
                mHandler.sendEmptyMessage(MSG_SOCKET_CONNECT_FAIL);
                return;
            }
            ChatAppLog.debug("connect success");

        });
    }

    /**
     * @param
     * @return
     * @version V1.0
     * @Title startReceiverMessage
     * @author wm
     * @createTime 2023/2/22 21:24
     * @description 接收服务端的信息
     */
    private void startReceiverMessage() {
        threadPool.execute(() -> {
            ChatAppLog.debug();
            try {
                while (true) {
                    String str = mClientIn.readLine();
                    ChatAppLog.debug("receiver " + str);
                    if (str != null && !"".equals(str)) {
                        Message message = new Message();
                        Bundle bundle = new Bundle();
                        bundle.putString("receiverMessage", str);
                        message.what = MSG_RECEIVE;
                        message.setData(bundle);
                        mHandler.sendMessage(message);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                ChatAppLog.error(e.toString());
            }
        });
    }

    /**
     * @version V1.0
     * @Title
     * @author wm
     * @createTime 2023/2/21 20:33
     * @description 创建Handler，更新UI、发送信息等事务
     * @param
     * @return
     */
    final Handler mHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_SOCKET_CONNECT_FAIL) {
                ChatAppLog.debug("connect fail");
                showToash("connect fail! check your IP and PORT!");
            } else if (msg.what == MSG_SOCKET_CONNECT) {
                ChatAppLog.debug("connect success");
                isConnect = true;
                llRequestUi.setVisibility(View.GONE);
                rlChatUi.setVisibility(View.VISIBLE);

            } else if (msg.what == MSG_SEND) {
                String getInputMessage = etInputMessage.getText().toString().trim();
                ChatAppLog.debug("sendMessage " + getInputMessage);
                if (!"".equals(getInputMessage)) {
                    threadPool.execute(() -> {
                        mClientOut.println(getInputMessage);
                        mClientOut.flush();
                    });
                    ChatAppLog.debug();
                    String temp = tvChatRecord.getText().toString() + "\n\t\t\t\t\t\t\t\t" + getInputMessage;
                    tvChatRecord.setText(temp);
                    etInputMessage.setText("");
                }

            } else if (msg.what == MSG_RECEIVE) {
                String receiverMessage = msg.getData().getString("receiverMessage").trim();
                ChatAppLog.debug(receiverMessage);
                String temp = tvChatRecord.getText().toString() + "\n\t" + receiverMessage;
                tvChatRecord.setText(temp);
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        ChatAppLog.debug();
        if (mSocket != null) {
            closeConnection();
            ChatAppLog.debug("close Socket");
            isConnect = false;
        }
    }

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