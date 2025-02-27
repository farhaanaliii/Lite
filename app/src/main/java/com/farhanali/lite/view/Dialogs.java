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
            JSONObject jsonObject = new JSONObject(jsonResponse);
            String name = jsonObject.getString("name");
            String version = jsonObject.getString("version");
            JSONArray changesArray = jsonObject.getJSONArray("changes");

            StringBuilder changesBuilder = new StringBuilder();
            changesBuilder.append("Updates for ").append(name).append(" v").append(version).append(":\n\n");

            for (int i = 0; i < changesArray.length(); i++) {
                JSONObject change = changesArray.getJSONObject(i);
                String type = change.getString("type");
                String description = change.getString("description");
                changesBuilder.append("• ").append(type).append(": ").append(description).append("\n");
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Update Available")
                    .setMessage(changesBuilder.toString())
                    .setPositiveButton("Download Now", (dialog, which) -> {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constant.GITHUB_REPO));
                        context.startActivity(browserIntent);
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .setCancelable(false);

            AlertDialog dialog = builder.create();
            dialog.show();

        } catch (JSONException e) {
            Log.e("JSON Error", "Failed to parse JSON response", e);
        }
    }



}
