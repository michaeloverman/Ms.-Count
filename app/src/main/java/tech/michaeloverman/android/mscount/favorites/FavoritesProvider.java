package tech.michaeloverman.android.mscount.favorites;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Class to access SQLite database storing movies marked as favorites.
 *
 * Created by Michael on 12/20/2016.
 */

public class FavoritesProvider extends ContentProvider {
    
    public static final String TAG = FavoritesProvider.class.getSimpleName();
    
    public static final int CODE_FAVORITES = 222;
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    public static final String ACTION_DATA_UPDATED = "tech.michaeloverman.android.mscount.ACTION_DATA_UPDATED";
    private FavoritesDBHelper mDBHelper;

    public static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = FavoritesContract.CONTENT_AUTHORITY;
        
        matcher.addURI(authority, FavoritesContract.PATH_FAVORITES, CODE_FAVORITES);
        
        return matcher;
    }
    
    @Override
    public boolean onCreate() {
        mDBHelper = new FavoritesDBHelper(getContext());
        return true;
    }
    
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection,
                        @Nullable String selection, @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {
        Cursor cursor;
        
        switch (sUriMatcher.match(uri)) {
            case CODE_FAVORITES:
                cursor = mDBHelper.getReadableDatabase().query(
                        FavoritesContract.FavoriteEntry.TABLE_NAME,
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
        throw new RuntimeException("getType not implemented");
    }
    
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues value) {
        final SQLiteDatabase db = mDBHelper.getWritableDatabase();
        long _id = 0L;
        
        switch(sUriMatcher.match(uri)) {
            case CODE_FAVORITES:
                db.beginTransaction();
                int rowsInserted = 0;
                try {
//                    int movieId = value.getAsInteger(FavoritesContract.FavoriteEntry.COLUMN_MOVIE_ID);
                    _id = db.insert(FavoritesContract.FavoriteEntry.TABLE_NAME, null, value);
                    if(_id != -1) {
                        rowsInserted++;
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();;
                }
                if(rowsInserted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return uri;
        }

//        sendUpdateIntent();

        return uri;
    }
    
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        
        int numRowsDeleted;
        
        if(selection == null) selection = "1";
        
        switch(sUriMatcher.match(uri)) {
            case CODE_FAVORITES:
                numRowsDeleted = mDBHelper.getWritableDatabase().delete(
                        FavoritesContract.FavoriteEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (numRowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

//        sendUpdateIntent();
        return numRowsDeleted;
    }

    
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
