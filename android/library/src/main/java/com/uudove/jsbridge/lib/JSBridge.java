package com.uudove.jsbridge.lib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;

import org.json.JSONObject;

import java.util.Map;

public class JSBridge {
    private Context context;
    private WebView webView;
    private OnRequestListener mOnRequestListener;

    public JSBridge(@NonNull WebView webView) {
        this.context = webView.getContext();
        this.webView = webView;
        initWebView();
    }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    private void initWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);

        webView.setDownloadListener(new DownloadListener() {

            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype,
                                        long contentLength) {
                try {
                    Uri uri = Uri.parse(url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    context.startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // Remove searchBoxJavaBridge_
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            webView.removeJavascriptInterface("searchBoxJavaBridge_");
        }
        webView.addJavascriptInterface(this, "jsbridge");
    }

    @JavascriptInterface
    public final String request(String url) {
        Log.d("JSBridge", url);
        return request(url, mOnRequestListener);
    }

    @JavascriptInterface
    public final String getValue(String url) {
        Log.d("JSBridge", url);
        return request(url, mOnRequestListener);
    }

    static String request(String url, OnRequestListener listener) {
        UriCompat uri = new UriCompat(url);
        String schema = uri.schema();
        if (!"jsbridge".equals(schema)) {
            return "";
        } else if (listener != null) {
            JSONObject result = listener.onRequest(uri.host(), uri.parameters());
            return result == null ? "" : result.toString();
        }

        return "";

    }

    final void response(String methodId, String result) {
        response(webView, methodId, result);
    }

    static void response(WebView webView, String methodId, String result) {
        String js = "javascript:" + "JSBridge.response('" + methodId + "'," + result + ");";
        loadUrl(webView, js);
    }

    public void loadUrl(String url) {
        loadUrl(webView, url);
    }

    private static void loadUrl(WebView webView, String url) {
        if (webView == null || TextUtils.isEmpty(url)) {
            return;
        }
        if (url.startsWith("javascript:")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                webView.evaluateJavascript(url, null);
            } else {
                webView.loadUrl(url);
            }
        } else {
            webView.loadUrl(url);
        }
    }

    public void setOnRequestListener(OnRequestListener listener) {
        mOnRequestListener = listener;
    }

    public interface OnRequestListener {
        JSONObject onRequest(String method, @NonNull Map<String, String> params);
    }
}
