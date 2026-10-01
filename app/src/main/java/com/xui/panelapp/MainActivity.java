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
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private ProgressBar progressBar;
    private TextView connectionStatus;
    private String panelUrl;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        connectionStatus = findViewById(R.id.connectionStatus);
        
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        setupWebView();
        loadPanelUrl();
    }

    private void loadPanelUrl() {
        String url = buildPanelUrl();
        if (url == null) {
            showUrlDialog();
            return;
        }
        panelUrl = url;
        webView.loadUrl(panelUrl);
    }

    private String buildPanelUrl() {
        String host = prefs.getString("panel_host", "").trim();
        if (host.isEmpty()) {
            return null;
        }
        String port = prefs.getString("panel_port", "2053").trim();
        String path = prefs.getString("panel_path", "/").trim();
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        boolean useHttps = prefs.getBoolean("use_https", true);
        return (useHttps ? "https://" : "http://") + host + ":" + port + path;
    }

    @Override
    protected void onResume() {
        super.onResume();
        String url = buildPanelUrl();
        if (url != null && !url.equals(panelUrl)) {
            panelUrl = url;
            webView.loadUrl(panelUrl);
        }
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
                connectionStatus.setVisibility(View.GONE);
                updateConnectionStatus(true, null);
            }

            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                updateConnectionStatus(false, "Загрузка...");
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                boolean acceptSelfSigned = prefs.getBoolean("accept_self_signed", true);

                if (acceptSelfSigned) {
                    new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Предупреждение безопасности")
                        .setMessage("Сертификат не доверен:\n\n" +
                                   error.getCertificate().toString() + "\n\n" +
                                   "Продолжить подключение?")
                        .setPositiveButton("Продолжить", (dialog, which) -> handler.proceed())
                        .setNegativeButton("Отмена", (dialog, which) -> handler.cancel())
                        .setOnCancelListener(dialog -> handler.cancel())
                        .show();
                } else {
                    handler.cancel();
                    Toast.makeText(MainActivity.this,
                        "SSL-ошибка: " + error.toString(), Toast.LENGTH_LONG).show();
                    updateConnectionStatus(false, "SSL ошибка");
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) {
                    String errorMessage = error.getDescription().toString();
                    Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                    updateConnectionStatus(false, "Ошибка подключения");
                }
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, android.webkit.WebResourceResponse errorResponse) {
                int statusCode = errorResponse.getStatusCode();
                if (statusCode >= 400) {
                    Toast.makeText(MainActivity.this,
                        "HTTP ошибка: " + statusCode, Toast.LENGTH_LONG).show();
                    updateConnectionStatus(false, "HTTP " + statusCode);
                }
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

    private void updateConnectionStatus(boolean connected, String status) {
        if (connected) {
            connectionStatus.setVisibility(View.GONE);
        } else {
            connectionStatus.setText(status != null ? status : "Нет подключения");
            connectionStatus.setTextColor(getColor(android.R.color.darker_gray));
            connectionStatus.setVisibility(View.VISIBLE);
        }
    }
}
