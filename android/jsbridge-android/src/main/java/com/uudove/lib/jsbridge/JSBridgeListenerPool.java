package com.uudove.lib.jsbridge;

import android.content.Context;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class JSBridgeListenerPool {

    private static List<JSBridge.OnRequestListener> listeners;

    static void registerCommonRequestListener(JSBridge.OnRequestListener listener) {
        if (listener == null) {
            return;
        }
        if (listeners == null) {
            listeners = new ArrayList<>();
        }
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    static void unregisterRequestListener(JSBridge.OnRequestListener listener) {
        if (listener != null && listeners != null) {
            listeners.remove(listener);
        }
    }

    static JSONObject onRequest(Context context, String method, Map<String, String> params) {
        if (listeners == null) {
            return null;
        }
        for (JSBridge.OnRequestListener listener : listeners) {
            JSONObject result = listener.onRequest(context, method, params);
            if (result != null) {
                return result;
            }
        }
        return null;
    }
}
