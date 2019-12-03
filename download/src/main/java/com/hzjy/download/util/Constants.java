package com.hzjy.download.util;

import android.os.Environment;

/**
 * pj567
 * 2019/11/21
 */

public class Constants {
    public static final int CACHE_TIME_OUT = 1000;
    public static final int EXECUTE_TIME_OUT = 1000;
    public static final int CONNECT_TIME = 10 * 1000;
    public static final int READ_TIME = 10 * 1000;
    public static final int MAX_TASK = 3;
    public static final String FILE_PATH = Environment.getExternalStorageDirectory().getPath() + "/hzjy/download/";
}
