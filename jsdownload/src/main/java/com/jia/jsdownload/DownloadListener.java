package com.jia.jsdownload;

import java.net.HttpURLConnection;

/**
 * 下载回调
 * Created by jia on 2017/7/22.
 */
public interface DownloadListener {

    /**
     * 取消
     */
    public void onCancel();

    /**
     * 下载失败
     */
    public void onFail();

    /**
     * 准备好下载
     * @param connection
     */
    public void onPreDownload(HttpURLConnection connection);

    /**
     * 下载进度
     * @param progress
     */
    public void onProgress(int progress);

    /**
     * 开始下载
     */
    public void onStart(long startLocation);

    /**
     * 恢复下载
     */
    public void onResume(long resumeLocation);

    /**
     * 停止下载
     */
    public void onStop(long stopLocation);

    /**
     * 完成下载
     */
    public void onComplete();

}
