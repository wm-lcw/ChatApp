package com.example.chatapp.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

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

    private ImageView ivClient, ivService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ivClient = findViewById(R.id.iv_client);
        ivService = findViewById(R.id.iv_service);
        ivClient.setOnClickListener(mListen);
        ivService.setOnClickListener(mListen);

    }

    View.OnClickListener mListen = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == ivClient){
                startActivity(new Intent(MainActivity.this,ToClientActivity.class));
            } else if (v == ivService){
                startActivity(new Intent(MainActivity.this,ToServiceActivity.class));
            }
        }
    };

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }
}