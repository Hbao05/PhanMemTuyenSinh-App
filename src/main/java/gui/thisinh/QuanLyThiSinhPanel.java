package gui.thisinh;

import bus.ThiSinhBUS;
import entity.ThiSinh;
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
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class QuanLyThiSinhPanel extends JPanel {
    private final ThiSinhBUS candidateBUS;

    // Thanh tìm kiếm
    private CustomTextField txtSearch;
    private CustomButton btnSearch, btnReset;

    // Các nút chức năng
    private CustomButton btnAdd, btnEdit, btnDelete, btnImport, btnViewDetail;

    // Bảng dữ liệu
    private CustomTable tblCandidates;
    private DefaultTableModel tableModel;

    // Phân trang
    private CustomButton btnPrev, btnNext;
    private JLabel lblPageInfo, lblTotalRecords;

    private int currentPage = 1;
    private int totalPages  = 1;
    private String currentKeyword = "";

    // ======================================================
    //  CONSTRUCTOR
    // ======================================================
    public QuanLyThiSinhPanel() {
        this.candidateBUS = new ThiSinhBUS();

        setLayout(new BorderLayout(0, 0));
        setBackground(UIConstants.BACKGROUND_COLOR);
        setOpaque(true);

        buildNorthArea();
        buildTable();
        buildFooter();
        setupEvents();

        loadDataToTable();
    }

    // ======================================================
    //  1+2. HEADER + TOOLBAR gộp trong 1 panel NORTH duy nhất
    //  (tránh lồng panel nhiều lớp gây che table header)
    // ======================================================
    private void buildNorthArea() {
        JPanel pnlNorth = new JPanel(new BorderLayout());
        pnlNorth.setOpaque(false);

        // ---- Header bar ----
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(UIConstants.PRIMARY_COLOR);
        pnlHeader.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel lblTitle = new JLabel("QUẢN LÝ THÍ SINH");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);

        lblTotalRecords = new JLabel("Đang tải...");
        lblTotalRecords.setFont(UIConstants.FONT_NORMAL);
        lblTotalRecords.setForeground(new Color(189, 215, 238));

        pnlHeader.add(lblTitle,       BorderLayout.WEST);
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
        txtSearch.setToolTipText("Nhập CCCD, Họ, Tên hoặc Họ & Tên");

        btnSearch = new CustomButton("Tìm", UIConstants.PRIMARY_COLOR);
        btnSearch.setPreferredSize(new Dimension(100, 36));

        btnReset = new CustomButton("Xoa loc", new Color(120, 120, 120));
        btnReset.setPreferredSize(new Dimension(110, 36));

        pnlSearch.add(lblSearch);
        pnlSearch.add(txtSearch);
        pnlSearch.add(btnSearch);
        pnlSearch.add(btnReset);

        // Nút chức năng (phải)
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlActions.setOpaque(false);

        btnAdd        = new CustomButton("+ Thêm mới",  UIConstants.SUCCESS_COLOR);
        btnEdit       = new CustomButton("Sua",         UIConstants.PRIMARY_COLOR);
        btnDelete     = new CustomButton("Xoa",         UIConstants.DANGER_COLOR);
        btnViewDetail = new CustomButton("Chi tiet",    UIConstants.PURPLE_COLOR);
        btnImport     = new CustomButton("Import",      UIConstants.TEAL_COLOR);

        for (CustomButton b : new CustomButton[]{btnAdd, btnEdit, btnDelete, btnViewDetail, btnImport}) {
            b.setPreferredSize(new Dimension(120, 36));
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

        // Gộp lại
        pnlNorth.add(pnlHeader,         BorderLayout.NORTH);
        pnlNorth.add(pnlToolbarWrapper, BorderLayout.CENTER);

        add(pnlNorth, BorderLayout.NORTH);
    }

    // ======================================================
    //  3. BẢNG DỮ LIỆU
    // ======================================================
    private void buildTable() {
        String[] columns = {"ID", "CCCD", "Họ", "Tên", "Ngày Sinh", "Giới Tính", "Khu Vực", "Đối Tượng"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
            @Override public Class<?> getColumnClass(int col) {
                return col == 0 ? Integer.class : String.class;
            }
        };

        tblCandidates = new CustomTable(tableModel);
        tblCandidates.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblCandidates.setRowHeight(28);

        // Căn chỉnh cột
        int[] widths = {50, 120, 160, 100, 100, 80, 80, 100};
        for (int i = 0; i < widths.length; i++) {
            tblCandidates.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        // Căn giữa cột ID và Giới tính, Khu vực
        for (int col : new int[]{0, 5, 6, 7}) {
            tblCandidates.getColumnModel().getColumn(col).setCellRenderer(CustomTable.centerRenderer());
        }

        JScrollPane scrollPane = new JScrollPane(tblCandidates);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        scrollPane.getViewport().setBackground(Color.WHITE);

        JPanel pnlCenter = new JPanel(new BorderLayout());
        pnlCenter.setOpaque(false);
        pnlCenter.setBorder(new EmptyBorder(0, 15, 0, 15));
        pnlCenter.add(scrollPane, BorderLayout.CENTER);
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
                new EmptyBorder(8, 15, 10, 15)
        ));

        // Thông tin trang (giữa)
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
        txtSearch.addActionListener(e -> doSearch()); // Enter trong ô tìm kiếm

        // Xóa bộ lọc
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            currentKeyword = "";
            currentPage = 1;
            loadDataToTable();
        });

        // Phân trang
        btnPrev.addActionListener(e -> {
            if (currentPage > 1) { currentPage--; loadDataToTable(); }
        });
        btnNext.addActionListener(e -> {
            if (currentPage < totalPages) { currentPage++; loadDataToTable(); }
        });

        // Thêm mới
        btnAdd.addActionListener(e -> {
            Window parent = SwingUtilities.getWindowAncestor(this);
            ThemThiSinhDialog dialog = new ThemThiSinhDialog(parent, candidateBUS);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                currentPage = 1;
                loadDataToTable();
            }
        });

        // Sửa
        btnEdit.addActionListener(e -> {
            int row = tblCandidates.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this,
                        "Vui lòng chọn thí sinh cần sửa!", "Chưa chọn", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int id = (int) tblCandidates.getValueAt(row, 0);
            ThiSinh tsFull = candidateBUS.getCandidate(id);
            if (tsFull != null) {
                Window parent = SwingUtilities.getWindowAncestor(this);
                SuaThiSinhDialog dialog = new SuaThiSinhDialog(parent, tsFull, candidateBUS);
                dialog.setVisible(true);
                if (dialog.isUpdated()) loadDataToTable();
            }
        });

        // Xóa
        btnDelete.addActionListener(e -> {
            int row = tblCandidates.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this,
                        "Vui lòng chọn thí sinh cần xóa!", "Chưa chọn", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String cccd = (String) tblCandidates.getValueAt(row, 1);
            String ten   = tblCandidates.getValueAt(row, 2) + " " + tblCandidates.getValueAt(row, 3);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc muốn xóa thí sinh?\n" +
                    "  CCCD : " + cccd + "\n" +
                    "  Họ tên: " + ten.trim(),
                    "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                int id = (int) tblCandidates.getValueAt(row, 0);
                String result = candidateBUS.deleteCandidate(id);
                if (result.startsWith("Success")) {
                    JOptionPane.showMessageDialog(this,
                            "Đã xóa thí sinh thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    // Điều chỉnh trang nếu xóa hết trang cuối
                    if (tableModel.getRowCount() == 1 && currentPage > 1) currentPage--;
                    loadDataToTable();
                } else {
                    JOptionPane.showMessageDialog(this, result, "Lỗi xóa", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Xem chi tiết
        btnViewDetail.addActionListener(e -> {
            int row = tblCandidates.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this,
                        "Vui lòng chọn thí sinh để xem chi tiết!", "Chưa chọn", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int id = (int) tblCandidates.getValueAt(row, 0);
            ThiSinh ts = candidateBUS.getCandidate(id);
            if (ts != null) showDetailDialog(ts);
        });

        // Double-click mở chi tiết
        tblCandidates.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tblCandidates.getSelectedRow();
                    if (row >= 0) {
                        int id = (int) tblCandidates.getValueAt(row, 0);
                        ThiSinh ts = candidateBUS.getCandidate(id);
                        if (ts != null) showDetailDialog(ts);
                    }
                }
            }
        });

        // Import Excel
        btnImport.addActionListener(e -> doImportExcel());
    }

    // ======================================================
    //  6. LOGIC NGHIỆP VỤ
    // ======================================================
    private void doSearch() {
        currentKeyword = txtSearch.getText().trim();
        currentPage = 1;
        loadDataToTable();
    }

    private void loadDataToTable() {
        tableModel.setRowCount(0);
        List<ThiSinh> list;

        if (currentKeyword.isEmpty()) {
            list       = candidateBUS.getList(currentPage);
            totalPages = candidateBUS.calculateTotalPages();
        } else {
            list       = candidateBUS.search(currentPage, currentKeyword);
            totalPages = candidateBUS.calculateSearchTotalPages(currentKeyword);
        }

        // Cập nhật thông tin trang và tổng
        lblPageInfo.setText("Trang " + currentPage + " / " + totalPages);
        btnPrev.setEnabled(currentPage > 1);
        btnNext.setEnabled(currentPage < totalPages);

        // Hiển thị tổng số bản ghi trong header
        if (!currentKeyword.isEmpty()) {
            long found = candidateBUS.getSearchCount(currentKeyword);
            lblTotalRecords.setText("Kết quả: \"" + currentKeyword + "\"  —  " + found + " thí sinh");
        } else {
            long total = candidateBUS.getTotalCount();
            lblTotalRecords.setText("Tổng cộng: " + total + " thí sinh");
        }

        if (list != null) {
            for (ThiSinh ts : list) {
                tableModel.addRow(new Object[]{
                        ts.getIdThiSinh(),
                        ts.getCccd(),
                        ts.getHo(),
                        ts.getTen(),
                        ts.getNgaySinh(),
                        ts.getGioiTinh(),
                        ts.getKhuVuc(),
                        ts.getDoiTuong()
                });
            }
        }
    }

    /** Hiển thị Dialog xem chi tiết thông tin thí sinh (chỉ đọc) */
    private void showDetailDialog(ThiSinh ts) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Chi tiết Thí sinh", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(430, 480);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        dialog.getContentPane().setBackground(UIConstants.BACKGROUND_COLOR);
        dialog.setLayout(new BorderLayout());

        // Tiêu đề
        JLabel lblTitle = new JLabel("THÔNG TIN CHI TIẾT", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitle.setForeground(UIConstants.TABLE_HEADER_COLOR);
        lblTitle.setBorder(new EmptyBorder(15, 0, 10, 0));
        dialog.add(lblTitle, BorderLayout.NORTH);

        // Nội dung – dùng GridLayout
        JPanel pnlInfo = new JPanel(new GridLayout(0, 2, 8, 10));
        pnlInfo.setOpaque(false);
        pnlInfo.setBorder(new EmptyBorder(5, 30, 20, 30));

        String[][] rows = {
                {"CCCD",          ts.getCccd()},
                {"Họ",            ts.getHo()},
                {"Tên",           ts.getTen()},
                {"Ngày sinh",     ts.getNgaySinh()},
                {"Giới tính",     ts.getGioiTinh()},
                {"Nơi sinh",      ts.getNoiSinh()},
                {"Điện thoại",    ts.getDienThoai()},
                {"Email",         ts.getEmail()},
                {"Đối tượng UT",  ts.getDoiTuong()},
                {"Khu vực UT",    ts.getKhuVuc()},
                {"Cập nhật lúc",  ts.getUpdatedAt() != null ? ts.getUpdatedAt().toString() : ""},
        };
        for (String[] r : rows) {
            JLabel lKey = new JLabel(r[0] + ":");
            lKey.setFont(UIConstants.FONT_BOLD);
            lKey.setForeground(Color.DARK_GRAY);

            JLabel lVal = new JLabel(r[1] != null ? r[1] : "—");
            lVal.setFont(UIConstants.FONT_NORMAL);
            lVal.setForeground(new Color(50, 50, 50));

            pnlInfo.add(lKey);
            pnlInfo.add(lVal);
        }
        dialog.add(pnlInfo, BorderLayout.CENTER);

        // Nút Đóng
        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlBtn.setOpaque(false);
        CustomButton btnClose = new CustomButton("Đóng", UIConstants.TABLE_HEADER_COLOR);
        btnClose.setPreferredSize(new Dimension(110, 36));
        btnClose.addActionListener(ev -> dialog.dispose());
        pnlBtn.add(btnClose);
        dialog.add(pnlBtn, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    /** Import Excel bằng SwingWorker để không đơ giao diện */
    private void doImportExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));
        fileChooser.setDialogTitle("Chọn file Excel Danh sách Thí sinh");

        if (fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File selectedFile = fileChooser.getSelectedFile();

        // Dialog Loading
        JDialog loadingDialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Đang xử lý...", Dialog.ModalityType.APPLICATION_MODAL);
        loadingDialog.setSize(370, 100);
        loadingDialog.setLocationRelativeTo(this);
        loadingDialog.setUndecorated(true);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setStringPainted(true);
        progressBar.setString("Đang đọc và import dữ liệu, vui lòng đợi...");
        progressBar.setFont(UIConstants.FONT_BOLD);
        progressBar.setForeground(UIConstants.SUCCESS_COLOR);

        JPanel pnlLoading = new JPanel(new BorderLayout(10, 10));
        pnlLoading.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.SUCCESS_COLOR, 2),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        pnlLoading.add(progressBar);
        loadingDialog.add(pnlLoading);

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                Set<String> cccdCache = candidateBUS.newImportCccdCache();
                ThiSinhBUS.ImportCandidateResult tongKet = new ThiSinhBUS.ImportCandidateResult();
                AtomicInteger soLoCoDuLieu = new AtomicInteger(0);
                ExcelUtil.forEachCandidateExcelBatch(
                        selectedFile,
                        ExcelUtil.CANDIDATE_IMPORT_BATCH_SIZE,
                        batch -> {
                            if (!batch.isEmpty()) {
                                soLoCoDuLieu.incrementAndGet();
                            }
                            tongKet.merge(candidateBUS.importCandidatesBatch(batch, cccdCache));
                        });
                if (soLoCoDuLieu.get() == 0 && tongKet.isEmptyTotals()) {
                    return "Lỗi: Danh sách import trống hoặc file Excel không có dữ liệu!";
                }
                return tongKet.formatMessage();
            }
            @Override
            protected void done() {
                loadingDialog.dispose();
                try {
                    String ketQua = get();
                    JOptionPane.showMessageDialog(QuanLyThiSinhPanel.this, ketQua,
                            "Kết quả Import", JOptionPane.INFORMATION_MESSAGE);
                    currentPage = 1;
                    currentKeyword = "";
                    txtSearch.setText("");
                    loadDataToTable();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(QuanLyThiSinhPanel.this,
                            "Lỗi nghiêm trọng khi Import:\n" + ex.getMessage(),
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
        loadingDialog.setVisible(true);
    }
}
