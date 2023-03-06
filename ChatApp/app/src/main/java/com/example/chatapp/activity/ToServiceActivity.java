package com.example.chatapp.activity;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.os.Bundle;

import com.example.chatapp.R;
import com.example.chatapp.base.BasicActivity;
import com.example.chatapp.fragment.ServerFirstFragment;

/**
 * @ClassName: ToServiceActivity
 * @Description: 服务端登录界面
 * @Author: wm
 * @CreateDate: 2023/2/21
 * @UpdateUser: updater
 * @UpdateDate: 2023/2/21
 * @UpdateRemark: 将操作移到ServerFirstFragment中
 * @Version: 1.0
 */
public class ToServiceActivity extends BasicActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindFragment();
    }

    private void bindFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        ServerFirstFragment serverFirstFragment = ServerFirstFragment.newInstance();
        fragmentTransaction.add(R.id.server_fragment_container, serverFirstFragment).addToBackStack("");
        fragmentTransaction.commit();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_to_service;
    }

}