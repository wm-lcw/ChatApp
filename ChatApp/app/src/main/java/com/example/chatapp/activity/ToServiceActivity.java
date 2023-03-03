package com.example.chatapp.activity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.chatapp.R;
import com.example.chatapp.adapter.MsgAdapter;
import com.example.chatapp.base.BasicActivity;
import com.example.chatapp.bean.Msg;
import com.example.chatapp.service.ServerChatService;
import com.example.chatapp.utils.ChatAppLog;
import com.example.chatapp.utils.Constant;
import com.example.chatapp.utils.NetWorkUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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
    private boolean connectState = false;
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
            public void onItemLongClick(View view, int position, int messageType) {
                //messageType 用于区分消息来源: 0-->发送的消息； 1-->接收的消息，接收的消息不能撤回
                initPopWindow(view, position, messageType);
            }
        });
    }

    /**
     * @param
     * @return
     * @version V1.0
     * @Title initPopWindow
     * @author wm
     * @createTime 2023/3/2 18:40
     * @description 创建长按消息弹出的弹框
     */
    @SuppressLint("NewApi")
    private void initPopWindow(View v, int position, int messageType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_popip, null, false);
        Button btnRevert = (Button) view.findViewById(R.id.btn_revert);
        Button btnDelete = (Button) view.findViewById(R.id.btn_delete);
        Button btnCopy = (Button) view.findViewById(R.id.btn_copy);

        //构造一个PopupWindow，参数依次是加载的View，宽高，是否可获取焦点
        final PopupWindow popWindow = new PopupWindow(view,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

        //设置加载动画
        popWindow.setAnimationStyle(R.anim.anim_pop);

        //这些为了点击非PopupWindow区域，PopupWindow会消失的，如果没有下面的
        //代码的话，你会发现，当你把PopupWindow显示出来了，无论你按多少次后退键
        //PopupWindow并不会关闭，而且退不出程序，加上下述代码可以解决这个问题
        popWindow.setTouchable(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
            popWindow.setTouchInterceptor(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return false;
                    // 这里如果返回true的话，touch事件将被拦截
                    // 拦截后 PopupWindow的onTouchEvent不被调用，这样点击外部区域无法dismiss
                }
            });
        }
        //要为popWindow设置一个背景才有效
        popWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
        int xLocation = 20, yLocation = 10;

        //如果是对方发送的消息，需要隐藏撤回按钮
        btnRevert.setVisibility(messageType == 1 ? View.GONE : View.VISIBLE);

        //设置popupWindow显示的位置，参数依次是参照传进来的组件View，x轴的偏移量，y轴的偏移量
        popWindow.showAsDropDown(v, xLocation, yLocation);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popWindow.dismiss();
                if (v == btnRevert) {
                    ChatAppLog.debug("revert " + position);
                    sendLongClickMessage(Constant.MSG_SOCKET_REVERT_MESSAGE, "revertPosition", position);
                } else if (v == btnDelete) {
                    ChatAppLog.debug("delete " + position);
                    sendLongClickMessage(Constant.MSG_SOCKET_DELETE_MESSAGE, "deletePosition", position);
                } else if (v == btnCopy) {
                    ChatAppLog.debug("copy " + position);
                    sendLongClickMessage(Constant.MSG_SOCKET_COPY_MESSAGE, "copyPosition", position);
                }
            }
        };

        //设置popupWindow里的按钮的事件
        btnRevert.setOnClickListener(onClickListener);
        btnDelete.setOnClickListener(onClickListener);
        btnCopy.setOnClickListener(onClickListener);
    }

    /**
     * @param
     * @return
     * @version V1.0
     * @Title sendLongClickMessage
     * @author wm
     * @createTime 2023/3/3 15:01
     * @description 长按消息：撤回、删除、复制操作发送Message的操作
     */
    private void sendLongClickMessage(int messageType, String key, int position) {
        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putInt(key, position);
        message.setData(bundle);
        message.what = messageType;
        mHandler.sendMessage(message);
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

    /**
     * @version V1.0
     * @Title
     * @author wm
     * @createTime 2023/3/2 18:41
     * @description 监听各组件的点击事件
     * @param
     * @return
     */
    View.OnClickListener mListen = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view == btListen) {
                ChatAppLog.debug("server " + serverChatService);
                if (serverChatService != null) {
                    serverChatService.startListen(Constant.TCP_PORT);
                }
                mHandler.sendEmptyMessage(Constant.MSG_SOCKET_LISTING);
                //点击开始监听按钮之后隐藏键盘
                hideKeyBoard();
            } else if (view == btSendMessage) {
                mHandler.sendEmptyMessage(Constant.MSG_SEND);
                //发送消息之后隐藏键盘
                hideKeyBoard();
            } else if (view == btStopListen) {
                mHandler.sendEmptyMessage(Constant.MSG_SOCKET_STOP_LISTING);
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
        @SuppressLint("NotifyDataSetChanged")
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == Constant.MSG_SOCKET_CONNECT_FAIL) {
                ChatAppLog.debug("connect fail");
                showToash("connect fail! check your Network and PORT!");
            } else if (msg.what == Constant.MSG_SOCKET_CONNECT) {
                ChatAppLog.debug("client connect success");
                String clientIp = msg.getData().getString("clientIp").trim();
                llListenUi.setVisibility(View.GONE);
                rlChatUi.setVisibility(View.VISIBLE);
                tvChatIp.setText(clientIp);
                connectState = true;
            } else if (msg.what == Constant.MSG_SEND) {
                String getInputMessage = etInputMessage.getText().toString().trim();
                ChatAppLog.debug("sendMessage " + getInputMessage);
                //发送消息给服务端
                serverChatService.sendMessageToClient(getInputMessage);
                //刷新聊天框的记录
                Msg msg1 = new Msg(getInputMessage, Msg.TYPE_SEND);
                msgList.add(msg1);
                //当有新消息，刷新RecyclerView的显示
                adapter.notifyItemInserted(msgList.size() - 1);
                //将RecyclerView定位到最后一行
                msgRecyclerView.scrollToPosition(msgList.size() - 1);
                //清空输入框内容
                etInputMessage.setText("");
            } else if (msg.what == Constant.MSG_RECEIVE) {
                //收到客户端发送的消息
                String receiverMessage = msg.getData().getString("receiveMessage").trim();
                ChatAppLog.debug("receiveMessage " + receiverMessage);
                /**
                 * 使用特殊字符+position的形式来组成撤回指令，对方撤回消息时，发送指令；
                 * 接收到消息之后判断是撤回指令还是正常的消息，若是撤回指令，将position对应的消息隐藏（这个position必是对方发送的消息）
                 * 若是正常信息，则添加到消息列表，显示出来
                 * */
                //检查是否是撤回指令
                int checkResult = checkRevertActionFormat(receiverMessage);
                ChatAppLog.debug("checkRevertActionFormat " + checkResult);
                if (checkResult != -1) {
                    //接收到的是撤回指令，对消息进行撤回操作
                    hideMessage(checkResult);
                    showToash("对方撤回了一条消息！");
                } else {
                    Msg msg1 = new Msg(receiverMessage, Msg.TYPE_RECEIVED);
                    msgList.add(msg1);
                }
                //当有新消息，刷新RecyclerView的显示
                adapter.notifyItemInserted(msgList.size() - 1);
                //将RecyclerView定位到最后一行
                msgRecyclerView.scrollToPosition(msgList.size() - 1);
            } else if (msg.what == Constant.MSG_SOCKET_CLOSE) {
                ChatAppLog.debug("disconnect!!!");
                showToash("连接已断开，请重新连接！");
                //收到服务端中断的信息
                llListenUi.setVisibility(View.VISIBLE);
                rlChatUi.setVisibility(View.GONE);
                serverChatService.closeClient();
                connectState = false;
            } else if (msg.what == Constant.MSG_SOCKET_LISTING) {
                showToash("开始监听！");
                btListen.setVisibility(View.GONE);
                btStopListen.setVisibility(View.VISIBLE);
                String ipMessage = "WiFi Ip: " + NetWorkUtils.getLocalIPAddress(mContext) + "\nHotspot Ip: " + NetWorkUtils.getHostIp();
                tvServiceIp.setText(ipMessage);
            } else if (msg.what == Constant.MSG_SOCKET_STOP_LISTING) {
                showToash("取消监听！");
                btListen.setVisibility(View.VISIBLE);
                btStopListen.setVisibility(View.GONE);
                serverChatService.stopListen();
            } else if (msg.what == Constant.MSG_SOCKET_REVERT_MESSAGE) {
                showToash("撤回消息！");
                int revertPosition = msg.getData().getInt("revertPosition");
                //在本机上隐藏撤回的消息
                hideMessage(revertPosition);
                //发送指令给对方，让对方隐藏该信息(信息由指令和撤回消息的下标组成)
                serverChatService.sendMessageToClient(Constant.MSG__REVERT_MESSAGE_ACTION + ":" + revertPosition);
            } else if (msg.what == Constant.MSG_SOCKET_DELETE_MESSAGE) {
                showToash("删除消息！");
                int deletePosition = msg.getData().getInt("deletePosition");
                hideMessage(deletePosition);
            } else if (msg.what == Constant.MSG_SOCKET_COPY_MESSAGE) {
                int copyPosition = msg.getData().getInt("copyPosition");
                String copyMessage = msgList.get(copyPosition).getContent();

                toCopyText(copyMessage);
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

    /**
     * @param hideMessagePosition ：消息的下标
     * @return
     * @version V1.0
     * @Title hideMessage
     * @author wm
     * @createTime 2023/3/3 10:11
     * @description 隐藏消息的操作：删除消息/撤回消息都需要隐藏
     */
    private boolean hideMessage(int hideMessagePosition) {
        if (hideMessagePosition < 0 || hideMessagePosition >= msgList.size()) {
            ChatAppLog.error("hideMessagePosition out of MsgList");
            return false;
        }
        msgList.get(hideMessagePosition).setVisible(false);
        adapter.notifyDataSetChanged();
        adapter.notifyItemInserted(msgList.size() - 1);
        return true;
    }


    /**
     * @param
     * @return
     * @version V1.0
     * @Title checkRevertActionFormat
     * @author wm
     * @createTime 2023/3/3 11:31
     * @description 检查是否是正确的撤回指令
     */
    private int checkRevertActionFormat(String revertMessage) {
        String[] strings = revertMessage.split(":");
        int revertPosition;
        ChatAppLog.debug("string[].length " + strings.length);
        if (strings.length == 2 && strings[0].startsWith(Constant.MSG__REVERT_MESSAGE_ACTION) && Pattern.matches(Constant.MATCH_POSITION_FORMAT_REGEX, strings[1])) {
            //分割字符串只有指令跟下标，且下标在消息队列的范围内
            revertPosition = Integer.parseInt(strings[1]);
            ChatAppLog.debug("revertPosition " + revertPosition);
            if (revertPosition >= 0 && revertPosition < msgList.size()) {
                return revertPosition;
            }
        }
        return -1;
    }

    /**
     * @param
     * @return
     * @version V1.0
     * @Title toCopyText
     * @author wm
     * @createTime 2023/3/3 14:55
     * @description 复制文字
     */
    private void toCopyText(String copyText) {
        ChatAppLog.debug(copyText);
        //获取剪贴板管理器：
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        // 创建普通字符型ClipData
        ClipData mClipData = ClipData.newPlainText("Label", copyText);
        // 将ClipData内容放到系统剪贴板里
        cm.setPrimaryClip(mClipData);
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
}