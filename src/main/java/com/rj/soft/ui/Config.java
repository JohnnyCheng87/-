package com.rj.soft.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import com.rj.soft.global.DB;
import com.rj.soft.utils.SocketUtils;

/**
 * 客户端配置界面
 *
 * @author cjy
 * @version 1.2
 * @since 2019年4月19日
 */
@SuppressWarnings({"deprecation", "serial"})
public class Config extends JFrame implements ActionListener, ItemListener {

    private static Logger log = Logger.getLogger(Config.class);

    /**
     * 服务器地址
     */
    private JLabel hostLabel;

    private JTextField hostField;

    /**
     * 端口
     */
    private JLabel portLabel;

    private JTextField portField;

    /**
     * 批次控制
     */
    private JLabel batchLabel;

    private JCheckBox isOpen;

    /**
     * 是否自动
     */
    private JLabel autoLabel;

    private JCheckBox isAuto;

    /**
     * MD5校验
     */
    private JLabel md5Label;

    private JCheckBox isMd5;

    /**
     * 单位名称
     */
    private JLabel companyLabel;

    private JTextField companyField;

    /**
     * 用户名
     */
    private JLabel userLabel;

    private JTextField userField;

    /**
     * 密码
     */
    private JLabel passLabel;

    private JPasswordField passField;

    /**
     * 上传目录
     */
    private JLabel uploadLabel;

    private JTextField uploadField;

    /**
     * 备份上传目录
     */
    private JLabel backupUploadLabel;

    private JTextField backupUploadField;

    /**
     * 选择上传
     */
    private JButton chooseUpload;

    /**
     * 选择备份上传
     */
    private JButton chooseBackupUpload;

    /**
     * 下载目录
     */
    private JLabel downloadLabel;

    private JTextField downloadField;

    /**
     * 备份下载目录
     */
    private JLabel backupDownloadLabel;

    private JTextField backupDownloadField;

    /**
     * 选择下载
     */
    private JButton chooseDownload;

    /**
     * 选择备份下载
     */
    private JButton chooseBackupDownload;

    /**
     * 保存
     */
    private JButton saveButton;

    /**
     * 测试连接
     */
    private JButton checkButton;

    /**
     * 取消
     */
    private JButton cancelButton;

    /**
     * 文件选择器
     */
    private JFileChooser fileChooser;

    /**
     * 画布
     */
    private JPanel panel;

    /**
     * 父窗口
     */
    private JFrame parent;

    public Config(JFrame parent) {
        this.parent = parent;
        setTitle("数据交换客户端v3.0【配置】");
        setWindowIcon();
        initUI();
        this.setLayout(null);
        this.setVisible(true);
        this.setResizable(false);
        this.setLocationRelativeTo(null);

        WindowListener winCloser = new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                winClose();
            }
        };
        addWindowListener(winCloser);
    }

    /**
     * 按钮点击事件
     */
    public void actionPerformed(ActionEvent e) {
        boolean flag = isAuto.isSelected();
        JButton button = (JButton) e.getSource();
        if (button.getName().equals("saveButton")) {
            if (checkInput(flag)) {
                int show = showConfirm("是否要保存当前配置?", "系统提示", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (show == 0) {
                    save(flag);
                    dispose();
                    ((Login) parent).maxSized();
                } else {
                    return;
                }
            } else {
                return;
            }
        } else if (button.getName().equals("checkButton")) {
            String strUrl = "http://" + hostField.getText() + ":" + portField.getText();
            String result = SocketUtils.checkServer(strUrl);
            showMessage(result, "系统提示", JOptionPane.WARNING_MESSAGE);
        } else if (button.getName().equals("cancelButton")) {
            int show = showConfirm("是否放弃当前操作?", "系统提示", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (show == 0) {
                dispose();
                ((Login) parent).maxSized();
            } else {
                return;
            }
        } else if (button.getName().equals("chooseUpload")) {
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int show = fileChooser.showOpenDialog(panel);
            File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile != null && show == JFileChooser.APPROVE_OPTION) {
                uploadField.setText(selectedFile.getPath());
            }
        } else if (button.getName().equals("chooseBackupUpload")) {
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int show = fileChooser.showOpenDialog(panel);
            File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile != null && show == JFileChooser.APPROVE_OPTION) {
                backupUploadField.setText(selectedFile.getPath());
            }
        } else if (button.getName().equals("chooseDownload")) {
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int show = fileChooser.showOpenDialog(panel);
            File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile != null && show == JFileChooser.APPROVE_OPTION) {
                downloadField.setText(selectedFile.getPath());
            }
        } else if (button.getName().equals("chooseBackupDownload")) {
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int show = fileChooser.showOpenDialog(panel);
            File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile != null && show == JFileChooser.APPROVE_OPTION) {
                backupDownloadField.setText(selectedFile.getPath());
            }
        }
    }

    public void itemStateChanged(ItemEvent e) {
        JCheckBox checkBox = (JCheckBox) e.getSource();
        if (checkBox.getName().equals("isAuto")) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                setAuto();
            } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                setUnAuto();
            }
        } else if (checkBox.getName().equals("isOpen")) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                System.out.println("open batch");

            } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                System.out.println("close batch");
            }
        }
    }

    /**
     * 窗口关闭事件
     */
    public void winClose() {
        ((Login) parent).maxSized();
    }

    /**
     * 初始化配置界面
     */
    public void initUI() {
        panel = new JPanel();
        fileChooser = new JFileChooser();

        saveButton = new JButton("保存");
        saveButton.setName("saveButton");
        saveButton.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        saveButton.addActionListener(this);

        checkButton = new JButton("测试连接");
        checkButton.setName("checkButton");
        checkButton.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        checkButton.addActionListener(this);

        cancelButton = new JButton("取消");
        cancelButton.setName("cancelButton");
        cancelButton.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        cancelButton.addActionListener(this);

        chooseUpload = new JButton("选择目录");
        chooseUpload.setName("chooseUpload");
        chooseUpload.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        chooseUpload.setBounds(370, 406, 100, 30);
        chooseUpload.setForeground(Color.BLUE);
        chooseUpload.addActionListener(this);

        chooseBackupUpload = new JButton("选择目录");
        chooseBackupUpload.setName("chooseBackupUpload");
        chooseBackupUpload.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        chooseBackupUpload.setBounds(370, 453, 100, 30);
        chooseBackupUpload.setForeground(Color.BLUE);
        chooseBackupUpload.addActionListener(this);

        chooseDownload = new JButton("选择目录");
        chooseDownload.setName("chooseDownload");
        chooseDownload.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        chooseDownload.setBounds(370, 500, 100, 30);
        chooseDownload.setForeground(Color.BLUE);
        chooseDownload.addActionListener(this);

        chooseBackupDownload = new JButton("选择目录");
        chooseBackupDownload.setName("chooseBackupDownload");
        chooseBackupDownload.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        chooseBackupDownload.setBounds(370, 547, 100, 30);
        chooseBackupDownload.setForeground(Color.BLUE);
        chooseBackupDownload.addActionListener(this);

        hostLabel = new JLabel("服务器地址: ");
        hostLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        hostLabel.setBounds(30, 28, 80, 30);
        hostField = new JTextField();
        hostField.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        hostField.setBounds(130, 33, 230, 20);

        portLabel = new JLabel("端口: ");
        portLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        portLabel.setBounds(30, 75, 80, 30);
        portField = new JTextField();
        portField.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        portField.setBounds(130, 80, 230, 20);

        batchLabel = new JLabel("批次控制：");
        batchLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        batchLabel.setBounds(30, 122, 80, 30);
        isOpen = new JCheckBox("开启");
        isOpen.setName("isOpen");
        isOpen.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        isOpen.setBounds(130, 127, 230, 20);
        isOpen.addItemListener(this);

        autoLabel = new JLabel("是否自动: ");
        autoLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        autoLabel.setBounds(30, 169, 80, 30);
        isAuto = new JCheckBox("自动");
        isAuto.setName("isAuto");
        isAuto.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        isAuto.setBounds(130, 174, 230, 20);
        isAuto.addItemListener(this);

        md5Label = new JLabel("MD5校验: ");
        md5Label.setFont(new Font("微软雅黑", Font.BOLD, 14));
        md5Label.setBounds(30, 216, 80, 30);
        isMd5 = new JCheckBox("开启");
        isMd5.setName("isMd5");
        isMd5.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        isMd5.setBounds(130, 221, 230, 20);
        isMd5.addItemListener(this);

        companyLabel = new JLabel("单位名称：");
        companyLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        companyLabel.setBounds(30, 263, 80, 30);
        companyField = new JTextField();
        companyField.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        companyField.setBounds(130, 268, 230, 20);

        userLabel = new JLabel("用户名: ");
        userLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        userLabel.setBounds(30, 310, 80, 30);
        userField = new JTextField();
        userField.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        userField.setBounds(130, 315, 230, 20);

        passLabel = new JLabel("密码: ");
        passLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        passLabel.setBounds(30, 357, 80, 30);
        passField = new JPasswordField();
        passField.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        passField.setBounds(130, 362, 230, 20);

        uploadLabel = new JLabel("上传目录: ");
        uploadLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        uploadLabel.setBounds(30, 404, 80, 30);
        uploadField = new JTextField();
        uploadField.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        uploadField.setBounds(130, 409, 230, 20);

        backupUploadLabel = new JLabel("备份上传: ");
        backupUploadLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        backupUploadLabel.setBounds(30, 451, 80, 30);
        backupUploadField = new JTextField();
        backupUploadField.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        backupUploadField.setBounds(130, 456, 230, 20);

        downloadLabel = new JLabel("下载目录: ");
        downloadLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        downloadLabel.setBounds(30, 498, 80, 30);
        downloadField = new JTextField();
        downloadField.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        downloadField.setBounds(130, 503, 230, 20);

        backupDownloadLabel = new JLabel("备份下载: ");
        backupDownloadLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        backupDownloadLabel.setBounds(30, 545, 80, 30);
        backupDownloadField = new JTextField();
        backupDownloadField.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        backupDownloadField.setBounds(130, 550, 230, 20);

        // 初始化数据
        initData();
        // 添加组件到面板
        addContentPane();
    }

    /**
     * 初始化各文本框
     */
    public void initData() {
        if (null != DB.SERVER_IP && !"".equals(DB.SERVER_IP)) {
            hostField.setText(DB.SERVER_IP);
        }
        if (null != DB.SERVER_PORT && !"".equals(DB.SERVER_PORT)) {
            portField.setText(DB.SERVER_PORT);
        }
        if (null != DB.IS_OPEN && !"".equals(DB.IS_OPEN)) {
            if ("true".equals(DB.IS_OPEN)) {
                isOpen.setSelected(true);
            } else {
                isOpen.setSelected(false);
            }
        } else {
            isOpen.setSelected(false);
        }
        if (null != DB.IS_AUTO && !"".equals(DB.IS_AUTO)) {
            if ("true".equals(DB.IS_AUTO)) {
                isAuto.setSelected(true);
                setAuto();
            } else {
                isAuto.setSelected(false);
                setUnAuto();
            }
        } else {// 当IS_AUTO为空时表示第一次运行
            isAuto.setSelected(false);
            setUnAuto();
        }
        if (null != DB.IS_MD5 && !"".equals(DB.IS_MD5)) {
            if ("true".equals(DB.IS_MD5)) {
                isMd5.setSelected(true);
            } else {
                isMd5.setSelected(false);
            }
        } else {
            isMd5.setSelected(false);
        }
        if (null != DB.COMPANY_NAME && !"".equals(DB.COMPANY_NAME)) {
            companyField.setText(DB.COMPANY_NAME);
        }
        if (null != DB.USERNAME && !"".equals(DB.USERNAME)) {
            userField.setText(DB.USERNAME);
        }
        if (null != DB.PASSWORD && !"".equals(DB.PASSWORD)) {
            passField.setText(DB.PASSWORD);
        }
        if (null != DB.UPLOAD_PATH && !"".equals(DB.UPLOAD_PATH)) {
            uploadField.setText(DB.UPLOAD_PATH);
        }
        if (null != DB.BACKUP_UPLOAD_PATH && !"".equals(DB.BACKUP_UPLOAD_PATH)) {
            backupUploadField.setText(DB.BACKUP_UPLOAD_PATH);
        }
        if (null != DB.DOWNLOAD_PATH && !"".equals(DB.DOWNLOAD_PATH)) {
            downloadField.setText(DB.DOWNLOAD_PATH);
        }
        if (null != DB.BACKUP_DOWNLOAD_PATH && !"".equals(DB.BACKUP_DOWNLOAD_PATH)) {
            backupDownloadField.setText(DB.BACKUP_DOWNLOAD_PATH);
        }
    }

    /**
     * 添加各组件到面板
     */
    public void addContentPane() {
        this.getContentPane().add(saveButton);
        this.getContentPane().add(cancelButton);
        this.getContentPane().add(checkButton);

        this.getContentPane().add(chooseUpload);
        this.getContentPane().add(chooseBackupUpload);
        this.getContentPane().add(chooseDownload);
        this.getContentPane().add(chooseBackupDownload);

        this.getContentPane().add(hostField);
        this.getContentPane().add(portField);
        this.getContentPane().add(isOpen);
        this.getContentPane().add(isAuto);
        this.getContentPane().add(isMd5);
        this.getContentPane().add(companyField);
        this.getContentPane().add(userField);
        this.getContentPane().add(passField);
        this.getContentPane().add(uploadField);
        this.getContentPane().add(backupUploadField);
        this.getContentPane().add(downloadField);
        this.getContentPane().add(backupDownloadField);

        this.getContentPane().add(hostLabel);
        this.getContentPane().add(portLabel);
        this.getContentPane().add(batchLabel);
        this.getContentPane().add(autoLabel);
        this.getContentPane().add(md5Label);
        this.getContentPane().add(companyLabel);
        this.getContentPane().add(userLabel);
        this.getContentPane().add(passLabel);
        this.getContentPane().add(uploadLabel);
        this.getContentPane().add(backupUploadLabel);
        this.getContentPane().add(downloadLabel);
        this.getContentPane().add(backupDownloadLabel);
    }

    /**
     * 设置为自动
     */
    public void setAuto() {
        setSize(500, 700);
        companyLabel.setVisible(true);
        companyField.setVisible(true);

        userLabel.setVisible(true);
        userField.setVisible(true);

        passLabel.setVisible(true);
        passField.setVisible(true);

        uploadLabel.setVisible(true);
        uploadField.setVisible(true);
        chooseUpload.setVisible(true);

        backupUploadLabel.setVisible(true);
        backupUploadField.setVisible(true);
        chooseBackupUpload.setVisible(true);

        downloadLabel.setVisible(true);
        downloadField.setVisible(true);
        chooseDownload.setVisible(true);

        backupDownloadLabel.setVisible(true);
        backupDownloadField.setVisible(true);
        chooseBackupDownload.setVisible(true);

        saveButton.setBounds(45, 600, 70, 30);
        checkButton.setBounds(155, 600, 100, 30);
        cancelButton.setBounds(295, 600, 70, 30);
    }

    /**
     * 设置为非自动
     */
    public void setUnAuto() {
        setSize(400, 350);
        companyLabel.setVisible(false);
        companyField.setVisible(false);

        userLabel.setVisible(false);
        userField.setVisible(false);

        passLabel.setVisible(false);
        passField.setVisible(false);

        uploadLabel.setVisible(false);
        uploadField.setVisible(false);
        chooseUpload.setVisible(false);

        backupUploadLabel.setVisible(false);
        backupUploadField.setVisible(false);
        chooseBackupUpload.setVisible(false);

        downloadLabel.setVisible(false);
        downloadField.setVisible(false);
        chooseDownload.setVisible(false);

        backupDownloadLabel.setVisible(false);
        backupDownloadField.setVisible(false);
        chooseBackupDownload.setVisible(false);

        saveButton.setBounds(45, 252, 70, 30);
        checkButton.setBounds(155, 252, 100, 30);
        cancelButton.setBounds(295, 252, 70, 30);
    }

    /**
     * 检查输入
     *
     * @param flag 是否自动
     */
    public boolean checkInput(boolean flag) {
        if (null == hostField.getText() || "".equals(hostField.getText())) {
            showMessage("请配置IP地址!", "系统提示", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (null == portField.getText() || "".equals(portField.getText())) {
            showMessage("请配置端口!", "系统提示", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if ((null == userField.getText() || "".equals(userField.getText())) && flag) {
            showMessage("请输入用户名!", "系统提示", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if ((null == companyField.getText() || "".equals(companyField.getText())) && flag) {
            if (!userField.getText().trim().equals("100001")) {// 除系统管理员外，其它用户需配置单位名称
                showMessage("请配置单位名称!", "系统提示", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        }
        if ((null == passField.getText() || "".equals(passField.getText())) && flag) {
            showMessage("请输入密码!", "系统提示", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if ((null == uploadField.getText() || "".equals(uploadField.getText())) && flag) {
            showMessage("请设置自动上传目录!", "系统提示", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if ((null == backupUploadField.getText() || "".equals(backupUploadField.getText())) && flag) {
            showMessage("请设置备份上传目录!", "系统提示", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if ((null == downloadField.getText() || "".equals(downloadField.getText())) && flag) {
            showMessage("请设置自动下载目录!", "系统提示", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if ((null == backupDownloadField.getText() || "".equals(backupDownloadField.getText())) && flag) {
            showMessage("请设置备份下载目录!", "系统提示", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    /**
     * 保存字段值
     *
     * @param flag 是否自动
     */
    public void save(boolean flag) {
        try {
            Properties prop = new Properties();
            InputStream is = new FileInputStream(DB.FILEPATH);
            prop.load(is);
            OutputStream os = new FileOutputStream(DB.FILEPATH);
            prop.setProperty("IP", hostField.getText());
            prop.setProperty("Port", portField.getText());
            prop.setProperty("IS_OPEN", isOpen.isSelected() ? "true" : "false");
            String auto = "";
            if (flag) {
                auto = "true";
            } else {
                auto = "false";
            }
            prop.setProperty("IS_AUTO", auto);
            prop.setProperty("IS_MD5", isMd5.isSelected() ? "true" : "false");
            prop.setProperty("CompanyName", companyField.getText());
            prop.setProperty("UserName", userField.getText());
            prop.setProperty("PassWord", passField.getText());
            prop.setProperty("UploadPath", uploadField.getText());
            prop.setProperty("BackupUploadPath", backupUploadField.getText());
            prop.setProperty("DownloadPath", downloadField.getText());
            prop.setProperty("BackupDownloadPath", backupDownloadField.getText());
            prop.store(os, null);
        } catch (Exception e) {
            log.error(e);
            e.printStackTrace();
        }
    }

    public void showMessage(String message, String title, int messageType) {
        JLabel label = new JLabel();
        label.setText(message);
        label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        JOptionPane.showMessageDialog(Config.this, label, title, messageType);
    }

    public int showConfirm(String message, String title, int optionType, int messageType) {
        JLabel label = new JLabel();
        label.setText(message);
        label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        return JOptionPane.showConfirmDialog(Config.this, label, title, optionType, messageType);
    }

    /**
     * 设置窗口图标
     */
    public void setWindowIcon() {
        ImageIcon icon = new ImageIcon(getClass().getResource("/icon.png"));
        this.setIconImage(icon.getImage());
    }

}
