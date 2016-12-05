package com.ganwal.locationTodo.ui;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Shows app Settings
 */

public class AppSettingsActivity extends PreferenceActivity {

    public static final String LOG_TAG = AppSettingsActivity.class.getSimpleName();
    public static final String PREF_CURRENT_USER_ID = "current_user_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new AppSettingsFragment()).commit();

    }


}