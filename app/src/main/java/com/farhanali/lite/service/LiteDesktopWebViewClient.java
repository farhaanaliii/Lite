package com.farhanali.lite.service;

import android.webkit.WebViewClient;
import android.webkit.WebView;
import android.webkit.WebResourceRequest;
import android.graphics.Bitmap;
import android.view.View;
import com.google.android.material.progressindicator.LinearProgressIndicator;

public class LiteDesktopWebViewClient extends WebViewClient {
    private final LinearProgressIndicator progressBar;
    private final WebView webView;
    public LiteDesktopWebViewClient(LinearProgressIndicator PB,WebView WV){
        progressBar = PB;
        webView = WV;
    }
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        return false;
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
        webView.evaluateJavascript("document.querySelector('meta[name=\"viewport\"]').setAttribute('content', 'width=1024px, initial-scale=' + (document.documentElement.clientWidth / 1024));", null);
    }
}
