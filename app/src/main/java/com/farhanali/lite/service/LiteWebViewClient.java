package com.farhanali.lite.service;

import android.webkit.WebViewClient;
import android.webkit.WebView;
import android.graphics.Bitmap;
import android.webkit.WebResourceRequest;
import android.view.View;
import com.google.android.material.progressindicator.LinearProgressIndicator;

public class LiteWebViewClient extends WebViewClient{
    private final LinearProgressIndicator progressBar;
    public LiteWebViewClient(LinearProgressIndicator mprogressBar){
        progressBar = mprogressBar;
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
        }

    }
