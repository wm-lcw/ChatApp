package com.example.chatapp.base;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

import com.example.chatapp.bean.Device;
import com.example.chatapp.bean.SocketBean;

import java.util.ArrayList;
import java.util.List;


/**
 * @author wm
 * @Classname BasicApplication
 * @Description 工程管理
 * @Version 1.0.0
 * @Date 2023/2/20 10:59
 * @Created by wm
 */
public class BasicApplication extends Application {
    private static ActivityManager activityManager;
    private static BasicApplication application;
    private static Context context;

    private List<Device> deviceList = new ArrayList<>();
    private List<SocketBean> socketBeanList = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        //声明Activity管理
        activityManager = new ActivityManager();
        context = getApplicationContext();
        application = this;

    }



    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    public static ActivityManager getActivityManager() {
        return activityManager;
    }

    /**
     * 内容提供器
     */
    public static Context getContext() {
        return context;
    }

    public static BasicApplication getApplication() {
        return application;
    }

    public List<Device> getDeviceList() {
        return deviceList;
    }

    public void setDeviceList(List<Device> deviceList) {
        this.deviceList = deviceList;
    }

    public List<SocketBean> getSocketBeanList() {
        return socketBeanList;
    }

    public void setSocketBeanList(List<SocketBean> socketBeanList) {
        this.socketBeanList = socketBeanList;
    }
}

