package gui.nguoidung;

import app.Session;
import bus.NguoiDungBUS;
import entity.NguoiDung;
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

public class QuanLyNguoiDungPanel extends JPanel {
    private final NguoiDungBUS bus = new NguoiDungBUS();

    private CustomTextField txtSearch;
    private CustomButton btnSearch, btnReset;
    private CustomButton btnAdd, btnEdit, btnChangePass, btnToggleRole, btnToggleActive, btnDelete;

    private CustomTable tblUsers;
    private DefaultTableModel tableModel;

    private CustomButton btnPrev, btnNext;
    private JLabel lblPageInfo, lblTotalRecords;

    private int currentPage = 1;
    private int totalPages = 1;
    private String currentKeyword = "";

    public QuanLyNguoiDungPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UIConstants.BACKGROUND_COLOR);
        setOpaque(true);

        buildHeader();
        buildToolbar();
        buildTable();
        buildFooter();
        setupEvents();
        loadData();
    }

    private void buildHeader() {
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(UIConstants.PRIMARY_COLOR);
        pnlHeader.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel lblTitle = new JLabel("QUẢN LÝ NGƯỜI DÙNG");
        lblTitle.setFont(UIConstants.FONT_TITLE);
        lblTitle.setForeground(Color.WHITE);

        lblTotalRecords = new JLabel("Đang tải...");
        lblTotalRecords.setFont(UIConstants.FONT_NORMAL);
        lblTotalRecords.setForeground(UIConstants.PRIMARY_LIGHT);

        pnlHeader.add(lblTitle, BorderLayout.WEST);
        pnlHeader.add(lblTotalRecords, BorderLayout.EAST);
        add(pnlHeader, BorderLayout.NORTH);
    }

    private void buildToolbar() {
        JPanel pnlToolbar = new JPanel(new BorderLayout(10, 0));
        pnlToolbar.setOpaque(false);
        pnlToolbar.setBorder(new EmptyBorder(10, 15, 8, 15));

        // Search - txtSearch co giãn lấp khoảng trống
        JPanel pnlSearch = new JPanel(new BorderLayout(10, 0));
        pnlSearch.setOpaque(false);
        JLabel lblSearch = new JLabel("Tìm kiếm:");
        lblSearch.setFont(UIConstants.FONT_BOLD);
        txtSearch = new CustomTextField(22);
        txtSearch.setPreferredSize(new Dimension(240, 36));
        txtSearch.setToolTipText("Nhập username hoặc họ tên");
        btnSearch = new CustomButton("Tìm", UIConstants.PRIMARY_COLOR);
        btnSearch.setPreferredSize(new Dimension(90, 36));
        btnReset = new CustomButton("Xóa lọc", UIConstants.GRAY_COLOR);
        btnReset.setPreferredSize(new Dimension(110, 36));

        JPanel pnlSearchBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlSearchBtns.setOpaque(false);
        pnlSearchBtns.add(btnSearch);
        pnlSearchBtns.add(btnReset);

        pnlSearch.add(lblSearch, BorderLayout.WEST);
        pnlSearch.add(txtSearch, BorderLayout.CENTER);
        pnlSearch.add(pnlSearchBtns, BorderLayout.EAST);

        // Actions
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlActions.setOpaque(false);
        btnAdd          = new CustomButton("+ Thêm",      UIConstants.SUCCESS_COLOR);
        btnEdit         = new CustomButton("Sửa",          UIConstants.PRIMARY_COLOR);
        btnChangePass   = new CustomButton("Đổi MK",       UIConstants.WARNING_COLOR);
        btnToggleRole   = new CustomButton("Đổi quyền",    UIConstants.TEAL_COLOR);
        btnToggleActive = new CustomButton("Bật/Tắt",   UIConstants.PURPLE_COLOR);
        btnDelete       = new CustomButton("Xóa",          UIConstants.DANGER_COLOR);
        btnAdd.setPreferredSize(new Dimension(95, 36));
        btnEdit.setPreferredSize(new Dimension(75, 36));
        btnChangePass.setPreferredSize(new Dimension(95, 36));
        btnToggleRole.setPreferredSize(new Dimension(105, 36));
        btnToggleActive.setPreferredSize(new Dimension(95, 36));
        btnDelete.setPreferredSize(new Dimension(75, 36));
        for (CustomButton b : new CustomButton[]{btnAdd, btnEdit, btnChangePass, btnToggleRole, btnToggleActive, btnDelete}) {
            pnlActions.add(b);
        }

        pnlToolbar.add(pnlSearch, BorderLayout.CENTER);
        pnlToolbar.add(pnlActions, BorderLayout.EAST);

        JSeparator sep = new JSeparator();
        sep.setForeground(UIConstants.BORDER_COLOR);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(pnlToolbar, BorderLayout.CENTER);
        wrapper.add(sep, BorderLayout.SOUTH);

        // Header + toolbar trong 1 panel NORTH
        JPanel pnlNorth = (JPanel) getComponent(0);
        JPanel combined = new JPanel(new BorderLayout());
        combined.setOpaque(false);
        combined.add(pnlNorth, BorderLayout.NORTH);
        combined.add(wrapper, BorderLayout.CENTER);

        remove(pnlNorth);
        add(combined, BorderLayout.NORTH);
    }

    private void buildTable() {
        String[] cols = {"ID", "Username", "Họ tên", "Email", "SĐT", "Quyền", "Trạng thái"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) { return c == 0 ? Integer.class : String.class; }
        };
        tblUsers = new CustomTable(tableModel);
        tblUsers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        int[] widths = {50, 130, 160, 180, 110, 90, 100};
        for (int i = 0; i < widths.length; i++)
            tblUsers.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Center: ID, Quyền, Trạng thái
        for (int c : new int[]{0, 5, 6})
            tblUsers.getColumnModel().getColumn(c).setCellRenderer(CustomTable.centerRenderer());

        // Badge renderer cho Quyền
        tblUsers.getColumnModel().getColumn(5).setCellRenderer(new CustomTable.ZebraRenderer(SwingConstants.CENTER) {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (!sel) {
                    if ("ADMIN".equals(v)) { setForeground(UIConstants.DANGER_COLOR); setFont(UIConstants.FONT_BOLD); }
                    else                   { setForeground(UIConstants.PRIMARY_COLOR); setFont(UIConstants.FONT_NORMAL); }
                }
                return this;
            }
        });

        // Badge renderer cho Trạng thái
        tblUsers.getColumnModel().getColumn(6).setCellRenderer(new CustomTable.ZebraRenderer(SwingConstants.CENTER) {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (!sel) {
                    if ("Hoạt động".equals(v)) setForeground(UIConstants.SUCCESS_COLOR);
                    else                        setForeground(UIConstants.DANGER_COLOR);
                }
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(tblUsers);
        scroll.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR));
        scroll.getViewport().setBackground(Color.WHITE);

        JPanel pnlCenter = new JPanel(new BorderLayout());
        pnlCenter.setOpaque(false);
        pnlCenter.setBorder(new EmptyBorder(0, 15, 0, 15));
        pnlCenter.add(scroll, BorderLayout.CENTER);
        add(pnlCenter, BorderLayout.CENTER);
    }

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
        pnlPaging.add(btnPrev);
        pnlPaging.add(lblPageInfo);
        pnlPaging.add(btnNext);

        pnlFooter.add(pnlPaging, BorderLayout.CENTER);
        add(pnlFooter, BorderLayout.SOUTH);
    }

    private void setupEvents() {
        btnSearch.addActionListener(e -> doSearch());
        txtSearch.addActionListener(e -> doSearch());
        btnReset.addActionListener(e -> { txtSearch.setText(""); currentKeyword = ""; currentPage = 1; loadData(); });

        btnPrev.addActionListener(e -> { if (currentPage > 1) { currentPage--; loadData(); } });
        btnNext.addActionListener(e -> { if (currentPage < totalPages) { currentPage++; loadData(); } });

        btnAdd.addActionListener(e -> {
            ThemNguoiDungDialog dlg = new ThemNguoiDungDialog(getParentWindow(), bus);
            dlg.setVisible(true);
            if (dlg.isSaved()) { currentPage = 1; loadData(); }
        });

        btnEdit.addActionListener(e -> {
            NguoiDung nd = getSelectedUser();
            if (nd == null) return;
            SuaNguoiDungDialog dlg = new SuaNguoiDungDialog(getParentWindow(), nd, bus);
            dlg.setVisible(true);
            if (dlg.isUpdated()) loadData();
        });

        btnChangePass.addActionListener(e -> {
            NguoiDung nd = getSelectedUser();
            if (nd == null) return;
            DoiMatKhauDialog dlg = new DoiMatKhauDialog(getParentWindow(), nd, bus);
            dlg.setVisible(true);
        });

        btnToggleRole.addActionListener(e -> {
            NguoiDung nd = getSelectedUser();
            if (nd == null) return;
            // Không cho tự đổi quyền của chính mình
            if (Session.getCurrentUser().getId().equals(nd.getId())) {
                JOptionPane.showMessageDialog(this, "Không thể đổi quyền của tài khoản đang đăng nhập!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String newRole = nd.getQuyen() == NguoiDung.Quyen.ADMIN ? "USER" : "ADMIN";
            int ok = JOptionPane.showConfirmDialog(this,
                    "Đổi quyền của \"" + nd.getUsername() + "\" thành " + newRole + "?",
                    "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) {
                String result = bus.toggleRole(nd.getId());
                if (result.startsWith("Success")) loadData();
                else JOptionPane.showMessageDialog(this, result, "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnToggleActive.addActionListener(e -> {
            NguoiDung nd = getSelectedUser();
            if (nd == null) return;
            if (Session.getCurrentUser().getId().equals(nd.getId())) {
                JOptionPane.showMessageDialog(this, "Không thể tắt tài khoản đang đăng nhập!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String action = nd.isKichHoat() ? "khóa" : "mở khóa";
            int ok = JOptionPane.showConfirmDialog(this,
                    "Bạn muốn " + action + " tài khoản \"" + nd.getUsername() + "\"?",
                    "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) {
                String result = bus.toggleActive(nd.getId());
                if (result.startsWith("Success")) loadData();
                else JOptionPane.showMessageDialog(this, result, "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnDelete.addActionListener(e -> {
            NguoiDung nd = getSelectedUser();
            if (nd == null) return;
            if (Session.getCurrentUser().getId().equals(nd.getId())) {
                JOptionPane.showMessageDialog(this, "Không thể xóa tài khoản đang đăng nhập!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int ok = JOptionPane.showConfirmDialog(this,
                    "Xóa người dùng \"" + nd.getUsername() + "\"?",
                    "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (ok == JOptionPane.YES_OPTION) {
                String result = bus.deleteUser(nd.getId());
                if (result.startsWith("Success")) {
                    if (tableModel.getRowCount() == 1 && currentPage > 1) currentPage--;
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(this, result, "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        tblUsers.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    NguoiDung nd = getSelectedUser();
                    if (nd != null) {
                        SuaNguoiDungDialog dlg = new SuaNguoiDungDialog(getParentWindow(), nd, bus);
                        dlg.setVisible(true);
                        if (dlg.isUpdated()) loadData();
                    }
                }
            }
        });
    }

    private void doSearch() {
        currentKeyword = txtSearch.getText().trim();
        currentPage = 1;
        loadData();
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<NguoiDung> list;

        if (currentKeyword.isEmpty()) {
            list = bus.getList(currentPage);
            totalPages = bus.calculateTotalPages();
            lblTotalRecords.setText("Tổng: " + bus.getTotalCount() + " người dùng");
        } else {
            list = bus.search(currentPage, currentKeyword);
            totalPages = bus.calculateSearchTotalPages(currentKeyword);
            lblTotalRecords.setText("Kết quả: " + bus.getSearchCount(currentKeyword) + " người dùng");
        }

        lblPageInfo.setText("Trang " + currentPage + " / " + totalPages);
        btnPrev.setEnabled(currentPage > 1);
        btnNext.setEnabled(currentPage < totalPages);

        if (list == null) return;
        for (NguoiDung nd : list) {
            tableModel.addRow(new Object[]{
                    nd.getId(),
                    nd.getUsername(),
                    nd.getHoTen() != null ? nd.getHoTen() : "",
                    nd.getEmail() != null ? nd.getEmail() : "",
                    nd.getDienThoai() != null ? nd.getDienThoai() : "",
                    nd.getQuyen().name(),
                    nd.isKichHoat() ? "Hoạt động" : "Đã khóa"
            });
        }
    }

    private NguoiDung getSelectedUser() {
        int row = tblUsers.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn người dùng!", "Chưa chọn", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        int id = (int) tblUsers.getValueAt(row, 0);
        return bus.getById(id);
    }

    private Window getParentWindow() {
        return SwingUtilities.getWindowAncestor(this);
    }
}
