package com.example.chatapp.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.chatapp.R;
import com.example.chatapp.bean.SocketBean;
import com.example.chatapp.utils.ChatAppLog;

import java.util.List;

/**
 * @author wm
 * @Classname SocketAdapter
 * @Description Socket适配器类
 * @Version 1.0.0
 * @Date 2023/3/5 22:11
 * @Created by wm
 */
public class SocketAdapter extends BaseAdapter {

    private Context mContext;
    private List<SocketBean> socketBeanList;
    private int onlineTextColor, offlineTextColor;

    public SocketAdapter(Context context, List<SocketBean> list){
        if (socketBeanList != null){
            socketBeanList.clear();
        }
        mContext = context;
        socketBeanList = list;
    }

    @Override
    public int getCount() {
        return socketBeanList.size();
    }

    @Override
    public Object getItem(int position) {
        return socketBeanList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.socket_item,null);
            viewHolder = new ViewHolder();
            viewHolder.tvSocketIp = convertView.findViewById(R.id.tv_socket_ip);
            viewHolder.tvClientEnable = convertView.findViewById(R.id.tv_client_enable);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.tvSocketIp.setText(socketBeanList.get(position).getIp());
        viewHolder.tvClientEnable.setText(socketBeanList.get(position).isSocketEnable() ? "online" : "offline");

        /**
         *  onlineTextColor = mContext.getResources().getColor(R.color.online);
         *  onlineTextColor = mContext.getResources().getColor(R.color.offline);
         *  viewHolder.tvSocketIp.setTextColor(onlineTextColor);
         *  这个方法设置颜色不起作用，需要用下面的方法
         */

        //根据客户当前的状态来设定字体颜色
        if (socketBeanList.get(position).isSocketEnable()){
            ChatAppLog.debug("isSocketEnable");
            viewHolder.tvClientEnable.setTextColor(Color.parseColor("#018786"));
            viewHolder.tvSocketIp.setTextColor(Color.parseColor("#018786"));
        } else {
            ChatAppLog.debug("disable");
            viewHolder.tvClientEnable.setTextColor(Color.parseColor("#6B6464"));
            viewHolder.tvSocketIp.setTextColor(Color.parseColor("#6B6464"));
        }
        return convertView;
    }

    static class ViewHolder {
        TextView tvSocketIp;
        TextView tvClientEnable;
    }
}
