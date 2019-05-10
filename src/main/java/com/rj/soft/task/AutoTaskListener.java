package com.rj.soft.task;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.rj.soft.global.DB;
import com.rj.soft.global.DataBox;
import com.rj.soft.utils.CommonUtils;
import com.rj.soft.utils.IdWorker;
import com.rj.soft.utils.SocketUtils;

/**
 * 客户端自动任务监听器（定时上传/下载）
 *
 * @author cjy
 * @version 1.4
 * @since 2019年4月19日
 */
public class AutoTaskListener implements ActionListener {

    private static Logger log = Logger.getLogger(AutoTaskListener.class);

    private String uploadPath;

    private String backupUploadPath;

    private String downloadPath;

    private String backupDownloadPath;

    public AutoTaskListener(String uploadPath, String backupUploadPath, String downloadPath,
                            String backupDownloadPath) {
        this.uploadPath = uploadPath;
        this.backupUploadPath = backupUploadPath;
        this.downloadPath = downloadPath;
        this.backupDownloadPath = backupDownloadPath;
        // 程序调用时，执行标识设置为false
        DB.EXEC_FLAG = false;
    }

    public void actionPerformed(ActionEvent e) {
        // 定时任务唯一编号
        long taskId = new IdWorker().nextId();
        log.info("[" + taskId + "]自动任务作业开始执行...");
        this.downTask(taskId);
        this.upTask(taskId);
        DB.EXEC_FLAG = true;
        log.info("[" + taskId + "]自动任务作业执行结束...");
    }

    /**
     * 下载任务
     *
     * @param taskId
     */
    public void downTask(long taskId) {
        try {
            log.info("下载保存目录：" + downloadPath + "，下载备份目录：" + backupDownloadPath);
            DataBox dataBox = CommonUtils.initDataBox();
            // 获取待下载文件列表
            JSONArray jsonArray = SocketUtils.listFiles(dataBox.getSocketIp(),
                    Integer.parseInt(dataBox.getSocketPort()), dataBox.getUserName());
            log.info("待下载：" + JSON.toJSONString(jsonArray));
            if (null != jsonArray && jsonArray.size() > 0) {
                // 将JSONArray转换为List
                List<Map<String, String>> fileList = CommonUtils.convertList(jsonArray);
                for (Map<String, String> map : fileList) {
                    String fileName = map.get("fileName");
                    String filePath = map.get("filePath");
                    boolean result = SocketUtils.downloadFile(filePath, fileName, dataBox);
                    if (result) {
                        log.info("下载文件：[" + filePath + "]成功!");
                    } else {
                        log.info("下载文件：[" + filePath + "]失败!");
                    }
                }
            } else {
                log.info("待下载文件列表为空!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.info("自动下载任务异常：" + e);
        }
    }

    /**
     * 上传任务
     *
     * @param taskId
     */
    public void upTask(long taskId) {
        try {
            log.info("上传目录：" + uploadPath + "，上传备份目录：" + backupUploadPath);
            File file = new File(uploadPath);
            if (!file.exists()) {
                file.mkdir();
            }
            file = new File(backupUploadPath);
            if (!file.exists()) {
                file.mkdir();
            }

            DataBox dataBox = CommonUtils.initDataBox();
            // 获取文件列表
            List<File> fileList = CommonUtils.getFiles(uploadPath);
            log.info("待上传：" + JSON.toJSONString(CommonUtils.getList(fileList)));
            // 过滤文件，解决因空文件导致整个任务卡死BUG（与socket通信，长度为0，一直等待返回结果）
            CommonUtils.splitList(fileList);
            if (null != fileList && fileList.size() > 0) {
                if (dataBox.isOpen()) {
                    // 上传前设置当前任务为DOING
                    SocketUtils.batchControl(dataBox, "DOING", String.valueOf(taskId), true);
                }
                for (File uploadFile : fileList) {
                    if (!CommonUtils.isOccupied(uploadFile)) {
                        // 上传备份文件
                        File backupFile = new File(backupUploadPath + "\\" + uploadFile.getName());
                        // 上传文件相对路径
                        String path = "";
                        if (dataBox.getUserName().equals("100001")) {// 若为系统管理员，可不用单位名称
                            path = uploadFile.getName();
                        } else {
                            path = dataBox.getCompanyName() + "/" + uploadFile.getName();
                        }
                        boolean result = SocketUtils.uploadFile(path, uploadFile, dataBox);
                        if (result) {
                            log.info("上传文件：[" + uploadFile.getName() + "]成功!");
                            // 拷贝文件
                            CommonUtils.copyFile(uploadFile, backupFile);
                            uploadFile.delete();
                        } else {
                            log.info("上传文件：[" + uploadFile.getName() + "]失败!");
                        }
                    } else {
                        log.info("当前文件：[" + uploadFile.getAbsolutePath() + "]正在操作!");
                    }
                }
                if (dataBox.isOpen()) {
                    // 上传后设置当前任务为DONE
                    SocketUtils.batchControl(dataBox, "DONE", String.valueOf(taskId), true);
                }
            } else {
                log.info("待上传文件列表为空!");
            }
        } catch (Exception e) {
            log.info("自动上传任务异常：" + e);
        }
    }

    public String getUploadPath() {
        return uploadPath;
    }

    public void setUploadPath(String uploadPath) {
        this.uploadPath = uploadPath;
    }

    public String getBackupUploadPath() {
        return backupUploadPath;
    }

    public void setBackupUploadPath(String backupUploadPath) {
        this.backupUploadPath = backupUploadPath;
    }

    public String getDownloadPath() {
        return downloadPath;
    }

    public void setDownloadPath(String downloadPath) {
        this.downloadPath = downloadPath;
    }

    public String getBackupDownloadPath() {
        return backupDownloadPath;
    }

    public void setBackupDownloadPath(String backupDownloadPath) {
        this.backupDownloadPath = backupDownloadPath;
    }

}
