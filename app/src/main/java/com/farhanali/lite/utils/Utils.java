package com.farhanali.lite.utils;

import android.net.NetworkCapabilities;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.widget.Toast;
import android.content.Context;
import android.net.ConnectivityManager;
import android.webkit.CookieManager;
import android.content.ClipData;
import android.content.ClipboardManager;

public class Utils {

    public static void Toast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    public static boolean isInternetOn(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
            if (networkCapabilities != null) {
                return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            }
        }
        return false;
    }

    public static String getCookies(String url) {
        return CookieManager.getInstance().getCookie(url);
    }

    public static void Copy(String text, Context context) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("text", text);
        clipboard.setPrimaryClip(clip);
    }

    public static void clearCache(Context context){
        context.deleteDatabase("webview.db");
        context.deleteDatabase("webviewCache.db");
        // Clear app cache directory
        java.io.File cacheDir = context.getCacheDir();
        if (cacheDir != null && cacheDir.isDirectory()) {
            deleteDir(cacheDir);
        }
    }

    public static void clearData(Context context){
        clearCache(context);
        WebStorage.getInstance().deleteAllData();
        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush();
    }

    private static void deleteDir(java.io.File dir) {
        if (dir == null || !dir.isDirectory()) return;
        java.io.File[] children = dir.listFiles();
        if (children != null) {
            for (java.io.File child : children) {
                if (child.isDirectory()) {
                    deleteDir(child);
                }
                child.delete();
            }
        }
    }
}
