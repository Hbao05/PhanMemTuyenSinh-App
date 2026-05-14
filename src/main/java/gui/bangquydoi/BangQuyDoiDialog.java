package gui.bangquydoi;

import bus.BangQuyDoiBUS;
import entity.BangQuyDoi;
import gui.component.CustomButton;
import gui.component.CustomTextField;
import gui.style.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Dialog thêm / sửa một bản ghi Bảng Quy Đổi.
 */
public class BangQuyDoiDialog extends JDialog {

    private final BangQuyDoiBUS bus;
    private final BangQuyDoi    target;   // null = thêm mới
    private boolean saved = false;

    private CustomTextField txtMaQuyDoi, txtPhuongThuc, txtToHop, txtMon, txtPhanVi;
    private JFormattedTextField txtDiemA, txtDiemB, txtDiemC, txtDiemD;

    public BangQuyDoiDialog(Window parent, BangQuyDoi target, BangQuyDoiBUS bus) {
        super(parent,
              target == null ? "Thêm mới bảng quy đổi" : "Sửa bảng quy đổi",
              ModalityType.APPLICATION_MODAL);
        this.bus    = bus;
        this.target = target;
        setSize(460, 500);
        setLocationRelativeTo(parent);
        setResizable(false);
        initUI();
        if (target != null) fillForm();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIConstants.BACKGROUND_COLOR);
        setContentPane(root);

        // Header
        JLabel lblTitle = new JLabel(
                target == null ? "THÊM MỚI BẢNG QUY ĐỔI" : "SỬA BẢNG QUY ĐỔI",
                SwingConstants.CENTER);
        lblTitle.setFont(UIConstants.FONT_HEADER);
        lblTitle.setForeground(UIConstants.TABLE_HEADER_COLOR);
        lblTitle.setBorder(new EmptyBorder(16, 0, 10, 0));
        root.add(lblTitle, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(4, 24, 4, 24));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets  = new Insets(5, 4, 5, 4);
        gc.anchor  = GridBagConstraints.WEST;

        txtMaQuyDoi  = new CustomTextField(20);
        txtPhuongThuc= new CustomTextField(20);
        txtToHop     = new CustomTextField(20);
        txtMon       = new CustomTextField(20);
        txtPhanVi    = new CustomTextField(20);
        txtDiemA     = makeDoubleField();
        txtDiemB     = makeDoubleField();
        txtDiemC     = makeDoubleField();
        txtDiemD     = makeDoubleField();

        Object[][] rows = {
            {"Mã quy đổi *",   txtMaQuyDoi},
            {"Phương thức *",  txtPhuongThuc},
            {"Tổ hợp",         txtToHop},
            {"Môn",            txtMon},
            {"Phạm vi",        txtPhanVi},
            {"Điểm A",         txtDiemA},
            {"Điểm B",         txtDiemB},
            {"Điểm C",         txtDiemC},
            {"Điểm D",         txtDiemD},
        };

        for (int i = 0; i < rows.length; i++) {
            gc.gridx = 0; gc.gridy = i; gc.fill = GridBagConstraints.NONE; gc.weightx = 0;
            JLabel lbl = new JLabel((String) rows[i][0]);
            lbl.setFont(UIConstants.FONT_BOLD);
            lbl.setPreferredSize(new Dimension(120, 28));
            form.add(lbl, gc);
            gc.gridx = 1; gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1;
            form.add((Component) rows[i][1], gc);
        }

        root.add(form, BorderLayout.CENTER);

        // Buttons
        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 12));
        pnlBtn.setOpaque(false);
        CustomButton btnSave   = new CustomButton("Lưu",  UIConstants.SUCCESS_COLOR);
        CustomButton btnCancel = new CustomButton("Hủy",  UIConstants.GRAY_COLOR);
        btnSave.setPreferredSize(new Dimension(110, 36));
        btnCancel.setPreferredSize(new Dimension(90, 36));
        btnSave.addActionListener(e -> doSave());
        btnCancel.addActionListener(e -> dispose());
        pnlBtn.add(btnSave);
        pnlBtn.add(btnCancel);
        root.add(pnlBtn, BorderLayout.SOUTH);
    }

    private JFormattedTextField makeDoubleField() {
        JFormattedTextField f = new JFormattedTextField();
        f.setColumns(10);
        f.setFont(UIConstants.FONT_NORMAL);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        return f;
    }

    private void fillForm() {
        txtMaQuyDoi.setText(target.getMaQuyDoi()   != null ? target.getMaQuyDoi()   : "");
        txtPhuongThuc.setText(target.getPhuongThuc()!= null ? target.getPhuongThuc(): "");
        txtToHop.setText(target.getToHop()          != null ? target.getToHop()      : "");
        txtMon.setText(target.getMon()               != null ? target.getMon()        : "");
        txtPhanVi.setText(target.getPhanVi()         != null ? target.getPhanVi()     : "");
        if (target.getDiemA() != null) txtDiemA.setValue(target.getDiemA());
        if (target.getDiemB() != null) txtDiemB.setValue(target.getDiemB());
        if (target.getDiemC() != null) txtDiemC.setValue(target.getDiemC());
        if (target.getDiemD() != null) txtDiemD.setValue(target.getDiemD());
    }

    private void doSave() {
        BangQuyDoi bqd = (target != null) ? target : new BangQuyDoi();
        bqd.setMaQuyDoi(txtMaQuyDoi.getText().trim());
        bqd.setPhuongThuc(txtPhuongThuc.getText().trim());
        bqd.setToHop(txtToHop.getText().trim());
        bqd.setMon(txtMon.getText().trim());
        bqd.setPhanVi(txtPhanVi.getText().trim());
        bqd.setDiemA(parseDouble(txtDiemA));
        bqd.setDiemB(parseDouble(txtDiemB));
        bqd.setDiemC(parseDouble(txtDiemC));
        bqd.setDiemD(parseDouble(txtDiemD));

        String result = (target == null)
                ? bus.addBangQuyDoi(bqd)
                : bus.updateBangQuyDoi(bqd);

        if (result.startsWith("Success")) {
            JOptionPane.showMessageDialog(this,
                    target == null ? "Thêm mới thành công!" : "Cập nhật thành công!",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            saved = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, result.replace("Error: ", ""),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Double parseDouble(JFormattedTextField f) {
        String text = f.getText().trim();
        if (text.isEmpty()) return null;
        try { return Double.parseDouble(text.replace(",", ".")); }
        catch (NumberFormatException e) { return null; }
    }

    public boolean isSaved() { return saved; }
}
