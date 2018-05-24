package com.tatteam.android.englishaccenttraining.utils;

import android.content.Context;
import android.support.v7.app.AlertDialog;

import com.tatteam.android.englishaccenttraining.R;

public class DialogUtils {
  private static AlertDialog sLoadingDialog;

  public static void showLoadingDialog(Context context) {
    sLoadingDialog = new AlertDialog.Builder(context)
            .setView(R.layout.dialog_loading)
            .setCancelable(false)
            .create();

    sLoadingDialog.show();
  }

  public static void dismissLoadingDialog() {
    sLoadingDialog.dismiss();
    sLoadingDialog = null;
  }
}
