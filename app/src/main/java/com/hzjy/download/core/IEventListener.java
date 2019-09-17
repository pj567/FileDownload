package com.hzjy.download.core;


/**
 * 基础事件
 */
public interface IEventListener {
    int PRE = 0;
    int START = 1;
    int PROGRESS = 2;
    int COMPLETE = 3;
    int STOP = 4;
    int CANCEL = 5;
    int FAIL = 6;

    /**
     * 预处理，有时有些地址链接比较慢，这时可以先在这个地方出来一些界面上的UI，如按钮的状态
     */
    void onPre(DownloadTask task, long length);

    /**
     * 开始
     */
    void onStart(DownloadTask task, long location);


    /**
     * 下载监听
     */
    void onProgress(DownloadTask task, long location);

    /**
     * 停止
     */
    void onStop(DownloadTask task, long location);

    /**
     * 下载完成
     */
    void onComplete(DownloadTask task);

    /**
     * 取消下载
     */
    void onCancel(DownloadTask task);

    /**
     * 下载失败
     *
     * @param msg 失败信息
     */
    void onFail(DownloadTask task, String msg);
}
