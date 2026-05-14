package gui.diemthi;

import bus.DiemThiBUS;
import entity.DiemThiXetTuyen;
import gui.component.CustomButton;
import gui.component.CustomTable;
import gui.component.CustomTextField;
import gui.style.UIConstants;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class QuanLyDiemThiPanel extends JPanel {

    private final DiemThiBUS bus = new DiemThiBUS();

    private CustomTextField   txtSearch;
    private JComboBox<String> cboFilter;
    private CustomButton      btnSearch, btnReset, btnThongKe;
    private CustomButton      btnAdd, btnEdit, btnDelete;

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
        buildTableForFilter("");
        buildFooter();
        setupEvents();
        loadData();
    }

    // ── HEADER + TOOLBAR ────────────────────────────────────────────────
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

        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlSearch.setOpaque(false);

        JLabel lblFilter = new JLabel("Loai:");
        lblFilter.setFont(UIConstants.FONT_BOLD);
        cboFilter = new JComboBox<>(new String[]{"Tất cả", "THPT", "VSAT", "DGNL"});
        cboFilter.setFont(UIConstants.FONT_NORMAL);
        cboFilter.setPreferredSize(new Dimension(100, 36));

        JLabel lblSearch = new JLabel("Tìm kiếm:");
        lblSearch.setFont(UIConstants.FONT_BOLD);
        txtSearch = new CustomTextField(18);
        txtSearch.setPreferredSize(new Dimension(200, 36));
        txtSearch.setToolTipText("Nhập CCCD hoặc số báo danh");
        btnSearch   = new CustomButton("Tìm",      UIConstants.PRIMARY_COLOR);
        btnReset    = new CustomButton("Xóa lọc",  UIConstants.GRAY_COLOR);
        btnThongKe  = new CustomButton("Thống kê", new Color(124, 58, 237));
        btnSearch.setPreferredSize(new Dimension(80, 36));
        btnReset.setPreferredSize(new Dimension(100, 36));
        btnThongKe.setPreferredSize(new Dimension(110, 36));

        pnlSearch.add(lblFilter); pnlSearch.add(cboFilter);
        pnlSearch.add(Box.createHorizontalStrut(6));
        pnlSearch.add(lblSearch); pnlSearch.add(txtSearch);
        pnlSearch.add(btnSearch); pnlSearch.add(btnReset);
        pnlSearch.add(Box.createHorizontalStrut(10));
        pnlSearch.add(btnThongKe);

        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pnlActions.setOpaque(false);
        btnAdd    = new CustomButton("+ Thêm", UIConstants.SUCCESS_COLOR);
        btnEdit   = new CustomButton("Sửa",    UIConstants.PRIMARY_COLOR);
        btnDelete = new CustomButton("Xóa",    UIConstants.DANGER_COLOR);
        for (CustomButton b : new CustomButton[]{btnAdd, btnEdit, btnDelete}) {
            b.setPreferredSize(new Dimension(110, 36));
            pnlActions.add(b);
        }

        pnlToolbar.add(pnlSearch,  BorderLayout.WEST);
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

    // ── TABLE (xây lại theo filter) ──────────────────────────────────────
    private void buildTableForFilter(String filter) {
        String[] cols;
        int[]    widths;

        if ("DGNL".equals(filter)) {
            cols   = new String[]{"ID","CCCD","Loai","NL1","NK1","NK2","CNCN","CNNN","KTPL"};
            widths = new int[]{45,130,60,65,65,65,65,65,65};
        } else if ("VSAT".equals(filter)) {
            cols   = new String[]{"ID","CCCD","SBD","Loai","Toan","Ly","Hoa","Sinh","Van","Su","Dia","T.Anh"};
            widths = new int[]{45,130,90,55,55,55,55,55,55,55,55,55};
        } else {
            // THPT hoặc Tất cả
            cols   = new String[]{"ID","CCCD","SBD","Loai","Toan","Ly","Hoa","Sinh","Van","Su","Dia","T.Anh"};
            widths = new int[]{45,130,90,55,55,55,55,55,55,55,55,55};
        }

        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                if (c == 0) return Integer.class;
                return String.class;
            }
        };

        if (tblData == null) {
            tblData = new CustomTable(tableModel);
            tblData.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            tblData.setRowHeight(28);

            JScrollPane scroll = new JScrollPane(tblData);
            scroll.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR));
            scroll.getViewport().setBackground(Color.WHITE);

            JPanel pnlCenter = new JPanel(new BorderLayout());
            pnlCenter.setOpaque(false);
            pnlCenter.setBorder(new EmptyBorder(0, 15, 0, 15));
            pnlCenter.add(scroll, BorderLayout.CENTER);
            add(pnlCenter, BorderLayout.CENTER);

            // Double-click = sửa
            tblData.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getClickCount() == 2) openEdit();
                }
            });
        } else {
            tblData.setModel(tableModel);
        }

        for (int i = 0; i < widths.length; i++)
            tblData.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Căn giữa cột ID và điểm
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
            currentFilter = "Tất cả".equals(sel) ? "" : sel;
            buildTableForFilter(currentFilter);
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

    // ── LOAD DATA ────────────────────────────────────────────────────────
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
        for (DiemThiXetTuyen dt : list) {
            String pt = dt.getPhuongThuc() != null ? dt.getPhuongThuc() : "";
            if ("DGNL".equals(currentFilter)) {
                tableModel.addRow(new Object[]{
                    dt.getIdDiemThi(), s(dt.getCccd()), pt,
                    f(dt.getNl1()), f(dt.getNk1()), f(dt.getNk2()),
                    f(dt.getCncn()), f(dt.getCnnn()), f(dt.getDiemKtpl())
                });
            } else {
                tableModel.addRow(new Object[]{
                    dt.getIdDiemThi(), s(dt.getCccd()), s(dt.getSoBaoDanh()), pt,
                    f(dt.getDiemToan()), f(dt.getDiemLy()),  f(dt.getDiemHoa()),
                    f(dt.getDiemSinh()), f(dt.getDiemVan()), f(dt.getDiemSu()),
                    f(dt.getDiemDia()), f(dt.getDiemTiengAnh())
                });
            }
        }
    }

    // ── HELPER ───────────────────────────────────────────────────────────
    private DiemThiXetTuyen getSelected() {
        int row = tblData.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một bản ghi!", "Chưa chọn",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }
        int id = (int) tblData.getValueAt(row, 0);
        return bus.getById(id);
    }

    private Window getParentWindow() { return SwingUtilities.getWindowAncestor(this); }
    private String s(String v)  { return v != null ? v : ""; }
    private String f(Double v)  { return v != null ? String.format("%.2f", v) : "-"; }
}
