package com.hzjy.download.core;

/**
 * pj567
 * 2019/9/16
 */

public interface OnInternalListener {
    int STOP = 0;
    int COMPLETE = 1;

    void onInternal(DownloadTask task, int what);
}
