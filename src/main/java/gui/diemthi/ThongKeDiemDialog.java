package gui.diemthi;

import bus.DiemThiBUS;
import bus.DiemThiBUS.Stats;
import gui.component.CustomButton;
import gui.style.UIConstants;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class ThongKeDiemDialog extends JDialog {

    private final DiemThiBUS bus;

    private JComboBox<String> cboPhuongThuc, cboMon;
    private DefaultTableModel statsModel;
    private JPanel            pnlChart;
    private JFreeChart        currentChart;

    public ThongKeDiemDialog(Window parent, DiemThiBUS bus) {
        super(parent, "Thống kê điểm thi", ModalityType.APPLICATION_MODAL);
        this.bus = bus;
        setSize(720, 580);
        setLocationRelativeTo(parent);
        setResizable(true);
        initUI();
        loadStats(); // khởi tạo với lựa chọn mặc định
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setBackground(UIConstants.BACKGROUND_COLOR);
        root.setBorder(new EmptyBorder(14, 16, 10, 16));
        setContentPane(root);

        // ── Thanh lọc ──
        JPanel pnlFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        pnlFilter.setOpaque(false);

        cboPhuongThuc = new JComboBox<>(new String[]{"THPT (4)", "VSAT (3)", "ĐGNL (2)"});
        cboMon        = new JComboBox<>();
        cboPhuongThuc.setFont(UIConstants.FONT_NORMAL);
        cboMon.setFont(UIConstants.FONT_NORMAL);
        cboPhuongThuc.setPreferredSize(new Dimension(100, 32));
        cboMon.setPreferredSize(new Dimension(130, 32));

        updateMonCombo("4");  // mặc định THPT
        cboPhuongThuc.addActionListener(e -> {
            String maPt = getMaPhuongThuc((String) cboPhuongThuc.getSelectedItem());
            updateMonCombo(maPt);
            loadStats();
        });
        cboMon.addActionListener(e -> loadStats());

        CustomButton btnXuatAnh = new CustomButton("Xuất PNG", UIConstants.PRIMARY_COLOR);
        btnXuatAnh.setPreferredSize(new Dimension(110, 32));
        btnXuatAnh.addActionListener(e -> exportPng());

        pnlFilter.add(new JLabel("Phương thức:") {{ setFont(UIConstants.FONT_BOLD); }});
        pnlFilter.add(cboPhuongThuc);
        pnlFilter.add(Box.createHorizontalStrut(10));
        pnlFilter.add(new JLabel("Môn:") {{ setFont(UIConstants.FONT_BOLD); }});
        pnlFilter.add(cboMon);
        pnlFilter.add(Box.createHorizontalStrut(20));
        pnlFilter.add(btnXuatAnh);
        root.add(pnlFilter, BorderLayout.NORTH);

        // ── Bảng thống kê ──
        String[] statsCols = {"Chỉ số", "Giá trị"};
        statsModel = new DefaultTableModel(statsCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tblStats = new JTable(statsModel);
        tblStats.setFont(UIConstants.FONT_NORMAL);
        tblStats.setRowHeight(26);
        tblStats.getTableHeader().setFont(UIConstants.FONT_BOLD);
        tblStats.getTableHeader().setBackground(UIConstants.TABLE_HEADER_COLOR);
        tblStats.getTableHeader().setForeground(Color.WHITE);
        tblStats.getColumnModel().getColumn(0).setPreferredWidth(140);
        tblStats.getColumnModel().getColumn(1).setPreferredWidth(100);
        JScrollPane scrStats = new JScrollPane(tblStats);
        scrStats.setPreferredSize(new Dimension(260, 0));
        scrStats.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_COLOR),
                "Số liệu thống kê", TitledBorder.LEFT, TitledBorder.TOP,
                UIConstants.FONT_BOLD, UIConstants.TABLE_HEADER_COLOR));

        // ── Histogram panel ──
        pnlChart = new JPanel(new BorderLayout());
        pnlChart.setBackground(Color.WHITE);
        pnlChart.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_COLOR),
                "Phổ điểm", TitledBorder.LEFT, TitledBorder.TOP,
                UIConstants.FONT_BOLD, UIConstants.TABLE_HEADER_COLOR));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrStats, pnlChart);
        split.setDividerLocation(270);
        split.setResizeWeight(0);
        split.setBorder(null);
        root.add(split, BorderLayout.CENTER);

        // ── Đóng ──
        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlBtn.setOpaque(false);
        CustomButton btnClose = new CustomButton("Đóng", UIConstants.GRAY_COLOR);
        btnClose.setPreferredSize(new Dimension(90, 34));
        btnClose.addActionListener(e -> dispose());
        pnlBtn.add(btnClose);
        root.add(pnlBtn, BorderLayout.SOUTH);
    }

    private void updateMonCombo(String pt) {
        cboMon.removeAllItems();
        for (String mon : DiemThiBUS.getMonListForPhuongThuc(pt))
            cboMon.addItem(mon);
    }

    private void loadStats() {
        String pt = getMaPhuongThuc((String) cboPhuongThuc.getSelectedItem());
        String mon = (String) cboMon.getSelectedItem();
        if (pt == null || mon == null) return;

        Stats stats = bus.getStats(pt, mon);
        statsModel.setRowCount(0);
        statsModel.addRow(new Object[]{"Số lượng", stats.count});
        statsModel.addRow(new Object[]{"Điểm thấp nhất", f(stats.min)});
        statsModel.addRow(new Object[]{"Điểm cao nhất",  f(stats.max)});
        statsModel.addRow(new Object[]{"Trung bình",     f(stats.avg)});
        statsModel.addRow(new Object[]{"Trung vị",       f(stats.median)});
        statsModel.addRow(new Object[]{"Độ lệch chuẩn",  f(stats.stdDev)});

        buildHistogram(pt, mon);
    }

    private void buildHistogram(String pt, String mon) {
        pnlChart.removeAll();

        double[] values = bus.getRawValues(pt, mon);

        if (values.length < 2) {
            JLabel lbl = new JLabel("Không đủ dữ liệu để vẽ biểu đồ", SwingConstants.CENTER);
            lbl.setFont(UIConstants.FONT_NORMAL);
            pnlChart.add(lbl, BorderLayout.CENTER);
            pnlChart.revalidate(); pnlChart.repaint();
            return;
        }

        // Tính binSize theo phương thức
        double binSize = "VSAT".equals(pt) ? 5.0 : "DGNL".equals(pt) ? 10.0 : 0.5;
        double min = values[0], max = values[0];
        for (double v : values) { if (v < min) min = v; if (v > max) max = v; }
        if (max == min) max = min + binSize;
        int bins = (int) Math.ceil((max - min) / binSize);
        if (bins < 1) bins = 1;
        if (bins > 200) bins = 200;

        HistogramDataset dataset = new HistogramDataset();
        dataset.addSeries(mon, values, bins);

        JFreeChart chart = ChartFactory.createHistogram(
                "Phổ điểm " + mon + " (" + pt + ")",
                mon, "Số thí sinh", dataset,
                PlotOrientation.VERTICAL, false, true, false);
        chart.getPlot().setBackgroundPaint(Color.WHITE);
        chart.setBackgroundPaint(Color.WHITE);

        currentChart = chart;

        ChartPanel cp = new ChartPanel(chart);
        cp.setPreferredSize(new Dimension(400, 300));
        pnlChart.add(cp, BorderLayout.CENTER);
        pnlChart.revalidate(); pnlChart.repaint();
    }

    private void exportPng() {
        if (currentChart == null) {
            JOptionPane.showMessageDialog(this, "Chưa có biểu đồ!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("pho_diem.png"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                ChartUtils.saveChartAsPNG(fc.getSelectedFile(), currentChart, 800, 500);
                JOptionPane.showMessageDialog(this, "Xuất ảnh thành công!", "OK", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xuất: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String f(double val) {
        return String.format("%.2f", val);
    }
    //HELPER
    private String getMaPhuongThuc(String display) {
        if (display == null) return "4";
        if (display.contains("3")) return "3";
        if (display.contains("2")) return "2";
        return "4"; // mặc định THPT
    }
}
