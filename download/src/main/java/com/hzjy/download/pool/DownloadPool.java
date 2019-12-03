package com.hzjy.download.pool;

/**
 * pj567
 * 2019/11/25
 */

public class DownloadPool {
    private static volatile DownloadPool instance;
    public DownloadCachePool cachePool;
    public DownloadExecutePool executePool;

    private DownloadPool() {
        cachePool = new DownloadCachePool();
        executePool = new DownloadExecutePool();
    }

    public static DownloadPool getInstance() {
        if (instance == null) {
            synchronized (DownloadPool.class) {
                if (instance == null) {
                    instance = new DownloadPool();
                }
            }
        }
        return instance;
    }
}
