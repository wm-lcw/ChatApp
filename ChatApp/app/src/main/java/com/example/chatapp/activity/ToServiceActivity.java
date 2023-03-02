package com.example.chatapp.activity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.example.chatapp.adapter.MsgAdapter;
import com.example.chatapp.base.BasicActivity;
import com.example.chatapp.bean.Msg;
import com.example.chatapp.service.ServerChatService;
import com.example.chatapp.utils.ChatAppLog;
import com.example.chatapp.utils.NetWorkUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: ToServiceActivity
 * @Description: 服务端登录界面
 * @Author: wm
 * @CreateDate: 2023/2/21
 * @UpdateUser: updater
 * @UpdateDate: 2023/2/21
 * @UpdateRemark: 更新内容
 * @Version: 1.0
 */
public class ToServiceActivity extends BasicActivity {
    private Context mContext;
    private EditText etPort, etInputMessage;
    private Button btListen, btSendMessage, btBack, btStopListen;
    private LinearLayout llListenUi, llListing, llListenPre;
    private RelativeLayout rlChatUi;
    private TextView tvServiceIp, tvChatIp;
    private String inPutIp, inPutPort;
    private String TCP_IP;
    private final int TCP_PORT = 3333;
    private boolean connectState = false;

    public static final int MSG_SEND = 1;
    public static final int MSG_RECEIVE = 2;
    public static final int MSG_SOCKET_CONNECT = 3;
    public static final int MSG_SOCKET_CONNECT_FAIL = 4;
    public static final int MSG_SOCKET_CLOSE = 5;
    public static final int MSG_SOCKET_LISTING = 6;
    public static final int MSG_SOCKET_STOP_LISTING = 7;

    private ServerChatService serverChatService;
    private List<Msg> msgList = new ArrayList<>();
    private RecyclerView msgRecyclerView;
    private MsgAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        Intent bindIntent = new Intent(ToServiceActivity.this, ServerChatService.class);
        bindService(bindIntent, connection, BIND_AUTO_CREATE);
        initData();
        initAdapter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (connectState) {
            llListenUi.setVisibility(View.GONE);
            rlChatUi.setVisibility(View.VISIBLE);
        } else {
            llListenUi.setVisibility(View.VISIBLE);
            rlChatUi.setVisibility(View.GONE);
            String ipMessage = "WiFi Ip: " + NetWorkUtils.getLocalIPAddress(mContext) + "\nHotspot Ip: " + NetWorkUtils.getHostIp();
            tvServiceIp.setText(ipMessage);
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_to_service;
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

        etPort = findViewById(R.id.et_service_port);
        btListen = findViewById(R.id.bt_service_listen);
        btListen.setOnClickListener(mListen);

        llListenUi = findViewById(R.id.ll_service_listen_ui);
        rlChatUi = findViewById(R.id.rl_service_chat_ui);
        etInputMessage = findViewById(R.id.et_service_input_message);
        btSendMessage = findViewById(R.id.bt_service_send_message);
        btSendMessage.setOnClickListener(mListen);

        btBack = findViewById(R.id.btn_service_back);
        btBack.setOnClickListener(mListen);
        tvChatIp = findViewById(R.id.tv_service_chat_ip);
        tvServiceIp = findViewById(R.id.tv_service_ip);

        llListing = findViewById(R.id.ll_service_listing);
        llListenPre = findViewById(R.id.ll_service_pre);
        btStopListen = findViewById(R.id.bt_service_cancel_listen);
        btStopListen.setOnClickListener(mListen);

    }

    /**
     * @param
     * @return
     * @version V1.0
     * @Title initAdapter
     * @author wm
     * @createTime 2023/3/1 19:25
     * @description 初始化适配器
     */
    private void initAdapter() {
        msgRecyclerView = findViewById(R.id.msg_recycle_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        msgRecyclerView.setLayoutManager(layoutManager);
        adapter = new MsgAdapter(msgList);
        msgRecyclerView.setAdapter(adapter);
        adapter.setOnItemLongClickListener(new MsgAdapter.OnRecyclerItemMessageLongListener() {
            @Override
            public void onItemLongClick(View view, int position) {
                showToash("longClick " + position);
            }
        });
    }
    
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            serverChatService = ((ServerChatService.ServerBinder) service).getService(mContext, mHandler);
            ChatAppLog.debug("" + serverChatService);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    View.OnClickListener mListen = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view == btListen) {
                ChatAppLog.debug("server " + serverChatService);
                if (serverChatService != null) {
                    serverChatService.startListen(TCP_PORT);
                }
                mHandler.sendEmptyMessage(MSG_SOCKET_LISTING);
                //点击开始监听按钮之后隐藏键盘
                hideKeyBoard();
            } else if (view == btSendMessage) {
                mHandler.sendEmptyMessage(MSG_SEND);
                //发送消息之后隐藏键盘
                hideKeyBoard();
            } else if (view == btStopListen) {
                mHandler.sendEmptyMessage(MSG_SOCKET_STOP_LISTING);
            } else if (view == btBack) {
                ChatAppLog.debug("back");
                finish();
            }


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
                showToash("connect fail! check your Network and PORT!");
            } else if (msg.what == MSG_SOCKET_CONNECT) {
                ChatAppLog.debug("client connect success");
                String clientIp = msg.getData().getString("clientIp").trim();
                llListenUi.setVisibility(View.GONE);
                rlChatUi.setVisibility(View.VISIBLE);
                tvChatIp.setText(clientIp);
                connectState = true;
            } else if (msg.what == MSG_SEND) {
                String getInputMessage = etInputMessage.getText().toString().trim();
                ChatAppLog.debug("sendMessage " + getInputMessage);
                //发送消息给服务端
                serverChatService.sendMessageToClient(getInputMessage);
                //刷新聊天框的记录
                Msg msg1 = new Msg(getInputMessage, Msg.TYPE_SEND);
                msgList.add(msg1);
                //当有新消息，刷新RecyclerVeiw的显示
                adapter.notifyItemInserted(msgList.size() - 1);
                //将RecyclerView定位到最后一行
                msgRecyclerView.scrollToPosition(msgList.size() - 1);
                //清空输入框内容
                etInputMessage.setText("");
            } else if (msg.what == MSG_RECEIVE) {
                //收到客户端发送的消息
                String receiverMessage = msg.getData().getString("receiveMessage").trim();
                ChatAppLog.debug("receiveMessage " + receiverMessage);
                Msg msg1 = new Msg(receiverMessage, Msg.TYPE_RECEIVED);
                msgList.add(msg1);
                //当有新消息，刷新RecyclerVeiw的显示
                adapter.notifyItemInserted(msgList.size() - 1);
                //将RecyclerView定位到最后一行
                msgRecyclerView.scrollToPosition(msgList.size() - 1);
            } else if (msg.what == MSG_SOCKET_CLOSE) {
                ChatAppLog.debug("disconnect!!!");
                showToash("连接已断开，请重新连接！");
                //收到服务端中断的信息
                llListenUi.setVisibility(View.VISIBLE);
                rlChatUi.setVisibility(View.GONE);
                serverChatService.closeClient();
                connectState = false;
            } else if (msg.what == MSG_SOCKET_LISTING) {
                showToash("开始监听！");
                btListen.setVisibility(View.GONE);
                btStopListen.setVisibility(View.VISIBLE);
                String ipMessage = "WiFi Ip: " + NetWorkUtils.getLocalIPAddress(mContext) + "\nHotspot Ip: " + NetWorkUtils.getHostIp();
                tvServiceIp.setText(ipMessage);
            } else if (msg.what == MSG_SOCKET_STOP_LISTING) {
                showToash("取消监听！");
                btListen.setVisibility(View.VISIBLE);
                btStopListen.setVisibility(View.GONE);
                serverChatService.stopListen();
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
            if (ToServiceActivity.this.getCurrentFocus() != null) {
                if (ToServiceActivity.this.getCurrentFocus().getWindowToken() != null) {
                    imm.hideSoftInputFromWindow(ToServiceActivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ChatAppLog.debug();
        serverChatService.closeClient();
        if (connection != null) {
            unbindService(connection);
        }
    }

    /**
     * @param
     * @return
     * @version V1.0
     * @Title hideKeyBoard
     * @author wm
     * @createTime 2023/3/1 17:06
     * @description 隐藏键盘
     */
    private void hideKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (ToServiceActivity.this.getCurrentFocus() != null) {
            if (ToServiceActivity.this.getCurrentFocus().getWindowToken() != null) {
                imm.hideSoftInputFromWindow(ToServiceActivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }


}