package com.tatteam.android.englishaccenttraining;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

import tatteam.com.app_common.util.CloseAppHandler;
import tatteam.com.app_common.util.CommonUtil;

public class StartActivity extends AppCompatActivity implements View.OnClickListener, CloseAppHandler.OnCloseAppListener {
    private Button btnStartApp;
    private ImageView btnOpenNewApp;
    private CloseAppHandler closeAppHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        btnStartApp = (Button) this.findViewById(R.id.btn_start_app);
        btnOpenNewApp = (ImageView) this.findViewById(R.id.btn_new_app);

        btnStartApp.setOnClickListener(this);
        btnOpenNewApp.setOnClickListener(this);

        closeAppHandler = new CloseAppHandler(this);
        closeAppHandler.setListener(this);
    }

    @Override
    public void onBackPressed() {
        closeAppHandler.setKeyBackPress(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start_app:
                Intent openApp = new Intent(StartActivity.this, MainActivity.class);
                startActivity(openApp);
                break;
            case R.id.btn_new_app:


                FirebaseAnalytics mFirebaseAnalytics= FirebaseAnalytics.getInstance(this);
                //customize
                Bundle bundle2 = new Bundle();
                String packageName = "com.essential.esl";
                if (CommonUtil.isPackageInstalled(packageName, this)) {
                    Intent intent = this.getPackageManager().getLaunchIntentForPackage(packageName);
                    startActivity(intent);
                    bundle2.putBoolean("is_exist", true);
                } else {
                    CommonUtil.openApplicationOnGooglePlay(this, packageName);
                    bundle2.putBoolean("is_exist", false);
                }

                mFirebaseAnalytics.logEvent("interest_esl", bundle2);
                break;
        }
    }

    @Override
    public void onRateAppDialogClose() {
        finish();
    }

    @Override
    public void onTryToCloseApp() {
        Toast.makeText(this, "Press once again to exit!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onReallyWantToCloseApp() {
        finish();
    }
}
