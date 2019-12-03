package com.hzjy.download.task;

import android.os.Handler;

import com.hzjy.download.DownloadStatus;
import com.hzjy.download.DownloadEntity;
import com.hzjy.download.util.CommonUtil;
import com.hzjy.download.util.Constants;
import com.hzjy.download.util.Logger;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * pj567
 * 2019/11/25
 */

public class ThreadRangeTask extends AbsThreadTask {
    private Logger logger = new Logger(this);
    private long startPos;
    private long endPos;

    public ThreadRangeTask(DownloadEntity entity, Handler handler, int position, long startPos, long endPos, DownloadListener listener) {
        super(entity, handler, position, listener);
        this.startPos = startPos;
        this.endPos = endPos;
        logger.e(startPos+"----"+endPos);
    }

    @Override
    public ThreadRangeTask call() throws Exception {
        logger.e("线程id:" + Thread.currentThread().getId());
        //执行下载
        if (CommonUtil.isEmpty(entity.getUrl())) {
            logger.e("url is null");
            entity.setStatus(DownloadStatus.ERROR);
            notifyUpdate(entity, DownloadTask.NOTIFY_ERROR);
            return this;
        }
        HttpURLConnection conn = null;
        BufferedInputStream is = null;
        RandomAccessFile file = null;
        try {
            URL url = new URL(entity.getUrl());
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Range", "bytes=" + startPos + "-" + endPos);
            conn.setConnectTimeout(Constants.CONNECT_TIME);
            conn.setReadTimeout(Constants.READ_TIME);
            conn.connect();
            if (conn.getResponseCode() == HttpURLConnection.HTTP_PARTIAL) {
                is = new BufferedInputStream(convertInputStream(conn));
                //创建可设置位置的文件
                file = new RandomAccessFile(entity.getFilePath() + entity.getFileName(), "rwd");
                file.seek(startPos);
                //设置每条线程写入文件的位置
                readNormal(is, file);
            } else {
                logger.e("连接失败");
                entity.setStatus(DownloadStatus.ERROR);
                notifyUpdate(entity, DownloadTask.NOTIFY_ERROR);
            }

        } catch (Exception e) {
            entity.setStatus(DownloadStatus.ERROR);
            notifyUpdate(entity, DownloadTask.NOTIFY_ERROR);
        } finally {
            if (file != null) {
                file.close();
            }
            if (is != null) {
                is.close();
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        return this;
    }

    /**
     * 读取普通的文件流
     */
    private void readNormal(InputStream is, RandomAccessFile file)
            throws IOException {
        byte[] buffer = new byte[bufferSize];
        int len;
        while (isRunning() && (len = is.read(buffer)) != -1) {
//            if (mSpeedBandUtil != null) {
//                mSpeedBandUtil.limitNextBytes(len);
//            }
            file.write(buffer, 0, len);
            if (listener != null) {
                listener.onProgress(position, len);
            }
        }
        logger.e("---"+Thread.currentThread().getId());
        if (!isRunning()) {
            if (listener != null) {
                listener.onDestroy(position);
            }
        }else {
            if (listener != null) {
                listener.onCompleted(position);
            }
        }
    }
}
