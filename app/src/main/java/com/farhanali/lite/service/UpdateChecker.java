package com.farhanali.lite.service;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.farhanali.lite.constant.Constant;
import com.farhanali.lite.utils.Utils;
import com.farhanali.lite.view.Dialogs;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.github.zafarkhaja.semver.Version;

public class UpdateChecker {
    private Version currentVersion;
    private Version latestVersion;
    private String jsonResponse;
    private final Context mContext;
    private final ExecutorService executor;

    public UpdateChecker(Context context) {
        mContext = context;
        executor = Executors.newSingleThreadExecutor();
        try {
            PackageInfo pInfo = mContext.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            currentVersion = Version.valueOf(pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void execute() {
        executor.execute(() -> {
            checkUpdate();
            if (latestVersion != null && latestVersion.greaterThan(currentVersion)) {
                showDialog();
            } else {
                ((AppCompatActivity) mContext).runOnUiThread(() -> {
                    Utils.Toast(mContext, "You are using the latest version.");
                });
                Log.d("No Update", "No update available");
            }
        });
    }

    private void checkUpdate() {
        ((AppCompatActivity) mContext).runOnUiThread(() -> {
            Utils.Toast(mContext, "Checking for updates...");
        });

        StringBuilder responseBuilder = new StringBuilder();
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(Constant.VERSION_URL).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        responseBuilder.append(line);
                    }
                }
            } else {
                Log.e("Network Error", "Response Code: " + connection.getResponseCode());
            }
        } catch (Exception e) {
            Log.e("Error", "Failed to fetch data", e);
        }

        try {
            jsonResponse = responseBuilder.toString();
            JSONObject jsonObject = new JSONObject(jsonResponse);
            latestVersion = Version.valueOf(jsonObject.getString("version"));
        } catch (Exception e) {
            Log.e("Parsing Error", "Failed to parse JSON", e);
        }
    }

    private void showDialog() {
        Log.d("Check Result", "Check completed");
        ((AppCompatActivity) mContext).runOnUiThread(() -> {
            Log.d("Update Available", "A new version is available");
            Dialogs.showUpdateDialog(mContext, jsonResponse);
        });
    }
}
