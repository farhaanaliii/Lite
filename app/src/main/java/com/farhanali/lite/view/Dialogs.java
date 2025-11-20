package com.farhanali.lite.view;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.widget.EditText;

import com.farhanali.lite.constant.Constant;
import com.farhanali.lite.utils.Utils;
import androidx.appcompat.app.AlertDialog;

import com.farhanali.lite.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Dialogs {

    public static void showCookieDialog(final Context context) {
        AlertDialog.Builder cookieDialog = new AlertDialog.Builder(context);
        final EditText edittext = new EditText(context);
        final String cookiesCopy = Utils.getCookies("https://www.facebook.com");
        edittext.setText(cookiesCopy);
        edittext.setFocusable(false);

        cookieDialog.setTitle("Cookies");
        cookieDialog.setView(edittext);
        cookieDialog.setPositiveButton("Copy to Clipboard", (dialog, whichButton) -> {
            Utils.Copy(cookiesCopy, context);
            Utils.Toast(context, "Copied!");
        });
        cookieDialog.show();
    }

    public static void showEditCookiesDialog(final Context context, final WebView webView, final CookieManager cookieManager) {
        AlertDialog.Builder cookieDialog = new AlertDialog.Builder(context);
        final EditText edittext = new EditText(context);
        final String cookiesCopy = Utils.getCookies(Constant.FACEBOOK_HOME);
        edittext.setText(cookiesCopy);
        edittext.setFocusable(true);

        cookieDialog.setTitle("Edit Cookies");
        cookieDialog.setView(edittext);
        cookieDialog.setPositiveButton("Save Cookies", (dialog, whichButton) -> {
            cookieManager.removeAllCookies(null);
            String[] cookies = edittext.getText().toString().trim().split(";");
            for (String cookie : cookies) {
                cookieManager.setCookie(Constant.FACEBOOK_HOME, cookie + ";");
            }
            Utils.Toast(context, "Cookies Saved!");
            webView.loadUrl(Constant.FACEBOOK_HOME);
        });
        cookieDialog.setNegativeButton("Cancel", (dialog, whichButton) -> {});
        cookieDialog.show();
    }

    public static void showCurrentUrlDialog(final Context context, final String url) {
        AlertDialog.Builder urlDialog = new AlertDialog.Builder(context);
        final EditText edittext = new EditText(context);
        edittext.setText(url);
        edittext.setFocusable(true);
        edittext.setEnabled(false);

        urlDialog.setTitle("Current URL");
        urlDialog.setView(edittext);
        urlDialog.setPositiveButton("Copy", (dialog, whichButton) -> {
            Utils.Copy(url, context);
            Utils.Toast(context, "Copied!");
        });
        urlDialog.setNegativeButton("Cancel", (dialog, whichButton) -> {});
        urlDialog.show();
    }

    public static void showUpdateDialog(Context context, String jsonResponse) {
        try {
            JSONObject json = new JSONObject(jsonResponse);
            String latestVersion = json.getString("latest_version");
            String downloadUrl = json.getString("download_url");
            JSONArray changelog = json.getJSONArray("changelog");
            
            SpannableStringBuilder message = new SpannableStringBuilder();
            message.append("Version ").append(latestVersion).append(" is available!\n\n");
            
            for (int i = 0; i < changelog.length(); i++) {
                JSONObject release = changelog.getJSONObject(i);
                if (release.getString("version").equals(latestVersion)) {
                    JSONArray changes = release.getJSONArray("changes");
                    message.append("What's new:\n");
                    
                    for (int j = 0; j < changes.length(); j++) {
                        JSONObject change = changes.getJSONObject(j);
                        String type = change.getString("type");
                        String description = change.getString("description");
                        
                        message.append(getChangeIcon(type))
                            .append(" ")
                            .append(description)
                            .append("\n");
                    }
                    break;
                }
            }
            
            message.append("\nWould you like to download the update?");
            
            new AlertDialog.Builder(context)
                .setTitle("Update Available")
                .setMessage(message)
                .setPositiveButton("Download", (dialog, which) -> {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl));
                    context.startActivity(browserIntent);
                })
                .setNegativeButton("Later", null)
                .show();
                
        } catch (Exception e) {
            new AlertDialog.Builder(context)
                .setTitle("Update Available")
                .setMessage("A new version is available!")
                .setPositiveButton("OK", null)
                .show();
        }
    }

    private static String getChangeIcon(String type) {
        switch (type) {
            case "feature": return "✨"; // New feature
            case "fix": return "🐛";     // Bug fix  
            case "improvement": return "⚡"; // Improvement
            case "security": return "🔒"; // Security fix
            case "performance": return "🚀"; // Performance
            case "ui": return "🎨";      // UI/UX change
            default: return "•";
        }
    }



}
