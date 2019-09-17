package com.hzjy.download;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;

import com.hzjy.download.core.DownloadTask;
import com.hzjy.download.core.HttpDownload;
import com.hzjy.download.core.IEventListener;
import com.hzjy.download.util.CommonUtil;
import com.hzjy.download.util.L;

public class MainActivity extends AppCompatActivity implements IEventListener {
    private long length = 0;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProgressBar = findViewById(R.id.mProgressBar);
        HttpDownload.init(this);
//        HttpDownload.download(this).setMaxNum(3);

//        HttpDownload.download(this).load("https://d3ed55267141fa896399d6caad7482d5.dd.cdntips.com/imtt.dd.qq.com/16891/apk/C2336B15561B9FD491EE60F6DD7BF048.apk?mkey=5d801ba0db983b78&f=184b&fsname=com.tencent.mm_7.0.6_1500.apk&csr=1bbd&cip=219.152.29.141&proto=https").setEventListener(this).start();
//        HttpDownload.download(this).load("https://7df17847ee20d598c859ade92f4e1190.dd.cdntips.com/imtt.dd.qq.com/16891/apk/64F5C416D1FBE6192A6C35338F7912DB.apk?mkey=5d801b86db983b78&f=07b4&fsname=com.qiyi.video_10.8.5_81360.apk&csr=1bbd&cip=219.152.29.141&proto=https").setEventListener(this).start();
//        Down down = new Down.Builder().context(this).load("cccc/aaa.apk?a=1").build();
//        down.setEventListener(this);
//        down.start();
//        Down down1 = new Down.Builder().context(this).load("cccc/bbb.apk?a=1").build();
//        down1.setEventListener(this);
//        down1.start();
//        Down down2 = new Down.Builder().context(this).load("cccc/bbb.apk?a=1").build();
//        down2.setEventListener(this);
//        down2.start();
    }

    @Override
    public void onPre(DownloadTask task, long length) {
        L.e(task.getTaskName() + "总长度" + length);
        if (task.getTaskName().contains("9FDF21EDFE927DD2FA76826CB66B4CC5.apk")) {
            this.length = length;
        }
        if (!CommonUtil.checkSDMemorySpace(task.getTaskEntity().getSavePath(), length, this)) {
            HttpDownload.download(this).allCancel();
        }
    }

    @Override
    public void onStart(DownloadTask task, long location) {
        L.e("开始下载");
    }

    @Override
    public void onProgress(DownloadTask task, long length) {
        if (task.getTaskName().contains("9FDF21EDFE927DD2FA76826CB66B4CC5.apk")) {
            mProgressBar.setProgress((int) (1.0 * length / this.length * 100));
            L.e(mProgressBar.getProgress() + "%");
        }
    }

    @Override
    public void onStop(DownloadTask task, long location) {
        L.e("停止下载");
    }

    @Override
    public void onComplete(DownloadTask task) {
        L.e(task.getTaskName());
    }

    @Override
    public void onCancel(DownloadTask task) {
        L.e("取消下载");
    }

    @Override
    public void onFail(DownloadTask task, String msg) {
        L.e(msg);
    }

    public void stop(View view) {
        HttpDownload.download(this).load("https://imtt.dd.qq.com/16891/apk/9FDF21EDFE927DD2FA76826CB66B4CC5.apk?fsname=com.tencent.mobileqq_8.1.3_1246.apk&csr=1bbd").stop();

    }

    public void start(View view) {
        HttpDownload.download(this).load("https://imtt.dd.qq.com/16891/apk/9FDF21EDFE927DD2FA76826CB66B4CC5.apk?fsname=com.tencent.mobileqq_8.1.3_1246.apk&csr=1bbd").setEventListener(this).start();
//        HttpDownload.download(this).load("https://d3ed55267141fa896399d6caad7482d5.dd.cdntips.com/imtt.dd.qq.com/16891/apk/C2336B15561B9FD491EE60F6DD7BF048.apk?mkey=5d801ba0db983b78&f=184b&fsname=com.tencent.mm_7.0.6_1500.apk&csr=1bbd&cip=219.152.29.141&proto=https").setEventListener(this).start();
//        HttpDownload.download(this).load("https://7df17847ee20d598c859ade92f4e1190.dd.cdntips.com/imtt.dd.qq.com/16891/apk/64F5C416D1FBE6192A6C35338F7912DB.apk?mkey=5d801b86db983b78&f=07b4&fsname=com.qiyi.video_10.8.5_81360.apk&csr=1bbd&cip=219.152.29.141&proto=https").setEventListener(this).start();
    }


    public void cancel(View view) {
        HttpDownload.download(this).load("https://imtt.dd.qq.com/16891/apk/9FDF21EDFE927DD2FA76826CB66B4CC5.apk?fsname=com.tencent.mobileqq_8.1.3_1246.apk&csr=1bbd").cancel();
    }

    public void allCancel(View view) {
        HttpDownload.download(this).allCancel();
    }
}
