package com.tatteam.android.englishaccenttraining.chat;

public class ChatMessage {
    public String time;
    public String content;
    public int viewType;
    public String from;
    public boolean sent = false;

    public static final int MY_MESSAGE = 0;
    public static final int THEIR_MESSAGE = 1;
    public static final int TIME = 2;
}
