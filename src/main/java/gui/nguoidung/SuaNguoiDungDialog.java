package gui.nguoidung;

import bus.NguoiDungBUS;
import entity.NguoiDung;
import gui.component.CustomButton;
import gui.component.CustomTextField;
import gui.style.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SuaNguoiDungDialog extends JDialog {
    private final NguoiDungBUS bus;
    private final NguoiDung nd;
    private boolean updated = false;

    private CustomTextField txtHoTen, txtEmail, txtSdt;

    public SuaNguoiDungDialog(Window parent, NguoiDung nd, NguoiDungBUS bus) {
        super(parent, "Sửa thông tin người dùng", ModalityType.APPLICATION_MODAL);
        this.bus = bus;
        this.nd  = nd;
        setSize(400, 340);
        setLocationRelativeTo(parent);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIConstants.BACKGROUND_COLOR);
        setContentPane(root);

        JLabel lblTitle = new JLabel("SỬA THÔNG TIN NGƯỜI DÙNG", SwingConstants.CENTER);
        lblTitle.setFont(UIConstants.FONT_HEADER);
        lblTitle.setForeground(UIConstants.TABLE_HEADER_COLOR);
        lblTitle.setBorder(new EmptyBorder(16, 0, 10, 0));
        root.add(lblTitle, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(0, 24, 0, 24));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 4, 6, 4);
        gc.anchor = GridBagConstraints.WEST;

        // Username (read-only)
        JLabel lblUn = new JLabel("Username");
        lblUn.setFont(UIConstants.FONT_BOLD);
        JLabel valUn = new JLabel(nd.getUsername());
        valUn.setFont(UIConstants.FONT_NORMAL);
        valUn.setForeground(UIConstants.GRAY_COLOR);

        txtHoTen = new CustomTextField(20);
        txtHoTen.setText(nd.getHoTen() != null ? nd.getHoTen() : "");
        txtEmail  = new CustomTextField(20);
        txtEmail.setText(nd.getEmail() != null ? nd.getEmail() : "");
        txtSdt    = new CustomTextField(20);
        txtSdt.setText(nd.getDienThoai() != null ? nd.getDienThoai() : "");

        Object[][] rows = {
            {"Username", valUn},
            {"Họ tên *", txtHoTen},
            {"Email",    txtEmail},
            {"SĐT",      txtSdt},
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
        CustomButton btnSave   = new CustomButton("Lưu",  UIConstants.SUCCESS_COLOR);
        CustomButton btnCancel = new CustomButton("Hủy",     UIConstants.GRAY_COLOR);
        btnSave.setPreferredSize(new Dimension(110, 36));
        btnCancel.setPreferredSize(new Dimension(90, 36));
        btnSave.addActionListener(e -> doSave());
        btnCancel.addActionListener(e -> dispose());
        pnlBtn.add(btnSave);
        pnlBtn.add(btnCancel);
        root.add(pnlBtn, BorderLayout.SOUTH);
    }

    private void doSave() {
        nd.setHoTen(txtHoTen.getText().trim());
        nd.setEmail(txtEmail.getText().trim());
        nd.setDienThoai(txtSdt.getText().trim());

        String result = bus.updateUser(nd);
        if (result.startsWith("Success")) {
            JOptionPane.showMessageDialog(this, "Cập nhật thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            updated = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, result.replace("Error: ", ""), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isUpdated() { return updated; }
}
