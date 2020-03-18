package com.hzjy.download.manager;

import android.content.Context;

import com.hzjy.download.DownloadEntity;
import com.hzjy.download.util.CommonUtil;
import com.hzjy.download.util.Constants;

/**
 * user by pj567
 * date on 2019/11/26.
 */

public class DownloadTarget {
    private DownloadEntity entity;
    public DownloadTarget(DownloadEntity entity) {
        this.entity = entity;
    }

    public DownloadTarget(String url) {
        String fileName = url.substring(url.lastIndexOf("/") + 1, url.length());
        if (fileName.contains("?")) {
            fileName = fileName.substring(0, fileName.indexOf("?"));
        }
        entity = new DownloadEntity(CommonUtil.getStrMd5(url), url, Constants.FILE_PATH, fileName);
    }

    public DownloadTarget setFilePath(String filePath) {
        entity.setFilePath(filePath);
        return this;
    }

    public DownloadTarget setFileName(String fileName) {
        entity.setFileName(fileName);
        return this;
    }

    public void start() {
        DownloadTaskManager.getInstance().start(entity);
    }

    public void pause() {
        DownloadTaskManager.getInstance().pause(entity);
    }

    public void cancel() {
        DownloadTaskManager.getInstance().cancel(entity);
    }
}
