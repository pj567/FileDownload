package com.hzjy.download.pool;

import android.text.TextUtils;

import com.hzjy.download.task.DownloadTask;
import com.hzjy.download.util.CommonUtil;
import com.hzjy.download.util.Constants;
import com.hzjy.download.util.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * pj567
 * 2019/11/25
 */

public class DownloadCachePool implements IDownloadPool {
    private static final int MAX_NUM = Integer.MAX_VALUE;  //最大下载任务数
    private static final Object LOCK = new Object();
    private Map<String, DownloadTask> mCacheMap;
    private LinkedBlockingQueue<DownloadTask> mCacheQueue;
    private Logger logger;

    public DownloadCachePool() {
        logger = new Logger(this);
        mCacheQueue = new LinkedBlockingQueue<>(MAX_NUM);
        mCacheMap = new ConcurrentHashMap<>();
    }

    @Override
    public boolean putTask(DownloadTask task) {
        synchronized (LOCK) {
            if (task == null) {
                logger.e("任务不能为空");
                return false;
            }
            String key = task.getKey();
            if (mCacheQueue.contains(task)) {
                logger.e(task.getTaskName() + "进入缓存队列失败,队列中已存在");
                return false;
            } else {
                boolean offer = mCacheQueue.offer(task);
                if (offer) {
                    logger.e(task.getTaskName() + "进入缓存队列成功");
                    mCacheMap.put(CommonUtil.keyToHashKey(key), task);
                }
                return offer;
            }
        }
    }

    @Override
    public DownloadTask pollTask() {
        synchronized (LOCK) {
            try {
                DownloadTask task = mCacheQueue.poll(Constants.CACHE_TIME_OUT, TimeUnit.MICROSECONDS);
                if (task != null) {
                    String key = task.getKey();
                    logger.e(task.getTaskName() + "从缓存队列出队成功");
                    mCacheMap.remove(CommonUtil.keyToHashKey(key));
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
        synchronized (LOCK) {
            if (CommonUtil.isEmpty(key)) {
                logger.e("从缓存队列获取任务失败,key为null");
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
        synchronized (LOCK) {
            if (task == null) {
                logger.e("从缓存队列移除失败,任务不能为空");
                return false;
            } else {
                String key = CommonUtil.keyToHashKey(task.getKey());
                mCacheMap.remove(key);
                logger.e(task.getTaskName() + "从缓存队列移除成功");
                return mCacheQueue.remove(task);
            }
        }
    }

    @Override
    public boolean removeTask(String key) {
        synchronized (LOCK) {
            if (TextUtils.isEmpty(key)) {
                logger.e("请传入有效的下载链接");
                return false;
            }
            String temp = CommonUtil.keyToHashKey(key);
            DownloadTask task = mCacheMap.get(temp);
            mCacheMap.remove(temp);
            logger.e(task.getTaskName() + "从缓存队列移除成功");
            return mCacheQueue.remove(task);
        }
    }

    @Override
    public int size() {
        return mCacheQueue.size();
    }

    @Override
    public Map<String, DownloadTask> getAllTask() {
        return mCacheMap;
    }
}
