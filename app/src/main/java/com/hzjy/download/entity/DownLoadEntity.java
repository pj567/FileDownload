package com.hzjy.download.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * pj567
 * 2019/9/16
 */

public class DownLoadEntity implements Parcelable{
    private String url;
    private String fileName;
    private String savePath;

    public DownLoadEntity() {
    }

    public DownLoadEntity(String url, String fileName, String savePath) {
        this.url = url;
        this.fileName = fileName;
        this.savePath = savePath;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    protected DownLoadEntity(Parcel in) {
        url = in.readString();
        fileName = in.readString();
        savePath = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeString(fileName);
        dest.writeString(savePath);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DownLoadEntity> CREATOR = new Creator<DownLoadEntity>() {
        @Override
        public DownLoadEntity createFromParcel(Parcel in) {
            return new DownLoadEntity(in);
        }

        @Override
        public DownLoadEntity[] newArray(int size) {
            return new DownLoadEntity[size];
        }
    };

    @Override
    public String toString() {
        return "DownLoadEntity{" +
                "url='" + url + '\'' +
                ", fileName='" + fileName + '\'' +
                ", savePath='" + savePath + '\'' +
                '}';
    }
}
