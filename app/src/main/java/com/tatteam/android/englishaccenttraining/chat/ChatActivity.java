package com.tatteam.android.englishaccenttraining.chat;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
  private String sender = "";

  private boolean mIsFirst;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

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
      if (mKeyboardContainer.getHeight() != height) {
        if (!mEmojiKeyboardShowed) {
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
    mButtonBack = findViewById(R.id.img_back);

    mDatabase = FirebaseDatabase.getInstance().getReference();

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

    mAdapterChat.setOnLoadMoreListener(new ChatMessagesAdapter.OnLoadMoreListener() {
      @Override
      public void onLoadMore() {
        getMoreMessages();
      }
    });
  }

  private void initRecyclerView() {
    mRecyclerChat = findViewById(R.id.recycler_chat);
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
    mDatabase.child(ID_FIREBASE).limitToLast(PAGE_SIZE).addChildEventListener(new ChildEventListener() {
      @Override
      public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        chatMessageArrayList.add(0, getChatMessage(dataSnapshot));
        mAdapterChat.notifyItemInserted(0);
        mRecyclerChat.scrollToPosition(0);
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

  private void getMoreMessages() {
    Log.e("Du ma", "Fuck");
    mDatabase.child(ID_FIREBASE).limitToLast(PAGE_SIZE).startAt(chatMessageArrayList.get(chatMessageArrayList.size() - 1).id)
            .addListenerForSingleValueEvent(new ValueEventListener() {
              @Override
              public void onDataChange(DataSnapshot dataSnapshot) {
                mAdapterChat.setCanLoadMore(dataSnapshot.getChildrenCount() == PAGE_SIZE);
                mAdapterChat.dismissLoadMore();
                int startIndex = chatMessageArrayList.size();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                  chatMessageArrayList.add(getChatMessage(data));
                }
                mAdapterChat.notifyItemRangeInserted(startIndex, chatMessageArrayList.size() - startIndex);
              }

              @Override
              public void onCancelled(DatabaseError databaseError) {

              }
            });
  }

  private void sendMessage() {
    sender = pre.getString(Constant.NAME, "");
    if (!sender.equals("")) {
      chatMessage = new ChatMessage(sender, DateTimeUtils.getCurrentTimeInWorldGMT(),
              mEtChat.getText().toString(), DeviceUtils.getInstance().getDeviceId(this));
      mDatabase.child(ID_FIREBASE).push().setValue(chatMessage);
      mEtChat.setText("");
    }
  }

  private ChatMessage getChatMessage(DataSnapshot data) {
    HashMap<String, String> message = (HashMap<String, String>) data.getValue();
    ChatMessage chatMessage = new ChatMessage();
    chatMessage.id = data.getKey();
    chatMessage.from = message.get(Constant.SENDER);
    try {
      chatMessage.time = DateTimeUtils.getChatTimeFromWorldTime(message.get(Constant.TIME));
    } catch (ParseException e) {
      e.printStackTrace();
    }
    chatMessage.content = message.get(Constant.CONTENT);
    chatMessage.deviceId = message.get(Constant.DEVICE_ID);

    if (chatMessage.deviceId.equals(DeviceUtils.getInstance().getDeviceId(this))) {
      chatMessage.viewType = ChatMessage.MY_MESSAGE;
    } else {
      chatMessage.viewType = ChatMessage.THEIR_MESSAGE;
    }

    chatMessage.sent = true;

    return chatMessage;
  }
}
