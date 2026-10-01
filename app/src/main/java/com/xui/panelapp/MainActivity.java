package com.xui.panelapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

public class MainActivity extends AppCompatActivity {
    
    private WebView webView;
    private ProgressBar progressBar;
    private String panelUrl;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        
        loadPanelUrl();
        setupWebView();
    }
    
    private void loadPanelUrl() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String host = prefs.getString("panel_host", "");
        String port = prefs.getString("panel_port", "2053");
        String path = prefs.getString("panel_path", "/");
        
        if (host.isEmpty()) {
            showUrlDialog();
            return;
        }
        
        boolean useHttps = prefs.getBoolean("use_https", true);
        panelUrl = (useHttps ? "https://" : "http://") + host + ":" + port + path;
        
        webView.loadUrl(panelUrl);
    }
    
    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
            }
            
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                // Принимать самоподписанные сертификаты (для локальных панелей)
                handler.proceed();
            }
        });
        
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                if (newProgress < 100) {
                    progressBar.setVisibility(View.VISIBLE);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }
    
    private void showUrlDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Настройка панели x-ui");
        builder.setMessage("Пожалуйста, настройте подключение к панели в настройках");
        builder.setPositiveButton("Настройки", (dialog, which) -> {
            startActivity(new Intent(this, SettingsActivity.class));
            finish();
        });
        builder.setNegativeButton("Позже", (dialog, which) -> {
            Toast.makeText(this, "Загрузка без URL", Toast.LENGTH_SHORT).show();
        });
        builder.setCancelable(false);
        builder.show();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_refresh) {
            webView.reload();
            return true;
        } else if (id == R.id.action_home) {
            if (panelUrl != null) {
                webView.loadUrl(panelUrl);
            }
            return true;
        } else if (id == R.id.action_back) {
            if (webView.canGoBack()) {
                webView.goBack();
            }
            return true;
        } else if (id == R.id.action_forward) {
            if (webView.canGoForward()) {
                webView.goForward();
            }
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
    
    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}
