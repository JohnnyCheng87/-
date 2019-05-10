package com.rj.soft.global;

/**
 * 全局数据集合
 *
 * @author cjy
 * @version 1.1
 * @since 2019年4月19日
 */
public class DataBox {

    private String socketIp;

    private String socketPort;

    private boolean isOpen;

    private String companyName;

    private String userName;

    private String passWord;

    private String isAuto;

    private String uploadPath;

    private String backupUploadPath;

    private String downloadPath;

    private String backupDownloadPath;

    private boolean isMd5;

    public String getSocketIp() {
        return socketIp;
    }

    public void setSocketIp(String socketIp) {
        this.socketIp = socketIp;
    }

    public String getSocketPort() {
        return socketPort;
    }

    public void setSocketPort(String socketPort) {
        this.socketPort = socketPort;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public String getIsAuto() {
        return isAuto;
    }

    public void setIsAuto(String isAuto) {
        this.isAuto = isAuto;
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

    public boolean isMd5() {
        return isMd5;
    }

    public void setMd5(boolean md5) {
        isMd5 = md5;
    }

}
