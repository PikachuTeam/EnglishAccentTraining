package com.tatteam.android.englishaccenttraining;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity implements View.OnClickListener {
    Button btnStartApp,btnOpenNewApp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        btnStartApp = (Button) this.findViewById(R.id.btn_start_app);
        btnOpenNewApp = (Button) this.findViewById(R.id.btn_new_app);
        btnStartApp.setOnClickListener(this);
        btnOpenNewApp.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_start_app:
                Intent openApp = new Intent(StartActivity.this,MainActivity.class);
                startActivity(openApp);
                break;
            case R.id.btn_new_app:

                break;
        }
    }
}
