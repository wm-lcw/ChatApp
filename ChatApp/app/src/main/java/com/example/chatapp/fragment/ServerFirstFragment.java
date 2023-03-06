package com.example.chatapp.fragment;

import static android.content.Context.BIND_AUTO_CREATE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.chatapp.R;
import com.example.chatapp.adapter.SocketAdapter;
import com.example.chatapp.base.BaseFragment;
import com.example.chatapp.base.BasicActivity;
import com.example.chatapp.bean.SocketBean;
import com.example.chatapp.service.ServerListenService;
import com.example.chatapp.utils.ChatAppLog;
import com.example.chatapp.utils.Constant;
import com.example.chatapp.utils.NetWorkUtils;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: ServerFirstFragment
 * @Description: server的首页
 * @Author: wm
 * @CreateDate: 2023/3/4
 * @UpdateUser: updater
 * @UpdateDate: 2023/3/4
 * @UpdateRemark: 更新内容
 * @Version: 1.0
 */
public class ServerFirstFragment extends BaseFragment {

    private Activity mActivity;
    private Context mContext;
    private Button btListen, btStopListen;
    private TextView tvServiceIp;
    private ListView clientList;
    private List<SocketBean> socketBeanList = new ArrayList<>();
    private SocketAdapter socketAdapter;
    private ServerListenService serverListenService;

    private static ServerFirstFragment instance = new ServerFirstFragment();

    public static ServerFirstFragment newInstance() {
        ChatAppLog.debug("" + instance);
        return instance;
    }

    private ServerFirstFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        mContext = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_server_first, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void findViewById(View view) {
        super.findViewById(view);
        tvServiceIp = view.findViewById(R.id.tv_service_ip);
        btListen = view.findViewById(R.id.bt_service_listen);
        btListen.setOnClickListener(mListen);

        btStopListen = view.findViewById(R.id.bt_service_cancel_listen);
        btStopListen.setOnClickListener(mListen);
        clientList = view.findViewById(R.id.lv_client_list);
    }

    @Override
    public void initViewData(View view) {
        super.initViewData(view);
        ChatAppLog.debug();
        Intent bindIntent = new Intent(getActivity(), ServerListenService.class);
        mContext.bindService(bindIntent, connection, BIND_AUTO_CREATE);
        initSocketAdapter();
    }

    /**
     * @param
     * @return
     * @version V1.0
     * @Title initSocketAdapter
     * @author wm
     * @createTime 2023/3/6 14:42
     * @description 初始化并配置SocketAdapter
     */
    private void initSocketAdapter() {
        socketAdapter = new SocketAdapter(mContext, socketBeanList);
        clientList.setAdapter(socketAdapter);
        //对列表中的客户端做点击监听处理
        clientList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(mContext, "选中客户为" + socketBeanList.get(position).getIp(), Toast.LENGTH_SHORT).show();
                if (mActivity != null && mActivity instanceof BasicActivity) {
                    Socket client = socketBeanList.get(position).getSocket();
                    ServerChatFragment serverChatFragment = new ServerChatFragment(client);
                    ((BasicActivity) mActivity).setFragment(ServerFirstFragment.this, R.id.server_fragment_container, serverChatFragment);
                }
            }
        });
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            serverListenService = ((ServerListenService.ServerBinder) service).getService(mContext, mHandler);
            ChatAppLog.debug("" + serverListenService);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    View.OnClickListener mListen = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view == btListen) {
                mHandler.sendEmptyMessage(Constant.MSG_SOCKET_LISTING);
            } else if (view == btStopListen) {
                mHandler.sendEmptyMessage(Constant.MSG_SOCKET_STOP_LISTING);
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
            } else if (msg.what == Constant.MSG_SOCKET_CONNECT) {
                ChatAppLog.debug("client connect success");
            } else if (msg.what == Constant.MSG_SOCKET_CLOSE) {
                ChatAppLog.debug("disconnect!!!");
            } else if (msg.what == Constant.MSG_SOCKET_LISTING) {
                ChatAppLog.debug("MSG_SOCKET_LISTING");
                btListen.setVisibility(View.GONE);
                btStopListen.setVisibility(View.VISIBLE);
                String ipMessage = "WiFi Ip: " + NetWorkUtils.getLocalIPAddress(mContext) + "\nHotspot Ip: " + NetWorkUtils.getHostIp();
                tvServiceIp.setText(ipMessage);
                if (serverListenService != null) {
                    serverListenService.startListen(Constant.TCP_PORT);
                }
            } else if (msg.what == Constant.MSG_SOCKET_STOP_LISTING) {
                ChatAppLog.debug("MSG_SOCKET_STOP_LISTING");
                btListen.setVisibility(View.VISIBLE);
                btStopListen.setVisibility(View.GONE);
                serverListenService.stopListen();
            } else if (msg.what == Constant.MSG_SOCKET_NEW_CLIENT) {
                Socket client = serverListenService.getNewClient();
                ChatAppLog.debug("client " + client);
                //有客户端连接，将客户socket保存到socketBeanList中
                socketBeanList.add(new SocketBean(client, client.getInetAddress().toString()));
                socketAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (connection != null) {
            mContext.unbindService(connection);
        }
    }
}