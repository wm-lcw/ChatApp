package com.example.chatapp.activity;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.chatapp.R;
import com.example.chatapp.base.BasicActivity;
import com.example.chatapp.service.ClientChatService;
import com.example.chatapp.utils.ChatAppLog;

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
    private Button btConnect, btSendMessage, btBack;
    private LinearLayout llRequestUi;
    private RelativeLayout rlChatUi;
    private TextView tvChatIp,tvChatRecord;
    private String inPutIp, inPutPort;
    private String TCP_IP;
    private int TCP_PORT;
    private ClientChatService clientChatService;

    private boolean isConnect = false;

    public static final int MSG_SEND = 1;
    public static final int MSG_RECEIVE = 2;
    public static final int MSG_SOCKET_CONNECT = 3;
    public static final int MSG_SOCKET_CONNECT_FAIL = 4;
    public static final int MSG_SOCKET_CLOSE = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        //启动MusicPlayService服务
        Intent bindIntent = new Intent(ToClientActivity.this, ClientChatService.class);
        bindService(bindIntent, connection, BIND_AUTO_CREATE);
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

        btBack = findViewById(R.id.btn_back);
        tvChatIp = findViewById(R.id.tv_chat_ip);
        btBack.setOnClickListener(mListen);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isConnect) {
            llRequestUi.setVisibility(View.GONE);
            rlChatUi.setVisibility(View.VISIBLE);
        } else {
            llRequestUi.setVisibility(View.VISIBLE);
            rlChatUi.setVisibility(View.GONE);
        }
    }

    View.OnClickListener mListen = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view == btConnect) {
                inPutIp = etIpDress.getText().toString().trim();
                inPutPort = etPort.getText().toString();
                //暂时删除PORT端口的输入及判断，暂时写死为3333
                if (inPutIp == null || "".equals(inPutIp)) {
                    showToash("please check IP or PORT format !!!");
                } else {
                    TCP_IP = inPutIp;
//                    TCP_PORT = Integer.parseInt(inPutPort);
                    TCP_PORT = 3333;
                    ChatAppLog.debug("ip:" + TCP_IP + ";  port:" + TCP_PORT);
                    clientChatService.connectSocket(TCP_IP, TCP_PORT);
                }
                //点击连接按钮之后隐藏键盘
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (ToClientActivity.this.getCurrentFocus() != null) {
                    if (ToClientActivity.this.getCurrentFocus().getWindowToken() != null) {
                        imm.hideSoftInputFromWindow(ToClientActivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                }
            } else if (view == btSendMessage) {
                ChatAppLog.debug();
                mHandler.sendEmptyMessage(MSG_SEND);
            } else if (view == btBack) {
                ChatAppLog.debug("back");
                closeConnection();
                finish();
            }


        }
    };

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            clientChatService = ((ClientChatService.ClientBinder) service).getService(mContext, mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

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
                tvChatIp.setText(TCP_IP);
//                clientChatService.monitorClientConnect();
            } else if (msg.what == MSG_SEND) {
                String getInputMessage = etInputMessage.getText().toString().trim();
                ChatAppLog.debug("sendMessage " + getInputMessage);
                //发送消息给服务端
                clientChatService.sendMessageToService(getInputMessage);
                //刷新聊天框的记录
                String temp = tvChatRecord.getText().toString() + "\n\t\t\t\t\t\t\t\t\t\t\t\t" + getInputMessage;
                tvChatRecord.setText(temp);
                etInputMessage.setText("");
            } else if (msg.what == MSG_RECEIVE) {
                //收到服务端发送的消息
                String receiverMessage = msg.getData().getString("receiverMessage").trim();
                ChatAppLog.debug("receiveMessage "+receiverMessage);
                String temp = tvChatRecord.getText().toString() + "\n\t" + receiverMessage;
                tvChatRecord.setText(temp);
            } else if (msg.what == MSG_SOCKET_CLOSE) {
                ChatAppLog.debug("disconnect!!!");
                showToash("连接已断开，请重新连接！");
                //收到服务端中断的信息
                closeConnection();
                llRequestUi.setVisibility(View.VISIBLE);
                rlChatUi.setVisibility(View.GONE);
            }
        }
    };

    /**
     * 点击空白区域隐藏键盘.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (ToClientActivity.this.getCurrentFocus() != null) {
                if (ToClientActivity.this.getCurrentFocus().getWindowToken() != null) {
                    imm.hideSoftInputFromWindow(ToClientActivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ChatAppLog.debug();
    }

    @Override
    protected void onStop() {
        super.onStop();
        ChatAppLog.debug();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ChatAppLog.debug();
        closeConnection();
        if (connection != null) {
            unbindService(connection);
        }
    }

    /**
     * @param
     * @return
     * @version V1.0
     * @Title closeConnection
     * @author wm
     * @createTime 2023/2/27 15:57
     * @description 关闭连接
     */
    private void closeConnection() {
        ChatAppLog.debug("");
        clientChatService.closeConnection();
        isConnect = false;
//        clientChatService.closeMonitorThread();
    }
}