package com.tatteam.android.englishaccenttraining.chat;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.transition.TransitionManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tatteam.android.englishaccenttraining.R;
import com.tatteam.android.englishaccenttraining.utils.Constant;
import com.tatteam.android.englishaccenttraining.utils.DateTimeUtils;
import com.tatteam.android.englishaccenttraining.utils.DeviceUtils;
import com.tatteam.android.englishaccenttraining.utils.KeyboardHeightObserver;
import com.tatteam.android.englishaccenttraining.utils.KeyboardHeightProvider;
import com.tatteam.android.englishaccenttraining.utils.PermissionChecker;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.github.rockerhieu.emojicon.EmojiconEditText;
import io.github.rockerhieu.emojicon.EmojiconGridFragment;
import io.github.rockerhieu.emojicon.EmojiconsFragment;
import io.github.rockerhieu.emojicon.emoji.Emojicon;

public class ChatActivity extends AppCompatActivity implements EmojiconsFragment.OnEmojiconBackspaceClickedListener, EmojiconGridFragment.OnEmojiconClickedListener, KeyboardHeightObserver, View.OnClickListener {
  private static final int RC_PERMISSION_READ_PHONE = 689;
  private static final int PAGE_SIZE = 20;

  private ConstraintLayout mContentArea;
  private EmojiconEditText mEtChat;
  private ImageView mBtnShowEmojiKeyboard;
  private View mKeyboardContainer;

  private KeyboardHeightProvider mKeyboardHeightProvider;

  private ConstraintSet mShowEmojiKeyboardConstraintSet;
  private ConstraintSet mHideEmojiKeyboardConstraintSet;

  private boolean mEmojiKeyboardShowed;
  private boolean mSoftKeyboardShowed;

  private ImageView mButtonSend, mButtonBack;
  private RecyclerView mRecyclerChat;
  private ChatMessagesAdapter mAdapterChat;
  private ArrayList<ChatMessage> chatMessageArrayList = new ArrayList<>();
  private final String ID_FIREBASE = "chat_data";
  ChatMessage chatMessage;
  private static DatabaseReference mDatabase;
  private SharedPreferences pre;

  private boolean mIsFirst;
  private boolean mFirebaseConnected;

  private ChatItemDecoration mChatItemDecoration;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    initFirebase();

    mIsFirst = true;

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

    if (PermissionChecker.checkPermission(this, Manifest.permission.READ_PHONE_STATE))
      getListMessage();
    else
      ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, RC_PERMISSION_READ_PHONE);
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
    hideSoftKeyboard();
    mDatabase.child(ID_FIREBASE).removeEventListener(mChildEventListener);
    super.onDestroy();
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == RC_PERMISSION_READ_PHONE) {
      if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        if (mIsFirst) {
          mIsFirst = false;
          getListMessage();
        } else
          sendMessage();
      }
    }
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
  public void onKeyboardHeightChanged(int height, int orientation) {
    if (height > 0) {
      if (!mEmojiKeyboardShowed) {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
      } else {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
      }
      if (mKeyboardContainer.getHeight() != height) {
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

  private void initFirebase() {
    FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    FirebaseDatabase.getInstance().getReference(ID_FIREBASE).keepSynced(true);

    mDatabase = FirebaseDatabase.getInstance().getReference();

    FirebaseDatabase.getInstance().getReference(".info/connected")
            .addValueEventListener(new ValueEventListener() {
              @Override
              public void onDataChange(DataSnapshot dataSnapshot) {
                mFirebaseConnected = dataSnapshot.getValue(Boolean.class);
                Log.e("Check connection", "" + mFirebaseConnected);
              }

              @Override
              public void onCancelled(DatabaseError databaseError) {

              }
            });
  }

  private void findViews() {
    mKeyboardContainer = findViewById(R.id.emoji_keyboard);
    mEtChat = findViewById(R.id.et_chat);
    mContentArea = findViewById(R.id.content_area);
    mBtnShowEmojiKeyboard = findViewById(R.id.btn_show_emoji_keyboard);
    mButtonSend = findViewById(R.id.btn_send);
    mButtonBack = findViewById(R.id.img_back);

    mButtonSend.setOnClickListener(this);
    mButtonBack.setOnClickListener(this);
    pre = this.getSharedPreferences(Constant.PREF_NAME, MODE_PRIVATE);

    initRecyclerView();
  }

  private void setEventListeners() {
    mBtnShowEmojiKeyboard.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (!mEmojiKeyboardShowed) {
          mBtnShowEmojiKeyboard.setImageResource(R.drawable.ic_keyboard);
          if (mSoftKeyboardShowed) {
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

  private void initRecyclerView() {
    mChatItemDecoration = new ChatItemDecoration();

    mRecyclerChat = findViewById(R.id.recycler_chat);
    mRecyclerChat.addItemDecoration(mChatItemDecoration);
    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
    linearLayoutManager.setReverseLayout(true);
    mRecyclerChat.setLayoutManager(linearLayoutManager);

    mAdapterChat = new ChatMessagesAdapter(this, chatMessageArrayList, mRecyclerChat);
    mRecyclerChat.setAdapter(mAdapterChat);
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
        if (!mEtChat.getText().toString().trim().equals("")) {
          if (PermissionChecker.checkPermission(this, Manifest.permission.READ_PHONE_STATE)) {
            sendMessage();
          } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, RC_PERMISSION_READ_PHONE);
          }
        }
        break;
      case R.id.img_back:
        onBackPressed();
        break;
    }
  }

  private void getListMessage() {
    mDatabase.child(ID_FIREBASE).limitToLast(PAGE_SIZE).addChildEventListener(mChildEventListener);
  }

  private void getMoreMessages() {
    String startChatId = "";

    for (int i = chatMessageArrayList.size() - 1; i >= 0; i++) {
      if (!TextUtils.isEmpty(chatMessageArrayList.get(i).id)) {
        startChatId = chatMessageArrayList.get(i).id;
        break;
      }
    }

    mDatabase.child(ID_FIREBASE).limitToFirst(PAGE_SIZE).startAt(startChatId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
              @Override
              public void onDataChange(DataSnapshot dataSnapshot) {
                mAdapterChat.setCanLoadMore(dataSnapshot.getChildrenCount() == PAGE_SIZE);
                mAdapterChat.dismissLoadMore();
                int startIndex = chatMessageArrayList.size();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                  chatMessageArrayList.add(getChatMessage(data));
                }
                mChatItemDecoration.updateList(chatMessageArrayList);
                mAdapterChat.notifyItemRangeInserted(startIndex, chatMessageArrayList.size() - startIndex);
              }

              @Override
              public void onCancelled(DatabaseError databaseError) {
              }
            });
  }

  private void sendMessage() {
    String sender = pre.getString(Constant.NAME, "");
    if (!sender.equals("")) {
      chatMessage = new ChatMessage(sender, DateTimeUtils.getCurrentTimeInWorldGMT(),
              mEtChat.getText().toString(), DeviceUtils.getInstance().getDeviceId(this));
      chatMessage.state = ChatMessage.STATE_SENDING;

      ChatMessage copiedMessage = copyChatMessage(chatMessage);
      try {
        copiedMessage.time = DateTimeUtils.getChatTimeFromWorldTime(copiedMessage.time);
      } catch (ParseException e) {
        e.printStackTrace();
      }
      addNewMessage(copiedMessage);

      mDatabase.child(ID_FIREBASE).push().setValue(chatMessage);
      mEtChat.setText("");
    }
  }

  private ChatMessage getChatMessage(DataSnapshot data) {
    HashMap<String, Object> message = (HashMap<String, Object>) data.getValue();
    ChatMessage chatMessage = new ChatMessage();
    chatMessage.id = data.getKey();
    chatMessage.from = message.get(Constant.SENDER).toString();
    try {
      chatMessage.time = DateTimeUtils.getChatTimeFromWorldTime(message.get(Constant.TIME).toString());
    } catch (ParseException e) {
      e.printStackTrace();
    }
    chatMessage.content = message.get(Constant.CONTENT).toString();
    chatMessage.deviceId = message.get(Constant.DEVICE_ID).toString();

    if (chatMessage.deviceId.equals(DeviceUtils.getInstance().getDeviceId(this))) {
      chatMessage.viewType = ChatMessage.MY_MESSAGE;
    } else {
      chatMessage.viewType = ChatMessage.THEIR_MESSAGE;
    }

    if (message.containsKey(Constant.STATE))
      chatMessage.state = Integer.parseInt(message.get(Constant.STATE).toString());
    else
      chatMessage.state = ChatMessage.STATE_SUCCESS;

    if (!chatMessageArrayList.isEmpty()) {
      ChatMessage previousMessage = chatMessageArrayList.get(0);
      chatMessage.isAdjacent = TextUtils.equals(previousMessage.deviceId, chatMessage.deviceId);
    }

    return chatMessage;
  }

  private ChatMessage copyChatMessage(ChatMessage toCopyMessage) {
    ChatMessage chatMessage = new ChatMessage();

    chatMessage.copy(toCopyMessage);

    if (!chatMessageArrayList.isEmpty()) {
      ChatMessage previousMessage = chatMessageArrayList.get(0);
      chatMessage.isAdjacent = TextUtils.equals(previousMessage.deviceId, chatMessage.deviceId);
    }
    return chatMessage;
  }

  private void addTimeMessage() {
    // Check if current msg has same date with previous
    try {
      if (!chatMessageArrayList.isEmpty() && !DateTimeUtils.isSameDate(chatMessageArrayList.get(0).time, chatMessage.time)) {
        ChatMessage dateChatMessage = new ChatMessage();
        dateChatMessage.time = chatMessage.time;
        dateChatMessage.viewType = ChatMessage.TIME;

        chatMessageArrayList.add(0, dateChatMessage);
        mChatItemDecoration.updateList(chatMessageArrayList);
        mAdapterChat.notifyItemInserted(0);
      }
    } catch (ParseException e) {
      e.printStackTrace();
    }
  }

  private void sortMessage(DataSnapshot dataSnapshot) {
    new AsyncTask<DataSnapshot, Void, Integer>() {
      @Override
      protected Integer doInBackground(DataSnapshot... dataSnapshots) {
        ChatMessage chatMessage = getChatMessage(dataSnapshots[0]);

        if (chatMessageArrayList.isEmpty()) {
          chatMessageArrayList.add(chatMessage);
          return 0;
        }
        int totalMessages = chatMessageArrayList.size();
        int insertedIndex = -1;
        try {
          for (int i = 0; i < totalMessages; i++) {
            if (DateTimeUtils.compareTwoTime(chatMessage.time, chatMessageArrayList.get(i).time) >= 0) {
              insertedIndex = i;
              chatMessageArrayList.add(i, chatMessage);
              break;
            }
          }
          if (insertedIndex == -1) {
            insertedIndex = chatMessageArrayList.size();
            chatMessageArrayList.add(chatMessage);
          }

          return insertedIndex;
        } catch (ParseException e) {
          e.printStackTrace();
        }
        return -1;
      }

      @Override
      protected void onPostExecute(Integer position) {
        super.onPostExecute(position);

        if (position != -1) {
          mChatItemDecoration.updateList(chatMessageArrayList);
          mAdapterChat.notifyItemInserted(position);
          mRecyclerChat.scrollToPosition(0);
        }
      }
    }.execute(dataSnapshot);
  }

  private ChildEventListener mChildEventListener = new ChildEventListener() {
    @Override
    public void onChildAdded(final DataSnapshot dataSnapshot, String previousKey) {
      ChatMessage chatMessage = getChatMessage(dataSnapshot);
      if (chatMessage.viewType != ChatMessage.MY_MESSAGE) {
        addNewMessage(chatMessage);
      } else {
        if (chatMessage.state == ChatMessage.STATE_SENDING) {
          int totalMessages = chatMessageArrayList.size();

          for (int i = 0; i < totalMessages; i++) {
            if (chatMessage.isSame(chatMessageArrayList.get(i))) {
              if (mFirebaseConnected) {
                chatMessageArrayList.get(i).state = ChatMessage.STATE_SUCCESS;
                chatMessage.state = ChatMessage.STATE_SUCCESS;
              } else {
                chatMessageArrayList.get(i).state = ChatMessage.STATE_ERROR;
                chatMessage.state = ChatMessage.STATE_ERROR;
              }

              Map<String, Object> childUpdates = new HashMap<>();
              childUpdates.put("/" + ID_FIREBASE + "/" + chatMessage.id + "/" + Constant.STATE, chatMessage.state);
              mDatabase.updateChildren(childUpdates);

              mAdapterChat.notifyItemChanged(i);
              break;
            }
          }
        } else if (chatMessage.state == ChatMessage.STATE_SUCCESS) {
          addNewMessage(chatMessage);
        }
      }
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
      ChatMessage chatMessage = getChatMessage(dataSnapshot);
      if (chatMessage.state == ChatMessage.STATE_ERROR && mFirebaseConnected) {
        chatMessage.state = ChatMessage.STATE_SUCCESS;

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/" + ID_FIREBASE + "/" + chatMessage.id + "/" + Constant.STATE, chatMessage.state);
        mDatabase.updateChildren(childUpdates);

        int totalMessages = chatMessageArrayList.size();
        for (int i = 0; i < totalMessages; i++) {
          if (chatMessage.isSame(chatMessageArrayList.get(i))) {
            chatMessageArrayList.get(i).state = ChatMessage.STATE_SUCCESS;
            mAdapterChat.notifyItemChanged(i);
            break;
          }
        }
      }
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
  };

  private void addNewMessage(ChatMessage chatMessage) {
    chatMessageArrayList.add(0, chatMessage);
    mChatItemDecoration.updateList(chatMessageArrayList);
    mAdapterChat.notifyItemInserted(0);
    mRecyclerChat.scrollToPosition(0);
  }
}
