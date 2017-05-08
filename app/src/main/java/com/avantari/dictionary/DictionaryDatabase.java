package com.avantari.dictionary;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.List;
import java.util.concurrent.TimeUnit;

class DictionaryDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "MyDBName.db";
    private static final String CONTACTS_TABLE_NAME = "dictionary";
    private static final String CONTACTS_COLUMN_ID = "id";
    private static final String CONTACTS_COLUMN_WORD = "word";
    private static final String CONTACTS_COLUMN_Description = "description";

    private static DictionaryDatabase database = null;

    private DictionaryDatabase(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    static DictionaryDatabase getInstance(Context context) {
        if (database == null) {
            database = new DictionaryDatabase(context.getApplicationContext());
        }
        return database;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // create Table
        db.execSQL(
                "create table " + CONTACTS_TABLE_NAME +
                        "(" + CONTACTS_COLUMN_ID + " integer not null primary key, " +
                        CONTACTS_COLUMN_WORD + " text," +
                        CONTACTS_COLUMN_Description + " text" +
                        ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // drop every time and create new table
        db.execSQL("DROP TABLE IF EXISTS " + CONTACTS_TABLE_NAME);
        onCreate(db);
    }

    String insertValue(List<String> word, List<String> description) {
        SQLiteDatabase db = this.getReadableDatabase();
        db.beginTransaction();
        long startTime = System.nanoTime();
        try {
            for (int i = 0; i < description.size(); i++) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(CONTACTS_COLUMN_WORD, word.get(i));
                contentValues.put(CONTACTS_COLUMN_Description, description.get(i));
//                Log.i("Inserting at: " + i, " " + word.get(i) + " " + description.get(i));
                db.insert(CONTACTS_TABLE_NAME, null, contentValues);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("Data Inserting Error", e.getLocalizedMessage());
        } finally {
            db.endTransaction();
        }
        db.close();
        long stopTime = System.nanoTime();
//        Log.i("Data Inserted",String.valueOf(TimeUnit.SECONDS.convert(stopTime - startTime, TimeUnit.NANOSECONDS)));
        return String.valueOf(TimeUnit.SECONDS.convert(stopTime - startTime, TimeUnit.NANOSECONDS));
    }
}
