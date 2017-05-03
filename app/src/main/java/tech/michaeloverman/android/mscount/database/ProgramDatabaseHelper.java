/* Copyright (C) 2017 Michael Overman - All Rights Reserved */
package tech.michaeloverman.android.mscount.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import timber.log.Timber;

import static tech.michaeloverman.android.mscount.database.ProgramDatabaseSchema.MetProgram;

/**
 * Created by Michael on 3/27/2017.
 */

public class ProgramDatabaseHelper extends SQLiteOpenHelper {

    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "programDatabase.db";


    public ProgramDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
//        Timber.d("ProgramDatabaseHelper constructor");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Timber.d("Database Helper onCreate()");

        final String SQL_BUILDER_STRING = "CREATE TABLE " + MetProgram.TABLE_NAME + " ("
                + MetProgram._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + MetProgram.COLUMN_COMPOSER + " TEXT NOT NULL, "
                + MetProgram.COLUMN_TITLE + " TEXT NOT NULL, "
                + MetProgram.COLUMN_PRIMARY_SUBDIVISIONS + " INTEGER NOT NULL, "
                + MetProgram.COLUMN_COUNTOFF_SUBDIVISIONS + " INTEGER NOT NULL, "
                + MetProgram.COLUMN_DEFAULT_TEMPO + " INTEGER NOT NULL, "
                + MetProgram.COLUMN_DEFAULT_RHYTHM + " INTEGER NOT NULL, "
                + MetProgram.COLUMN_TEMPO_MULTIPLIER + " REAL, "
                + MetProgram.COLUMN_MEASURE_COUNT_OFFSET + " INTEGER, "
                + MetProgram.COLUMN_DATA_ARRAY + " TEXT NOT NULL, "
                + MetProgram.COLUMN_FIREBASE_ID + " TEXT, "
                + MetProgram.COLUMN_CREATOR_ID + " TEXT);";

        db.execSQL(SQL_BUILDER_STRING);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //TODO This must be fixed before actual release to save users' data if/when db is updated
        db.execSQL(" DROP TABLE IF EXISTS " + DATABASE_NAME);

        onCreate(db);
    }
}
