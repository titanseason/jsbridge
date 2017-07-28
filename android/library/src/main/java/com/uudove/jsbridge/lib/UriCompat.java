package com.uudove.jsbridge.lib;

import android.annotation.TargetApi;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class UriCompat {
    private Uri uri;

    UriCompat(String uriString) {
        try {
            this.uri = Uri.parse(uriString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    String schema() {
        return uri == null ? null : uri.getScheme();
    }

    String host() {
        return uri == null ? null : uri.getHost();
    }

    String parameter(String key) {
        return uri == null ? null : uri.getQueryParameter(key);
    }

    @NonNull
    Map<String, String> parameters() {
        if (uri == null) {
            return new HashMap<>();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return getParamsHoneycomb();
        } else {
            return getParamsBase();
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private Map<String, String> getParamsHoneycomb() {
        Map<String, String> map = new HashMap<>();

        Set<String> keys = uri.getQueryParameterNames();
        if (keys == null) {
            return map;
        }
        for (String key : keys) {
            map.put(key, uri.getQueryParameter(key));
        }
        return map;
    }

    private Map<String, String> getParamsBase() {
        Map<String, String> map = new HashMap<>();

        String query = uri.getEncodedQuery();
        if (TextUtils.isEmpty(query)) {
            return map;
        }

        String[] ands = query.split("&");
        for (String and : ands) {
            String[] eqs = and.split("=");
            String key = eqs[0];
            String value = eqs.length < 2 ? "" : eqs[1];
            map.put(key, decode(value));
        }
        return map;
    }

    private String decode(String text) {
        if (text == null) {
            return "";
        }

        try {
            return URLDecoder.decode(text, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return text;
    }
}
