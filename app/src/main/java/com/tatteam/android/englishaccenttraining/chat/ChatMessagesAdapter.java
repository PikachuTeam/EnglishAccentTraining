package com.tatteam.android.englishaccenttraining.chat;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tatteam.android.englishaccenttraining.R;
import com.tatteam.android.englishaccenttraining.utils.DateTimeUtils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class ChatMessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
  private static final int VISIBLE_THRESHOLD = 8;
  private static final int LOAD_MORE = 753;

  private Context mContext;
  private List<ChatMessage> mMessageList = new ArrayList<>();

  private RecyclerView mRecyclerView;

  private OnLoadMoreListener mLoadMoreListener;

  private boolean mIsLoading;
  private boolean mCanLoadMore;

  public void setOnLoadMoreListener(OnLoadMoreListener loadMoreListener) {
    mLoadMoreListener = loadMoreListener;
  }

  public void setCanLoadMore(boolean canLoadMore) {
    mCanLoadMore = canLoadMore;
  }

  public ChatMessagesAdapter(Context context, ArrayList<ChatMessage> messageList, RecyclerView recyclerView) {
    mContext = context;
    mMessageList = messageList;
    mRecyclerView = recyclerView;
    mCanLoadMore = true;

    mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
      @Override
      public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        LinearLayoutManager layoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();

        int totalItems = layoutManager.getChildCount();
        int lastVisibleItem = layoutManager.findLastVisibleItemPosition();

        if (mCanLoadMore && lastVisibleItem + VISIBLE_THRESHOLD >= totalItems && !mIsLoading) {
          if (mLoadMoreListener != null) {
            mIsLoading = true;
            showLoadMore();
            mLoadMoreListener.onLoadMore();
          }
        }
      }
    });
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
      case LOAD_MORE:
        return new LoadMoreHolder(inflater.inflate(R.layout.item_load_more, parent, false));
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
    } else if (holder instanceof TimeViewHolder) {
      ((TimeViewHolder) holder).bindData(mMessageList.get(position));
    }
  }

  @Override
  public int getItemCount() {
    return mMessageList.size();
  }

  @Override
  public int getItemViewType(int position) {
    if (mMessageList.get(position) == null)
      return LOAD_MORE;
    return mMessageList.get(position).viewType;
  }

  public void showLoadMore() {
    mRecyclerView.post(new Runnable() {
      @Override
      public void run() {
        mMessageList.add(null);
        final int loadMoreIndex = mMessageList.size() - 1;
        notifyItemInserted(loadMoreIndex);
      }
    });
  }

  public void dismissLoadMore() {
    mRecyclerView.post(new Runnable() {
      @Override
      public void run() {
        mIsLoading = false;
        int loadMoreIndex = -1;
        for (int i = mMessageList.size() - 1; i >= 0; i--) {
          if (mMessageList.get(i) == null) {
            loadMoreIndex = i;
            break;
          }
        }
        if (loadMoreIndex != -1) {
          mMessageList.remove(loadMoreIndex);
          notifyItemRemoved(loadMoreIndex);
        }
      }
    });
  }

  public class TimeViewHolder extends RecyclerView.ViewHolder {
    TextView mTextDate;

    public TimeViewHolder(View itemView) {
      super(itemView);
      mTextDate = itemView.findViewById(R.id.tv_time);
    }

    public void bindData(ChatMessage chatMessage) {
      mTextDate.setText("");
      if (!TextUtils.isEmpty(chatMessage.time)) {
        try {
          mTextDate.setText(DateTimeUtils.getChatDateToShow(chatMessage.time));
        } catch (ParseException e) {
          e.printStackTrace();
        }
      }
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

      mTextTimeSent.setText("");
      if (!TextUtils.isEmpty(chatMessage.time)) {
        try {
          mTextTimeSent.setText(DateTimeUtils.getChatTimeToShow(chatMessage.time));
        } catch (ParseException e) {
          e.printStackTrace();
        }
      }

      if (!chatMessage.isAdjacent) {
        mTextSender.setVisibility(View.VISIBLE);
        mTextSender.setText(chatMessage.from);
      } else {
        mTextSender.setVisibility(View.GONE);
      }
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

      mTextTimeSent.setText("");
      if (!TextUtils.isEmpty(chatMessage.time)) {
        try {
          mTextTimeSent.setText(DateTimeUtils.getChatTimeToShow(chatMessage.time));
        } catch (ParseException e) {
          e.printStackTrace();
        }
      }

      switch (chatMessage.state) {
        case ChatMessage.STATE_SENDING:
          mImageSend.setImageResource(R.drawable.ic_sending);
          break;
        case ChatMessage.STATE_ERROR:
          mImageSend.setImageResource(R.drawable.ic_message_error);
          break;
        default:
          mImageSend.setImageResource(R.drawable.ic_sent);
          break;
      }
    }
  }

  class LoadMoreHolder extends RecyclerView.ViewHolder {

    public LoadMoreHolder(View itemView) {
      super(itemView);
    }
  }

  interface OnLoadMoreListener {
    void onLoadMore();
  }
}
