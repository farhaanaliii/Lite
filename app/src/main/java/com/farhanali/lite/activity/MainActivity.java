package com.farhanali.lite.activity;

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

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        init();

    }
    @SuppressLint("SetJavaScriptEnabled")
    private void init(){
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
        if(userAgent.equals("")){
            userAgent = webSettings.getUserAgentString();
            settings.saveUserAgent(userAgent);
        }

        if(!settings.getCustomUserAgent().equals("")){
            userAgent = settings.getCustomUserAgent();
        }

        webSettings.setUserAgentString(userAgent.trim());
        webSettings.setJavaScriptEnabled(settings.isJavaScriptEnabled());
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webView.setWebViewClient(new LiteWebViewClient(progressBar));
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            progressBar.setProgress(newProgress, true);
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
