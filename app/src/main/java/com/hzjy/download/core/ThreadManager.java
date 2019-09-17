package com.hzjy.download.core;

import com.hzjy.download.util.CommonUtil;
import com.hzjy.download.util.L;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * pj567
 * 2019/9/16
 */

public class ThreadManager {
    private final String TAG = "ThreadManager:";
    private static final ReentrantLock lock = new ReentrantLock();
    private static volatile ThreadManager instance = null;
    private ExecutorService mExePool;
    private Map<String, FutureContainer> mThreadTasks = new ConcurrentHashMap<>();

    public static synchronized ThreadManager getInstance() {
        if (instance == null) {
            instance = new ThreadManager();
        }
        return instance;
    }

    private ThreadManager() {
        mExePool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    public void setPoolSize(int poolSize) {
        if (poolSize > Runtime.getRuntime().availableProcessors()) {
            mExePool = Executors.newFixedThreadPool(poolSize);
        }
    }

    public void startThread(DownloadTask task) {
        try {
            lock.tryLock(2, TimeUnit.SECONDS);
            if (mExePool.isShutdown()) {
                L.e(TAG + "线程池已关闭");
                return;
            }
            String key = getKey(task.getKey());
            FutureContainer temp = mThreadTasks.get(key);
            if (temp != null) {
                return;
            }
            FutureContainer container = new FutureContainer();
            container.threadTask = task;
            container.future = mExePool.submit(task);
            mThreadTasks.put(key, container);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 删除单个线程任务
     *
     * @param task 线程任务
     */
    public boolean removeSingleTaskThread(DownloadTask task) {
        try {
            lock.tryLock(2, TimeUnit.SECONDS);
            if (mExePool.isShutdown()) {
                L.e(TAG + "线程池已经关闭");
                return false;
            }
            if (task == null) {
                return false;
            }
            FutureContainer temp = mThreadTasks.get(getKey(task.getKey()));
            if (temp != null && temp.threadTask == task) {
                mThreadTasks.remove(getKey(task.getKey()));
                task.destroy();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return false;
    }

    private String getKey(String key) {
        return CommonUtil.getStrMd5(key);
    }

    private class FutureContainer {
        Future future;
        DownloadTask threadTask;
    }
}
