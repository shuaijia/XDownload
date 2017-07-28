package com.jia.jsdownload.config;

/**
 * Describtion: 错误码常量类
 * Created by jia on 2017/7/28.
 * 人之所以能，是相信能
 */
public class ErrorCode {
    /**
     * 请求连接返回文件大小小于0
     */
    public static final int CONNECTION_LENGTH_IS_NULL=1;

    /**
     * url错误
     */
    public static final int URL_ERROR=2;

    /**
     * IO异常
     */
    public static final int IO_ERROR=3;

    /**
     * 服务器错误
     */
    public static final int SERVER_ERROR=4;

    /**
     * 传输协议异常
     */
    public static final int PROTOCOL_ERROR=5;
}
