package com.rj.soft.ui.comp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.log4j.Logger;

import com.rj.soft.global.DB;
import com.rj.soft.global.DataBox;
import com.rj.soft.utils.CommonUtils;
import com.rj.soft.utils.DateUtils;
import com.rj.soft.utils.SocketUtils;

/**
 * 自定义面板
 *
 * @author cjy
 * @version 1.0
 * @since 2018年2月5日
 */
@SuppressWarnings({"serial", "unchecked", "deprecation", "rawtypes"})
public class CustomPanel extends JPanel {

    private static Logger log = Logger.getLogger(CustomPanel.class);

    /**
     * 自定义表格模型
     */
    private final CustomTableModel tableModel = new CustomTableModel();

    private final TableRowSorter sorter = new TableRowSorter(tableModel);

    private final JTable table;

    /**
     * 任务集合
     */
    private final HashSet set = new HashSet();

    /**
     * Swing线程进度列表
     */
    public List workList = new ArrayList();

    /**
     * 进度集合
     */
    public Map<Integer, Long> processMap = new HashMap<Integer, Long>();

    private JLabel finishedLabel, unfinishedLabel;

    private int currentNum = 0;

    /**
     * boolean结果集合
     */
    public boolean[] resultList;

    /**
     * 文件名列表
     */
    public static String[] fileNames;

    /**
     * 文件路径列表
     */
    private static String[] filePaths;

    /**
     * 本地路径
     */
    private String localURL = "";

    /**
     * 选择文件路径
     */
    private String chooseFileURL = "";

    /**
     * 当前登陆用户名
     */
    private String userName = "";

    /**
     * 面前切换标识：true 下载 | false 上传
     */
    private boolean tag = false;

    public CustomPanel(JSONArray jsonArray, JLabel finishedLabel, JLabel unfinishedLabel, String url, String userName)
            throws IOException {
        super(new BorderLayout());
        if ("".equals(url)) {
            localURL = "E:\\service\\";
            tag = true;
        } else {
            localURL = url + "\\";
            tag = false;
        }
        resultList = new boolean[jsonArray.size()];
        this.userName = userName;
        this.finishedLabel = finishedLabel;
        this.unfinishedLabel = unfinishedLabel;

        // 初始化json数据
        initData(jsonArray);

        table = new JTable(tableModel) {

            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component component = super.prepareRenderer(renderer, row, column);
                if (isRowSelected(row)) {
                    component.setForeground(getSelectionForeground());
                    component.setBackground(getSelectionBackground());
                } else {
                    component.setForeground(getForeground());
                }
                return component;
            }

            public JPopupMenu getComponentPopupMenu() {
                return addPopup();
            }
        };

        table.setRowSorter(sorter);
        // 滚动条
        JScrollPane scrollPane = new JScrollPane(table);
        // 背景色
        scrollPane.getViewport().setBackground(Color.black);
        // 弹出菜单
        table.setComponentPopupMenu(new JPopupMenu());
        // 是否始终大到足以填充封闭视口的高度
        table.setFillsViewportHeight(true);
        // 将单元格间距的高度和宽度设置为指定的Dimension
        table.setIntercellSpacing(new Dimension());
        // 是否绘制单元格间的水平线
        table.setShowHorizontalLines(true);
        // 是否绘制单元格间的垂直线
        table.setShowVerticalLines(false);
        // 停止编辑时重新定义焦点，避免TableCellEditor丢失数据
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        // 表示JTable中列的所有属性，如宽度、大小可调整性、最小和最大宽度等。
        TableColumn column = table.getColumnModel().getColumn(0);
        column.setMaxWidth(540);
        column.setMinWidth(540);
        column.setPreferredWidth(540);
        column.setCellRenderer(new CustomCellRenderer());
        column = table.getColumnModel().getColumn(1);
        // 绘制此列各值的TableCellRenderer
        column.setCellRenderer(new ProgressRenderer());
        // 添加按钮
        add(scrollPane, BorderLayout.CENTER);
        setPreferredSize(new Dimension(320, 110));
    }

    /**
     * 表格右键菜单
     *
     * @return
     */
    private JPopupMenu addPopup() {
        JPopupMenu popupMenu = new JPopupMenu();
        Action act = new CustomAction("续传", null, "continueAction");
        int[] selection = table.getSelectedRows();
        if (selection == null || selection.length <= 0) {
            act.setEnabled(false);
        }
        popupMenu.add(act);
        return popupMenu;
    }

    /**
     * 处理方法
     *
     * @param index
     */
    public void processMethod(final int index) {
        // 初始设置为false
        resultList[index] = false;
        // 添加到进度集合中
        processMap.put(index, new Long(0));

        // 初始化SwingWorker任务
        SwingWorker<Integer, Integer> worker = new SwingWorker<Integer, Integer>() {

            // 最大任务数
            private int taskSize = 200;

            private int sleepSize = 30;

            private int readSize = 1024;

            /**
             * 后台进程
             */
            protected Integer doInBackground() throws Exception {
                // 前端传递过来的本地路径结合当前文件路径重新拼接
                String tempURL = localURL.contains(filePaths[index]) ? localURL : localURL + filePaths[index];

                if (!tempURL.contains("service")) {// 上传
                    System.out.println("上传文件路径：" + tempURL);

                    StringBuffer sb = new StringBuffer();
                    // 读取本地文件
                    File file = new File(tempURL);
                    int length = (int) file.length();
                    FileInputStream fis = null;
                    BufferedInputStream bis = null;
                    Socket socket = null;
                    InputStream in = null;
                    OutputStream out = null;
                    BufferedReader br = null;
                    try {
                        socket = new Socket(DB.SERVER_IP, Integer.parseInt(DB.SERVER_PORT));
                        out = socket.getOutputStream();
                        // 写入上传请求头
                        out.write(new String("POST /uploadFile\r\n").getBytes());
                        out.write(new String("Content-Length: " + length + "\r\n").getBytes());
                        out.write(new String("UserName: " + userName + "\r\n").getBytes());
                        out.write(new String("Time: " + DateUtils.getNowDate() + "\r\n").getBytes());
                        out.write(new String("FilePath: " + filePaths[index] + "\r\n").getBytes("GBK"));
                        out.write(new String("\r\n").getBytes());

                        log.info("当前用户：" + userName + "，正在上传文件：" + filePaths[index] + "，大小：" + length);

                        fis = new FileInputStream(file);
                        bis = new BufferedInputStream(fis);
                        byte[] buf = new byte[readSize];
                        int total = 0, size = 0;
                        // 百分比
                        int percent = 0;
                        // 写入上传字节流
                        while ((size = bis.read(buf, 0, readSize)) != -1) {
                            total += size;
                            out.write(buf, 0, size);
                            for (int i = 0; i < 10; i++) {
                                if (i == 9) {
                                    percent = (int) (100 * total / (long) length);
                                    publish(percent);
                                }
                            }
                        }

                        // socket返回
                        in = socket.getInputStream();
                        br = new BufferedReader(new InputStreamReader(in, "GBK"));
                        String line = "";
                        br.readLine();
                        while ((line = br.readLine()) != null) {
                            sb.append(line + "\n");
                        }

                        log.info("当前用户：" + userName + "，上传文件：" + filePaths[index] + "，大小：" + length + " 完成!");

                        try {
                            fis.close();
                            bis.close();
                            in.close();
                            out.close();
                            br.close();
                            socket.close();
                        } catch (Exception e) {
                            log.error(e);
                            e.printStackTrace();
                        }
                        // 删除本地文件
                        file.delete();
                    } catch (Exception e) {
                        log.error(e);
                        // 断点续传标识，出错时递减
                        currentNum--;
                        e.printStackTrace();
                    } finally {
                        fis.close();
                        bis.close();
                        in.close();
                        out.close();
                        br.close();
                        socket.close();
                    }
                    return sleepSize * length;
                } else {// 下载

                    if (!"".equals(chooseFileURL)) {
                        tempURL = chooseFileURL + "/" + filePaths[index];
                        File file = new File(chooseFileURL);
                        if (!file.isDirectory()) {
                            file.mkdirs();
                        }
                    } else {
                        tempURL = DB.DEFAULT_FILE_PATH + filePaths[index];
                    }
                    System.out.println("下载文件路径：" + tempURL);

                    Socket socket = null;
                    InputStream in = null;
                    OutputStream out = null;
                    FileOutputStream fos = null;
                    DataInputStream dis = null;
                    // 断点续传标识
                    boolean breakPoint = false;
                    // 下载文件的起始位置
                    long startIndex = 0;
                    byte[] buf = new byte[readSize];
                    long total = 0;
                    int size = 0;
                    // 百分比
                    int percent = 0;
                    try {
                        socket = new Socket(DB.SERVER_IP, Integer.parseInt(DB.SERVER_PORT));
                        out = socket.getOutputStream();
                        // 写入下载请求头
                        out.write(new String("GET /downloadFile\r\n").getBytes());
                        out.write(new String("UserName: " + userName + "\r\n").getBytes());
                        out.write(new String("FilePath: " + filePaths[index] + "\r\n").getBytes("GBK"));
                        out.write(new String("\r\n").getBytes());

                        File file = new File(tempURL + ".temp");
                        if (file.exists()) {
                            // 获取已存在文件的大小，作为续传的起始位置
                            startIndex = file.length();
                            breakPoint = true;
                        }
                        // 设置append为true，可追加
                        fos = new FileOutputStream(file, true);
                        in = socket.getInputStream();
                        dis = new DataInputStream(in);
                        // 获取待下载文件的完整长度（服务器返回）
                        String contentLength = dis.readLine();
                        long length = 0;
                        if (null != contentLength && !"".equals(contentLength)) {
                            length = Integer
                                    .parseInt(contentLength.substring(contentLength.indexOf("Content-Length: ") + 16));
                        }
                        log.info("当前用户：" + userName + "，正在下载文件：" + filePaths[index] + "，大小：" + length);
                        if (length == 0) {
                            publish(100);
                            file.renameTo(new File(tempURL));
                        } else {
                            while ((size = dis.read(buf, 0, readSize)) != -1) {
                                total += size;
                                if (total < startIndex) {
                                    percent = (int) (100 * startIndex / length);
                                    publish(percent);
                                    continue;
                                } else {
                                    if (breakPoint) {
                                        // 断点续传
                                        int breakStart = Integer.parseInt((total - startIndex) + "");
                                        fos.write(buf, 0, breakStart);
                                        breakPoint = false;
                                    } else {
                                        fos.write(buf, 0, size);
                                    }
                                }
                                for (int i = 0; i < 1000; i++) {
                                    if (i == 999) {
                                        percent = (int) (100 * total / length);
                                        publish(percent);
                                    }
                                }
                                if (total == length) {
                                    break;
                                }
                            }
                        }
                        if (null != fos)
                            fos.close();
                        // 重命名文件
                        file.renameTo(new File(tempURL));
                        log.info("当前用户：" + userName + "，下载文件：" + filePaths[index] + "，大小：" + length + " 完成!");
                    } catch (Exception e) {
                        log.error(e);
                        processMap.put(index, new Long(total));
                        currentNum--;
                        e.printStackTrace();
                    } finally {
                        try {
                            if (null != fos)
                                fos.close();
                            in.close();
                            dis.close();
                            out.close();
                            socket.close();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            log.error(ex);
                        }
                    }
                    return sleepSize * taskSize;
                }
            }

            /**
             * 进行中处理方法
             */
            protected void process(List data) {
                for (Object value : data) {
                    tableModel.setValueAt((Integer) value, index, 1);
                }
                // 传送变更事件给指定行列（进度）
                tableModel.fireTableCellUpdated(index, 1);
            }

            /**
             * 完成后处理方法
             */
            protected void done() {
                resultList[index] = true;
                // 当前完成传输数量递增
                currentNum++;
                DB.THREADNUM++;
                finishedLabel.setText("已完成: " + currentNum);
                unfinishedLabel.setText("未完成: " + (DB.TOTALTNUM - currentNum));

                // 截取当前索引
                int tempIndex = index;
                tempIndex += 10;
                if (tempIndex < fileNames.length) {
                    // 从Swing线程列表中取出执行
                    SwingWorker<Integer, Integer> tempWorker = (SwingWorker<Integer, Integer>) (workList
                            .get(tempIndex));
                    tempWorker.execute();
                }
                if (!tag) {
                    log.info("批次任务[" + DB.curTaskId + "]，总任务数：" + DB.TOTALTNUM + "，已完成任务数：" + currentNum);
                    // 两者一致表示当前任务执行完毕
                    if (DB.TOTALTNUM == currentNum) {
                        DataBox dataBox = CommonUtils.initDataBox();
                        if (dataBox.isOpen()) {// 开启批次控制
                            String taskId = DB.curTaskId;
                            log.info("批次[" + taskId + "]->完成："
                                    + SocketUtils.batchControl(dataBox, "DONE", taskId, false));
                            DB.curTaskId = "";
                        }
                    }
                }
            }

        };

        tableModel.addWorker(worker, new ProgressBean((String) fileNames[index], 0));
        workList.add(worker);
    }

    /**
     * 对json数据初始化
     *
     * @param jsonArray
     */
    public void initData(JSONArray jsonArray) {
        String names = "";
        String paths = "";
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            // 以特殊标识连接，用split分隔
            names += (String) jsonObject.get("fileName") + "@";
            paths += (String) jsonObject.get("filePath") + "@";
        }
        fileNames = names.split("@");
        filePaths = paths.split("@");
    }

    /**
     * 刷新选择文件路径
     *
     * @param url
     */
    public void refreshChooseURL(String url) {
        this.chooseFileURL = url;
    }

    /**
     * 自定义Action
     *
     * @author Administrator
     */
    class CustomAction extends AbstractAction {

        /**
         * 操作
         */
        private String operation;

        public CustomAction(String text, Icon icon, String operation) {
            super(text, icon);
            this.operation = operation;
        }

        public void actionPerformed(ActionEvent e) {
            if ("continueAction".equalsIgnoreCase(operation)) {// 续传
                continuedActionPerformed(e);
            } else if ("cancelAction".equalsIgnoreCase(operation)) {// 取消
                cancelActionPerformed(e);
            } else if ("deleteAction".equalsIgnoreCase(operation)) {// 删除
                deleteActionPerformed(e);
            }
        }

        public String getOperation() {
            return operation;
        }

        public void setOperation(String operation) {
            this.operation = operation;
        }

    }

    /**
     * 续传事件（多选）
     *
     * @param evt
     */
    public synchronized void continuedActionPerformed(ActionEvent evt) {
        int[] selection = table.getSelectedRows();
        if (null == selection || selection.length <= 0) {
            return;
        }
        for (int i = 0; i < selection.length; i++) {
            int index = table.convertRowIndexToModel(selection[i]);
            System.out.println("序列号：" + index);
            set.add(index);
            SwingWorker<Integer, Integer> worker = (SwingWorker<Integer, Integer>) workList.get(index);
            worker.execute();
            System.out.println("进度：" + worker.getProgress());
        }
        table.repaint();
    }

    /**
     * 取消事件
     *
     * @param evt
     */
    public synchronized void cancelActionPerformed(ActionEvent evt) {
        int[] selection = table.getSelectedRows();
        if (null == selection || selection.length <= 0) {
            return;
        }
        for (int i = 0; i < selection.length; i++) {
            int index = table.convertRowIndexToModel(selection[i]);
            SwingWorker<Integer, Integer> worker = tableModel.getWorker(index);
            if (null != worker && !worker.isDone()) {
                worker.cancel(true);
            }
            worker = null;
        }
        table.repaint();
    }

    /**
     * 删除事件
     *
     * @param evt
     */
    public synchronized void deleteActionPerformed(ActionEvent evt) {
        int[] selection = table.getSelectedRows();
        if (null == selection || selection.length <= 0) {
            return;
        }
        for (int i = 0; i < selection.length; i++) {
            int index = table.convertRowIndexToModel(selection[i]);
            System.out.println("序列号：" + index);
            set.add(index);
            SwingWorker worker = tableModel.getWorker(index);
            if (null != worker && !worker.isDone()) {
                // worker.cancel(true);
            }
            worker = null;
        }
        final RowFilter<CustomTableModel, Integer> filter = new RowFilter<CustomTableModel, Integer>() {
            public boolean include(Entry<? extends CustomTableModel, ? extends Integer> entry) {
                Integer index = entry.getIdentifier();
                return !set.contains(index);
            }
        };
        sorter.setRowFilter(filter);
        table.repaint();
    }

}
