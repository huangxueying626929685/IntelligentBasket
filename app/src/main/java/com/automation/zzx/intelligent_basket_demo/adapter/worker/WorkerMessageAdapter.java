package com.automation.zzx.intelligent_basket_demo.adapter.worker;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.automation.zzx.intelligent_basket_demo.R;
import com.automation.zzx.intelligent_basket_demo.entity.MessageInfo;

import java.util.List;

/**
 * Created by pengchenghu on 2019/4/8.
 * Author Email: 15651851181@163.com
 * Describe:
 */
public class WorkerMessageAdapter extends RecyclerView.Adapter<WorkerMessageAdapter.ViewHolder>{

    private List<MessageInfo> mMessageInfoList;

    public class ViewHolder extends RecyclerView.ViewHolder{
        View mView;
        TextView tvTime;//消息时间
        LinearLayout llMessage;
        TextView tvTitle;//消息标题
        TextView tvDescription;//消息内容

        public ViewHolder(View itemView) {
            super(itemView);
            // 控件初始化
            mView = itemView;
            tvTime = itemView.findViewById(R.id.tv_message_time);
            llMessage = itemView.findViewById(R.id.ll_message);
            tvTitle = itemView.findViewById(R.id.tv_message_title);
            tvDescription = itemView.findViewById(R.id.tv_message_description);
        }
    }

    public WorkerMessageAdapter(List<MessageInfo> messageInfoList){
        mMessageInfoList = messageInfoList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_message_info,viewGroup,false);
        final ViewHolder holder = new ViewHolder(view);
        holder.llMessage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                MessageInfo mMessageInfo = mMessageInfoList.get(position);
                Toast.makeText(v.getContext(),"RentAdmin报警提示："+mMessageInfo.getmDescription(),Toast.LENGTH_SHORT).show();
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        MessageInfo mMessageInfo = mMessageInfoList.get(i);
        viewHolder.tvTime.setText(mMessageInfo.getmTime());
        viewHolder.tvTitle.setText(mMessageInfo.getmTitle());
        viewHolder.tvDescription.setText(mMessageInfo.getmDescription());
    }

    @Override
    public int getItemCount() {
        return mMessageInfoList.size();
    }
}