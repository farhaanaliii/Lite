package com.farhanali.lite.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.farhanali.lite.constant.Constant;

public class Settings {
    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    public Settings(Context context) {
        sharedPreferences = context.getSharedPreferences(Constant.SHARED_PREFS, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public boolean isDesktopModeEnabled() {
        return sharedPreferences.getBoolean(Constant.PREF_KEY_IS_DESKTOP, false);
    }

    public void setDesktopModeEnabled(boolean isEnabled) {
        editor.putBoolean(Constant.PREF_KEY_IS_DESKTOP, isEnabled).apply();
    }

    public void saveUserAgent(String userAgent) {
        editor.putString(Constant.PREF_KEY_USER_AGENT, userAgent).apply();
    }

    public String getUserAgent() {
        return sharedPreferences.getString(Constant.PREF_KEY_USER_AGENT, "");
    }

    public void saveCustomUserAgent(String userAgent) {
        editor.putString(Constant.PREF_KEY_CUSTOM_USER_AGENT, userAgent).apply();
    }

    public String getCustomUserAgent() {
        return sharedPreferences.getString(Constant.PREF_KEY_CUSTOM_USER_AGENT, "");
    }

    public boolean isJavaScriptEnabled() {
        return sharedPreferences.getBoolean(Constant.PREF_KEY_JAVASCRIPT, true);
    }

    public void setJavaScriptEnabled(boolean isEnabled) {
        editor.putBoolean(Constant.PREF_KEY_JAVASCRIPT, isEnabled).apply();
    }

    public void clearCache() {
        editor.remove(Constant.PREF_KEY_CACHE).apply();
    }

    public void resetToDefaults() {
        editor.clear().apply();
    }
}
