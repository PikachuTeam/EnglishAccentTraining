package com.tatteam.android.englishaccenttraining;

import android.content.Context;

import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Created by ThanhNH-Mac on 11/24/16.
 */

public class LogEvents {

    private static LogEvents instance;

    private Context context;
    private FirebaseAnalytics firebaseAnalytics;

    private LogEvents() {
    }
    public static LogEvents getInstance() {
        if (instance == null) {
            instance = new LogEvents();
        }
        return instance;
    }

    public void init(Context context){
        this.context = context;

//        firebaseAnalytics

    }
}
