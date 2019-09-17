package com.hzjy.download.core;

import android.annotation.SuppressLint;
import android.content.Context;

/**
 * pj567
 * 2019/9/16
 */

public class ContextManager {
    @SuppressLint("StaticFieldLeak")
    private static volatile ContextManager instance = null;
    private static final String TAG = "ContextManager";
    private static final Object lock = new Object();
    public static Context mContext;

    private ContextManager(Context context) {
        mContext = context.getApplicationContext();
    }

    public static ContextManager getInstance(Context context) {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new ContextManager(context);
                }
            }
        }
        return instance;
    }
}
