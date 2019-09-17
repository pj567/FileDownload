package com.hzjy.download.core;

import android.os.Environment;

import com.hzjy.download.entity.DownLoadEntity;

import java.io.File;

/**
 * pj567
 * 2019/9/16
 */

public class DownloadTarget {
    private DownLoadEntity entity;
    private DownloadTask task;

    public DownloadTarget(DownLoadEntity entity) {
        this.entity = entity;
        task = new DownloadTask(entity);
    }

    public DownloadTarget(String url) {
        String fileName = url.substring(url.lastIndexOf("/") + 1, url.length());
        if (fileName.contains("?")) {
            fileName = fileName.substring(0, fileName.indexOf("?"));
        }
        String savePath = Environment.getExternalStorageDirectory().getPath() + "/hzjy/download/";
        entity = new DownLoadEntity(url, fileName, savePath);
        task = new DownloadTask(entity);
    }

    public DownloadTarget fileName(String fileName) {
        entity.setFileName(fileName);
        task.setEntity(entity);
        return this;
    }

    public DownloadTarget savePath(String savePath) {
        entity.setSavePath(savePath);
        task.setEntity(entity);
        return this;
    }

    public DownloadTarget setEventListener(IEventListener listener) {
        task.setEventListener(listener);
        return this;
    }

    public void start() {
        File file = new File(entity.getSavePath());
        if(!file.exists()&&!file.isDirectory()){
            file.mkdirs();
        }
        DownLoadManager.getInstance().putTask(task);
    }

    public void stop() {
        DownLoadManager.getInstance().stopTask(task);
    }

    public void cancel() {
        DownLoadManager.getInstance().cancel(task);
    }

}
