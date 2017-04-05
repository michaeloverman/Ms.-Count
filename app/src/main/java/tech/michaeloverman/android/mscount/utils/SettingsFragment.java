package tech.michaeloverman.android.mscount.utils;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;

import tech.michaeloverman.android.mscount.R;

/**
 * Created by Michael on 4/4/2017.
 */

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("down_beat_click")) {
//            PrefUtils.resetDownBeatClick(getContext());
        } else if (key.equals("inner_beat_click")) {
//            PrefUtils.resetInnerBeatClick(getContext());
        }
        Preference pref = findPreference(key);
        if(pref != null) {
            setPreferenceSummary(pref, sharedPreferences.getString(key, ""));
        }
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_general);

        SharedPreferences shPref = getPreferenceScreen().getSharedPreferences();
        PreferenceScreen prefScreen = getPreferenceScreen();
        int count = prefScreen.getPreferenceCount();
        for (int i = 0; i < count; i++) {
            Preference p = prefScreen.getPreference(i);
            String value = shPref.getString(p.getKey(), "");
            setPreferenceSummary(p, value);
        }
    }

    private void setPreferenceSummary(Preference preference, Object value) {
        String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            ListPreference listPref = (ListPreference) preference;
            int prefIndex = listPref.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPref.getEntries()[prefIndex]);
            }
        } else {
            preference.setSummary(stringValue);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}