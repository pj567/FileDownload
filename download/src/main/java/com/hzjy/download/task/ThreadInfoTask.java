package com.hzjy.download.task;

import android.net.TrafficStats;
import android.os.Process;

import com.hzjy.download.util.Constants;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

/**
 * user by pj567
 * date on 2019/11/27.
 */

public class ThreadInfoTask implements Runnable {
    private String url;
    private ConnectListener listener;
    private volatile boolean isRunning;

    public ThreadInfoTask(String url, ConnectListener listener) {
        this.url = url;
        this.listener = listener;
    }

    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        TrafficStats.setThreadStatsTag(UUID.randomUUID().toString().hashCode());
        isRunning = true;
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Range", "bytes=0-" + Integer.MAX_VALUE);
            connection.setConnectTimeout(Constants.CONNECT_TIME);
            connection.setReadTimeout(Constants.READ_TIME);
            int responseCode = connection.getResponseCode();
            int contentLength = connection.getContentLength();
            boolean isSupportRange = false;
            if (responseCode == HttpURLConnection.HTTP_PARTIAL) {
                isSupportRange = true;
            }
            if (listener != null) {
                listener.onConnect(isSupportRange, contentLength);
            }
            isRunning = false;
        } catch (IOException e) {
            isRunning = false;
            if (listener != null) {
                listener.onInfoError(e.getMessage());
            }
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void cancel() {
        Thread.currentThread().interrupt();
    }

    interface ConnectListener {
        void onConnect(boolean isSupportRange, int totalLength);

        void onInfoError(String message);
    }
}
