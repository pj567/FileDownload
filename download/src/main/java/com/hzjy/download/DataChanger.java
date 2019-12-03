package com.hzjy.download;

import android.content.Context;

import com.hzjy.download.db.DBController;
import com.hzjy.download.util.Logger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

/**
 * pj567
 * 2019/11/21
 */

public class DataChanger extends Observable {
    private volatile static DataChanger instance;
    private LinkedHashMap<String, DownloadEntity> mOperatedEntryMap;
    private Context mContext;
    private Logger logger = new Logger(this);
    private DataChanger(Context context) {
        mContext = context.getApplicationContext();
        mOperatedEntryMap = new LinkedHashMap<>();
    }

    public static DataChanger getInstance(Context context) {
        if (instance == null) {
            synchronized (DataChanger.class) {
                if (instance == null) {
                    instance = new DataChanger(context);
                }
            }
        }
        return instance;
    }

    public void postStatus(DownloadEntity entry) {
        mOperatedEntryMap.put(entry.getId(), entry);
        if (entry.getStatus() == DownloadStatus.CANCEL || entry.getStatus() == DownloadStatus.COMPLETED) {
            logger.e("下载完成");
            DBController.getInstance(mContext).deleteEntity(entry);
            mOperatedEntryMap.remove(entry.getId());
        } else {
            DBController.getInstance(mContext).newOrUpdate(entry);
        }
        setChanged();
        notifyObservers(entry);
    }

    public List<DownloadEntity> queryRecoverAllList() {
        List<DownloadEntity> entryList = null;
        for (Map.Entry<String, DownloadEntity> entry : mOperatedEntryMap.entrySet()) {
            if (entry.getValue().getStatus() == DownloadStatus.PAUSE
                    || entry.getValue().getStatus() == DownloadStatus.UPDATE
                    || entry.getValue().getStatus() == DownloadStatus.WAIT) {
                if (entryList == null) {
                    entryList = new ArrayList<>();
                }
                entryList.add(entry.getValue());
            }
        }
        return entryList;
    }

    public DownloadEntity queryDownloadEntryById(String entityId) {
        return mOperatedEntryMap.get(entityId);
    }

    public void addToOperatedEntryMap(String entityId, DownloadEntity entity) {
        mOperatedEntryMap.put(entityId, entity);
    }

    public boolean containsDownloadEntry(String entityId) {
        return mOperatedEntryMap.containsKey(entityId);
    }
}
