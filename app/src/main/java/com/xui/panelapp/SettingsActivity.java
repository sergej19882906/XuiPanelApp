package com.xui.panelapp;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, new SettingsFragment())
                .commit();
        }
        
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);
            
            EditTextPreference hostPref = findPreference("panel_host");
            EditTextPreference portPref = findPreference("panel_port");
            
            if (hostPref != null) {
                hostPref.setSummaryProvider((EditTextPreference.SummaryProvider<EditTextPreference>) preference -> {
                    String text = preference.getText();
                    return (text == null || text.isEmpty()) ? "Не указано" : text;
                });
            }
            
            if (portPref != null) {
                portPref.setSummaryProvider((EditTextPreference.SummaryProvider<EditTextPreference>) preference -> {
                    String text = preference.getText();
                    return (text == null || text.isEmpty()) ? "2053" : text;
                });
            }
        }
    }
}
