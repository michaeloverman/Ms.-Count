/* Copyright (C) 2017 Michael Overman - All Rights Reserved */
package tech.michaeloverman.android.mscount.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import tech.michaeloverman.android.mscount.R;
import timber.log.Timber;

/**
 * Created by Michael on 3/27/2017.
 */

public final class PrefUtils {

    public static final String PREF_FAVORITE_PROGRAM = "favorite";
    public static final String PREF_DOWNBEAT_CLICK_DEFAULT = "4";
    public static final String PREF_INNERBEAT_CLICK_DEFAULT = "2";
    private static final String PREF_USE_FIREBASE = "use_firebase";
    private static final String PREF_CURRENT_TEMPO = "programmable_tempo_key";
    private static final String PREF_PIECE_KEY = "programmable_piece_id";
    private static final String PREF_WEAR_STATUS_KEY = "pref_wear_status";

    private PrefUtils() {
    }

    public static int getDownBeatClickId(Context context) {
        SharedPreferences shp = PreferenceManager.getDefaultSharedPreferences(context);
        String id = shp.getString(context.getResources().getString(R.string.down_beat_click_key),
                PREF_DOWNBEAT_CLICK_DEFAULT);
        return Integer.parseInt(id);
    }

    public static int getInnerBeatClickId(Context context) {
        SharedPreferences shp = PreferenceManager.getDefaultSharedPreferences(context);
        String id = shp.getString(context.getResources().getString(R.string.inner_beat_click_key),
                PREF_INNERBEAT_CLICK_DEFAULT);
        return Integer.parseInt(id);
    }

    public static void saveCurrentProgramToPrefs(Context context,
                                                boolean useFirebase, String key, int tempo) {
        SharedPreferences.Editor prefs = PreferenceManager
                .getDefaultSharedPreferences(context).edit();
        prefs.putBoolean(PREF_USE_FIREBASE, useFirebase);
        prefs.putString(PREF_PIECE_KEY, key);
        prefs.putInt(PREF_CURRENT_TEMPO, tempo);
        prefs.commit();
    }

    public static void saveWidgetSelectedPieceToPrefs(Context context, int key) {
        saveCurrentProgramToPrefs(context, false, Integer.toString(key), getSavedTempo(context));
    }

    public static String getSavedPieceKey(Context context) {
        SharedPreferences shp = PreferenceManager.getDefaultSharedPreferences(context);
        String key = shp.getString(PREF_PIECE_KEY, null);
        return key;
    }

    public static int getSavedTempo(Context context) {
        SharedPreferences shp = PreferenceManager.getDefaultSharedPreferences(context);
        int id = shp.getInt(PREF_CURRENT_TEMPO, 120);
        return id;
    }

    public static void saveFirebaseStatus(Context context, boolean firebase) {
        Timber.d("saving useFirebase: " + firebase);
        SharedPreferences.Editor prefs = PreferenceManager
                .getDefaultSharedPreferences(context).edit();
        prefs.putBoolean(PREF_USE_FIREBASE, firebase);
        prefs.commit();
    }

    public static boolean usingFirebase(Context context) {
        SharedPreferences shp = PreferenceManager.getDefaultSharedPreferences(context);
        boolean firebase = shp.getBoolean(PREF_USE_FIREBASE, true);
        Timber.d("checking useFirebase: " + firebase);
        return firebase;
    }

    public static void saveWearStatus(Context context, boolean status) {
        SharedPreferences.Editor prefs = PreferenceManager
                .getDefaultSharedPreferences(context).edit();
        prefs.putBoolean(PREF_WEAR_STATUS_KEY, status);
        prefs.commit();
    }

    public static boolean wearPresent(Context context) {
        SharedPreferences shp = PreferenceManager.getDefaultSharedPreferences(context);
        boolean wearStatus = shp.getBoolean(PREF_WEAR_STATUS_KEY, false);
        return wearStatus;
    }
}
