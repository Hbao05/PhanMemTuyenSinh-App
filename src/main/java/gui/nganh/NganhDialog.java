package gui.nganh;

import bus.NganhBUS;
import bus.ToHopMonThiBUS;
import entity.Nganh;
import entity.ToHopMonThi;
import gui.component.CustomButton;
import gui.component.CustomComboBox;
import gui.component.CustomTextField;
import gui.style.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Dialog dùng chung cho cả Thêm mới và Sửa ngành.
 * Truyền nganh=null để ở chế độ Thêm, truyền object thực để ở chế độ Sửa.
 */
public class NganhDialog extends JDialog {

    private final NganhBUS nganhBUS;
    private Nganh nganh;          // null = thêm mới, not-null = sửa
    private boolean isSaved = false;

    // --- Các trường thông tin cơ bản ---
    private CustomTextField txtMaNganh, txtTenNganh, txtChiTieu, txtDiemSan;
    private JComboBox<String> cbxToHopGoc;

    // --- Checkbox phương thức xét tuyển ---
    private JCheckBox chkTuyenThang, chkDGNL, chkTHPT, chkVSAT;

    // ======================================================
    public NganhDialog(Window parent, Nganh nganh, NganhBUS bus) {
        super(parent,
                nganh == null ? "Thêm ngành mới" : "Cập nhật thông tin ngành",
                ModalityType.APPLICATION_MODAL);
        this.nganhBUS = bus;
        this.nganh    = nganh;

        setSize(500, 480);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIConstants.BACKGROUND_COLOR);

        buildUI();
        if (nganh != null) fillForm();   // Chế độ Sửa: đổ dữ liệu vào form
    }

    // ──────────────────────────────────────────────────────
    //  XÂY DỰNG GIAO DIỆN
    // ──────────────────────────────────────────────────────
    private void buildUI() {
        // --- Header ---
        JLabel lblTitle = new JLabel(
                nganh == null ? "THÊM NGÀNH MỚI" : "CẬP NHẬT THÔNG TIN NGÀNH",
                SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitle.setForeground(UIConstants.TABLE_HEADER_COLOR);
        lblTitle.setBorder(new EmptyBorder(15, 0, 8, 0));
        add(lblTitle, BorderLayout.NORTH);

        // --- Body ---
        JPanel pnlBody = new JPanel(new BorderLayout(0, 10));
        pnlBody.setOpaque(false);
        pnlBody.setBorder(new EmptyBorder(0, 28, 0, 28));

        // -- Phần 1: Thông tin cơ bản (GridLayout) --
        JPanel pnlForm = new JPanel(new GridLayout(5, 2, 10, 12));
        pnlForm.setOpaque(false);

        pnlForm.add(createLabel("Mã ngành (*)"));
        txtMaNganh = new CustomTextField(20);
        pnlForm.add(txtMaNganh);

        pnlForm.add(createLabel("Tên ngành (*)"));
        txtTenNganh = new CustomTextField(20);
        pnlForm.add(txtTenNganh);

        pnlForm.add(createLabel("Tổ hợp gốc"));
        cbxToHopGoc = new JComboBox<>();
        loadToHopGocData();
        pnlForm.add(cbxToHopGoc);

        pnlForm.add(createLabel("Chỉ tiêu (*)"));
        txtChiTieu = new CustomTextField(20);
        txtChiTieu.setToolTipText("Số nguyên ≥ 0");
        pnlForm.add(txtChiTieu);

        pnlForm.add(createLabel("Điểm sàn"));
        txtDiemSan = new CustomTextField(20);
        txtDiemSan.setToolTipText("Ngưỡng điểm xét tuyển, VD: 15.5");
        pnlForm.add(txtDiemSan);

        pnlBody.add(pnlForm, BorderLayout.NORTH);

        // -- Phần 2: Phương thức xét tuyển (Checkbox group) --
        JPanel pnlPT = new JPanel(new BorderLayout(0, 6));
        pnlPT.setOpaque(false);

        JLabel lblPT = new JLabel("Phương thức xét tuyển:");
        lblPT.setFont(UIConstants.FONT_BOLD);
        lblPT.setForeground(Color.DARK_GRAY);
        pnlPT.add(lblPT, BorderLayout.NORTH);

        JPanel pnlChecks = new JPanel(new GridLayout(2, 2, 15, 6));
        pnlChecks.setOpaque(false);
        pnlChecks.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 210, 210), 1, true),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));

        chkTuyenThang = createCheckBox("Tuyển thẳng");
        chkDGNL       = createCheckBox("Đánh giá năng lực (ĐGNL)");
        chkTHPT       = createCheckBox("Điểm thi THPT");
        chkVSAT       = createCheckBox("V-SAT");

        pnlChecks.add(chkTuyenThang);
        pnlChecks.add(chkDGNL);
        pnlChecks.add(chkTHPT);
        pnlChecks.add(chkVSAT);

        pnlPT.add(pnlChecks, BorderLayout.CENTER);
        pnlBody.add(pnlPT, BorderLayout.CENTER);

        add(pnlBody, BorderLayout.CENTER);

        // --- Footer: nút bấm ---
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        pnlButtons.setOpaque(false);

        CustomButton btnCancel = new CustomButton("Hủy bỏ", UIConstants.DANGER_COLOR);
        CustomButton btnSave   = new CustomButton(
                nganh == null ? "Thêm mới" : "Lưu thay đổi",
                UIConstants.SUCCESS_COLOR);
        btnSave.setPreferredSize(new Dimension(130, 35));

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> save());

        pnlButtons.add(btnCancel);
        pnlButtons.add(btnSave);
        add(pnlButtons, BorderLayout.SOUTH);
    }

    // ──────────────────────────────────────────────────────
    //  ĐỔ DỮ LIỆU VÀO FORM (chế độ Sửa)
    // ──────────────────────────────────────────────────────
    private void fillForm() {
        txtMaNganh.setText(nganh.getMaNganh());
        // Cho phép sửa mã ngành (BUS sẽ check trùng)

        txtTenNganh.setText(nganh.getTenNganh());
        cbxToHopGoc.setSelectedItem(nganh.getToHopGoc());
        txtChiTieu.setText(String.valueOf(nganh.getChiTieu()));
        txtDiemSan.setText(nganh.getDiemSan() != null ? String.valueOf(nganh.getDiemSan()) : "");

        chkTuyenThang.setSelected("Y".equalsIgnoreCase(nganh.getTuyenThang()));
        chkDGNL.setSelected("Y".equalsIgnoreCase(nganh.getDgnl()));
        chkTHPT.setSelected("Y".equalsIgnoreCase(nganh.getThpt()));
        chkVSAT.setSelected("Y".equalsIgnoreCase(nganh.getVsat()));
    }

    // ──────────────────────────────────────────────────────
    //  LƯU DỮ LIỆU
    // ──────────────────────────────────────────────────────
    private void save() {
        // Gom dữ liệu từ form
        Nganh data = (nganh != null) ? nganh : new Nganh();
        data.setMaNganh(txtMaNganh.getText().trim());
        data.setTenNganh(txtTenNganh.getText().trim());
        data.setToHopGoc(cbxToHopGoc.getSelectedItem() != null ? cbxToHopGoc.getSelectedItem().toString() : null);
        data.setChiTieu(NganhBUS.parseIntSafe(txtChiTieu.getText()));
        data.setDiemSan(NganhBUS.parseDoubleSafe(txtDiemSan.getText()));
        data.setTuyenThang(chkTuyenThang.isSelected() ? "Y" : null);
        data.setDgnl(chkDGNL.isSelected() ? "Y" : null);
        data.setThpt(chkTHPT.isSelected() ? "Y" : null);
        data.setVsat(chkVSAT.isSelected() ? "Y" : null);

        String result = (nganh == null)
                ? nganhBUS.addNganh(data)
                : nganhBUS.updateNganh(data);

        if (result.startsWith("Success")) {
            String msg = (nganh == null) ? "Thêm ngành thành công!" : "Cập nhật ngành thành công!";
            JOptionPane.showMessageDialog(this, msg, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            isSaved = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, result, "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ──────────────────────────────────────────────────────
    //  HELPER
    // ──────────────────────────────────────────────────────
    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIConstants.FONT_BOLD);
        lbl.setForeground(Color.DARK_GRAY);
        return lbl;
    }

    private JCheckBox createCheckBox(String text) {
        JCheckBox cb = new JCheckBox(text);
        cb.setFont(UIConstants.FONT_NORMAL);
        cb.setOpaque(false);
        cb.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return cb;
    }

    public boolean isSaved() { return isSaved; }

    private void loadToHopGocData() {
        ToHopMonThiBUS thBus = new ToHopMonThiBUS();
        java.util.List<ToHopMonThi> list = thBus.getAll();
        cbxToHopGoc.addItem(""); // Option cho phép trống
        if (list != null) {
            for (ToHopMonThi t : list) {
                cbxToHopGoc.addItem(t.getMaToHop());
            }
        }
    }
}
