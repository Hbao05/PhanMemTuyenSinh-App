package gui.nganhtohop;

import bus.NganhBUS;
import bus.NganhToHopBUS;
import bus.ToHopMonThiBUS;
import entity.Nganh;
import entity.NganhToHop;
import entity.ToHopMonThi;
import gui.component.CustomButton;
import gui.component.CustomTextField;
import gui.style.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class NganhToHopDialog extends JDialog {

    private final NganhToHopBUS bus;
    private final NganhToHop item; // null if adding
    private boolean isSaved = false;

    private JComboBox<String> cbxNganh, cbxToHop;
    private CustomTextField txtHs1, txtHs2, txtHs3, txtDoLech;
    private JLabel lblMon1, lblMon2, lblMon3;

    public NganhToHopDialog(Window parent, NganhToHop item, NganhToHopBUS bus) {
        super(parent, item == null ? "Thêm liên kết Ngành-Tổ hợp" : "Sửa liên kết Ngành-Tổ hợp", ModalityType.APPLICATION_MODAL);
        this.bus = bus;
        this.item = item;

        setSize(500, 500);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIConstants.BACKGROUND_COLOR);

        buildUI();
        loadInitialData();
        if (item != null) fillForm();
    }

    private void buildUI() {
        JLabel lblTitle = new JLabel(item == null ? "THÊM LIÊN KẾT MỚI" : "SỬA LIÊN KẾT", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(UIConstants.TABLE_HEADER_COLOR);
        lblTitle.setBorder(new EmptyBorder(15, 0, 10, 0));
        add(lblTitle, BorderLayout.NORTH);

        JPanel pnlForm = new JPanel(new GridLayout(8, 2, 10, 15));
        pnlForm.setOpaque(false);
        pnlForm.setBorder(new EmptyBorder(10, 40, 20, 40));

        pnlForm.add(new JLabel("Chọn Ngành (*)"));
        cbxNganh = new JComboBox<>();
        pnlForm.add(cbxNganh);

        pnlForm.add(new JLabel("Chọn Tổ hợp (*)"));
        cbxToHop = new JComboBox<>();
        cbxToHop.addActionListener(e -> updateSubjectLabels());
        pnlForm.add(cbxToHop);

        lblMon1 = new JLabel("Môn 1:");
        pnlForm.add(lblMon1);
        txtHs1 = new CustomTextField(10);
        txtHs1.setText("1");
        pnlForm.add(txtHs1);

        lblMon2 = new JLabel("Môn 2:");
        pnlForm.add(lblMon2);
        txtHs2 = new CustomTextField(10);
        txtHs2.setText("1");
        pnlForm.add(txtHs2);

        lblMon3 = new JLabel("Môn 3:");
        pnlForm.add(lblMon3);
        txtHs3 = new CustomTextField(10);
        txtHs3.setText("1");
        pnlForm.add(txtHs3);

        pnlForm.add(new JLabel("Độ lệch:"));
        txtDoLech = new CustomTextField(10);
        txtDoLech.setText("0.0");
        pnlForm.add(txtDoLech);

        add(pnlForm, BorderLayout.CENTER);

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        pnlButtons.setOpaque(false);
        CustomButton btnCancel = new CustomButton("Hủy", UIConstants.DANGER_COLOR);
        CustomButton btnSave = new CustomButton("Lưu", UIConstants.SUCCESS_COLOR);
        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> save());
        pnlButtons.add(btnCancel);
        pnlButtons.add(btnSave);
        add(pnlButtons, BorderLayout.SOUTH);
    }

    private void loadInitialData() {
        NganhBUS nBus = new NganhBUS();
        List<Nganh> nList = nBus.getAll();
        if (nList != null) {
            for (Nganh n : nList) cbxNganh.addItem(n.getMaNganh() + " - " + n.getTenNganh());
        }

        ToHopMonThiBUS tBus = new ToHopMonThiBUS();
        List<ToHopMonThi> tList = tBus.getAll();
        if (tList != null) {
            for (ToHopMonThi t : tList) cbxToHop.addItem(t.getMaToHop());
        }
    }

    private void updateSubjectLabels() {
        String maToHop = (String) cbxToHop.getSelectedItem();
        if (maToHop == null) return;

        ToHopMonThiBUS tBus = new ToHopMonThiBUS();
        ToHopMonThi t = null;
        for (ToHopMonThi item : tBus.getAll()) {
            if (item.getMaToHop().equals(maToHop)) {
                t = item;
                break;
            }
        }

        if (t != null) {
            lblMon1.setText("Hệ số " + t.getMon1() + ":");
            lblMon2.setText("Hệ số " + t.getMon2() + ":");
            lblMon3.setText("Hệ số " + t.getMon3() + ":");
        }
    }

    private void fillForm() {
        // ComboBox chứa chuỗi "maNganh - tenNganh", dùng startsWith để tìm đúng item
        String maNganhCanChon = item.getMaNganh();
        for (int i = 0; i < cbxNganh.getItemCount(); i++) {
            if (cbxNganh.getItemAt(i).startsWith(maNganhCanChon + " - ")) {
                cbxNganh.setSelectedIndex(i);
                break;
            }
        }
        cbxNganh.setEnabled(false); // Không cho sửa ngành/tổ hợp khi đang edit liên kết
        cbxToHop.setSelectedItem(item.getMaToHop());
        cbxToHop.setEnabled(false);
        txtHs1.setText(String.valueOf(item.getHsMon1()));
        txtHs2.setText(String.valueOf(item.getHsMon2()));
        txtHs3.setText(String.valueOf(item.getHsMon3()));
        txtDoLech.setText(String.valueOf(item.getDoLech()));
        updateSubjectLabels();
    }

    private void save() {
        NganhToHop data = (item != null) ? item : new NganhToHop();
        
        String selNganh = (String) cbxNganh.getSelectedItem();
        if (selNganh == null) return;
        String maNganh = selNganh.split(" - ")[0];
        String maToHop = (String) cbxToHop.getSelectedItem();

        data.setMaNganh(maNganh);
        data.setMaToHop(maToHop);
        data.setTbKeys(maNganh + "_" + maToHop);
        
        try {
            data.setHsMon1(Integer.parseInt(txtHs1.getText().trim()));
            data.setHsMon2(Integer.parseInt(txtHs2.getText().trim()));
            data.setHsMon3(Integer.parseInt(txtHs3.getText().trim()));
            data.setDoLech(Double.parseDouble(txtDoLech.getText().trim()));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Hệ số và độ lệch phải là số!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Bổ sung thông tin môn học
        ToHopMonThiBUS tBus = new ToHopMonThiBUS();
        ToHopMonThi t = null;
        for (ToHopMonThi thmt : tBus.getAll()) {
            if (thmt.getMaToHop().equals(maToHop)) {
                t = thmt;
                break;
            }
        }
        if (t != null) {
            data.setThMon1(t.getMon1());
            data.setThMon2(t.getMon2());
            data.setThMon3(t.getMon3());
            
            // Reset các cột điểm
            data.setTo(null); data.setVa(null); data.setLi(null); data.setHo(null);
            data.setSi(null); data.setSu(null); data.setDi(null); data.setN1(null);
            data.setTi(null); data.setKtpl(null); data.setKhac(null);

            updateSubjectCoefficient(data, t.getMon1(), data.getHsMon1());
            updateSubjectCoefficient(data, t.getMon2(), data.getHsMon2());
            updateSubjectCoefficient(data, t.getMon3(), data.getHsMon3());
        }

        String result = (item == null) ? bus.addNganhToHop(data) : bus.updateNganhToHop(data);
        if (result.startsWith("Success")) {
            JOptionPane.showMessageDialog(this, "Lưu thành công!");
            isSaved = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, result, "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateSubjectCoefficient(NganhToHop item, String code, int hs) {
        if (code == null) return;
        switch (code.toUpperCase()) {
            case "TO": item.setTo(hs); break;
            case "VA": item.setVa(hs); break;
            case "LI": item.setLi(hs); break;
            case "HO": item.setHo(hs); break;
            case "SI": item.setSi(hs); break;
            case "SU": item.setSu(hs); break;
            case "DI": item.setDi(hs); break;
            case "N1": item.setN1(hs); break;
            case "TI": item.setTi(hs); break;
            case "KTPL": item.setKtpl(hs); break;
            default: item.setKhac(hs); break;
        }
    }

    public boolean isSaved() { return isSaved; }
}
