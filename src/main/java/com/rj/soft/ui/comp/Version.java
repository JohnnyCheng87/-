package com.rj.soft.ui.comp;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

/**
 * 版本信息
 * 
 * @author cjy
 * @since 2018年8月31日
 * @version 1.5
 * 
 */
@SuppressWarnings("serial")
public class Version extends JFrame implements ActionListener {

	private JLabel nameLabel, versionLabel, releaseLabel, companyLabel;

	public Version() {
		setTitle("版本信息");
		setWindowIcon();
		this.setPreferredSize(new Dimension(300, 250));
		setSize(300, 250);
		setLayout(null);
		initUI();
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
		setResizable(false);
	}

	public void actionPerformed(ActionEvent e) {
		// TODO
	}

	/**
	 * 初始化界面
	 */
	public void initUI() {
		nameLabel = new JLabel("数据交换客户端");
		nameLabel.setFont(new Font("微软雅黑", Font.ITALIC, 15));
		nameLabel.setBounds(50, 20, 200, 30);
		this.getContentPane().add(nameLabel);

		versionLabel = new JLabel("当前版本：v3.0");
		versionLabel.setFont(new Font("微软雅黑", Font.ITALIC, 15));
		versionLabel.setBounds(50, 70, 200, 30);
		this.getContentPane().add(versionLabel);

		releaseLabel = new JLabel("发布时间：2019年4月19日");
		releaseLabel.setFont(new Font("微软雅黑", Font.ITALIC, 15));
		releaseLabel.setBounds(50, 120, 200, 30);
		this.getContentPane().add(releaseLabel);

		companyLabel = new JLabel("榕基软件 1993-2019");
		companyLabel.setFont(new Font("微软雅黑", Font.ITALIC, 15));
		companyLabel.setBounds(50, 170, 200, 30);
		this.getContentPane().add(companyLabel);
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
				new Version();
			}
		});
	}

}
