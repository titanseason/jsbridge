package com.uudove.jsbridge;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.webkit.WebView;

import com.uudove.jsbridge.lib.JSBridge;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class MainActivity extends Activity {

    private JSBridge jsBridge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initWebView();
    }

    private void initWebView() {
        WebView webView = (WebView) findViewById(R.id.web_view);
        webView.clearCache(true);
        jsBridge = new JSBridge(webView);
        jsBridge.loadUrl("file:///android_asset/demo.html");
        jsBridge.setOnRequestListener(new JSBridge.OnRequestListener() {
            @Override
            public JSONObject onRequest(String method, @NonNull Map<String, String> params) {
                Log.d("MainActivity", "method: " + method);
                for (String key : params.keySet()) {
                    Log.d("MainActivity", key + "=" + params.get(key));
                }
                if ("add".equals(method)) {
                    String a = params.get("a");
                    String b = params.get("b");
                    String result = a == null ? b : a + b;
                    JSONObject json = new JSONObject();
                    try {
                        json.put("result", result);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    return json;
                }
                return null;
            }
        });
    }
}
