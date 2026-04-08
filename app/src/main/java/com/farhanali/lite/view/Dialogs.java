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
        
        textInputLayout.setHint(context.getString(R.string.cookies));
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

        cookieDialog.setTitle(context.getString(R.string.cookies));
        cookieDialog.setView(view);
        cookieDialog.setPositiveButton(context.getString(R.string.copy_to_clipboard), (dialog, whichButton) -> {
            Utils.Copy(currentFormat[0], context);
            Utils.Toast(context, context.getString(R.string.copied));
        });
        cookieDialog.show();
    }

    public static void showEditCookiesDialog(final Context context, final WebView webView, final CookieManager cookieManager) {
        MaterialAlertDialogBuilder cookieDialog = new MaterialAlertDialogBuilder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_edittext, null);
        
        final TextInputEditText editText = view.findViewById(R.id.edit_text);
        final TextInputLayout textInputLayout = view.findViewById(R.id.text_input_layout);
        final Spinner formatSpinner = view.findViewById(R.id.format_spinner);
        
        textInputLayout.setHint(context.getString(R.string.cookies));

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
                
                String[] hints = {context.getString(R.string.hint_string_format), context.getString(R.string.hint_netscape_format), context.getString(R.string.hint_json_array_format), context.getString(R.string.hint_json_dict_format)};
                if (position < hints.length) {
                    textInputLayout.setHint(hints[position]);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        cookieDialog.setTitle(context.getString(R.string.edit_cookies));
        cookieDialog.setView(view);
        cookieDialog.setPositiveButton(context.getString(R.string.save_cookies), (dialog, whichButton) -> {
            if (editText.getText() != null) {
                String inputText = editText.getText().toString().trim();
                
                if (inputText.isEmpty()) {
                    Utils.Toast(context, context.getString(R.string.cookies_empty));
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
                    
                    Utils.Toast(context, context.getString(R.string.cookies_saved));
                    webView.loadUrl(Constant.FACEBOOK_HOME);
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    Utils.Toast(context, context.getString(R.string.invalid_cookie_format));
                }
            }
        });
        cookieDialog.setNegativeButton(android.R.string.cancel, null);
        cookieDialog.show();
    }

    public static void showCurrentUrlDialog(final Context context, final String url) {
        MaterialAlertDialogBuilder urlDialog = new MaterialAlertDialogBuilder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_edittext, null);
        final TextInputEditText editText = view.findViewById(R.id.edit_text);
        final TextInputLayout textInputLayout = view.findViewById(R.id.text_input_layout);
        textInputLayout.setHint(context.getString(R.string.current_url));

        editText.setText(url);
        editText.setFocusable(false);

        urlDialog.setTitle(context.getString(R.string.current_url));
        urlDialog.setView(view);
        urlDialog.setPositiveButton(android.R.string.copy, (dialog, whichButton) -> {
            Utils.Copy(url, context);
            Utils.Toast(context, context.getString(R.string.copied));
        });
        urlDialog.setNegativeButton(android.R.string.cancel, null);
        urlDialog.show();
    }

    public static void showUpdateDialog(Context context, String jsonResponse) {
        try {
            JSONObject json = new JSONObject(jsonResponse);
            String latestVersion = json.getString("latest_version");
            String downloadUrl = json.getString("download_url");
            JSONArray changelog = json.getJSONArray("changelog");
            
            SpannableStringBuilder message = new SpannableStringBuilder();
            message.append(context.getString(R.string.version_available, latestVersion)).append("\n\n");
            
            for (int i = 0; i < changelog.length(); i++) {
                JSONObject release = changelog.getJSONObject(i);
                if (release.getString("version").equals(latestVersion)) {
                    JSONArray changes = release.getJSONArray("changes");
                    message.append(context.getString(R.string.whats_new)).append("\n");
                    
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
            
            message.append("\n").append(context.getString(R.string.download_update_prompt));
            
            new MaterialAlertDialogBuilder(context)
                .setTitle(context.getString(R.string.update_available))
                .setMessage(message)
                .setPositiveButton(context.getString(R.string.download), (dialog, which) -> {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl));
                    context.startActivity(browserIntent);
                })
                .setNegativeButton(context.getString(R.string.later), null)
                .show();
                
        } catch (Exception e) {
            new MaterialAlertDialogBuilder(context)
                .setTitle(context.getString(R.string.update_available))
                .setMessage(context.getString(R.string.new_version_available))
                .setPositiveButton(android.R.string.ok, null)
                .show();
        }
    }

    private static String getChangeIcon(String type) {
        return switch (type) {
            case "feature" -> "✨"; // New feature
            case "fix" -> "🐛";     // Bug fix
            case "improvement" -> "⚡"; // Improvement
            case "security" -> "🔒"; // Security fix
            case "performance" -> "🚀"; // Performance
            case "ui" -> "🎨";      // UI/UX change
            default -> "•";
        };
    }
}
