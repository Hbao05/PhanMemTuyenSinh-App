package gui.tohop;

import bus.ToHopMonThiBUS;
import entity.ToHopMonThi;
import gui.component.CustomButton;
import gui.component.CustomTextField;
import gui.style.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ToHopDialog extends JDialog {

    private final ToHopMonThiBUS bus;
    private ToHopMonThi toHop;
    private boolean isSaved = false;

    private CustomTextField txtMaToHop, txtMon1, txtMon2, txtMon3, txtTenToHop;

    public ToHopDialog(Window parent, ToHopMonThi toHop, ToHopMonThiBUS bus) {
        super(parent, toHop == null ? "Thêm tổ hợp mới" : "Cập nhật tổ hợp", ModalityType.APPLICATION_MODAL);
        this.bus = bus;
        this.toHop = toHop;

        setSize(450, 420);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIConstants.BACKGROUND_COLOR);

        buildUI();
        if (toHop != null) fillForm();
    }

    private void buildUI() {
        JLabel lblTitle = new JLabel(
                toHop == null ? "THÊM TỔ HỢP MỚI" : "CẬP NHẬT TỔ HỢP",
                SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitle.setForeground(UIConstants.TABLE_HEADER_COLOR);
        lblTitle.setBorder(new EmptyBorder(15, 0, 8, 0));
        add(lblTitle, BorderLayout.NORTH);

        JPanel pnlBody = new JPanel(new BorderLayout(0, 10));
        pnlBody.setOpaque(false);
        pnlBody.setBorder(new EmptyBorder(10, 28, 10, 28));

        JPanel pnlForm = new JPanel(new GridLayout(5, 2, 10, 12));
        pnlForm.setOpaque(false);

        pnlForm.add(createLabel("Mã tổ hợp (*)"));
        txtMaToHop = new CustomTextField(20);
        pnlForm.add(txtMaToHop);

        pnlForm.add(createLabel("Môn 1 (*)"));
        txtMon1 = new CustomTextField(20);
        pnlForm.add(txtMon1);

        pnlForm.add(createLabel("Môn 2 (*)"));
        txtMon2 = new CustomTextField(20);
        pnlForm.add(txtMon2);

        pnlForm.add(createLabel("Môn 3 (*)"));
        txtMon3 = new CustomTextField(20);
        pnlForm.add(txtMon3);

        pnlForm.add(createLabel("Tên tổ hợp"));
        txtTenToHop = new CustomTextField(20);
        txtTenToHop.setToolTipText("Ví dụ: Toán, Lý, Hóa");
        pnlForm.add(txtTenToHop);

        pnlBody.add(pnlForm, BorderLayout.NORTH);
        add(pnlBody, BorderLayout.CENTER);

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        pnlButtons.setOpaque(false);

        CustomButton btnCancel = new CustomButton("Hủy bỏ", UIConstants.DANGER_COLOR);
        CustomButton btnSave   = new CustomButton(
                toHop == null ? "Thêm mới" : "Lưu thay đổi",
                UIConstants.SUCCESS_COLOR);
        btnSave.setPreferredSize(new Dimension(130, 35));

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> save());

        pnlButtons.add(btnCancel);
        pnlButtons.add(btnSave);
        add(pnlButtons, BorderLayout.SOUTH);
    }

    private void fillForm() {
        txtMaToHop.setText(toHop.getMaToHop());
        txtMaToHop.setEditable(false);
        txtMaToHop.setBackground(new Color(235, 235, 235));

        txtMon1.setText(toHop.getMon1());
        txtMon2.setText(toHop.getMon2());
        txtMon3.setText(toHop.getMon3());
        txtTenToHop.setText(toHop.getTenToHop() != null ? toHop.getTenToHop() : "");
    }

    private void save() {
        ToHopMonThi data = (toHop != null) ? toHop : new ToHopMonThi();
        data.setMaToHop(txtMaToHop.getText().trim());
        data.setMon1(txtMon1.getText().trim());
        data.setMon2(txtMon2.getText().trim());
        data.setMon3(txtMon3.getText().trim());
        data.setTenToHop(txtTenToHop.getText().trim().isEmpty() ? null : txtTenToHop.getText().trim());

        String result = (toHop == null)
                ? bus.addToHop(data)
                : bus.updateToHop(data);

        if (result.startsWith("Success")) {
            JOptionPane.showMessageDialog(this, (toHop == null) ? "Thêm thành công!" : "Cập nhật thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            isSaved = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, result, "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIConstants.FONT_BOLD);
        lbl.setForeground(Color.DARK_GRAY);
        return lbl;
    }

    public boolean isSaved() { return isSaved; }
}
