package com.tatteam.android.englishaccenttraining.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import static android.content.Context.CLIPBOARD_SERVICE;

public class CommonUtils {
  public static final String COPY_LABEL = "ESL";

  public static void copyText(Context context, String toCopyText) {
    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
    ClipData clip = ClipData.newPlainText(COPY_LABEL, toCopyText);
    clipboard.setPrimaryClip(clip);
  }
}
