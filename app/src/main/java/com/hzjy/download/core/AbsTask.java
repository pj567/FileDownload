package com.hzjy.download.core;

import android.net.TrafficStats;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;

import com.hzjy.download.entity.DownLoadEntity;
import com.hzjy.download.util.L;

import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * pj567
 * 2019/9/16
 */

public abstract class AbsTask implements Callable<AbsTask>, Handler.Callback {
    protected DownLoadEntity entity;
    private boolean isDestroy = false;
    protected Handler mHandler;

    public AbsTask(DownLoadEntity entity) {
        this.entity = entity;
        this.mHandler =  new Handler(Looper.getMainLooper(), this);
    }

    public void setEntity(DownLoadEntity entity) {
        this.entity = entity;
    }

    @Override
    public AbsTask call() throws Exception {
        isDestroy = false;
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        TrafficStats.setThreadStatsTag(UUID.randomUUID().toString().hashCode());
        return this;
    }

    protected boolean isLive() {
        return !Thread.currentThread().isInterrupted() && !isDestroy;
    }

    public boolean isDestroy() {
        return Thread.currentThread().isInterrupted();
    }

    public void destroy() {
        this.isDestroy = true;
    }
}
