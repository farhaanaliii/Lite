package com.farhanali.lite.activity;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.webkit.WebView;
import android.webkit.CookieManager;
import android.os.Bundle;
import com.farhanali.lite.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.farhanali.lite.utils.Utils;
import com.farhanali.lite.constant.Constant;
import android.webkit.WebSettings;
import com.farhanali.lite.service.LiteWebViewClient;
import android.webkit.WebChromeClient;
import com.farhanali.lite.service.LiteDesktopWebViewClient;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import com.farhanali.lite.service.UpdateChecker;
import android.content.Context;
import com.farhanali.lite.utils.Settings;
import com.farhanali.lite.view.Dialogs;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.google.android.material.progressindicator.LinearProgressIndicator;

public class MainActivity extends AppCompatActivity{
    WebView webView;
	WebSettings webSettings;
    LinearProgressIndicator progressBar;
    boolean isDesktopMode;
    String userAgent;
	CookieManager cookieManager = CookieManager.getInstance();
    Context context;
    Settings settings;
    private static boolean hasCheckedUpdate = false;
    ActivityResultLauncher<Intent> settingsLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            recreate();
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

        settings = new Settings(context);

        progressBar = findViewById(R.id.progressBar);
        webView = findViewById(R.id.webView);
        webSettings = webView.getSettings();

        isDesktopMode = settings.isDesktopModeEnabled();
        
        cookieManager.setAcceptCookie(true);
        
        userAgent = settings.getUserAgent();
        if(userAgent.isEmpty()){
            userAgent = webSettings.getUserAgentString();
            settings.saveUserAgent(userAgent);
        }

        if(!settings.getCustomUserAgent().isEmpty()){
            userAgent = settings.getCustomUserAgent();
        }

        webSettings.setUserAgentString(userAgent.trim());
        webSettings.setJavaScriptEnabled(settings.isJavaScriptEnabled());
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webView.setBackgroundColor(android.graphics.Color.TRANSPARENT);

        if (androidx.webkit.WebViewFeature.isFeatureSupported(androidx.webkit.WebViewFeature.ALGORITHMIC_DARKENING)) {
            androidx.webkit.WebSettingsCompat.setAlgorithmicDarkeningAllowed(webSettings, true);
        }

        androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefresh = findViewById(R.id.swipeRefresh);
        swipeRefresh.setOnRefreshListener(() -> webView.reload());

        webView.setWebViewClient(new LiteWebViewClient(progressBar));
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            progressBar.setProgress(newProgress, true);
            if (newProgress == 100) {
                swipeRefresh.setRefreshing(false);
            }
            }

            @Override
            public boolean onShowFileChooser(WebView webView, android.webkit.ValueCallback<android.net.Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                if (mFilePathCallback != null) {
                    mFilePathCallback.onReceiveValue(null);
                }
                mFilePathCallback = filePathCallback;

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                if (fileChooserParams.getMode() == FileChooserParams.MODE_OPEN_MULTIPLE) {
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                }
                fileChooserLauncher.launch(Intent.createChooser(intent, "Select File"));
                return true;
            }
        });

        if(settings.isDesktopModeEnabled()){
            webSettings.setUseWideViewPort(true);
            webSettings.setSupportZoom(true);
            webSettings.setLoadWithOverviewMode(true);
            webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
            webView.setScrollbarFadingEnabled(false);
            webSettings.setUserAgentString(Constant.DESKTOP_USERAGENT);
            webView.setWebViewClient(new LiteDesktopWebViewClient(progressBar, webView));
        }

        webView.loadUrl(Constant.FACEBOOK_HOME);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack();
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
		desk.setChecked(isDesktopMode);
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
            Dialogs.showCurrentUrlDialog(context, webView.getUrl());
        }
        else if(id == R.id.home) {
            webView.loadUrl(Constant.FACEBOOK_HOME);
        }
        else if(id == R.id.desktop_mode){
            desktopMode(item);
        }
        else if(id == R.id.about) {
            startActivity(new Intent(context, AboutActivity.class));
        }
        else if(id == R.id.editCookies){
            Dialogs.showEditCookiesDialog(context, webView, cookieManager);
        } else if (id == R.id.checkupdates) {
            if(Utils.isInternetOn(context)){
                new UpdateChecker(context).execute();
            }else{
                Utils.Toast(context, getString(R.string.no_internet));
            }
        } else if(id == R.id.settings){
            settingsLauncher.launch(new Intent(context, SettingsActivity.class));
        }

        return true;
    }
    private void desktopMode(MenuItem item) {
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);

        boolean isDesktopEnabled = !item.isChecked();
        webSettings.setUserAgentString(isDesktopEnabled ? Constant.DESKTOP_USERAGENT : userAgent);
        webView.setWebViewClient(isDesktopEnabled ? new LiteDesktopWebViewClient(progressBar, webView) : new LiteWebViewClient(progressBar));
        webView.loadUrl(Constant.FACEBOOK_HOME);

        isDesktopMode = isDesktopEnabled;
        item.setChecked(isDesktopEnabled);
        settings.setDesktopModeEnabled(isDesktopEnabled);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!hasCheckedUpdate){
            hasCheckedUpdate = true;
            if(Utils.isInternetOn(context)){
                new UpdateChecker(context).execute();
            }else{
                Utils.Toast(context, getString(R.string.no_internet));
            }
        }
    }

    public WebView getWebView(){
        return webView;
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
