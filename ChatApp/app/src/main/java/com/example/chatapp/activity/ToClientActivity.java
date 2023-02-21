package com.example.chatapp.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
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
    private String ip, port;
    /**
     * 创建线程池
     * */
    ExecutorService threadPool = new ThreadPoolExecutor(1,
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
     *  @version V1.0
     *  @Title initData
     *  @author wm
     *  @createTime 2023/2/21 20:32
     *  @description 初始化布局、数据
     *  @param
     *  @return
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
//            ip = etIpDress.getText().toString();
//            port = etPort.getText().toString();
                //暂时将服务器和端口写死
//                ip = "192.88.1.32";
                ip = "192.168.18.65";
                ChatAppLog.debug("ip:" + ip + ";  port:" + port);
                toConnectService();
            } else if (view == btSendMessage) {
                ChatAppLog.debug();
                mHandler.sendEmptyMessage(MSG_SEND);
            }


        }
    };

    /**
     *  @version V1.0
     *  @Title toConnectService
     *  @author wm
     *  @createTime 2023/2/21 20:32
     *  @description 连接服务器
     *  @param
     *  @return
     */
    private void toConnectService() {
        threadPool.execute(() -> {
            try {
                //指定ip地址和端口号
                mSocket = new Socket(ip, 3333);
                //获取输出流、输入流
                mClientIn = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                mClientOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream())), true);
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
     *  @version V1.0
     *  @Title
     *  @author wm
     *  @createTime 2023/2/21 20:33
     *  @description 创建Handler，更新UI、发送信息等事务
     *  @param
     *  @return
     */
    final Handler mHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_SOCKET_CONNECT_FAIL) {
                ChatAppLog.debug("connect fail");
            } else if (msg.what == MSG_SOCKET_CONNECT) {
                ChatAppLog.debug("connect success");
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
                    String temp = tvChatRecord.getText().toString() + "\n" + getInputMessage;
                    tvChatRecord.setText(temp);
                    etInputMessage.setText("");
                }

            }
        }
    };
}