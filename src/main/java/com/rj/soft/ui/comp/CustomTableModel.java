package com.rj.soft.ui.comp;

import java.awt.Component;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * 自定义表格模型
 * 
 * @author cjy
 * @since 2018年2月5日
 * @version 1.0
 * 
 */
@SuppressWarnings({ "serial", "rawtypes" })
public class CustomTableModel extends DefaultTableModel {

	private static final ColumnContext[] columnArray = { new ColumnContext("文件名", String.class, false),
			new ColumnContext("进度", Integer.class, false) };

	private final Map<Integer, SwingWorker> workerMap = new HashMap<Integer, SwingWorker>();

	private int index = 0;

	public void addWorker(SwingWorker worker, ProgressBean progressBean) {
		Object[] rowData = { progressBean.getName(), progressBean.getProgress() };
		super.addRow(rowData);
		workerMap.put(index, worker);
		index += 1;
	}

	public synchronized SwingWorker getWorker(int identifier) {
		Integer key = (Integer) getValueAt(identifier, 0);
		return workerMap.get(key);
	}

	public ProgressBean getProgress(int identifier) {
		return new ProgressBean((String) getValueAt(identifier, 0), (Integer) getValueAt(identifier, 0));
	}

	public boolean isCellEditable(int row, int col) {
		return columnArray[col].isEditable;
	}

	public Class<?> getColumnClass(int modelIndex) {
		return columnArray[modelIndex].columnClass;
	}

	public int getColumnCount() {
		return columnArray.length;
	}

	public String getColumnName(int modelIndex) {
		return columnArray[modelIndex].columnName;
	}

	/**
	 * 自定义表格行
	 * 
	 * @author cjy
	 * @since 2017年7月25日
	 * @version 1.0
	 * 
	 */
	private static class ColumnContext {
		public final String columnName;
		public final Class columnClass;
		public final boolean isEditable;

		public ColumnContext(String columnName, Class columnClass, boolean isEditable) {
			this.columnName = columnName;
			this.columnClass = columnClass;
			this.isEditable = isEditable;
		}
	}

}

/**
 * 进度条实体Bean
 * 
 * @author cjy
 * @since 2017年7月25日
 * @version 1.0
 * 
 */
class ProgressBean {

	private String name;

	private Integer progress;

	public ProgressBean(String name, Integer progress) {
		// TODO Auto-generated constructor stub
		this.name = name;
		this.progress = progress;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getProgress() {
		return progress;
	}

	public void setProgress(Integer progress) {
		this.progress = progress;
	}

}

/**
 * 自定义表格进度条组件
 * 
 * @author cjy
 * @since 2017年7月25日
 * @version 1.0
 * 
 */
@SuppressWarnings("serial")
class ProgressRenderer extends DefaultTableCellRenderer {

	private final JProgressBar progressBar = new JProgressBar(0, 100);

	public ProgressRenderer() throws IOException {
		super();
		setOpaque(true);
		progressBar.setStringPainted(true);
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		Integer i = (Integer) value;
		String text = "完成";
		setHorizontalAlignment(SwingConstants.CENTER);
		if (i < 0) {

		} else if (i < 100) {
			progressBar.setValue(i);
			return progressBar;
		}

		if (column == 0) {
			setHorizontalAlignment(SwingConstants.CENTER);
		}
		super.getTableCellRendererComponent(table, text, isSelected, hasFocus, row, column);
		return this;
	}

}

/**
 * 自定义表格渲染组件
 * 
 * @author cjy
 * @since 2017年7月25日
 * @version 1.0
 * 
 */
@SuppressWarnings("serial")
class CustomCellRenderer extends DefaultTableCellRenderer {

	public CustomCellRenderer() throws IOException {
		super();
		setOpaque(true);
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		if (column == 0) {
			setHorizontalAlignment(SwingConstants.LEFT);
		}
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		return this;
	}

}
