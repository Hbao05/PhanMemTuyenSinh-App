package gui.nguyenvong;

import bus.NganhBUS;
import bus.NganhToHopBUS;
import bus.NguyenVongBUS;
import bus.ThiSinhBUS;
import entity.Nganh;
import entity.NganhToHop;
import entity.NguyenVongXetTuyen;
import entity.ThiSinh;
import gui.component.CustomButton;
import gui.component.CustomTextField;
import gui.style.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class NguyenVongDialog extends JDialog {

    private final NguyenVongBUS  bus;
    private final NguyenVongXetTuyen target;
    private boolean saved = false;

    private CustomTextField   txtCccd;
    private JLabel            lblHoTen;
    private JComboBox<String> cboNganh, cboToHop, cboPhuongThuc;
    private JSpinner          spThuTu;

    private final ThiSinhBUS    thiSinhBUS    = new ThiSinhBUS();
    private final NganhBUS      nganhBUS      = new NganhBUS();
    private final NganhToHopBUS nganhToHopBUS = new NganhToHopBUS();

    public NguyenVongDialog(Window parent, NguyenVongXetTuyen target, NguyenVongBUS bus) {
        super(parent,
              target == null ? "Thêm mới nguyện vọng" : "Sửa nguyện vọng",
              ModalityType.APPLICATION_MODAL);
        this.bus    = bus;
        this.target = target;
        setSize(460, 440);
        setLocationRelativeTo(parent);
        setResizable(false);
        initUI();
        if (target != null) fillForm();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIConstants.BACKGROUND_COLOR);
        setContentPane(root);

        JLabel lblTitle = new JLabel(
                target == null ? "THÊM MỚI NGUYỆN VỌNG" : "SỬA NGUYỆN VỌNG",
                SwingConstants.CENTER);
        lblTitle.setFont(UIConstants.FONT_HEADER);
        lblTitle.setForeground(UIConstants.TABLE_HEADER_COLOR);
        lblTitle.setBorder(new EmptyBorder(16, 0, 10, 0));
        root.add(lblTitle, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(4, 24, 4, 24));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets  = new Insets(5, 4, 5, 4);
        gc.anchor  = GridBagConstraints.WEST;

        // CCCD + nút tra cứu
        txtCccd = new CustomTextField(16);
        CustomButton btnTraCuu = new CustomButton("Tra cứu", UIConstants.PRIMARY_COLOR);
        btnTraCuu.setPreferredSize(new Dimension(90, 30));
        btnTraCuu.addActionListener(e -> traCuuThiSinh());
        JPanel pnlCccd = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        pnlCccd.setOpaque(false);
        pnlCccd.add(txtCccd); pnlCccd.add(btnTraCuu);

        lblHoTen = new JLabel(" ");
        lblHoTen.setFont(UIConstants.FONT_SMALL);
        lblHoTen.setForeground(UIConstants.SUCCESS_COLOR);

        spThuTu = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
        spThuTu.setFont(UIConstants.FONT_NORMAL);

        cboNganh = new JComboBox<>();
        cboNganh.setFont(UIConstants.FONT_NORMAL);
        loadNganh();
        cboNganh.addActionListener(e -> onNganhChanged());

        cboToHop = new JComboBox<>();
        cboToHop.setFont(UIConstants.FONT_NORMAL);

        cboPhuongThuc = new JComboBox<>(new String[]{"THPT", "VSAT", "DGNL"});
        cboPhuongThuc.setFont(UIConstants.FONT_NORMAL);

        Object[][] rows = {
            {"CCCD *",         pnlCccd},
            {"Họ tên TS",      lblHoTen},
            {"Thứ tự NV *",    spThuTu},
            {"Ngành *",        cboNganh},
            {"Tổ hợp môn",     cboToHop},
            {"Phương thức *",  cboPhuongThuc},
        };

        for (int i = 0; i < rows.length; i++) {
            gc.gridx = 0; gc.gridy = i; gc.fill = GridBagConstraints.NONE; gc.weightx = 0;
            JLabel lbl = new JLabel((String) rows[i][0]);
            lbl.setFont(UIConstants.FONT_BOLD);
            lbl.setPreferredSize(new Dimension(130, 28));
            form.add(lbl, gc);
            gc.gridx = 1; gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1;
            form.add((Component) rows[i][1], gc);
        }

        root.add(form, BorderLayout.CENTER);

        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 12));
        pnlBtn.setOpaque(false);
        CustomButton btnSave   = new CustomButton("Lưu",  UIConstants.SUCCESS_COLOR);
        CustomButton btnCancel = new CustomButton("Hủy",  UIConstants.GRAY_COLOR);
        btnSave.setPreferredSize(new Dimension(110, 36));
        btnCancel.setPreferredSize(new Dimension(90, 36));
        btnSave.addActionListener(e -> doSave());
        btnCancel.addActionListener(e -> dispose());
        pnlBtn.add(btnSave); pnlBtn.add(btnCancel);
        root.add(pnlBtn, BorderLayout.SOUTH);
    }

    private void loadNganh() {
        List<Nganh> list = nganhBUS.getAll();
        if (list != null)
            list.forEach(n -> cboNganh.addItem(n.getMaNganh() + " - " + n.getTenNganh()));
        if (cboNganh.getItemCount() > 0) onNganhChanged();
    }

    private void onNganhChanged() {
        if(cboToHop != null) {
            cboToHop.removeAllItems();
        } else {
            cboToHop = new JComboBox<>();
            cboToHop.setFont(UIConstants.FONT_NORMAL);
        }
        String sel = (String) cboNganh.getSelectedItem();
        if (sel == null) return;
        String maNganh = sel.split(" - ")[0].trim();
        List<NganhToHop> list = nganhToHopBUS.getByMaNganh(maNganh);
        if (list != null)
            list.forEach(t -> cboToHop.addItem(t.getMaToHop()));
    }

    private void traCuuThiSinh() {
        String cccd = txtCccd.getText().trim();
        if (cccd.isEmpty()) { lblHoTen.setText("Nhập CCCD trước!"); return; }
        ThiSinh ts = thiSinhBUS.getByCccd(cccd);
        if (ts != null) {
            lblHoTen.setText(ts.getHo() + " " + ts.getTen());
            lblHoTen.setForeground(UIConstants.SUCCESS_COLOR);
        } else {
            lblHoTen.setText("Không tìm thấy thí sinh!");
            lblHoTen.setForeground(UIConstants.DANGER_COLOR);
        }
    }

    private void fillForm() {
        txtCccd.setText(target.getCccd() != null ? target.getCccd() : "");
        traCuuThiSinh();

        spThuTu.setValue(target.getThuTuNguyenVong() > 0 ? target.getThuTuNguyenVong() : 1);

        selectComboByPrefix(cboNganh, target.getMaNganh());
        onNganhChanged(); // reload tổ hợp theo ngành đã chọn

        if (target.getToHopMon() != null) {
            for (int i = 0; i < cboToHop.getItemCount(); i++) {
                if (target.getToHopMon().equals(cboToHop.getItemAt(i))) {
                    cboToHop.setSelectedIndex(i); break;
                }
            }
        }

        if (target.getPhuongThuc() != null) cboPhuongThuc.setSelectedItem(target.getPhuongThuc());
    }

    private void selectComboByPrefix(JComboBox<String> cbo, String prefix) {
        if (prefix == null || prefix.isBlank()) return;
        for (int i = 0; i < cbo.getItemCount(); i++) {
            String item = cbo.getItemAt(i);
            if (item != null && item.startsWith(prefix)) { cbo.setSelectedIndex(i); return; }
        }
    }

    private void doSave() {
        NguyenVongXetTuyen nv = target != null ? target : new NguyenVongXetTuyen();

        nv.setCccd(txtCccd.getText().trim());
        nv.setThuTuNguyenVong(((Number) spThuTu.getValue()).intValue());

        String nganhSel = (String) cboNganh.getSelectedItem();
        nv.setMaNganh(nganhSel != null ? nganhSel.split(" - ")[0].trim() : "");

        nv.setToHopMon((String) cboToHop.getSelectedItem());
        nv.setPhuongThuc((String) cboPhuongThuc.getSelectedItem());

        String result = target == null ? bus.addNguyenVong(nv) : bus.updateNguyenVong(nv);
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

    public boolean isSaved() { return saved; }
}
