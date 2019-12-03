package com.hzjy.download.manager;

import com.hzjy.download.task.AbsThreadTask;
import com.hzjy.download.task.DownloadTask;
import com.hzjy.download.util.CommonUtil;
import com.hzjy.download.util.Constants;
import com.hzjy.download.util.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 下载线程池管理
 * pj567
 * 2019/11/25
 */
public class DownloadThreadManager {
    private static final ReentrantLock LOCK = new ReentrantLock();
    private static volatile DownloadThreadManager instance = null;
    private ExecutorService mExePool;
    private Map<String, FutureContainer> mDownloadTasks = new ConcurrentHashMap<>();
    private Logger logger = new Logger(this);

    public static DownloadThreadManager getInstance() {
        if (instance == null) {
            synchronized (DownloadThreadManager.class) {
                instance = new DownloadThreadManager();
            }
        }
        return instance;
    }

    private DownloadThreadManager() {
        mExePool = Executors.newFixedThreadPool(Constants.MAX_TASK * DownloadTask.MAX_CHILD);
    }

    public void startThread(DownloadTask task) {
        logger.e("startThread");
        try {
            LOCK.tryLock(2, TimeUnit.SECONDS);
            if (mExePool.isShutdown()) {
                logger.e("线程池已关闭");
                return;
            }
            String key = getKey(task.getKey());
            FutureContainer temp = mDownloadTasks.get(key);
            if (temp != null) {
                return;
            }
            FutureContainer container = new FutureContainer();
            container.threadTask = task;
            if (container.futureHashMap == null) {
                container.futureHashMap = new HashMap<>();
            }
            for (Map.Entry<Integer, AbsThreadTask> taskEntry : task.getThreadTaskMap().entrySet()) {
                Future submit = mExePool.submit(taskEntry.getValue());
                container.futureHashMap.put(taskEntry.getKey(), submit);
            }
            mDownloadTasks.put(key, container);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * 删除线程任务
     *
     * @param task 线程任务
     */
    public boolean removeTaskThread(DownloadTask task) {
        try {
            LOCK.tryLock(2, TimeUnit.SECONDS);
            if (mExePool.isShutdown()) {
                logger.e("线程池已经关闭");
                return false;
            }
            if (task == null) {
                return false;
            }
            FutureContainer temp = mDownloadTasks.get(getKey(task.getKey()));
            if (temp != null && temp.threadTask.getKey().equals(task.getKey())) {
                mDownloadTasks.remove(getKey(task.getKey()));
                task.destroy();
                logger.e(task.getTaskName() + "is destroy");
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            LOCK.unlock();
        }
        return false;
    }

    private String getKey(String key) {
        return CommonUtil.getStrMd5(key);
    }

    private class FutureContainer {
        HashMap<Integer, Future> futureHashMap;
        DownloadTask threadTask;
    }
}
