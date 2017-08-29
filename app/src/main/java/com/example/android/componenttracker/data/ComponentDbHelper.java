package com.example.android.componenttracker.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.componenttracker.data.ComponentContract.ComponentEntry;


public class ComponentDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "parts.db";

    public ComponentDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_COMPONENTS_TABLE = "CREATE TABLE " + ComponentEntry.TABLE_NAME + " ("
                + ComponentEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ComponentEntry.COLUMN_NAME + " TEXT NOT NULL, "
                + ComponentEntry.COLUMN_MANUFACTURER + " TEXT NOT NULL, "
                + ComponentEntry.COLUMN_PRICE + " INTEGER NOT NULL, "
                + ComponentEntry.COLUMN_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                + ComponentEntry.COLUMN_PHOTO + " TEXT);";
        db.execSQL(SQL_CREATE_COMPONENTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
