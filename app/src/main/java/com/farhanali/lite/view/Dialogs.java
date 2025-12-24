package com.farhanali.lite.view;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import com.farhanali.lite.R;
import com.farhanali.lite.constant.Constant;
import com.farhanali.lite.utils.Utils;
import com.farhanali.lite.utils.CookieFormatter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import org.json.JSONArray;
import org.json.JSONObject;

public class Dialogs {

    public static void showCookieDialog(final Context context) {
        MaterialAlertDialogBuilder cookieDialog = new MaterialAlertDialogBuilder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_edittext, null);
        
        final TextInputEditText editText = view.findViewById(R.id.edit_text);
        final TextInputLayout textInputLayout = view.findViewById(R.id.text_input_layout);
        final Spinner formatSpinner = view.findViewById(R.id.format_spinner);
        
        textInputLayout.setHint("Cookies");
        editText.setFocusable(false);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
            context,
            R.array.cookie_formats,
            android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        formatSpinner.setAdapter(adapter);

        final String baseCookies = Utils.getCookies("https://www.facebook.com");
        final String[] currentFormat = {baseCookies};
        
        editText.setText(baseCookies);

        formatSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String formatted = CookieFormatter.convertToFormat(baseCookies, position, ".facebook.com");
                currentFormat[0] = formatted;
                editText.setText(formatted);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        cookieDialog.setTitle("Cookies");
        cookieDialog.setView(view);
        cookieDialog.setPositiveButton("Copy to Clipboard", (dialog, whichButton) -> {
            Utils.Copy(currentFormat[0], context);
            Utils.Toast(context, "Copied!");
        });
        cookieDialog.show();
    }

    public static void showEditCookiesDialog(final Context context, final WebView webView, final CookieManager cookieManager) {
        MaterialAlertDialogBuilder cookieDialog = new MaterialAlertDialogBuilder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_edittext, null);
        
        final TextInputEditText editText = view.findViewById(R.id.edit_text);
        final TextInputLayout textInputLayout = view.findViewById(R.id.text_input_layout);
        final Spinner formatSpinner = view.findViewById(R.id.format_spinner);
        
        textInputLayout.setHint("Cookies");

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
            context,
            R.array.cookie_formats,
            android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        formatSpinner.setAdapter(adapter);

        final String currentCookies = Utils.getCookies(Constant.FACEBOOK_HOME);
        
        editText.setText(currentCookies);

        formatSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String formatted = CookieFormatter.convertToFormat(currentCookies, position, ".facebook.com");
                editText.setText(formatted);
                
                String[] hints = {"name1=value1; name2=value2", "Netscape format", "JSON Array format", "JSON Dictionary format"};
                if (position < hints.length) {
                    textInputLayout.setHint(hints[position]);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        cookieDialog.setTitle("Edit Cookies");
        cookieDialog.setView(view);
        cookieDialog.setPositiveButton("Save Cookies", (dialog, whichButton) -> {
            if (editText.getText() != null) {
                String inputText = editText.getText().toString().trim();
                
                if (inputText.isEmpty()) {
                    Utils.Toast(context, "Cookies cannot be empty!");
                    return;
                }
                
                try {
                    String standardCookies;
                    int selectedFormat = formatSpinner.getSelectedItemPosition();
                    
                    if (selectedFormat == CookieFormatter.FORMAT_STRING) {
                        standardCookies = CookieFormatter.fromStringFormat(inputText);
                    } else if (selectedFormat == CookieFormatter.FORMAT_NETSCAPE) {
                        standardCookies = CookieFormatter.fromNetscapeFormat(inputText);
                    } else if (selectedFormat == CookieFormatter.FORMAT_JSON_ARRAY) {
                        standardCookies = CookieFormatter.fromJsonArrayFormat(inputText);
                    } else if (selectedFormat == CookieFormatter.FORMAT_JSON_DICT) {
                        standardCookies = CookieFormatter.fromJsonDictFormat(inputText);
                    } else {
                        standardCookies = CookieFormatter.parseToString(inputText);
                    }
                    
                    if (standardCookies.isEmpty()) {
                        Utils.Toast(context, context.getString(R.string.invalid_cookie_format));
                        return;
                    }
                    
                    cookieManager.removeAllCookies(null);
                    String[] cookies = standardCookies.split(";");
                    
                    for (String cookie : cookies) {
                        cookie = cookie.trim();
                        if (!cookie.isEmpty()) {
                            cookieManager.setCookie(Constant.FACEBOOK_HOME, cookie + ";");
                        }
                    }
                    
                    Utils.Toast(context, "Cookies Saved!");
                    webView.loadUrl(Constant.FACEBOOK_HOME);
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    Utils.Toast(context, context.getString(R.string.invalid_cookie_format));
                }
            }
        });
        cookieDialog.setNegativeButton("Cancel", null);
        cookieDialog.show();
    }

    public static void showCurrentUrlDialog(final Context context, final String url) {
        MaterialAlertDialogBuilder urlDialog = new MaterialAlertDialogBuilder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_edittext, null);
        final TextInputEditText editText = view.findViewById(R.id.edit_text);
        final TextInputLayout textInputLayout = view.findViewById(R.id.text_input_layout);
        textInputLayout.setHint("Current URL");

        editText.setText(url);
        editText.setFocusable(false);

        urlDialog.setTitle("Current URL");
        urlDialog.setView(view);
        urlDialog.setPositiveButton("Copy", (dialog, whichButton) -> {
            Utils.Copy(url, context);
            Utils.Toast(context, "Copied!");
        });
        urlDialog.setNegativeButton("Cancel", null);
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
            
            new MaterialAlertDialogBuilder(context)
                .setTitle("Update Available")
                .setMessage(message)
                .setPositiveButton("Download", (dialog, which) -> {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl));
                    context.startActivity(browserIntent);
                })
                .setNegativeButton("Later", null)
                .show();
                
        } catch (Exception e) {
            new MaterialAlertDialogBuilder(context)
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
