package com.tatteam.android.englishaccenttraining.chat;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tatteam.android.englishaccenttraining.R;

import java.util.ArrayList;
import java.util.List;

public class ChatMessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private List<ChatMessage> mMessageList = new ArrayList<>();


    public ChatMessagesAdapter(Context context, ArrayList<ChatMessage> messageList) {
        mContext = context;
        mMessageList = messageList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        switch (viewType) {
            case ChatMessage.MY_MESSAGE:
                return new MyMessViewHolder(inflater.inflate(R.layout.item_my_message, parent, false));
            case ChatMessage.THEIR_MESSAGE:
                return new TheirMessViewHolder(inflater.inflate(R.layout.item_their_message, parent, false));
            default:
                return new TimeViewHolder(inflater.inflate(R.layout.item_time_message, parent, false));

        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyMessViewHolder) {
            ((MyMessViewHolder) holder).bindData(mMessageList.get(position));
        } else if (holder instanceof TheirMessViewHolder) {
            ((TheirMessViewHolder) holder).bindData(mMessageList.get(position));
        } else {
            ((TimeViewHolder) holder).bindData(mMessageList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mMessageList.get(position).viewType;
    }

    public class TimeViewHolder extends RecyclerView.ViewHolder {
        TextView mTextDate;

        public TimeViewHolder(View itemView) {
            super(itemView);
            mTextDate = itemView.findViewById(R.id.tv_time);
        }

        public void bindData(ChatMessage chatMessage) {
            mTextDate.setText(chatMessage.time);
        }
    }

    public class TheirMessViewHolder extends RecyclerView.ViewHolder {
        TextView mTextContent, mTextTimeSent, mTextSender;

        public TheirMessViewHolder(View itemView) {
            super(itemView);
            mTextContent = itemView.findViewById(R.id.tv_content);
            mTextTimeSent = itemView.findViewById(R.id.tv_time_chat);
            mTextSender = itemView.findViewById(R.id.tv_user_name);
        }

        public void bindData(ChatMessage chatMessage) {
            mTextContent.setText(chatMessage.content);
            mTextTimeSent.setText(processDateTime(chatMessage.time));
            mTextSender.setText(chatMessage.from);
        }
    }

    public class MyMessViewHolder extends RecyclerView.ViewHolder {
        TextView mTextContent, mTextTimeSent;
        ImageView mImageSend;

        public MyMessViewHolder(View itemView) {
            super(itemView);
            mTextContent = itemView.findViewById(R.id.tv_content_my_mess);
            mTextTimeSent = itemView.findViewById(R.id.tv_time_chat_my_mess);
            mImageSend = itemView.findViewById(R.id.img_send);
        }

        public void bindData(ChatMessage chatMessage) {
            mTextContent.setText(chatMessage.content);
            mTextTimeSent.setText(processDateTime(chatMessage.time));
            if (chatMessage.sent) {
                mImageSend.setImageResource(R.drawable.ic_sent);
            } else {
                mImageSend.setImageResource(R.drawable.ic_sending);
            }

        }
    }

    public String processDateTime(String time) {
        String processTime = time.substring(9, 11) + ":" + time.substring(11, 13);
        return processTime;
    }

}
