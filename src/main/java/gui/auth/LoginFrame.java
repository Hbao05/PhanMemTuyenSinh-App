package gui.auth;

import app.Session;
import bus.NguoiDungBUS;
import entity.NguoiDung;
import gui.MainFrame;
import gui.component.CustomButton;
import gui.component.CustomTextField;
import gui.style.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class LoginFrame extends JFrame {
    private final NguoiDungBUS bus = new NguoiDungBUS();

    private CustomTextField txtUsername;
    private JPasswordField txtPassword;
    private CustomButton btnLogin;
    private JLabel lblError;

    private int failCount = 0;
    private static final int MAX_FAIL = 3;
    private static final int LOCK_SECONDS = 30;

    public LoginFrame() {
        setTitle("Đăng nhập — Hệ thống Tuyển sinh 2026");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setSize(420, 480);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIConstants.BACKGROUND_COLOR);
        setContentPane(root);

        // ── Header ──────────────────────────────────────────────────
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(UIConstants.PRIMARY_COLOR);
        header.setBorder(new EmptyBorder(30, 20, 30, 20));

        // Logo vẽ bằng Java2D (tránh lỗi emoji trên Swing)
        JPanel lblIcon = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 50));
                g2.fillOval(0, 0, 60, 60);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
                FontMetrics fm = g2.getFontMetrics();
                String t = "TS";
                g2.drawString(t, (60 - fm.stringWidth(t)) / 2, (60 + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(60, 60); }
            @Override public Dimension getMaximumSize()   { return new Dimension(60, 60); }
        };
        lblIcon.setOpaque(false);
        lblIcon.setAlignmentX(CENTER_ALIGNMENT);

        JLabel lblTitle = new JLabel("HE THONG TUYEN SINH 2026");
        lblTitle.setFont(UIConstants.FONT_TITLE);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setAlignmentX(CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Đăng nhập để tiếp tục");
        lblSub.setFont(UIConstants.FONT_NORMAL);
        lblSub.setForeground(new Color(214, 234, 248));
        lblSub.setAlignmentX(CENTER_ALIGNMENT);

        header.add(lblIcon);
        header.add(Box.createVerticalStrut(8));
        header.add(lblTitle);
        header.add(Box.createVerticalStrut(4));
        header.add(lblSub);
        root.add(header, BorderLayout.NORTH);

        // ── Form ────────────────────────────────────────────────────
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(28, 36, 28, 36));

        form.add(makeLabel("Tên đăng nhập"));
        form.add(Box.createVerticalStrut(6));
        txtUsername = new CustomTextField(20);
        txtUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        form.add(txtUsername);

        form.add(Box.createVerticalStrut(16));
        form.add(makeLabel("Mật khẩu"));
        form.add(Box.createVerticalStrut(6));

        txtPassword = new JPasswordField();
        txtPassword.setFont(UIConstants.FONT_NORMAL);
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        form.add(txtPassword);

        form.add(Box.createVerticalStrut(8));
        lblError = new JLabel(" ");
        lblError.setFont(UIConstants.FONT_SMALL);
        lblError.setForeground(UIConstants.DANGER_COLOR);
        lblError.setAlignmentX(LEFT_ALIGNMENT);
        form.add(lblError);

        form.add(Box.createVerticalStrut(16));
        btnLogin = new CustomButton("Đăng nhập", UIConstants.PRIMARY_COLOR);
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnLogin.setAlignmentX(LEFT_ALIGNMENT);
        btnLogin.addActionListener(e -> doLogin());
        form.add(btnLogin);

        root.add(form, BorderLayout.CENTER);

        // ── Footer ──────────────────────────────────────────────────
        JLabel lblFooter = new JLabel("Hệ thống quản lý tuyển sinh nội bộ", SwingConstants.CENTER);
        lblFooter.setFont(UIConstants.FONT_SMALL);
        lblFooter.setForeground(UIConstants.GRAY_COLOR);
        lblFooter.setBorder(new EmptyBorder(10, 0, 14, 0));
        root.add(lblFooter, BorderLayout.SOUTH);

        // Enter để đăng nhập
        KeyAdapter enterKey = new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) doLogin();
            }
        };
        txtUsername.addKeyListener(enterKey);
        txtPassword.addKeyListener(enterKey);
    }

    private JLabel makeLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIConstants.FONT_BOLD);
        lbl.setForeground(UIConstants.TABLE_HEADER_COLOR);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }

    private void doLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showError("Vui lòng nhập đầy đủ username và mật khẩu!");
            return;
        }

        NguoiDung user = bus.login(username, password);

        if (user == null) {
            failCount++;
            if (failCount >= MAX_FAIL) {
                lockUI();
            } else {
                showError("Sai username hoặc mật khẩu! (Lần " + failCount + "/" + MAX_FAIL + ")");
            }
            txtPassword.setText("");
            return;
        }

        // Đăng nhập thành công
        Session.login(user);
        dispose();
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }

    private void showError(String msg) {
        lblError.setText(msg);
    }

    private void lockUI() {
        btnLogin.setEnabled(false);
        txtUsername.setEnabled(false);
        txtPassword.setEnabled(false);

        int[] remaining = {LOCK_SECONDS};
        showError("Sai quá " + MAX_FAIL + " lần. Khóa " + remaining[0] + " giây...");

        Timer timer = new Timer(1000, null);
        timer.addActionListener(e -> {
            remaining[0]--;
            if (remaining[0] <= 0) {
                timer.stop();
                failCount = 0;
                btnLogin.setEnabled(true);
                txtUsername.setEnabled(true);
                txtPassword.setEnabled(true);
                showError(" ");
            } else {
                showError("Sai quá " + MAX_FAIL + " lần. Khóa " + remaining[0] + " giây...");
            }
        });
        timer.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new LoginFrame().setVisible(true);
        });
    }
}
