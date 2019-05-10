package com.rj.soft.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;

import org.apache.log4j.Logger;

import com.rj.soft.global.DB;
import com.rj.soft.global.DataBox;
import com.rj.soft.task.AutoTaskListener;
import com.rj.soft.utils.CommonUtils;

/**
 * 客户端自动任务界面（定时上传/下载）
 * 
 * @author cjy
 * @since 2018年2月11日
 * @version 1.1
 * 
 */
@SuppressWarnings("serial")
public class AutoTask extends JFrame implements ActionListener {

	private Logger log = Logger.getLogger(AutoTask.class);

	private JButton startButton, stopButton, exitButton;

	/**
	 * 自动任务时间间隔标签
	 */
	private JLabel autoLabel;

	private JSpinner autoSpinner;

	/**
	 * 备注说明
	 */
	private JLabel remarkLabel;

	private Timer autoTimer;

	/**
	 * timer执行时间间隔数值单元(单位为每分钟)
	 */
	private int delayUnit = 60 * 1000;

	/**
	 * 父窗口
	 */
	private JFrame parent;

	public AutoTask(JFrame parent) {
		this.parent = parent;
		this.setTitle("数据交换客户端v3.0【定时】");
		this.setWindowIcon();
		this.initUI();
		this.setLayout(null);
		this.setVisible(true);
		this.setResizable(false);
		this.setLocationRelativeTo(null);
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				// 窗口关闭
				winClose();
			}
		});
	}

	/**
	 * 初始化界面
	 */
	public void initUI() {
		setSize(400, 250);

		startButton = new JButton("启动");
		startButton.setName("startButton");
		startButton.addActionListener(this);
		startButton.setBounds(70, 120, 70, 30);
		startButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		stopButton = new JButton("停止");
		stopButton.setName("stopButton");
		stopButton.addActionListener(this);
		stopButton.setBounds(170, 120, 70, 30);
		stopButton.setEnabled(false);
		stopButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		exitButton = new JButton("退出");
		exitButton.setName("exitButton");
		exitButton.addActionListener(this);
		exitButton.setBounds(270, 120, 70, 30);
		exitButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		autoLabel = new JLabel("任务时间周期(单位: 分钟)");
		autoLabel.setBounds(80, 50, 250, 30);
		autoLabel.setFont(new Font("微软雅黑", Font.BOLD, 12));
		autoSpinner = new JSpinner();
		// 单位为分钟: 当前值=1, 最小值=1, 最大值=60, 增量=1
		autoSpinner.setModel(new SpinnerNumberModel(1, 1, 60, 1));
		autoSpinner.setBounds(240, 50, 60, 30);

		remarkLabel = new JLabel("备注: 最小为1分钟, 最大为60分钟, 增量为1分钟");
		remarkLabel.setForeground(Color.BLUE);
		remarkLabel.setBounds(50, 170, 350, 30);
		remarkLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		getContentPane().add(startButton);
		getContentPane().add(stopButton);
		getContentPane().add(exitButton);
		getContentPane().add(autoLabel);
		getContentPane().add(autoSpinner);
		getContentPane().add(remarkLabel);
	}

	/**
	 * 按钮点击事件
	 */
	public void actionPerformed(ActionEvent e) {
		DataBox dataBox = CommonUtils.initDataBox();
		JButton button = (JButton) e.getSource();
		if (button.getName().equals("startButton")) {
			int interval = Integer.valueOf(autoSpinner.getValue().toString());
			// 任务空闲时
			if (DB.EXEC_FLAG) {
				log.info("任务空闲状态...");
				autoTimer = new Timer(interval * delayUnit, new AutoTaskListener(dataBox.getUploadPath(),
						dataBox.getBackupUploadPath(), dataBox.getDownloadPath(), dataBox.getBackupDownloadPath()));
				autoTimer.setRepeats(true);
				autoTimer.start();
				stopButton.setEnabled(true);
				startButton.setEnabled(false);
				autoSpinner.setEnabled(false);
				showMessage("自动任务已开启!", "系统提示", JOptionPane.INFORMATION_MESSAGE);
				log.info("自动任务已开启!");
			} else {
				log.info("任务执行状态...");
			}
		} else if (button.getName().equals("stopButton")) {
			if (null == autoTimer) {
				showMessage("自动任务为空!", "系统提示", JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			if (autoTimer.isRunning()) {
				int show = showConfirm("自动任务正在运行，是否要关闭?", "系统提示", JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE);
				if (show == 0) {
					autoTimer.stop();
					autoTimer = null;
					startButton.setEnabled(true);
					stopButton.setEnabled(false);
					autoSpinner.setEnabled(true);
					DB.EXEC_FLAG = true;
					log.info("自动任务已关闭!");
				} else {
					return;
				}
			} else {
				showMessage("自动任务已关闭!", "系统提示", JOptionPane.INFORMATION_MESSAGE);
				return;
			}
		} else if (button.getName().equals("exitButton")) {
			if (null != autoTimer && autoTimer.isRunning()) {
				int show = showConfirm("自动任务正在运行，是否要退出?", "系统提示", JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE);
				if (show == 0) {
					DB.EXEC_FLAG = true;
					dispose();
					((Main) parent).maxSized();
				} else {
					return;
				}
			} else {
				dispose();
				((Main) parent).maxSized();
			}
		}
	}

	/**
	 * 窗口关闭事件
	 */
	public void winClose() {
		if (null != autoTimer && autoTimer.isRunning()) {
			int show = showConfirm("自动任务正在运行，是否要退出?", "系统提示", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (show == 0) {
				DB.EXEC_FLAG = true;
				dispose();
				((Main) parent).maxSized();
			} else {
				return;
			}
		} else {
			dispose();
			((Main) parent).maxSized();
		}
	}

	public void showMessage(String message, String title, int messageType) {
		JLabel label = new JLabel();
		label.setText(message);
		label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		JOptionPane.showMessageDialog(AutoTask.this, label, title, messageType);
	}

	public int showConfirm(String message, String title, int optionType, int messageType) {
		JLabel label = new JLabel();
		label.setText(message);
		label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		return JOptionPane.showConfirmDialog(AutoTask.this, label, title, optionType, messageType);
	}

	/**
	 * 设置窗口图标
	 */
	public void setWindowIcon() {
		ImageIcon icon = new ImageIcon(getClass().getResource("/icon.png"));
		this.setIconImage(icon.getImage());
	}

}
