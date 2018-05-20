package com.tatteam.android.englishaccenttraining.utils;

import android.Manifest;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

public class DeviceUtils {
  private String mDeviceId;

  private static DeviceUtils sInstance;

  private DeviceUtils() {
  }

  public static DeviceUtils getInstance() {
    if (sInstance == null) {
      synchronized (DeviceUtils.class) {
        if (sInstance == null) {
          sInstance = new DeviceUtils();
        }
      }
    }
    return sInstance;
  }

  public String getDeviceId(Context context) {
    if (TextUtils.isEmpty(mDeviceId)) {
      if (PermissionChecker.checkPermission(context, Manifest.permission.READ_PHONE_STATE)) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          mDeviceId = telephonyManager.getImei();
        } else {
          mDeviceId = telephonyManager.getDeviceId();
        }
      }

      if (TextUtils.isEmpty(mDeviceId))
        mDeviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
    return mDeviceId;
  }
}
