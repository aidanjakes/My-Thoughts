package com.rilchil.mythoughts;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ThoughtDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "thoughts.db";
    private static final int DATABASE_VERSION = 1;

    public ThoughtDatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //all dates should be stored as yyyy-MM-dd and times as HH:mm:ss
        final String createTableString =
                "CREATE TABLE " + ThoughtTable.TABLE_NAME + "( " +
                 ThoughtTable.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                 ThoughtTable.COLUMN_TITLE + " TEXT, " +
                 ThoughtTable.COLUMN_THOUGHT_TEXT + " TEXT, " +
                        ThoughtTable.COLUMN_DATE + " TEXT, " +
                        ThoughtTable.COLUMN_TIME + " TEXT)";
        db.execSQL(createTableString);











    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /*
       db.delete(ThoughtTable.TABLE_NAME, null ,null);
       final String createTableString =
                "CREATE TABLE " + ThoughtTable.TABLE_NAME + "( " +
                        ThoughtTable.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        ThoughtTable.COLUMN_TITLE + " TEXT, " +
                        ThoughtTable.COLUMN_THOUGHT_TEXT + " TEXT, " +
                        ThoughtTable.COLUMN_DATE + " TEXT, " +
                        ThoughtTable.COLUMN_TIME + " TEXT)";
       db.execSQL(createTableString);

         */
       //db.execSQL("DROP TABLE IF EXISTS " + ThoughtTable.TABLE_NAME);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //db.delete(ThoughtTable.TABLE_NAME, null ,null);
        //db.delete(ThoughtTable.TABLE_NAME, null ,null);
        /*
        String title = "title";
        String thought = "thought";
        for(int i = 0; i < 500; i++){
            StringBuilder builder = new StringBuilder();
            builder.append(title).append(i);
            StringBuilder newBuilder = new StringBuilder().append(thought).append(i);
            String date = "2020-11-30";
            String time = "00:00";

            insertThought(db, builder.toString(), newBuilder.toString(), date, time);

        }

         */

    }

    public static void insertThought(SQLiteDatabase db, String title,String thought,
                                      String date, String time){
        ContentValues contentValues = new ContentValues();
        contentValues.put(ThoughtTable.COLUMN_TITLE, title);
        contentValues.put(ThoughtTable.COLUMN_DATE, date);
        contentValues.put(ThoughtTable.COLUMN_THOUGHT_TEXT, thought);
        contentValues.put(ThoughtTable.COLUMN_TIME, time);
        db.insert(ThoughtTable.TABLE_NAME, null, contentValues);

    }
}
