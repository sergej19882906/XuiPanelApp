package com.xui.panelapp;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;

import java.net.InetAddress;
import java.util.regex.Pattern;

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

        private static final Pattern IP_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
        );

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

                hostPref.setOnPreferenceChangeListener((preference, newValue) -> {
                    String host = (String) newValue;
                    if (host == null || host.trim().isEmpty()) {
                        Toast.makeText(getContext(), "Хост не может быть пустым", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    if (!isValidHost(host.trim())) {
                        Toast.makeText(getContext(), "Неверный формат хоста (IP или домен)", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    return true;
                });
            }

            if (portPref != null) {
                portPref.setSummaryProvider((EditTextPreference.SummaryProvider<EditTextPreference>) preference -> {
                    String text = preference.getText();
                    return (text == null || text.isEmpty()) ? "2053" : text;
                });

                portPref.setOnPreferenceChangeListener((preference, newValue) -> {
                    String portStr = (String) newValue;
                    if (portStr == null || portStr.trim().isEmpty()) {
                        Toast.makeText(getContext(), "Порт не может быть пустым", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    if (!isValidPort(portStr.trim())) {
                        Toast.makeText(getContext(), "Порт должен быть от 1 до 65535", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    return true;
                });
            }
        }

        private boolean isValidHost(String host) {
            if (host == null || host.isEmpty()) {
                return false;
            }
            if (IP_PATTERN.matcher(host).matches()) {
                return true;
            }
            try {
                InetAddress.getByName(host);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        private boolean isValidPort(String portStr) {
            try {
                int port = Integer.parseInt(portStr);
                return port >= 1 && port <= 65535;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }
}
