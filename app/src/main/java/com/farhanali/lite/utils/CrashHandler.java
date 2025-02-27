package com.farhanali.lite.utils;

import android.app.Application;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;

import com.farhanali.lite.activity.CrashActivity;

public final class CrashHandler{
    public static final UncaughtExceptionHandler DEFAULT_UNCAUGHT_EXCEPTION_HANDLER = Thread.getDefaultUncaughtExceptionHandler();
    public static void init(Application app) {
        init(app, null);
    }
    public static void init(final Application app, final String crashDir) {
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler(){
			@Override
            public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {
				try{
                	tryUncaughtException(thread, throwable);
                }catch (Throwable e){
                    e.printStackTrace();
                    if(DEFAULT_UNCAUGHT_EXCEPTION_HANDLER != null)
                        DEFAULT_UNCAUGHT_EXCEPTION_HANDLER.uncaughtException(thread, throwable);
                    }
                }
                private void tryUncaughtException(Thread thread, Throwable throwable) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss", Locale.US);
                    final String time = sdf.format(new Date());
                    String versionName = "unknown";
                    long versionCode = 0;
                    try { 
                        PackageInfo packageInfo = app.getPackageManager().getPackageInfo(app.getPackageName(), 0);
                        versionName = packageInfo.versionName;
                        versionCode = Build.VERSION.SDK_INT >= 28 ? packageInfo.getLongVersionCode()
                            : packageInfo.versionCode;
                    } catch (PackageManager.NameNotFoundException ignored) {}

                    String fullStackTrace; {
                        StringWriter sw = new StringWriter(); 
                        PrintWriter pw = new PrintWriter(sw);
                        throwable.printStackTrace(pw);
                        fullStackTrace = sw.toString();
                        pw.close();
                    }

                    String errorLog = "Time Of Crash      : " + time + "\n" +
                            "Device Manufacturer: " + Build.MANUFACTURER + "\n" +
                            "Device Model       : " + Build.MODEL + "\n" +
                            "Android Version    : " + Build.VERSION.RELEASE + "\n" +
                            "Android SDK        : " + Build.VERSION.SDK_INT + "\n" +
                            "App VersionName    : " + versionName + "\n" +
                            "App VersionCode    : " + versionCode + "\n" +
                            "\n\n" + fullStackTrace;


                    Intent intent = new Intent(app, CrashActivity.class);
                    intent.addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                                    | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
                    );
                    intent.putExtra(CrashActivity.EXTRA_CRASH_INFO, errorLog);
                    try {
                        app.startActivity(intent);
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(0);
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                        if (DEFAULT_UNCAUGHT_EXCEPTION_HANDLER != null)
                            DEFAULT_UNCAUGHT_EXCEPTION_HANDLER.uncaughtException(thread, throwable);
                    }

                }
            });
    }

    
}
