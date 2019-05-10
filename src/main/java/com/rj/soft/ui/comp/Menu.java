package com.rj.soft.ui.comp;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.rj.soft.global.DB;
import com.rj.soft.ui.AutoTask;
import com.rj.soft.ui.Main;
import com.rj.soft.ui.UpdatePwd;

/**
 * 客户端菜单
 * 
 * @author cjy
 * @since 2018年2月26日
 * @version 1.1
 * 
 */
@SuppressWarnings("serial")
public class Menu extends JFrame implements ActionListener {

	private JMenuBar menuBar;

	private JMenu sysMenu, logMenu, helpMenu;

	private JMenuItem autoTaskItem, updatePwdItem, exitItem;

	private JMenuItem upLogItem, downLogItem;

	private JMenuItem aboutItem;

	private JFrame frame;

	public Menu(JFrame frame) {
		this.frame = frame;

		menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);

		sysMenu = new JMenu("系统");
		sysMenu.setFont(new Font("微软雅黑", Font.PLAIN, 15));
		sysMenu.setName("sysMenu");

		autoTaskItem = new JMenuItem("自动任务");
		autoTaskItem.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		autoTaskItem.setName("autoTaskItem");
		autoTaskItem.addActionListener(this);

		updatePwdItem = new JMenuItem("修改密码");
		updatePwdItem.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		updatePwdItem.setName("updatePwdItem");
		updatePwdItem.addActionListener(this);

		exitItem = new JMenuItem("退出");
		exitItem.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		exitItem.setName("exitItem");
		exitItem.addActionListener(this);

		sysMenu.add(autoTaskItem);
		sysMenu.addSeparator();
		sysMenu.add(updatePwdItem);
		sysMenu.addSeparator();
		sysMenu.add(exitItem);

		logMenu = new JMenu("日志");
		logMenu.setFont(new Font("微软雅黑", Font.PLAIN, 15));
		logMenu.setName("logMenu");

		upLogItem = new JMenuItem("上传日志");
		upLogItem.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		upLogItem.setName("upLogItem");
		upLogItem.addActionListener(this);

		downLogItem = new JMenuItem("下载日志");
		downLogItem.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		downLogItem.setName("downLogItem");
		downLogItem.addActionListener(this);

		logMenu.add(upLogItem);
		logMenu.addSeparator();
		logMenu.add(downLogItem);

		helpMenu = new JMenu("帮助");
		helpMenu.setFont(new Font("微软雅黑", Font.PLAIN, 15));
		helpMenu.setName("helpMenu");

		aboutItem = new JMenuItem("版本信息");
		aboutItem.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		aboutItem.setName("aboutItem");
		aboutItem.addActionListener(this);

		helpMenu.add(aboutItem);

		menuBar.add(sysMenu);
		menuBar.add(logMenu);
		menuBar.add(helpMenu);
	}

	/**
	 * 菜单项点击事件
	 */
	public void actionPerformed(ActionEvent e) {
		JMenuItem menuItem = (JMenuItem) e.getSource();
		if (menuItem.getName().equals("autoTaskItem")) {
			// 检查客户端配置是否完成
			if (checkConfig()) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						new AutoTask(frame);
					}
				});
				((Main) frame).miniSized();
			} else {
				return;
			}
		} else if (menuItem.getName().equals("updatePwdItem")) {
			if (!frame.getTitle().equals("数据交换客户端【首页】")) {
				frame.dispose();
			}
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					new UpdatePwd(frame);
				}
			});
		} else if (menuItem.getName().equals("logoutItem")) {

		} else if (menuItem.getName().equals("exitItem")) {
			int show = showConfirm("是否要退出客户端?", "系统提示", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (show == 0) {
				System.exit(0);
			} else {
				return;
			}
		} else if (menuItem.getName().equals("upLogItem")) {
			showMessage("功能开发中......", "系统提示", JOptionPane.INFORMATION_MESSAGE);
			return;
		} else if (menuItem.getName().equals("downLogItem")) {
			showMessage("功能开发中......", "系统提示", JOptionPane.INFORMATION_MESSAGE);
			return;
		} else if (menuItem.getName().equals("aboutItem")) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					new Version();
				}
			});
			// showMessage("功能开发中......", "系统提示",
			// JOptionPane.INFORMATION_MESSAGE);
			// return;
		}
	}

	/**
	 * 检查客户端配置【IP、端口、是否自动、单位名称、用户名、密码、上传/下载路径】
	 */
	public boolean checkConfig() {
		// 首先判断是否勾选自动
		if (null != DB.IS_AUTO && !"".equals(DB.IS_AUTO) && !"true".equals(DB.IS_AUTO)) {
			showMessage("客户端未启用自动模式!", "系统提示", JOptionPane.INFORMATION_MESSAGE);
			return false;
		}
		boolean tag = true;
		if (null == DB.SERVER_IP || "".equals(DB.SERVER_IP)) {
			tag = false;
		}
		if (null == DB.SERVER_PORT || "".equals(DB.SERVER_PORT)) {
			tag = false;
		}
		if (null == DB.USERNAME || "".equals(DB.USERNAME)) {
			tag = false;
		}
		if (null == DB.COMPANY_NAME || "".equals(DB.COMPANY_NAME)) {
			if (!DB.USERNAME.equals("100001")) {// 除系统管理员外，其它用户需配置单位名称
				tag = false;
			}
		}
		if (null == DB.PASSWORD || "".equals(DB.PASSWORD)) {
			tag = false;
		}
		if (null == DB.UPLOAD_PATH || "".equals(DB.UPLOAD_PATH)) {
			tag = false;
		}
		if (null == DB.BACKUP_UPLOAD_PATH || "".equals(DB.BACKUP_UPLOAD_PATH)) {
			tag = false;
		}
		if (null == DB.DOWNLOAD_PATH || "".equals(DB.DOWNLOAD_PATH)) {
			tag = false;
		}
		if (null == DB.BACKUP_DOWNLOAD_PATH || "".equals(DB.BACKUP_DOWNLOAD_PATH)) {
			tag = false;
		}
		// 以上任意一项参数校验失败
		if (!tag) {
			showMessage("请检查客户端配置!", "系统提示", JOptionPane.INFORMATION_MESSAGE);
			return false;
		}
		return true;
	}

	public int showConfirm(String message, String title, int optionType, int messageType) {
		JLabel label = new JLabel();
		label.setText(message);
		label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		return JOptionPane.showConfirmDialog(frame, label, title, optionType, messageType);
	}

	public void showMessage(String message, String title, int messageType) {
		JLabel label = new JLabel();
		label.setText(message);
		label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		JOptionPane.showMessageDialog(frame, label, title, messageType);
	}

}
