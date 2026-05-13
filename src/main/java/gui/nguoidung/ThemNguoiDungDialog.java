package gui.nguoidung;

import bus.NguoiDungBUS;
import entity.NguoiDung;
import gui.component.CustomButton;
import gui.component.CustomTextField;
import gui.style.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ThemNguoiDungDialog extends JDialog {
    private final NguoiDungBUS bus;
    private boolean saved = false;

    private CustomTextField txtUsername, txtHoTen, txtEmail, txtSdt;
    private JPasswordField txtPassword, txtConfirmPass;
    private JComboBox<String> cboQuyen;

    public ThemNguoiDungDialog(Window parent, NguoiDungBUS bus) {
        super(parent, "Thêm người dùng mới", ModalityType.APPLICATION_MODAL);
        this.bus = bus;
        setSize(420, 480);
        setLocationRelativeTo(parent);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIConstants.BACKGROUND_COLOR);
        setContentPane(root);

        JLabel lblTitle = new JLabel("THÊM NGƯỜI DÙNG MỚI", SwingConstants.CENTER);
        lblTitle.setFont(UIConstants.FONT_HEADER);
        lblTitle.setForeground(UIConstants.TABLE_HEADER_COLOR);
        lblTitle.setBorder(new EmptyBorder(16, 0, 10, 0));
        root.add(lblTitle, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(0, 24, 0, 24));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 4, 5, 4);
        gc.anchor = GridBagConstraints.WEST;

        txtUsername    = new CustomTextField(20);
        txtPassword    = new JPasswordField(20);
        txtConfirmPass = new JPasswordField(20);
        txtHoTen       = new CustomTextField(20);
        txtEmail       = new CustomTextField(20);
        txtSdt         = new CustomTextField(20);
        cboQuyen       = new JComboBox<>(new String[]{"USER", "ADMIN"});
        cboQuyen.setFont(UIConstants.FONT_NORMAL);

        stylePassField(txtPassword);
        stylePassField(txtConfirmPass);

        Object[][] rows = {
            {"Username *",    txtUsername},
            {"Mật khẩu *",   txtPassword},
            {"Xác nhận MK *", txtConfirmPass},
            {"Họ tên",        txtHoTen},
            {"Email",         txtEmail},
            {"Điện thoại",    txtSdt},
            {"Quyền",         cboQuyen},
        };

        for (int i = 0; i < rows.length; i++) {
            gc.gridx = 0; gc.gridy = i; gc.fill = GridBagConstraints.NONE;
            JLabel lbl = new JLabel((String) rows[i][0]);
            lbl.setFont(UIConstants.FONT_BOLD);
            form.add(lbl, gc);
            gc.gridx = 1; gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1;
            form.add((Component) rows[i][1], gc);
            gc.weightx = 0;
        }

        root.add(form, BorderLayout.CENTER);

        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        pnlBtn.setOpaque(false);
        CustomButton btnSave   = new CustomButton("Luu",  UIConstants.SUCCESS_COLOR);
        CustomButton btnCancel = new CustomButton("Hủy",     UIConstants.GRAY_COLOR);
        btnSave.setPreferredSize(new Dimension(110, 36));
        btnCancel.setPreferredSize(new Dimension(90, 36));
        btnSave.addActionListener(e -> doSave());
        btnCancel.addActionListener(e -> dispose());
        pnlBtn.add(btnSave);
        pnlBtn.add(btnCancel);
        root.add(pnlBtn, BorderLayout.SOUTH);
    }

    private void stylePassField(JPasswordField f) {
        f.setFont(UIConstants.FONT_NORMAL);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
    }

    private void doSave() {
        String password = new String(txtPassword.getPassword());
        String confirm  = new String(txtConfirmPass.getPassword());

        if (!password.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu xác nhận không khớp!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        NguoiDung nd = new NguoiDung();
        nd.setUsername(txtUsername.getText().trim());
        nd.setHoTen(txtHoTen.getText().trim());
        nd.setEmail(txtEmail.getText().trim());
        nd.setDienThoai(txtSdt.getText().trim());
        nd.setQuyen("ADMIN".equals(cboQuyen.getSelectedItem()) ? NguoiDung.Quyen.ADMIN : NguoiDung.Quyen.USER);
        nd.setKichHoat(true);

        String result = bus.addUser(nd, password);
        if (result.startsWith("Success")) {
            JOptionPane.showMessageDialog(this, "Thêm người dùng thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            saved = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, result.replace("Error: ", ""), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() { return saved; }
}
