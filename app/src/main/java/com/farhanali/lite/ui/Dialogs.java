package com.farhanali.lite.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.farhanali.lite.BuildConfig;
import com.farhanali.lite.Constant;
import com.farhanali.lite.R;
import com.farhanali.lite.web.CookieFormatter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONObject;

public class Dialogs {

    public static void showCookieDialog(final Context context) {
        MaterialAlertDialogBuilder cookieDialog = new MaterialAlertDialogBuilder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_cookies, null);
        
        final TextView codeText = view.findViewById(R.id.code_text);
        final Spinner formatSpinner = view.findViewById(R.id.format_spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
            context,
            R.array.cookie_formats,
            android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        formatSpinner.setAdapter(adapter);

        final String baseCookies = Utils.getCookies(Constant.FACEBOOK_HOME);
        final String[] currentFormat = {baseCookies};
        
        codeText.setText(baseCookies);

        formatSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String formatted = CookieFormatter.convertToFormat(baseCookies, position, ".facebook.com");
                currentFormat[0] = formatted;
                codeText.setText(formatted);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        String userId = CookieFormatter.getCookie(baseCookies, "c_user");
        if (userId != null) {
            view.findViewById(R.id.account_card).setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.account_user_id)).setText("ID: " + userId);
            view.findViewById(R.id.account_copy_btn).setOnClickListener(v -> {
                Utils.copy(Constant.FACEBOOK_HOME + "/" + userId, context);
                Utils.toast(context, R.string.copied);
            });
        }

        cookieDialog.setTitle(context.getString(R.string.cookies));
        cookieDialog.setView(view);
        cookieDialog.setPositiveButton(context.getString(R.string.copy_to_clipboard), (dialog, which) -> {
            Utils.copy(currentFormat[0], context);
            Utils.toast(context, R.string.copied);
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
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
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
        cookieDialog.setPositiveButton(context.getString(R.string.save_cookies), (dialog, which) -> {
            if (editText.getText() != null) {
                String inputText = editText.getText().toString().trim();
                
                if (inputText.isEmpty()) {
                    Utils.toast(context, R.string.cookies_empty);
                    return;
                }
                
                try {
                    String standardCookies = CookieFormatter.parse(inputText, formatSpinner.getSelectedItemPosition());
                    
                    if (standardCookies.isEmpty()) {
                        Utils.toast(context, R.string.invalid_cookie_format);
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
                    cookieManager.flush();
                    
                    Utils.toast(context, R.string.cookies_saved);
                    webView.loadUrl(Constant.FACEBOOK_HOME);
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    Utils.toast(context, R.string.invalid_cookie_format);
                }
            }
        });
        cookieDialog.setNegativeButton(android.R.string.cancel, null);
        cookieDialog.show();
    }

    public static void showCurrentUrlDialog(final Context context, final String url) {
        MaterialAlertDialogBuilder urlDialog = new MaterialAlertDialogBuilder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_edittext, null);
        view.findViewById(R.id.spinner_container).setVisibility(View.GONE);
        final TextInputEditText editText = view.findViewById(R.id.edit_text);
        editText.setMinHeight(0);
        editText.setMinimumHeight(0);
        final TextInputLayout textInputLayout = view.findViewById(R.id.text_input_layout);
        textInputLayout.setHint(context.getString(R.string.current_url));

        editText.setText(url);
        editText.setFocusable(false);

        urlDialog.setTitle(context.getString(R.string.current_url));
        urlDialog.setView(view);
        urlDialog.setPositiveButton(android.R.string.copy, (dialog, which) -> {
            Utils.copy(url, context);
            Utils.toast(context, R.string.copied);
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

    public static void showAboutDialog(Context context) {
        String title = context.getString(R.string.app_name) + " v" + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")";

        String message = context.getString(R.string.about_description) + "\n\n"
                + context.getString(R.string.about_developer, "Farhan Ali") + "\n"
                + context.getString(R.string.about_license, "MIT");

        new MaterialAlertDialogBuilder(context)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.about_github, (dialog, which) -> {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constant.GITHUB_REPO)));
                })
                .setNeutralButton(R.string.about_license_btn, (dialog, which) -> {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constant.GITHUB_REPO + "/blob/main/LICENSE")));
                })
                .setNegativeButton(R.string.close, null)
                .show();
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
