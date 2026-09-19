package com.farhanali.lite;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebChromeClient;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.farhanali.lite.settings.SettingsActivity;
import com.farhanali.lite.ui.Dialogs;
import com.farhanali.lite.ui.Utils;
import com.farhanali.lite.web.Browser;
import com.google.android.material.appbar.MaterialToolbar;

public class MainActivity extends AppCompatActivity{
    Browser browser;
    Context context;
    private static boolean hasCheckedUpdate = false;

    private final ActivityResultLauncher<String> notif_permission_launcher = registerForActivityResult(
        new ActivityResultContracts.RequestPermission(),
        is_granted -> {
        });

    private android.webkit.ValueCallback<android.net.Uri[]> mFilePathCallback;
    private final ActivityResultLauncher<Intent> fileChooserLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (mFilePathCallback != null) {
                android.net.Uri[] results = null;
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    if (result.getData().getClipData() != null) {
                        int count = result.getData().getClipData().getItemCount();
                        results = new android.net.Uri[count];
                        for (int i = 0; i < count; i++) {
                            results[i] = result.getData().getClipData().getItemAt(i).getUri();
                        }
                    } else if (result.getData().getData() != null) {
                        results = new android.net.Uri[]{result.getData().getData()};
                    }
                }
                mFilePathCallback.onReceiveValue(results);
                mFilePathCallback = null;
            }
        });

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                notif_permission_launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        context = this;
        init();

    }
    @SuppressLint("SetJavaScriptEnabled")
    private void init(){
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.toolLayout), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.swipeRefresh), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

        // Tint overflow icon white for visibility on blue toolbar
        fixOverflowIconColor(toolbar);

        browser = new Browser(
            context,
            findViewById(R.id.webView),
            findViewById(R.id.progressBar),
            findViewById(R.id.swipeRefresh)
        );

        browser.init((filePathCallback, fileChooserParams) -> {
            if (mFilePathCallback != null) {
                mFilePathCallback.onReceiveValue(null);
            }
            mFilePathCallback = filePathCallback;

            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            if (fileChooserParams.getMode() == WebChromeClient.FileChooserParams.MODE_OPEN_MULTIPLE) {
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            }
            fileChooserLauncher.launch(Intent.createChooser(intent, getString(R.string.select_file)));
            return true;
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (browser.canGoBack()) {
                    browser.goBack();
                } else {
                    finish();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.web_menu, menu);
        MenuItem desk = menu.findItem(R.id.desktop_mode);
		desk.setChecked(browser.isDesktopMode());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        int id = item.getItemId();

        if(id == R.id.getCookies){
            Dialogs.showCookieDialog(context);
        }
        else if (id == R.id.current_url){
            Dialogs.showCurrentUrlDialog(context, browser.getUrl());
        }
        else if(id == R.id.home) {
            browser.loadHome();
        }
        else if(id == R.id.refresh) {
            browser.reload();
        }
        else if(id == R.id.desktop_mode){
            browser.desktopMode(item);
        }
        else if(id == R.id.about) {
            Dialogs.showAboutDialog(context);
        }
        else if(id == R.id.editCookies){
            Dialogs.showEditCookiesDialog(context, browser.getWebView(), browser.getCookieManager());
        } else if (id == R.id.checkupdates) {
            if(Utils.isInternetOn(context)){
                Updater.check(context);
            }else{
                Utils.toast(context, R.string.no_internet);
            }
        } else if(id == R.id.settings){
            startActivity(new Intent(context, SettingsActivity.class));
        }

        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!hasCheckedUpdate) {
            hasCheckedUpdate = true;
            if (Utils.isInternetOn(context)) {
                Updater.check(context);
            } else {
                Utils.toast(context, R.string.no_internet);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        browser.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        browser.onResume();
        browser.syncSettings();
    }

    private void fixOverflowIconColor(MaterialToolbar toolbar) {
        try {
            android.graphics.drawable.Drawable overflowIcon = toolbar.getOverflowIcon();
            if (overflowIcon != null) {
                overflowIcon.setTint(getResources().getColor(R.color.md_theme_light_onPrimary, getTheme()));
                toolbar.setOverflowIcon(overflowIcon);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
