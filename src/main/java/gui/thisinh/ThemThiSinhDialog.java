package gui.thisinh;

import bus.ThiSinhBUS;
import entity.ThiSinh;
import gui.component.CustomButton;
import gui.component.CustomComboBox;
import gui.component.CustomTextField;
import gui.style.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;

/**
 * Dialog Thêm mới Thí sinh.
 * Theo đúng định dạng và style của SuaThiSinhDialog, nhưng
 * tất cả các ô đều để trắng và sẽ gọi BUS insert thay vì update.
 */
public class ThemThiSinhDialog extends JDialog {
    private ThiSinhBUS candidateBUS;
    private boolean isSaved = false;

    // Các Component nhập liệu
    private CustomTextField txtCccd, txtHo, txtTen, txtNgaySinh;
    private CustomTextField txtDienThoai, txtEmail, txtNoiSinh;
    private CustomTextField txtDoiTuong, txtKhuVuc;
    private CustomComboBox<String> cbxGioiTinh;

    public ThemThiSinhDialog(Window parent, ThiSinhBUS candidateBUS) {
        super(parent, "Thêm thí sinh mới", ModalityType.APPLICATION_MODAL);
        this.candidateBUS = candidateBUS;

        setSize(480, 600);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIConstants.BACKGROUND_COLOR);

        initComponents();
    }

    private void initComponents() {
        // --- HEADER ---
        JLabel lblTitle = new JLabel("THÊM THÍ SINH MỚI", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(UIConstants.TABLE_HEADER_COLOR);
        lblTitle.setBorder(new EmptyBorder(15, 0, 10, 0));
        add(lblTitle, BorderLayout.NORTH);

        // --- FORM NHẬP LIỆU (10 hàng x 2 cột) ---
        JPanel pnlForm = new JPanel(new GridLayout(10, 2, 10, 12));
        pnlForm.setOpaque(false);
        pnlForm.setBorder(new EmptyBorder(10, 30, 10, 30));

        pnlForm.add(createLabel("Số CCCD (*)"));
        txtCccd = new CustomTextField(20);
        pnlForm.add(txtCccd);

        pnlForm.add(createLabel("Họ"));
        txtHo = new CustomTextField(20);
        pnlForm.add(txtHo);

        pnlForm.add(createLabel("Tên (*)"));
        txtTen = new CustomTextField(20);
        pnlForm.add(txtTen);

        pnlForm.add(createLabel("Ngày sinh (dd/MM/yyyy)"));
        txtNgaySinh = new CustomTextField(20);
        pnlForm.add(txtNgaySinh);

        pnlForm.add(createLabel("Giới tính"));
        cbxGioiTinh = new CustomComboBox<>(new String[]{"Nam", "Nữ", "Khác"});
        pnlForm.add(cbxGioiTinh);

        pnlForm.add(createLabel("Nơi sinh"));
        txtNoiSinh = new CustomTextField(20);
        pnlForm.add(txtNoiSinh);

        pnlForm.add(createLabel("Điện thoại"));
        txtDienThoai = new CustomTextField(20);
        pnlForm.add(txtDienThoai);

        pnlForm.add(createLabel("Email"));
        txtEmail = new CustomTextField(20);
        pnlForm.add(txtEmail);

        pnlForm.add(createLabel("Đối tượng ưu tiên"));
        txtDoiTuong = new CustomTextField(20);
        pnlForm.add(txtDoiTuong);

        pnlForm.add(createLabel("Khu vực ưu tiên"));
        txtKhuVuc = new CustomTextField(20);
        pnlForm.add(txtKhuVuc);

        add(pnlForm, BorderLayout.CENTER);

        // --- NÚT BẤM ---
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        pnlButtons.setOpaque(false);

        CustomButton btnCancel = new CustomButton("Hủy bỏ", UIConstants.DANGER_COLOR);
        CustomButton btnSave   = new CustomButton("Thêm mới", UIConstants.SUCCESS_COLOR);

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> saveNewCandidate());

        pnlButtons.add(btnCancel);
        pnlButtons.add(btnSave);
        add(pnlButtons, BorderLayout.SOUTH);
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIConstants.FONT_BOLD);
        lbl.setForeground(Color.DARK_GRAY);
        return lbl;
    }

    private void saveNewCandidate() {
        // Gom dữ liệu từ form
        ThiSinh ts = new ThiSinh();
        ts.setCccd(txtCccd.getText().trim());
        ts.setHo(txtHo.getText().trim());
        ts.setTen(txtTen.getText().trim());
        ts.setNgaySinh(txtNgaySinh.getText().trim());
        ts.setGioiTinh(cbxGioiTinh.getSelectedItem() != null
                ? cbxGioiTinh.getSelectedItem().toString() : "Nam");
        ts.setNoiSinh(txtNoiSinh.getText().trim());
        ts.setDienThoai(txtDienThoai.getText().trim());
        ts.setEmail(txtEmail.getText().trim());
        ts.setDoiTuong(txtDoiTuong.getText().trim());
        ts.setKhuVuc(txtKhuVuc.getText().trim());
        ts.setUpdatedAt(LocalDate.now());

        // Gọi BUS xử lý nghiệp vụ
        String result = candidateBUS.addCandidate(ts);

        if (result.startsWith("Success")) {
            JOptionPane.showMessageDialog(this,
                    "Thêm thí sinh thành công!", "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);
            isSaved = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    result, "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Cho phép màn hình chính biết có cần reload bảng không. */
    public boolean isSaved() {
        return isSaved;
    }
}
