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
            if(p instanceof ListPreference) {
                // get list of available clicks, insert in settings
                setEntriesAndValues(p);
            }
            String value = shPref.getString(p.getKey(), "");
            setPreferenceSummary(p, value);
        }

    }

    /**
     * Sets the summary description of the setting variable
     * @param preference
     * @param value
     */
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

    /**
     * Pulls list of available click sounds from SettingsActivity, inserts into settings item
     * @param p
     */
    private void setEntriesAndValues(Preference p) {
        String[] entries = ((SettingsActivity)getActivity()).mEntries;
        String[] values = ((SettingsActivity)getActivity()).mValues;

        ListPreference lp = (ListPreference) p;
        lp.setEntries(entries);
        lp.setEntryValues(values);
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
