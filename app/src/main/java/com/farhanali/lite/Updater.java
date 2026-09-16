package com.farhanali.lite;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.farhanali.lite.ui.Dialogs;
import com.farhanali.lite.ui.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Updater {
    private static final Handler main_handler = new Handler(Looper.getMainLooper());
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static void check(Context context) {
        Utils.toast(context, R.string.checking_updates);
        executor.execute(() -> {
            String json_str = fetch(Constant.VERSION_URL);
            main_handler.post(() -> on_result(context, json_str));
        });
    }

    public static void resolve_and_download(Context context) {
        executor.execute(() -> {
            String[] apk = resolve_apk();
            main_handler.post(() -> {
                if (apk == null || apk[0] == null) {
                    Utils.toast(context, R.string.download_failed);
                    return;
                }
                if (apk[1] == null || apk[1].trim().isEmpty()) {
                    Utils.toast(context, R.string.update_missing_hash);
                    return;
                }
                Dialogs.showDownloadProgressDialog(context, apk[0], apk[1]);
            });
        });
    }

    public static void start_update_download(Context context, String url, String sha256) {
        if (sha256 == null || sha256.trim().isEmpty()) {
            Utils.toast(context, R.string.update_missing_hash);
            return;
        }
        String file_name = Uri.parse(url).getLastPathSegment();
        if (file_name == null || file_name.isEmpty()) file_name = "update.apk";
        DownloadService.start_download(context, url, file_name, true, sha256);
    }

    private static void on_result(Context context, String json_str) {
        if (json_str == null) {
            Utils.toast(context, R.string.no_internet);
            return;
        }
        try {
            JSONObject json = new JSONObject(json_str);
            if (json.optLong("latest_version_code", -1) > BuildConfig.VERSION_CODE) {
                Dialogs.showUpdateDialog(context, json_str);
            } else {
                Utils.toast(context, R.string.latest_version);
            }
        } catch (Exception e) {
            Utils.toast(context, R.string.update_check_failed);
        }
    }

    private static String[] resolve_apk() {
        try {
            String json_str = fetch(Constant.GITHUB_API_RELEASES);
            if (json_str == null) return null;
            JSONArray assets = new JSONObject(json_str).getJSONArray("assets");
            for (int i = 0; i < assets.length(); i++) {
                JSONObject asset = assets.getJSONObject(i);
                if (asset.getString("name").endsWith(".apk")) {
                    String url = asset.getString("browser_download_url");
                    String sha256 = asset.optString("digest", null);
                    if (sha256 == null || sha256.isEmpty()) {
                        sha256 = asset.optString("label", null);
                    }
                    return new String[]{url, sha256};
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static String fetch(String url_str) {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url_str).openConnection();
            conn.setRequestProperty("Accept", "application/vnd.github+json");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) return null;
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}
