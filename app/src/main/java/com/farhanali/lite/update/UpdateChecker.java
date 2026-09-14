package com.farhanali.lite.update;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.farhanali.lite.BuildConfig;
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
        Utils.toast(context, R.string.checking_updates);
        EXECUTOR.execute(() -> {
            String jsonStr = fetchJson();
            MAIN_HANDLER.post(() -> onResult(context, jsonStr));
        });
    }

    private static void onResult(Context context, String jsonStr) {
        if (jsonStr == null) {
            Utils.toast(context, R.string.no_internet);
            return;
        }
        try {
            JSONObject json = new JSONObject(jsonStr);
            long currentCode = BuildConfig.VERSION_CODE;
            if (isUpdateAvailable(currentCode, json)) {
                Dialogs.showUpdateDialog(context, jsonStr);
            } else {
                Utils.toast(context, R.string.latest_version);
            }
        } catch (Exception e) {
            Utils.toast(context, R.string.update_check_failed);
        }
    }

    private static boolean isUpdateAvailable(long currentCode, JSONObject json) {
        long latestCode = json.optLong("latest_version_code", -1);
        return currentCode > 0 && latestCode > currentCode;
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
