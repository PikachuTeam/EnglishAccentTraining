package com.tatteam.android.englishaccenttraining;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Thanh on 13/09/2015.
 */
public class AssetDataBaseOpenHelper {
    private static final String DB_NAME = "trainyouraccent.db";

    private Context context;

    public AssetDataBaseOpenHelper(Context context) {
        this.context = context;
    }

    public SQLiteDatabase openDatabase() {
        String dbPath = "data/data/" + context.getPackageName() + "/databases/";
        File dbFile = new File(dbPath);
        if (!dbFile.exists()) {
            try {
                dbFile.mkdir();
                dbFile = new File(dbPath + DB_NAME);
                dbFile.createNewFile();
                copyDatabase(dbFile);
            } catch (IOException e) {
                throw new RuntimeException("Error creating source database", e);
            }
        } else {
            dbFile = new File(dbPath + DB_NAME);
            if(!dbFile.exists()){
                try {
                    dbFile.createNewFile();
                    copyDatabase(dbFile);
                } catch (IOException e) {
                    throw new RuntimeException("Error creating source database", e);
                }
            }
        }
        return SQLiteDatabase.openDatabase(dbFile.getPath(), null, SQLiteDatabase.OPEN_READWRITE);

    }

    private void copyDatabase(File dbFile) throws IOException {
        InputStream is = context.getAssets().open(DB_NAME);
        OutputStream os = new FileOutputStream(dbFile);

        byte[] buffer = new byte[1024];
        while (is.read(buffer) > 0) {
            os.write(buffer);
        }

        os.flush();
        os.close();
        is.close();
    }
}

