package com.rj.soft.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.log4j.Logger;

import com.rj.soft.global.DB;
import com.rj.soft.global.DataBox;

/**
 * 通用工具类
 *
 * @author cjy
 * @version 1.3
 * @since 2019年4月19日
 */
public class CommonUtils {

    private static Logger log = Logger.getLogger(CommonUtils.class);

    /**
     * 初始化数据集合
     *
     * @return
     */
    public static DataBox initDataBox() {
        log.info("读取配置文件：" + DB.FILEPATH);
        DataBox box = new DataBox();
        Properties props = new Properties();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(new File(DB.FILEPATH));
            props.load(fis);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (null != fis)
                    fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        box.setSocketIp(props.getProperty("IP"));
        box.setSocketPort(props.getProperty("Port"));
        box.setOpen(Boolean.parseBoolean(props.getProperty("IS_OPEN")));
        box.setIsAuto(props.getProperty("IS_AUTO"));
        box.setMd5(Boolean.parseBoolean(props.getProperty("IS_MD5")));
        box.setCompanyName(props.getProperty("CompanyName"));
        box.setUserName(props.getProperty("UserName"));
        box.setPassWord(props.getProperty("PassWord"));
        box.setUploadPath(props.getProperty("UploadPath"));
        box.setBackupUploadPath(props.getProperty("BackupUploadPath"));
        box.setDownloadPath(props.getProperty("DownloadPath"));
        box.setBackupDownloadPath(props.getProperty("BackupDownloadPath"));
        return box;
    }

    /**
     * 将JSONArray转换为List
     *
     * @param jsonArray
     * @return
     */
    public static List<Map<String, String>> convertList(JSONArray jsonArray) {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        for (int i = 0; i < jsonArray.size(); i++) {
            Map<String, String> map = new HashMap<String, String>();
            JSONObject object = (JSONObject) jsonArray.get(i);
            String fileName = object.getString("fileName");
            String filePath = object.getString("filePath");
            filePath = filePath.replace("/", "\\");
            map.put("fileName", fileName);
            map.put("filePath", filePath);
            list.add(map);
        }
        return list;
    }

    /**
     * 拷贝文件（或目录）
     *
     * @param src
     * @param dest
     */
    public static void copyFile(File src, File dest) {
        InputStream in = null;
        OutputStream out = null;
        long st = System.currentTimeMillis();
        try {
            in = new FileInputStream(src);
            out = new FileOutputStream(dest);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e);
        } finally {
            try {
                if (null != out)
                    out.close();
                if (null != in)
                    in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        long et = System.currentTimeMillis();
        log.info("拷贝文件：[" + src.getName() + "]耗时" + (et - st) + "ms");
    }

    /**
     * 拷贝文件（或目录）
     *
     * @param srcPath
     * @param destPath
     */
    public static void copyFile(String srcPath, String destPath) {
        InputStream in = null;
        OutputStream out = null;
        long st = System.currentTimeMillis();
        try {
            in = new FileInputStream(srcPath);
            out = new FileOutputStream(destPath);
            byte[] buf = new byte[4 * 1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e);
        } finally {
            try {
                if (null != out)
                    out.close();
                if (null != in)
                    in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        long et = System.currentTimeMillis();
        log.info("拷贝文件：[" + srcPath + "]耗时" + (et - st) + "ms");
    }

    /**
     * 获取文件列表
     *
     * @param filePath
     * @return
     */
    public static List<File> getFiles(String filePath) {
        List<File> fileList = new ArrayList<File>();
        File file = new File(filePath);
        File[] files = file.listFiles();
        for (File f : files) {
            if (f.isDirectory()) {
                getFiles(f.getPath());
            } else {
                fileList.add(f);
            }
        }
        return fileList;
    }

    /**
     * 获取文件列表
     *
     * @param list
     * @return
     */
    public static List<String> getList(List<File> list) {
        List<String> fileList = new ArrayList<String>();
        if (null != list && list.size() > 0) {
            for (File file : list) {
                fileList.add(file.getName());
            }
        }
        return fileList;
    }

    /**
     * 过滤文件列表（空文件）
     *
     * @param fileList
     * @return
     */
    public static void splitList(List<File> fileList) {
        List<File> tempList = new ArrayList<File>();
        for (File file : fileList) {
            if (file.length() == 0) {
                tempList.add(file);
                log.info("文件：[" + file.getAbsolutePath() + "]为空文件，过滤!");
            } else if (file.getName().endsWith(".temp")) {
                tempList.add(file);
                log.info("文件：[" + file.getAbsolutePath() + "]为缓存文件，过滤!");
            }
        }
        fileList.removeAll(tempList);
    }

    /**
     * 判断文件是否被占用
     *
     * @param file
     * @return
     */
    public static boolean isOccupied(File file) {
        if (file.renameTo(file)) {
            return false;
        } else {
            return true;
        }
    }

}
