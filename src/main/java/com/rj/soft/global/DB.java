package com.rj.soft.global;

import com.alibaba.fastjson.JSONArray;

/**
 * 全局变量
 *
 * @author cjy
 * @version 1.1
 * @since 2019年4月19日
 */
public class DB {

    /**
     * 服务器IP
     */
    public static String SERVER_IP;

    /**
     * 服务器端口
     */
    public static String SERVER_PORT;

    /**
     * 是否开启批次控制
     */
    public static String IS_OPEN;

    /**
     * 是否开启自动模式（自动上传/下载任务）
     */
    public static String IS_AUTO;

    /**
     * 是否开启MD5校验
     */
    public static String IS_MD5;

    /**
     * 接入单位名称
     */
    public static String COMPANY_NAME;

    /**
     * 数据交换账号（根据接入单位分配）
     */
    public static String USERNAME;

    /**
     * 账号登陆密码
     */
    public static String PASSWORD;

    /**
     * 自动上传目录
     */
    public static String UPLOAD_PATH;

    /**
     * 备份自动上传目录
     */
    public static String BACKUP_UPLOAD_PATH;

    /**
     * 自动下载目录
     */
    public static String DOWNLOAD_PATH;

    /**
     * 备份自动下载目录
     */
    public static String BACKUP_DOWNLOAD_PATH;

    /**
     * 默认文件路径（下载）
     */
    public static String DEFAULT_FILE_PATH;

    public static String FILEPATH;

    public static String PATH;

    public static JSONArray FILEJSONARRAY;

    public static int THREADNUM = 0;

    public static int EXENUM = 0;

    public static int TOTALTNUM = 0;

    /**
     * 定时任务执行标识: true为空闲，false为非空闲
     */
    public static boolean EXEC_FLAG = true;

    /**
     * 当前任务编号
     */
    public static String curTaskId = "";

}
