package com.farhanali.lite.app;

import android.app.Application;
import com.farhanali.lite.utils.CrashHandler;

public class App extends Application {
    private static App app;
    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        applyTheme(new com.farhanali.lite.utils.Settings(this).getTheme());
        CrashHandler.init(this);
    }
    
    public static void applyTheme(String theme) {
        if ("dark".equals(theme)) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
        } else if ("light".equals(theme)) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }
    
    public static App getApp() {
        return app;
    }

}
