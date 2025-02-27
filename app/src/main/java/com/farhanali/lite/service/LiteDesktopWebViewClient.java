package com.farhanali.lite.service;

import android.webkit.WebViewClient;
import android.webkit.WebView;
import android.webkit.WebResourceRequest;
import android.graphics.Bitmap;
import android.widget.ProgressBar;
import android.view.View;

public class LiteDesktopWebViewClient extends WebViewClient {
    private final ProgressBar progressBar;
    private final WebView webView;
    public LiteDesktopWebViewClient(ProgressBar PB,WebView WV){
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
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        progressBar.setVisibility(View.INVISIBLE);
        webView.evaluateJavascript("document.querySelector('meta[name=\"viewport\"]').setAttribute('content', 'width=1024px, initial-scale=' + (document.documentElement.clientWidth / 1024));", null);
    }
}
