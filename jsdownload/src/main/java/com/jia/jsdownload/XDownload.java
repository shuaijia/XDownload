package com.jia.jsdownload;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.SparseArray;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Properties;

import static android.net.sip.SipErrorCode.TIME_OUT;

/**
 * 下载工具类
 * 注意：1、恢复下载时：已下载的文件大小 = 该线程的上一次断点的位置 - 该线程起始下载位置；
 * 2、为了保证下载文件的完整性，只要记录文件不存在就需要重新进行下载；
 * Created by jia on 2017/7/23.
 */

public class XDownload {

    public static final int THREAD_NUM = 3;

    private boolean isCancel = false;
    private boolean isStop = false;

    private DownloadListener listener;

    private long mCurrentLocation = 0;

    private boolean isDownloading;

    private int mCancelNum;

    private int mStopNum;

    private int mCompleteThreadNum;

    private boolean newTask;


    public void download(final Context context, @NonNull final String downloadUrl, @NonNull final String filePath,
                         @NonNull final DownloadListener downloadListener) {
        isDownloading = true;
        mCurrentLocation = 0;
        isStop = false;
        isCancel = false;
        mCancelNum = 0;
        mStopNum = 0;
        final File dFile = new File(filePath);

        //读取已完成的线程数
        final File configFile = new File(context.getFilesDir().getPath() + "/temp/" + dFile.getName() + ".properties");

        try {
            if (!configFile.exists()) { //记录文件被删除，则重新下载
                newTask = true;
//                FileUtil.createFile(configFile.getPath());
            } else {
                newTask = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            listener.onFail();
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
                    conn.setConnectTimeout(TIME_OUT);
                    conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
                    conn.setRequestProperty("Accept", "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
                    conn.connect();

                    int len = conn.getContentLength();
                    if (len < 0) {  //网络被劫持时会出现这个问题
                        listener.onFail();
                        return;
                    }
                    int code = conn.getResponseCode();

                    if (code == 200) {
                        int fileLength = conn.getContentLength();
                        //必须建一个文件
//                        FileUtil.createFile(filePath);
                        RandomAccessFile file = new RandomAccessFile(filePath, "rwd");
                        //设置文件长度
                        file.setLength(fileLength);
                        listener.onPreDownload(conn);

                        //分配每条线程的下载区间
                        Properties pro = null;
//                        pro = Util.loadConfig(configFile);
                        int blockSize = fileLength / THREAD_NUM;
                        SparseArray tasks = new SparseArray<>();
                        for (int i = 0; i < THREAD_NUM; i++) {

                            long startL = i * blockSize, endL = (i + 1) * blockSize;
                            Object state = pro.getProperty(dFile.getName() + "_state_" + i);
                            if (state != null && Integer.parseInt(state + "") == 1) {  //该线程已经完成
                                mCurrentLocation += endL - startL;
                                mCompleteThreadNum++;
                                if (mCompleteThreadNum == THREAD_NUM) {
                                    if (configFile.exists()) {
                                        configFile.delete();
                                    }
                                    listener.onComplete();
                                    isDownloading = false;
                                    System.gc();
                                    return;
                                }
                                continue;
                            }
                            //分配下载位置
                            Object record = pro.getProperty(dFile.getName() + "_record_" + i);
                            if (!newTask && record != null && Long.parseLong(record + "") > 0) {       //如果有记录，则恢复下载
                                Long r = Long.parseLong(record + "");
                                mCurrentLocation += r - startL;
                                startL = r;
                            }
                            if (i == (THREAD_NUM - 1)) {
                                endL = fileLength;//如果整个文件的大小不为线程个数的整数倍，则最后一个线程的结束位置即为文件的总长度
                            }
                            DownloadEntity entity = new DownloadEntity(fileLength, downloadUrl, i, startL, endL, dFile, context);
                            DownloadTask task = new DownloadTask(entity);
                            tasks.put(i, new Thread(task));

                        }

                        if (mCurrentLocation > 0) {
                            listener.onResume(mCurrentLocation);
                        } else {
                            listener.onStart(mCurrentLocation);
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
                        listener.onFail();
                    }


                } catch (MalformedURLException e) {
                    isDownloading = false;
                    System.gc();
                    listener.onFail();
                } catch (ProtocolException e) {
                    isDownloading = false;
                    System.gc();
                    listener.onFail();
                } catch (IOException e) {
                    isDownloading = false;
                    System.gc();
                    listener.onFail();
                }


            }
        }).start();
    }

    public void setCancel(boolean cancel) {
        isCancel = cancel;
    }

    public void setStop(boolean stop) {
        isStop = stop;
    }

    /**
     * 下载任务类
     */
    class DownloadTask implements Runnable {

        private static final String TAG = "DownloadTask";

        private DownloadEntity dEntity;

        private String configFPath;

        public DownloadTask(DownloadEntity downloadEntity) {
            this.dEntity = downloadEntity;
            configFPath = downloadEntity.context.getFilesDir().getPath() + "/temp/" + downloadEntity.tempFile.getName() + ".properties";
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
                        /**
                         * 这里需改为百分比
                         */
                        listener.onProgress((int) mCurrentLocation);
                    }
                    currentLocation += len;
                }
                file.close();
                is.close();

                if (isCancel) {
                    synchronized (XDownload.this) {
                        mCancelNum++;
                        if (mCancelNum == THREAD_NUM) {
                            File configFile = new File(configFPath);
                            if (configFile.exists()) {
                                configFile.delete();
                            }

                            if (dEntity.tempFile.exists()) {
                                dEntity.tempFile.delete();
                            }

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
                        String location = String.valueOf(currentLocation);

                        /**
                         * 此处为断点续传机制，讲暂停时的下载位置保存
                         */
//                        writeConfig(dEntity.tempFile.getName() + "_record_" + dEntity.threadId, location);
                        if (mStopNum == THREAD_NUM) {

                            isDownloading = false;
                            listener.onStop(mCurrentLocation);
                            System.gc();
                        }
                    }
                    return;
                }

//                writeConfig(dEntity.tempFile.getName() + "_state_" + dEntity.threadId, 1 + "");
//                listener.onChildComplete(dEntity.endLocation);
                mCompleteThreadNum++;
                if (mCompleteThreadNum == THREAD_NUM) {
                    File configFile = new File(configFPath);
                    if (configFile.exists()) {
                        configFile.delete();
                    }
                    listener.onComplete();
                    isDownloading = false;
                    System.gc();
                }

            } catch (MalformedURLException e) {
                isDownloading = false;
                listener.onFail();
            } catch (IOException e) {
                isDownloading = false;
                listener.onFail();
            }

        }


    }
}
