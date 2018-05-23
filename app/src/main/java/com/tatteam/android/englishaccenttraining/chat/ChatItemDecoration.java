package com.tatteam.android.englishaccenttraining.chat;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.List;

public class ChatItemDecoration extends RecyclerView.ItemDecoration {
  private List<ChatMessage> mMessages;

  private int mTop;
  private int mLeft;
  private int mBottom;
  private int mRight;

  public ChatItemDecoration() {
    mTop = mLeft = mBottom = mRight = 10;
  }

  public void updateList(List<ChatMessage> messages) {
    mMessages = messages;
  }

  @Override
  public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
    if (view != null) {
      int position = parent.getChildAdapterPosition(view);

      if (position >= 0) {
        if (position == 0) {
          outRect.bottom = mBottom;
        } else {
          outRect.bottom = 0;
        }

        if (mMessages != null && mMessages.get(position) != null && mMessages.get(position).isAdjacent) {
          outRect.top = 0;
        } else {
          outRect.top = mTop;
        }

        outRect.left = mLeft;
        outRect.right = mRight;
      }
    }
  }
}
