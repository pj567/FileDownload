package com.hzjy.downloads;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.hzjy.download.DataWatcher;
import com.hzjy.download.DownloadEntity;
import com.hzjy.download.manager.DownloadManager;
import com.hzjy.download.util.Logger;

/**
 * user by pj567
 * date on 2019/11/26.
 */

public class TestActivity extends AppCompatActivity implements View.OnClickListener {
    private Button startBt;
    private Button pauseBt;
    private Button cancelBt;
    private DownloadEntity entity;
    private Logger logger = new Logger(this);
    private DataWatcher watcher = new DataWatcher() {
        @Override
        public void notifyUpdate(DownloadEntity data) {
            entity = data;
            if (data.isRange()) {
                pauseBt.setEnabled(true);
            } else {
                pauseBt.setEnabled(true);
            }
            logger.e(data.getStatus() + ":" + data.getCurrentLength() + "/" + data.getTotalLength());
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        startBt = (Button) findViewById(R.id.startBt);
        pauseBt = (Button) findViewById(R.id.pauseBt);
        cancelBt = (Button) findViewById(R.id.cancelBt);
        startBt.setOnClickListener(this);
        pauseBt.setOnClickListener(this);
        cancelBt.setOnClickListener(this);
    }

    //https://c3db609eeeb35344d150370c50da5095.dd.cdntips.com/imtt.dd.qq.com/16891/apk/BA601A280323494CADAE2416B8A81AC4.apk?mkey=5ddb5f6cdb983c78&f=0af0&fsname=com.tencent.mobileqq_8.1.8_1276.apk&csr=1bbd&cip=219.152.26.141&proto=https
    @Override
    public void onClick(View v) {
        if (entity == null) {
            entity = new DownloadEntity("https://20bcd96ad202702427d824bd0564da6c.dd.cdntips.com/imtt.dd.qq.com/16891/apk/3F0C88327D1A9ED85CBBC3BC7962511B.apk?mkey=5dddd475db98395a&f=8917&fsname=com.qiyi.video_10.11.0_81400.apk&csr=1bbd&cip=219.152.31.175&proto=https");
        }
        switch (v.getId()) {
            case R.id.startBt:
                DownloadManager.download(this).load(entity).start();
                break;
            case R.id.pauseBt:
                DownloadManager.download(this).pauseAll();
                break;
            case R.id.cancelBt:
                DownloadManager.download(this).load(entity).cancel();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        DownloadManager.download(this).addObserver(watcher);
    }

    @Override
    protected void onPause() {
        super.onPause();
        DownloadManager.download(this).removeObserver(watcher);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
