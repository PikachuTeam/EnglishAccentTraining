package com.tatteam.android.englishaccenttraining.chat;

import android.support.annotation.IntDef;
import android.text.TextUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ChatMessage {
  public String id;
  public String time;
  public String content;
  public int viewType;
  public String from;
  public String deviceId;
  public String nameColor;
  @MessageState
  public int state;
  public boolean isAdjacent;

  public static final int MY_MESSAGE = 0;
  public static final int THEIR_MESSAGE = 1;
  public static final int TIME = 2;

  public static final int STATE_SENDING = 10;
  public static final int STATE_SUCCESS = 11;
  public static final int STATE_ERROR = 12;

  public ChatMessage() {
  }

  public ChatMessage(String sender, String time, String content, String deviceId) {
    this.from = sender;
    this.time = time;
    this.content = content;
    this.deviceId = deviceId;
  }

  public boolean isSame(ChatMessage other) {
    return TextUtils.equals(other.deviceId, deviceId)
            && TextUtils.equals(other.time, time)
            && TextUtils.equals(other.content, content);
  }

  public void copy(ChatMessage toCopyMessage) {
    id = toCopyMessage.id;
    content = toCopyMessage.content;
    time = toCopyMessage.time;
    deviceId = toCopyMessage.deviceId;
    state = toCopyMessage.state;
    from = toCopyMessage.from;
  }

  @IntDef({STATE_SENDING, STATE_SUCCESS, STATE_ERROR})
  @Retention(RetentionPolicy.SOURCE)
  public @interface MessageState {
  }
}
