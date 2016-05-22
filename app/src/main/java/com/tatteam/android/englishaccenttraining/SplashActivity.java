package com.tatteam.android.englishaccenttraining;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;

import tatteam.com.app_common.AppCommon;
import tatteam.com.app_common.sqlite.DatabaseLoader;
import tatteam.com.app_common.ui.activity.BaseSplashActivity;
import tatteam.com.app_common.util.AppConstant;

public class SplashActivity extends BaseSplashActivity {
    @Override
    protected int getLayoutResIdContentView() {
        return R.layout.activity_splash;
    }

    @Override
    protected void onCreateContentView() {

    }

    @Override
    protected void onInitAppCommon() {
        AppCommon.getInstance().initIfNeeded(getApplicationContext());
        AppCommon.getInstance().increaseLaunchTime();
        AppCommon.getInstance().syncAdsIfNeeded(AppConstant.AdsType.SMALL_BANNER_LANGUAGE_LEARNING, AppConstant.AdsType.BIG_BANNER_LANGUAGE_LEARNING);

        DatabaseLoader.getInstance().createIfNeeded(getApplicationContext(), "trainyouraccent_2.db");
    }

    @Override
    protected void onFinishInitAppCommon() {
        switchToMainActivity();
    }


    private void switchToMainActivity() {
        startActivity(new Intent(SplashActivity.this, StartActivity.class));
        this.finish();
    }


}
