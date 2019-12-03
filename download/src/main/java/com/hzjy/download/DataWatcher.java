package com.hzjy.download;

import java.util.Observable;
import java.util.Observer;

/**
 * pj567
 * 2019/11/21
 */

public abstract class DataWatcher implements Observer {
    @Override
    public void update(Observable observable, Object data) {
        if (data instanceof DownloadEntity) {
            notifyUpdate((DownloadEntity) data);
        }
    }

    public abstract void notifyUpdate(DownloadEntity data);
}
