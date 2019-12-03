package com.hzjy.download.util;

import android.util.Log;

/**
 * pj567
 * 2019/11/25
 */

public class Logger {
    private String TAG = "pj567";
    private boolean debug = true;

    public Logger(Object object) {
        TAG = object.getClass().getSimpleName();
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void v(String msg) {
        if (debug) {
            Log.v(TAG, msg);
        }
    }

    public void d(String msg) {
        if (debug) {
            Log.d(TAG, msg);
        }
    }

    public void e(String msg) {
        if (debug) {
            Log.e(TAG, msg);
        }
    }

    public void i(String msg) {
        if (debug) {
            Log.i(TAG, msg);
        }
    }
}
