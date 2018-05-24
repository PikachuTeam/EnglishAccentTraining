package com.tatteam.android.englishaccenttraining.chat;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tatteam.android.englishaccenttraining.MainActivity;
import com.tatteam.android.englishaccenttraining.R;
import com.tatteam.android.englishaccenttraining.utils.Constant;
import com.tatteam.android.englishaccenttraining.utils.DeviceUtils;
import com.tatteam.android.englishaccenttraining.utils.DialogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
    mEditName.setSelection(mEditName.getText().length());
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.btn_ok:
        DialogUtils.showLoadingDialog(mContext);
        FirebaseDatabase.getInstance().getReference().child(Constant.TABLE_USERS)
                .addListenerForSingleValueEvent(mGetUserColorListener);
        break;
    }
  }

  private ValueEventListener mGetUserColorListener = new ValueEventListener() {
    @Override
    public void onDataChange(final DataSnapshot dataSnapshot) {
      new AsyncTask<Void, Void, String>() {
        @Override
        protected String doInBackground(Void... voids) {
          String deviceId = DeviceUtils.getInstance().getDeviceId(mContext);
          String color = "";
          List<String> usedColors = new ArrayList<>();

          for (DataSnapshot data : dataSnapshot.getChildren()) {
            if (data.getKey().equals(deviceId)) {
              color = data.getValue().toString();
              break;
            } else {
              usedColors.add(data.getValue().toString());
            }
          }

          if (TextUtils.isEmpty(color)) {
            Random random = new Random();
            do {
              String red = random.nextInt(255) + "";
              String green = random.nextInt(255) + "";
              String blue = random.nextInt(255) + "";

              boolean existed = false;

              for (String usedColor : usedColors) {
                String[] colors = usedColor.split(",");

                if (red.equals(colors[0].trim()) && green.equals(colors[1].trim()) && blue.equals(colors[2].trim())) {
                  existed = true;
                  break;
                }
              }

              if (!existed) {
                color = TextUtils.join(",", new String[]{red, green, blue});
              }
            } while (TextUtils.isEmpty(color));

            Map<String, Object> toUpdateData = new HashMap<>();
            toUpdateData.put("/" + Constant.TABLE_USERS + "/" + deviceId, color);
            FirebaseDatabase.getInstance().getReference().updateChildren(toUpdateData);
          }
          return color;
        }

        @Override
        protected void onPostExecute(String color) {
          super.onPostExecute(color);
          DialogUtils.dismissLoadingDialog();
          MainActivity.userColor = color;

          SharedPreferences.Editor editor = pre.edit();
          if (!mEditName.getText().toString().trim().equals("")) {
            editor.putString(Constant.NAME, mEditName.getText().toString());
            editor.commit();
            dismiss();
            mContext.startActivity(new Intent(mainActivity, ChatActivity.class));
          } else {
            Toast.makeText(mContext, mContext.getString(R.string.alert_mess), Toast.LENGTH_SHORT).show();
          }
        }
      }.execute();
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
      DialogUtils.dismissLoadingDialog();
    }
  };
}
