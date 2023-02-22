package com.example.chatapp.base;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


/**
 * @ClassName: BasicActivity
 * @Description: 基础Activity
 * @Author: wm
 * @CreateDate: 2023/2/20
 * @UpdateUser: updater
 * @UpdateDate: 2023/2/20
 * @UpdateRemark: 更新内容
 * @Version: 1.0
 */
public abstract class BasicActivity extends AppCompatActivity implements UiCallBack {
    /**
     * 快速点击的时间间隔
     */
    private static final int FAST_CLICK_DELAY_TIME = 500;
    /**
     * 最后点击的时间
     */
    private static long lastClickTime;
    /**
     * 上下文参数
     */
    protected Activity context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Activity布局加载前的处理
        initBeforeView(savedInstanceState);
        this.context = this;

        //添加继承这个BaseActivity的Activity
        BasicApplication.getActivityManager().addActivity(this);
        //绑定布局id
        if (getLayoutId() > 0) {
            setContentView(getLayoutId());
        }
        //初始化数据
        initBundleData(savedInstanceState);
    }

    @Override
    public void initBeforeView(Bundle savedInstanceState) {
    }

    @Override
    public void initBundleData(Bundle savedInstanceState) {

    }

    /**
     * 返回
     *
     * @param toolbar
     */
    protected void Back(Toolbar toolbar) {
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.finish();
                if (!isFastClick()) {
                    context.finish();
                }
            }
        });
    }

    /**
     * 两次点击间隔不能少于500ms  防止多次点击
     *
     * @return flag
     */
    protected static boolean isFastClick() {
        boolean flag = true;
        long currentClickTime = System.currentTimeMillis();
        if ((currentClickTime - lastClickTime) >= FAST_CLICK_DELAY_TIME) {
            flag = false;
        }
        lastClickTime = currentClickTime;

        return flag;
    }

    /**
     * 消息提示
     */
    protected void showToash(CharSequence charSequence) {
        Toast.makeText(context, charSequence, Toast.LENGTH_SHORT).show();
    }
}


