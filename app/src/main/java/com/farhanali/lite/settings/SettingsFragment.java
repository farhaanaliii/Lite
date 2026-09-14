package com.farhanali.lite.settings;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.farhanali.lite.App;
import com.farhanali.lite.Constant;
import com.farhanali.lite.R;
import com.farhanali.lite.ui.Utils;

public class SettingsFragment extends PreferenceFragmentCompat {

    private Settings settings;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setSharedPreferencesName(Constant.SHARED_PREFS);
        setPreferencesFromResource(R.xml.activity_settings, rootKey);
        settings = new Settings(requireContext());

        ListPreference themePref = findPreference(Constant.PREF_KEY_THEME);
        if (themePref != null) {
            themePref.setOnPreferenceChangeListener((preference, newValue) -> {
                String theme = (String) newValue;
                settings.setTheme(theme);
                App.applyTheme(theme);
                return true;
            });
        }

        Preference clearCachePref = findPreference(Constant.PREF_KEY_CACHE);
        if (clearCachePref != null) {
            clearCachePref.setOnPreferenceClickListener(preference -> {
                Utils.clearCache(requireContext());
                Utils.toast(getContext(), getString(R.string.cache_cleared));
                return true;
            });
        }

        Preference clearStoragePref = findPreference(Constant.PREF_KEY_DATA);
        if (clearStoragePref != null) {
            clearStoragePref.setOnPreferenceClickListener(preference -> {
                Utils.clearData(requireContext());
                Utils.toast(getContext(), getString(R.string.storage_cleared));
                return true;
            });
        }

        Preference resetSettingsPref = findPreference(Constant.PREF_KEY_RESET);
        if (resetSettingsPref != null) {
            resetSettingsPref.setOnPreferenceClickListener(preference -> {
                settings.saveCustomUserAgent("");
                settings.setJavaScriptEnabled(true);
                Utils.toast(getContext(), getString(R.string.settings_reset));
                return true;
            });
        }

        EditTextPreference userAgentPref = findPreference(Constant.PREF_KEY_CUSTOM_USER_AGENT);
        if (userAgentPref != null) {
            userAgentPref.setDialogLayoutResource(R.layout.dialog_edittext_useragent);
            userAgentPref.setText(settings.getCustomUserAgent());
            userAgentPref.setOnPreferenceChangeListener((preference, newValue) -> {
                String newUserAgent = (String) newValue;
                settings.saveCustomUserAgent(newUserAgent);
                Utils.toast(getContext(), getString(R.string.user_agent_updated));
                return true;
            });
        }

        SwitchPreferenceCompat jsEnabledPref = findPreference(Constant.PREF_KEY_JAVASCRIPT);
        if (jsEnabledPref != null) {
            jsEnabledPref.setChecked(settings.isJavaScriptEnabled());
            jsEnabledPref.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean isEnabled = (Boolean) newValue;
                settings.setJavaScriptEnabled(isEnabled);
                Utils.toast(getContext(), getString(R.string.javascript_updated));
                return true;
            });
        }
    }
}
