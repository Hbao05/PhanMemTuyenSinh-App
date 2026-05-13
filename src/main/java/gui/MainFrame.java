package gui;

import app.Session;
import gui.bangquydoi.QuanLyBangQuyDoiPanel;
import gui.diemcong.QuanLyDiemCongPanel;
import gui.diemthi.QuanLyDiemThiPanel;
import gui.nguyenvong.QuanLyNguyenVongPanel;
import gui.nguoidung.QuanLyNguoiDungPanel;
import gui.nganh.QuanLyNganhPanel;
import gui.nganhtohop.QuanLyNganhToHopPanel;
import gui.style.UIConstants;
import gui.thisinh.QuanLyThiSinhPanel;
import gui.tohop.QuanLyToHopPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {
    private JPanel pnlContent;
    private CardLayout cardLayout;
    private final List<SidebarItem> menuItems = new ArrayList<>();

    public MainFrame() {
        setTitle("He Thong Quan Ly Tuyen Sinh 2026");
        setSize(1280, 720);
        setMinimumSize(new Dimension(1024, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        initSidebar();
        initContentPanel();
    }

    // ── Sidebar ─────────────────────────────────────────────────────
    private void initSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(UIConstants.SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(240, 0));

        sidebar.add(buildLogoPanel(),   BorderLayout.NORTH);
        sidebar.add(buildNavPanel(),    BorderLayout.CENTER);
        sidebar.add(buildFooterPanel(), BorderLayout.SOUTH);

        add(sidebar, BorderLayout.WEST);
    }

    private JPanel buildLogoPanel() {
        JPanel pnl = new JPanel();
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        pnl.setBackground(UIConstants.SIDEBAR_BG);
        pnl.setBorder(BorderFactory.createEmptyBorder(24, 20, 16, 20));

        // Logo: vẽ hình tròn "TS" bằng Java2D thay cho emoji
        JPanel logoCircle = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIConstants.PRIMARY_COLOR);
                g2.fillOval(0, 0, 48, 48);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 17));
                FontMetrics fm = g2.getFontMetrics();
                String text = "TS";
                g2.drawString(text, (48 - fm.stringWidth(text)) / 2, (48 + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(48, 48); }
            @Override public Dimension getMaximumSize()   { return new Dimension(48, 48); }
        };
        logoCircle.setOpaque(false);
        logoCircle.setAlignmentX(CENTER_ALIGNMENT);

        JLabel title = new JLabel("TUYEN SINH");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(Color.WHITE);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("He thong quan ly 2026");
        subtitle.setFont(UIConstants.FONT_SMALL);
        subtitle.setForeground(UIConstants.SIDEBAR_TEXT);
        subtitle.setAlignmentX(CENTER_ALIGNMENT);

        String displayName = Session.getDisplayName();
        String role = Session.isAdmin() ? "ADMIN" : "USER";
        JLabel lblUser = new JLabel(displayName + "  [" + role + "]");
        lblUser.setFont(UIConstants.FONT_SMALL);
        lblUser.setForeground(new Color(144, 205, 244));
        lblUser.setAlignmentX(CENTER_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(55, 65, 81));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        pnl.add(logoCircle);
        pnl.add(Box.createVerticalStrut(10));
        pnl.add(title);
        pnl.add(Box.createVerticalStrut(2));
        pnl.add(subtitle);
        pnl.add(Box.createVerticalStrut(10));
        pnl.add(lblUser);
        pnl.add(Box.createVerticalStrut(14));
        pnl.add(sep);
        return pnl;
    }

    private JPanel buildNavPanel() {
        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBackground(UIConstants.SIDEBAR_BG);
        nav.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

        String[][] entries = {
            {"Quản lý Người dùng",      "USER"},
            {"Quản lý Thí sinh",        "CANDIDATE"},
            {"Quản lý Ngành",           "MAJOR"},
            {"Tổ hợp môn thi",          "SUBJECT_GROUP"},
            {"Ngành - Tổ hợp",          "MAJOR_GROUP"},
            {"Quản lý Điểm thi",        "SCORE"},
            {"Quản lý Điểm cộng",       "BONUS"},
            {"Nguyện vọng & Xét tuyển", "ADMISSION"},
            {"Bảng quy đổi",            "CONVERSION"},
        };

        for (String[] entry : entries) {
            if ("USER".equals(entry[1]) && !Session.isAdmin()) continue;
            SidebarItem item = new SidebarItem(entry[0], entry[1]);
            nav.add(item);
            menuItems.add(item);
        }

        menuItems.stream()
                .filter(i -> "CANDIDATE".equals(i.cardName))
                .findFirst()
                .ifPresent(i -> i.setActive(true));

        nav.add(Box.createVerticalGlue());
        return nav;
    }

    private JPanel buildFooterPanel() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(UIConstants.SIDEBAR_BG);
        pnl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(55, 65, 81)),
                BorderFactory.createEmptyBorder(12, 14, 16, 14)));

        JButton btnExit = new JButton("Dang xuat / Thoat");
        btnExit.setFont(UIConstants.FONT_BOLD);
        btnExit.setForeground(new Color(252, 129, 129));
        btnExit.setBackground(new Color(55, 30, 30));
        btnExit.setOpaque(true);
        btnExit.setContentAreaFilled(true);
        btnExit.setBorderPainted(false);
        btnExit.setFocusPainted(false);
        btnExit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExit.setPreferredSize(new Dimension(Integer.MAX_VALUE, 38));
        btnExit.addActionListener(e -> System.exit(0));
        btnExit.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                btnExit.setBackground(UIConstants.DANGER_COLOR);
                btnExit.setForeground(Color.WHITE);
            }
            @Override public void mouseExited(MouseEvent e) {
                btnExit.setBackground(new Color(55, 30, 30));
                btnExit.setForeground(new Color(252, 129, 129));
            }
        });

        pnl.add(btnExit, BorderLayout.CENTER);
        return pnl;
    }

    // ── Content panel ────────────────────────────────────────────────
    private void initContentPanel() {
        cardLayout = new CardLayout();
        pnlContent = new JPanel(cardLayout);
        pnlContent.setBackground(UIConstants.BACKGROUND_COLOR);

        pnlContent.add(new QuanLyNguoiDungPanel(),                          "USER");
        pnlContent.add(new QuanLyThiSinhPanel(),                            "CANDIDATE");
        pnlContent.add(new QuanLyNganhPanel(),                              "MAJOR");
        pnlContent.add(new QuanLyToHopPanel(),                              "SUBJECT_GROUP");
        pnlContent.add(new QuanLyNganhToHopPanel(),                         "MAJOR_GROUP");
        pnlContent.add(new QuanLyDiemThiPanel(),                            "SCORE");
        pnlContent.add(new QuanLyDiemCongPanel(),                           "BONUS");
        pnlContent.add(new QuanLyNguyenVongPanel(),                         "ADMISSION");
        pnlContent.add(new QuanLyBangQuyDoiPanel(),                         "CONVERSION");

        cardLayout.show(pnlContent, "CANDIDATE");
        add(pnlContent, BorderLayout.CENTER);
    }

    private JPanel createPlaceholder(String name) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UIConstants.BACKGROUND_COLOR);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(40, 60, 40, 60)));

        JLabel lblName = new JLabel(name, SwingConstants.CENTER);
        lblName.setFont(UIConstants.FONT_TITLE);
        lblName.setForeground(UIConstants.TABLE_HEADER_COLOR);
        lblName.setAlignmentX(CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Chuc nang dang duoc phat trien...", SwingConstants.CENTER);
        lblSub.setFont(UIConstants.FONT_NORMAL);
        lblSub.setForeground(UIConstants.GRAY_COLOR);
        lblSub.setAlignmentX(CENTER_ALIGNMENT);

        card.add(lblName);
        card.add(Box.createVerticalStrut(8));
        card.add(lblSub);
        panel.add(card);
        return panel;
    }

    // ── Sidebar Item ─────────────────────────────────────────────────
    private class SidebarItem extends JPanel {
        final String cardName;
        private boolean active  = false;
        private boolean hovered = false;

        SidebarItem(String label, String cardName) {
            this.cardName = cardName;
            setLayout(new BorderLayout());
            setOpaque(false);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            setPreferredSize(new Dimension(240, 44));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            // Thanh accent trái khi active
            JPanel accent = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    g.setColor(active ? UIConstants.PRIMARY_COLOR : UIConstants.SIDEBAR_BG);
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            };
            accent.setPreferredSize(new Dimension(4, 0));
            accent.setOpaque(false);

            JLabel lblText = new JLabel(label, SwingConstants.CENTER);
            lblText.setFont(UIConstants.FONT_NORMAL);
            lblText.setForeground(UIConstants.SIDEBAR_TEXT);

            add(accent, BorderLayout.WEST);
            add(lblText, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                @Override public void mouseExited (MouseEvent e) { hovered = false; repaint(); }
                @Override public void mouseClicked(MouseEvent e) {
                    menuItems.forEach(i -> i.setActive(false));
                    setActive(true);
                    cardLayout.show(pnlContent, cardName);
                }
            });
        }

        void setActive(boolean a) {
            active = a;
            for (Component c : getComponents()) setLabelColors(c);
            repaint();
        }

        private void setLabelColors(Component c) {
            if (c instanceof JLabel lbl)
                lbl.setForeground(active ? Color.WHITE : UIConstants.SIDEBAR_TEXT);
            else if (c instanceof Container ctn)
                for (Component child : ctn.getComponents()) setLabelColors(child);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            if (active)        g2.setColor(new Color(41, 128, 185, 40));
            else if (hovered)  g2.setColor(UIConstants.SIDEBAR_HOVER);
            else               g2.setColor(UIConstants.SIDEBAR_BG);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
            new MainFrame().setVisible(true);
        });
    }
}
