package com.chernegasergiy.battery.ui;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import com.chernegasergiy.battery.R;

import android.content.Intent;
import android.content.SharedPreferences;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String ACTION_SETTINGS_CHANGED = "com.chernegasergiy.battery.SETTINGS_CHANGED";
    public static final String EXTRA_SETTING_KEY = "setting_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Intent intent = new Intent(ACTION_SETTINGS_CHANGED);
        intent.putExtra(EXTRA_SETTING_KEY, key);
        sendBroadcast(intent);
    }
    
    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
