package com.tatteam.android.englishaccenttraining.chat;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tatteam.android.englishaccenttraining.MainActivity;
import com.tatteam.android.englishaccenttraining.R;
import com.tatteam.android.englishaccenttraining.utils.Constant;

import static android.content.Context.MODE_PRIVATE;

public class RenameDialog extends Dialog implements View.OnClickListener {
    private TextView mTextOk, mTextCancel;
    private EditText mEditName;

    private Context mContext;
    private SharedPreferences pre;
    private Activity mainActivity;

    public RenameDialog(@NonNull Activity context) {
        super(context);
        mContext = context;
        mainActivity = context;
        pre = mContext.getSharedPreferences(Constant.PREF_NAME, MODE_PRIVATE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_rename);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        getWindow().setLayout((8 * width) / 9, WindowManager.LayoutParams.WRAP_CONTENT);
        findViews();
        init();
    }

    private void findViews() {
        mTextOk = findViewById(R.id.btn_ok);
        mEditName = findViewById(R.id.ed_rename);
    }

    private void init() {
        mTextOk.setOnClickListener(this);
        mEditName.setText(pre.getString(Constant.NAME, ""));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok:
                SharedPreferences.Editor editor = pre.edit();
                if (!mEditName.getText().toString().trim().equals("")) {
                    editor.putString(Constant.NAME, mEditName.getText().toString());
                    editor.commit();
                    dismiss();
                    mContext.startActivity(new Intent(mainActivity, ChatActivity.class));
                } else {
                    Toast.makeText(mContext, mContext.getString(R.string.alert_mess), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
