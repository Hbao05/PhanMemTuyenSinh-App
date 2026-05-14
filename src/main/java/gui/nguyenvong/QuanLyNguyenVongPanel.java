package gui.nguyenvong;

import bus.NguyenVongBUS;
import bus.XetTuyenEngine;
import entity.Nganh;
import entity.NguyenVongXetTuyen;
import entity.ThiSinh;
import gui.component.CustomButton;
import gui.component.CustomTable;
import gui.component.CustomTextField;
import gui.style.UIConstants;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class QuanLyNguyenVongPanel extends JPanel {

    private final NguyenVongBUS bus = new NguyenVongBUS();

    private CustomTextField   txtSearch;
    private JComboBox<String> cboFilterNganh, cboFilterKetQua;
    private CustomButton      btnSearch, btnReset;
    private CustomButton      btnAdd, btnEdit, btnDelete, btnXetTuyen, btnXemKetQua;

    private CustomTable       tblData;
    private DefaultTableModel tableModel;

    private CustomButton btnPrev, btnNext;
    private JLabel       lblPageInfo, lblTotalRecords;

    private int    currentPage    = 1;
    private int    totalPages     = 1;
    private String currentKeyword = "";
    private String filterNganh    = "";
    private String filterKetQua   = "";

    public QuanLyNguyenVongPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UIConstants.BACKGROUND_COLOR);
        setOpaque(true);

        buildNorthArea();
        buildTable();
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

        JLabel lblTitle = new JLabel("NGUYỆN VỌNG & XÉT TUYỂN");
        lblTitle.setFont(UIConstants.FONT_TITLE);
        lblTitle.setForeground(Color.WHITE);

        lblTotalRecords = new JLabel("Đang tải...");
        lblTotalRecords.setFont(UIConstants.FONT_NORMAL);
        lblTotalRecords.setForeground(UIConstants.PRIMARY_LIGHT);

        pnlHeader.add(lblTitle,        BorderLayout.WEST);
        pnlHeader.add(lblTotalRecords, BorderLayout.EAST);

        // Toolbar dòng 1: Filter
        JPanel pnlFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlFilter.setOpaque(false);
        pnlFilter.setBorder(new EmptyBorder(8, 15, 0, 15));

        cboFilterNganh = new JComboBox<>(new String[]{"Tất cả ngành"});
        cboFilterNganh.setFont(UIConstants.FONT_NORMAL);
        cboFilterNganh.setPreferredSize(new Dimension(180, 34));

        cboFilterKetQua = new JComboBox<>(new String[]{
            "Tất cả KQ",
            XetTuyenEngine.KQ_TRUNG_TUYEN,
            XetTuyenEngine.KQ_TRUOT_NV,
            XetTuyenEngine.KQ_TRUOT_NGANH,
            XetTuyenEngine.KQ_KHONG_XET,
            XetTuyenEngine.KQ_CHUA_XET
        });
        cboFilterKetQua.setFont(UIConstants.FONT_NORMAL);
        cboFilterKetQua.setPreferredSize(new Dimension(150, 34));

        pnlFilter.add(new JLabel("Ngành:") {{ setFont(UIConstants.FONT_BOLD); }});
        pnlFilter.add(cboFilterNganh);
        pnlFilter.add(Box.createHorizontalStrut(10));
        pnlFilter.add(new JLabel("Kết quả:") {{ setFont(UIConstants.FONT_BOLD); }});
        pnlFilter.add(cboFilterKetQua);

        // Toolbar dòng 2: Search + Actions
        JPanel pnlToolbar = new JPanel(new BorderLayout(10, 0));
        pnlToolbar.setOpaque(false);
        pnlToolbar.setBorder(new EmptyBorder(6, 15, 8, 15));

        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlSearch.setOpaque(false);
        JLabel lblSearch = new JLabel("Tìm kiếm:");
        lblSearch.setFont(UIConstants.FONT_BOLD);
        txtSearch = new CustomTextField(18);
        txtSearch.setPreferredSize(new Dimension(200, 36));
        txtSearch.setToolTipText("Nhập CCCD hoặc mã ngành");
        btnSearch = new CustomButton("Tìm",     UIConstants.PRIMARY_COLOR);
        btnReset  = new CustomButton("Xóa lọc", UIConstants.GRAY_COLOR);
        btnSearch.setPreferredSize(new Dimension(80, 36));
        btnReset.setPreferredSize(new Dimension(100, 36));
        pnlSearch.add(lblSearch); pnlSearch.add(txtSearch);
        pnlSearch.add(btnSearch); pnlSearch.add(btnReset);

        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pnlActions.setOpaque(false);
        btnAdd       = new CustomButton("+ Thêm",       UIConstants.SUCCESS_COLOR);
        btnEdit      = new CustomButton("Sửa",          UIConstants.PRIMARY_COLOR);
        btnDelete    = new CustomButton("Xóa",          UIConstants.DANGER_COLOR);
        btnXemKetQua = new CustomButton("Xem KQ",      new Color(124, 58, 237));
        btnXetTuyen  = new CustomButton("Chạy xét tuyển", new Color(5, 150, 105));
        for (CustomButton b : new CustomButton[]{btnAdd, btnEdit, btnDelete, btnXemKetQua}) {
            b.setPreferredSize(new Dimension(110, 36));
            pnlActions.add(b);
        }
        btnXetTuyen.setPreferredSize(new Dimension(140, 36));
        pnlActions.add(btnXetTuyen);

        pnlToolbar.add(pnlSearch,  BorderLayout.WEST);
        pnlToolbar.add(pnlActions, BorderLayout.EAST);

        JSeparator sep = new JSeparator();
        sep.setForeground(UIConstants.BORDER_COLOR);
        JPanel toolbarWrapper = new JPanel(new BorderLayout());
        toolbarWrapper.setOpaque(false);
        toolbarWrapper.add(pnlFilter,  BorderLayout.NORTH);
        toolbarWrapper.add(pnlToolbar, BorderLayout.CENTER);
        toolbarWrapper.add(sep,        BorderLayout.SOUTH);

        pnlNorth.add(pnlHeader,      BorderLayout.NORTH);
        pnlNorth.add(toolbarWrapper, BorderLayout.CENTER);
        add(pnlNorth, BorderLayout.NORTH);
    }

    // ── TABLE ────────────────────────────────────────────────────────────
    private void buildTable() {
        String[] cols = {"ID","TT","CCCD","Họ tên","Mã ngành","Tên ngành",
                         "Tổ hợp","PT","DTHGXT","DC","Ưu tiên","DXT","Kết quả"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tblData = new CustomTable(tableModel);
        tblData.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblData.setRowHeight(28);

        int[] widths = {45,40,120,150,80,150,65,55,65,55,60,65,110};
        for (int i = 0; i < widths.length; i++)
            tblData.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Căn giữa số
        for (int c : new int[]{0,1,7,8,9,10,11})
            tblData.getColumnModel().getColumn(c).setCellRenderer(CustomTable.centerRenderer());
        // Cột kết quả: renderer màu
        tblData.getColumnModel().getColumn(12).setCellRenderer(new KetQuaXetTuyenDialog.KetQuaCellRenderer());

        JScrollPane scroll = new JScrollPane(tblData);
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
    }

    // ── FOOTER ──────────────────────────────────────────────────────────
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
        cboFilterNganh.addActionListener(e -> applyFilter());
        cboFilterKetQua.addActionListener(e -> applyFilter());

        btnSearch.addActionListener(e -> doSearch());
        txtSearch.addActionListener(e -> doSearch());
        btnReset.addActionListener(e -> {
            txtSearch.setText(""); currentKeyword = ""; currentPage = 1; loadData();
        });

        btnPrev.addActionListener(e -> { if (currentPage > 1)         { currentPage--; loadData(); } });
        btnNext.addActionListener(e -> { if (currentPage < totalPages) { currentPage++; loadData(); } });

        btnAdd.addActionListener(e -> {
            NguyenVongDialog dlg = new NguyenVongDialog(getParentWindow(), null, bus);
            dlg.setVisible(true);
            if (dlg.isSaved()) { currentPage = 1; loadData(); }
        });

        btnEdit.addActionListener(e -> openEdit());

        btnDelete.addActionListener(e -> {
            NguyenVongXetTuyen nv = getSelected();
            if (nv == null) return;
            int ok = JOptionPane.showConfirmDialog(this,
                    "Xóa nguyện vọng của CCCD \"" + nv.getCccd() + "\" - Ngành \"" + nv.getMaNganh() + "\"?",
                    "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (ok == JOptionPane.YES_OPTION) {
                String result = bus.deleteNguyenVong(nv.getIdNv());
                if (result.startsWith("Success")) {
                    if (tableModel.getRowCount() == 1 && currentPage > 1) currentPage--;
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(this, result, "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnXetTuyen.addActionListener(e -> {
            XetTuyenDialog dlg = new XetTuyenDialog(getParentWindow(), bus);
            dlg.setVisible(true);
            if (dlg.isCompleted()) { currentPage = 1; loadData(); }
        });

        btnXemKetQua.addActionListener(e -> {
            KetQuaXetTuyenDialog dlg = new KetQuaXetTuyenDialog(getParentWindow(), bus);
            dlg.setVisible(true);
        });
    }

    private void applyFilter() {
        String selNganh = (String) cboFilterNganh.getSelectedItem();
        filterNganh = (selNganh == null || selNganh.startsWith("Tất cả")) ? "" : selNganh.split(" - ")[0].trim();

        String selKq = (String) cboFilterKetQua.getSelectedItem();
        filterKetQua = (selKq == null || selKq.startsWith("Tất cả")) ? "" : selKq;

        currentPage = 1;
        currentKeyword = "";
        txtSearch.setText("");
        loadData();
    }

    private void openEdit() {
        NguyenVongXetTuyen nv = getSelected();
        if (nv == null) return;
        NguyenVongDialog dlg = new NguyenVongDialog(getParentWindow(), nv, bus);
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

        List<NguyenVongXetTuyen> list;
        if (currentKeyword.isEmpty()) {
            list       = bus.getList(currentPage, filterNganh, filterKetQua);
            totalPages = bus.calculateTotalPages(filterNganh, filterKetQua);
            lblTotalRecords.setText("Tổng: " + bus.getTotalCount(filterNganh, filterKetQua) + " bản ghi");
        } else {
            list       = bus.search(currentPage, currentKeyword);
            totalPages = bus.calculateSearchTotalPages(currentKeyword);
            lblTotalRecords.setText("Kết quả: " + bus.getSearchCount(currentKeyword) + " bản ghi");
        }

        lblPageInfo.setText("Trang " + currentPage + " / " + totalPages);
        btnPrev.setEnabled(currentPage > 1);
        btnNext.setEnabled(currentPage < totalPages);

        if (list == null) return;
        for (NguyenVongXetTuyen nv : list) {
            ThiSinh ts   = nv.getThiSinh();
            Nganh   ng   = nv.getNganh();
            String hoTen   = ts != null ? ts.getHo() + " " + ts.getTen() : "";
            String tenNganh = ng != null ? ng.getTenNganh() : "";

            tableModel.addRow(new Object[]{
                nv.getIdNv(),
                nv.getThuTuNguyenVong(),
                s(nv.getCccd()),
                hoTen,
                s(nv.getMaNganh()),
                tenNganh,
                s(nv.getToHopMon()),
                s(nv.getPhuongThuc()),
                f(nv.getDiemThxt()),
                f(nv.getDiemCong()),
                f(nv.getDiemUtqd()),
                f(nv.getDiemXetTuyen()),
                s(nv.getKetQua()),
            });
        }
    }

    // ── HELPER ───────────────────────────────────────────────────────────
    private NguyenVongXetTuyen getSelected() {
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
