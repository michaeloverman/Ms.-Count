package tech.michaeloverman.android.mscount.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import timber.log.Timber;

/**
 * Created by Michael on 3/27/2017.
 */

public class ProgramProvider extends ContentProvider {

    public static final int CODE_ALL_OF_IT = 100;
    public static final int CODE_COMPOSER = 101;
    public static final int CODE_COMPOSER_WITH_PIECE = 102;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private ProgramDatabaseHelper mHelper;

    public static UriMatcher buildUriMatcher() {
        Timber.d("UriMatcher creation...");
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = ProgramDatabaseSchema.AUTHORITY;

        matcher.addURI(authority, ProgramDatabaseSchema.PATH_PROGRAMS, CODE_ALL_OF_IT);
        matcher.addURI(authority, ProgramDatabaseSchema.PATH_PROGRAMS + "/*", CODE_COMPOSER);
        matcher.addURI(authority, ProgramDatabaseSchema.PATH_PROGRAMS + "/*/*", CODE_COMPOSER_WITH_PIECE);

        return matcher;
    }


    @Override
    public boolean onCreate() {
        mHelper = new ProgramDatabaseHelper(getContext());
        Timber.d("onCreate()");
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor cursor;

        Timber.d("query()");

        switch(sUriMatcher.match(uri)) {
            case CODE_COMPOSER:
                Timber.d("UriMatcher: CODE_COMPOSER");
                String composer = uri.getLastPathSegment();
                selectionArgs = new String[] { composer };

                cursor = mHelper.getReadableDatabase().query(
                        ProgramDatabaseSchema.MetProgram.TABLE_NAME,
                        null,
                        ProgramDatabaseSchema.MetProgram.COLUMN_COMPOSER + " = ? ",
                        selectionArgs,
                        null,
                        null,
                        sortOrder);

                break;

            case CODE_COMPOSER_WITH_PIECE:
                Timber.d("UriMatcher: CODE_COMPOSER_WITH_PIECE");
                String title = uri.getLastPathSegment();
                selectionArgs = new String[] { title };
                cursor = mHelper.getReadableDatabase().query(
                        ProgramDatabaseSchema.MetProgram.TABLE_NAME,
                        null,
                        ProgramDatabaseSchema.MetProgram.COLUMN_TITLE + " = ? ",
                        selectionArgs,
                        null,
                        null,
                        sortOrder);

                break;
            case CODE_ALL_OF_IT:
                Timber.d("UriMatcher: CODE_ALL_OF_IT");
                cursor = mHelper.getReadableDatabase().query(
                        ProgramDatabaseSchema.MetProgram.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);

                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        Timber.d("insert() ~~~!!!");
        SQLiteDatabase db = mHelper.getWritableDatabase();
        boolean success = true;
        Uri returnUri;

        switch(sUriMatcher.match(uri)) {
            case CODE_ALL_OF_IT:
                db.beginTransaction();
                try {
                    long _id = db.insert(ProgramDatabaseSchema.MetProgram.TABLE_NAME, null, values);
                    if(_id == -1) {
                        Toast.makeText(getContext(), "Problem saving to database!!", Toast.LENGTH_SHORT).show();
                        success = false;
                    } else {
                        db.setTransactionSuccessful();
                        returnUri = ProgramDatabaseSchema.MetProgram.CONTENT_URI;
                        getContext().getContentResolver().notifyChange(uri, null);
                    }
                } finally {
                    db.endTransaction();
                }

                if(!success) returnUri = null;

            default:
                Toast.makeText(getContext(), "Database request improperly formatted.", Toast.LENGTH_SHORT).show();
                returnUri = null;

        }

        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        Timber.d("delete()...");
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        Timber.d("update...()");
        return 0;
    }
}
