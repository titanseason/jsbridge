package com.uudove.lib.jsbridge;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.webkit.ClientCertRequest;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class JSBridgeWebView extends WebView {

    private JSBridge jsBridge;
    private DefaultWebViewClient mDefaultWebViewClient;
    private WebViewClient mCustomWebViewClient;
    private OnScrollChangedListener mOnScrollChangedListener;

    private int mScrolledY;
    private int mScrolledX;

    public JSBridgeWebView(Context context) {
        super(context);
        initJSBridge();
    }

    public JSBridgeWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initJSBridge();
    }

    public JSBridgeWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initJSBridge();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public JSBridgeWebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initJSBridge();
    }

    private void initJSBridge() {
        if (jsBridge == null) {
            jsBridge = new JSBridge(this);
        }
        if (mDefaultWebViewClient == null) {
            mDefaultWebViewClient = new DefaultWebViewClient();
            super.setWebViewClient(mDefaultWebViewClient);
        }
    }

    @Override
    public void loadUrl(String url) {
        initJSBridge();
        jsBridge.loadUrl(url);
    }

    @Override
    public void setWebViewClient(WebViewClient client) {
        initJSBridge();
        mCustomWebViewClient = client;
    }

    @Override
    protected void onScrollChanged(int left, int top, int oldLeft, int oldTop) {
        super.onScrollChanged(left, top, oldLeft, oldTop);
        mScrolledX = left;
        mScrolledY = top;
        if (mOnScrollChangedListener != null) {
            mOnScrollChangedListener.onScrollChanged(left, top, oldLeft, oldTop);
        }
    }

    /**
     * Get the current scrolled offset in X-axis
     *
     * @return scrolled offset
     */
    public int getScrollOffsetY() {
        return mScrolledY;
    }

    /**
     * Get the current scrolled offset in Y-axis
     *
     * @return scrolled offset
     */
    public int getScrollOffsetX() {
        return mScrolledX;
    }

    /**
     * Set the scroll callback
     *
     * @param listener callback
     */
    public void setOnScrollChangedListener(OnScrollChangedListener listener) {
        mOnScrollChangedListener = listener;
    }

    /**
     * Set the request listener which called from javascript
     *
     * @param listener request listener
     */
    public void setOnRequestListener(JSBridge.OnRequestListener listener) {
        initJSBridge();
        jsBridge.setOnRequestListener(listener);
    }

    /**
     * Whether this WebView has load another url.
     *
     * @return true - has load another url; false - otherwise.
     */
    public boolean hasLoadAnotherUrl() {
        return mDefaultWebViewClient != null && mDefaultWebViewClient.hasLoadAnotherUrl();
    }

    private class DefaultWebViewClient extends WebViewClient {

        private String mInitUrl; // First URL
        private boolean hasLoadAnotherUrl; // Is load other URLs

        @SuppressWarnings("deprecation")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return interceptUrl(url)
                    || mCustomWebViewClient != null
                    && mCustomWebViewClient.shouldOverrideUrlLoading(view, url);
        }

        @TargetApi(Build.VERSION_CODES.N)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return interceptUrl(request.getUrl().toString())
                    || mCustomWebViewClient != null
                    && mCustomWebViewClient.shouldOverrideUrlLoading(view, request);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (!TextUtils.isEmpty(mInitUrl) && !mInitUrl.equals(url)) {
                hasLoadAnotherUrl = true;
            }
            if (TextUtils.isEmpty(mInitUrl)) {
                mInitUrl = url;
            }
            if (mCustomWebViewClient != null) {
                mCustomWebViewClient.onPageFinished(view, url);
            }
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            if (mCustomWebViewClient != null) {
                mCustomWebViewClient.onPageStarted(view, url, favicon);
            }
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
            if (mCustomWebViewClient != null) {
                mCustomWebViewClient.onLoadResource(view, url);
            }
        }

        @TargetApi(Build.VERSION_CODES.M)
        @Override
        public void onPageCommitVisible(WebView view, String url) {
            super.onPageCommitVisible(view, url);
            if (mCustomWebViewClient != null) {
                mCustomWebViewClient.onPageCommitVisible(view, url);
            }
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            if (mCustomWebViewClient != null) {
                return mCustomWebViewClient.shouldInterceptRequest(view, url);
            }
            return super.shouldInterceptRequest(view, url);
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            if (mCustomWebViewClient != null) {
                return mCustomWebViewClient.shouldInterceptRequest(view, request);
            }
            return super.shouldInterceptRequest(view, request);
        }

        @Override
        public void onTooManyRedirects(WebView view, Message cancelMsg, Message continueMsg) {
            super.onTooManyRedirects(view, cancelMsg, continueMsg);
            if (mCustomWebViewClient != null) {
                mCustomWebViewClient.onTooManyRedirects(view, cancelMsg, continueMsg);
            }
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            if (mCustomWebViewClient != null) {
                mCustomWebViewClient.onReceivedError(view, errorCode, description, failingUrl);
            }
        }

        @TargetApi(Build.VERSION_CODES.M)
        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            if (mCustomWebViewClient != null) {
                mCustomWebViewClient.onReceivedError(view, request, error);
            }
        }

        @TargetApi(Build.VERSION_CODES.M)
        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            super.onReceivedHttpError(view, request, errorResponse);
            if (mCustomWebViewClient != null) {
                mCustomWebViewClient.onReceivedHttpError(view, request, errorResponse);
            }
        }

        @Override
        public void onFormResubmission(WebView view, Message dontResend, Message resend) {
            super.onFormResubmission(view, dontResend, resend);
            if (mCustomWebViewClient != null) {
                mCustomWebViewClient.onFormResubmission(view, dontResend, resend);
            }
        }

        @Override
        public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
            super.doUpdateVisitedHistory(view, url, isReload);
            if (mCustomWebViewClient != null) {
                mCustomWebViewClient.doUpdateVisitedHistory(view, url, isReload);
            }
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            super.onReceivedSslError(view, handler, error);
            if (mCustomWebViewClient != null) {
                mCustomWebViewClient.onReceivedSslError(view, handler, error);
            }
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onReceivedClientCertRequest(WebView view, ClientCertRequest request) {
            super.onReceivedClientCertRequest(view, request);
            if (mCustomWebViewClient != null) {
                mCustomWebViewClient.onReceivedClientCertRequest(view, request);
            }
        }

        @Override
        public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
            super.onReceivedHttpAuthRequest(view, handler, host, realm);
            if (mCustomWebViewClient != null) {
                mCustomWebViewClient.onReceivedHttpAuthRequest(view, handler, host, realm);
            }
        }

        @Override
        public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
            if (mCustomWebViewClient != null) {
                return mCustomWebViewClient.shouldOverrideKeyEvent(view, event);
            }
            return super.shouldOverrideKeyEvent(view, event);
        }

        @Override
        public void onUnhandledKeyEvent(WebView view, KeyEvent event) {
            super.onUnhandledKeyEvent(view, event);
            if (mCustomWebViewClient != null) {
                mCustomWebViewClient.onUnhandledKeyEvent(view, event);
            }
        }

        @Override
        public void onScaleChanged(WebView view, float oldScale, float newScale) {
            super.onScaleChanged(view, oldScale, newScale);
            if (mCustomWebViewClient != null) {
                mCustomWebViewClient.onScaleChanged(view, oldScale, newScale);
            }
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
        @Override
        public void onReceivedLoginRequest(WebView view, String realm, String account, String args) {
            super.onReceivedLoginRequest(view, realm, account, args);
            if (mCustomWebViewClient != null) {
                mCustomWebViewClient.onReceivedLoginRequest(view, realm, account, args);
            }
        }

        private boolean interceptUrl(String url) {
            initJSBridge();
            return jsBridge != null && jsBridge.interceptUrl(url);
        }

        boolean hasLoadAnotherUrl() {
            return hasLoadAnotherUrl;
        }
    }

    /**
     * The WebView scroll position listener
     */
    public interface OnScrollChangedListener {
        /**
         * Callback when WebView's content scrolled
         *
         * @param left    new x position
         * @param top     new y position
         * @param oldLeft old x position
         * @param oldTop  old y position
         */
        void onScrollChanged(int left, int top, int oldLeft, int oldTop);
    }
}
