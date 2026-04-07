package gui;

import bus.ThiSinhBUS;
import entity.ThiSinh;
import gui.style.UIConstants;
import gui.component.CustomButton;
import gui.component.CustomTable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class QuanLyThiSinhGUI extends JPanel {
    private final ThiSinhBUS candidateBUS;

    // Khai báo các thành phần giao diện
    private JTextField txtSearch;
    private CustomButton btnSearch, btnImport, btnPrev, btnNext, btnEdit;
    private CustomTable tblCandidates;
    private DefaultTableModel tableModel;
    private JLabel lblPageInfo;

    // Biến lưu trạng thái phân trang
    private int currentPage = 1;
    private int totalPages = 1;
    private String currentKeyword = "";

    public QuanLyThiSinhGUI() {
        this.candidateBUS = new ThiSinhBUS();
        setLayout(new BorderLayout(15, 15));
        setBackground(UIConstants.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        initComponents();
        setupEventHandlers();
        loadDataToTable(); // Tải dữ liệu lần đầu
    }

    private void initComponents() {
        // === 1. KHU VỰC TRÊN CÙNG: Tìm kiếm & Import ===
        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setOpaque(false); // Xóa nền để lấy màu của nền gốc

        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlSearch.setOpaque(false);
        JLabel lblSearch = new JLabel("Tìm kiếm (CCCD/Tên):");
        lblSearch.setFont(UIConstants.FONT_NORMAL);
        txtSearch = new JTextField(25);
        txtSearch.setFont(UIConstants.FONT_NORMAL);
        txtSearch.setPreferredSize(new Dimension(250, 35));

        btnSearch = new CustomButton("Tìm kiếm", UIConstants.PRIMARY_COLOR);
        pnlSearch.add(lblSearch);
        pnlSearch.add(txtSearch);
        pnlSearch.add(btnSearch);

        JPanel pnlAction = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pnlAction.setOpaque(false);
        btnImport = new CustomButton("Import Excel", UIConstants.SUCCESS_COLOR);
        pnlAction.add(btnImport);

        pnlTop.add(pnlSearch, BorderLayout.WEST);
        pnlTop.add(pnlAction, BorderLayout.EAST);
        add(pnlTop, BorderLayout.NORTH);

        // === 2. KHU VỰC GIỮA: Bảng dữ liệu ===
        String[] columns = {"ID", "CCCD", "Họ", "Tên", "Ngày Sinh", "Giới Tính", "Khu Vực"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblCandidates = new CustomTable(tableModel);
        add(new JScrollPane(tblCandidates), BorderLayout.CENTER);

        // === 3. KHU VỰC DƯỚI CÙNG: Phân trang & Sửa ===
        JPanel pnlBottom = new JPanel(new BorderLayout());
        pnlBottom.setOpaque(false);

        JPanel pnlPagination = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        pnlPagination.setOpaque(false);
        btnPrev = new CustomButton("<< Trước", UIConstants.PRIMARY_COLOR);
        lblPageInfo = new JLabel("Trang 1 / 1");
        lblPageInfo.setFont(UIConstants.FONT_BOLD);
        btnNext = new CustomButton("Sau >>", UIConstants.PRIMARY_COLOR);

        pnlPagination.add(btnPrev);
        pnlPagination.add(lblPageInfo);
        pnlPagination.add(btnNext);

        JPanel pnlEdit = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pnlEdit.setOpaque(false);
        btnEdit = new CustomButton("Sửa Thí Sinh", UIConstants.PRIMARY_COLOR);
        pnlEdit.setPreferredSize(new Dimension(150, 35)); // Nút này chữ dài nên cho to ra tí
        pnlEdit.add(btnEdit);

        pnlBottom.add(pnlPagination, BorderLayout.CENTER);
        pnlBottom.add(pnlEdit, BorderLayout.EAST);
        add(pnlBottom, BorderLayout.SOUTH);
    }

    private void setupEventHandlers() {
        // Sự kiện: Tìm kiếm
        btnSearch.addActionListener(e -> {
            currentKeyword = txtSearch.getText().trim();
            currentPage = 1;
            loadDataToTable();
        });

        // Sự kiện: Phân trang (Trang trước)
        btnPrev.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                loadDataToTable();
            }
        });

        // Sự kiện: Phân trang (Trang sau)
        btnNext.addActionListener(e -> {
            if (currentPage < totalPages) {
                currentPage++;
                loadDataToTable();
            }
        });

        // Sự kiện: Sửa thông tin
        btnEdit.addActionListener(e -> {
            int selectedRow = tblCandidates.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một thí sinh trong bảng để sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // Lấy ID thí sinh từ cột 0
            int idTs = (int) tblCandidates.getValueAt(selectedRow, 0);
            String cccd = (String) tblCandidates.getValueAt(selectedRow, 1);

            // TODO: Mở hộp thoại sửa (Chúng ta sẽ làm Form sửa sau)
            JOptionPane.showMessageDialog(this, "Chức năng mở hộp thoại sửa cho thí sinh có CCCD: " + cccd, "Sửa", JOptionPane.INFORMATION_MESSAGE);
        });

        // Sự kiện: Import
        btnImport.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                // TODO: Gọi thư viện đọc Excel ở đây
                JOptionPane.showMessageDialog(this, "Đã chọn file: " + fileChooser.getSelectedFile().getName(), "Import", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    // Hàm gọi BUS để nạp dữ liệu lên bảng
    private void loadDataToTable() {
        tableModel.setRowCount(0);
        List<ThiSinh> list;

        if (currentKeyword.isEmpty()) {
            list = candidateBUS.getList(currentPage);
            totalPages = candidateBUS.calculateTotalPages();
        } else {
            list = candidateBUS.search(currentPage, currentKeyword);
            totalPages = candidateBUS.calculateSearchTotalPages(currentKeyword);
        }

        lblPageInfo.setText("Trang " + currentPage + " / " + totalPages);
        btnPrev.setEnabled(currentPage > 1);
        btnNext.setEnabled(currentPage < totalPages);

        if (list != null) {
            for (ThiSinh ts : list) {
                tableModel.addRow(new Object[]{
                        ts.getIdThiSinh(), ts.getCccd(), ts.getHo(), ts.getTen(),
                        ts.getNgaySinh(), ts.getGioiTinh(), ts.getKhuVuc()
                });
            }
        }
    }

    public static void main(String[] args) {
        JFrame f = new JFrame();
        QuanLyThiSinhGUI test = new QuanLyThiSinhGUI();
        f.setContentPane(new QuanLyThiSinhGUI());
        f.setVisible(true);

    }
}
