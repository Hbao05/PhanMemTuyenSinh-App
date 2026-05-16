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

public class SuaThiSinhDialog extends JDialog {
    private ThiSinh candidate;
    private ThiSinhBUS candidateBUS;
    private boolean isUpdated = false;

    // Các Component nhập liệu
    private CustomTextField txtCccd, txtSbd, txtHo, txtTen, txtNgaySinh, txtDienThoai, txtEmail;
    private CustomTextField txtKhuVuc, txtDoiTuong;
    private CustomComboBox<String> cbxGioiTinh, cbxNoiSinh;

    public SuaThiSinhDialog(Window parent, ThiSinh candidate, ThiSinhBUS candidateBUS) {
        super(parent, "Cập nhật thông tin thí sinh", ModalityType.APPLICATION_MODAL);
        this.candidate = candidate;
        this.candidateBUS = candidateBUS;

        setSize(480, 600);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIConstants.BACKGROUND_COLOR);

        initComponents();
        loadDataToForm();
    }

    private void initComponents() {
        // --- PHẦN HEADER ---
        JLabel lblTitle = new JLabel("SỬA THÔNG TIN THÍ SINH", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(UIConstants.TABLE_HEADER_COLOR);
        lblTitle.setBorder(new EmptyBorder(15, 0, 10, 0));
        add(lblTitle, BorderLayout.NORTH);

        // --- PHẦN FORM NHẬP LIỆU ---
        // Dùng GridLayout chia làm 11 hàng, 2 cột (Nhãn - Ô nhập)
        JPanel pnlForm = new JPanel(new GridLayout(11, 2, 10, 15));
        pnlForm.setOpaque(false);
        pnlForm.setBorder(new EmptyBorder(10, 30, 20, 30));

        pnlForm.add(createLabel("Số CCCD (*)"));
        txtCccd = new CustomTextField(20);
        pnlForm.add(txtCccd);

        pnlForm.add(createLabel("Số báo danh"));
        txtSbd = new CustomTextField(20);
        pnlForm.add(txtSbd);

        pnlForm.add(createLabel("Họ"));
        txtHo = new CustomTextField(20);
        pnlForm.add(txtHo);

        pnlForm.add(createLabel("Tên (*)"));
        txtTen = new CustomTextField(20);
        pnlForm.add(txtTen);

        pnlForm.add(createLabel("Ngày sinh"));
        txtNgaySinh = new CustomTextField(20);
        pnlForm.add(txtNgaySinh);

        pnlForm.add(createLabel("Giới tính"));
        cbxGioiTinh = new CustomComboBox<>(new String[]{"Nam", "Nữ", "Khác"});
        pnlForm.add(cbxGioiTinh);

        pnlForm.add(createLabel("Nơi sinh"));
        cbxNoiSinh = new CustomComboBox<>(util.ProvinceUtil.PROVINCES);
        pnlForm.add(cbxNoiSinh);

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

        // --- PHẦN NÚT BẤM ---
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        pnlButtons.setOpaque(false);

        CustomButton btnCancel = new CustomButton("Hủy bỏ", UIConstants.DANGER_COLOR);
        CustomButton btnSave = new CustomButton("Lưu thay đổi", UIConstants.SUCCESS_COLOR);

        // Xử lý sự kiện nút Hủy
        btnCancel.addActionListener(e -> dispose());

        // Xử lý sự kiện nút Lưu
        btnSave.addActionListener(e -> saveCandidate());

        pnlButtons.add(btnCancel);
        pnlButtons.add(btnSave);
        add(pnlButtons, BorderLayout.SOUTH);
    }

    // Hàm tiện ích tạo Nhãn (Label) cho Form
    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIConstants.FONT_BOLD);
        lbl.setForeground(Color.DARK_GRAY);
        return lbl;
    }

    // Đổ dữ liệu từ Object Thí Sinh vào các ô Text
    private void loadDataToForm() {
        if (candidate != null) {
            txtCccd.setText(candidate.getCccd());
            txtSbd.setText(candidate.getSoBaoDanh());
            txtHo.setText(candidate.getHo());
            txtTen.setText(candidate.getTen());
            txtNgaySinh.setText(candidate.getNgaySinh());
            cbxGioiTinh.setSelectedItem(candidate.getGioiTinh());
            cbxNoiSinh.setSelectedItem(candidate.getNoiSinh());
            txtDienThoai.setText(candidate.getDienThoai());
            txtEmail.setText(candidate.getEmail());
            txtDoiTuong.setText(candidate.getDoiTuong() != null ? candidate.getDoiTuong() : "");
            txtKhuVuc.setText(candidate.getKhuVuc() != null ? candidate.getKhuVuc() : "");
        }
    }

    // Gom dữ liệu từ Form, đẩy vào Object và gọi BUS lưu
    private void saveCandidate() {
        // Cập nhật lại Object hiện tại
        candidate.setCccd(txtCccd.getText().trim());
        candidate.setSoBaoDanh(txtSbd.getText().trim());
        candidate.setHo(txtHo.getText().trim());
        candidate.setTen(txtTen.getText().trim());
        candidate.setNgaySinh(txtNgaySinh.getText().trim());
        candidate.setGioiTinh(cbxGioiTinh.getSelectedItem().toString());
        candidate.setNoiSinh(cbxNoiSinh.getSelectedItem().toString());
        candidate.setDienThoai(txtDienThoai.getText().trim());
        candidate.setEmail(txtEmail.getText().trim());
        candidate.setDoiTuong(txtDoiTuong.getText().trim());
        candidate.setKhuVuc(txtKhuVuc.getText().trim());

        // Cập nhật ngày sửa cuối
        candidate.setUpdatedAt(LocalDate.now());

        // Gọi tầng BUS xử lý (BUS sẽ kiểm tra CCCD, độ dài, v.v.)
        String result = candidateBUS.updateCandidateInfo(candidate);

        if (result.startsWith("Success")) {
            JOptionPane.showMessageDialog(this, "Đã cập nhật thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            isUpdated = true; // Bật cờ hiệu để màn hình chính biết cần tải lại bảng
            dispose(); // Đóng Form
        } else {
            JOptionPane.showMessageDialog(this, result, "Lỗi cập nhật", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Cho phép màn hình chính kiểm tra xem Admin có thực sự sửa không hay chỉ mở lên rồi bấm Hủy
    public boolean isUpdated() {
        return isUpdated;
    }
}
