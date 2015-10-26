package com.tatteam.android.englishaccenttraining;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Date;

/**
 * Created by ThanhNH on 5/2/2015.
 */
public class LocalSharedPreferManager {
    private static final String PREF_NAME = "english_accent_training_prefer";
    private static LocalSharedPreferManager instance;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private LocalSharedPreferManager() {
    }

    public static LocalSharedPreferManager getInstance() {
        if (instance == null) {
            instance = new LocalSharedPreferManager();
        }
        return instance;
    }

    public void init(Context context) {
        if (pref == null) {
            pref = context.getSharedPreferences(PREF_NAME, context.MODE_PRIVATE);
            editor = pref.edit();
        }
    }


    public void setIsRateApp(boolean isRateApp){
        editor.putBoolean("android_rate_app", isRateApp);
        editor.commit();
    }

    public boolean isRateApp(){
        return pref.getBoolean("android_rate_app", false);
    }

    public void setRateAppRemindInterval() {
        editor.putLong("android_rate_remind_interval", (new Date()).getTime());
        editor.commit();
    }

    public long getRateAppRemindInterval() {
        return pref.getLong("android_rate_remind_interval", 0);
    }

    public void resetRateAppLaunchTime() {
        editor.putLong("android_rate_launch_time", (0));
        editor.commit();
    }

    public void setRateAppLaunchTime() {
        long launchTime = getRateAppRateAppLaunchTime() + 1;
        editor.putLong("android_rate_launch_time", (launchTime));
        editor.commit();
    }

    public long getRateAppRateAppLaunchTime() {
        return pref.getLong("android_rate_launch_time", 0);
    }


    public boolean isRateAppOverDate(int threshold) {
        return (new Date()).getTime() - getRateAppRemindInterval() >= (long) (threshold * 24 * 60 * 60 * 1000);
    }

    public boolean isRateAppOverLaunchTime(long threshold) {
        return getRateAppRateAppLaunchTime() >= threshold;
    }

    public void destroy() {
        instance = null;
    }

}
