package gui.nganhtohop;

import bus.NganhToHopBUS;
import entity.NganhToHop;
import gui.component.CustomButton;
import gui.component.CustomTable;
import gui.component.CustomTextField;
import gui.style.UIConstants;
import util.ExcelUtil;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

public class QuanLyNganhToHopPanel extends JPanel {

    private final NganhToHopBUS bus;

    private CustomTextField txtSearch;
    private CustomButton    btnSearch, btnReset;
    private CustomButton    btnDelete, btnImport;

    private CustomTable       tblNganhToHop;
    private DefaultTableModel tableModel;

    private CustomButton btnPrev, btnNext;
    private JLabel       lblPageInfo, lblTotalRecords;

    private int    currentPage    = 1;
    private int    totalPages     = 1;
    private String currentKeyword = "";

    public QuanLyNganhToHopPanel() {
        this.bus = new NganhToHopBUS();

        setLayout(new BorderLayout(0, 0));
        setBackground(UIConstants.BACKGROUND_COLOR);
        setOpaque(true);

        buildNorthArea();
        buildTable();
        buildFooter();
        setupEvents();

        loadData();
    }

    // ── HEADER + TOOLBAR ──
    private void buildNorthArea() {
        JPanel pnlNorth = new JPanel(new BorderLayout());
        pnlNorth.setOpaque(false);

        // ── Header ──
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(UIConstants.PRIMARY_COLOR);
        pnlHeader.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel lblTitle = new JLabel("QUẢN LÝ NGÀNH - TỔ HỢP");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);

        lblTotalRecords = new JLabel("Đang tải...");
        lblTotalRecords.setFont(UIConstants.FONT_NORMAL);
        lblTotalRecords.setForeground(new Color(189, 215, 238));

        pnlHeader.add(lblTitle, BorderLayout.WEST);
        pnlHeader.add(lblTotalRecords, BorderLayout.EAST);

        // ── Toolbar ──
        JPanel pnlToolbar = new JPanel(new BorderLayout(10, 0));
        pnlToolbar.setOpaque(false);
        pnlToolbar.setBorder(new EmptyBorder(10, 15, 8, 15));

        // Search
        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlSearch.setOpaque(false);

        JLabel lblSearch = new JLabel("Tìm kiếm:");
        lblSearch.setFont(UIConstants.FONT_BOLD);

        txtSearch = new CustomTextField(22);
        txtSearch.setPreferredSize(new Dimension(260, 36));
        txtSearch.setToolTipText("Nhập mã ngành hoặc mã tổ hợp");

        btnSearch = new CustomButton("Tìm", UIConstants.PRIMARY_COLOR);
        btnSearch.setPreferredSize(new Dimension(100, 36));

        btnReset = new CustomButton("Xóa lọc", new Color(120, 120, 120));
        btnReset.setPreferredSize(new Dimension(110, 36));

        pnlSearch.add(lblSearch);
        pnlSearch.add(txtSearch);
        pnlSearch.add(btnSearch);
        pnlSearch.add(btnReset);

        // Actions
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlActions.setOpaque(false);

        btnDelete = new CustomButton("Xóa",      UIConstants.DANGER_COLOR);
        btnImport = new CustomButton("Import",   UIConstants.TEAL_COLOR);

        for (CustomButton b : new CustomButton[]{btnDelete, btnImport}) {
            b.setPreferredSize(new Dimension(118, 36));
            pnlActions.add(b);
        }

        pnlToolbar.add(pnlSearch,  BorderLayout.WEST);
        pnlToolbar.add(pnlActions, BorderLayout.EAST);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(200, 200, 200));

        JPanel pnlToolbarWrapper = new JPanel(new BorderLayout());
        pnlToolbarWrapper.setOpaque(false);
        pnlToolbarWrapper.add(pnlToolbar, BorderLayout.CENTER);
        pnlToolbarWrapper.add(sep,        BorderLayout.SOUTH);

        pnlNorth.add(pnlHeader,         BorderLayout.NORTH);
        pnlNorth.add(pnlToolbarWrapper, BorderLayout.CENTER);

        add(pnlNorth, BorderLayout.NORTH);
    }

    // ── TABLE ──
    private void buildTable() {
        String[] cols = {
            "ID", "Mã Ngành", "Mã Tổ Hợp",
            "Môn 1", "HS 1", "Môn 2", "HS 2", "Môn 3", "HS 3",
            "Độ Lệch", "tb_keys"
        };

        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                if (c == 0) return Integer.class;          // ID
                if (c == 4 || c == 6 || c == 8) return Integer.class; // HS
                if (c == 9) return Double.class;           // Độ lệch
                return String.class;
            }
        };

        tblNganhToHop = new CustomTable(tableModel);
        tblNganhToHop.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblNganhToHop.setRowHeight(28);

        // Độ rộng cột
        int[] widths = {40, 90, 90, 70, 40, 70, 40, 70, 40, 70, 120};
        for (int i = 0; i < widths.length; i++) {
            tblNganhToHop.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // Căn giữa các cột số
        for (int i : new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9}) {
            tblNganhToHop.getColumnModel().getColumn(i).setCellRenderer(CustomTable.centerRenderer());
        }

        JScrollPane scroll = new JScrollPane(tblNganhToHop);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        scroll.getViewport().setBackground(Color.WHITE);

        JPanel pnlCenter = new JPanel(new BorderLayout());
        pnlCenter.setOpaque(false);
        pnlCenter.setBorder(new EmptyBorder(0, 15, 0, 15));
        pnlCenter.add(scroll, BorderLayout.CENTER);
        add(pnlCenter, BorderLayout.CENTER);
    }

    // ── FOOTER / PAGING ──
    private void buildFooter() {
        JPanel pnlFooter = new JPanel(new BorderLayout());
        pnlFooter.setOpaque(false);
        pnlFooter.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, new Color(220, 220, 220)),
                new EmptyBorder(8, 15, 10, 15)));

        JPanel pnlPaging = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        pnlPaging.setOpaque(false);

        btnPrev = new CustomButton("Trước", UIConstants.PRIMARY_COLOR);
        btnPrev.setPreferredSize(new Dimension(105, 32));

        lblPageInfo = new JLabel("Trang 1 / 1");
        lblPageInfo.setFont(UIConstants.FONT_BOLD);
        lblPageInfo.setForeground(UIConstants.TABLE_HEADER_COLOR);

        btnNext = new CustomButton("Sau", UIConstants.PRIMARY_COLOR);
        btnNext.setPreferredSize(new Dimension(105, 32));

        pnlPaging.add(btnPrev);
        pnlPaging.add(lblPageInfo);
        pnlPaging.add(btnNext);

        pnlFooter.add(pnlPaging, BorderLayout.CENTER);
        add(pnlFooter, BorderLayout.SOUTH);
    }

    // ── EVENTS ──
    private void setupEvents() {
        btnSearch.addActionListener(e -> doSearch());
        txtSearch.addActionListener(e -> doSearch());

        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            currentKeyword = "";
            currentPage    = 1;
            loadData();
        });

        btnPrev.addActionListener(e -> { if (currentPage > 1) { currentPage--; loadData(); } });
        btnNext.addActionListener(e -> { if (currentPage < totalPages) { currentPage++; loadData(); } });

        btnDelete.addActionListener(e -> {
            int row = tblNganhToHop.getSelectedRow();
            if (row < 0) { warn("Vui lòng chọn bản ghi cần xóa!"); return; }

            String maNganh = (String) tblNganhToHop.getValueAt(row, 1);
            String maToHop = (String) tblNganhToHop.getValueAt(row, 2);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc muốn xóa liên kết " + maNganh + " - " + maToHop + "?",
                    "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                int id = (int) tblNganhToHop.getValueAt(row, 0);
                String result = bus.deleteNganhToHop(id);
                if (result.startsWith("Success")) {
                    JOptionPane.showMessageDialog(this, "Đã xóa thành công!");
                    if (tableModel.getRowCount() == 1 && currentPage > 1) currentPage--;
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(this, result, "Lỗi xóa", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnImport.addActionListener(e -> doImportExcel());
    }

    // ── SEARCH ──
    private void doSearch() {
        currentKeyword = txtSearch.getText().trim();
        currentPage    = 1;
        loadData();
    }

    // ── LOAD DATA ──
    private void loadData() {
        tableModel.setRowCount(0);

        List<NganhToHop> list;
        if (currentKeyword.isEmpty()) {
            list       = bus.getList(currentPage);
            totalPages = bus.calculateTotalPages();
        } else {
            list       = bus.search(currentPage, currentKeyword);
            totalPages = bus.calculateSearchTotalPages(currentKeyword);
        }

        lblPageInfo.setText("Trang " + currentPage + " / " + totalPages);
        btnPrev.setEnabled(currentPage > 1);
        btnNext.setEnabled(currentPage < totalPages);

        if (!currentKeyword.isEmpty()) {
            long found = bus.getSearchCount(currentKeyword);
            lblTotalRecords.setText("Kết quả: \"" + currentKeyword + "\"  |  " + found + " bản ghi");
        } else {
            long total = bus.getTotalCount();
            lblTotalRecords.setText("Tổng cộng: " + total + " liên kết Ngành-Tổ hợp");
        }

        if (list != null) {
            for (NganhToHop n : list) {
                tableModel.addRow(new Object[]{
                    n.getId(),
                    n.getMaNganh(),
                    n.getMaToHop(),
                    n.getThMon1() != null ? n.getThMon1() : "",
                    n.getHsMon1() != null ? n.getHsMon1() : "",
                    n.getThMon2() != null ? n.getThMon2() : "",
                    n.getHsMon2() != null ? n.getHsMon2() : "",
                    n.getThMon3() != null ? n.getThMon3() : "",
                    n.getHsMon3() != null ? n.getHsMon3() : "",
                    n.getDoLech() != null ? n.getDoLech() : "",
                    n.getTbKeys() != null ? n.getTbKeys() : ""
                });
            }
        }
    }

    // ── IMPORT EXCEL ──
    private void doImportExcel() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));
        fc.setDialogTitle("Chọn file Excel Ngành - Tổ hợp (tohopmon.xlsx)");

        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File file = fc.getSelectedFile();

        JDialog loadingDialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Đang xử lý...", Dialog.ModalityType.APPLICATION_MODAL);
        loadingDialog.setSize(370, 100);
        loadingDialog.setLocationRelativeTo(this);
        loadingDialog.setUndecorated(true);

        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);
        bar.setStringPainted(true);
        bar.setString("Đang import Ngành - Tổ hợp...");
        bar.setFont(UIConstants.FONT_BOLD);
        bar.setForeground(UIConstants.SUCCESS_COLOR);

        JPanel pnlLoad = new JPanel(new BorderLayout(10, 10));
        pnlLoad.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.SUCCESS_COLOR, 2),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        pnlLoad.add(bar);
        loadingDialog.add(pnlLoad);

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                List<NganhToHop> danhSach = ExcelUtil.readNganhToHopExcel(file);
                return bus.importNganhToHop(danhSach);
            }
            @Override
            protected void done() {
                loadingDialog.dispose();
                try {
                    String result = get();
                    JOptionPane.showMessageDialog(QuanLyNganhToHopPanel.this, result,
                            "Kết quả Import", JOptionPane.INFORMATION_MESSAGE);
                    currentPage    = 1;
                    currentKeyword = "";
                    txtSearch.setText("");
                    loadData();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    String msg = ex.getMessage();
                    if (ex.getCause() != null) msg = ex.getCause().getMessage();
                    JOptionPane.showMessageDialog(QuanLyNganhToHopPanel.this,
                            "Lỗi khi Import:\n" + msg,
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
        loadingDialog.setVisible(true);
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Chưa chọn", JOptionPane.WARNING_MESSAGE);
    }
}
