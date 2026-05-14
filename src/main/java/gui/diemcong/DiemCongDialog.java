package gui.diemcong;

import bus.DiemCongBUS;
import bus.NganhBUS;
import bus.ThiSinhBUS;
import bus.ToHopMonThiBUS;
import entity.DiemCongXetTuyen;
import entity.Nganh;
import entity.ThiSinh;
import entity.ToHopMonThi;
import gui.component.CustomButton;
import gui.component.CustomTextField;
import gui.style.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class DiemCongDialog extends JDialog {

    private final DiemCongBUS   bus;
    private final DiemCongXetTuyen target;
    private boolean saved = false;

    private CustomTextField txtCccd;
    private JLabel          lblHoTen;
    private JComboBox<String> cboNganh, cboToHop, cboPhuongThuc;
    private JSpinner        spDiemCc, spDiemUtXt;
    private JTextArea       txtGhiChu;

    private final ThiSinhBUS     thiSinhBUS     = new ThiSinhBUS();
    private final NganhBUS       nganhBUS       = new NganhBUS();
    private final ToHopMonThiBUS toHopBUS       = new ToHopMonThiBUS();

    public DiemCongDialog(Window parent, DiemCongXetTuyen target, DiemCongBUS bus) {
        super(parent,
              target == null ? "Thêm mới điểm cộng" : "Sửa điểm cộng",
              ModalityType.APPLICATION_MODAL);
        this.bus    = bus;
        this.target = target;
        setSize(480, 520);
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
                target == null ? "THÊM MỚI ĐIỂM CỘNG" : "SỬA ĐIỂM CỘNG",
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

        // CCCD row: text + nút tra cứu
        txtCccd = new CustomTextField(16);
        CustomButton btnTraCuu = new CustomButton("Tra cứu", UIConstants.PRIMARY_COLOR);
        btnTraCuu.setPreferredSize(new Dimension(90, 30));
        btnTraCuu.addActionListener(e -> traCuuThiSinh());
        JPanel pnlCccd = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        pnlCccd.setOpaque(false);
        pnlCccd.add(txtCccd);
        pnlCccd.add(btnTraCuu);

        lblHoTen = new JLabel(" ");
        lblHoTen.setFont(UIConstants.FONT_SMALL);
        lblHoTen.setForeground(UIConstants.SUCCESS_COLOR);

        // Nganh combobox
        cboNganh = new JComboBox<>();
        cboNganh.setFont(UIConstants.FONT_NORMAL);
        loadNganh();

        // ToHop combobox
        cboToHop = new JComboBox<>();
        cboToHop.setFont(UIConstants.FONT_NORMAL);
        cboToHop.addItem("(Không có)");
        loadToHop();

        // Phuong thuc
        cboPhuongThuc = new JComboBox<>(new String[]{"THPT", "VSAT", "DGNL"});
        cboPhuongThuc.setFont(UIConstants.FONT_NORMAL);

        // Diem spinners
        spDiemCc   = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 10.0, 0.1));
        spDiemUtXt = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 3.0, 0.5));
        styleSpinner(spDiemCc);
        styleSpinner(spDiemUtXt);

        txtGhiChu = new JTextArea(3, 20);
        txtGhiChu.setFont(UIConstants.FONT_NORMAL);
        txtGhiChu.setLineWrap(true);
        txtGhiChu.setWrapStyleWord(true);
        txtGhiChu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)));
        JScrollPane scrGhiChu = new JScrollPane(txtGhiChu);

        Object[][] rows = {
            {"CCCD *",       pnlCccd},
            {"Họ tên TS",    lblHoTen},
            {"Ngành *",      cboNganh},
            {"Tổ hợp",       cboToHop},
            {"Phương thức *",cboPhuongThuc},
            {"Điểm CC",      spDiemCc},
            {"Điểm UTXT",    spDiemUtXt},
            {"Ghi chú",      scrGhiChu},
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
        pnlBtn.add(btnSave);
        pnlBtn.add(btnCancel);
        root.add(pnlBtn, BorderLayout.SOUTH);
    }

    private void loadNganh() {
        List<Nganh> list = nganhBUS.getAll();
        if (list != null) {
            for (Nganh n : list)
                cboNganh.addItem(n.getMaNganh() + " - " + n.getTenNganh());
        }
    }

    private void loadToHop() {
        List<ToHopMonThi> list = toHopBUS.getAll();
        if (list != null) {
            for (ToHopMonThi t : list)
                cboToHop.addItem(t.getMaToHop() + " - " + t.getTenToHop());
        }
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
        // Hiển thị họ tên nếu đã load lazy
        if (target.getThiSinh() != null)
            lblHoTen.setText(target.getThiSinh().getHo() + " " + target.getThiSinh().getTen());
        else
            traCuuThiSinh();

        selectComboByPrefix(cboNganh,    target.getMaNganh());
        selectComboByPrefix(cboToHop,    target.getMaToHop());

        if (target.getPhuongThuc() != null)
            cboPhuongThuc.setSelectedItem(target.getPhuongThuc());

        if (target.getDiemCc() != null)   spDiemCc.setValue(target.getDiemCc());
        if (target.getDiemUtXt() != null) spDiemUtXt.setValue(target.getDiemUtXt());
        if (target.getGhiChu() != null)   txtGhiChu.setText(target.getGhiChu());
    }

    private void selectComboByPrefix(JComboBox<String> cbo, String prefix) {
        if (prefix == null || prefix.isBlank()) return;
        for (int i = 0; i < cbo.getItemCount(); i++) {
            String item = cbo.getItemAt(i);
            if (item != null && item.startsWith(prefix)) { cbo.setSelectedIndex(i); return; }
        }
    }

    private void doSave() {
        DiemCongXetTuyen dc = target != null ? target : new DiemCongXetTuyen();

        dc.setCccd(txtCccd.getText().trim());

        // Lấy mã ngành từ combobox (trước dấu " - ")
        String nganhItem = (String) cboNganh.getSelectedItem();
        dc.setMaNganh(nganhItem != null ? nganhItem.split(" - ")[0].trim() : "");

        // Lấy mã tổ hợp
        String toHopItem = (String) cboToHop.getSelectedItem();
        if (toHopItem != null && !toHopItem.startsWith("(")) {
            dc.setMaToHop(toHopItem.split(" - ")[0].trim());
        } else {
            dc.setMaToHop("");
        }

        dc.setPhuongThuc((String) cboPhuongThuc.getSelectedItem());
        dc.setDiemCc(((Number) spDiemCc.getValue()).doubleValue());
        dc.setDiemUtXt(((Number) spDiemUtXt.getValue()).doubleValue());
        dc.setGhiChu(txtGhiChu.getText().trim());

        String result = target == null ? bus.addDiemCong(dc) : bus.updateDiemCong(dc);
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

    private void styleSpinner(JSpinner sp) {
        sp.setFont(UIConstants.FONT_NORMAL);
        ((JSpinner.DefaultEditor) sp.getEditor()).getTextField().setFont(UIConstants.FONT_NORMAL);
    }

    public boolean isSaved() { return saved; }
}
