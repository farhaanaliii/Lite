package com.farhanali.lite.update;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.core.content.pm.PackageInfoCompat;

import com.farhanali.lite.Constant;
import com.farhanali.lite.R;
import com.farhanali.lite.ui.Dialogs;
import com.farhanali.lite.ui.Utils;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UpdateChecker {
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    public static void check(Context context) {
        Utils.Toast(context, context.getString(R.string.checking_updates));
        EXECUTOR.execute(() -> {
            String jsonStr = fetchJson();
            MAIN_HANDLER.post(() -> onResult(context, jsonStr));
        });
    }

    private static void onResult(Context context, String jsonStr) {
        if (jsonStr == null) {
            Utils.Toast(context, context.getString(R.string.no_internet));
            return;
        }
        try {
            JSONObject json = new JSONObject(jsonStr);
            PackageInfo pInfo = getPackageInfo(context);
            long currentCode = pInfo != null ? PackageInfoCompat.getLongVersionCode(pInfo) : 0;
            if (isUpdateAvailable(currentCode, json)) {
                Dialogs.showUpdateDialog(context, jsonStr);
            } else {
                Utils.Toast(context, context.getString(R.string.latest_version));
            }
        } catch (Exception e) {
            Utils.Toast(context, context.getString(R.string.update_check_failed));
        }
    }

    private static boolean isUpdateAvailable(long currentCode, JSONObject json) {
        long latestCode = json.optLong("latest_version_code", -1);
        return currentCode > 0 && latestCode > currentCode;
    }

    private static PackageInfo getPackageInfo(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                return context.getPackageManager().getPackageInfo(
                    context.getPackageName(),
                    PackageManager.PackageInfoFlags.of(0)
                );
            }
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String fetchJson() {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(Constant.VERSION_URL).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                return sb.toString();
            }
        } catch (Exception ignored) {
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
