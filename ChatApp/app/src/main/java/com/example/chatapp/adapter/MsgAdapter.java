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
        private OnRecyclerItemMessageLongListener mOnItemInLong = null;

        public ViewHolder(View view, OnRecyclerItemMessageLongListener longListener) {
            super(view);
            this.mOnItemInLong = longListener;
            leftLayout = view.findViewById(R.id.left_layout);
            rightLayout = view.findViewById(R.id.right_layout);
            leftMsg = view.findViewById(R.id.left_msg);
            rightMsg = view.findViewById(R.id.right_msg);
            head1 = view.findViewById(R.id.head_left);
            head2 = view.findViewById(R.id.head_right);
            leftMsg.setOnLongClickListener(this);
            rightMsg.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View view) {
            int position = getAdapterPosition();
            if (mOnItemInLong != null) {
                if (position != RecyclerView.NO_POSITION) {
                    mOnItemInLong.onItemLongClick(view, position);
                }
            }
            return true;
        }

    }

    public MsgAdapter(List<Msg> msgList) {
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
        void onItemLongClick(View view, int position);
    }

    /**
     * @param
     * @return
     * @version V1.0
     * @Title setOnItemLongClickListener
     * @author wm
     * @createTime 2023/3/2 16:09
     * @description 在Activity端调用
     */
    public void setOnItemLongClickListener(OnRecyclerItemMessageLongListener listener) {
        this.mOnItemLong = listener;
    }
}

