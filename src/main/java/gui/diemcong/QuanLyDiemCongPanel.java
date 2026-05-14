package gui.diemcong;

import bus.DiemCongBUS;
import entity.DiemCongXetTuyen;
import entity.ThiSinh;
import gui.component.CustomButton;
import gui.component.CustomTable;
import gui.component.CustomTextField;
import gui.style.UIConstants;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

public class QuanLyDiemCongPanel extends JPanel {

    private final DiemCongBUS bus = new DiemCongBUS();

    private CustomTextField txtSearch;
    private CustomButton    btnSearch, btnReset;
    private CustomButton    btnImport, btnAdd, btnEdit, btnDelete;

    private CustomTable       tblData;
    private DefaultTableModel tableModel;

    private CustomButton btnPrev, btnNext;
    private JLabel       lblPageInfo, lblTotalRecords;

    private int    currentPage    = 1;
    private int    totalPages     = 1;
    private String currentKeyword = "";

    public QuanLyDiemCongPanel() {
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

        JLabel lblTitle = new JLabel("QUẢN LÝ ĐIỂM CỘNG");
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
        JLabel lblSearch = new JLabel("Tìm kiếm:");
        lblSearch.setFont(UIConstants.FONT_BOLD);
        txtSearch = new CustomTextField(22);
        txtSearch.setPreferredSize(new Dimension(260, 36));
        txtSearch.setToolTipText("Nhập CCCD, mã ngành, mã tổ hợp");
        btnSearch = new CustomButton("Tìm",     UIConstants.PRIMARY_COLOR);
        btnReset  = new CustomButton("Xóa lọc", UIConstants.GRAY_COLOR);
        btnSearch.setPreferredSize(new Dimension(90, 36));
        btnReset.setPreferredSize(new Dimension(110, 36));
        pnlSearch.add(lblSearch);
        pnlSearch.add(txtSearch);
        pnlSearch.add(btnSearch);
        pnlSearch.add(btnReset);

        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pnlActions.setOpaque(false);
        btnImport     = new CustomButton("Import",   UIConstants.TEAL_COLOR);
        btnAdd    = new CustomButton("+ Thêm", UIConstants.SUCCESS_COLOR);
        btnEdit   = new CustomButton("Sửa",    UIConstants.PRIMARY_COLOR);
        btnDelete = new CustomButton("Xóa",    UIConstants.DANGER_COLOR);
        for (CustomButton b : new CustomButton[]{btnImport, btnAdd, btnEdit, btnDelete}) {
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

    // ── TABLE ────────────────────────────────────────────────────────────
    private void buildTable() {
        String[] cols = {"ID", "CCCD", "Họ tên thí sinh", "Mã ngành", "Mã tổ hợp",
                         "Phương thức", "Điểm CC", "Điểm UTXT", "Tổng", "Ghi chú"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                if (c == 0) return Integer.class;
                if (c >= 6 && c <= 8) return Double.class;
                return String.class;
            }
        };

        tblData = new CustomTable(tableModel);
        tblData.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblData.setRowHeight(28);

        int[] widths = {45, 120, 160, 90, 90, 100, 70, 80, 60, 140};
        for (int i = 0; i < widths.length; i++)
            tblData.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        for (int c : new int[]{0, 6, 7, 8})
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

        btnPrev.addActionListener(e -> { if (currentPage > 1)         { currentPage--; loadData(); } });
        btnNext.addActionListener(e -> { if (currentPage < totalPages) { currentPage++; loadData(); } });

        btnImport.addActionListener(e -> doImportExcel());

        btnAdd.addActionListener(e -> {
            DiemCongDialog dlg = new DiemCongDialog(getParentWindow(), null, bus);
            dlg.setVisible(true);
            if (dlg.isSaved()) { currentPage = 1; loadData(); }
        });

        btnEdit.addActionListener(e -> {
            DiemCongXetTuyen dc = getSelected();
            if (dc == null) return;
            DiemCongDialog dlg = new DiemCongDialog(getParentWindow(), dc, bus);
            dlg.setVisible(true);
            if (dlg.isSaved()) loadData();
        });

        btnDelete.addActionListener(e -> {
            DiemCongXetTuyen dc = getSelected();
            if (dc == null) return;
            int ok = JOptionPane.showConfirmDialog(this,
                    "Xóa điểm cộng của CCCD \"" + dc.getCccd() + "\" - Ngành \"" + dc.getMaNganh() + "\"?",
                    "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (ok == JOptionPane.YES_OPTION) {
                String result = bus.deleteDiemCong(dc.getIdDiemCong());
                if (result.startsWith("Success")) {
                    if (tableModel.getRowCount() == 1 && currentPage > 1) currentPage--;
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(this, result, "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        tblData.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    DiemCongXetTuyen dc = getSelected();
                    if (dc != null) {
                        DiemCongDialog dlg = new DiemCongDialog(getParentWindow(), dc, bus);
                        dlg.setVisible(true);
                        if (dlg.isSaved()) loadData();
                    }
                }
            }
        });
    }

    private void doSearch() {
        currentKeyword = txtSearch.getText().trim();
        currentPage    = 1;
        loadData();
    }

    private void doImportExcel() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn file Excel điểm cộng (.xlsx)");
        chooser.setFileFilter(new FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            
            JDialog progressDialog = new JDialog(getParentWindow(), "Đang import...", Dialog.ModalityType.APPLICATION_MODAL);
            progressDialog.setSize(400, 150);
            progressDialog.setLocationRelativeTo(this);
            progressDialog.setLayout(new BorderLayout(10, 10));
            
            JLabel lblStatus = new JLabel("Đang chuẩn bị đọc file...", SwingConstants.CENTER);
            lblStatus.setFont(UIConstants.FONT_NORMAL);
            lblStatus.setBorder(new EmptyBorder(10, 10, 0, 10));
            
            JProgressBar progressBar = new JProgressBar();
            progressBar.setIndeterminate(true);
            progressBar.setStringPainted(true);
            progressBar.setString("Đang xử lý...");
            
            JPanel centerPanel = new JPanel(new BorderLayout());
            centerPanel.setBorder(new EmptyBorder(10, 20, 20, 20));
            centerPanel.add(progressBar, BorderLayout.CENTER);
            
            progressDialog.add(lblStatus, BorderLayout.NORTH);
            progressDialog.add(centerPanel, BorderLayout.CENTER);
            
            SwingWorker<Void, String> worker = new SwingWorker<>() {
                private int finalSuccess = 0;
                private int finalTotal = 0;
                private String errorMessage = null;

                @Override
                protected Void doInBackground() {
                    bus.importFromExcel(file, (processed, success, message) -> {
                        finalTotal = processed;
                        finalSuccess = success;
                        if (message.startsWith("Lỗi")) {
                            errorMessage = message;
                        }
                        publish("Đã đọc: " + processed + " | Thành công: " + success + " (" + message + ")");
                    });
                    return null;
                }

                @Override
                protected void process(List<String> chunks) {
                    if (!chunks.isEmpty()) {
                        String latestMessage = chunks.get(chunks.size() - 1);
                        lblStatus.setText(latestMessage);
                        progressBar.setString(latestMessage);
                    }
                }

                @Override
                protected void done() {
                    progressDialog.dispose();
                    if (errorMessage != null) {
                        JOptionPane.showMessageDialog(QuanLyDiemCongPanel.this, errorMessage, "Lỗi Import", JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(QuanLyDiemCongPanel.this, 
                            "Import thành công!\nTổng dòng đã đọc: " + finalTotal + "\nSố dòng thêm/cập nhật: " + finalSuccess, 
                            "Hoàn thành", JOptionPane.INFORMATION_MESSAGE);
                        currentPage = 1;
                        loadData();
                    }
                }
            };
            
            worker.execute();
            progressDialog.setVisible(true); // Blocks until disposed
        }
    }

    // ── LOAD DATA ────────────────────────────────────────────────────────
    private void loadData() {
        tableModel.setRowCount(0);

        List<DiemCongXetTuyen> list;
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
        for (DiemCongXetTuyen dc : list) {
            String hoTen = "";
            ThiSinh ts = dc.getThiSinh();
            if (ts != null) hoTen = ts.getHo() + " " + ts.getTen();

            tableModel.addRow(new Object[]{
                    dc.getIdDiemCong(),
                    dc.getCccd()       != null ? dc.getCccd()       : "",
                    hoTen,
                    dc.getMaNganh()    != null ? dc.getMaNganh()    : "",
                    dc.getMaToHop()    != null ? dc.getMaToHop()    : "",
                    dc.getPhuongThuc() != null ? dc.getPhuongThuc() : "",
                    dc.getDiemCc()     != null ? dc.getDiemCc()     : 0.0,
                    dc.getDiemUtXt()   != null ? dc.getDiemUtXt()   : 0.0,
                    dc.getDiemTong()   != null ? dc.getDiemTong()   : 0.0,
                    dc.getGhiChu()     != null ? dc.getGhiChu()     : "",
            });
        }
    }

    // ── HELPER ───────────────────────────────────────────────────────────
    private DiemCongXetTuyen getSelected() {
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
}
