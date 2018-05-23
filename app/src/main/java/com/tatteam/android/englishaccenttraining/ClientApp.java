package com.tatteam.android.englishaccenttraining;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;
import tatteam.com.app_common.AppCommon;
import tatteam.com.app_common.sqlite.DatabaseLoader;

/**
 * Created by Thanh on 26/09/2015.
 */
public class ClientApp extends Application {
  @Override
  public void onCreate() {
    super.onCreate();
    AppCommon.getInstance().initIfNeeded(getApplicationContext());
    DatabaseLoader.getInstance().restoreState(getApplicationContext());

    FirebaseDatabase.getInstance().setPersistenceEnabled(true);
  }

  @Override
  public void onTerminate() {
    super.onTerminate();
  }
}
