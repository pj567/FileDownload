package com.hzjy.download.util;

import android.util.Log;

/**
 * pj567
 * 2019/9/10
 */

public class L {
    private static final String TAG = "pj567";

    public static void e(String msg) {
        Log.e(TAG, msg);
    }

    public static void v(String msg) {
        Log.v(TAG, msg);
    }

    public static void i(String msg) {
        Log.i(TAG, msg);
    }

    public static void d(String msg) {
        Log.d(TAG, msg);
    }

    public static void w(String msg) {
        Log.w(TAG, msg);
    }


    public static String getExceptionString(Throwable ex) {
        if (ex == null) {
            return "";
        }
        StringBuilder err = new StringBuilder();
        err.append("ExceptionDetailed:\n");
        err.append("====================Exception Info====================\n");
        err.append(ex.toString());
        err.append("\n");
        StackTraceElement[] stack = ex.getStackTrace();
        for (StackTraceElement stackTraceElement : stack) {
            err.append(stackTraceElement.toString()).append("\n");
        }
        Throwable cause = ex.getCause();
        if (cause != null) {
            err.append("【Caused by】: ");
            err.append(cause.toString());
            err.append("\n");
            StackTraceElement[] stackTrace = cause.getStackTrace();
            for (StackTraceElement stackTraceElement : stackTrace) {
                err.append(stackTraceElement.toString()).append("\n");
            }
        }
        err.append("===================================================");
        return err.toString();
    }
}
