package com.hzjy.download.manager;

import android.content.Context;

import com.hzjy.download.DataChanger;
import com.hzjy.download.DataWatcher;
import com.hzjy.download.DownloadEntity;

/**
 * pj567
 * 2019/11/25
 */

public class DownloadManager {
    private static volatile DownloadManager instance;
    private Context mContext;
    private DataChanger mDataChanger;
    private DownloadManager(Context context) {
        this.mContext = context.getApplicationContext();
        DownloadTaskManager.getInstance().init(mContext);
        mDataChanger = DataChanger.getInstance(mContext);
    }

    public static DownloadManager download(Context context) {
        if (instance == null) {
            synchronized (DownloadManager.class) {
                if (instance == null) {
                    instance = new DownloadManager(context);
                }
            }
        }
        return instance;
    }

    public DownloadTarget load(DownloadEntity entity) {
        return new DownloadTarget(entity);
    }

    public DownloadTarget load(String url) {
        return new DownloadTarget(url);
    }

    public void addObserver(DataWatcher watcher) {
        DataChanger.getInstance(mContext).addObserver(watcher);
    }

    public void removeObserver(DataWatcher watcher) {
        DataChanger.getInstance(mContext).deleteObserver(watcher);
        pauseAll();
    }

    public void pauseAll() {
        DownloadTaskManager.getInstance().pauseAll();
    }

    public void recoverAll() {
        DownloadTaskManager.getInstance().recoverAll();
    }
    public DownloadEntity queryDownloadEntryById(String entryId){
        return mDataChanger.queryDownloadEntryById(entryId);
    }
}
