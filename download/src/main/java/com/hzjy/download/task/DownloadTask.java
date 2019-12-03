package com.hzjy.download.task;

import android.os.Handler;
import android.os.Message;

import com.hzjy.download.DownloadEntity;
import com.hzjy.download.DownloadStatus;
import com.hzjy.download.manager.DownloadThreadManager;
import com.hzjy.download.util.CommonUtil;
import com.hzjy.download.util.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * pj567
 * 2019/11/25
 */

public class DownloadTask implements ThreadInfoTask.ConnectListener, AbsThreadTask.DownloadListener {
    public static final int NOTIFY_CONNECT = 1;
    public static final int NOTIFY_START = 2;
    public static final int NOTIFY_UPDATE = 3;
    public static final int NOTIFY_PAUSED = 4;
    public static final int NOTIFY_CANCEL = 5;
    public static final int NOTIFY_COMPLETED = 6;
    public static final int NOTIFY_ERROR = 7;
    public static final int MAX_CHILD = 3;
    private Handler mHandler;
    private DownloadEntity entity;
    private Map<Integer, AbsThreadTask> threadTaskMap;
    private ThreadInfoTask infoTask;
    private ExecutorService mExecutor;
    private Logger logger = new Logger(this);
    private volatile boolean isPaused;
    private volatile boolean isCanceled;
    private volatile int taskCount = 0;

    public DownloadTask(DownloadEntity entity, Handler handler) {
        this.entity = entity;
        this.threadTaskMap = new HashMap<>();
        this.mHandler = handler;
    }

    public void start() {
        taskCount = 0;
        if (entity.getTotalLength() > 0) {
            startDownload();
        } else {
            if (mExecutor == null) {
                mExecutor = Executors.newCachedThreadPool();
            }
            entity.setStatus(DownloadStatus.CONNECT);
            notifyUpdate(entity, DownloadTask.NOTIFY_CONNECT);
            infoTask = new ThreadInfoTask(entity.getUrl(), this);
            mExecutor.submit(infoTask);
        }
    }

    public void pause() {
        isPaused = true;
        if (infoTask != null && infoTask.isRunning()) {
            infoTask.cancel();
        }
    }

    public void cancel() {
        isCanceled = true;
        if (infoTask != null && infoTask.isRunning()) {
            infoTask.cancel();
        }
    }

    public void destroy() {
        for (Map.Entry<Integer, AbsThreadTask> threadTask : threadTaskMap.entrySet()) {
            threadTask.getValue().destroy();
        }
    }

    public String getKey() {
        return entity != null && !CommonUtil.isEmpty(entity.getUrl()) ? entity.getUrl() : "";
    }

    public String getTaskName() {
        return entity != null && !CommonUtil.isEmpty(entity.getFileName()) ? entity.getFileName() : "";
    }

    public DownloadEntity getDownloadEntity() {
        return entity;
    }

    public Map<Integer, AbsThreadTask> getThreadTaskMap() {
        return threadTaskMap;
    }

    public void notifyUpdate(DownloadEntity entity, int what) {
        Message msg = mHandler.obtainMessage();
        msg.what = what;
        msg.obj = entity;
        mHandler.sendMessage(msg);
    }

    @Override
    public void onConnect(boolean isSupportRange, int totalLength) {
        entity.setRange(isSupportRange);
        entity.setTotalLength(totalLength);
        startDownload();
    }

    @Override
    public void onInfoError(String message) {
        entity.setStatus(DownloadStatus.ERROR);
        notifyUpdate(entity, DownloadTask.NOTIFY_ERROR);
    }

    private void startDownload() {
        isPaused = false;
        isCanceled = false;
        entity.setStatus(DownloadStatus.START);
        notifyUpdate(entity, DownloadTask.NOTIFY_START);
        if (entity.isRange()) {
            startMultiDownload();
        } else {
            startSingleDownload();
        }
    }

    private void startSingleDownload() {
        logger.e("startSingleDownload");
        File file = new File(entity.getFilePath() + entity.getFileName());
        if (file.exists()) {
            file.delete();
        }
        ThreadSingleTask singleTask = new ThreadSingleTask(entity, mHandler, ThreadSingleTask.SINGLE_POSITION, this);
        threadTaskMap.put(ThreadSingleTask.SINGLE_POSITION, singleTask);
        DownloadThreadManager.getInstance().startThread(this);
    }

    private void startMultiDownload() {
        long block = entity.getTotalLength() / MAX_CHILD;
        long startPos = 0;
        long endPos = 0;
        if (entity.rangeMap == null) {
            entity.rangeMap = new HashMap<>();
            for (int i = 0; i < MAX_CHILD; i++) {
                entity.rangeMap.put(i, 0L);
            }
        }
        for (int i = 0; i < MAX_CHILD; i++) {
            startPos = i * block + entity.rangeMap.get(i);
            if (i == MAX_CHILD - 1) {
                endPos = entity.getTotalLength();
            } else {
                endPos = (i + 1) * block - 1;
            }
            if (startPos < endPos) {
                ThreadRangeTask rangeTask = new ThreadRangeTask(entity, mHandler, i, startPos, endPos, this);
                threadTaskMap.put(i, rangeTask);
            }
        }
        DownloadThreadManager.getInstance().startThread(this);
    }

    @Override
    public void onProgress(int position, long progress) {
        if (position == ThreadSingleTask.SINGLE_POSITION) {
            entity.setCurrentLength(progress);
        } else {
            long range = entity.rangeMap.get(position) + progress;
            entity.rangeMap.put(position, range);
            long currentLength = 0;
            for (Map.Entry<Integer, Long> entry : entity.rangeMap.entrySet()) {
                currentLength += entry.getValue();
            }
            entity.setCurrentLength(currentLength);
        }
        if (entity.getTotalLength() > 0) {
            int percent = (int) (entity.getCurrentLength() * 100L / entity.getTotalLength());
            if (percent > entity.getPercent()) {
                entity.setPercent(percent);
                entity.setStatus(DownloadStatus.UPDATE);
                notifyUpdate(entity, DownloadTask.NOTIFY_UPDATE);
            }
        }
    }

    @Override
    public void onDestroy(int position) {
        //要是下载不支持分段下载直接通知UI取消
        if (position == ThreadSingleTask.SINGLE_POSITION) {
            entity.setStatus(DownloadStatus.CANCEL);
            notifyUpdate(entity, DownloadTask.NOTIFY_CANCEL);
            entity.reset();
            File file = new File(entity.getFilePath() + entity.getFileName());
            if (file.exists()) {
                file.delete();
            }
        } else {
            if (threadTaskMap.containsKey(position)) {
                taskCount++;
                if (taskCount < threadTaskMap.size()) {
                    return;
                }
            }
            taskCount = 0;
            if (isPaused) {
                entity.setStatus(DownloadStatus.PAUSE);
                notifyUpdate(entity, DownloadTask.NOTIFY_PAUSED);
            } else if (isCanceled) {
                entity.setStatus(DownloadStatus.CANCEL);
                notifyUpdate(entity, DownloadTask.NOTIFY_CANCEL);
                File file = new File(entity.getFilePath() + entity.getFileName());
                if (file.exists()) {
                    file.delete();
                }
            }
        }
    }

    @Override
    public void onCompleted(int position) {
        if (position == ThreadSingleTask.SINGLE_POSITION) {
            entity.setStatus(DownloadStatus.COMPLETED);
            notifyUpdate(entity, DownloadTask.NOTIFY_COMPLETED);
        } else {
            if (threadTaskMap.containsKey(position)) {
                taskCount++;
                if (taskCount < threadTaskMap.size()) {
                    return;
                }
            }
            taskCount = 0;
            entity.setStatus(DownloadStatus.COMPLETED);
            notifyUpdate(entity, DownloadTask.NOTIFY_COMPLETED);
        }
    }
}
