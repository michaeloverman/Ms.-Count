package tech.michaeloverman.android.mscount.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import tech.michaeloverman.android.mscount.R;

/**
 * Created by Michael on 3/27/2017.
 */

public final class PrefUtils {

    public static final String PREF_FAVORITE_PROGRAM = "favorite";
    public static final String PREF_DOWNBEAT_CLICK_DEFAULT = "4";
    public static final String PREF_INNERBEAT_CLICK_DEFAULT = "2";

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


}
