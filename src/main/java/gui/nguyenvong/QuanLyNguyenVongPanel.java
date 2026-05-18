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
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

public class QuanLyNguyenVongPanel extends JPanel {

    private final NguyenVongBUS bus = new NguyenVongBUS();

    private CustomTextField   txtSearch;
    private JComboBox<ComboItem> cboFilterNganh;
    private JComboBox<String> cboFilterKetQua;
    private CustomButton      btnSearch, btnReset;
    private CustomButton      btnImport, btnAdd, btnEdit, btnDelete, btnXetTuyen, btnXemKetQua;

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

        cboFilterNganh = new JComboBox<>();
        cboFilterNganh.addItem(new ComboItem("", "Tất cả ngành"));
        try {
            bus.NganhBUS nganhBUS = new bus.NganhBUS();
            java.util.List<Nganh> listNganh = nganhBUS.getAll();
            if (listNganh != null) {
                for (Nganh n : listNganh) {
                    cboFilterNganh.addItem(new ComboItem(n.getMaNganh(), n.getTenNganh()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        cboFilterNganh.setFont(UIConstants.FONT_NORMAL);
        cboFilterNganh.setPreferredSize(new Dimension(250, 34));

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
        btnImport    = new CustomButton("Import",   UIConstants.TEAL_COLOR);
        btnAdd       = new CustomButton("+ Thêm",       UIConstants.SUCCESS_COLOR);
        btnEdit      = new CustomButton("Sửa",          UIConstants.PRIMARY_COLOR);
        btnDelete    = new CustomButton("Xóa",          UIConstants.DANGER_COLOR);
        btnXemKetQua = new CustomButton("Xem KQ",      new Color(124, 58, 237));
        btnXetTuyen  = new CustomButton("Chạy xét tuyển", new Color(5, 150, 105));
        for (CustomButton b : new CustomButton[]{btnImport, btnAdd, btnEdit, btnDelete, btnXemKetQua}) {
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
        String[] cols = {"ID","TTNV","CCCD","Họ tên","Mã ngành","Tên ngành",
                         "Tổ hợp","PT","DTHXT","ĐC","ĐƯT","ĐXT","Kết quả"};
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

        btnImport.addActionListener(e -> doImportExcel());

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
        ComboItem selNganh = (ComboItem) cboFilterNganh.getSelectedItem();
        filterNganh = (selNganh == null) ? "" : selNganh.getKey();

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

    private void doImportExcel() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn file Excel nguyện vọng (.xlsx)");
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
                        JOptionPane.showMessageDialog(QuanLyNguyenVongPanel.this, errorMessage, "Lỗi Import", JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(QuanLyNguyenVongPanel.this, 
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

    static class ComboItem {
        private String key;
        private String value;
        public ComboItem(String key, String value) {
            this.key = key;
            this.value = value;
        }
        public String getKey() { return key; }
        public String getValue() { return value; }
        @Override
        public String toString() { return value; }
    }
}
