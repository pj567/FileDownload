package com.hzjy.download.pool;

/**
 * pj567
 * 2019/9/16
 */

public class DownPool {
    private static volatile DownPool instance = null;
    public CachePool cachePool;
    public ExecutePool executePool;

    private DownPool() {
        cachePool = new CachePool();
        executePool = new ExecutePool();
    }

    public static DownPool getInstance() {
        if (instance == null) {
            synchronized (DownPool.class) {
                if (instance == null) {
                    instance = new DownPool();
                }
            }
        }
        return instance;
    }
}
