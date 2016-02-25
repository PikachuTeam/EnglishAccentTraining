package com.tatteam.android.englishaccenttraining;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import tatteam.com.app_common.sqlite.BaseDataSource;

/**
 * Created by Thanh on 15/09/2015.
 */
public class DataSource extends BaseDataSource{


    //query from database
    public static ArrayList<Lesson> getListLesson(){
        SQLiteDatabase sqLiteDatabase = openConnection();
        ArrayList<Lesson> lessonsList = new ArrayList<>();
        Cursor cursor = sqLiteDatabase.rawQuery("select * from practice",null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {

            Lesson lesson = new Lesson();
            lesson.setId(cursor.getInt(4));
            lesson.setLessonName(cursor.getString(3));
            lesson.setMusicName(cursor.getString(2));
            lesson.setTranscription(cursor.getString(1));
            lesson.setReducedSpeech(cursor.getString(0));
            lessonsList.add(lesson);
            cursor.moveToNext();
        }
        cursor.close();

        closeConnection();
        return lessonsList;
    }
}

