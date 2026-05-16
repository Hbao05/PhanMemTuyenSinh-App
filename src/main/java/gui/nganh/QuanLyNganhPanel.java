package gui.nganh;

import bus.NganhBUS;
import entity.Nganh;
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

public class QuanLyNganhPanel extends JPanel {

    private final NganhBUS nganhBUS;

    // Thanh tìm kiếm
    private CustomTextField txtSearch;
    private CustomButton    btnSearch, btnReset;

    // Nút chức năng
    private CustomButton btnAdd, btnEdit, btnDelete, btnImport;

    // Bảng dữ liệu
    private CustomTable       tblNganh;
    private DefaultTableModel tableModel;

    // Phân trang
    private CustomButton btnPrev, btnNext;
    private JLabel       lblPageInfo, lblTotalRecords;

    private int    currentPage    = 1;
    private int    totalPages     = 1;
    private String currentKeyword = "";

    // ======================================================
    //  CONSTRUCTOR
    // ======================================================
    public QuanLyNganhPanel() {
        this.nganhBUS = new NganhBUS();

        setLayout(new BorderLayout(0, 0));
        setBackground(UIConstants.BACKGROUND_COLOR);
        setOpaque(true);

        buildNorthArea();   // Header + Toolbar trong 1 panel NORTH duy nhất
        buildTable();
        buildFooter();
        setupEvents();      // Gọi SAU buildFooter()

        loadData();
    }

    // ======================================================
    //  1+2. HEADER + TOOLBAR (gộp 1 panel NORTH duy nhất)
    //  Tránh lồng panel nhiều lớp gây che table-header
    // ======================================================
    private void buildNorthArea() {
        JPanel pnlNorth = new JPanel(new BorderLayout());
        pnlNorth.setOpaque(false);

        // ---- Header bar ----
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(UIConstants.PRIMARY_COLOR);
        pnlHeader.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel lblTitle = new JLabel("QUẢN LÝ NGÀNH ĐÀO TẠO");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);

        lblTotalRecords = new JLabel("Đang tải...");
        lblTotalRecords.setFont(UIConstants.FONT_NORMAL);
        lblTotalRecords.setForeground(new Color(189, 215, 238));

        pnlHeader.add(lblTitle,        BorderLayout.WEST);
        pnlHeader.add(lblTotalRecords, BorderLayout.EAST);

        // ---- Toolbar ----
        JPanel pnlToolbar = new JPanel(new BorderLayout(10, 0));
        pnlToolbar.setOpaque(false);
        pnlToolbar.setBorder(new EmptyBorder(10, 15, 8, 15));

        // Tìm kiếm (trái)
        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlSearch.setOpaque(false);

        JLabel lblSearch = new JLabel("Tìm kiếm:");
        lblSearch.setFont(UIConstants.FONT_BOLD);

        txtSearch = new CustomTextField(22);
        txtSearch.setPreferredSize(new Dimension(260, 36));
        txtSearch.setToolTipText("Nhập mã ngành hoặc tên ngành");

        btnSearch = new CustomButton("Tìm", UIConstants.PRIMARY_COLOR);
        btnSearch.setPreferredSize(new Dimension(100, 36));

        btnReset = new CustomButton("Xóa lọc", new Color(120, 120, 120));
        btnReset.setPreferredSize(new Dimension(110, 36));
        btnReset.setToolTipText("Xóa từ khóa tìm kiếm, hiển thị toàn bộ danh sách");

        pnlSearch.add(lblSearch);
        pnlSearch.add(txtSearch);
        pnlSearch.add(btnSearch);
        pnlSearch.add(btnReset);

        // Nút chức năng (phải)
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlActions.setOpaque(false);

        btnAdd        = new CustomButton("+ Thêm mới",  UIConstants.SUCCESS_COLOR);
        btnEdit       = new CustomButton("Sửa",           UIConstants.PRIMARY_COLOR);
        btnDelete     = new CustomButton("Xóa",      UIConstants.DANGER_COLOR);
        btnImport     = new CustomButton("Import",   UIConstants.TEAL_COLOR);

        for (CustomButton b : new CustomButton[]{btnAdd, btnEdit, btnDelete, btnImport}) {
            b.setPreferredSize(new Dimension(118, 36));
            pnlActions.add(b);
        }

        pnlToolbar.add(pnlSearch,  BorderLayout.WEST);
        pnlToolbar.add(pnlActions, BorderLayout.EAST);

        // Đường kẻ phân cách
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(200, 200, 200));

        JPanel pnlToolbarWrapper = new JPanel(new BorderLayout());
        pnlToolbarWrapper.setOpaque(false);
        pnlToolbarWrapper.add(pnlToolbar, BorderLayout.CENTER);
        pnlToolbarWrapper.add(sep,        BorderLayout.SOUTH);

        // Gộp thành 1 NORTH panel
        pnlNorth.add(pnlHeader,         BorderLayout.NORTH);
        pnlNorth.add(pnlToolbarWrapper, BorderLayout.CENTER);

        add(pnlNorth, BorderLayout.NORTH);
    }

    // ======================================================
    //  3. BẢNG DỮ LIỆU
    // ======================================================
    private void buildTable() {
        String[] cols = {"ID", "Mã Ngành", "Tên Ngành", "Tổ Hợp Gốc",
                         "Chỉ Tiêu", "Điểm Sàn", "SL Đăng Ký", "TT", "ĐGNL", "THPT", "VSAT"};

        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return (c == 0 || c == 4 || c == 6) ? Integer.class : String.class;
            }
        };

        tblNganh = new CustomTable(tableModel);
        tblNganh.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblNganh.setRowHeight(28);

        // Độ rộng cột
        int[] widths = {45, 95, 250, 90, 75, 80, 90, 45, 55, 55, 55};
        for (int i = 0; i < widths.length; i++) {
            tblNganh.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // Căn giữa cột số
        for (int c : new int[]{0, 3, 4, 5, 6}) {
            tblNganh.getColumnModel().getColumn(c).setCellRenderer(CustomTable.centerRenderer());
        }

        // Renderer đặc biệt: Y -> "Có" (xanh), null/khác -> "-" (xám)
        CustomTable.ZebraRenderer flagRend = new CustomTable.ZebraRenderer(SwingConstants.CENTER) {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int col) {
                boolean isY = "Y".equalsIgnoreCase(String.valueOf(value));
                super.getTableCellRendererComponent(table, isY ? "Có" : "-", isSelected, hasFocus, row, col);
                if (!isSelected) setForeground(isY ? UIConstants.SUCCESS_COLOR : new Color(180, 180, 180));
                return this;
            }
        };
        for (int c : new int[]{7, 8, 9, 10}) {
            tblNganh.getColumnModel().getColumn(c).setCellRenderer(flagRend);
        }

        JScrollPane scroll = new JScrollPane(tblNganh);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        scroll.getViewport().setBackground(Color.WHITE);

        JPanel pnlCenter = new JPanel(new BorderLayout());
        pnlCenter.setOpaque(false);
        pnlCenter.setBorder(new EmptyBorder(0, 15, 0, 15));
        pnlCenter.add(scroll, BorderLayout.CENTER);
        add(pnlCenter, BorderLayout.CENTER);
    }

    // ======================================================
    //  4. FOOTER – Phân trang
    // ======================================================
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

    // ======================================================
    //  5. SỰ KIỆN
    // ======================================================
    private void setupEvents() {
        // Tìm kiếm
        btnSearch.addActionListener(e -> doSearch());
        txtSearch.addActionListener(e -> doSearch());

        // Xóa lọc: xóa từ khóa, hiển thị toàn bộ danh sách
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            currentKeyword = "";
            currentPage    = 1;
            loadData();
        });

        // Phân trang
        btnPrev.addActionListener(e -> { if (currentPage > 1) { currentPage--; loadData(); } });
        btnNext.addActionListener(e -> { if (currentPage < totalPages) { currentPage++; loadData(); } });

        // Thêm mới
        btnAdd.addActionListener(e -> {
            NganhDialog dlg = new NganhDialog(SwingUtilities.getWindowAncestor(this), null, nganhBUS);
            dlg.setVisible(true);
            if (dlg.isSaved()) { currentPage = 1; loadData(); }
        });

        // Sửa
        btnEdit.addActionListener(e -> {
            Nganh n = getSelectedNganh();
            if (n == null) return;
            NganhDialog dlg = new NganhDialog(SwingUtilities.getWindowAncestor(this), n, nganhBUS);
            dlg.setVisible(true);
            if (dlg.isSaved()) loadData();
        });

        // Xóa
        btnDelete.addActionListener(e -> {
            int row = tblNganh.getSelectedRow();
            if (row < 0) { warn("Vui lòng chọn ngành cần xóa!"); return; }

            String ma  = (String) tblNganh.getValueAt(row, 1);
            String ten = (String) tblNganh.getValueAt(row, 2);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc muốn xóa ngành?\n  Mã  : " + ma + "\n  Tên : " + ten,
                    "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                int id = (int) tblNganh.getValueAt(row, 0);
                String result = nganhBUS.deleteNganh(id);
                if (result.startsWith("Success")) {
                    JOptionPane.showMessageDialog(this, "Đã xóa ngành thành công!");
                    if (tableModel.getRowCount() == 1 && currentPage > 1) currentPage--;
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(this, result, "Lỗi xóa", JOptionPane.ERROR_MESSAGE);
                }
            }
        });


        // Double-click mở chi tiết
        tblNganh.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Nganh n = getSelectedNganh();
                    if (n != null) showDetailDialog(n);
                }
            }
        });

        // Import Excel
        btnImport.addActionListener(e -> doImportExcel());
    }

    // ======================================================
    //  6. LOAD DỮ LIỆU
    // ======================================================
    private void doSearch() {
        currentKeyword = txtSearch.getText().trim();
        currentPage    = 1;
        loadData();
    }

    private void loadData() {
        tableModel.setRowCount(0);

        List<Nganh> list;
        if (currentKeyword.isEmpty()) {
            list       = nganhBUS.getList(currentPage);
            totalPages = nganhBUS.calculateTotalPages();
        } else {
            list       = nganhBUS.search(currentPage, currentKeyword);
            totalPages = nganhBUS.calculateSearchTotalPages(currentKeyword);
        }

        lblPageInfo.setText("Trang " + currentPage + " / " + totalPages);
        btnPrev.setEnabled(currentPage > 1);
        btnNext.setEnabled(currentPage < totalPages);

        if (!currentKeyword.isEmpty()) {
            long found = nganhBUS.getSearchCount(currentKeyword);
            lblTotalRecords.setText("Kết quả: \"" + currentKeyword + "\"  |  " + found + " ngành");
        } else {
            long total = nganhBUS.getTotalCount();
            lblTotalRecords.setText("Tổng cộng: " + total + " ngành");
        }

        if (list != null) {
            for (Nganh n : list) {
                long slDangKy = nganhBUS.getSoLuongDangKy(n.getMaNganh());
                tableModel.addRow(new Object[]{
                        n.getIdNganh(),
                        n.getMaNganh(),
                        n.getTenNganh(),
                        n.getToHopGoc(),
                        n.getChiTieu(),
                        n.getDiemSan() != null ? n.getDiemSan() : "-",
                        slDangKy,
                        n.getTuyenThang(),
                        n.getDgnl(),
                        n.getThpt(),
                        n.getVsat()
                });
            }
        }
    }

    // ======================================================
    //  7. IMPORT EXCEL
    // ======================================================
    private void doImportExcel() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));
        fc.setDialogTitle("Chọn file Excel Danh sách Ngành (Chi tiêu / Ngưỡng đầu vào)");

        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File file = fc.getSelectedFile();

        // Dialog loading
        JDialog loadingDialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Đang xử lý...", Dialog.ModalityType.APPLICATION_MODAL);
        loadingDialog.setSize(370, 100);
        loadingDialog.setLocationRelativeTo(this);
        loadingDialog.setUndecorated(true);

        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);
        bar.setStringPainted(true);
        bar.setString("Đang import danh sách ngành...");
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
                List<Nganh> danhSach = ExcelUtil.readNganhExcel(file);
                return nganhBUS.importNganh(danhSach);
            }
            @Override
            protected void done() {
                loadingDialog.dispose();
                try {
                    String result = get();
                    JOptionPane.showMessageDialog(QuanLyNganhPanel.this, result,
                            "Kết quả Import", JOptionPane.INFORMATION_MESSAGE);
                    currentPage    = 1;
                    currentKeyword = "";
                    txtSearch.setText("");
                    loadData();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(QuanLyNganhPanel.this,
                            "Lỗi khi Import:\n" + ex.getMessage(),
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
        loadingDialog.setVisible(true);
    }

    // ======================================================
    //  8. DIALOG CHI TIẾT (chỉ đọc)
    // ======================================================
    private void showDetailDialog(Nganh n) {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Chi tiết Ngành", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(460, 520);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);
        dlg.getContentPane().setBackground(UIConstants.BACKGROUND_COLOR);
        dlg.setLayout(new BorderLayout());

        JLabel lblTitle = new JLabel("THÔNG TIN CHI TIẾT NGÀNH", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(UIConstants.TABLE_HEADER_COLOR);
        lblTitle.setBorder(new EmptyBorder(15, 0, 10, 0));
        dlg.add(lblTitle, BorderLayout.NORTH);

        JPanel pnlInfo = new JPanel(new GridLayout(0, 2, 8, 10));
        pnlInfo.setOpaque(false);
        pnlInfo.setBorder(new EmptyBorder(5, 30, 20, 30));

        Object[][] rows = {
            {"Mã ngành",           n.getMaNganh()},
            {"Tên ngành",          n.getTenNganh()},
            {"Tổ hợp gốc",         n.getToHopGoc()},
            {"Chỉ tiêu",           n.getChiTieu()},
            {"Số TS đăng ký",      nganhBUS.getSoLuongDangKy(n.getMaNganh())},
            {"Điểm sàn",           n.getDiemSan()},
            {"Điểm trúng tuyển",   n.getDiemTrungTuyen()},
            {"Tuyển thẳng",        flag(n.getTuyenThang())},
            {"Xét ĐGNL",           flag(n.getDgnl())},
            {"Xét THPT",           flag(n.getThpt())},
            {"Xét V-SAT",          flag(n.getVsat())},
            {"SL xét tuyển thẳng", n.getSlXtt()},
            {"SL xét ĐGNL",        n.getSlDgnl()},
            {"SL xét V-SAT",       n.getSlVsat()},
            {"SL xét THPT",        n.getSlThpt()},
        };

        for (Object[] r : rows) {
            JLabel lKey = new JLabel(r[0] + ":");
            lKey.setFont(UIConstants.FONT_BOLD);
            lKey.setForeground(Color.DARK_GRAY);

            String val = (r[1] != null) ? r[1].toString() : "-";
            JLabel lVal = new JLabel(val.isEmpty() ? "-" : val);
            lVal.setFont(UIConstants.FONT_NORMAL);
            lVal.setForeground(new Color(50, 50, 50));

            pnlInfo.add(lKey);
            pnlInfo.add(lVal);
        }
        dlg.add(pnlInfo, BorderLayout.CENTER);

        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlBtn.setOpaque(false);
        CustomButton btnClose = new CustomButton("Đóng", UIConstants.TABLE_HEADER_COLOR);
        btnClose.setPreferredSize(new Dimension(110, 36));
        btnClose.addActionListener(ev -> dlg.dispose());
        pnlBtn.add(btnClose);
        dlg.add(pnlBtn, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    // ======================================================
    //  HELPER
    // ======================================================
    private Nganh getSelectedNganh() {
        int row = tblNganh.getSelectedRow();
        if (row < 0) { warn("Vui lòng chọn một ngành trong danh sách!"); return null; }
        return nganhBUS.getById((int) tblNganh.getValueAt(row, 0));
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Chưa chọn", JOptionPane.WARNING_MESSAGE);
    }

    private String flag(String val) {
        return "Y".equalsIgnoreCase(val) ? "Có" : "Không";
    }
}
