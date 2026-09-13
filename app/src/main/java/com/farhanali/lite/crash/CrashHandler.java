package com.farhanali.lite.crash;

import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Process;
import android.util.Log;

import androidx.core.content.pm.PackageInfoCompat;

import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class CrashHandler{
    private static final UncaughtExceptionHandler DEFAULT_HANDLER = Thread.getDefaultUncaughtExceptionHandler();

    public static void init(Application app) {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            try {
                handleCrash(app, throwable);
            } catch (Throwable e) {
                Log.e("CrashHandler", "Failed to handle crash", e);
                if (DEFAULT_HANDLER != null) {
                    DEFAULT_HANDLER.uncaughtException(thread, throwable);
                } else {
                    Process.killProcess(Process.myPid());
                    System.exit(10);
                }
            }
        });
    }

    private static void handleCrash(Application app, Throwable throwable) {
        String time = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss", Locale.US).format(new Date());
        String versionName = "unknown";
        long versionCode = 0;

        try {
            PackageInfo packageInfo = app.getPackageManager().getPackageInfo(app.getPackageName(), 0);
            versionName = packageInfo.versionName;
            versionCode = PackageInfoCompat.getLongVersionCode(packageInfo);
        } catch (PackageManager.NameNotFoundException ignored) {}

        String errorLog = "Time Of Crash      : " + time + "\n" +
                "Device Manufacturer: " + Build.MANUFACTURER + "\n" +
                "Device Model       : " + Build.MODEL + "\n" +
                "Android Version    : " + Build.VERSION.RELEASE + "\n" +
                "Android SDK        : " + Build.VERSION.SDK_INT + "\n" +
                "App VersionName    : " + versionName + "\n" +
                "App VersionCode    : " + versionCode + "\n\n" +
                Log.getStackTraceString(throwable);

        Intent intent = new Intent(app, CrashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(CrashActivity.EXTRA_CRASH_INFO, errorLog);

        app.startActivity(intent);
        Process.killProcess(Process.myPid());
        System.exit(0);
    }
}
