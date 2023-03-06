package com.example.chatapp.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.chatapp.R;
import com.example.chatapp.base.BaseFragment;
import com.example.chatapp.base.BasicActivity;
import com.example.chatapp.bean.Device;
import com.example.chatapp.utils.ChatAppLog;
import com.example.chatapp.utils.DeviceSearcher;

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

    private Button btnTest, btnScanDevice;
    private Activity mActivity;

    private static ClientFirstFragment instance = new ClientFirstFragment();

    public static ClientFirstFragment newInstance() {

        ChatAppLog.debug("" + instance);
        return instance;
    }

    private ClientFirstFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_client_first, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mActivity = getActivity();
    }

    @Override
    public void findViewById(View view) {
        super.findViewById(view);
        btnTest = view.findViewById(R.id.btn_test);
        btnScanDevice = view.findViewById(R.id.btn_scan_service);
    }

    @Override
    public void initViewData(View view) {
        super.initViewData(view);
        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mActivity != null && mActivity instanceof BasicActivity) {
                    ClientChatFragment clientChatFragment = new ClientChatFragment("", "");
                    ((BasicActivity) mActivity).setFragment(ClientFirstFragment.this, R.id.client_fragment_container, clientChatFragment);
                }
            }
        });

        btnScanDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //开始查找设备
                DeviceSearcher.search(onSearchDevice);
            }
        });
    }

    private OnSearchDevice onSearchDevice = new OnSearchDevice();

    private class OnSearchDevice implements DeviceSearcher.OnSearchListener{

        @Override
        public void onSearchStart() {
            ChatAppLog.debug("start search");
        }

        @Override
        public void onSearchedNewOne(Device device) {
            ChatAppLog.debug("find device " + device.getIp());
        }

        @Override
        public void onSearchFinish() {
            ChatAppLog.debug("stop search");
        }
    }

}