package com.hzjy.download.core;


import com.hzjy.download.entity.DownLoadEntity;

/**
 * Created by lyy on 2017/2/13.
 */
public interface ITask {


  /**
   * 唯一标识符，DownloadTask 为下载地址
   */
  String getKey();


  /**
   * 获取信息实体
   */
  DownLoadEntity getTaskEntity();




  /**
   * 获取任务名，也就是文件名
   */
  String getTaskName();

}
