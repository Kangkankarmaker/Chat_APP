package com.kangkan.chatapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kangkan.chatapp.MessageActivity;
import com.kangkan.chatapp.Model.Chat;
import com.kangkan.chatapp.Model.User;
import com.kangkan.chatapp.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.viewHolder> {


    public static final int MSG_TYPE_LEFT =0;
    public static final int MSG_TYPE_RIGHT =1;

    private Context mContext;
    private List<Chat> mChat;
    private String imgURL;

    FirebaseUser fuser;

    public MessageAdapter(Context mContext, List<Chat> mChat, String imgURL) {
        this.mContext = mContext;
        this.mChat = mChat;
        this.imgURL = imgURL;
    }

    @NonNull
    @Override
    public MessageAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == MSG_TYPE_RIGHT) {
            View view= LayoutInflater.from(mContext).inflate(R.layout.chat_item_right,parent,false);
            return new MessageAdapter.viewHolder(view);
        }
        else {
            View view= LayoutInflater.from(mContext).inflate(R.layout.chat_item_left,parent,false);
            return new MessageAdapter.viewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.viewHolder holder, int position) {

        Chat chat=mChat.get(position);

        holder.show_msg.setText(chat.getMessage());

        if (imgURL.equals("Default"))
        {
            holder.imageView.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(mContext).load(imgURL).into(holder.imageView);
        }


        if (position==mChat.size()-1){
            if (chat.isIsseen()){
                holder.txt_seen.setText("Seen");
            }else {
                holder.txt_seen.setText("Delivered");
            }
        }else {
            holder.txt_seen.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder {

        public CircleImageView imageView;
        public TextView show_msg;
        public TextView txt_seen;
        public viewHolder(@NonNull View itemView) {
            super(itemView);

            imageView=itemView.findViewById(R.id.profile_image);
            show_msg=itemView.findViewById(R.id.txt_show_message);
            txt_seen=itemView.findViewById(R.id.txt_seen);
        }
    }

    @Override
    public int getItemViewType(int position) {
        fuser= FirebaseAuth.getInstance().getCurrentUser();

        if (mChat.get(position).getSender().equals(fuser.getUid()))
        {
            return MSG_TYPE_RIGHT;
        }
        else {
            return MSG_TYPE_LEFT;
        }

    }
}
