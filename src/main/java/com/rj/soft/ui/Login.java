package com.rj.soft.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;

import com.alibaba.fastjson.JSONObject;
import org.apache.log4j.Logger;

import com.rj.soft.global.Cookie;
import com.rj.soft.global.DB;
import com.rj.soft.utils.CookieUtils;
import com.rj.soft.utils.SocketUtils;

/**
 * 客户端登陆界面
 *
 * @author cjy
 * @version 1.1
 * @since 2018年8月30日
 */
@SuppressWarnings({"serial", "deprecation"})
public class Login extends JFrame implements ActionListener, ItemListener {

    private static Logger log = Logger.getLogger(Login.class);

    private static final JLabel userNameLabel = new JLabel("用户名");

    private static final JTextField userNameInput = new JTextField();

    private static final JLabel passWordLabel = new JLabel("密    码");

    private static final JPasswordField passWordInput = new JPasswordField();

    private static final JButton loginButton = new JButton("登录");

    private static final JButton configButton = new JButton("配置");

    private static final JButton exitButton = new JButton("退出");

    private static final JCheckBox remUserBox = new JCheckBox("记住用户");

    private static final JCheckBox remPassBox = new JCheckBox("记住密码");

    /**
     * 文件路径（私有变量）
     */
    private static String path;

    /**
     * 用户名
     */
    private static String userName = "";

    /**
     * 登陆密码
     */
    private static String passWord = "";

    /**
     * Cookie
     */
    private Cookie cookie;

    // 配置文件路径
    private static String configPath = "";

    /**
     * 配置文件路径启动时动态加载
     */
    static {
        try {
            // 获取类运行时的当前路径
            path = new File("").getAbsolutePath();
            configPath = path + File.separator + "config.properties";
            log.info("配置文件路径：" + configPath);
        } catch (Exception e) {
            log.error(e);
            // 初始化失败，默认放在C盘
            configPath = "C:\\数据交换客户端\\config.properties";
        }
    }

    public Login() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            log.error(e);
        }
        // 初始化配置文件
        ConfigFile();
        setTitle("数据交换客户端v3.0【登陆】");
        setWindowIcon();
        setLayout(null);
        // 初始化登陆界面
        initUI();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        setSize(new Dimension(330, 180));
        setLocationRelativeTo(null);
        setResizable(false);
        this.getContentPane();
    }

    /**
     * 按钮事件方法
     */
    public void actionPerformed(ActionEvent e) {
        JButton button = (JButton) e.getSource();
        if (button.getName().equals("loginButton")) {
            // 刷新cookie
            refreshCookie();
            // 用户登陆校验
            loginValid();
        } else if (button.getName().equals("configButton")) {
            // 刷新cookie
            refreshCookie();
            // 加载配置文件
            loadConfig();
            new Config(this);
            miniSized();
        } else if (button.getName().equals("exitButton")) {
            int show = showConfirm("是否要退出客户端?", "系统提示", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
            if (show == 0) {
                // 刷新cookie
                refreshCookie();
                System.exit(0);
            } else {
                return;
            }
        }
    }

    /**
     * CheckBox事件方法
     */
    public void itemStateChanged(ItemEvent e) {

    }

    /**
     * 初始化登陆界面
     */
    public void initUI() {
        userNameLabel.setBounds(30, 20, 100, 21);
        userNameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        userNameLabel.setForeground(Color.DARK_GRAY);
        userNameInput.setBounds(90, 20, 130, 21);
        userNameInput.setBorder(new LineBorder(Color.GRAY, 2));

        remUserBox.setBounds(220, 20, 80, 21);
        remUserBox.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        remUserBox.setForeground(Color.BLUE);

        passWordLabel.setBounds(30, 60, 100, 21);
        passWordLabel.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        passWordInput.setBounds(90, 60, 130, 21);
        passWordInput.setBorder(new LineBorder(Color.GRAY, 2));

        remPassBox.setBounds(220, 60, 80, 21);
        remPassBox.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        remPassBox.setForeground(Color.BLUE);

        loginButton.setBounds(30, 100, 60, 21);
        loginButton.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        loginButton.setName("loginButton");
        loginButton.addActionListener(this);

        exitButton.setBounds(230, 100, 60, 21);
        exitButton.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        exitButton.setName("exitButton");
        exitButton.addActionListener(this);

        configButton.setBounds(130, 100, 60, 21);
        configButton.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        configButton.setName("configButton");
        configButton.addActionListener(this);

        add(userNameLabel);
        add(userNameInput);
        add(remUserBox);
        add(passWordLabel);
        add(passWordInput);
        add(remPassBox);
        add(loginButton);
        add(exitButton);
        add(configButton);

        // 加载配置文件
        loadConfig();

        cookie = CookieUtils.getCookie(new File(configPath));
        if ("1".equals(cookie.getRemUser())) {
            remUserBox.setSelected(true);
            if (cookie.getUserName() != null && !"".equals(cookie.getUserName())) {
                userNameInput.setText(cookie.getUserName());
            }
        }
        if ("1".equals(cookie.getRemPass())) {
            remPassBox.setSelected(true);
            if (cookie.getPassWord() != null && !"".equals(cookie.getPassWord())) {
                passWordInput.setText(cookie.getPassWord());
            }
        }
    }

    /**
     * 初始化配置文件
     */
    public void ConfigFile() {
        String filePath = configPath;
        DB.FILEPATH = filePath;
        DB.PATH = path;
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                file.createNewFile();
                Properties prop = new Properties();
                InputStream fis = new FileInputStream(filePath);
                prop.load(fis);
                OutputStream fos = new FileOutputStream(filePath);
                // 服务器IP
                prop.setProperty("IP", "127.0.0.1");
                // 服务器端口
                prop.setProperty("Port", "");
                // 是否开启批次控制（默认关闭）
                prop.setProperty("IS_OPEN", "false");
                // 是否开启自动模式（自动上传/下载任务）
                prop.setProperty("IS_AUTO", "false");
                // 是否开启MD5校验（默认关闭）
                prop.setProperty("IS_MD5", "false");
                // 接入单位名称
                prop.setProperty("CompanyName", "");
                // 数据交换账号（根据接入单位分配）
                prop.setProperty("UserName", "");
                // 账号登陆密码
                prop.setProperty("PassWord", "12345678");
                // 自动上传目录（默认）
                prop.setProperty("UploadPath", path + File.separator + "upload");
                // 备份自动上传目录（默认）
                prop.setProperty("BackupUploadPath", path + File.separator + "backup" + File.separator + "upload");
                // 自动下载目录（默认）
                prop.setProperty("DownloadPath", path + File.separator + "download");
                // 备份自动下载目录（默认）
                prop.setProperty("BackupDownloadPath", path + File.separator + "backup" + File.separator + "download");
                // 默认文件路径（下载）
                prop.setProperty("DefaultFilePath", "C:\\TEMP\\");
                // 是否记住用户
                prop.setProperty("RemUser", "0");
                // 是否记住密码
                prop.setProperty("RemPass", "0");
                prop.setProperty("RemUserName", "");
                prop.setProperty("RemPassWord", "");
                prop.store(fos, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        File uploadFile = new File(DB.PATH + "\\upload");
        if (!uploadFile.exists()) {
            try {
                uploadFile.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
                log.info("创建目录失败：" + e);
            }
        }
        File downloadFile = new File(DB.PATH + "\\download");
        if (!downloadFile.exists()) {
            try {
                downloadFile.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
                log.info("创建目录失败：" + e);
            }
        }
        File backupFile = new File(DB.PATH + "\\backup");
        if (!backupFile.exists()) {
            try {
                backupFile.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
                log.info("创建目录失败：" + e);
            }
        }
        File backupUploadFile = new File(DB.PATH + "\\backup\\upload");
        if (!backupUploadFile.exists()) {
            try {
                backupUploadFile.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
                log.info("创建目录失败：" + e);
            }
        }
        File backupDownloadFile = new File(DB.PATH + "\\backup\\download");
        if (!backupDownloadFile.exists()) {
            try {
                backupDownloadFile.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
                log.info("创建目录失败：" + e);
            }
        }
    }

    /**
     * 初始化配置路径
     */
    public void FindLocation() {
        File file = new File("C:\\");
        File[] files = file.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                if (files[i].getName().equals("数据交换客户端")) {
                    path = files[i].getAbsolutePath();
                    System.out.println(path);
                    break;
                }
                File[] files2 = files[i].listFiles();
                if (null != files2) {
                    for (int j = 0; j < files2.length; j++) {
                        if (files2[j].isDirectory()) {
                            if (files2[j].getName().equals("数据交换客户端")) {
                                path = files2[j].getAbsolutePath();
                                System.out.println(path);
                                break;
                            }
                        }
                    }
                }
            }
        }
        if (path == null) {
            path = "C:\\数据交换客户端";
            File tempfile = new File(path);
            if (!tempfile.exists()) {
                try {
                    tempfile.mkdir();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("创建数据交换客户端失败");
                }
            }
        }
    }

    /**
     * 加载配置文件
     */
    public void loadConfig() {
        Properties properties = new Properties();
        FileInputStream fis;
        try {
            fis = new FileInputStream(new File(path + "\\config.properties"));
            properties.load(fis);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        DB.SERVER_IP = properties.getProperty("IP");
        DB.SERVER_PORT = properties.getProperty("Port");
        DB.IS_OPEN = properties.getProperty("IS_OPEN");
        DB.IS_AUTO = properties.getProperty("IS_AUTO");
        DB.IS_MD5 = properties.getProperty("IS_MD5");
        DB.COMPANY_NAME = properties.getProperty("CompanyName");
        DB.USERNAME = properties.getProperty("UserName");
        DB.PASSWORD = properties.getProperty("PassWord");
        DB.UPLOAD_PATH = properties.getProperty("UploadPath");
        DB.BACKUP_UPLOAD_PATH = properties.getProperty("BackupUploadPath");
        DB.DOWNLOAD_PATH = properties.getProperty("DownloadPath");
        DB.BACKUP_DOWNLOAD_PATH = properties.getProperty("BackupDownloadPath");
        DB.DEFAULT_FILE_PATH = properties.getProperty("DefaultFilePath");
    }

    /**
     * 刷新cookie
     */
    public void refreshCookie() {
        if (remUserBox.isSelected()) {
            save("RemUser", "1");
            save("RemUserName", userNameInput.getText());
        } else {
            save("RemUser", "0");
        }
        if (remPassBox.isSelected()) {
            save("RemPass", "1");
            save("RemPassWord", passWordInput.getText());
        } else {
            save("RemPass", "0");
        }
    }

    /**
     * 保存键值
     *
     * @param key
     * @param value
     */
    public void save(String key, String value) {
        try {
            File file = new File(configPath);
            Properties props = new Properties();
            InputStream is = new FileInputStream(file);
            props.load(is);
            OutputStream os = new FileOutputStream(file);
            props.setProperty(key, value);
            props.store(os, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 用户登陆校验
     */
    public void loginValid() {
        // 加载配置文件
        loadConfig();
        try {
            userName = userNameInput.getText().trim();
            if (null == userName || "".equals(userName)) {
                showMessage("请输入用户名!", "系统提示", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            passWord = passWordInput.getText().trim();
            if (null == passWord || "".equals(passWord)) {
                showMessage("请输入密码!", "系统提示", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            JSONObject jsonObject = SocketUtils.loginValid(DB.SERVER_IP, Integer.parseInt(DB.SERVER_PORT), userName,
                    passWord);
            if ("true".equals(jsonObject.get("success") + "")) {
                dispose();
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        new Main(userName, "").buildInit();
                    }
                });
                return;
            } else {
                showMessage(jsonObject.get("msg").toString(), "系统提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (Exception e) {
            log.error(e);
            showMessage("用户登陆异常!", "系统提示", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 自定义提示消息
     *
     * @param message     消息
     * @param title       标题
     * @param messageType 消息类型
     */
    public void showMessage(String message, String title, int messageType) {
        JLabel label = new JLabel();
        label.setText(message);
        label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        JOptionPane.showMessageDialog(Login.this, label, title, messageType);
    }

    public int showConfirm(String message, String title, int optionType, int messageType) {
        JLabel label = new JLabel();
        label.setText(message);
        label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        return JOptionPane.showConfirmDialog(Login.this, label, title, optionType, messageType);
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Login();
            }
        });
    }

}
