package com.example.chatapp.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import com.example.chatapp.R;
import com.example.chatapp.adapter.DeviceAdapter;
import com.example.chatapp.base.BaseFragment;
import com.example.chatapp.base.BasicActivity;
import com.example.chatapp.bean.Device;
import com.example.chatapp.utils.ChatAppLog;
import com.example.chatapp.utils.DeviceSearcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: ClientFirstFragment
 * @Description: java类作用描述
 * @Author: wm
 * @CreateDate: 2023/3/4
 * @UpdateUser: updater
 * @UpdateDate: 2023/3/4
 * @UpdateRemark: 更新内容
 * @Version: 1.0
 */
public class ClientFirstFragment extends BaseFragment {

    private Context mContext;
    private Activity mActivity;
    private Button btnScanDevice;
    private List<Device> deviceList = new ArrayList<>();
    private DeviceAdapter deviceAdapter;
    private ListView lvServiceList;
    /**
     * deviceMap用于筛选重复搜索到的服务，使用服务端设备UUID作为key，Device作为value
     */
    private Map<String, Device> deviceMap = new HashMap<>();

    @SuppressLint("StaticFieldLeak")
    private static final ClientFirstFragment INSTANCE = new ClientFirstFragment();

    public static ClientFirstFragment newInstance() {
        ChatAppLog.debug("" + INSTANCE);
        return INSTANCE;
    }

    private ClientFirstFragment() {

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

        return inflater.inflate(R.layout.fragment_client_first, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void findViewById(View view) {
        super.findViewById(view);
        btnScanDevice = view.findViewById(R.id.btn_scan_service);
        lvServiceList = view.findViewById(R.id.lv_service_list);

    }

    @Override
    public void initViewData(View view) {
        super.initViewData(view);

        btnScanDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //开始查找设备
                DeviceSearcher.search(onSearchDevice);
            }
        });
        initAdapter();
    }

    /**
     * @param
     * @return
     * @version V1.0
     * @Title initAdapter
     * @author wm
     * @createTime 2023/3/7 11:07
     * @description 初始化Service设备列表的Adapter
     */
    private void initAdapter() {
//        deviceList.add(new Device("192.168.x.x", 3333, "uuid"));
//        deviceList.add(new Device("192.168.x.x", 3333, "uuid"));
//        deviceList.add(new Device("192.168.125.65", 3333, "uuid"));
        deviceAdapter = new DeviceAdapter(mContext, deviceList);
        lvServiceList.setAdapter(deviceAdapter);
        lvServiceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String ip = deviceList.get(position).getIp();
                int port = deviceList.get(position).getPort();
                if (mActivity != null && mActivity instanceof BasicActivity) {
                    ClientChatFragment clientChatFragment = new ClientChatFragment(ip, "");
                    ((BasicActivity) mActivity).setFragment(ClientFirstFragment.this, R.id.client_fragment_container, clientChatFragment);
                }
            }
        });
    }

    private OnSearchDevice onSearchDevice = new OnSearchDevice();

    private class OnSearchDevice implements DeviceSearcher.OnSearchListener {

        @Override
        public void onSearchStart() {
            ChatAppLog.debug("start search");
        }

        @Override
        public void onSearchedNewOne(Device device) {
            String deviceUuid = device.getUuid();
            ChatAppLog.debug("find device IP:" + device.getIp() + "; UUID : " + deviceUuid);
            //使用Map来筛选重复搜索出来的同一服务设备
            if (!deviceMap.containsKey(deviceUuid)) {
                deviceMap.put(deviceUuid, device);
                deviceList.add(device);
                deviceAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onSearchFinish() {
            ChatAppLog.debug("stop search");
        }
    }

}