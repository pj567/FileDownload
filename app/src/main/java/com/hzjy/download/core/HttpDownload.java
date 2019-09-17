package com.hzjy.download.core;

import android.content.Context;
import android.os.Environment;

import com.hzjy.download.entity.DownLoadEntity;

/**
 * pj567
 * 2019/9/16
 */

public class HttpDownload {
    private static volatile HttpDownload instance = null;

    private HttpDownload(Context context) {
        init(context.getApplicationContext());
    }

    public static ContextManager init(Context context) {
        return ContextManager.getInstance(context);
    }

    public static HttpDownload download(Context context) {
        if (instance == null) {
            synchronized (HttpDownload.class) {
                if (instance == null) {
                    instance = new HttpDownload(context);
                }
            }
        }
        return instance;
    }

    public DownloadTarget load(String url) {

        return new DownloadTarget(url);
    }

    public DownloadTarget load(DownLoadEntity entity) {

        return new DownloadTarget(entity);
    }

    public void setMaxNum(int max) {
        DownLoadManager.getInstance().setMaxSize(max);
    }

    public void allCancel() {
        DownLoadManager.getInstance().allCancel();
    }

}
