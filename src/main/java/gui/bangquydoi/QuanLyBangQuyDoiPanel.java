package gui.bangquydoi;

import bus.BangQuyDoiBUS;
import entity.BangQuyDoi;
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

public class QuanLyBangQuyDoiPanel extends JPanel {

    private final BangQuyDoiBUS bus = new BangQuyDoiBUS();

    private CustomTextField txtSearch;
    private CustomButton    btnSearch, btnReset;
    private CustomButton    btnAdd, btnEdit, btnDelete, btnImport;

    private CustomTable       tblData;
    private DefaultTableModel tableModel;

    private CustomButton btnPrev, btnNext;
    private JLabel       lblPageInfo, lblTotalRecords;

    private int    currentPage    = 1;
    private int    totalPages     = 1;
    private String currentKeyword = "";

    public QuanLyBangQuyDoiPanel() {
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

        // Header
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(UIConstants.PRIMARY_COLOR);
        pnlHeader.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel lblTitle = new JLabel("BẢNG QUY ĐỔI ĐIỂM");
        lblTitle.setFont(UIConstants.FONT_TITLE);
        lblTitle.setForeground(Color.WHITE);

        lblTotalRecords = new JLabel("Đang tải...");
        lblTotalRecords.setFont(UIConstants.FONT_NORMAL);
        lblTotalRecords.setForeground(UIConstants.PRIMARY_LIGHT);

        pnlHeader.add(lblTitle,        BorderLayout.WEST);
        pnlHeader.add(lblTotalRecords, BorderLayout.EAST);

        // Toolbar
        JPanel pnlToolbar = new JPanel(new BorderLayout(10, 0));
        pnlToolbar.setOpaque(false);
        pnlToolbar.setBorder(new EmptyBorder(10, 15, 8, 15));

        JPanel pnlSearch = new JPanel(new BorderLayout(10, 0));
        pnlSearch.setOpaque(false);
        JLabel lblSearch = new JLabel("Tìm kiếm:");
        lblSearch.setFont(UIConstants.FONT_BOLD);
        txtSearch = new CustomTextField(22);
        txtSearch.setPreferredSize(new Dimension(260, 36));
        txtSearch.setToolTipText("Nhập mã quy đổi, phương thức, tổ hợp, môn");
        btnSearch = new CustomButton("Tìm",      UIConstants.PRIMARY_COLOR);
        btnReset  = new CustomButton("Xóa lọc",  UIConstants.GRAY_COLOR);
        btnSearch.setPreferredSize(new Dimension(90, 36));
        btnReset.setPreferredSize(new Dimension(110, 36));

        JPanel pnlSearchBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlSearchBtns.setOpaque(false);
        pnlSearchBtns.add(btnSearch);
        pnlSearchBtns.add(btnReset);

        pnlSearch.add(lblSearch, BorderLayout.WEST);
        pnlSearch.add(txtSearch, BorderLayout.CENTER);
        pnlSearch.add(pnlSearchBtns, BorderLayout.EAST);

        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlActions.setOpaque(false);
        btnAdd    = new CustomButton("+ Thêm",  UIConstants.SUCCESS_COLOR);
        btnEdit   = new CustomButton("Sửa",     UIConstants.PRIMARY_COLOR);
        btnDelete = new CustomButton("Xóa",     UIConstants.DANGER_COLOR);
        btnImport = new CustomButton("Import",  UIConstants.TEAL_COLOR);
        for (CustomButton b : new CustomButton[]{btnAdd, btnEdit, btnDelete, btnImport}) {
            b.setPreferredSize(new Dimension(110, 36));
            pnlActions.add(b);
        }

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

    // ── TABLE ────────────────────────────────────────────────────────────
    private void buildTable() {
        String[] cols = {"ID", "Mã QĐ", "Phương thức", "Tổ hợp", "Môn",
                         "Điểm A", "Điểm B", "Điểm C", "Điểm D", "Phạm vi"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                if (c == 0) return Integer.class;
                if (c >= 5 && c <= 8) return Double.class;
                return String.class;
            }
        };

        tblData = new CustomTable(tableModel);
        tblData.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblData.setRowHeight(28);

        int[] widths = {45, 100, 110, 80, 80, 65, 65, 65, 65, 120};
        for (int i = 0; i < widths.length; i++)
            tblData.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Căn giữa: ID, Diem A-D
        for (int c : new int[]{0, 5, 6, 7, 8})
            tblData.getColumnModel().getColumn(c).setCellRenderer(CustomTable.centerRenderer());

        JScrollPane scroll = new JScrollPane(tblData);
        scroll.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR));
        scroll.getViewport().setBackground(Color.WHITE);

        JPanel pnlCenter = new JPanel(new BorderLayout());
        pnlCenter.setOpaque(false);
        pnlCenter.setBorder(new EmptyBorder(0, 15, 0, 15));
        pnlCenter.add(scroll, BorderLayout.CENTER);
        add(pnlCenter, BorderLayout.CENTER);
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
        pnlPaging.add(btnPrev);
        pnlPaging.add(lblPageInfo);
        pnlPaging.add(btnNext);

        pnlFooter.add(pnlPaging, BorderLayout.CENTER);
        add(pnlFooter, BorderLayout.SOUTH);
    }

    // ── EVENTS ───────────────────────────────────────────────────────────
    private void setupEvents() {
        btnSearch.addActionListener(e -> doSearch());
        txtSearch.addActionListener(e -> doSearch());
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            currentKeyword = "";
            currentPage    = 1;
            loadData();
        });

        btnPrev.addActionListener(e -> { if (currentPage > 1)          { currentPage--; loadData(); } });
        btnNext.addActionListener(e -> { if (currentPage < totalPages)  { currentPage++; loadData(); } });

        btnAdd.addActionListener(e -> {
            BangQuyDoiDialog dlg = new BangQuyDoiDialog(getParentWindow(), null, bus);
            dlg.setVisible(true);
            if (dlg.isSaved()) { currentPage = 1; loadData(); }
        });

        btnEdit.addActionListener(e -> {
            BangQuyDoi bqd = getSelected();
            if (bqd == null) return;
            BangQuyDoiDialog dlg = new BangQuyDoiDialog(getParentWindow(), bqd, bus);
            dlg.setVisible(true);
            if (dlg.isSaved()) loadData();
        });

        btnDelete.addActionListener(e -> {
            BangQuyDoi bqd = getSelected();
            if (bqd == null) return;
            int ok = JOptionPane.showConfirmDialog(this,
                    "Xóa bản ghi \"" + bqd.getMaQuyDoi() + "\"?",
                    "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (ok == JOptionPane.YES_OPTION) {
                String result = bus.deleteBangQuyDoi(bqd.getIdQd());
                if (result.startsWith("Success")) {
                    if (tableModel.getRowCount() == 1 && currentPage > 1) currentPage--;
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(this, result, "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Double-click = sửa
        tblData.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    BangQuyDoi bqd = getSelected();
                    if (bqd != null) {
                        BangQuyDoiDialog dlg = new BangQuyDoiDialog(getParentWindow(), bqd, bus);
                        dlg.setVisible(true);
                        if (dlg.isSaved()) loadData();
                    }
                }
            }
        });

        // Import Excel
        btnImport.addActionListener(e -> doImportExcel());
    }

    // ── SEARCH ───────────────────────────────────────────────────────────
    private void doSearch() {
        currentKeyword = txtSearch.getText().trim();
        currentPage    = 1;
        loadData();
    }

    // ── LOAD DATA ────────────────────────────────────────────────────────
    private void loadData() {
        tableModel.setRowCount(0);

        List<BangQuyDoi> list;
        if (currentKeyword.isEmpty()) {
            list       = bus.getList(currentPage);
            totalPages = bus.calculateTotalPages();
            lblTotalRecords.setText("Tổng: " + bus.getTotalCount() + " bản ghi");
        } else {
            list       = bus.search(currentPage, currentKeyword);
            totalPages = bus.calculateSearchTotalPages(currentKeyword);
            lblTotalRecords.setText("Kết quả: " + bus.getSearchCount(currentKeyword) + " bản ghi");
        }

        lblPageInfo.setText("Trang " + currentPage + " / " + totalPages);
        btnPrev.setEnabled(currentPage > 1);
        btnNext.setEnabled(currentPage < totalPages);

        if (list == null) return;
        for (BangQuyDoi b : list) {
            tableModel.addRow(new Object[]{
                    b.getIdQd(),
                    b.getMaQuyDoi()    != null ? b.getMaQuyDoi()   : "",
                    b.getPhuongThuc()  != null ? b.getPhuongThuc() : "",
                    b.getToHop()       != null ? b.getToHop()       : "",
                    b.getMon()         != null ? b.getMon()         : "",
                    b.getDiemA(),
                    b.getDiemB(),
                    b.getDiemC(),
                    b.getDiemD(),
                    b.getPhanVi()      != null ? b.getPhanVi()      : ""
            });
        }
    }

    // ── HELPER ───────────────────────────────────────────────────────────
    private BangQuyDoi getSelected() {
        int row = tblData.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một bản ghi!", "Chưa chọn",
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }
        int id = (int) tblData.getValueAt(row, 0);
        return bus.getById(id);
    }

    private Window getParentWindow() {
        return SwingUtilities.getWindowAncestor(this);
    }

    /** Import bảng quy đổi từ Excel */
    private void doImportExcel() {
        javax.swing.filechooser.FileNameExtensionFilter filter =
                new javax.swing.filechooser.FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx");
        javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
        fc.setFileFilter(filter);
        fc.setDialogTitle("Chọn file Excel Bảng Quy Đổi");
        if (fc.showOpenDialog(this) != javax.swing.JFileChooser.APPROVE_OPTION) return;
        java.io.File file = fc.getSelectedFile();

        // Hỏi xóa dữ liệu cũ?
        int opt = JOptionPane.showConfirmDialog(this,
                "Bạn muốn XÓA tất cả dữ liệu cũ trước khi import?\n" +
                "(Chọn Yes = xóa hết rồi import, No = thêm vào dữ liệu hiện có)",
                "Xác nhận Import", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (opt == JOptionPane.CANCEL_OPTION) return;
        boolean replaceAll = (opt == JOptionPane.YES_OPTION);

        // Loading dialog
        JDialog loading = new JDialog(getParentWindow(), "Đang import...", Dialog.ModalityType.APPLICATION_MODAL);
        loading.setSize(350, 80);
        loading.setLocationRelativeTo(this);
        loading.setUndecorated(true);
        JProgressBar pb = new JProgressBar();
        pb.setIndeterminate(true);
        pb.setStringPainted(true);
        pb.setString("Đang đọc và import dữ liệu...");
        pb.setFont(UIConstants.FONT_BOLD);
        pb.setForeground(UIConstants.SUCCESS_COLOR);
        JPanel pnlLoad = new JPanel(new BorderLayout());
        pnlLoad.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.SUCCESS_COLOR, 2),
                new EmptyBorder(20, 20, 20, 20)));
        pnlLoad.add(pb);
        loading.add(pnlLoad);

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override protected String doInBackground() {
                return bus.importExcel(file, replaceAll);
            }
            @Override protected void done() {
                loading.dispose();
                try {
                    String result = get();
                    String msg = result.startsWith("Success") ? result.substring(8) : result;
                    JOptionPane.showMessageDialog(QuanLyBangQuyDoiPanel.this, msg,
                            result.startsWith("Success") ? "Kết quả Import" : "Lỗi",
                            result.startsWith("Success") ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
                    currentPage = 1;
                    currentKeyword = "";
                    txtSearch.setText("");
                    loadData();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(QuanLyBangQuyDoiPanel.this,
                            "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
        loading.setVisible(true);
    }
}
