package com.tatteam.android.englishaccenttraining.chat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.transition.TransitionManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tatteam.android.englishaccenttraining.R;
import com.tatteam.android.englishaccenttraining.utils.Constant;
import com.tatteam.android.englishaccenttraining.utils.KeyboardHeightObserver;
import com.tatteam.android.englishaccenttraining.utils.KeyboardHeightProvider;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import io.github.rockerhieu.emojicon.EmojiconEditText;
import io.github.rockerhieu.emojicon.EmojiconGridFragment;
import io.github.rockerhieu.emojicon.EmojiconsFragment;
import io.github.rockerhieu.emojicon.emoji.Emojicon;

public class ChatActivity extends AppCompatActivity implements EmojiconsFragment.OnEmojiconBackspaceClickedListener, EmojiconGridFragment.OnEmojiconClickedListener, KeyboardHeightObserver, View.OnClickListener {
    private ConstraintLayout mContentArea;
    private EmojiconEditText mEtChat;
    private ImageView mBtnShowEmojiKeyboard;
    private View mKeyboardContainer;

    private KeyboardHeightProvider mKeyboardHeightProvider;

    private ConstraintSet mShowEmojiKeyboardConstraintSet;
    private ConstraintSet mHideEmojiKeyboardConstraintSet;

    private int mKeyboardHeight;
    private boolean mEmojiKeyboardShowed;
    private boolean mSoftKeyboardShowed;

    private ImageView mButtonSend;
    private RecyclerView mRecyclerChat;
    private ChatMessagesAdapter mAdapterChat;
    private ArrayList<ChatMessage> chatMessageArrayList = new ArrayList<>();
    private final String ID_FIREBASE = "chat_data";
    ChatMessage chatMessage;
    private static DatabaseReference mDatabase;
    private SharedPreferences pre;
    private String sender = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chat_without_emoji_keyboard);

        mKeyboardHeightProvider = new KeyboardHeightProvider(this);

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.emoji_keyboard, EmojiconsFragment.newInstance(false))
                .commit();

        findViews();
        setEventListeners();
        initConstraintSet();

        findViewById(R.id.view_layout).post(new Runnable() {
            @Override
            public void run() {
                mKeyboardHeightProvider.start();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mKeyboardHeightProvider.setKeyboardHeightObserver(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mKeyboardHeightProvider.setKeyboardHeightObserver(null);
    }

    @Override
    protected void onDestroy() {
        mKeyboardHeightProvider.close();
        super.onDestroy();
    }

    @Override
    public void onEmojiconBackspaceClicked(View v) {
        EmojiconsFragment.backspace(mEtChat);
    }

    @Override
    public void onEmojiconClicked(Emojicon emojicon) {
        EmojiconsFragment.input(mEtChat, emojicon);
    }

    @Override
    public void onBackPressed() {
        if (mEmojiKeyboardShowed && !mSoftKeyboardShowed) {
            showEmojiKeyboard(false);
        } else
            super.onBackPressed();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.e("Keycode dispatch", event.getKeyCode() + "");
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.e("Keycode", keyCode + "");
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onKeyboardHeightChanged(int height, int orientation) {
        if (height > 0) {
            if (mKeyboardContainer.getHeight() != height) {
                if (!mEmojiKeyboardShowed) {
                    Log.e("Dafuq", "This");
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                }

                mShowEmojiKeyboardConstraintSet.constrainHeight(R.id.emoji_keyboard, height);
                ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) mKeyboardContainer.getLayoutParams();
                layoutParams.height = height;
                mKeyboardContainer.setLayoutParams(layoutParams);
            }
        } else {
            if (mEmojiKeyboardShowed && mSoftKeyboardShowed)
                showEmojiKeyboard(false);
        }
        mSoftKeyboardShowed = height > 0;
    }

    private void findViews() {
        mKeyboardContainer = findViewById(R.id.emoji_keyboard);
        mEtChat = findViewById(R.id.et_chat);
        mContentArea = findViewById(R.id.content_area);
        mBtnShowEmojiKeyboard = findViewById(R.id.btn_show_emoji_keyboard);
        mButtonSend = findViewById(R.id.btn_send);

        mDatabase = FirebaseDatabase.getInstance().getReference();


        mAdapterChat = new ChatMessagesAdapter(this, chatMessageArrayList);
        mRecyclerChat = findViewById(R.id.recycler_chat);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerChat.setLayoutManager(linearLayoutManager);
        mRecyclerChat.setAdapter(mAdapterChat);


        getListMessage();
        mButtonSend.setOnClickListener(this);
        pre = this.getSharedPreferences(Constant.PREF_NAME, MODE_PRIVATE);
    }

    private void setEventListeners() {
        mBtnShowEmojiKeyboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mEmojiKeyboardShowed) {
                    mBtnShowEmojiKeyboard.setImageResource(R.drawable.ic_keyboard);
                    if (mSoftKeyboardShowed) {
                        mSoftKeyboardShowed = false;
                        hideSoftKeyboard();
                    }
                    showEmojiKeyboard(true);
                    return;
                }

                if (!mSoftKeyboardShowed) {
                    showSoftKeyboard();
                    return;
                }

                hideSoftKeyboard();
            }
        });
    }


    private void initConstraintSet() {
        mHideEmojiKeyboardConstraintSet = new ConstraintSet();
        mHideEmojiKeyboardConstraintSet.clone(mContentArea);

        mShowEmojiKeyboardConstraintSet = new ConstraintSet();
        mShowEmojiKeyboardConstraintSet.clone(this, R.layout.activity_chat_with_emoji_keyboard);
    }

    private void showEmojiKeyboard(boolean showed) {
        mEmojiKeyboardShowed = showed;

        if (!mEmojiKeyboardShowed) {
            mBtnShowEmojiKeyboard.setImageResource(R.drawable.ic_emoji);
        }

        TransitionManager.beginDelayedTransition(mContentArea);
        ConstraintSet constraintSet = mEmojiKeyboardShowed ? mShowEmojiKeyboardConstraintSet : mHideEmojiKeyboardConstraintSet;
        constraintSet.applyTo(mContentArea);
    }

    private void showSoftKeyboard() {
        mSoftKeyboardShowed = true;
        mBtnShowEmojiKeyboard.setImageResource(R.drawable.ic_emoji);

        if (mEmojiKeyboardShowed) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        }

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    private void hideSoftKeyboard() {
        mSoftKeyboardShowed = false;
        if (mEmojiKeyboardShowed) {
            mBtnShowEmojiKeyboard.setImageResource(R.drawable.ic_keyboard);
        }
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send:
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                String currentDateTime = sdf.format(new Date());

                if (!mEtChat.getText().toString().trim().equals("")) {
                    sender = pre.getString(Constant.NAME, "");
                    if (!sender.equals("")) {
                        chatMessage = new ChatMessage(sender, currentDateTime.toString(), mEtChat.getText().toString(), ChatMessage.MY_MESSAGE);
                        mDatabase.child(ID_FIREBASE).child(chatMessage.time).setValue(chatMessage);
                        mEtChat.setText("");
                    }
                }
                break;
        }
    }

    private void getListMessage() {
        mDatabase.child(ID_FIREBASE).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                HashMap<String, String> message = (HashMap<String, String>) dataSnapshot.getValue();
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.from = message.get(Constant.SENDER);
                chatMessage.time = message.get(Constant.TIME);
                chatMessage.content = message.get(Constant.CONTENT);
//                chatMessage.viewType = Long.par.parseInt(message.get("viewType"));
                chatMessageArrayList.add(chatMessage);
                mRecyclerChat.scrollToPosition(chatMessageArrayList.size() - 1);
                mAdapterChat.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
