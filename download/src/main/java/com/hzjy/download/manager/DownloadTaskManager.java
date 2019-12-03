package com.hzjy.download.manager;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.hzjy.download.DataChanger;
import com.hzjy.download.DownloadEntity;
import com.hzjy.download.DownloadStatus;
import com.hzjy.download.db.DBController;
import com.hzjy.download.pool.DownloadCachePool;
import com.hzjy.download.pool.DownloadExecutePool;
import com.hzjy.download.pool.DownloadPool;
import com.hzjy.download.task.DownloadTask;
import com.hzjy.download.util.Logger;

import java.io.File;
import java.util.List;

/**
 * 任务管理
 * user by pj567
 * date on 2019/11/26.
 */

public class DownloadTaskManager implements Handler.Callback {
    private static volatile DownloadTaskManager instance;
    private static final Object LOCK = new Object();
    private DownloadCachePool cachePool;
    private DownloadExecutePool executePool;
    private Handler mHandler;
    private DataChanger mDataChanger;
    private Logger logger = new Logger(this);

    private DownloadTaskManager() {
        cachePool = DownloadPool.getInstance().cachePool;
        executePool = DownloadPool.getInstance().executePool;
        mHandler = new Handler(Looper.getMainLooper(), this);
    }

    public void init(Context context) {
        mDataChanger = DataChanger.getInstance(context.getApplicationContext());
        List<DownloadEntity> entityList = DBController.getInstance(context).queryAll();
        if (entityList != null) {
            for (DownloadEntity entity : entityList) {
                if (entity.getStatus() == DownloadStatus.UPDATE
                        || entity.getStatus() == DownloadStatus.WAIT
                        || entity.getStatus() == DownloadStatus.PAUSE) {
                    entity.setStatus(DownloadStatus.PAUSE);
                    addDownload(entity);
                }
                mDataChanger.addToOperatedEntryMap(entity.getId(), entity);
            }
        }
    }

    public static DownloadTaskManager getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new DownloadTaskManager();
                }
            }
        }
        return instance;
    }

    public void start(DownloadEntity entity) {
        if (mDataChanger.containsDownloadEntry(entity.getId())) {
            entity = mDataChanger.queryDownloadEntryById(entity.getId());
        }
        addDownload(entity);
    }

    public void pause(DownloadEntity entity) {
        String key = entity.getUrl();
        boolean executeTask = executePool.taskExits(key);
        if (executeTask) {
            DownloadTask cancelTask = executePool.getTask(key);
            cancelTask.pause();
            boolean b = DownloadThreadManager.getInstance().removeTaskThread(cancelTask);
            logger.e(b ? cancelTask.getTaskName() + "取消成功" : cancelTask.getTaskName() + "取消失败");
        } else {
            boolean cacheTask = cachePool.taskExits(key);
            if (cacheTask) {
                DownloadTask cancelTask = cachePool.getTask(key);
                boolean b = cachePool.removeTask(cancelTask);
                logger.e(b ? cancelTask.getTaskName() + "取消成功" : cancelTask.getTaskName() + "取消失败");
                cancelTask.pause();
            }
        }
    }

    public void cancel(DownloadEntity entity) {
        String key = entity.getUrl();
        boolean executeTask = executePool.taskExits(key);
        if (executeTask) {
            DownloadTask cancelTask = executePool.getTask(key);
            cancelTask.cancel();
            boolean b = DownloadThreadManager.getInstance().removeTaskThread(cancelTask);
            logger.e(b ? cancelTask.getTaskName() + "取消成功" : cancelTask.getTaskName() + "取消失败");
        } else {
            boolean cacheTask = cachePool.taskExits(key);
            if (cacheTask) {
                DownloadTask cancelTask = cachePool.getTask(key);
                boolean b = cachePool.removeTask(cancelTask);
                logger.e(b ? cancelTask.getTaskName() + "取消成功" : cancelTask.getTaskName() + "取消失败");
                cancelTask.cancel();
            }
        }
    }

    private void addDownload(DownloadEntity entity) {
        File file = new File(entity.getFilePath());
        if (!file.exists() && !file.isDirectory()) {
            logger.e("创建文件夹");
            boolean b = file.mkdirs();
            if (b) {
                logger.e("创建文件夹成功");
            }
        }
        DownloadTask downloadTask = new DownloadTask(entity, mHandler);
        if (executePool.size() < executePool.getMaxTaskSize()) {
            executePool.putTask(downloadTask);
            downloadTask.start();
        } else {
            entity.setStatus(DownloadStatus.WAIT);
            mDataChanger.postStatus(entity);
            cachePool.putTask(downloadTask);
        }
    }

    private void checkNext(DownloadEntity entity) {
        logger.e("checkNext");
        boolean taskExits = executePool.taskExits(entity.getUrl());
        if (taskExits) {
            executePool.removeTask(entity.getUrl());
        }
        DownloadTask task = cachePool.pollTask();
        if (task != null) {
            executePool.putTask(task);
            task.start();
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        DownloadEntity entity = (DownloadEntity) msg.obj;
        switch (msg.what) {
            case DownloadTask.NOTIFY_CONNECT:
                entity.setStatus(DownloadStatus.CONNECT);
//                logger.e("NOTIFY_CONNECT");
                break;
            case DownloadTask.NOTIFY_START:
                entity.setStatus(DownloadStatus.START);
//                logger.e("NOTIFY_START");
                break;
            case DownloadTask.NOTIFY_UPDATE:
                entity.setStatus(DownloadStatus.UPDATE);
//                logger.e("NOTIFY_UPDATE");
                break;
            case DownloadTask.NOTIFY_COMPLETED:
                entity.setStatus(DownloadStatus.COMPLETED);
                logger.e("NOTIFY_COMPLETED");
                checkNext(entity);
                break;
            case DownloadTask.NOTIFY_ERROR:
                entity.setStatus(DownloadStatus.ERROR);
                logger.e("NOTIFY_ERROR");
                checkNext(entity);
                break;
            case DownloadTask.NOTIFY_PAUSED:
                logger.e("NOTIFY_PAUSED");
                entity.setStatus(DownloadStatus.PAUSE);
                checkNext(entity);
                break;
            case DownloadTask.NOTIFY_CANCEL:
                logger.e("NOTIFY_CANCEL");
                entity.setStatus(DownloadStatus.CANCEL);
                checkNext(entity);
                break;
        }
        mDataChanger.postStatus(entity);
        return false;
    }

    public void pauseAll() {
        for (String key : cachePool.getAllTask().keySet()) {
            DownloadTask task = cachePool.getAllTask().get(key);
            cachePool.removeTask(cachePool.getTask(task.getKey()));
            DownloadEntity entity = task.getDownloadEntity();
            entity.setStatus(DownloadStatus.PAUSE);
            mDataChanger.postStatus(entity);
        }
        for (String key : executePool.getAllTask().keySet()) {
            DownloadTask task = executePool.getAllTask().get(key);
            pause(task.getDownloadEntity());
        }
    }

    public void recoverAll() {
        List<DownloadEntity> entityList = mDataChanger.queryRecoverAllList();
        if (entityList != null) {
            for (DownloadEntity entity : entityList) {
                addDownload(entity);
            }
        }
    }
}
