package com.rj.soft.ui.comp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import antlr.collections.impl.Vector;
import org.apache.log4j.Logger;

import com.rj.soft.ui.Main;

/**
 * 自定义文件选择器
 * 
 * @author cjy
 * @since 2018年2月5日
 * @version 1.0
 * 
 */
@SuppressWarnings("serial")
public class PopChoose extends JFrame implements MouseListener, ActionListener {

	public static final ImageIcon ICON_COMPUTER = new ImageIcon("computer.gif");

	public static final ImageIcon ICON_DISK = new ImageIcon("disk.gif");

	public static final ImageIcon ICON_FOLDER = new ImageIcon("folder.gif");

	public static final ImageIcon ICON_EXPANDEDFOLDER = new ImageIcon("expandedfolder.gif");

	protected JTree m_tree;

	protected DefaultTreeModel m_model;

	protected JTextField m_display;

	private JMenuBar menuBar;

	private static JMenu dirMenu, fileMenu;

	private JButton confirmButton, cancelButton;

	private JPanel buttonPanel;

	private JFrame frame;

	private static String userName = "";

	@SuppressWarnings("static-access")
	public PopChoose(String menu, final JFrame frame, String userName) {
		super("请选择目录");
		setSize(400, 300);
		this.frame = frame;
		this.userName = userName;
		// 创建菜单
		this.buildMenu(menu);
		DefaultMutableTreeNode top = new DefaultMutableTreeNode(new IconData(ICON_COMPUTER, null, "Computer"));
		File[] roots = File.listRoots();
		for (int k = 0; k < roots.length; k++) {
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(
					new IconData(ICON_DISK, null, new FileNode(roots[k])));
			top.add(node);
			node.add(new DefaultMutableTreeNode(new Boolean(true)));
		}
		m_model = new DefaultTreeModel(top);

		m_tree = new JTree(m_model);
		m_tree.putClientProperty("JTree.lineStyle", "Angled");

		TreeCellRenderer renderer = new IconCellRenderer();
		m_tree.setCellRenderer(renderer);
		m_tree.addTreeExpansionListener(new DirExpansionListener());
		m_tree.addTreeSelectionListener(new DirSelectionListener());
		m_tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		m_tree.setShowsRootHandles(true);
		m_tree.setEditable(false);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.getViewport().add(m_tree);
		getContentPane().add(scrollPane, BorderLayout.CENTER);

		m_display = new JTextField();
		m_display.setEditable(false);
		getContentPane().add(m_display, BorderLayout.NORTH);

		confirmButton = new JButton("确定");
		confirmButton.setName("confirmButton");
		confirmButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		confirmButton.addActionListener(this);

		cancelButton = new JButton("取消");
		cancelButton.setName("cancelButton");
		cancelButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		cancelButton.addActionListener(this);

		buttonPanel = new JPanel();
		buttonPanel.add(confirmButton);
		buttonPanel.add(cancelButton);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		WindowListener winCloser = new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				exitAndClose();
			}
		};
		addWindowListener(winCloser);

		setVisible(true);
	}

	/**
	 * 创建菜单
	 * 
	 * @param menu
	 */
	public void buildMenu(String menu) {
		menuBar = new JMenuBar();

		dirMenu = new JMenu("目录");
		dirMenu.setName("dirMenu");
		dirMenu.addMouseListener(this);

		fileMenu = new JMenu("文件");
		fileMenu.setName("fileMenu");
		if (fileMenu.getName().equals(menu)) {
			fileMenu.setEnabled(false);
		} else {
			dirMenu.setEnabled(false);
		}
		fileMenu.addMouseListener(this);

		menuBar.add(dirMenu);
		menuBar.add(fileMenu);

		this.setLocationRelativeTo(null);
	}

	/**
	 * 鼠标点击事件
	 * 
	 * @param e
	 */
	public void mouseClicked(MouseEvent e) {
		JMenu menu = (JMenu) e.getSource();
		if (menu.getName().equals("dirMenu")) {
			dirMenu.setEnabled(false);
			fileMenu.setEnabled(true);
			new PopChoose("dirMenu", frame, userName);
			dispose();
		} else if (menu.getName().equals("fileMenu")) {
			fileMenu.setEnabled(false);
			dirMenu.setEnabled(true);
			new PopChoose("fileMenu", frame, userName);
			dispose();
		}
	}

	public void mouseEntered(MouseEvent e) {

	}

	public void mouseExited(MouseEvent e) {

	}

	public void mousePressed(MouseEvent e) {

	}

	public void mouseReleased(MouseEvent e) {

	}

	/**
	 * 按钮响应事件
	 * 
	 * @param e
	 */
	public void actionPerformed(ActionEvent e) {
		JButton button = (JButton) e.getSource();
		if (button.getName().equals("confirmButton")) {
			File file = new File(m_display.getText());
			if (file.listFiles() == null || file.listFiles().length == 0) {
				JOptionPane.showMessageDialog(null, "当前选中的文件目录为空!", "标题", JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (file.exists()) {
				Main mainFrame = new Main(userName, m_display.getText() + "@");
				mainFrame.buildInit();
				dispose();
			} else {
				JOptionPane.showMessageDialog(null, "当前选中的文件目录不存在!", "标题", JOptionPane.ERROR_MESSAGE);
				return;
			}
		} else if (button.getName().equals("cancelButton")) {
			dispose();
			Main mainFrame = new Main(userName, "");
			mainFrame.buildInit();
		}
	}

	public void exitAndClose() {
		frame.dispose();
		dispose();
		Main mainFrame = new Main(userName, "");
		mainFrame.buildInit();
	}

	DefaultMutableTreeNode getTreeNode(TreePath path) {
		return (DefaultMutableTreeNode) (path.getLastPathComponent());
	}

	FileNode getFileNode(DefaultMutableTreeNode node) {
		if (node == null)
			return null;
		Object obj = node.getUserObject();
		if (obj instanceof IconData)
			obj = ((IconData) obj).getObject();
		if (obj instanceof FileNode)
			return (FileNode) obj;
		else
			return null;
	}

	class DirExpansionListener implements TreeExpansionListener {

		public void treeExpanded(TreeExpansionEvent event) {
			final DefaultMutableTreeNode node = getTreeNode(event.getPath());
			final FileNode fileNode = getFileNode(node);
			Thread runner = new Thread() {
				public void run() {
					if (null != fileNode && fileNode.expand(node, dirMenu.isEnabled())) {
						Runnable runnable = new Runnable() {
							public void run() {
								m_model.reload(node);
							}
						};
						SwingUtilities.invokeLater(runnable);
					}
				}
			};
			runner.start();
		}

		public void treeCollapsed(TreeExpansionEvent event) {

		}

	}

	class DirSelectionListener implements TreeSelectionListener {

		public void valueChanged(TreeSelectionEvent event) {
			DefaultMutableTreeNode node = getTreeNode(event.getPath());
			FileNode fileNode = getFileNode(node);
			if (null != fileNode) {
				m_display.setText(fileNode.getFile().getAbsolutePath());
			} else {
				m_display.setText("");
			}
		}

	}

}

/**
 * 自定义图标渲染组件
 * 
 * @author cjy
 * @since 2017年7月26日
 * @version 1.0
 * 
 */
@SuppressWarnings("serial")
class IconCellRenderer extends JLabel implements TreeCellRenderer {

	protected Color m_textSelectionColor;

	protected Color m_textNonSelectionColor;

	protected Color m_bkSelectionColor;

	protected Color m_bkNonSelectionColor;

	protected Color m_borderSelectionColor;

	protected boolean m_selected;

	public IconCellRenderer() {
		super();
		m_textSelectionColor = UIManager.getColor("Tree.selectionForeground");
		m_textNonSelectionColor = UIManager.getColor("Tree.textForeground");
		m_bkSelectionColor = UIManager.getColor("Tree.selectionBackground");
		m_bkNonSelectionColor = UIManager.getColor("Tree.textBackground");
		m_borderSelectionColor = UIManager.getColor("Tree.selectionBorderColor");
		setOpaque(false);
	}

	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
			boolean leaf, int row, boolean hasFocus) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		Object obj = node.getUserObject();
		setText(obj.toString());
		if (obj instanceof Boolean)
			setText("Retrieving data...");
		if (obj instanceof IconData) {
			IconData idata = (IconData) obj;
			if (expanded)
				setIcon(idata.getExpandedIcon());
			else
				setIcon(idata.getIcon());
		} else
			setIcon(null);
		setFont(tree.getFont());
		setForeground(selected ? m_textSelectionColor : m_textNonSelectionColor);
		setBackground(selected ? m_bkSelectionColor : m_bkNonSelectionColor);
		m_selected = selected;
		return this;
	}

	public void paintComponent(Graphics g) {
		Color bColor = getBackground();
		Icon icon = getIcon();
		g.setColor(bColor);
		int offset = 0;
		if (icon != null && getText() != null)
			offset = (icon.getIconWidth() + getIconTextGap());
		g.fillRect(offset, 0, getWidth() - 1 - offset, getHeight() - 1);
		if (m_selected) {
			g.setColor(m_borderSelectionColor);
			g.drawRect(offset, 0, getWidth() - 1 - offset, getHeight() - 1);
		}
		super.paintComponent(g);
	}

}

/**
 * 自定义文件节点
 * 
 * @author cjy
 * @since 2017年7月26日
 * @version 1.0
 * 
 */
class FileNode {

	private static Logger logger = Logger.getLogger(FileNode.class);

	protected File m_file;

	public FileNode(File file) {
		m_file = file;
	}

	public File getFile() {
		return m_file;
	}

	public String toString() {
		return m_file.getName().length() > 0 ? m_file.getName() : m_file.getPath();
	}

	public boolean expand(DefaultMutableTreeNode parent, boolean tag) {
		DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) parent.getFirstChild();
		if (null == childNode)
			return false;
		Object obj = childNode.getUserObject();
		if (!(obj instanceof Boolean))
			return false;
		parent.removeAllChildren();
		File[] files = listFiles();
		if (null == files)
			return true;
		Vector vec = new Vector();
		for (int k = 0; k < files.length; k++) {
			File f = files[k];
			if (!tag) {
				if (!(f.isDirectory()))
					continue;
			}
			FileNode newNode = new FileNode(f);
			boolean isAdded = false;
			for (int i = 0; i < vec.size(); i++) {
				FileNode node = (FileNode) vec.elementAt(i);
				if (newNode.compareTo(node) < 0) {
					vec.setElementAt(newNode, i);
					isAdded = true;
					break;
				}
			}
			if (!isAdded)
				vec.appendElement(newNode);
		}
		for (int i = 0; i < vec.size(); i++) {
			FileNode fileNode = (FileNode) vec.elementAt(i);
			IconData data = new IconData(PopChoose.ICON_FOLDER, PopChoose.ICON_EXPANDEDFOLDER, fileNode);
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(data);
			parent.add(node);
			File tt = new File(m_file + "\\" + node);
			if (tag) {
				if (tt.isDirectory()) {
					node.add(new DefaultMutableTreeNode(new Boolean(true)));
				}
			} else {
				if (fileNode.hasSubDirs()) {
					node.add(new DefaultMutableTreeNode(new Boolean(true)));
				}
			}
		}
		return true;
	}

	public boolean hasSubDirs() {
		File[] files = listFiles();
		if (null == files)
			return false;
		for (int k = 0; k < files.length; k++) {
			if (files[k].isDirectory()) {
				return true;
			}
		}
		return false;
	}

	public int compareTo(FileNode src) {
		return m_file.getName().compareToIgnoreCase(src.m_file.getName());
	}

	protected File[] listFiles() {
		if (!m_file.isDirectory())
			return null;
		try {
			return m_file.listFiles();
		} catch (Exception e) {
			logger.error(e);
			JOptionPane.showMessageDialog(null, "读取文件: " + m_file.getAbsolutePath() + " 出错!", "Warning",
					JOptionPane.WARNING_MESSAGE);
			return null;
		}
	}

}

/**
 * 自定义图标数据
 * 
 * @author cjy
 * @since 2017年7月26日
 * @version 1.0
 * 
 */
class IconData {

	protected Icon m_icon;

	protected Icon m_expandedIcon;

	protected Object m_data;

	public IconData(Icon icon, Object data) {
		m_icon = icon;
		m_expandedIcon = null;
		m_data = data;
	}

	public IconData(Icon icon, Icon expandedIcon, Object data) {
		m_icon = icon;
		m_expandedIcon = expandedIcon;
		m_data = data;
	}

	public Icon getIcon() {
		return m_icon;
	}

	public Icon getExpandedIcon() {
		return m_expandedIcon != null ? m_expandedIcon : m_icon;
	}

	public Object getObject() {
		return m_data;
	}

	public String toString() {
		return m_data.toString();
	}

}