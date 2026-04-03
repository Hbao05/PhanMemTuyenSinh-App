package gui;

import bus.ThiSinhBUS;
import entity.ThiSinh;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class QuanLyThiSinhGUI extends JPanel {
    private final ThiSinhBUS candidateBUS;

    // UI Components
    private JTextField txtSearch;
    private JButton btnSearch;
    private JButton btnImport;
    private JTable tblCandidates;
    private DefaultTableModel tableModel;
    private JButton btnPrev, btnNext, btnEdit;
    private JLabel lblPageInfo;

    // Pagination & Search States
    private int currentPage = 1;
    private int totalPages = 1;
    private String currentKeyword = "";

    public QuanLyThiSinhGUI() {
        this.candidateBUS = new ThiSinhBUS();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initComponents();
        setupEventHandlers();
        loadDataToTable(); // Load data when panel is created
    }

    private void initComponents() {
        // ================= 1. TOP PANEL: Search & Import =================
        JPanel pnlTop = new JPanel(new BorderLayout());

        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlSearch.add(new JLabel("Search (CCCD / Name):"));
        txtSearch = new JTextField(25);
        btnSearch = new JButton("Search");
        pnlSearch.add(txtSearch);
        pnlSearch.add(btnSearch);

        JPanel pnlAction = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnImport = new JButton("Import from Excel");
        pnlAction.add(btnImport);

        pnlTop.add(pnlSearch, BorderLayout.WEST);
        pnlTop.add(pnlAction, BorderLayout.EAST);
        add(pnlTop, BorderLayout.NORTH);

        // ================= 2. CENTER PANEL: Data Table =================
        String[] columns = {"ID", "CCCD", "Last Name (Họ)", "First Name (Tên)", "DOB", "Phone", "Gender"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Prevent direct editing on the table
            }
        };
        tblCandidates = new JTable(tableModel);
        tblCandidates.setRowHeight(25);
        tblCandidates.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));

        add(new JScrollPane(tblCandidates), BorderLayout.CENTER);

        // ================= 3. BOTTOM PANEL: Pagination & Edit =================
        JPanel pnlBottom = new JPanel(new BorderLayout());

        JPanel pnlPagination = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPrev = new JButton("<< Prev");
        lblPageInfo = new JLabel("Page 1 / 1");
        btnNext = new JButton("Next >>");

        pnlPagination.add(btnPrev);
        pnlPagination.add(lblPageInfo);
        pnlPagination.add(btnNext);

        JPanel pnlEdit = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnEdit = new JButton("Edit Selected Candidate");
        pnlEdit.add(btnEdit);

        pnlBottom.add(pnlPagination, BorderLayout.CENTER);
        pnlBottom.add(pnlEdit, BorderLayout.EAST);
        add(pnlBottom, BorderLayout.SOUTH);
    }

    private void setupEventHandlers() {
        // Handle Search
        btnSearch.addActionListener(e -> {
            currentKeyword = txtSearch.getText().trim();
            currentPage = 1; // Reset to first page on new search
            loadDataToTable();
        });

        // Handle Previous Page
        btnPrev.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                loadDataToTable();
            }
        });

        // Handle Next Page
        btnNext.addActionListener(e -> {
            if (currentPage < totalPages) {
                currentPage++;
                loadDataToTable();
            }
        });

        // Handle Edit Button
        btnEdit.addActionListener(e -> {
            int selectedRow = tblCandidates.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(this, "Please select a candidate from the table to edit.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Extract basic data from the selected row (using ID or CCCD to fetch full object if needed)
            int id = (int) tblCandidates.getValueAt(selectedRow, 0);
            String cccd = (String) tblCandidates.getValueAt(selectedRow, 1);
            String ho = (String) tblCandidates.getValueAt(selectedRow, 2);
            String ten = (String) tblCandidates.getValueAt(selectedRow, 3);

            // Note: In a real app, you would open an EditDialog here and pass the ID or Candidate object
            // Example:
            // UpdateCandidateDialog dialog = new UpdateCandidateDialog(SwingUtilities.getWindowAncestor(this), id, candidateBUS);
            // dialog.setVisible(true);
            // if(dialog.isUpdated()) { loadDataToTable(); }

            JOptionPane.showMessageDialog(this, "Edit dialog will open for Candidate: " + ho + " " + ten + " (CCCD: " + cccd + ")", "Edit Action", JOptionPane.INFORMATION_MESSAGE);
        });

        // Handle Import Button
        btnImport.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                // Future integration: Pass the selected file to ExcelUtil, get List<ThiSinh>, then call candidateBUS.importCandidates(list)
                JOptionPane.showMessageDialog(this, "Selected file: " + fileChooser.getSelectedFile().getName() + "\n(Excel reading logic will be implemented here)", "Import Action", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    // ================= CORE DATA LOADING METHOD =================
    private void loadDataToTable() {
        tableModel.setRowCount(0); // Clear existing rows
        List<ThiSinh> list;

        // Routing logic: Search vs Normal Listing
        if (currentKeyword.isEmpty()) {
            list = candidateBUS.getList(currentPage);
            totalPages = candidateBUS.calculateTotalPages();
        } else {
            list = candidateBUS.search(currentPage, currentKeyword);
            totalPages = candidateBUS.calculateSearchTotalPages(currentKeyword);
        }

        // Update UI state
        lblPageInfo.setText("Page " + currentPage + " / " + totalPages);
        btnPrev.setEnabled(currentPage > 1);
        btnNext.setEnabled(currentPage < totalPages);

        // Populate Table
        if (list != null && !list.isEmpty()) {
            for (ThiSinh candidate : list) {
                Object[] rowData = {
                        candidate.getIdThiSinh(),
                        candidate.getCccd(),
                        candidate.getHo(),
                        candidate.getTen(),
                        candidate.getNgaySinh(),
                        candidate.getDienThoai(),
                        candidate.getGioiTinh()
                };
                tableModel.addRow(rowData);
            }
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Quản lý thí sinh");

            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(900, 600);
            frame.setLocationRelativeTo(null);

            frame.setContentPane(new QuanLyThiSinhGUI());

            frame.setVisible(true);
        });
    }
}
