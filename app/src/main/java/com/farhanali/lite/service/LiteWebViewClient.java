package com.farhanali.lite.service;

import android.webkit.WebViewClient;
import android.webkit.WebView;
import android.graphics.Bitmap;
import android.view.View;
import com.google.android.material.progressindicator.LinearProgressIndicator;

public class LiteWebViewClient extends WebViewClient{
    private final LinearProgressIndicator progressBar;
    private boolean isDesktopMode;

    public LiteWebViewClient(LinearProgressIndicator progressBar, boolean isDesktopMode) {
        this.progressBar = progressBar;
        this.isDesktopMode = isDesktopMode;
    }

    public void setDesktopMode(boolean isDesktopMode) {
        this.isDesktopMode = isDesktopMode;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        if (progressBar != null) {
            progressBar.setVisibility(View.INVISIBLE);
        }
        if (isDesktopMode) {
            view.evaluateJavascript(
                "document.querySelector('meta[name=\"viewport\"]').setAttribute('content', 'width=1024px, initial-scale=' + (document.documentElement.clientWidth / 1024));",
                null
            );
        }
    }
}
