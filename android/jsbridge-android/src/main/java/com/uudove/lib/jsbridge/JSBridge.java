package com.uudove.lib.jsbridge;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;

import org.json.JSONObject;

import java.util.Map;

public class JSBridge {

    private static final String UNSUPPORTED = "unsupported";

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
        JSLog.d("JSBridge request: " + url);
        String result = request(context, url, mOnRequestListener);
        JSLog.d("JSBridge request result: " + result);
        return result;
    }

    @JavascriptInterface
    public final String getValue(String url) {
        JSLog.d("JSBridge getValue: " + url);
        String result = request(context, url, mOnRequestListener);
        JSLog.d("JSBridge getValue result: " + result);
        return result;
    }

    boolean interceptUrl(String url) {
        UriCompat uri = new UriCompat(url);
        String schema = uri.schema();
        if ("jsbridge".equals(schema)) {
            String methodId = uri.parameter("jsbridgeMethodId");
            String result = request(url);
            response(methodId, result);
            return true;
        }

        return false;
    }

    private static String request(Context context, String url, OnRequestListener listener) {
        UriCompat uri = new UriCompat(url);
        String schema = uri.schema();
        if (!"jsbridge".equals(schema)) {
            return UNSUPPORTED;
        } else {
            JSONObject result = JSBridgeListenerPool.onRequest(context, uri.host(), uri.parameters());
            if (result == null && listener != null) {
                result = listener.onRequest(context, uri.host(), uri.parameters());
            }
            if (result != null) {
                return result.toString();
            }
        }
        return UNSUPPORTED;
    }

    private void response(String methodId, String result) {
        response(webView, methodId, result);
    }

    private static void response(WebView webView, String methodId, String result) {
        String js = "javascript:" + "JSBridge.response('" + methodId + "'," + result + ");";
        loadUrl(webView, js);
    }

    /**
     * Same as {@link WebView#loadUrl(String)}, but if the url starts with <i>javascript:</i> , this method will call
     * {@link WebView#evaluateJavascript(String, ValueCallback)} method after android 4.4
     *
     * @param url the url to be loaded
     */
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

    /**
     * Set whether to show log message on console.
     *
     * @param logEnabled true - enable log; false - otherwise
     */
    public static void setLogEnabled(boolean logEnabled) {
        JSLog.setLogEnabled(logEnabled);
    }

    /**
     * In most cases, we need to get the same information from native app in different WebViews.
     * Thus, you can register a {@linkplain OnRequestListener} to the pool, and just need to write the logic once. <br/>
     * If native app got a request form javascript, the <i>OnRequestListener Pool</i> will handle the request
     * first, if any <i>OnRequestListener</i> returns none null value, we will response to javascript immediately.
     *
     * @param listener OnRequestListener
     */
    public static void registerCommonRequestListener(OnRequestListener listener) {
        JSBridgeListenerPool.registerCommonRequestListener(listener);
    }

    /**
     * Remove a <i>OnRequestListener</i> from the <i>OnRequestListener Pool</i>
     *
     * @param listener OnRequestListener
     */
    public static void unregisterCommonRequestListener(OnRequestListener listener) {
        JSBridgeListenerPool.unregisterRequestListener(listener);
    }

    /**
     * Set the special <i>OnRequestListener</i> for the current WebView. This <i>OnRequestListener</i> will not be put
     * in to the <i>OnRequestListener Pool</i>.
     *
     * @param listener OnRequestListener
     */
    public void setOnRequestListener(OnRequestListener listener) {
        mOnRequestListener = listener;
    }

    /**
     * Callback if there's a request from javascript.
     */
    public interface OnRequestListener {

        /**
         * RequestListener for requests from javascript
         *
         * @param context Context
         * @param method  the method name, not null
         * @param params  the method params, in the form of <i>Map&lt;String, String&gt;</i>
         * @return the result in the form of JSONObject, if don't support this given method name, just return null.
         */
        JSONObject onRequest(Context context, String method, Map<String, String> params);
    }
}
