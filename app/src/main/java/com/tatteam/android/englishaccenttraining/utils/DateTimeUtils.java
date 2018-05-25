package com.tatteam.android.englishaccenttraining.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
    Date date = dateFormat.parse(dateTime);

    Calendar today = Calendar.getInstance();
    Calendar toCompareDate = Calendar.getInstance();
    toCompareDate.setTime(date);

    if (toCompareDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            toCompareDate.get(Calendar.MONTH) == today.get(Calendar.MONTH)) {

      if (toCompareDate.get(Calendar.DATE) == today.get(Calendar.DATE))
        return "Today";

      if (today.get(Calendar.DATE) - toCompareDate.get(Calendar.DATE) == 1)
        return "Yesterday";
    }

    DateFormat toShowFormat = new SimpleDateFormat("dd MMM yyyy");
    return toShowFormat.format(date);
  }

  public static boolean isSameDate(String time1, String time2) throws ParseException {
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Calendar dateWithoutTime1 = Calendar.getInstance();
    dateWithoutTime1.setTime(dateFormat.parse(time1));
    dateWithoutTime1.set(Calendar.HOUR_OF_DAY, 0);
    dateWithoutTime1.set(Calendar.MINUTE, 0);
    dateWithoutTime1.set(Calendar.SECOND, 0);
    dateWithoutTime1.set(Calendar.MILLISECOND, 0);

    Calendar dateWithoutTime2 = Calendar.getInstance();
    dateWithoutTime2.setTime(dateFormat.parse(time2));
    dateWithoutTime2.set(Calendar.HOUR_OF_DAY, 0);
    dateWithoutTime2.set(Calendar.MINUTE, 0);
    dateWithoutTime2.set(Calendar.SECOND, 0);
    dateWithoutTime2.set(Calendar.MILLISECOND, 0);

    return dateWithoutTime1.compareTo(dateWithoutTime2) == 0;
  }

  public static int compareTwoTime(String time1, String time2) throws ParseException {
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Calendar dateTime1 = Calendar.getInstance();
    dateTime1.setTime(dateFormat.parse(time1));

    Calendar dateTime2 = Calendar.getInstance();
    dateTime2.setTime(dateFormat.parse(time2));

    return dateTime1.compareTo(dateTime2);
  }
}
