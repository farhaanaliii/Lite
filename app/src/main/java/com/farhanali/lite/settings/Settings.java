package com.farhanali.lite.settings;

import android.content.Context;
import android.content.SharedPreferences;

import com.farhanali.lite.Constant;

public class Settings {
    private final SharedPreferences sharedPreferences;

    public Settings(Context context) {
        sharedPreferences = context.getSharedPreferences(Constant.SHARED_PREFS, Context.MODE_PRIVATE);
    }

    public boolean isDesktopModeEnabled() {
        return sharedPreferences.getBoolean(Constant.PREF_KEY_IS_DESKTOP, false);
    }

    public void setDesktopModeEnabled(boolean isEnabled) {
        sharedPreferences.edit().putBoolean(Constant.PREF_KEY_IS_DESKTOP, isEnabled).apply();
    }

    public void saveUserAgent(String userAgent) {
        sharedPreferences.edit().putString(Constant.PREF_KEY_USER_AGENT, userAgent).apply();
    }

    public String getUserAgent() {
        return sharedPreferences.getString(Constant.PREF_KEY_USER_AGENT, "");
    }

    public void saveCustomUserAgent(String userAgent) {
        sharedPreferences.edit().putString(Constant.PREF_KEY_CUSTOM_USER_AGENT, userAgent).apply();
    }

    public String getCustomUserAgent() {
        return sharedPreferences.getString(Constant.PREF_KEY_CUSTOM_USER_AGENT, "");
    }

    public boolean isJavaScriptEnabled() {
        return sharedPreferences.getBoolean(Constant.PREF_KEY_JAVASCRIPT, true);
    }

    public void setJavaScriptEnabled(boolean isEnabled) {
        sharedPreferences.edit().putBoolean(Constant.PREF_KEY_JAVASCRIPT, isEnabled).apply();
    }

    public String getTheme() {
        return sharedPreferences.getString(Constant.PREF_KEY_THEME, "system");
    }

    public void setTheme(String theme) {
        sharedPreferences.edit().putString(Constant.PREF_KEY_THEME, theme).apply();
    }

    public void resetToDefaults() {
        sharedPreferences.edit().clear().apply();
    }
}
