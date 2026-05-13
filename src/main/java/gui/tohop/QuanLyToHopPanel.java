package gui.tohop;

import bus.ToHopMonThiBUS;
import entity.ToHopMonThi;
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

public class QuanLyToHopPanel extends JPanel {

    private final ToHopMonThiBUS bus;

    private CustomTextField txtSearch;
    private CustomButton    btnSearch, btnReset;
    private CustomButton btnAdd, btnEdit, btnDelete, btnImport;

    private CustomTable       tblToHop;
    private DefaultTableModel tableModel;

    private CustomButton btnPrev, btnNext;
    private JLabel       lblPageInfo, lblTotalRecords;

    private int    currentPage    = 1;
    private int    totalPages     = 1;
    private String currentKeyword = "";

    public QuanLyToHopPanel() {
        this.bus = new ToHopMonThiBUS();

        setLayout(new BorderLayout(0, 0));
        setBackground(UIConstants.BACKGROUND_COLOR);
        setOpaque(true);

        buildNorthArea();
        buildTable();
        buildFooter();
        setupEvents();

        loadData();
    }

    private void buildNorthArea() {
        JPanel pnlNorth = new JPanel(new BorderLayout());
        pnlNorth.setOpaque(false);

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(UIConstants.PRIMARY_COLOR);
        pnlHeader.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel lblTitle = new JLabel("QUẢN LÝ TỔ HỢP MÔN THI");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);

        lblTotalRecords = new JLabel("Đang tải...");
        lblTotalRecords.setFont(UIConstants.FONT_NORMAL);
        lblTotalRecords.setForeground(new Color(189, 215, 238));

        pnlHeader.add(lblTitle, BorderLayout.WEST);
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
        txtSearch.setToolTipText("Nhập mã tổ hợp hoặc tên tổ hợp");

        btnSearch = new CustomButton("Tim", UIConstants.PRIMARY_COLOR);
        btnSearch.setPreferredSize(new Dimension(100, 36));

        btnReset = new CustomButton("Xoa loc", new Color(120, 120, 120));
        btnReset.setPreferredSize(new Dimension(110, 36));

        pnlSearch.add(lblSearch);
        pnlSearch.add(txtSearch);
        pnlSearch.add(btnSearch);
        pnlSearch.add(btnReset);

        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlActions.setOpaque(false);

        btnAdd    = new CustomButton("+ Thêm mới",  UIConstants.SUCCESS_COLOR);
        btnEdit   = new CustomButton("Sua",           UIConstants.PRIMARY_COLOR);
        btnDelete = new CustomButton("Xoa",      UIConstants.DANGER_COLOR);
        btnImport = new CustomButton("Import",   UIConstants.TEAL_COLOR);

        for (CustomButton b : new CustomButton[]{btnAdd, btnEdit, btnDelete, btnImport}) {
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

    private void buildTable() {
        String[] cols = {"ID", "Mã Tổ Hợp", "Môn 1", "Môn 2", "Môn 3", "Tên Tổ Hợp"};

        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return c == 0 ? Integer.class : String.class;
            }
        };

        tblToHop = new CustomTable(tableModel);
        tblToHop.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblToHop.setRowHeight(28);

        int[] widths = {50, 100, 120, 120, 120, 200};
        for (int i = 0; i < widths.length; i++) {
            tblToHop.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        tblToHop.getColumnModel().getColumn(0).setCellRenderer(CustomTable.centerRenderer());
        tblToHop.getColumnModel().getColumn(1).setCellRenderer(CustomTable.centerRenderer());

        JScrollPane scroll = new JScrollPane(tblToHop);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
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
                new MatteBorder(1, 0, 0, 0, new Color(220, 220, 220)),
                new EmptyBorder(8, 15, 10, 15)));

        JPanel pnlPaging = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        pnlPaging.setOpaque(false);

        btnPrev = new CustomButton("◀ Trước", UIConstants.PRIMARY_COLOR);
        btnPrev.setPreferredSize(new Dimension(105, 32));

        lblPageInfo = new JLabel("Trang 1 / 1");
        lblPageInfo.setFont(UIConstants.FONT_BOLD);
        lblPageInfo.setForeground(UIConstants.TABLE_HEADER_COLOR);

        btnNext = new CustomButton("Sau ▶", UIConstants.PRIMARY_COLOR);
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

        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            currentKeyword = "";
            currentPage    = 1;
            loadData();
        });

        btnPrev.addActionListener(e -> { if (currentPage > 1) { currentPage--; loadData(); } });
        btnNext.addActionListener(e -> { if (currentPage < totalPages) { currentPage++; loadData(); } });

        btnAdd.addActionListener(e -> {
            ToHopDialog dlg = new ToHopDialog(SwingUtilities.getWindowAncestor(this), null, bus);
            dlg.setVisible(true);
            if (dlg.isSaved()) { currentPage = 1; loadData(); }
        });

        btnEdit.addActionListener(e -> {
            ToHopMonThi t = getSelected();
            if (t == null) return;
            ToHopDialog dlg = new ToHopDialog(SwingUtilities.getWindowAncestor(this), t, bus);
            dlg.setVisible(true);
            if (dlg.isSaved()) loadData();
        });

        btnDelete.addActionListener(e -> {
            int row = tblToHop.getSelectedRow();
            if (row < 0) { warn("Vui lòng chọn tổ hợp cần xóa!"); return; }

            String ma  = (String) tblToHop.getValueAt(row, 1);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc muốn xóa tổ hợp " + ma + "?",
                    "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                int id = (int) tblToHop.getValueAt(row, 0);
                String result = bus.deleteToHop(id);
                if (result.startsWith("Success")) {
                    JOptionPane.showMessageDialog(this, "Đã xóa tổ hợp thành công!");
                    if (tableModel.getRowCount() == 1 && currentPage > 1) currentPage--;
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(this, result, "Lỗi xóa", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnImport.addActionListener(e -> doImportExcel());
    }

    private void doSearch() {
        currentKeyword = txtSearch.getText().trim();
        currentPage    = 1;
        loadData();
    }

    private void loadData() {
        tableModel.setRowCount(0);

        List<ToHopMonThi> list;
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
            lblTotalRecords.setText("Kết quả: \"" + currentKeyword + "\"  —  " + found + " tổ hợp");
        } else {
            long total = bus.getTotalCount();
            lblTotalRecords.setText("Tổng cộng: " + total + " tổ hợp");
        }

        if (list != null) {
            for (ToHopMonThi t : list) {
                tableModel.addRow(new Object[]{
                        t.getIdToHop(),
                        t.getMaToHop(),
                        t.getMon1(),
                        t.getMon2(),
                        t.getMon3(),
                        t.getTenToHop() != null ? t.getTenToHop() : ""
                });
            }
        }
    }

    private void doImportExcel() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));
        fc.setDialogTitle("Chọn file Excel Danh sách Tổ hợp môn");

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
        bar.setString("Đang import danh sách tổ hợp...");
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
                List<ToHopMonThi> danhSach = ExcelUtil.readToHopExcel(file);
                return bus.importToHop(danhSach);
            }
            @Override
            protected void done() {
                loadingDialog.dispose();
                try {
                    String result = get();
                    JOptionPane.showMessageDialog(QuanLyToHopPanel.this, result,
                            "Kết quả Import", JOptionPane.INFORMATION_MESSAGE);
                    currentPage    = 1;
                    currentKeyword = "";
                    txtSearch.setText("");
                    loadData();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(QuanLyToHopPanel.this,
                            "Lỗi khi Import:\n" + ex.getMessage(),
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
        loadingDialog.setVisible(true);
    }

    private ToHopMonThi getSelected() {
        int row = tblToHop.getSelectedRow();
        if (row < 0) { warn("Vui lòng chọn một tổ hợp trong danh sách!"); return null; }
        return bus.getById((int) tblToHop.getValueAt(row, 0));
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Chưa chọn", JOptionPane.WARNING_MESSAGE);
    }
}
