package com.tatteam.android.englishaccenttraining.chat;

import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import java.util.List;

public class ChatMessageDiffUtil extends DiffUtil.Callback {
  private List<ChatMessage> mOldList;
  private List<ChatMessage> mNewList;

  public ChatMessageDiffUtil(List<ChatMessage> oldList, List<ChatMessage> newList) {
    this.mOldList = oldList;
    this.mNewList = newList;
  }

  @Override
  public int getOldListSize() {
    return mOldList != null ? mOldList.size() : 0;
  }

  @Override
  public int getNewListSize() {
    return mNewList != null ? mNewList.size() : 0;
  }

  @Override
  public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
    return mOldList.get(oldItemPosition).isSame(mNewList.get(newItemPosition));
  }

  @Override
  public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
    return mOldList.get(oldItemPosition).equals(mNewList.get(newItemPosition));
  }

  @Nullable
  @Override
  public Object getChangePayload(int oldItemPosition, int newItemPosition) {

    return super.getChangePayload(oldItemPosition, newItemPosition);
  }
}
