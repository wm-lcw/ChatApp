package com.example.chatapp.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.chatapp.R;
import com.example.chatapp.base.BasicActivity;

/**
 * @ClassName: MainActivity
 * @Description: 主页
 * @Author: wm
 * @CreateDate: 2023/2/20
 * @UpdateUser: updater
 * @UpdateDate: 2023/2/20
 * @UpdateRemark: 更新内容
 * @Version: 1.0
 */
public class MainActivity extends BasicActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }
}