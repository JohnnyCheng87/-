package com.rj.soft.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.log4j.Logger;

import com.rj.soft.global.DB;
import com.rj.soft.global.DataBox;
import com.rj.soft.ui.comp.CustomPanel;
import com.rj.soft.ui.comp.Menu;
import com.rj.soft.ui.comp.PopChoose;
import com.rj.soft.utils.CommonUtils;
import com.rj.soft.utils.DateUtils;
import com.rj.soft.utils.IdWorker;
import com.rj.soft.utils.SocketUtils;


/**
 * 客户端主操作界面
 *
 * @author cjy
 * @version 1.1
 * @since 2018年5月15日
 */
@SuppressWarnings({"serial", "unchecked", "rawtypes"})
public class Main extends JFrame implements ActionListener, ChangeListener {

    private static Logger log = Logger.getLogger(Main.class);

    private Container content;

    private JPanel panel, downloadPanel, uploadPanel, progressPanel, progressPanel1;

    /**
     * 自定义进度面板（上传/下载）
     */
    public CustomPanel customPanel1, customPanel2;

    private JButton operateButton, chooseButton;

    private JLabel totalLabel, totalNumLabel, finishedLabel, unfinishedLabel, localFolderLabel, progressLabel;

    /**
     * 文件目录选择器
     */
    private JFileChooser fileChooser;

    private JTextField localFolderField;

    /**
     * 面板
     */
    private static JTabbedPane tabbedPane;

    boolean tag = false;

    private static boolean useTag = false;

    /**
     * 当前登陆用户名
     */
    private String userName;

    private JSONArray jsonArray = null;

    private JSONArray fileArray;

    private JSONArray fileJsonArray = new JSONArray();

    private JSONArray jsonArrayRelative = new JSONArray();

    /**
     * 本地路径
     */
    private String localURL = "";

    /**
     * 选择文件路径
     */
    private static String chooseFileURL;

    /**
     * 文件列表
     */
    private List fileList = new ArrayList();

    public Main(String userName, String fileName) {
        if (null != fileName && !"".equals(fileName)) {
            if (fileName.contains("@")) {// 表示选择文件上传
                fileArray = loadChooseFiles(fileName.split("@")[0]);
                tag = true;
            }
        }
        setTitle("数据交换客户端v3.0【首页】");
        setWindowIcon();
        content = this.getContentPane();
        this.userName = userName;
        // 系统菜单
        new Menu(this);
    }

    /**
     * 按钮响应事件
     */
    public void actionPerformed(ActionEvent e) {
        // 只允许选文件夹
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        JButton button = (JButton) e.getSource();
        if (button.getText().equals("选择文件")) {// 选择文件（上传）
            dispose();
            new PopChoose("dirMenu", this, userName);
            return;
        } else if (button.getName().equals("chooseButton")) {// 选择位置（下载）
            int show = fileChooser.showOpenDialog(panel);

            File selectedFile = fileChooser.getSelectedFile();
            if (null != selectedFile && show == JFileChooser.APPROVE_OPTION) {
                localFolderField.setText(selectedFile.getPath());
                chooseFileURL = localFolderField.getText().trim();
                customPanel1.refreshChooseURL(chooseFileURL);
            }
        } else if (button.getName().equals("operateButton")) {// 下载/上传
            if (jsonArray.size() == 0) {
                showMessage("没有可操作文件!", "系统提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            System.out.println(jsonArray.size() + "********" + finishedLabel.getText() + "********");
            String text = finishedLabel.getText();
            if (text.contains("已完成: ")) {
                text = text.substring(4);
            } else {
                text = text.replace("已传送: ", "");
            }
            text = text.replace(" ", "");
            System.out.println("text = " + text);
            int finishedNum = Integer.parseInt(text);
            System.out.println("finishedNum = " + finishedNum);
            if (finishedNum != 0) {
                if (jsonArray.size() > finishedNum) {
                    showMessage("本次下载完成后再下载!", "系统提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
            if (jsonArray.size() == finishedNum) {
                progressPanel1 = null;
                dispose();
                final Main mainFrame = new Main(userName, "");
                mainFrame.buildInit();
                if (mainFrame.jsonArray.size() > 0) {
                    new Thread(new Runnable() {
                        public void run() {
                            upAndDown();
                        }
                    }).start();
                }
            }
            if (useTag) {
                if (localFolderLabel.getText().equals("默认目录")) {// 下载
                    File file = new File(localFolderField.getText());
                    if (file.exists()) {
                        customPanel1.refreshChooseURL(localFolderField.getText());
                        writeFileList(jsonArrayRelative);
                        upAndDown();
                    } else {
                        file.mkdirs();
                        if (file.exists()) {
                            customPanel1.refreshChooseURL(localFolderField.getText());
                            writeFileList(jsonArrayRelative);
                            upAndDown();
                        } else {
                            showMessage("不存在该目录!", "系统提示", JOptionPane.WARNING_MESSAGE);
                        }
                    }
                } else {// 上传
                    DataBox dataBox = CommonUtils.initDataBox();
                    if (dataBox.isOpen()) {// 开启批次控制
                        // 上传前发送重置命令
                        log.info("重置命令[" + this.userName + "]：" + SocketUtils.resetStatus(dataBox));
                        long taskId = new IdWorker().nextId();
                        DB.curTaskId = String.valueOf(taskId);
                        log.info("批次[" + taskId + "]->执行："
                                + SocketUtils.batchControl(dataBox, "DOING", String.valueOf(taskId), false));
                    }
                    writeFileList(jsonArrayRelative);
                    upAndDown();
                    // operateButton.setEnabled(false);
                }
            } else {
                showMessage("没有可操作的文件!", "系统提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
    }

    /**
     * 上传/下载面板切换
     */
    public void stateChanged(ChangeEvent e) {
        JTabbedPane tempPane = (JTabbedPane) e.getSource();
        tempPane.getSelectedComponent();
        String name = tempPane.getSelectedComponent().getName();
        if (name.equals("switchPanel")) {
            progressLabel.setText("文件上传进度");
            chooseButton.setText("选择文件");
            operateButton.setText("上传");
            totalLabel.setText("总条数: 0");
            totalNumLabel.setText("总任务: 0");
            finishedLabel.setText("已传送: 0");
            unfinishedLabel.setText("未传送: 0");
            localFolderLabel.setVisible(false);
            localFolderField.setVisible(false);
            tabbedPane.setBackgroundAt(0, new Color(200, 221, 242));
            content.remove(progressPanel);
            content.add(progressPanel1);
        } else {
            progressPanel1 = null;
            dispose();
            Main mainFrame = new Main(userName, "");
            mainFrame.buildInit();
        }
    }

    public int showConfirm(String message, String title, int optionType, int messageType) {
        JLabel label = new JLabel();
        label.setText(message);
        label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        return JOptionPane.showConfirmDialog(Main.this, label, title, optionType, messageType);
    }

    public void showMessage(String message, String title, int messageType) {
        JLabel label = new JLabel();
        label.setText(message);
        label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        JOptionPane.showMessageDialog(Main.this, label, title, messageType);
    }

    /**
     * 初始化界面
     */
    public void buildInit() {

        finishedLabel = new JLabel("已传送: 0");
        finishedLabel.setBounds(106, 495, 80, 20);
        finishedLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));

        unfinishedLabel = new JLabel("未传送: 0");
        unfinishedLabel.setBounds(186, 495, 80, 20);
        unfinishedLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));

        JTable table = createTable("table");
        table.setBorder(new LineBorder(new Color(122, 138, 153), 1));

        totalLabel.setBounds(760, 7, 100, 20);
        totalLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        totalNumLabel.setBounds(20, 495, 80, 20);
        totalNumLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));

        panel = new JPanel();

        uploadPanel = new JPanel();
        uploadPanel.setName("uploadPanel");

        downloadPanel = new JPanel();
        downloadPanel.setName("downloadPanel");
        downloadPanel.setVisible(false);
        downloadPanel.setLayout(new BorderLayout());
        downloadPanel.setVisible(true);
        downloadPanel.add(new JScrollPane(table));

        progressPanel = new JPanel();
        progressPanel.setBounds(15, 310, 817, 180);
        progressPanel.setLayout(new BorderLayout());
        progressPanel.add(new JScrollPane(customPanel1));
        if (progressPanel1 == null) {
            progressPanel1 = new JPanel();
            progressPanel1.setBounds(15, 310, 817, 180);
            progressPanel1.setLayout(new BorderLayout());
            progressPanel1.add(new JScrollPane(customPanel2));
        }

        chooseButton = new JButton();
        chooseButton.setName("chooseButton");
        chooseButton.addActionListener(this);
        chooseButton.setBounds(240, 218, 130, 31);
        chooseButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));

        localFolderField = new JTextField();

        if (tag) {
            operateButton = new JButton("上传");
            chooseButton.setText("选择文件");

            localFolderLabel = new JLabel();
            localFolderLabel.setVisible(false);
            localFolderField.setText("");
            localFolderField.setVisible(false);

            progressLabel = new JLabel("文件上传进度");
        } else {// 默认显示下载
            operateButton = new JButton("下载");
            chooseButton.setText("选择位置");

            localFolderLabel = new JLabel("默认目录");
            localFolderField.setText("C:/temp");

            progressLabel = new JLabel("文件下载进度");
        }

        operateButton.setName("operateButton");
        operateButton.addActionListener(this);
        operateButton.setBounds(440, 218, 130, 31);
        operateButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));

        localFolderLabel.setBounds(18, 258, 80, 20);
        localFolderLabel.setHorizontalAlignment(SwingConstants.CENTER);
        localFolderLabel.setSize(68, 20);
        localFolderLabel.setBorder(new LineBorder(new Color(122, 138, 153), 1));
        localFolderLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));

        localFolderField.setBounds(85, 258, 744, 21);
        localFolderField.setFont(new Font("微软雅黑", Font.PLAIN, 12));

        progressLabel.setBounds(23, 285, 80, 20);
        progressLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));

        fileChooser = new JFileChooser();
        fileChooser.setBounds(20, 20, 50, 50);
        fileChooser.setFont(new Font("微软雅黑", Font.PLAIN, 12));

        content.add(totalLabel);
        content.add(totalNumLabel);
        content.add(finishedLabel);
        content.add(unfinishedLabel);

        content.add(downloadPanel);

        content.add(chooseButton);
        content.add(operateButton);

        content.add(localFolderLabel);
        content.add(localFolderField);

        content.add(progressLabel);
        content.add(progressPanel);

        buildTablePane();
        content.add(tabbedPane);

        content.setVisible(true);
        content.setLayout(null);
        this.pack();
        this.setSize(855, 586);
        this.setVisible(true);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
    }

    /**
     * 初始化表格
     */
    public void buildTablePane() {
        JLabel label = new JLabel("");
        JPanel switchPanel = new JPanel();
        switchPanel.setName("switchPanel");
        switchPanel.add(label);
        switchPanel.setBorder(new LineBorder(new Color(122, 138, 153), 1, true));

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("上传", null, switchPanel, "上传");
        tabbedPane.addTab("下载", null, downloadPanel, "下载");
        if (tag) {
            System.out.println("上传");
            downloadPanel.setVisible(true);
            tabbedPane.setComponentAt(0, uploadPanel);
            tabbedPane.setBackgroundAt(0, new Color(200, 221, 242));
            tabbedPane.setBackgroundAt(1, new Color(238, 238, 238));

        } else {// 默认展示下载面板
            System.out.println("下载");
            tabbedPane.setSelectedIndex(1);
            tabbedPane.setBackgroundAt(0, new Color(238, 238, 238));
        }
        tabbedPane.addChangeListener(this);
        tabbedPane.setVisible(true);
        tabbedPane.setBounds(15, 5, 818, 195);
        tabbedPane.setFont(new Font("微软雅黑", Font.PLAIN, 12));
    }

    /**
     * 创建表格
     *
     * @param tableName
     * @return
     */
    public JTable createTable(String tableName) {
        final JTable table = new JTable();
        table.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        DefaultTableModel dtm = (DefaultTableModel) table.getModel();
        String[] tableHeads = new String[]{"上传用户", "文件名", "时间"};
        dtm.setColumnIdentifiers(tableHeads);

        // 动态获取数据
        loadTableData(tableName, dtm);

        TableColumnModel tcm = table.getColumnModel();
        tcm.getColumn(0).setWidth(100);
        tcm.getColumn(0).setPreferredWidth(100);
        tcm.getColumn(0).setMaxWidth(100);
        tcm.getColumn(1).setWidth(450);
        tcm.getColumn(1).setPreferredWidth(540);
        tcm.getColumn(1).setMaxWidth(540);

        if (useTag) {
            tcm.getColumn(2).setWidth(155);
            tcm.getColumn(2).setPreferredWidth(155);
            tcm.getColumn(2).setMaxWidth(155);
        } else {
            tcm.getColumn(2).setWidth(170);
            tcm.getColumn(2).setPreferredWidth(170);
            tcm.getColumn(2).setMaxWidth(170);
        }

        table.addMouseListener(new MouseListener() {

            public void mouseReleased(MouseEvent e) {

            }

            public void mousePressed(MouseEvent e) {
                System.out.println("行数: " + table.rowAtPoint(e.getPoint()));
            }

            public void mouseExited(MouseEvent e) {

            }

            public void mouseEntered(MouseEvent e) {

            }

            public void mouseClicked(MouseEvent e) {

            }
        });

        return table;
    }

    /**
     * 加载表格数据（动态获取）
     *
     * @param tableName
     * @param dtm
     */
    public void loadTableData(String tableName, DefaultTableModel dtm) {
        try {
            System.out.println(tag);
            if (!tag) {// 加载下载数据
                // 动态获取前置空
                jsonArray = null;
                jsonArray = SocketUtils.listFiles(DB.SERVER_IP, Integer.parseInt(DB.SERVER_PORT), userName);
            } else {// 加载上传数据
                jsonArray = fileArray;
            }
            customPanel1 = new CustomPanel(jsonArray, finishedLabel, unfinishedLabel, localURL, userName);
            customPanel2 = new CustomPanel(jsonArray, finishedLabel, unfinishedLabel, localURL, userName);

            unfinishedLabel.setText("未传送: " + jsonArray.size());

            // 初始化
            for (int i = 0; i < jsonArray.size(); i++) {
                customPanel1.processMethod(i);
            }

            totalLabel = new JLabel("总条数: " + jsonArray.size());
            totalNumLabel = new JLabel("总任务: " + jsonArray.size());
            DB.TOTALTNUM = jsonArray.size();
            if (jsonArray.size() == 0) {
                useTag = false;
            } else {
                useTag = true;
            }
            System.out.println("表格列数：" + dtm.getRowCount());
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (tableName.equals("table")) {// 待传输表格
                    System.out.println("文件名称：" + jsonObject.getString("fileName"));
                    if (tag) {// 上传
                        dtm.addRow(new Object[]{userName, jsonObject.getString("fileName"), DateUtils.getNowDate()});
                    } else {// 下载
                        dtm.addRow(new Object[]{jsonObject.getString("userName"), jsonObject.getString("fileName"),
                                jsonObject.getString("time")});
                    }
                } else {// 进度表格
                    dtm.addRow(new Object[]{jsonObject.getString("fileName"), ""});
                }
            }
        } catch (Exception e) {
            log.error(e);
            e.printStackTrace();
        }
    }

    /**
     * 上传和下载任务
     */
    public void upAndDown() {
        new Thread(new Runnable() {
            public void run() {
                for (int i = 0; i < 10; i++) {
                    final int index = i;
                    new Thread(new Runnable() {
                        public void run() {
                            if (index < customPanel1.workList.size()) {
                                workerThread(index);
                            }
                        }
                    }).start();
                    System.out.println("当前Thread-" + i);
                }
            }
        }).start();
    }

    /**
     * 任务线程
     *
     * @param index
     */
    public void workerThread(int index) {
        SwingWorker<Integer, Integer> worker = (SwingWorker<Integer, Integer>) (customPanel1.workList.get(index));
        worker.execute();
    }

    /**
     * 切换至上传面板时,通过弹出窗口选择上传目录,加载文件列表
     *
     * @param path
     * @return
     */
    public JSONArray loadChooseFiles(String path) {
        localURL = path;
        DB.DEFAULT_FILE_PATH = path;
        File file = new File(path + "\\FILELIST.txt");
        if (file.exists()) {
            System.out.println("存在");
            // 若存在FILELIST.txt,根据FILELIST.txt读取文件夹的中文件列表
            File dir = new File(path);
            File[] files = dir.listFiles();
            FileInputStream fis = null;
            ByteArrayOutputStream baos = null;
            StringBuffer sb = new StringBuffer();
            try {
                fis = new FileInputStream(file);
                byte[] buf = new byte[1024];
                baos = new ByteArrayOutputStream();
                int size = 0;
                while ((size = fis.read(buf, 0, 1024)) != -1) {
                    baos.write(buf, 0, size);
                    sb.append(new String(buf, 0, size));
                }
            } catch (Exception e) {
                log.error(e);
                e.printStackTrace();
            } finally {
                try {
                    if (null != fis)
                        fis.close();
                    if (null != baos)
                        baos.close();
                } catch (Exception e) {
                    log.error(e);
                    e.printStackTrace();
                }
            }
            JSONArray array = new JSONArray();
            for (int i = 0; i < files.length; i++) {
                String[] ss = sb.toString().split("]");
                for (int k = 0; k < ss.length; k++) {
                    String temp = ss[k].substring(1, ss[k].toString().length());
                    JSONObject jsonObject = JSONObject.parseObject(temp);
                    // 本地目录的文件名需与FILELIST的文件名一致
                    if (files[i].getName().equals(jsonObject.get("fileName"))) {
                        // 相对路径JSONArray
                        jsonArrayRelative.add(jsonObject);

                        // 绝对路径JSONArray
                        JSONObject jsonObject2 = jsonObject;
                        jsonObject2.put("filePath", files[i].getAbsolutePath().toString());
                        System.out.println(jsonObject2);
                        array.add(jsonObject2);

                        break;
                    }
                }
            }
            System.out.println("array = " + array.toString());
            return array;
        } else {
            System.out.println("不存在");
            // 若不存在FILELIST.txt,从文件夹中读取文件列表
            readFileList(path);
            return fileJsonArray;
        }
    }

    /**
     * 将获取的文件列表写入FILELIST.txt中
     *
     * @param jsonArray
     */
    public void writeFileList(JSONArray jsonArray) {
        if (jsonArrayRelative.size() == 0) {
            jsonArray = this.jsonArray;
        }
        if (null != jsonArray) {
            File file = null;
            FileOutputStream fos = null;
            int index = 0;
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (null != jsonObject.get("filePath") && "" != jsonObject.get("filePath").toString()) {
                    if (index == 0) {
                        // 获取相对路径，形如：福建省旅游局/test.xml
                        String relativePath = jsonObject.get("filePath").toString();
                        String fileName = localFolderField.getText() + "/" + relativePath;
                        File dir = new File(fileName);
                        dir.mkdirs();
                        dir.delete();
                        if (relativePath.lastIndexOf("/") != -1) {
                            String path = localFolderField.getText() + "/"
                                    + relativePath.substring(0, relativePath.lastIndexOf("/")) + "/FILELIST.txt";
                            file = new File(path);
                            if (file.exists()) {
                                file.delete();
                            }
                        }
                        break;
                    }
                    index++;
                }
            }
            if (null != file) {
                try {
                    fos = new FileOutputStream(file, true);
                    fos.write(jsonArray.toString().getBytes());
                    fos.flush();
                } catch (Exception e) {
                    log.error(e);
                    e.printStackTrace();
                }
            }
            if (null != fos) {
                try {
                    fos.close();
                } catch (IOException e) {
                    log.error(e);
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 根据文件路径读取文件列表（转JSONArray）
     *
     * @param path
     */
    public void readFileList(String path) {
        searchFile(path);
        // 文件夹名称
        String folder = "";
        for (int i = 0; i < fileList.size(); i++) {
            if (fileList.get(i).toString().indexOf(".") != -1) {
                String temp = fileList.get(i).toString();
                temp = temp.substring(0, temp.lastIndexOf("\\"));
                folder = temp.substring(temp.lastIndexOf("\\") + 1);
                break;
            }
        }
        System.out.println("文件夹：" + folder);
        for (int i = 0; i < fileList.size(); i++) {
            if (fileList.get(i).toString().indexOf(".") != -1) {
                if (fileList.get(i).toString().contains("FILELIST.txt")) {
                    continue;
                }
                String fileName = fileList.get(i).toString().substring(fileList.get(i).toString().lastIndexOf("\\"));
                if (fileName.indexOf("\\") != 1) {
                    fileName = fileName.replace("\\", "");
                }
                String filePath = folder + "/" + fileName;
                JSONObject jsonObject = JSONObject
                        .parseObject("{'fileName':'" + fileName + "','filePath':'" + filePath + "'}");
                fileJsonArray.add(jsonObject);
            }
        }
        System.out.println("fileJsonArray == " + fileJsonArray.toString());
    }

    /**
     * 根据文件名检索文件
     *
     * @param fileName
     */
    public void searchFile(String fileName) {
        File file = new File(fileName);
        File[] files = file.listFiles();
        if (null != files && files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                recursionFile(files[i]);
            }
        }
    }

    /**
     * 递归遍历文件
     *
     * @param file
     */
    public void recursionFile(File file) {
        File[] files = file.listFiles();
        if (null != files && files.length > 0) {
            for (File f : files) {
                if (f.isDirectory()) {
                    recursionFile(f);
                } else {
                    fileList.add(f.getAbsoluteFile());
                }
            }
        }
        fileList.add(file.getAbsoluteFile());
    }

    /**
     * 最小化窗口
     */
    public void miniSized() {
        this.setExtendedState(JFrame.ICONIFIED | getExtendedState());
    }

    /**
     * 最大化窗口
     */
    public void maxSized() {
        this.setExtendedState(JFrame.NORMAL);
    }

    /**
     * 设置窗口图标
     */
    public void setWindowIcon() {
        ImageIcon icon = new ImageIcon(getClass().getResource("/icon.png"));
        this.setIconImage(icon.getImage());
    }

}
