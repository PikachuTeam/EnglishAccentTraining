package com.tatteam.android.englishaccenttraining.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

public class PermissionChecker {
  public static boolean checkPermission(Context context, String permission) {
    return ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
  }
}
