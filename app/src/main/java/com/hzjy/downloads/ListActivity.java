package com.hzjy.downloads;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.hzjy.download.DataWatcher;
import com.hzjy.download.DownloadStatus;
import com.hzjy.download.DownloadEntity;
import com.hzjy.download.manager.DownloadManager;
import com.hzjy.download.util.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * pj567
 * 2019/11/22
 */

public class ListActivity extends AppCompatActivity {
    private DownloadManager mDownloadManager;
    private Logger logger = new Logger(this);
    private DataWatcher watcher = new DataWatcher() {
        @Override
        public void notifyUpdate(DownloadEntity data) {
//            logger.e(data.getStatus() + ";" + data.getCurrentLength() + "/" + data.getTotalLength());
            int index = entryList.indexOf(data);
            if (index != -1) {
                entryList.remove(index);
                entryList.add(index, data);
                adapter.notifyDataSetChanged();
            }
//            logger.e(data.toString());
        }
    };
    private List<DownloadEntity> entryList = new ArrayList<>();
    private ListView listView;
    private Button statusAll;
    private DownloadAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        mDownloadManager = DownloadManager.download(this);
        entryList.add(new DownloadEntity("https://20bcd96ad202702427d824bd0564da6c.dd.cdntips.com/imtt.dd.qq.com/16891/apk/3F0C88327D1A9ED85CBBC3BC7962511B.apk?mkey=5dddd475db98395a&f=8917&fsname=com.qiyi.video_10.11.0_81400.apk&csr=1bbd&cip=219.152.31.175&proto=https"));
        entryList.add(new DownloadEntity("https://c3db609eeeb35344d150370c50da5095.dd.cdntips.com/imtt.dd.qq.com/16891/apk/BA601A280323494CADAE2416B8A81AC4.apk?mkey=5ddb5f6cdb983c78&f=0af0&fsname=com.tencent.mobileqq_8.1.8_1276.apk&csr=1bbd&cip=219.152.26.141&proto=https"));
        entryList.add(new DownloadEntity("https://01bc74cfa0050d76846b4fd7fe26ecd2.dd.cdntips.com/imtt.dd.qq.com/16891/apk/19CE288600586506EF9DCE3564DC19FA.apk?mkey=5dde3474db98395a&f=1026&fsname=com.tencent.mm_7.0.9_1540.apk&csr=1bbd&cip=219.152.31.175&proto=https"));
        entryList.add(new DownloadEntity("https://c3db609eeeb35344d150370c50da5095.dd.cdntips.com/imtt.dd.qq.com/16891/apk/8FD3ED686BA547C47BAAA5129407B0D3.apk?mkey=5dde3430db98395a&f=8935&fsname=com.tencent.qqmusic_9.6.5.6_1175.apk&csr=1bbd&cip=219.152.31.175&proto=https"));
        entryList.add(new DownloadEntity("https://fd5d336228ed23142cf05495cd976dc8.dd.cdntips.com/imtt.dd.qq.com/16891/apk/BF9109B1AD25858089CAA8935B99E223.apk?mkey=5dde3402db98395a&f=0c27&fsname=com.smile.gifmaker_6.10.1.11563_11563.apk&csr=1bbd&cip=219.152.31.175&proto=https"));
        DownloadEntity entry = null;
        DownloadEntity realEntry = null;
        for (int i = 0; i < entryList.size(); i++) {
            entry = entryList.get(i);
            realEntry = mDownloadManager.queryDownloadEntryById(entry.getId());
            if (realEntry != null) {
                entryList.remove(i);
                entryList.add(i, realEntry);
            }
        }
        statusAll = (Button) findViewById(R.id.statusAll);
        listView = (ListView) findViewById(R.id.listView);
        adapter = new DownloadAdapter();
        listView.setAdapter(adapter);
        statusAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (statusAll.getText().toString().equals("全部开始")) {
                    mDownloadManager.recoverAll();
                    statusAll.setText("全部暂停");
                } else {
                    mDownloadManager.pauseAll();
                    statusAll.setText("全部开始");
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDownloadManager.addObserver(watcher);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDownloadManager.removeObserver(watcher);
    }

    public class DownloadAdapter extends BaseAdapter {

        public DownloadAdapter() {
        }

        @Override
        public int getCount() {
            return entryList.size();
        }

        @Override
        public Object getItem(int position) {
            return entryList != null ? entryList.get(position) : null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false);
                holder = new ViewHolder();
                holder.statusTv = (TextView) convertView.findViewById(R.id.statusTv);
                holder.statusBt = (Button) convertView.findViewById(R.id.statusBt);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final DownloadEntity entry = entryList.get(position);
            if (entry.getStatus() == DownloadStatus.COMPLETED) {
                holder.statusBt.setText("完成");
            }
            holder.statusTv.setText(entry.getFileName() + " is " + entry.getStatus() + "进度:" + entry.getCurrentLength() + "/" + entry.getTotalLength());
            holder.statusBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (entry.getStatus() == DownloadStatus.IDLE || entry.getStatus() == DownloadStatus.CANCEL || entry.getStatus() == DownloadStatus.PAUSE) {
                        mDownloadManager.load(entry).start();
                        holder.statusBt.setText("暂停");
                    } else if (entry.getStatus() == DownloadStatus.UPDATE || entry.getStatus() == DownloadStatus.WAIT) {
                        mDownloadManager.load(entry).pause();
                        holder.statusBt.setText("继续");
                    }
                }
            });
            return convertView;
        }

        private class ViewHolder {
            TextView statusTv;
            Button statusBt;
        }
    }

}
