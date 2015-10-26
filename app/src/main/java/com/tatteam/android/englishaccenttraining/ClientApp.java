package com.tatteam.android.englishaccenttraining;

import android.app.Application;

/**
 * Created by Thanh on 26/09/2015.
 */
public class ClientApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DataSource.getInstance().init(getApplicationContext());
    }
    @Override
    public void onTerminate() {
        DataSource.getInstance().destroy();
        super.onTerminate();
    }
}
