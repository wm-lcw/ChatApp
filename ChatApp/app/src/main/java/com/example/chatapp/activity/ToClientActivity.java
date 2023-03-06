package com.example.chatapp.activity;

import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.chatapp.R;
import com.example.chatapp.base.BasicActivity;
import com.example.chatapp.fragment.ClientFirstFragment;
/**
 * @ClassName: ToClientActivity
 * @Description: 客户端主页
 * @Author: wm
 * @CreateDate: 2023/2/21
 * @UpdateUser: updater
 * @UpdateDate: 2023/2/21
 * @UpdateRemark: 将相关操作移植到ClientFirstFragment中
 * @Version: 1.0
 */
public class ToClientActivity extends BasicActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindFragment();
    }

    /**
     * @param
     * @return
     * @version V1.0
     * @Title bindFragment
     * @author wm
     * @createTime 2023/3/6 15:19
     * @description 绑定ClientFirstFragment
     */
    private void bindFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        ClientFirstFragment clientFirstFragment = ClientFirstFragment.newInstance();
        fragmentTransaction.add(R.id.client_fragment_container, clientFirstFragment).addToBackStack("");
        fragmentTransaction.commit();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_to_client;
    }

}