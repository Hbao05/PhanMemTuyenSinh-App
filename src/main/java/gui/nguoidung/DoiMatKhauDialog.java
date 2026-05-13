package gui.nguoidung;

import bus.NguoiDungBUS;
import entity.NguoiDung;
import gui.component.CustomButton;
import gui.style.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DoiMatKhauDialog extends JDialog {
    private final NguoiDungBUS bus;
    private final NguoiDung nd;

    private JPasswordField txtOld, txtNew, txtConfirm;

    public DoiMatKhauDialog(Window parent, NguoiDung nd, NguoiDungBUS bus) {
        super(parent, "Đổi mật khẩu — " + nd.getUsername(), ModalityType.APPLICATION_MODAL);
        this.bus = bus;
        this.nd  = nd;
        setSize(380, 300);
        setLocationRelativeTo(parent);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIConstants.BACKGROUND_COLOR);
        setContentPane(root);

        JLabel lblTitle = new JLabel("ĐỔI MẬT KHẨU", SwingConstants.CENTER);
        lblTitle.setFont(UIConstants.FONT_HEADER);
        lblTitle.setForeground(UIConstants.TABLE_HEADER_COLOR);
        lblTitle.setBorder(new EmptyBorder(16, 0, 10, 0));
        root.add(lblTitle, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(0, 24, 0, 24));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(7, 4, 7, 4);
        gc.anchor = GridBagConstraints.WEST;

        txtOld     = makePassField();
        txtNew     = makePassField();
        txtConfirm = makePassField();

        String[][] rows = {{"Mật khẩu cũ *", null}, {"Mật khẩu mới *", null}, {"Xác nhận MK *", null}};
        JPasswordField[] fields = {txtOld, txtNew, txtConfirm};

        for (int i = 0; i < rows.length; i++) {
            gc.gridx = 0; gc.gridy = i; gc.fill = GridBagConstraints.NONE;
            JLabel lbl = new JLabel(rows[i][0]);
            lbl.setFont(UIConstants.FONT_BOLD);
            form.add(lbl, gc);
            gc.gridx = 1; gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1;
            form.add(fields[i], gc);
            gc.weightx = 0;
        }

        root.add(form, BorderLayout.CENTER);

        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        pnlBtn.setOpaque(false);
        CustomButton btnSave   = new CustomButton("Doi MK", UIConstants.WARNING_COLOR);
        CustomButton btnCancel = new CustomButton("Hủy",        UIConstants.GRAY_COLOR);
        btnSave.setPreferredSize(new Dimension(120, 36));
        btnCancel.setPreferredSize(new Dimension(90, 36));
        btnSave.addActionListener(e -> doChange());
        btnCancel.addActionListener(e -> dispose());
        pnlBtn.add(btnSave);
        pnlBtn.add(btnCancel);
        root.add(pnlBtn, BorderLayout.SOUTH);
    }

    private JPasswordField makePassField() {
        JPasswordField f = new JPasswordField(20);
        f.setFont(UIConstants.FONT_NORMAL);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        return f;
    }

    private void doChange() {
        String oldPwd = new String(txtOld.getPassword());
        String newPwd = new String(txtNew.getPassword());
        String confirm = new String(txtConfirm.getPassword());

        if (!newPwd.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu mới và xác nhận không khớp!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String result = bus.changePassword(nd.getId(), oldPwd, newPwd);
        if (result.startsWith("Success")) {
            JOptionPane.showMessageDialog(this, "Đổi mật khẩu thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, result.replace("Error: ", ""), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
