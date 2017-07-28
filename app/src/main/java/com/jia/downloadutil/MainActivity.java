package com.jia.downloadutil;

import android.content.Context;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.jia.jsdownload.DownloadListener;
import com.jia.jsdownload.XDownload;

import java.net.HttpURLConnection;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = MainActivity.this;

        XDownload download = new XDownload();
        download.download(context, "", "", new DownloadListener() {
            @Override
            public void onPreDownload(HttpURLConnection connection) {
                Log.e(TAG, "onPreDownload: 准备好");
            }

            @Override
            public void onStart() {
                Log.e(TAG, "onStart: 开始下载");
            }

            @Override
            public void onProgress(int progress) {
                Log.e(TAG, "onProgress: 下载进度" + progress);
            }

            @Override
            public void onStop(long stopLocation) {
                Log.e(TAG, "onStop: 暂停下载" + stopLocation);
            }

            @Override
            public void onResume(long resumeLocation) {
                Log.e(TAG, "onResume: 恢复下载" + resumeLocation);
            }

            @Override
            public void onComplete() {
                Log.e(TAG, "onComplete: 下载完成");
            }

            @Override
            public void onCancel() {
                Log.e(TAG, "onCancel: 取消下载");
            }

            @Override
            public void onFail(int errorCode) {
                Log.e(TAG, "onFail: 下载失败");
            }
        });
    }
}
