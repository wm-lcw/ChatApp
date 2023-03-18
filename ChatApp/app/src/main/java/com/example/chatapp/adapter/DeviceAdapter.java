package com.example.chatapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.chatapp.R;
import com.example.chatapp.bean.Device;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wm
 * @Classname DeviceAdapter
 * @Description 客户端serviceDeviceList适配器
 * @Version 1.0.0
 * @Date 2023/3/7 9:38
 * @Created by wm
 */
public class DeviceAdapter  extends BaseAdapter {

    private Context mContext;
    private List<Device> deviceList = new ArrayList<>();

    public DeviceAdapter(Context mContext, List<Device> deviceList) {
        //赋值前先清空原List
        if (this.deviceList != null){
            this.deviceList.clear();
        }
        this.mContext = mContext;
        this.deviceList = deviceList;
    }

    @Override
    public int getCount() {
        return deviceList.size();
    }

    @Override
    public Object getItem(int position) {
        return deviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.device_item,null);
            viewHolder = new ViewHolder();
            viewHolder.tvServiceDevice = convertView.findViewById(R.id.tv_device_message);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (DeviceAdapter.ViewHolder) convertView.getTag();
        }
        viewHolder.tvServiceDevice.setText("IP : "+deviceList.get(position).getIp() + " \t PORT : "+deviceList.get(position).getPort());
        return convertView;
    }

    static class ViewHolder {
        TextView tvServiceDevice;
    }
}
