
package com.hzjy.download.pool;

import android.text.TextUtils;

import com.hzjy.download.core.DownloadTask;
import com.hzjy.download.util.CommonUtil;
import com.hzjy.download.util.L;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 任务执行池，所有当前下载任务都该任务池中，默认下载大小为2
 */
public class ExecutePool implements IPool {
    private final String TAG = "ExecutePool:";
    private static final Object lock = new Object();
    private final long TIME_OUT = 1000;
    private ArrayBlockingQueue<DownloadTask> mExecuteQueue;
    private Map<String, DownloadTask> mExecuteMap;
    private int mSize;

    ExecutePool() {
        mSize = getMaxSize();
        mExecuteQueue = new ArrayBlockingQueue<>(mSize);
        mExecuteMap = new ConcurrentHashMap<>();
    }

    /**
     * 获取最大任务数配置
     */
    protected int getMaxSize() {
        return 2;
    }

    public int maxSize() {
        return mSize;
    }

    /**
     * 获取所有正在执行的任务
     */
    public Map<String, DownloadTask> getAllTask() {
        return mExecuteMap;
    }

    @Override
    public boolean putTask(DownloadTask task) {
        synchronized (lock) {
            if (task == null) {
                L.e(TAG + "任务不能为空！！");
                return false;
            }
            if (mExecuteQueue.contains(task)) {
                L.e(TAG + "任务【" + task.getTaskName() + "】进入执行队列失败，原因：已经在执行队列中");
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

    /**
     * 设置执行队列最大任务数
     *
     * @param maxNum 下载数
     */
    public void setMaxNum(int maxNum) {
        synchronized (lock) {
            try {
                ArrayBlockingQueue<DownloadTask> temp = new ArrayBlockingQueue<>(maxNum);
                DownloadTask task;
                while ((task = mExecuteQueue.poll(TIME_OUT, TimeUnit.MICROSECONDS)) != null) {
                    temp.offer(task);
                }
                mExecuteQueue = temp;
                mSize = maxNum;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 添加新任务
     *
     * @param newTask 新任务
     */
    boolean putNewTask(DownloadTask newTask) {
        synchronized (lock) {
            String url = newTask.getKey();
            boolean s = mExecuteQueue.offer(newTask);
            L.e(TAG + "任务【" + newTask.getTaskName() + "】进入执行队列" + (s ? "成功" : "失败"));
            if (s) {
                mExecuteMap.put(CommonUtil.keyToHashKey(url), newTask);
            }
            return s;
        }
    }

    /**
     * 队列满时，将移除下载队列中的第一个任务
     */
    boolean pollFirstTask() {
        synchronized (lock) {
            try {
                DownloadTask oldTask = mExecuteQueue.poll(TIME_OUT, TimeUnit.MICROSECONDS);
                if (oldTask == null) {
                    L.e(TAG + "移除任务失败，原因：任务为null");
                    return false;
                }
                oldTask.destroy();
                String key = CommonUtil.keyToHashKey(oldTask.getKey());
                mExecuteMap.remove(key);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }

    @Override
    public DownloadTask pollTask() {
        synchronized (lock) {
            try {
                DownloadTask task;
                task = mExecuteQueue.poll(TIME_OUT, TimeUnit.MICROSECONDS);
                if (task != null) {
                    String url = task.getKey();
                    mExecuteMap.remove(CommonUtil.keyToHashKey(url));
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
        synchronized (lock) {
            if (TextUtils.isEmpty(key)) {
                L.e(TAG + "key 为null");
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
        synchronized (lock) {
            if (task == null) {
                L.e(TAG + "任务不能为空");
                return false;
            } else {
                return removeTask(task.getKey());
            }
        }
    }

    @Override
    public boolean removeTask(String key) {
        synchronized (lock) {
            if (TextUtils.isEmpty(key)) {
                L.e(TAG + "key 为null");
                return false;
            }
            String convertKey = CommonUtil.keyToHashKey(key);
            DownloadTask task = mExecuteMap.get(convertKey);
            final int oldQueueSize = mExecuteQueue.size();
            boolean isSuccess = mExecuteQueue.remove(task);
            final int newQueueSize = mExecuteQueue.size();
            if (isSuccess && newQueueSize != oldQueueSize) {
                mExecuteMap.remove(convertKey);
                return true;
            }
            return false;
        }
    }

    @Override
    public int size() {
        return mExecuteQueue.size();
    }
}