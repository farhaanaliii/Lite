package com.farhanali.lite.app;

import android.app.Application;
import com.farhanali.lite.utils.CrashHandler;

public class App extends Application {
    private static App app;
    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        CrashHandler.init(this);
    }
    public static App getApp() {
        return app;
    }

}
