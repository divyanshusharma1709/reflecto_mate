package com.sgih.dialogactivity;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;

import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter{
    private Context mContext;
    private List<Message> messagesList;


    public RecyclerViewAdapter(Context c, List<Message> mList) {
        mContext = c;
        messagesList = mList;
    }



    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView iconC;
        TextView mViewC;
        ImageView iconU;
        TextView mViewU;

        public MyViewHolder(View view) {
            super(view);
            mViewC = view.findViewById(R.id.message);
            iconC = view.findViewById(R.id.cbi);
            iconU = view.findViewById(R.id.ubi);
            mViewU = view.findViewById(R.id.umessage);
        }
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        return new MyViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        TextView msg = holder.itemView.findViewById(R.id.message);
        ImageView icon = holder.itemView.findViewById(R.id.cbi);
        TextView msgU = holder.itemView.findViewById(R.id.umessage);
        ImageView uicon = holder.itemView.findViewById(R.id.ubi);


        Message message = messagesList.get(position);

        msg.setText(message.getCmessage());
        icon.setImageResource(R.drawable.img);
        uicon.setImageResource(R.drawable.robot);
        msgU.setText(message.getUMessage());
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }
}