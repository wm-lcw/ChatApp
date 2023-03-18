package com.example.chatapp.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.R;
import com.example.chatapp.bean.Msg;

import java.util.List;

/**
 * @author wm
 * @Classname MsgAdapter
 * @Description Msg适配器
 * @Version 1.0.0
 * @Date 2023/3/1 18:09
 * @Created by wm
 */
public class MsgAdapter extends RecyclerView.Adapter<MsgAdapter.ViewHolder> {

    private List<Msg> mMsgList;
    private OnRecyclerItemMessageLongListener mOnItemLong = null;

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        LinearLayout leftLayout;
        LinearLayout rightLayout;
        TextView leftMsg;
        TextView rightMsg;
        ImageView head1;
        ImageView head2;
        //ViewHolder内创建一个OnRecyclerItemMessageLongListener对象
        private OnRecyclerItemMessageLongListener mOnItemInLong = null;

        //创建ViewHolder对象时将全局的OnRecyclerItemMessageLongListener对象（也就是Activity传进来的那个）传进来
        public ViewHolder(View view, OnRecyclerItemMessageLongListener longListener) {
            super(view);
            this.mOnItemInLong = longListener;
            leftLayout = view.findViewById(R.id.left_layout);
            rightLayout = view.findViewById(R.id.right_layout);
            leftMsg = view.findViewById(R.id.left_msg);
            rightMsg = view.findViewById(R.id.right_msg);
            head1 = view.findViewById(R.id.head_left);
            head2 = view.findViewById(R.id.head_right);
            //对下面两个组件进行长按监听
            leftMsg.setOnLongClickListener(this);
            rightMsg.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View view) {
            int position = getAdapterPosition();
            if (mOnItemInLong != null) {
                //这里确保拿到的position是有效的
                if (position != RecyclerView.NO_POSITION) {
                    int messageType = 0;
                    //messageType 用于区分消息来源: 0-->发送的消息； 1-->接收的消息
                    if (view == leftMsg){
                        messageType = 1;
                    }
                    //回调onItemLongClick方法，该方法在Activity中实现
                    mOnItemInLong.onItemLongClick(view, position, messageType);
                }
            }
            return true;
        }

        /**
         *  @version V1.0
         *  @Title setVisibility
         *  @author wm
         *  @createTime 2023/3/2 19:11
         *  @description 设置该Item是否可见
         *  @param
         *  @return 
         */
        public void setVisibility(boolean isVisible){
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) itemView.getLayoutParams();
            if (isVisible){
                params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                params.width = LinearLayout.LayoutParams.MATCH_PARENT;
            } else {
                params.height = 0;
                params.width = 0;
            }
            itemView.setLayoutParams(params);
        }

    }

    public MsgAdapter(List<Msg> msgList) {
        if (mMsgList != null){
            mMsgList.clear();
        }
        mMsgList = msgList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.msg_item, parent, false);
        return new ViewHolder(view, mOnItemLong);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Msg msg = mMsgList.get(position);
        if (msg.getType() == Msg.TYPE_RECEIVED) {
            //如果是收到消息，则显示在左边，将右边布局隐藏
            holder.leftLayout.setVisibility(View.VISIBLE);
            holder.rightLayout.setVisibility(View.GONE);
            holder.leftMsg.setText(msg.getContent());
        } else if (msg.getType() == Msg.TYPE_SEND) {
            holder.rightLayout.setVisibility(View.VISIBLE);
            holder.leftLayout.setVisibility(View.GONE);
            holder.rightMsg.setText(msg.getContent());
        }
        //设置该Item是否可见
        holder.setVisibility(mMsgList.get(position).isVisible());
    }

    @Override
    public int getItemCount() {
        return mMsgList.size();
    }


    /**
     * @param
     * @author wm
     * @version V1.0
     * @Title
     * @createTime 2023/3/2 16:09
     * @description 创建回调接口
     * @return
     */
    public interface OnRecyclerItemMessageLongListener {
        void onItemLongClick(View view, int position, int messageType);
    }

    /**
     * @param
     * @return
     * @version V1.0
     * @Title setOnItemLongClickListener
     * @author wm
     * @createTime 2023/3/2 16:09
     * @description 在Activity端调用，Activity将OnRecyclerItemMessageLongListener对象传进来
     */
    public void setOnItemLongClickListener(OnRecyclerItemMessageLongListener listener) {
        this.mOnItemLong = listener;
    }
}

