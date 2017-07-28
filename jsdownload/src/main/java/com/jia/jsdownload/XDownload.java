package com.jia.jsdownload;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.SparseArray;

import com.jia.jsdownload.config.ErrorCode;
import com.jia.jsdownload.db.XDownloadDBUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Properties;

/**
 * 下载工具类
 * 注意：1、恢复下载时：已下载的文件大小 = 该线程的上一次断点的位置 - 该线程起始下载位置；
 * 2、为了保证下载文件的完整性，只要记录文件不存在就需要重新进行下载；
 * Created by jia on 2017/7/23.
 */
public class XDownload {

    // 下载线程数
    public static final int THREAD_NUM = 3;

    // 是否取消下载
    private boolean isCancel = false;

    // 是否暂停下载
    private boolean isStop = false;

    // 下载回调
    private DownloadListener listener;

    // 当前下载位置
    private long mCurrentLocation = 0;

    // 文件总大小
    private long fileLength = 0;

    // 是否正在现在
    private boolean isDownloading;

    // 取消线程数
    private int mCancelNum;

    // 暂停线程数
    private int mStopNum;

    // 完成线程数
    private int mCompleteThreadNum;

    // 是否新开下载任务
    private boolean newTask;

    private XDownloadDBUtils dbUtils;

    private String downloadUrl;


    public void download(final Context context, @NonNull final String downloadUrl, @NonNull final String filePath,
                         @NonNull final DownloadListener downloadListener) {
        isDownloading = true;
        mCurrentLocation = 0;
        isStop = false;
        isCancel = false;
        mCancelNum = 0;
        mStopNum = 0;
        this.downloadUrl = downloadUrl;
        final File dFile = new File(filePath);

        // 创建数据库工具
        dbUtils = new XDownloadDBUtils(context);

        try {
            //记录文件被删除，则重新下载
            if (dbUtils.haveFile(downloadUrl)) {
                newTask = false;
            } else {
                dbUtils.addDownloadRecord(downloadUrl);
                newTask = true;
            }
        } catch (Exception e) {
            listener.onFail(ErrorCode.IO_ERROR);
            return;
        }

        newTask = !dFile.exists();

        new Thread(new Runnable() {
            @Override
            public void run() {
                listener = downloadListener;
                URL url = null;
                try {
                    url = new URL(downloadUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Charset", "UTF-8");
                    conn.setConnectTimeout(6000);
                    conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
                    conn.setRequestProperty("Accept", "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
                    conn.connect();

                    int len = conn.getContentLength();
                    if (len < 0) {
                        listener.onFail(ErrorCode.CONNECTION_LENGTH_IS_NULL);
                        return;
                    }
                    int code = conn.getResponseCode();

                    if (code == 200) {
                        fileLength = conn.getContentLength();

                        dbUtils.setFileLength(downloadUrl, fileLength);

                        //必须建一个文件
                        File dFile = new File(filePath);
                        RandomAccessFile file = new RandomAccessFile(filePath, "rwd");
                        //设置文件长度
                        file.setLength(fileLength);

                        listener.onPreDownload(conn);

                        //分配每条线程的下载区间
                        long blockSize = fileLength / THREAD_NUM;
                        SparseArray tasks = new SparseArray<>();

                        // 判断三个线程当前位置是否为设计的结束位置，是的话表示当前线程已经下载完
                        for (int i = 0; i < THREAD_NUM; i++) {

                            long startL = i * blockSize;
                            long endL = (i + 1) * blockSize - 1;
                            if (i == 2) {
                                endL = fileLength;
                            }

                            if (endL == dbUtils.getThreadCurrentPosition(downloadUrl, i)) {
                                mCompleteThreadNum++;
                                mCurrentLocation += endL - startL;
                                if (mCompleteThreadNum == THREAD_NUM) {
                                    listener.onComplete();
                                    // 下载完成，删除记录
                                    dbUtils.deleteDownloadRecord(downloadUrl);
                                    isDownloading = false;
                                    System.gc();
                                    return;
                                }
                                continue;
                            }


                            //分配下载位置
                            if (!newTask) {       //如果有记录，则恢复下载
                                Long r = dbUtils.getThreadCurrentPosition(downloadUrl, i);
                                mCurrentLocation += r - startL;
                                startL = r;
                            }

                            DownloadEntity entity = new DownloadEntity(fileLength, downloadUrl, i, startL, endL, dFile, context);
                            DownloadTask task = new DownloadTask(entity, i);
                            tasks.put(i, new Thread(task));

                        }

                        if (mCurrentLocation > 0) {
                            listener.onResume(mCurrentLocation);
                        } else {
                            listener.onStart();
                        }

                        // 遍历子线程任务，开始下载
                        for (int i = 0, count = tasks.size(); i < count; i++) {
                            Thread task = (Thread) tasks.get(i);
                            if (task != null) {
                                task.start();
                            }
                        }

                        // 返回码不是200，下载失败
                    } else {

                        isDownloading = false;
                        System.gc();
                        listener.onFail(ErrorCode.SERVER_ERROR);
                    }


                } catch (MalformedURLException e) {
                    isDownloading = false;
                    System.gc();
                    listener.onFail(ErrorCode.URL_ERROR);
                } catch (ProtocolException e) {
                    isDownloading = false;
                    System.gc();
                    listener.onFail(ErrorCode.PROTOCOL_ERROR);
                } catch (IOException e) {
                    isDownloading = false;
                    System.gc();
                    listener.onFail(ErrorCode.IO_ERROR);
                }


            }
        }).start();
    }

    public void cancelDownload(boolean cancel) {
        isCancel = cancel;
    }

    public void stopDownload(boolean stop) {
        isStop = stop;
    }

    /**
     * 下载任务类
     */
    class DownloadTask implements Runnable {

        private static final String TAG = "DownloadTask";

        private DownloadEntity dEntity;

        private int threadId;

        public DownloadTask(DownloadEntity downloadEntity, int threadId) {
            this.dEntity = downloadEntity;
            this.threadId = threadId;
        }

        @Override
        public void run() {

            try {
                URL url = new URL(dEntity.downloadUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                //在头里面请求下载开始位置和结束位置
                conn.setRequestProperty("Range", "bytes=" + dEntity.startLocation + "-" + dEntity.endLocation);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Charset", "UTF-8");
                conn.setConnectTimeout(5000);
                conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
                conn.setRequestProperty("Accept", "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
                conn.setReadTimeout(2000);  //设置读取流的等待时间,必须设置该参数

                InputStream is = conn.getInputStream();

                //创建可设置位置的文件
                RandomAccessFile file = new RandomAccessFile(dEntity.tempFile, "rwd");
                //设置每条线程写入文件的位置
                file.seek(dEntity.startLocation);

                byte[] buffer = new byte[1024];
                int len;
                //当前子线程的下载位置
                long currentLocation = dEntity.startLocation;
                while ((len = is.read(buffer)) != -1) {
                    if (isCancel) {
                        break;
                    }

                    if (isStop) {
                        break;
                    }

                    //把下载数据数据写入文件
                    file.write(buffer, 0, len);
                    synchronized (XDownload.this) {
                        mCurrentLocation += len;
                        // 返回下载进度
                        int percent = (int) ((mCurrentLocation / fileLength) * 100);
                        listener.onProgress(percent);
                    }
                    currentLocation += len;
                }
                file.close();
                is.close();

                if (isCancel) {
                    synchronized (XDownload.this) {
                        mCancelNum++;
                        if (mCancelNum == THREAD_NUM) {
                            dbUtils.deleteDownloadRecord(downloadUrl);

                            isDownloading = false;
                            listener.onCancel();
                            System.gc();
                        }
                    }
                    return;
                }

                //停止状态不需要删除记录文件
                if (isStop) {
                    synchronized (XDownload.this) {
                        mStopNum++;

                        /**
                         * 此处为断点续传机制，将暂停时的下载位置保存
                         */
                        dbUtils.setThreadCurrentPosition(downloadUrl, threadId,currentLocation);
                        if (mStopNum == THREAD_NUM) {

                            isDownloading = false;
                            listener.onStop(mCurrentLocation);
                            System.gc();
                        }
                    }
                    return;
                }

                mCompleteThreadNum++;
                if (mCompleteThreadNum == THREAD_NUM) {
                    listener.onComplete();
                    // 下载完成，删除记录
                    dbUtils.deleteDownloadRecord(downloadUrl);
                    isDownloading = false;
                    System.gc();
                }

            } catch (MalformedURLException e) {
                isDownloading = false;
                listener.onFail(ErrorCode.URL_ERROR);
            } catch (IOException e) {
                isDownloading = false;
                listener.onFail(ErrorCode.IO_ERROR);
            }

        }


    }
}
