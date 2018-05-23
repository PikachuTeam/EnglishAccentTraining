package com.tatteam.android.englishaccenttraining;

import android.app.Application;

//import com.crashlytics.android.Crashlytics;
//import io.fabric.sdk.android.Fabric;
import tatteam.com.app_common.AppCommon;
import tatteam.com.app_common.sqlite.DatabaseLoader;

/**
 * Created by Thanh on 26/09/2015.
 */
public class ClientApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
//        Fabric.with(this, new Crashlytics());
        AppCommon.getInstance().initIfNeeded(getApplicationContext());
        DatabaseLoader.getInstance().restoreState(getApplicationContext());
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
