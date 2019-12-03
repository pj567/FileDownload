package com.hzjy.download;

import com.hzjy.download.util.CommonUtil;
import com.hzjy.download.util.Constants;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.HashMap;

/**
 * pj567
 * 2019/11/25
 */
@DatabaseTable(tableName = "download_entity")
public class DownloadEntity implements Serializable{
    @DatabaseField(columnName = "id", id = true)
    private String id;
    @DatabaseField
    private String url;
    @DatabaseField
    private String filePath;
    @DatabaseField
    private String fileName;
    @DatabaseField
    private long currentLength;
    @DatabaseField
    private long totalLength;
    @DatabaseField
    private boolean range;
    @DatabaseField(dataType = DataType.SERIALIZABLE)
    public HashMap<Integer, Long> rangeMap;
    @DatabaseField
    private int percent;
    @DatabaseField(dataType = DataType.ENUM_TO_STRING)
    private DownloadStatus status = DownloadStatus.IDLE;

    public DownloadEntity() {

    }

    public DownloadEntity(String url) {
        this.url = url;
        if (!CommonUtil.isEmpty(url)) {
            setId(CommonUtil.getStrMd5(url));
            String fileName = url.substring(url.lastIndexOf("/") + 1, url.length());
            if (fileName.contains("?")) {
                fileName = fileName.substring(0, fileName.indexOf("?"));
            }
            setFileName(fileName);
            setFilePath(Constants.FILE_PATH);
        }
    }

    public DownloadEntity(String id, String url, String filePath, String fileName) {
        this.id = id;
        this.url = url;
        this.filePath = filePath;
        this.fileName = fileName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getCurrentLength() {
        return currentLength;
    }

    public void setCurrentLength(long currentLength) {
        this.currentLength = currentLength;
    }

    public long getTotalLength() {
        return totalLength;
    }

    public void setTotalLength(long totalLength) {
        this.totalLength = totalLength;
    }

    public boolean isRange() {
        return range;
    }

    public void setRange(boolean range) {
        this.range = range;
    }

    public int getPercent() {
        return percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }

    public DownloadStatus getStatus() {
        return status;
    }

    public void setStatus(DownloadStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "DownloadEntity{" +
                "id='" + id + '\'' +
                ", url='" + url + '\'' +
                ", filePath='" + filePath + '\'' +
                ", fileName='" + fileName + '\'' +
                ", currentLength=" + currentLength +
                ", totalLength=" + totalLength +
                ", range=" + range +
                ", rangeMap=" + rangeMap +
                ", percent=" + percent +
                ", status=" + status +
                '}';
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj.hashCode() == this.hashCode();
    }

    public void reset() {
        this.currentLength = 0;
        this.rangeMap = null;
        this.percent = 0;
    }
}
