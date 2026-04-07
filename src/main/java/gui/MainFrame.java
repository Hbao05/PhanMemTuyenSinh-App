package gui;

import gui.style.UIConstants;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame{
    private JPanel pnlSidebar;
    private JPanel pnlContent;
    private CardLayout cardLayout;

    public MainFrame() {
        setTitle("Hệ Thống Quản Lý Tuyển Sinh 2026 - Admin Panel");
        setSize(1280, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        initSidebar();
        initContentPanel();

        add(pnlSidebar, BorderLayout.WEST);
        add(pnlContent, BorderLayout.CENTER);
    }

    private void initSidebar() {
        pnlSidebar = new JPanel();
        pnlSidebar.setLayout(new BoxLayout(pnlSidebar, BoxLayout.Y_AXIS));
        pnlSidebar.setBackground(UIConstants.TABLE_HEADER_COLOR); // Dùng màu tối cho ngầu
        pnlSidebar.setPreferredSize(new Dimension(250, 0));

        // Tiêu đề Sidebar
        JLabel lblTitle = new JLabel("MENU QUẢN LÝ");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(20, 0, 30, 0));
        pnlSidebar.add(lblTitle);

        // Tạo các nút bấm cho 9 chức năng
        addMenuButton("1. Quản lý Người dùng", "USER");
        addMenuButton("2. Quản lý Thí sinh", "CANDIDATE");
        addMenuButton("3. Quản lý Ngành", "MAJOR");
        addMenuButton("4. Quản lý Tổ hợp môn", "SUBJECT_GROUP");
        addMenuButton("5. Ngành - Tổ hợp", "MAJOR_GROUP");
        addMenuButton("6. Quản lý Điểm thi", "SCORE");
        addMenuButton("7. Quản lý Điểm cộng", "BONUS");
        addMenuButton("8. Nguyện vọng & Xét tuyển", "ADMISSION");
        addMenuButton("9. Bảng quy đổi", "CONVERSION");

        pnlSidebar.add(Box.createVerticalGlue()); // Đẩy nút Thoát xuống dưới cùng

        // Nút Thoát
        JButton btnExit = new JButton("Đăng xuất / Thoát");
        btnExit.setMaximumSize(new Dimension(250, 40));
        btnExit.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnExit.setBackground(UIConstants.DANGER_COLOR);
        btnExit.setForeground(Color.WHITE);
        btnExit.setFocusPainted(false);
        btnExit.addActionListener(e -> System.exit(0));
        pnlSidebar.add(btnExit);
    }

    private void addMenuButton(String title, String cardName) {
        JButton btn = new JButton(title);
        btn.setMaximumSize(new Dimension(250, 45)); // Kích thước cố định cho nút menu
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setFont(UIConstants.FONT_BOLD);
        btn.setBackground(UIConstants.TABLE_HEADER_COLOR);
        btn.setForeground(Color.LIGHT_GRAY);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY)); // Đường viền mờ ở dưới
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hiệu ứng hover chuột
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(70, 90, 110)); // Sáng lên khi di chuột
                btn.setForeground(Color.WHITE);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(UIConstants.TABLE_HEADER_COLOR); // Trở về bình thường
                btn.setForeground(Color.LIGHT_GRAY);
            }
        });

        // Sự kiện chuyển màn hình khi bấm nút
        btn.addActionListener(e -> cardLayout.show(pnlContent, cardName));

        pnlSidebar.add(btn);
    }

    private void initContentPanel() {
        cardLayout = new CardLayout();
        pnlContent = new JPanel(cardLayout);

        // --- CẮM CÁC PANEL CHỨC NĂNG VÀO ĐÂY ---

        // 1. Placeholder cho Quản lý Người dùng
        pnlContent.add(createPlaceholder("Màn hình Quản lý Người dùng"), "USER");

        // 2. Chức năng Quản lý Thí sinh (Cắm đồ thật vào!)
        pnlContent.add(new QuanLyThiSinhGUI(), "CANDIDATE");

        // 3 đến 9. Placeholder cho các màn hình chưa làm
        pnlContent.add(createPlaceholder("Màn hình Quản lý Ngành"), "MAJOR");
        pnlContent.add(createPlaceholder("Màn hình Quản lý Tổ hợp môn"), "SUBJECT_GROUP");
        pnlContent.add(createPlaceholder("Màn hình Quản lý Ngành - Tổ hợp"), "MAJOR_GROUP");
        pnlContent.add(createPlaceholder("Màn hình Quản lý Điểm thi"), "SCORE");
        pnlContent.add(createPlaceholder("Màn hình Quản lý Điểm cộng"), "BONUS");
        pnlContent.add(createPlaceholder("Màn hình Nguyện vọng & Xét tuyển"), "ADMISSION");
        pnlContent.add(createPlaceholder("Màn hình Bảng quy đổi"), "CONVERSION");

        // Mặc định khi mở app lên sẽ hiển thị màn hình Thí sinh
        cardLayout.show(pnlContent, "CANDIDATE");
    }

    // Hàm tạo màn hình tạm thời cho những chức năng chưa code
    private JPanel createPlaceholder(String text) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UIConstants.BACKGROUND_COLOR);
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 24));
        label.setForeground(Color.GRAY);
        panel.add(label);
        return panel;
    }
}
