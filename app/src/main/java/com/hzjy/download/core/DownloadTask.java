package com.hzjy.download.core;

import android.os.Message;
import android.text.TextUtils;

import com.hzjy.download.entity.DownLoadEntity;
import com.hzjy.download.util.BufferedRandomAccessFile;
import com.hzjy.download.util.CommonUtil;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

/**
 * pj567
 * 2019/9/16
 */

public class DownloadTask extends AbsTask implements ITask {
    private IEventListener eventListener;
    private OnInternalListener onInternalListener;
    private int bufferSize = 8192;

    public DownloadTask(DownLoadEntity entity) {
        super(entity);
    }

    public void setEventListener(IEventListener eventListener) {
        this.eventListener = eventListener;
    }

    public void setOnInternalListener(OnInternalListener onInternalListener) {
        this.onInternalListener = onInternalListener;
    }

    public IEventListener getEventListener() {
        return eventListener;
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case IEventListener.PRE:
                if (eventListener != null) {
                    eventListener.onPre(this, (Long) msg.obj);
                }
                break;
            case IEventListener.START:
                if (eventListener != null) {
                    eventListener.onStart(this, 0);
                }
                break;
            case IEventListener.STOP:
                if (eventListener != null) {
                    eventListener.onStop(this, (Long) msg.obj);
                }
                if (onInternalListener != null) {
                    onInternalListener.onInternal(this, OnInternalListener.STOP);
                }
                break;
//            case IEventListener.CANCEL:
//                if (eventListener != null) {
//                    eventListener.onCancel(this);
//                }
//                break;
            case IEventListener.PROGRESS:
                if (eventListener != null) {
                    eventListener.onProgress(this, (Long) msg.obj);
                }
                break;
            case IEventListener.COMPLETE:
                if (eventListener != null) {
                    eventListener.onComplete(this);
                }
                if (onInternalListener != null) {
                    onInternalListener.onInternal(this, OnInternalListener.COMPLETE);
                }
                break;
            case IEventListener.FAIL:
                if (eventListener != null) {
                    eventListener.onFail(this, (String) msg.obj);
                }
                break;
        }
        return false;
    }

    @Override
    public String getKey() {
        return entity != null && !CommonUtil.isEmpty(entity.getUrl()) ? entity.getUrl() : "";
    }

    @Override
    public DownLoadEntity getTaskEntity() {
        return entity;
    }


    @Override
    public String getTaskName() {
        return entity != null && !CommonUtil.isEmpty(entity.getFileName()) ? entity.getFileName() : "";
    }

    @Override
    public DownloadTask call() throws Exception {
        super.call();

        //执行下载
        if (TextUtils.isEmpty(entity.getUrl())) {
            fail(IEventListener.FAIL, "url is null");
            return this;
        }
        HttpURLConnection conn = null;
        BufferedInputStream is = null;
        BufferedRandomAccessFile file = null;
        try {
            URL url = new URL(entity.getUrl());
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(20 * 1000);
            conn.connect();
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                int contentLength = conn.getContentLength();
                send(IEventListener.PRE, contentLength);
                is = new BufferedInputStream(convertInputStream(conn));
                //创建可设置位置的文件
                file = new BufferedRandomAccessFile(entity.getSavePath() + entity.getFileName(), "rwd", bufferSize);
                //设置每条线程写入文件的位置
                long startLength = file.length();
                if (contentLength > startLength) {
                    file.seek(startLength);
                    send(IEventListener.START, startLength);
                    readNormal(is, file, startLength);
                } else if (contentLength == startLength) {
                    send(IEventListener.COMPLETE, startLength);
                } else {
                    fail(IEventListener.FAIL, "文件大小异常");
                }
            } else {
                fail(IEventListener.FAIL, "连接失败");
            }

        } catch (Exception e) {
            fail(IEventListener.FAIL, e.toString());
            e.printStackTrace();
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
    private void readNormal(InputStream is, BufferedRandomAccessFile file, long startLength)
            throws IOException {
        byte[] buffer = new byte[bufferSize];
        long curr = startLength;
        int len;
        while (isLive() && (len = is.read(buffer)) != -1) {
//            if (mSpeedBandUtil != null) {
//                mSpeedBandUtil.limitNextBytes(len);
//            }
            file.write(buffer, 0, len);
            curr += len;
            send(IEventListener.PROGRESS, curr);
        }
        if (!isLive()) {
            send(IEventListener.STOP, curr);
        } else {
            send(IEventListener.COMPLETE, curr);
        }
    }

    private void send(int what, long length) {
        Message message = mHandler.obtainMessage();
        message.what = what;
        if (length != -1) {
            message.obj = length;
        }
        mHandler.sendMessage(message);
    }

    private void fail(int what, String msg) {
        Message message = mHandler.obtainMessage();
        message.what = what;
        message.obj = msg;
        mHandler.sendMessage(message);
    }

    private InputStream convertInputStream(HttpURLConnection connection) throws IOException {
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
}
