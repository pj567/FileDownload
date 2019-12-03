package com.hzjy.download.task;

import android.net.TrafficStats;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.text.TextUtils;

import com.hzjy.download.DownloadEntity;
import com.hzjy.download.util.CommonUtil;
import com.hzjy.download.util.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

/**
 * pj567
 * 2019/11/25
 */

public abstract class AbsThreadTask implements Callable<AbsThreadTask> {
    protected DownloadEntity entity;
    protected int position;
    protected Handler mHandler;
    private volatile boolean isDestroy;
    protected int bufferSize = 2048;
    protected DownloadListener listener;
    protected Logger logger = new Logger(this);

    public AbsThreadTask(DownloadEntity entity, Handler handler, int position, DownloadListener listener) {
        this.entity = entity;
        this.mHandler = handler;
        this.position = position;
        this.listener = listener;
    }

    @Override
    public AbsThreadTask call() throws Exception {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        TrafficStats.setThreadStatsTag(UUID.randomUUID().toString().hashCode());
        return this;
    }

    public boolean isRunning() {
        return !Thread.currentThread().isInterrupted() && !isDestroy;
    }

    public boolean isInterrupted() {
        return Thread.currentThread().isInterrupted();
    }

    public void destroy() {
        isDestroy = true;
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

    public void notifyUpdate(DownloadEntity entity, int what) {
        Message msg = mHandler.obtainMessage();
        msg.what = what;
        msg.obj = entity;
        mHandler.sendMessage(msg);
    }

    protected InputStream convertInputStream(HttpURLConnection connection) throws IOException {
        String encoding = connection.getHeaderField("Content-Encoding");
        if (TextUtils.isEmpty(encoding)) {
            return connection.getInputStream();
        }
        if (encoding.contains("gzip")) {
            return new GZIPInputStream(connection.getInputStream());
        } else if (encoding.contains("deflate")) {
            return new InflaterInputStream(connection.getInputStream());
        } else {
            return connection.getInputStream();
        }
    }

    interface DownloadListener {
        void onProgress(int position, long progress);

        void onDestroy(int position);

        void onCompleted(int position);
    }
}
