package com.example.chatapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.chatapp.R;
import com.example.chatapp.bean.SocketBean;

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

    public SocketAdapter(Context context, List<SocketBean> list){
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
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.tvSocketIp.setText(socketBeanList.get(position).getIp());
        return convertView;
    }

    static class ViewHolder {
        TextView tvSocketIp;
    }
}
