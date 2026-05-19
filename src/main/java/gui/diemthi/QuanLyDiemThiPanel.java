package gui.diemthi;

import bus.DiemThiBUS;
import entity.DiemThiXetTuyen;
import gui.component.CustomButton;
import gui.component.CustomTable;
import gui.component.CustomTextField;
import gui.style.UIConstants;
import util.ExcelUtil;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.Comparator;

public class QuanLyDiemThiPanel extends JPanel {

    private final DiemThiBUS bus = new DiemThiBUS();

    private CustomTextField   txtSearch;
    private JComboBox<String> cboFilter;
    private CustomButton      btnSearch, btnReset, btnThongKe;
    private CustomButton      btnAdd, btnEdit, btnDelete, btnImport;

    private CustomTable       tblData;
    private DefaultTableModel tableModel;

    private CustomButton btnPrev, btnNext;
    private JLabel       lblPageInfo, lblTotalRecords;

    private int    currentPage    = 1;
    private int    totalPages     = 1;
    private String currentKeyword = "";
    private String currentFilter  = "";  // "" = tất cả, "THPT"/"VSAT"/"DGNL"

    public QuanLyDiemThiPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UIConstants.BACKGROUND_COLOR);
        setOpaque(true);

        buildNorthArea();
        buildTableForAll(); // Luôn cố định cấu trúc full cột để tránh lỗi co nhỏ UI
        buildFooter();
        setupEvents();
        loadData();
    }

    // ── HEADER + TOOLBAR (Đã sửa lỗi đè nút) ───────────────────────────
    private void buildNorthArea() {
        JPanel pnlNorth = new JPanel(new BorderLayout());
        pnlNorth.setOpaque(false);

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(UIConstants.PRIMARY_COLOR);
        pnlHeader.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel lblTitle = new JLabel("QUẢN LÝ ĐIỂM THI");
        lblTitle.setFont(UIConstants.FONT_TITLE);
        lblTitle.setForeground(Color.WHITE);

        lblTotalRecords = new JLabel("Đang tải...");
        lblTotalRecords.setFont(UIConstants.FONT_NORMAL);
        lblTotalRecords.setForeground(UIConstants.PRIMARY_LIGHT);

        pnlHeader.add(lblTitle,        BorderLayout.WEST);
        pnlHeader.add(lblTotalRecords, BorderLayout.EAST);

        JPanel pnlToolbar = new JPanel(new BorderLayout(10, 0));
        pnlToolbar.setOpaque(false);
        pnlToolbar.setBorder(new EmptyBorder(10, 15, 8, 15));

        // Cụm tìm kiếm bên trái - txtSearch co giãn lấp khoảng trống
        JPanel pnlSearch = new JPanel(new BorderLayout(10, 0));
        pnlSearch.setOpaque(false);

        JLabel lblFilter = new JLabel("Loại:");
        lblFilter.setFont(UIConstants.FONT_BOLD);
        cboFilter = new JComboBox<>(new String[]{"Tất cả", "THPT", "VSAT", "DGNL"});
        cboFilter.setFont(UIConstants.FONT_NORMAL);
        cboFilter.setPreferredSize(new Dimension(90, 36));

        JLabel lblSearch = new JLabel("Tìm:");
        lblSearch.setFont(UIConstants.FONT_BOLD);
        txtSearch = new CustomTextField(15);
        txtSearch.setPreferredSize(new Dimension(160, 36));
        txtSearch.setToolTipText("Nhập CCCD hoặc số báo danh");

        btnSearch = new CustomButton("Tìm", UIConstants.PRIMARY_COLOR);
        btnReset  = new CustomButton("Xóa lọc", UIConstants.GRAY_COLOR);
        btnSearch.setPreferredSize(new Dimension(75, 36));
        btnReset.setPreferredSize(new Dimension(90, 36));

        JPanel pnlSearchLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlSearchLeft.setOpaque(false);
        pnlSearchLeft.add(lblFilter);
        pnlSearchLeft.add(cboFilter);
        pnlSearchLeft.add(Box.createHorizontalStrut(4));
        pnlSearchLeft.add(lblSearch);

        JPanel pnlSearchBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlSearchBtns.setOpaque(false);
        pnlSearchBtns.add(btnSearch);
        pnlSearchBtns.add(btnReset);

        pnlSearch.add(pnlSearchLeft, BorderLayout.WEST);
        pnlSearch.add(txtSearch, BorderLayout.CENTER);
        pnlSearch.add(pnlSearchBtns, BorderLayout.EAST);

        // Cụm hành động bên phải - Đưa nút Thống Kê về đây để dàn đều UI
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlActions.setOpaque(false);

        btnThongKe = new CustomButton("Thống kê", new Color(124, 58, 237));
        btnAdd     = new CustomButton("+ Thêm", UIConstants.SUCCESS_COLOR);
        btnEdit    = new CustomButton("Sửa",    UIConstants.PRIMARY_COLOR);
        btnDelete  = new CustomButton("Xóa",    UIConstants.DANGER_COLOR);
        btnImport  = new CustomButton("Import", new Color(14, 165, 233));

        // Đặt kích thước vừa vặn cho các nút hành động (Kích thước 100 giúp không bị tràn)
        for (CustomButton b : new CustomButton[]{btnThongKe, btnAdd, btnEdit, btnDelete}) {
            b.setPreferredSize(new Dimension(100, 36));
            pnlActions.add(b);
        }
        btnImport.setPreferredSize(new Dimension(100, 36));
        pnlActions.add(btnImport);

        pnlToolbar.add(pnlSearch,  BorderLayout.CENTER);
        pnlToolbar.add(pnlActions, BorderLayout.EAST);

        JSeparator sep = new JSeparator();
        sep.setForeground(UIConstants.BORDER_COLOR);
        JPanel toolbarWrapper = new JPanel(new BorderLayout());
        toolbarWrapper.setOpaque(false);
        toolbarWrapper.add(pnlToolbar, BorderLayout.CENTER);
        toolbarWrapper.add(sep,        BorderLayout.SOUTH);

        pnlNorth.add(pnlHeader,      BorderLayout.NORTH);
        pnlNorth.add(toolbarWrapper, BorderLayout.CENTER);
        add(pnlNorth, BorderLayout.NORTH);
    }

    // ── CỐ ĐỊNH BẢNG FULL CỘT (Hiện đủ NK1 -> NK10, Thứ tự THPT -> ĐGNL -> VSAT) ──
    private void buildTableForAll() {
        // Thứ tự sắp xếp các cột: Thông tin chung -> Điểm THPT -> Điểm Năng Khiếu (1-10) -> Điểm ĐGNL -> Điểm VSAT
        String[] cols = {
                // [0-3] Thông tin chung
                "ID", "CCCD", "SBD", "Loại",
                // [4-15] Điểm THPT / VSAT (cùng cột DB)
                "Toán", "Lý", "Hóa", "Sinh", "Văn", "Sử", "Địa", "GDCD",
                "N1_Thi", "N1_CC", "TI", "KTPL",
                // [16-25] Năng khiếu NK1-NK10
                "NK1","NK2","NK3","NK4","NK5","NK6","NK7","NK8","NK9","NK10",
                // [26-28] ĐGNL riêng
                "NL1", "CNCN", "CNNN"
        };

        // Đặt độ rộng hiển thị mặc định cho từng cột để cuộn ngang đẹp mắt
        int[] widths = new int[cols.length];
        widths[0] = 50;   // ID
        widths[1] = 130;  // CCCD
        widths[2] = 80;   // SBD
        widths[3] = 60;   // Loại
        for (int i = 4; i < cols.length; i++) widths[i] = 65;

        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                if (c == 0) return Integer.class;
                return String.class;
            }
        };

        tblData = new CustomTable(tableModel);
        tblData.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblData.setRowHeight(28);
        tblData.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // BẮT BUỘC để kích hoạt thanh cuộn ngang độc lập

        JScrollPane scroll = new JScrollPane(tblData,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR));
        scroll.getViewport().setBackground(Color.WHITE);

        JPanel pnlCenter = new JPanel(new BorderLayout());
        pnlCenter.setOpaque(false);
        pnlCenter.setBorder(new EmptyBorder(0, 15, 0, 15));
        pnlCenter.add(scroll, BorderLayout.CENTER);
        add(pnlCenter, BorderLayout.CENTER);

        tblData.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) openEdit();
            }
        });

        // Áp cấu hình độ rộng cột
        for (int i = 0; i < widths.length && i < tblData.getColumnCount(); i++)
            tblData.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Căn giữa toàn bộ các cột dữ liệu số và loại điểm
        for (int c = 0; c < cols.length; c++) {
            if (c == 0 || c >= 3)
                tblData.getColumnModel().getColumn(c).setCellRenderer(CustomTable.centerRenderer());
        }
    }

    // ── FOOTER / PAGING ──────────────────────────────────────────────────
    private void buildFooter() {
        JPanel pnlFooter = new JPanel(new BorderLayout());
        pnlFooter.setOpaque(false);
        pnlFooter.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, UIConstants.BORDER_COLOR),
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
        pnlPaging.add(btnPrev); pnlPaging.add(lblPageInfo); pnlPaging.add(btnNext);

        pnlFooter.add(pnlPaging, BorderLayout.CENTER);
        add(pnlFooter, BorderLayout.SOUTH);
    }

    // ── EVENTS ───────────────────────────────────────────────────────────
    private void setupEvents() {
        cboFilter.addActionListener(e -> {
            String sel = (String) cboFilter.getSelectedItem();
            currentFilter = switch (sel) {
                case "THPT" -> "4";
                case "VSAT" -> "3";
                case "DGNL" -> "2";
                default     -> "";   // "Tất cả"
            };
            currentPage    = 1;
            currentKeyword = "";
            txtSearch.setText("");
            loadData();
        });

        btnSearch.addActionListener(e -> doSearch());
        txtSearch.addActionListener(e -> doSearch());
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            currentKeyword = "";
            currentPage    = 1;
            loadData();
        });

        btnPrev.addActionListener(e -> { if (currentPage > 1)         { currentPage--; loadData(); } });
        btnNext.addActionListener(e -> { if (currentPage < totalPages) { currentPage++; loadData(); } });

        btnAdd.addActionListener(e -> {
            DiemThiDialog dlg = new DiemThiDialog(getParentWindow(), null, bus);
            dlg.setVisible(true);
            if (dlg.isSaved()) { currentPage = 1; loadData(); }
        });

        btnEdit.addActionListener(e -> openEdit());

        btnDelete.addActionListener(e -> {
            DiemThiXetTuyen dt = getSelected();
            if (dt == null) return;
            int ok = JOptionPane.showConfirmDialog(this,
                    "Xóa bản ghi điểm thi của CCCD \"" + dt.getCccd() + "\"?",
                    "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (ok == JOptionPane.YES_OPTION) {
                String result = bus.deleteDiemThi(dt.getIdDiemThi());
                if (result.startsWith("Success")) {
                    if (tableModel.getRowCount() == 1 && currentPage > 1) currentPage--;
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(this, result, "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnThongKe.addActionListener(e -> {
            ThongKeDiemDialog dlg = new ThongKeDiemDialog(getParentWindow(), bus);
            dlg.setVisible(true);
        });
        btnImport.addActionListener(e -> doImport());
    }

    private void openEdit() {
        DiemThiXetTuyen dt = getSelected();
        if (dt == null) return;
        DiemThiDialog dlg = new DiemThiDialog(getParentWindow(), dt, bus);
        dlg.setVisible(true);
        if (dlg.isSaved()) loadData();
    }

    private void doSearch() {
        currentKeyword = txtSearch.getText().trim();
        currentPage    = 1;
        loadData();
    }

    private void doImport() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Chọn file Excel điểm thi (đã join)");
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Excel files (*.xlsx)", "xlsx"));

        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = fc.getSelectedFile();
        try {
            // Hiện cursor chờ khi đọc file lớn
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            List<DiemThiXetTuyen> list = ExcelUtil.readDiemThiExcel(file);
            setCursor(Cursor.getDefaultCursor());

            if (list.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "File không có dữ liệu hoặc sai định dạng!\n" +
                                "Yêu cầu: dòng đầu là header, có cột CCCD và D_PHUONGTHUC.",
                        "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Đếm theo phương thức để hiện xác nhận rõ ràng
            long soThpt = list.stream().filter(d -> "4".equals(d.getPhuongThuc())).count();
            long soDgnl = list.stream().filter(d -> "2".equals(d.getPhuongThuc())).count();
            long soVsat = list.stream().filter(d -> "3".equals(d.getPhuongThuc())).count();

            int confirm = JOptionPane.showConfirmDialog(this,
                    String.format("Tìm thấy %d dòng:\n" +
                                    "  THPT (4): %d dòng\n" +
                                    "  ĐGNL (2): %d dòng\n" +
                                    "  VSAT (3): %d dòng\n\n" +
                                    "Tiến hành import?",
                            list.size(), soThpt, soDgnl, soVsat),
                    "Xác nhận import", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            // Thực hiện import
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            String result = bus.importDiemThi(list);
            setCursor(Cursor.getDefaultCursor());

            JOptionPane.showMessageDialog(this,
                    result, "Kết quả import", JOptionPane.INFORMATION_MESSAGE);

            // Reload lại danh sách
            currentPage = 1;
            loadData();

        } catch (IllegalArgumentException ex) {
            setCursor(Cursor.getDefaultCursor());
            JOptionPane.showMessageDialog(this,
                    "File sai định dạng:\n" + ex.getMessage(),
                    "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            setCursor(Cursor.getDefaultCursor());
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi đọc file:\n" + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── LOAD DATA (Đã thêm logic Sort Ưu tiên THPT -> ĐGNL -> VSAT) ──
    private void loadData() {
        tableModel.setRowCount(0);

        List<DiemThiXetTuyen> list;
        if (currentKeyword.isEmpty()) {
            list       = bus.getList(currentPage, currentFilter);
            totalPages = bus.calculateTotalPages(currentFilter);
            lblTotalRecords.setText("Tổng: " + bus.getTotalCount(currentFilter) + " bản ghi");
        } else {
            list       = bus.search(currentPage, currentKeyword, currentFilter);
            totalPages = bus.calculateSearchTotalPages(currentKeyword, currentFilter);
            lblTotalRecords.setText("Kết quả: " + bus.getSearchCount(currentKeyword, currentFilter) + " bản ghi");
        }

        lblPageInfo.setText("Trang " + currentPage + " / " + totalPages);
        btnPrev.setEnabled(currentPage > 1);
        btnNext.setEnabled(currentPage < totalPages);

        if (list == null) return;

        // Tiến hành Sort dữ liệu hiển thị theo thứ tự mong muốn: THPT (4) -> ĐGNL (2) -> VSAT (3)
        list.sort((a, b) -> {
            String ptA = a.getPhuongThuc() != null ? a.getPhuongThuc() : "";
            String ptB = b.getPhuongThuc() != null ? b.getPhuongThuc() : "";

            if (!ptA.equals(ptB)) {
                int weightA = "4".equals(ptA) ? 1 : ("2".equals(ptA) ? 2 : 3);
                int weightB = "4".equals(ptB) ? 1 : ("2".equals(ptB) ? 2 : 3);
                if (weightA != weightB) {
                    return Integer.compare(weightA, weightB);
                }
            }
            // SỬA TẠI ĐÂY: Đổi so sánh (b, a) thành (a, b) để ID chạy tăng dần từ 1, 2, 3... giống dưới Database
            return Integer.compare(a.getIdDiemThi(), b.getIdDiemThi());
        });

        // Đổ dữ liệu vào hàng theo cấu trúc full cột cố định
        for (DiemThiXetTuyen dt : list) {
            String ptRow = dt.getPhuongThuc() != null ? dt.getPhuongThuc() : "";
            Object[] r = new Object[tableModel.getColumnCount()];

            // [0-3] Thông tin chung — luôn set
            r[0] = dt.getIdDiemThi();
            r[1] = s(dt.getCccd());
            r[2] = s(dt.getSoBaoDanh());
            r[3] = tenPhuongThuc(dt.getPhuongThuc());

            // Mặc định tất cả cột điểm là "-"
            for (int i = 4; i < r.length; i++) r[i] = "-";

            if ("4".equals(ptRow)) {
                // ── THPT ──
                // [4-15] điểm môn
                r[4]  = f(dt.getDiemToan());
                r[5]  = f(dt.getDiemLy());
                r[6]  = f(dt.getDiemHoa());
                r[7]  = f(dt.getDiemSinh());
                r[8]  = f(dt.getDiemVan());
                r[9]  = f(dt.getDiemSu());
                r[10] = f(dt.getDiemDia());
                r[11] = f(dt.getDiemGdcd());
                r[12] = f(dt.getN1Thi());
                r[13] = f(dt.getN1Cc());
                r[14] = f(dt.getDiemTiengAnh()); // TI
                r[15] = f(dt.getDiemKtpl());     // KTPL
                // [16-25] NK
                r[16] = f(dt.getNk1());  r[17] = f(dt.getNk2());
                r[18] = f(dt.getNk3());  r[19] = f(dt.getNk4());
                r[20] = f(dt.getNk5());  r[21] = f(dt.getNk6());
                r[22] = f(dt.getNk7());  r[23] = f(dt.getNk8());
                r[24] = f(dt.getNk9());  r[25] = f(dt.getNk10());
                // [26-28] ĐGNL → "-" (đã set mặc định)

            } else if ("3".equals(ptRow)) {
                // ── VSAT ── dùng cùng cột DB với THPT
                r[4]  = f(dt.getDiemToan());
                r[5]  = f(dt.getDiemLy());
                r[6]  = f(dt.getDiemHoa());
                r[7]  = f(dt.getDiemSinh());
                r[8]  = f(dt.getDiemVan());
                r[9]  = f(dt.getDiemSu());
                r[10] = f(dt.getDiemDia());
                r[11] = "-";               // GDCD: VSAT không có
                r[12] = f(dt.getN1Thi()); // N1_THI: điểm thi ngoại ngữ
                r[13] = "-";               // N1_CC: không áp dụng
                r[14] = "-";               // TI: không áp dụng
                r[15] = "-";               // KTPL: không áp dụng
                // [16-28] NK + ĐGNL → "-"

            } else if ("2".equals(ptRow)) {
                // ── ĐGNL ──
                // [4-15] THPT → "-" (đã set mặc định)
                // [16-21] NK1-NK6: ĐGNL có thể có năng khiếu
                r[16] = f(dt.getNk1());  r[17] = f(dt.getNk2());
                r[18] = f(dt.getNk3());  r[19] = f(dt.getNk4());
                r[20] = f(dt.getNk5());  r[21] = f(dt.getNk6());
                r[22] = f(dt.getNk7());  r[23] = f(dt.getNk8());
                r[24] = f(dt.getNk9());  r[25] = f(dt.getNk10());
                // [22-25] NK7-NK10 → "-"
                // [26-28] ĐGNL riêng
                r[26] = f(dt.getNl1());
                r[27] = f(dt.getCncn());
                r[28] = f(dt.getCnnn());
                // KTPL và TI của ĐGNL nằm ở cột chung [14] và [15]
                r[14] = f(dt.getDiemTiengAnh()); // TI
                r[15] = f(dt.getDiemKtpl());     // KTPL
            }

            tableModel.addRow(r);
        }
    }

    // ── HELPER ───────────────────────────────────────────────────────────
    private DiemThiXetTuyen getSelected() {
        int row = tblData.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một bản ghi!", "Chưa chọn", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        int id = (int) tblData.getValueAt(row, 0);
        return bus.getById(id);
    }

    private Window getParentWindow() { return SwingUtilities.getWindowAncestor(this); }
    private String s(String v)  { return v != null ? v : ""; }
    private String f(Double v)  { return v != null ? String.format("%.2f", v) : "-"; }

    private String tenPhuongThuc(String ma) {
        return switch (ma != null ? ma : "") {
            case "2" -> "ĐGNL";
            case "3" -> "VSAT";
            case "4" -> "THPT";
            default  -> ma != null ? ma : "";
        };
    }
}