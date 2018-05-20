package com.tatteam.android.englishaccenttraining.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class DateTimeUtils {
  public static String getCurrentTimeInWorldGMT() {
    Calendar now = Calendar.getInstance();
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    return dateFormat.format(now.getTime());
  }

  public static String getChatTimeFromWorldTime(String worldTime) throws ParseException {
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    DateFormat chatTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    chatTimeFormat.setTimeZone(TimeZone.getDefault());
    return chatTimeFormat.format(dateFormat.parse(worldTime));
  }

  public static String getChatTimeToShow(String dateTime) throws ParseException {
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    DateFormat toShowFormat = new SimpleDateFormat("HH:mm");
    return toShowFormat.format(dateFormat.parse(dateTime));
  }

  public static String getChatDateToShow(String dateTime) throws ParseException {
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    DateFormat toShowFormat = new SimpleDateFormat("dd MMM yyyy");
    return toShowFormat.format(dateFormat.parse(dateTime));
  }
}
