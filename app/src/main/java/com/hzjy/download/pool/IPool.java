package com.hzjy.download.pool;


import com.hzjy.download.core.DownloadTask;

/**
 * 任务池
 */
interface IPool {
    /**
     * 将下载任务添加到任务池中
     */
    boolean putTask(DownloadTask task);

    /**
     * 按照队列原则取出下载任务
     *
     * @return 返回null或者下载任务
     */
    DownloadTask pollTask();

    /**
     * 通过key获取任务，当任务不为空时，队列将删除该下载任务
     *
     * @return 返回null或者下载任务
     */
    DownloadTask getTask(String key);

    /**
     * 任务是否存在
     *
     * @return {@code true} 任务存在
     */
    boolean taskExits(String key);

    /**
     * 删除任务池中的下载任务
     *
     * @param task {@link DownloadTask}
     * @return true:移除成功
     */
    boolean removeTask(DownloadTask task);

    /**
     * 通过key除下载任务
     *
     * @param key 下载链接
     * @return true:移除成功
     */
    boolean removeTask(String key);

    /**
     * 池子大小
     *
     * @return 返回缓存池或者执行池大小
     */
    int size();
}