package com.rj.soft.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;

import com.rj.soft.global.DataBox;

/**
 * Socket工具类（与数据交换系统通信）
 *
 * @author cjy
 * @version 1.6
 * @since 2019年4月19日
 */
@SuppressWarnings("deprecation")
public class SocketUtils {

    private static Logger log = Logger.getLogger(SocketUtils.class);

    /**
     * 检查服务器是否正常
     *
     * @param strUrl
     * @return
     */
    public static String checkServer(String strUrl) {
        String result = "";
        try {
            String responseBody = "";
            System.out.println("请求地址: " + strUrl);
            HttpClient client = new HttpClient();
            GetMethod method = new GetMethod(strUrl);
            method.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");
            client.getParams().setContentCharset("UTF-8");
            // 连接超时，10秒
            client.getHttpConnectionManager().getParams().setConnectionTimeout(10000);
            client.executeMethod(method);
            if (method.getStatusCode() == HttpStatus.SC_OK) {
                method.setDoAuthentication(true);
                responseBody = method.getResponseBodyAsString();
            }
            System.out.println("请求结果: " + responseBody);
            if ("SUCCESS".equals(responseBody)) {
                result = "连接成功!";
            }
        } catch (SocketTimeoutException e) {
            log.error(e);
            e.printStackTrace();
            result = "连接超时!";
        } catch (Exception e) {
            log.error(e);
            e.printStackTrace();
            result = "连接失败，错误：" + e.getMessage();
        }
        return result;
    }

    /**
     * 用户登陆校验
     *
     * @param host     socket服务器IP
     * @param port     socket服务器端口
     * @param userName 用户名
     * @param passWord 登陆密码
     * @return
     */
    public static JSONObject loginValid(String host, int port, String userName, String passWord) {
        Socket socket = null;
        InputStream in = null;
        OutputStream out = null;
        BufferedReader br = null;
        JSONObject jsonObject = null;
        try {
            socket = new Socket(host, port);
            socket.setSoTimeout(30000);
            out = socket.getOutputStream();
            out.write(new String("GET /loginValid\r\n").getBytes());
            out.write(new String("UserName: " + userName + "\r\n").getBytes());
            out.write(new String("Password: " + passWord + "\r\n").getBytes());
            out.write(new String("\r\n").getBytes());
            out.flush();

            in = socket.getInputStream();
            br = new BufferedReader(new InputStreamReader(in, "GBK"));
            StringBuffer sb = new StringBuffer();
            String line = "";
            br.readLine();
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            jsonObject = JSONObject.parseObject(sb.toString());
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            log.error(e);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e);
        } finally {
            try {
                if (null != in)
                    in.close();
                if (null != out)
                    out.close();
                if (null != br)
                    br.close();
                if (null != socket)
                    socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return jsonObject;
    }

    /**
     * 修改登陆密码
     *
     * @param host     socket服务器IP
     * @param port     socket服务器端口
     * @param userName 用户名
     * @param oldPass  旧密码
     * @param newPass  新密码
     * @return
     */
    public static JSONObject updatePass(String host, int port, String userName, String oldPass, String newPass) {
        Socket socket = null;
        InputStream in = null;
        OutputStream out = null;
        BufferedReader br = null;
        JSONObject jsonObject = null;
        try {
            socket = new Socket(host, port);
            socket.setSoTimeout(30000);
            out = socket.getOutputStream();
            out.write(new String("GET /updatePwd\r\n").getBytes());
            out.write(new String("UserName: " + userName + "\r\n").getBytes());
            out.write(new String("OldPwd: " + oldPass + "\r\n").getBytes());
            out.write(new String("NewPwd: " + newPass + "\r\n").getBytes());
            out.write(new String("\r\n").getBytes());
            in = socket.getInputStream();
            br = new BufferedReader(new InputStreamReader(in, "GBK"));
            StringBuffer sb = new StringBuffer();
            String line = "";
            br.readLine();
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            jsonObject = JSONObject.parseObject(sb.toString());
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            log.error(e);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e);
        } finally {
            try {
                if (null != in)
                    in.close();
                if (null != out)
                    out.close();
                if (null != br)
                    br.close();
                if (null != socket)
                    socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return jsonObject;
    }

    /**
     * 获取文件列表
     *
     * @param host     socket服务器IP
     * @param port     socket服务器端口
     * @param userName 用户名
     * @return
     */
    public static JSONArray listFiles(String host, int port, String userName) {
        JSONArray jsonArray = null;
        Socket socket = null;
        InputStream in = null;
        OutputStream out = null;
        BufferedReader br = null;
        try {
            socket = new Socket(host, port);
            socket.setSoTimeout(60000);
            out = socket.getOutputStream();
            out.write(new String("GET /getFileList\r\n").getBytes());
            out.write(new String("UserName: " + userName + "\r\n").getBytes());
            out.write(new String("\r\n").getBytes());
            out.flush();
            in = socket.getInputStream();
            br = new BufferedReader(new InputStreamReader(in, "GBK"));
            StringBuffer sb = new StringBuffer();
            String line = "";
            br.readLine();
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            jsonArray = JSONArray.parseArray(sb.toString());
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            log.error(e);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e);
        } finally {
            try {
                if (null != in)
                    in.close();
                if (null != out)
                    out.close();
                if (null != br)
                    br.close();
                if (null != socket)
                    socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return jsonArray;
    }

    /**
     * 批次控制
     *
     * @param dataBox 数据集合实体
     * @param command 命令
     * @param taskId  任务编号
     * @param tag     标识
     * @return
     */
    public static boolean batchControl(DataBox dataBox, String command, String taskId, boolean tag) {
        boolean result = false;
        Socket socket = null;
        InputStream in = null;
        BufferedReader br = null;
        OutputStream out = null;
        try {
            socket = new Socket(dataBox.getSocketIp(), Integer.parseInt(dataBox.getSocketPort()));
            socket.setSoTimeout(10000);
            out = socket.getOutputStream();
            out.write(new String("GET /batchControl\r\n").getBytes());
            out.write(new String("UserName: " + dataBox.getUserName() + "\r\n").getBytes());
            out.write(new String("Command: " + command + "\r\n").getBytes());
            out.write(new String("TaskId: " + taskId + "\r\n").getBytes());
            out.write(new String("\r\n").getBytes());
            in = socket.getInputStream();
            br = new BufferedReader(new InputStreamReader(in, "utf-8"));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            log.info("socket返回结果: " + sb.toString());
            result = true;
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            log.error(e);
        } catch (Exception e) {
            log.error(e);
            e.printStackTrace();
        } finally {
            try {
                if (null != in)
                    in.close();
                if (null != out)
                    out.close();
                if (null != br)
                    br.close();
                if (null != socket)
                    socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 重置状态
     *
     * @param dataBox
     * @return
     */
    public static boolean resetStatus(DataBox dataBox) {
        boolean result = false;
        Socket socket = null;
        InputStream in = null;
        BufferedReader br = null;
        OutputStream out = null;
        try {
            socket = new Socket(dataBox.getSocketIp(), Integer.parseInt(dataBox.getSocketPort()));
            socket.setSoTimeout(10000);
            out = socket.getOutputStream();
            out.write(new String("GET /resetStatus\r\n").getBytes());
            out.write(new String("UserName: " + dataBox.getUserName() + "\r\n").getBytes());
            out.write(new String("Command: DONE\r\n").getBytes());
            out.write(new String("\r\n").getBytes());
            in = socket.getInputStream();
            br = new BufferedReader(new InputStreamReader(in, "utf-8"));
            StringBuffer sb = new StringBuffer();
            String line = "";
            br.readLine();
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            result = true;
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            log.error(e);
        } catch (Exception e) {
            log.error(e);
            e.printStackTrace();
        } finally {
            try {
                if (null != in)
                    in.close();
                if (null != out)
                    out.close();
                if (null != br)
                    br.close();
                if (null != socket)
                    socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 下载文件
     *
     * @param filePath
     * @param fileName
     * @param dataBox
     * @return
     */
    public static boolean downloadFile(String filePath, String fileName, DataBox dataBox) {
        boolean result = false;
        Socket socket = null;
        InputStream in = null;
        DataInputStream dis = null;
        OutputStream out = null;
        FileOutputStream fos = null;
        String tempPath = null;
        // 文件md5值
        String md5 = null;
        long start = System.currentTimeMillis();
        try {
            socket = new Socket(dataBox.getSocketIp(), Integer.parseInt(dataBox.getSocketPort()));
            // 超时时间：10分钟
            socket.setSoTimeout(600 * 1000);
            socket.setSendBufferSize(128 * 1024);
            socket.setTcpNoDelay(true);
            out = socket.getOutputStream();
            out.write(new String("GET /downloadFile\r\n").getBytes());
            out.write(new String("UserName: " + dataBox.getUserName() + "\r\n").getBytes());
            out.write(new String("FilePath: " + filePath + "\r\n").getBytes("GBK"));
            if (dataBox.isMd5()) {
                out.write(new String("MD5Valid: true\r\n").getBytes());
            }
            out.write(new String("\r\n").getBytes());
            out.flush();
            log.info("待下载文件：" + filePath);
            // 先将文件下载到备份目录下
            tempPath = dataBox.getBackupDownloadPath();
            File temp = new File(tempPath);
            if (!temp.exists()) {
                temp.mkdir();
            }
            // 由追加内容修改为覆盖
            fos = new FileOutputStream(tempPath + "\\" + fileName);
            in = socket.getInputStream();
            dis = new DataInputStream(in);
            long length = 0;
            String contentLength = dis.readLine();
            if (null != contentLength && !"".equals(contentLength)) {
                length = Integer.parseInt(contentLength.substring(contentLength.indexOf("Content-Length: ") + 16));
            }
            log.info("待下载文件长度：" + length);
            if (dataBox.isMd5()) {
                String md5Str = dis.readLine();
                if (null != md5Str && !"".equals(md5Str)) {
                    md5 = md5Str.substring(md5Str.indexOf("MD5: ") + 5);
                }
            }
            // 写文件流
            byte[] buf = new byte[4 * 1024];
            int size = 0;
            int total = 0;
            while ((size = dis.read(buf, 0, 1024)) != -1) {
                total += size;
                fos.write(buf, 0, size);
                if (total == length) {
                    break;
                }
            }
            fos.flush();
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            log.error("下载文件超时：" + e);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            log.error("内存溢出：" + e);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("下载文件出错：" + e);
        } finally {
            try {
                if (null != out)
                    out.close();
                if (null != fos)
                    fos.close();
                if (null != in)
                    in.close();
                if (null != dis)
                    dis.close();
                if (null != socket)
                    socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (dataBox.isMd5()) {
                log.info("待下载文件md5：" + md5);
                if (null != md5) {
                    try {
                        // 验证文件md5
                        log.info("验证文件md5...");
                        String destMd5 = MD5Utility.getFileMD5String(tempPath + "\\" + fileName);
                        if (null != destMd5 && destMd5.equals(md5)) {
                            // 验证md5成功后，拷贝文件到正式目录
                            CommonUtils.copyFile(tempPath + "\\" + fileName, dataBox.getDownloadPath() + "\\" + fileName);
                            result = true;
                        } else {
                            log.info("待下载文件[" + filePath + "]md5值被篡改");
                            // 验证md5失败后，拷贝文件到md5Failed目录
                            File md5Failed = new File(tempPath + "\\md5Failed");
                            if (!md5Failed.exists()) md5Failed.mkdir();
                            CommonUtils.copyFile(tempPath + "\\" + fileName, tempPath + "\\md5Failed\\" + fileName);
                            Files.delete(Paths.get(tempPath + "\\" + fileName));
                            result = false;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        result = false;
                    }
                } else {
                    log.info("待下载文件[" + filePath + "]md5值为空");
                    result = false;
                }
            } else {
                // 下载完成后，拷贝文件到正式目录
                CommonUtils.copyFile(tempPath + "\\" + fileName, dataBox.getDownloadPath() + "\\" + fileName);
            }
        }
        long end = System.currentTimeMillis();
        log.info("下载文件：[" + filePath + "]耗时" + (end - start) + "ms");
        return result;
    }

    /**
     * 上传文件
     *
     * @param path
     * @param file
     * @param dataBox
     * @return
     */
    public static boolean uploadFile(String path, File file, DataBox dataBox) {
        boolean result = false;
        Socket socket = null;
        InputStream in = null;
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        BufferedReader br = null;
        OutputStream out = null;
        StringBuffer sb = new StringBuffer();
        long start = System.currentTimeMillis();
        long fileLength = (int) file.length();
        try {
            socket = new Socket(dataBox.getSocketIp(), Integer.parseInt(dataBox.getSocketPort()));
            // 超时时间：10分钟
            socket.setSoTimeout(600 * 1000);
            socket.setSendBufferSize(128 * 1024);
            socket.setTcpNoDelay(true);
            out = socket.getOutputStream();
            out.write(new String("POST /uploadFile\r\n").getBytes());
            out.write(new String("Content-Length: " + fileLength + "\r\n").getBytes());
            out.write(new String("UserName: " + dataBox.getUserName() + "\r\n").getBytes());
            out.write(new String("Time: " + DateUtils.getNowDate() + "\r\n").getBytes());
            out.write(new String("FilePath: " + path + "\r\n").getBytes("GBK"));
            if (dataBox.isMd5()) {
                // 读取文件MD5
                String md5 = null;
                try {
                    md5 = MD5Utility.getFileMD5String(file);
                } catch (IOException e) {
                    log.error("读取文件md5异常：" + e);
                }
                log.info("文件MD5：" + md5);
                out.write(new String("MD5: " + md5 + "\r\n").getBytes());
            }
            out.write(new String("\r\n").getBytes());

            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            byte[] buf = new byte[4 * 1024];
            int len = 0;
            int total = 0;
            while ((len = bis.read(buf)) != -1) {
                total += len;
                out.write(buf, 0, len);
                if (total == fileLength) {
                    break;
                }
            }
            out.flush();
            in = socket.getInputStream();
            br = new BufferedReader(new InputStreamReader(in, "utf-8"));
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            log.info("socket返回结果：" + sb.toString());
            if (sb.toString().contains("true")) {
                result = true;
            }
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            log.error("上传文件超时：" + e);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            log.error("内存溢出：" + e);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("上传文件出错：" + e);
        } finally {
            try {
                if (null != out)
                    out.close();
                if (null != in)
                    in.close();
                if (null != fis)
                    fis.close();
                if (null != bis)
                    bis.close();
                if (null != br)
                    br.close();
                if (null != socket)
                    socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        long end = System.currentTimeMillis();
        long costTime = (end - start);
        log.info("上传文件：[" + path + "]耗时" + costTime + "ms");
        log.info("平均上传速率：" + average(fileLength, costTime));
        return result;
    }

    /**
     * 平均速度
     *
     * @param fileLength 文件长度，字节
     * @param costTime   耗时，毫秒
     * @return
     */
    public static String average(long fileLength, long costTime) {
        String str = "";
        try {
            float fval = (float) fileLength / (1024 * 1024);
            float cval = (float) costTime / 1000;
            float val = fval / cval;
            if (val >= 1) {
                DecimalFormat df = new DecimalFormat("#.##");
                str = df.format(val) + "M/s";
            } else {
                val = val * 1024;
                DecimalFormat df = new DecimalFormat("#.##");
                str = df.format(val) + "KB/s";
            }
        } catch (Exception e) {
            e.printStackTrace();
            str = "UNKNOWN";
        }
        return str;
    }

    public static void main(String[] args) {
        File file = new File("D:\\ceshi\\test.xml");
        long fileLength = file.length();
        long costTime = 31;
        System.out.println(average(fileLength, costTime));
    }

}
