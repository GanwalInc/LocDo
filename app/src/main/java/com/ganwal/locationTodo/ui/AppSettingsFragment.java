package com.ganwal.locationTodo.ui;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ganwal.locationTodo.R;

public class AppSettingsFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    final String LOG_TAG = AppSettingsFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        Preference pref = getPreferenceManager().findPreference("pref_sort_order");
        if(pref != null) {
            pref.setOnPreferenceChangeListener(this);
            if (pref instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) pref;
                String existingVal = listPreference.getValue();
                pref.setSummary(listPreference.getEntry());
            }
        }
        return rootView;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String strValue = newValue.toString();
        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(strValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        }
        return true;
    }
}



