package com.hzjy.download.pool;

import android.text.TextUtils;

import com.hzjy.download.core.DownloadTask;
import com.hzjy.download.util.CommonUtil;
import com.hzjy.download.util.L;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * pj567
 * 2019/9/16
 */

public class CachePool implements IPool {
    private final String TAG = "CachePool:";
    private static final int MAX_NUM = Integer.MAX_VALUE;  //最大下载任务数
    private static final long TIME_OUT = 1000;
    private static final Object lock = new Object();
    private Map<String, DownloadTask> mCacheMap;
    private LinkedBlockingQueue<DownloadTask> mCacheQueue;

    CachePool() {
        mCacheQueue = new LinkedBlockingQueue<>(MAX_NUM);
        mCacheMap = new ConcurrentHashMap<>();

    }

    /**
     * 获取被缓存的任务
     */
    public Map<String, DownloadTask> getAllTask() {
        return mCacheMap;
    }

    /**
     * 清除所有缓存的任务
     */
    public void clear() {
        for (String key : mCacheMap.keySet()) {
            DownloadTask task = mCacheMap.get(key);
            mCacheQueue.remove(task);
            mCacheMap.remove(key);
        }
    }

    /**
     * 将任务放在队首
     */
    public boolean putTaskToFirst(DownloadTask task) {
        if (mCacheQueue.isEmpty()) {
            return putTask(task);
        } else {
            Set<DownloadTask> temps = new LinkedHashSet<>();
            temps.add(task);
            for (int i = 0, len = size(); i < len; i++) {
                DownloadTask temp = pollTask();
                temps.add(temp);
            }
            for (DownloadTask t : temps) {
                putTask(t);
            }
            return true;
        }
    }

    @Override
    public boolean putTask(DownloadTask task) {
        synchronized (lock) {
            if (task == null) {
                L.e(TAG + "任务不能为空！！");
                return false;
            }
            String key = task.getKey();
            if (mCacheQueue.contains(task)) {
                L.e(TAG + "任务【" + task.getTaskName() + "】进入缓存队列失败，原因：已经在缓存队列中");
                return false;
            } else {
                boolean offer = mCacheQueue.offer(task);
                L.e(TAG + "任务【" + task.getTaskName() + "】进入缓存队列" + (offer ? "成功" : "失败"));
                if (offer) {
                    mCacheMap.put(CommonUtil.keyToHashKey(key), task);
                }
                return offer;
            }
        }
    }

    @Override
    public DownloadTask pollTask() {
        synchronized (lock) {
            try {
                DownloadTask task = mCacheQueue.poll(TIME_OUT, TimeUnit.MICROSECONDS);
                if (task != null) {
                    String url = task.getKey();
                    mCacheMap.remove(CommonUtil.keyToHashKey(url));
                }
                return task;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public DownloadTask getTask(String key) {
        synchronized (lock) {
            if (TextUtils.isEmpty(key)) {
                L.e(TAG + "key 为null");
                return null;
            }
            return mCacheMap.get(CommonUtil.keyToHashKey(key));
        }
    }

    @Override
    public boolean taskExits(String key) {
        return mCacheMap.containsKey(CommonUtil.keyToHashKey(key));
    }

    @Override
    public boolean removeTask(DownloadTask task) {
        synchronized (lock) {
            if (task == null) {
                L.e(TAG + "任务不能为空");
                return false;
            } else {
                String key = CommonUtil.keyToHashKey(task.getKey());
                mCacheMap.remove(key);
                return mCacheQueue.remove(task);
            }
        }
    }

    @Override
    public boolean removeTask(String key) {
        synchronized (lock) {
            if (TextUtils.isEmpty(key)) {
                L.e(TAG + "请传入有效的下载链接");
                return false;
            }
            String temp = CommonUtil.keyToHashKey(key);
            DownloadTask task = mCacheMap.get(temp);
            mCacheMap.remove(temp);
            return mCacheQueue.remove(task);
        }
    }

    @Override
    public int size() {
        return mCacheQueue.size();
    }
}
