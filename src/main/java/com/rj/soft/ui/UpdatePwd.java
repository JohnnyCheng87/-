package com.rj.soft.ui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.alibaba.fastjson.JSONObject;
import org.apache.log4j.Logger;

import com.rj.soft.global.DB;
import com.rj.soft.utils.SocketUtils;

/**
 * 客户端修改密码界面
 * 
 * @author cjy
 * @since 2018年2月5日
 * @version 1.0
 * 
 */
@SuppressWarnings({ "serial", "deprecation" })
public class UpdatePwd extends JFrame implements ActionListener {

	private static Logger log = Logger.getLogger(UpdatePwd.class);

	private JButton submitButton, resetButton;
	private JTextField usernameField;
	private JPasswordField oldpasswordField, newpasswordField, repasswordField;
	private JLabel usernameLabel, oldpasswordLabel, newpasswordLabel, repasswordLabel;

	private JFrame parent;

	public UpdatePwd(JFrame parent) {
		this.parent = parent;
		setTitle("数据交换客户端v3.0【修改密码】");
		setWindowIcon();
		this.setPreferredSize(new Dimension(500, 380));
		setSize(500, 380);
		setLayout(null);
		initUI();
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
		setResizable(false);
	}

	public void initUI() {
		usernameLabel = new JLabel("用户名:");
		usernameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 15));
		usernameLabel.setBounds(50, 25, 150, 30);
		this.getContentPane().add(usernameLabel);

		oldpasswordLabel = new JLabel("旧密码");
		oldpasswordLabel.setFont(new Font("微软雅黑", Font.PLAIN, 15));
		oldpasswordLabel.setBounds(50, 85, 150, 30);
		this.getContentPane().add(oldpasswordLabel);

		newpasswordLabel = new JLabel("新密码");
		newpasswordLabel.setFont(new Font("微软雅黑", Font.PLAIN, 15));
		newpasswordLabel.setBounds(50, 145, 150, 30);
		this.getContentPane().add(newpasswordLabel);

		repasswordLabel = new JLabel("重复密码");
		repasswordLabel.setFont(new Font("微软雅黑", Font.PLAIN, 15));
		repasswordLabel.setBounds(50, 205, 150, 30);
		this.getContentPane().add(repasswordLabel);

		usernameField = new JTextField();
		usernameField.setBounds(160, 25, 270, 30);
		this.getContentPane().add(usernameField);

		oldpasswordField = new JPasswordField();
		oldpasswordField.setBounds(160, 85, 270, 30);
		this.getContentPane().add(oldpasswordField);

		newpasswordField = new JPasswordField();
		newpasswordField.setBounds(160, 145, 270, 30);
		this.getContentPane().add(newpasswordField);

		repasswordField = new JPasswordField();
		repasswordField.setBounds(160, 205, 270, 30);
		this.getContentPane().add(repasswordField);

		submitButton = new JButton("确定");
		submitButton.setFont(new Font("微软雅黑", Font.PLAIN, 15));
		submitButton.setName("submitButton");
		submitButton.setBounds(60, 280, 150, 30);
		this.getContentPane().add(submitButton);
		submitButton.addActionListener(this);

		resetButton = new JButton("重置");
		resetButton.setFont(new Font("微软雅黑", Font.PLAIN, 15));
		resetButton.setName("resetButton");
		resetButton.setBounds(270, 280, 150, 30);
		this.getContentPane().add(resetButton);
		resetButton.addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		JButton button = (JButton) e.getSource();
		if (button.getName().equals("resetButton")) {
			usernameField.setText("");
			newpasswordField.setText("");
			oldpasswordField.setText("");
			repasswordField.setText("");
			return;
		} else if (button.getName().equals("submitButton")) {
			// 检查输入
			if (checkInput()) {
				int show = showConfirm("是否要修改密码?", "系统提示", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (show == 0) {
					// 修改密码
					this.updatePass();
				} else {
					return;
				}
			} else {
				return;
			}
		}
	}

	/**
	 * 检查输入【用户名、旧密码、新密码、重复密码】
	 * 
	 * @return
	 */
	public boolean checkInput() {
		if (null == usernameField.getText().trim() || "".equals(usernameField.getText().trim())) {
			showMessage("请输入用户名!", "系统提示", JOptionPane.WARNING_MESSAGE);
			return false;
		}
		if (null == oldpasswordField.getText() || "".equals(oldpasswordField.getText().trim())) {
			showMessage("请输入旧密码!", "系统提示", JOptionPane.WARNING_MESSAGE);
			return false;
		}
		if (null == newpasswordField.getText() || "".equals(newpasswordField.getText().trim())) {
			showMessage("请输入新密码!", "系统提示", JOptionPane.WARNING_MESSAGE);
			return false;
		}
		if (null == repasswordField.getText() || "".equals(repasswordField.getText().trim())) {
			showMessage("请输入确认密码!", "系统提示", JOptionPane.WARNING_MESSAGE);
			return false;
		}
		if (!newpasswordField.getText().trim().equals(repasswordField.getText().trim())) {
			showMessage("新密码和确认密码不一致，请重新输入!", "系统提示", JOptionPane.WARNING_MESSAGE);
			return false;
		}
		return true;
	}

	/**
	 * 修改密码
	 */
	public void updatePass() {
		try {
			JSONObject jsonObject = SocketUtils.updatePass(DB.SERVER_IP, Integer.parseInt(DB.SERVER_PORT),
					usernameField.getText().trim(), oldpasswordField.getText().trim(),
					newpasswordField.getText().trim());
			showMessage(jsonObject.getString("msg"), "系统提示", JOptionPane.INFORMATION_MESSAGE);
			if ("true".equals(jsonObject.get("success") + "")) {
				this.dispose();
				parent.dispose();
				new Login();
			} else {
				return;
			}
		} catch (Exception ex) {
			log.error(ex);
			showMessage("修改密码异常!", "系统提示", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void showMessage(String message, String title, int messageType) {
		JLabel label = new JLabel();
		label.setText(message);
		label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		JOptionPane.showMessageDialog(UpdatePwd.this, label, title, messageType);
	}

	public int showConfirm(String message, String title, int optionType, int messageType) {
		JLabel label = new JLabel();
		label.setText(message);
		label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		return JOptionPane.showConfirmDialog(UpdatePwd.this, label, title, optionType, messageType);
	}

	/**
	 * 设置窗口图标
	 */
	public void setWindowIcon() {
		ImageIcon icon = new ImageIcon(getClass().getResource("/icon.png"));
		this.setIconImage(icon.getImage());
	}

}
