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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.chatapp.R;
import com.example.chatapp.adapter.SocketAdapter;
import com.example.chatapp.base.BaseFragment;
import com.example.chatapp.base.BasicActivity;
import com.example.chatapp.base.BasicApplication;
import com.example.chatapp.bean.SocketBean;
import com.example.chatapp.service.ServerListenService;
import com.example.chatapp.utils.ChatAppLog;
import com.example.chatapp.utils.Constant;
import com.example.chatapp.utils.NetWorkUtils;

import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private List<SocketBean> socketBeanList;
    private SocketAdapter socketAdapter;
    private ServerListenService serverListenService;
    /**
     * clientMap用于筛选重复连接的客户，使用IP作为key，Socket作为value
     */
    private final Map<String, Socket> clientMap = new HashMap<>();

    private static ServerFirstFragment instance = new ServerFirstFragment();
    private int currentPosition = 0;

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
        socketBeanList = BasicApplication.getApplication().getSocketBeanList();

        //fragment里面的onResume,onPause这些方法,只有在他依赖的activity进入相应的生命周期的时候才会调用的．fragment之间的切换是不会调用的
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

        String ipMessage = "WiFi Ip: " + NetWorkUtils.getLocalIPAddress(mContext) + "\nHotspot Ip: " + NetWorkUtils.getHostIp();
        tvServiceIp.setText(ipMessage);

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
//        Socket testSocket = new Socket();
//        socketBeanList.add(new SocketBean(testSocket,"192.168.x.x"));
//        socketBeanList.add(new SocketBean(testSocket,"192.168.x.x"));
//        socketBeanList.add(new SocketBean(testSocket,"192.168.x.x"));
        socketAdapter = new SocketAdapter(mContext, socketBeanList);
        clientList.setAdapter(socketAdapter);
        //对列表中的客户端做点击监听处理
        clientList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (socketBeanList.get(position).isSocketEnable()) {
                    showToast("选中客户为" + socketBeanList.get(position).getIp());
                    if (mActivity != null && mActivity instanceof BasicActivity) {
                        ServerChatFragment serverChatFragment = new ServerChatFragment(socketBeanList.get(position).getSocket());
                        currentPosition = position;
                        ((BasicActivity) mActivity).setFragment(ServerFirstFragment.this, R.id.server_fragment_container, serverChatFragment);
                        serverChatFragment.setRefreshCallBack(new RefreshClientListCallBack());
                    }
                } else {
                    showToast("该客户已离线！");
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
                String newClientIp = client.getInetAddress().toString();
                //使用Map来筛选重复连接的客户
                if (!clientMap.containsKey(newClientIp)) {
                    //有客户端连接，将客户socket保存到clientMap和socketBeanList中
                    clientMap.put(newClientIp, client);
                    socketBeanList.add(new SocketBean(client, newClientIp, !client.isClosed()));
                    socketAdapter.notifyDataSetChanged();
                    showToast("客户: " + newClientIp + "请求连接！");
                } else {
                    //若该用户已存在，重新连接时提示为上线状态
                    clientMap.put(newClientIp, client);
                    int i = findClientPosition(newClientIp);
                    if (i >= 0 && i < socketBeanList.size()) {
                        socketBeanList.remove(i);
                        socketBeanList.add(new SocketBean(client, newClientIp, !client.isClosed()));
                        socketAdapter.notifyDataSetChanged();
                        showToast("客户: " + newClientIp + "已上线！");
                    }
                }
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

    /**
     * @param
     * @author wm
     * @version V1.0
     * @Title
     * @createTime 2023/3/8 19:27
     * @description 实现回调接口，更新客户列表的可用状态
     * @return
     */
    private class RefreshClientListCallBack implements ServerChatFragment.RefreshClientListListener {
        @Override
        public void refreshList(boolean enable) {
            ChatAppLog.debug();
            socketBeanList.get(currentPosition).setSocketEnable(enable);
            socketAdapter.notifyDataSetChanged();
        }
    }

    /**
     * @param
     * @return
     * @version V1.0
     * @Title findClientPosition
     * @author wm
     * @createTime 2023/3/18 14:08
     * @description 查找客户列表中是否已存在该客户，已存在则返回下标
     */
    private int findClientPosition(String ip) {
        SocketBean socketBean;
        for (int i = 0; i < socketBeanList.size(); i++) {
            socketBean = socketBeanList.get(i);
            if (socketBean.getIp().equals(ip)) {
                return i;
            }
        }
        return -1;
    }

}