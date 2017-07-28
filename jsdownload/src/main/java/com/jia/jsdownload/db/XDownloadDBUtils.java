package com.jia.jsdownload.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Describtion: 下载工具类
 * Created by jia on 2017/7/28.
 * 人之所以能，是相信能
 */
public class XDownloadDBUtils {

    public static final String TAG = "LearnRecordUtils";

    private XDownloadOpenHelper mOpenHelper;
    private SQLiteDatabase db;

    private Context context;

    public XDownloadDBUtils(Context context) {
        this.context = context;
        mOpenHelper = new XDownloadOpenHelper(context);
        db = mOpenHelper.getWritableDatabase();
    }

    /**
     * 判断是否有下载记录
     *
     * @return
     */
    public boolean haveFile(String downloadUrl) {
        boolean flag = false;
        String sql = "select * from downloadInfo where downloadUrl=?";
        Cursor cursor = db.rawQuery(sql, new String[]{downloadUrl});
        if (cursor.moveToNext()) {
            flag = true;
        }
        return flag;
    }

    /**
     * 向数据库插入一条数据
     */
    public void addDownloadRecord(String downloadUrl) {
        String sql = "insert into downloadInfo (downloadUrl) values(?)";
        db.execSQL(sql, new Object[]{downloadUrl});
    }

    /**
     * 根据record编辑结束时间
     */
    public void setFileLength(String downloadUrl, long fileLength) {
        String sql = "update downloadInfo set fileLength=? where downloadUrl=?";
        db.execSQL(sql, new Object[]{fileLength, downloadUrl});
    }

    /**
     * 根据userId查询所有的相关的courseId
     * @return
     */
    public long getThreadCurrentPosition(String downloadUrl ,int threadId) {
        long endPosition = 0;
        switch (threadId){
            case 0:
                String sql1 = "select oneCurrentPosition from downloadInfo where downloadUrl=?";
                Cursor cursor1 = db.rawQuery(sql1, new String[]{downloadUrl});
                while (cursor1.moveToNext()) {
                    endPosition=cursor1.getLong(cursor1.getColumnIndex("oneCurrentPosition"));
                }
                break;
            case 1:
                String sql2 = "select twoCurrentPosition from downloadInfo where downloadUrl=?";
                Cursor cursor2 = db.rawQuery(sql2, new String[]{downloadUrl});
                while (cursor2.moveToNext()) {
                    endPosition=cursor2.getLong(cursor2.getColumnIndex("twoCurrentPosition"));
                }
                break;
            case 2:
                String sql3 = "select threeCurrentPosition from downloadInfo where downloadUrl=?";
                Cursor cursor3 = db.rawQuery(sql3, new String[]{downloadUrl});
                while (cursor3.moveToNext()) {
                    endPosition=cursor3.getLong(cursor3.getColumnIndex("threeCurrentPosition"));
                }
                break;
        }
        return endPosition;
    }

    /**
     * 根据userId查询所有的相关的courseId
     * @return
     */
    public void setThreadCurrentPosition(String downloadUrl ,int threadId,long currentPosition) {
        switch (threadId){
            case 0:
                String sql1 = "update downloadInfo set oneCurrentPosition=? where downloadUrl=?";
                db.execSQL(sql1, new Object[]{currentPosition, downloadUrl});
                break;
            case 1:
                String sql2 = "update downloadInfo set twoCurrentPosition=? where downloadUrl=?";
                db.execSQL(sql2, new Object[]{currentPosition, downloadUrl});
                break;
            case 2:
                String sql3 = "update downloadInfo set threeCurrentPosition=? where downloadUrl=?";
                db.execSQL(sql3, new Object[]{currentPosition, downloadUrl});
                break;
        }
    }

    /**
     * 从数据库中删除一条数据
     */
    public void deleteDownloadRecord(String downloadUrl) {
        String sql = "delete from downloadInfo where downloadUrl=?";
        db.execSQL(sql, new Object[]{downloadUrl});
    }
}
