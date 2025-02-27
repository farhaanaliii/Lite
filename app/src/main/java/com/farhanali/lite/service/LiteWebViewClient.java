package com.farhanali.lite.service;

import android.webkit.WebViewClient;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.graphics.Bitmap;
import android.webkit.WebResourceRequest;
import android.util.Log;
import android.view.View;

public class LiteWebViewClient extends WebViewClient{
    private final ProgressBar progressBar;
    public LiteWebViewClient(ProgressBar mprogressBar){
        progressBar = mprogressBar;
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
        }

    }
