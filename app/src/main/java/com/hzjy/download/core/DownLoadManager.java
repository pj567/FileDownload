package com.hzjy.download.core;


import com.hzjy.download.pool.CachePool;
import com.hzjy.download.pool.DownPool;
import com.hzjy.download.pool.ExecutePool;
import com.hzjy.download.util.L;

import java.util.Map;

/**
 * pj567
 * 2019/9/16
 */

public class DownLoadManager implements OnInternalListener {
    private static final String TAG = "DownLoadManager:";
    private static volatile DownLoadManager instance = null;
    private static final Object lock = new Object();
    private CachePool cachePool;
    private ExecutePool executePool;

    private DownLoadManager() {
        cachePool = DownPool.getInstance().cachePool;
        executePool = DownPool.getInstance().executePool;
    }

    public static DownLoadManager getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new DownLoadManager();
                }
            }
        }
        return instance;
    }

    public void putTask(DownloadTask task) {
        task.setOnInternalListener(this);
        if (executePool.size() < executePool.maxSize()) {
            executePool.putTask(task);
            ThreadManager.getInstance().startThread(task);
        } else {
            cachePool.putTask(task);
        }
    }

    private void next(DownloadTask task) {
        if (executePool.size() >= executePool.maxSize()) {
            boolean taskExits = executePool.taskExits(task.getKey());
            if (taskExits) {
                executePool.removeTask(task);
                if (cachePool.size() > 0) {
                    DownloadTask newTask = cachePool.pollTask();
                    executePool.putTask(newTask);
                    L.e(TAG + "执行新的" + newTask.getTaskName());
                    ThreadManager.getInstance().startThread(newTask);
                }
            }
        }
    }

    @Override
    public void onInternal(DownloadTask task, int what) {
        switch (what) {
            case COMPLETE:
                L.e(TAG + task.getTaskName() + "下载完成");
                ThreadManager.getInstance().removeSingleTaskThread(task);
                next(task);
                break;
            case STOP:
                L.e(TAG + task.getTaskName() + "任务停止");
                next(task);
                break;
        }
    }

    public void stopTask(DownloadTask task) {
        boolean taskExits = executePool.taskExits(task.getKey());
        if (taskExits) {
            boolean b = ThreadManager.getInstance().removeSingleTaskThread(executePool.getTask(task.getKey()));
            L.e(TAG + "" + (b ? "停止成功" : "停止失败"));
        } else {
            boolean cacheTask = cachePool.taskExits(task.getKey());
            if (cacheTask) {
                DownloadTask cancelTask = cachePool.getTask(task.getKey());
                IEventListener eventListener = cancelTask.getEventListener();
                boolean b = cachePool.removeTask(cancelTask);
                if (eventListener != null) {
                    eventListener.onCancel(cancelTask);
                }
                L.e(TAG + "" + (b ? "停止成功" : "停止失败"));
            }
        }
    }


    public void cancel(DownloadTask task) {
        boolean executeTask = executePool.taskExits(task.getKey());
        if (executeTask) {
            DownloadTask cancelTask = executePool.getTask(task.getKey());
            IEventListener eventListener = cancelTask.getEventListener();
            boolean b = ThreadManager.getInstance().removeSingleTaskThread(cancelTask);
            if (eventListener != null) {
                eventListener.onCancel(cancelTask);
            }
            L.e(TAG + "" + (b ? "取消成功" : "取消失败"));
        } else {
            boolean cacheTask = cachePool.taskExits(task.getKey());
            if (cacheTask) {
                DownloadTask cancelTask = cachePool.getTask(task.getKey());
                IEventListener eventListener = cancelTask.getEventListener();
                boolean b = cachePool.removeTask(cachePool.getTask(task.getKey()));
                L.e(TAG + "" + (b ? "取消成功" : "取消失败"));
                if (eventListener != null) {
                    eventListener.onCancel(cancelTask);
                }
            }
        }
    }

    public void allCancel() {
        for (String key : cachePool.getAllTask().keySet()) {
            DownloadTask task = cachePool.getAllTask().get(key);
            IEventListener eventListener = task.getEventListener();
            boolean b = cachePool.removeTask(cachePool.getTask(task.getKey()));
            L.e(TAG + "" + (b ? "取消成功" : "取消失败"));
            if (eventListener != null) {
                eventListener.onCancel(task);
            }
        }
        for (String key : executePool.getAllTask().keySet()) {
            DownloadTask task = executePool.getAllTask().get(key);
            boolean b = ThreadManager.getInstance().removeSingleTaskThread(task);
        }

    }

    public void setMaxSize(int maxSize) {
        executePool.setMaxNum(maxSize);
        ThreadManager.getInstance().setPoolSize(maxSize);
    }
}
