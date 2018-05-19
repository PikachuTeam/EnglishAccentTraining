package com.tatteam.android.englishaccenttraining.chat;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.transition.TransitionManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import com.tatteam.android.englishaccenttraining.R;
import com.tatteam.android.englishaccenttraining.utils.KeyboardHeightObserver;
import com.tatteam.android.englishaccenttraining.utils.KeyboardHeightProvider;

import io.github.rockerhieu.emojicon.EmojiconEditText;
import io.github.rockerhieu.emojicon.EmojiconGridFragment;
import io.github.rockerhieu.emojicon.EmojiconsFragment;
import io.github.rockerhieu.emojicon.emoji.Emojicon;

public class ChatActivity extends AppCompatActivity implements EmojiconsFragment.OnEmojiconBackspaceClickedListener, EmojiconGridFragment.OnEmojiconClickedListener, KeyboardHeightObserver {
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
        mBtnShowEmojiKeyboard =
                findViewById(R.id.btn_show_emoji_keyboard);
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

        TransitionManager.beginDelayedTransition(mContentArea);
        ConstraintSet constraintSet = mEmojiKeyboardShowed ? mShowEmojiKeyboardConstraintSet : mHideEmojiKeyboardConstraintSet;
        constraintSet.applyTo(mContentArea);
    }

    private void showSoftKeyboard() {
        mSoftKeyboardShowed = true;
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    private void hideSoftKeyboard() {
        mSoftKeyboardShowed = false;
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
    }
}
