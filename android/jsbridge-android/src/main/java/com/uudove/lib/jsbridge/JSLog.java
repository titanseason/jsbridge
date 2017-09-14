package com.uudove.lib.jsbridge;

import android.util.Log;

class JSLog {
    private static final String TAG = "JSBridge";

    private static boolean LOG = false;

    static void setLogEnabled(boolean logEnabled) {
        LOG = logEnabled;
    }

    static void d(String text) {
        if (LOG) {
            Log.d(TAG, text);
        }
    }
}
