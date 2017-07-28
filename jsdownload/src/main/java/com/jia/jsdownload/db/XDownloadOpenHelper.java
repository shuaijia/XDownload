package com.jia.jsdownload.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Describtion:下载记录数据库openhelper
 * Created by jia on 2017/7/28.
 * 人之所以能，是相信能
 */
public class XDownloadOpenHelper extends SQLiteOpenHelper {
    // 数据库名字
    private static final String DB_NAME = "xdownload.db";

    public XDownloadOpenHelper(Context context) {
        super(context, DB_NAME, null, 1);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists downloadInfo (id int primary key, downloadUrl varchar(20), fileLength long, oneCurrentPosition long, oneEndPosition long, twoCurrentPosition long, twoEndPosition long, threeCurrentPosition long, threeEndPosition long, isComplete boolean)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
