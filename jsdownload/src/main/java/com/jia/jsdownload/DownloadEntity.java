package com.jia.jsdownload;

import android.content.Context;

import java.io.File;

/**
 * 下载实体类
 * Created by jia on 2017/7/22.
 */

public class DownloadEntity {

    // 文件大小
    long fileSize;
    // 下载地址
    String downloadUrl;
    // 线程id
    int threadId;
    // 开始位置
    long startLocation;
    // 结束位置
    long endLocation;
    // 下载文件
    File tempFile;
    // 上下文
    Context context;

    public DownloadEntity(long fileSize, String downloadUrl, int threadId, long startLocation, long endLocation, File tempFile, Context context) {
        this.fileSize = fileSize;
        this.downloadUrl = downloadUrl;
        this.threadId = threadId;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.tempFile = tempFile;
        this.context = context;
    }
}
