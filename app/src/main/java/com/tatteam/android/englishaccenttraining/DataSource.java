package com.tatteam.android.englishaccenttraining;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

/**
 * Created by Thanh on 15/09/2015.
 */
public class DataSource {
    private static DataSource instance;
    private Context context;
    private SQLiteDatabase sqLiteDatabase;

    private DataSource() {
    }

    public static DataSource getInstance() {
        if (instance == null) {
            instance = new DataSource();
        }
        return instance;
    }

    public void init(Context context) {
        this.context = context;
    }

    //import db from assets if need and open connection
    public void createDatabaseIfNeed() {
        this.openConnection();
    }

    private void openConnection() {
        if (sqLiteDatabase == null || !sqLiteDatabase.isOpen()) {
            AssetDataBaseOpenHelper assetDatabaseOpenHelper = new AssetDataBaseOpenHelper(context);
            sqLiteDatabase = assetDatabaseOpenHelper.openDatabase();
        }
    }

    private void closeConnection() {
        if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
            sqLiteDatabase.close();
        }
    }

    public void destroy() {
        closeConnection();
        instance = null;
    }


    //query from database
    public ArrayList<Lesson> getListLesson(){
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
        return lessonsList;
    }
}

