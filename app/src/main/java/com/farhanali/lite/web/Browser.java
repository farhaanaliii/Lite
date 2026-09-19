package com.farhanali.lite.web;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.farhanali.lite.Constant;
import com.farhanali.lite.settings.Settings;
import com.google.android.material.progressindicator.LinearProgressIndicator;

public class Browser {
    public interface FileChooserCallback {
        boolean onShowFileChooser(ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams);
    }

    private final WebView webView;
    private final LinearProgressIndicator progressBar;
    private final SwipeRefreshLayout swipeRefreshLayout;
    private final CookieManager cookieManager = CookieManager.getInstance();
    private final Settings appSettings;
    private final WebSettings settings;

    private LiteWebViewClient webViewClient;
    private boolean isDesktopMode;
    private String defaultUserAgent;

    public Browser(Context context, WebView webView, LinearProgressIndicator progressBar, SwipeRefreshLayout swipeRefreshLayout) {
        this.webView = webView;
        this.progressBar = progressBar;
        this.swipeRefreshLayout = swipeRefreshLayout;
        this.appSettings = new Settings(context);
        this.settings = webView.getSettings();
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void init(FileChooserCallback fileChooserCallback) {
        cookieManager.setAcceptCookie(true);

        defaultUserAgent = appSettings.getUserAgent();
        if (defaultUserAgent.isEmpty()) {
            defaultUserAgent = settings.getUserAgentString();
            appSettings.saveUserAgent(defaultUserAgent);
        }

        isDesktopMode = appSettings.isDesktopModeEnabled();

        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        webView.setBackgroundColor(Color.TRANSPARENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            settings.setAlgorithmicDarkeningAllowed(true);
        }

        swipeRefreshLayout.setOnRefreshListener(this::reload);

        webViewClient = new LiteWebViewClient(progressBar, isDesktopMode);
        webView.setWebViewClient(webViewClient);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                progressBar.setProgress(newProgress, true);
                if (newProgress == 100) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                return fileChooserCallback != null && fileChooserCallback.onShowFileChooser(filePathCallback, fileChooserParams);
            }
        });

        applyDesktopMode(isDesktopMode);
        syncSettings();
        loadHome();
    }

    public void syncSettings() {
        settings.setJavaScriptEnabled(appSettings.isJavaScriptEnabled());
        if (!isDesktopMode) {
            settings.setUserAgentString(getActiveUserAgent());
        }
    }

    public void setDesktopMode(boolean enabled) {
        isDesktopMode = enabled;
        appSettings.setDesktopModeEnabled(enabled);
        applyDesktopMode(enabled);
        loadHome();
    }

    public void desktopMode(MenuItem item) {
        boolean newState = !item.isChecked();
        setDesktopMode(newState);
        item.setChecked(newState);
    }

    private void applyDesktopMode(boolean enabled) {
        settings.setUseWideViewPort(enabled);
        settings.setSupportZoom(enabled);
        settings.setLoadWithOverviewMode(enabled);
        webView.setScrollBarStyle(enabled ? WebView.SCROLLBARS_OUTSIDE_OVERLAY : WebView.SCROLLBARS_INSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(!enabled);
        settings.setUserAgentString(enabled ? Constant.DESKTOP_USERAGENT : getActiveUserAgent());
        webViewClient.setDesktopMode(enabled);
    }

    private String getActiveUserAgent() {
        String customUa = appSettings.getCustomUserAgent();
        return !customUa.isEmpty() ? customUa.trim() : defaultUserAgent;
    }

    public boolean isDesktopMode() {
        return isDesktopMode;
    }

    public void loadHome() {
        webView.loadUrl(Constant.FACEBOOK_HOME);
    }

    public void reload() {
        webView.reload();
    }

    public boolean canGoBack() {
        return webView.canGoBack();
    }

    public void goBack() {
        webView.goBack();
    }

    public String getUrl() {
        return webView.getUrl();
    }

    public WebView getWebView() {
        return webView;
    }

    public CookieManager getCookieManager() {
        return cookieManager;
    }

    public void onPause() {
        webView.onPause();
    }

    public void onResume() {
        webView.onResume();
    }
}
