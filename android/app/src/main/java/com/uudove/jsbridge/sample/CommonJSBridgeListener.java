package com.uudove.jsbridge.sample;

import android.content.Context;

import com.uudove.lib.jsbridge.JSBridge;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CommonJSBridgeListener implements JSBridge.OnRequestListener {

    @Override
    public JSONObject onRequest(Context context, String method, Map<String, String> params) {
        if ("app_info".equals(method)) {
            return getAppInfo(context);
        }

        return null;
    }

    private JSONObject getAppInfo(Context context) {
        Map<String, String> map = new HashMap<>();
        map.put("version_code", String.valueOf(BuildConfig.VERSION_CODE));
        map.put("version_name", BuildConfig.VERSION_NAME);
        map.put("flavor", BuildConfig.FLAVOR);
        return new JSONObject(map);
    }
}
