package com.example.chatapp.fragment;

import static android.content.Context.BIND_AUTO_CREATE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import android.widget.Toast;

import com.example.chatapp.R;
import com.example.chatapp.activity.ToClientActivity;
import com.example.chatapp.adapter.MsgAdapter;
import com.example.chatapp.base.BaseFragment;
import com.example.chatapp.base.BasicActivity;
import com.example.chatapp.bean.Msg;
import com.example.chatapp.service.ClientChatService;
import com.example.chatapp.utils.ChatAppLog;
import com.example.chatapp.utils.Constant;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @ClassName: ClientChatFragment
 * @Description: 客户端聊天Fragment
 * @Author: wm
 * @CreateDate: 2023/3/4
 * @UpdateUser: updater
 * @UpdateDate: 2023/3/4
 * @UpdateRemark: 更新内容
 * @Version: 1.0
 */
public class ClientChatFragment extends BaseFragment {

    private Button btnClose;
    private Activity mActivity;

    private Context mContext;
    private EditText etInputMessage;
    private Button btSendMessage, btBack;
    private LinearLayout llRequestUi;
    private RelativeLayout rlChatUi;
    private TextView tvChatIp;
    private String TCP_IP;
    private ClientChatService clientChatService;

    private List<Msg> msgList = new ArrayList<>();
    private RecyclerView msgRecyclerView;
    private MsgAdapter adapter;
    private boolean isConnect = false;

    public ClientChatFragment(String ip, String port) {
//        TCP_IP = ip;
        TCP_IP = "192.168.231.65";
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        mContext = getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_client_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //这里接下来会执行findViewById和setViewData两个方法
    }


    @Override
    public void findViewById(View view) {
        super.findViewById(view);
        btnClose = view.findViewById(R.id.btn_close_chat);
        llRequestUi = view.findViewById(R.id.ll_request_ui);
        rlChatUi = view.findViewById(R.id.rl_chat_ui);
        etInputMessage = view.findViewById(R.id.et_input_message);
        btSendMessage = view.findViewById(R.id.bt_send_message);
        btSendMessage.setOnClickListener(mListen);
        btBack = view.findViewById(R.id.btn_close_chat);
        tvChatIp = view.findViewById(R.id.tv_chat_ip);
        btBack.setOnClickListener(mListen);
        msgRecyclerView = view.findViewById(R.id.msg_recycle_view);
    }

    @Override
    public void initViewData(View view) {
        super.initViewData(view);
        initAdapter();
        //启动MusicPlayService服务
        Intent bindIntent = new Intent(mActivity, ClientChatService.class);
        mContext.bindService(bindIntent, connection, BIND_AUTO_CREATE);
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            clientChatService = ((ClientChatService.ClientBinder) service).getService(mContext, mHandler);
            if (clientChatService != null ){
                //获取到service对象之后直接开始连接
                ChatAppLog.debug("ip:" + TCP_IP + ";  port:" + Constant.TCP_PORT);
                clientChatService.connectSocket(TCP_IP, Constant.TCP_PORT);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    /**
     * @param
     * @return
     * @version V1.0
     * @Title initAdapter
     * @author wm
     * @createTime 2023/3/1 19:23
     * @description 初始化适配器
     */
    private void initAdapter() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
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
        Button btnRevert = view.findViewById(R.id.btn_revert);
        Button btnDelete = view.findViewById(R.id.btn_delete);
        Button btnCopy = view.findViewById(R.id.btn_copy);

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

    View.OnClickListener mListen = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view == btSendMessage) {
                ChatAppLog.debug();
                mHandler.sendEmptyMessage(Constant.MSG_SEND);
                //点击发送键之后隐藏键盘
                hideKeyBoard();
            } else if (view == btBack) {
                ChatAppLog.debug("back");
                closeConnection();
                if (mActivity != null && mActivity instanceof BasicActivity) {
                    ((BasicActivity) mActivity).removeFragment(ClientChatFragment.this, ClientFirstFragment.newInstance());
                }
            }


        }
    };

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
        InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (mActivity.getCurrentFocus() != null) {
            if (mActivity.getCurrentFocus().getWindowToken() != null) {
                imm.hideSoftInputFromWindow(mActivity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
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
            if (msg.what == Constant.MSG_SOCKET_CONNECT_FAIL) {
                ChatAppLog.debug("connect fail");
                Toast.makeText(mContext, "connect fail! check your IP and PORT!", Toast.LENGTH_SHORT).show();
            } else if (msg.what == Constant.MSG_SOCKET_CONNECT) {
                ChatAppLog.debug("connect success");
                isConnect = true;
                tvChatIp.setText(TCP_IP);
            } else if (msg.what == Constant.MSG_SEND) {
                String getInputMessage = etInputMessage.getText().toString().trim();
                ChatAppLog.debug("sendMessage " + getInputMessage);
                //发送消息给服务端
                clientChatService.sendMessageToService(getInputMessage);
                //刷新聊天框的记录
                Msg msg1 = new Msg(getInputMessage, Msg.TYPE_SEND);
                msgList.add(msg1);
                //当有新消息，刷新RecyclerVeiw的显示
                adapter.notifyItemInserted(msgList.size() - 1);
                //将RecyclerView定位到最后一行
                msgRecyclerView.scrollToPosition(msgList.size() - 1);
                //清空输入框内容
                etInputMessage.setText("");
            } else if (msg.what == Constant.MSG_RECEIVE) {
                //收到服务端发送的消息
                String receiverMessage = msg.getData().getString("receiverMessage").trim();
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
                    Toast.makeText(mContext, "对方撤回了一条消息！", Toast.LENGTH_SHORT).show();
                } else {
                    Msg msg1 = new Msg(receiverMessage, Msg.TYPE_RECEIVED);
                    msgList.add(msg1);
                }
                //当有新消息，刷新RecyclerVeiw的显示
                adapter.notifyItemInserted(msgList.size() - 1);
                //将RecyclerView定位到最后一行
                msgRecyclerView.scrollToPosition(msgList.size() - 1);
            } else if (msg.what == Constant.MSG_SOCKET_CLOSE) {
                ChatAppLog.debug("disconnect!!!");
                Toast.makeText(mContext, "连接已断开，请重新连接！", Toast.LENGTH_SHORT).show();
                //收到服务端中断的信息
                closeConnection();
            } else if (msg.what == Constant.MSG_SOCKET_REVERT_MESSAGE) {
                Toast.makeText(mContext, "撤回消息！", Toast.LENGTH_SHORT).show();
                int revertPosition = msg.getData().getInt("revertPosition");
                //在本机上隐藏撤回的消息
                hideMessage(revertPosition);
                //发送指令给对方，让对方隐藏该信息(信息由指令和撤回消息的下标组成)
                clientChatService.sendMessageToService(Constant.MSG__REVERT_MESSAGE_ACTION + ":" + revertPosition);
            } else if (msg.what == Constant.MSG_SOCKET_DELETE_MESSAGE) {
                Toast.makeText(mContext, "删除消息！", Toast.LENGTH_SHORT).show();
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
        ClipboardManager cm = (ClipboardManager) mActivity.getSystemService(Context.CLIPBOARD_SERVICE);
        // 创建普通字符型ClipData
        ClipData mClipData = ClipData.newPlainText("Label", copyText);
        // 将ClipData内容放到系统剪贴板里
        cm.setPrimaryClip(mClipData);
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