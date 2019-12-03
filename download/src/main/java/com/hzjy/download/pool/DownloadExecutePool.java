package com.hzjy.download.pool;

import com.hzjy.download.util.Logger;
import com.hzjy.download.task.DownloadTask;
import com.hzjy.download.util.CommonUtil;
import com.hzjy.download.util.Constants;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * pj567
 * 2019/11/25
 */

public class DownloadExecutePool implements IDownloadPool {
    private static final Object LOCK = new Object();
    private ArrayBlockingQueue<DownloadTask> mExecuteQueue;
    private Map<String, DownloadTask> mExecuteMap;
    private int mSize;
    private Logger logger;

    public DownloadExecutePool() {
        logger = new Logger(this);
        mSize = getMaxSize();
        mExecuteQueue = new ArrayBlockingQueue<>(mSize);
        mExecuteMap = new ConcurrentHashMap<>();
    }

    /**
     * 获取最大任务数配置
     */
    private int getMaxSize() {
        return Constants.MAX_TASK;
    }

    public int getMaxTaskSize() {
        return mSize;
    }


    @Override
    public boolean putTask(DownloadTask task) {
        synchronized (LOCK) {
            if (task == null) {
                logger.e("任务不能为空！！");
                return false;
            }
            if (mExecuteQueue.contains(task)) {
                logger.e(task.getTaskName() + "进入执行队列失败,已经在执行队列中");
                return false;
            } else {
                if (mExecuteQueue.size() >= mSize) {
                    if (pollFirstTask()) {
                        return putNewTask(task);
                    }
                } else {
                    return putNewTask(task);
                }
            }
        }
        return false;
    }

    /***
     * 执行新的下载移除队列中第一条执行完的下载
     * @return
     */
    private boolean pollFirstTask() {
        synchronized (LOCK) {
            try {
                DownloadTask oldTask = mExecuteQueue.poll(Constants.EXECUTE_TIME_OUT, TimeUnit.MICROSECONDS);
                if (oldTask == null) {
                    logger.e("从执行队列移除任务失败,任务为null");
                    return false;
                }
                oldTask.destroy();
                String key = CommonUtil.keyToHashKey(oldTask.getKey());
                mExecuteMap.remove(key);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
            logger.e("从执行队列移除任务成功");
            return true;
        }
    }

    /**
     * 执行下一条任务
     *
     * @param newTask
     * @return
     */
    private boolean putNewTask(DownloadTask newTask) {
        synchronized (LOCK) {
            String url = newTask.getKey();
            boolean offer = mExecuteQueue.offer(newTask);
            logger.e(newTask.getTaskName() + "进入执行队列" + (offer ? "成功" : "失败"));
            if (offer) {
                mExecuteMap.put(CommonUtil.keyToHashKey(url), newTask);
            }
            return offer;
        }
    }

    @Override
    public DownloadTask pollTask() {
        synchronized (LOCK) {
            try {
                DownloadTask task;
                task = mExecuteQueue.poll(Constants.EXECUTE_TIME_OUT, TimeUnit.MICROSECONDS);
                if (task != null) {
                    String key = task.getKey();
                    mExecuteMap.remove(CommonUtil.keyToHashKey(key));
                    logger.e("从执行队列中出队成功");
                } else {
                    logger.e("从执行队列中出队失败");
                }
                return task;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    public DownloadTask getTask(String key) {
        synchronized (LOCK) {
            if (CommonUtil.isEmpty(key)) {
                logger.e("从执行队列中获取任务失败,key为null");
                return null;
            }
            return mExecuteMap.get(CommonUtil.keyToHashKey(key));
        }
    }

    @Override
    public boolean taskExits(String key) {
        return mExecuteMap.containsKey(CommonUtil.keyToHashKey(key));
    }

    @Override
    public boolean removeTask(DownloadTask task) {
        synchronized (LOCK) {
            if (task == null) {
                logger.e("从执行队列中移除任务失败,任务为空");
                return false;
            } else {
                return removeTask(task.getKey());
            }
        }
    }

    @Override
    public boolean removeTask(String key) {
        synchronized (LOCK) {
            if (CommonUtil.isEmpty(key)) {
                logger.e("从执行队列中移除任务失败,key为null");
                return false;
            }
            String convertKey = CommonUtil.keyToHashKey(key);
            DownloadTask task = mExecuteMap.get(convertKey);
            final int oldQueueSize = mExecuteQueue.size();
            boolean isSuccess = mExecuteQueue.remove(task);
            final int newQueueSize = mExecuteQueue.size();
            if (isSuccess && newQueueSize != oldQueueSize) {
                mExecuteMap.remove(convertKey);
                logger.e("从执行队列中移除任务成功");
                return true;
            }
            return false;
        }
    }

    /**
     * 执行队列中任务数量
     *
     * @return
     */
    @Override
    public int size() {
        return mExecuteMap.size();
    }

    /**
     * 获取所有正在执行的任务
     */
    @Override
    public Map<String, DownloadTask> getAllTask() {
        return mExecuteMap;
    }
}
