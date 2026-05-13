package gui.diemthi;

import bus.DiemThiBUS;
import entity.DiemThiXetTuyen;
import gui.component.CustomButton;
import gui.component.CustomTextField;
import gui.style.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class DiemThiDialog extends JDialog {

    private final DiemThiBUS      bus;
    private final DiemThiXetTuyen target;
    private boolean saved = false;

    private CustomTextField txtCccd, txtSbd;
    private JRadioButton    rdoThpt, rdoVsat, rdoDgnl;
    private CardLayout      cardScore;
    private JPanel          pnlScore;

    // THPT/VSAT fields (shared, label differs)
    private JSpinner spToan, spLy, spHoa, spSinh, spVan, spSu, spDia, spTiengAnh;
    // ĐGNL fields
    private JSpinner spNl1, spNk1, spNk2, spCncn, spCnnn, spKtpl;

    public DiemThiDialog(Window parent, DiemThiXetTuyen target, DiemThiBUS bus) {
        super(parent,
              target == null ? "Them moi Diem Thi" : "Sua Diem Thi",
              ModalityType.APPLICATION_MODAL);
        this.bus    = bus;
        this.target = target;
        setSize(540, 580);
        setLocationRelativeTo(parent);
        setResizable(false);
        initUI();
        if (target != null) fillForm();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(0, 4));
        root.setBackground(UIConstants.BACKGROUND_COLOR);
        root.setBorder(new EmptyBorder(0, 0, 0, 0));
        setContentPane(root);

        JLabel lblTitle = new JLabel(
                target == null ? "THEM MOI DIEM THI" : "SUA DIEM THI",
                SwingConstants.CENTER);
        lblTitle.setFont(UIConstants.FONT_HEADER);
        lblTitle.setForeground(UIConstants.TABLE_HEADER_COLOR);
        lblTitle.setBorder(new EmptyBorder(14, 0, 8, 0));
        root.add(lblTitle, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 6));
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(0, 20, 0, 20));

        // ── Phần thông tin chung ──
        JPanel pnlInfo = new JPanel(new GridBagLayout());
        pnlInfo.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets  = new Insets(4, 4, 4, 4);
        gc.anchor  = GridBagConstraints.WEST;

        txtCccd = new CustomTextField(18);
        txtSbd  = new CustomTextField(18);

        addRow(pnlInfo, gc, 0, "CCCD *",    txtCccd);
        addRow(pnlInfo, gc, 1, "So bao danh", txtSbd);

        // ── Radio phương thức ──
        JPanel pnlPt = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        pnlPt.setOpaque(false);
        rdoThpt = new JRadioButton("THPT");
        rdoVsat = new JRadioButton("VSAT");
        rdoDgnl = new JRadioButton("DGNL");
        rdoThpt.setFont(UIConstants.FONT_BOLD); rdoThpt.setOpaque(false);
        rdoVsat.setFont(UIConstants.FONT_BOLD); rdoVsat.setOpaque(false);
        rdoDgnl.setFont(UIConstants.FONT_BOLD); rdoDgnl.setOpaque(false);
        ButtonGroup bg = new ButtonGroup();
        bg.add(rdoThpt); bg.add(rdoVsat); bg.add(rdoDgnl);
        rdoThpt.setSelected(true);
        pnlPt.add(new JLabel("Phuong thuc *:") {{ setFont(UIConstants.FONT_BOLD); }});
        pnlPt.add(rdoThpt); pnlPt.add(rdoVsat); pnlPt.add(rdoDgnl);
        addRow(pnlInfo, gc, 2, null, pnlPt);

        center.add(pnlInfo, BorderLayout.NORTH);

        // ── Card panels cho điểm ──
        cardScore = new CardLayout();
        pnlScore  = new JPanel(cardScore);
        pnlScore.setOpaque(false);

        // Tạo spinners THPT/VSAT (chung model, max thay đổi theo PT)
        spToan    = makeSpinner(0, 150, 0.25);
        spLy      = makeSpinner(0, 150, 0.25);
        spHoa     = makeSpinner(0, 150, 0.25);
        spSinh    = makeSpinner(0, 150, 0.25);
        spVan     = makeSpinner(0, 150, 0.25);
        spSu      = makeSpinner(0, 150, 0.25);
        spDia     = makeSpinner(0, 150, 0.25);
        spTiengAnh= makeSpinner(0, 150, 0.25);

        pnlScore.add(buildThptVsatPanel(), "THPT");
        pnlScore.add(buildThptVsatPanel2(), "VSAT");
        pnlScore.add(buildDgnlPanel(),     "DGNL");

        cardScore.show(pnlScore, "THPT");
        center.add(pnlScore, BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);

        // ── Buttons ──
        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        pnlBtn.setOpaque(false);
        CustomButton btnSave   = new CustomButton("Luu",  UIConstants.SUCCESS_COLOR);
        CustomButton btnCancel = new CustomButton("Huy",  UIConstants.GRAY_COLOR);
        btnSave.setPreferredSize(new Dimension(110, 36));
        btnCancel.setPreferredSize(new Dimension(90, 36));
        btnSave.addActionListener(e -> doSave());
        btnCancel.addActionListener(e -> dispose());
        pnlBtn.add(btnSave); pnlBtn.add(btnCancel);
        root.add(pnlBtn, BorderLayout.SOUTH);

        // ── Radio listeners ──
        rdoThpt.addActionListener(e -> cardScore.show(pnlScore, "THPT"));
        rdoVsat.addActionListener(e -> cardScore.show(pnlScore, "VSAT"));
        rdoDgnl.addActionListener(e -> cardScore.show(pnlScore, "DGNL"));
    }

    private JPanel buildThptVsatPanel() {
        return buildScoreGrid(new Object[][]{
            {"Toan (0-10)", spToan}, {"Ly (0-10)", spLy},
            {"Hoa (0-10)", spHoa},  {"Sinh (0-10)", spSinh},
            {"Van (0-10)", spVan},  {"Su (0-10)", spSu},
            {"Dia (0-10)", spDia},  {"Tieng Anh (0-10)", spTiengAnh},
        }, "Diem thi THPT");
    }

    private JPanel buildThptVsatPanel2() {
        // Tái dùng cùng spinner nhưng với label VSAT
        return buildScoreGrid(new Object[][]{
            {"Toan (0-150)", spToan}, {"Ly (0-150)", spLy},
            {"Hoa (0-150)", spHoa},  {"Sinh (0-150)", spSinh},
            {"Van (0-150)", spVan},  {"Su (0-150)", spSu},
            {"Dia (0-150)", spDia},  {"Tieng Anh (0-150)", spTiengAnh},
        }, "Diem thi V-SAT");
    }

    private JPanel buildDgnlPanel() {
        spNl1  = makeSpinner(0, 1000, 1);
        spNk1  = makeSpinner(0, 1000, 1);
        spNk2  = makeSpinner(0, 1000, 1);
        spCncn = makeSpinner(0, 1000, 1);
        spCnnn = makeSpinner(0, 1000, 1);
        spKtpl = makeSpinner(0, 1000, 1);
        return buildScoreGrid(new Object[][]{
            {"NL1",  spNl1},  {"NK1", spNk1},
            {"NK2",  spNk2},  {"CNCN", spCncn},
            {"CNNN", spCnnn}, {"KTPL", spKtpl},
        }, "Diem thi DGNL");
    }

    private JPanel buildScoreGrid(Object[][] rows, String title) {
        JPanel pnl = new JPanel(new GridLayout(0, 2, 8, 4));
        pnl.setOpaque(false);
        pnl.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_COLOR),
                title, TitledBorder.LEFT, TitledBorder.TOP,
                UIConstants.FONT_BOLD, UIConstants.TABLE_HEADER_COLOR));
        for (Object[] row : rows) {
            JLabel lbl = new JLabel((String) row[0]);
            lbl.setFont(UIConstants.FONT_NORMAL);
            pnl.add(lbl);
            pnl.add((Component) row[1]);
        }
        return pnl;
    }

    private JSpinner makeSpinner(double min, double max, double step) {
        JSpinner sp = new JSpinner(new SpinnerNumberModel(0.0, min, max, step));
        sp.setFont(UIConstants.FONT_NORMAL);
        ((JSpinner.DefaultEditor) sp.getEditor()).getTextField().setFont(UIConstants.FONT_NORMAL);
        return sp;
    }

    private void addRow(JPanel pnl, GridBagConstraints gc, int row, String label, Component field) {
        if (label != null) {
            gc.gridx = 0; gc.gridy = row; gc.fill = GridBagConstraints.NONE; gc.weightx = 0;
            JLabel lbl = new JLabel(label);
            lbl.setFont(UIConstants.FONT_BOLD);
            lbl.setPreferredSize(new Dimension(120, 28));
            pnl.add(lbl, gc);
            gc.gridx = 1; gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1;
            pnl.add(field, gc);
        } else {
            gc.gridx = 0; gc.gridy = row; gc.gridwidth = 2;
            gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1;
            pnl.add(field, gc);
            gc.gridwidth = 1;
        }
    }

    private void fillForm() {
        txtCccd.setText(target.getCccd()       != null ? target.getCccd()       : "");
        txtSbd.setText(target.getSoBaoDanh()    != null ? target.getSoBaoDanh() : "");

        String pt = target.getPhuongThuc();
        if ("VSAT".equals(pt))  { rdoVsat.setSelected(true); cardScore.show(pnlScore, "VSAT"); }
        else if ("DGNL".equals(pt)) { rdoDgnl.setSelected(true); cardScore.show(pnlScore, "DGNL"); }
        else                    { rdoThpt.setSelected(true); cardScore.show(pnlScore, "THPT"); }

        safeSet(spToan,    target.getDiemToan());
        safeSet(spLy,      target.getDiemLy());
        safeSet(spHoa,     target.getDiemHoa());
        safeSet(spSinh,    target.getDiemSinh());
        safeSet(spVan,     target.getDiemVan());
        safeSet(spSu,      target.getDiemSu());
        safeSet(spDia,     target.getDiemDia());
        safeSet(spTiengAnh,target.getDiemTiengAnh());

        if (spNl1 != null) {
            safeSet(spNl1,  target.getNl1());
            safeSet(spNk1,  target.getNk1());
            safeSet(spNk2,  target.getNk2());
            safeSet(spCncn, target.getCncn());
            safeSet(spCnnn, target.getCnnn());
            safeSet(spKtpl, target.getDiemKtpl());
        }
    }

    private void safeSet(JSpinner sp, Double val) {
        if (val != null) sp.setValue(val);
    }

    private void doSave() {
        DiemThiXetTuyen dt = target != null ? target : new DiemThiXetTuyen();

        dt.setCccd(txtCccd.getText().trim());
        dt.setSoBaoDanh(txtSbd.getText().trim());

        String pt = rdoVsat.isSelected() ? "VSAT" : rdoDgnl.isSelected() ? "DGNL" : "THPT";
        dt.setPhuongThuc(pt);

        if ("DGNL".equals(pt)) {
            dt.setNl1(val(spNl1));   dt.setNk1(val(spNk1));
            dt.setNk2(val(spNk2));   dt.setCncn(val(spCncn));
            dt.setCnnn(val(spCnnn)); dt.setDiemKtpl(val(spKtpl));
            dt.setDiemToan(null); dt.setDiemLy(null); dt.setDiemHoa(null);
            dt.setDiemSinh(null); dt.setDiemVan(null); dt.setDiemSu(null);
            dt.setDiemDia(null);  dt.setDiemTiengAnh(null);
        } else {
            dt.setDiemToan(val(spToan));     dt.setDiemLy(val(spLy));
            dt.setDiemHoa(val(spHoa));       dt.setDiemSinh(val(spSinh));
            dt.setDiemVan(val(spVan));       dt.setDiemSu(val(spSu));
            dt.setDiemDia(val(spDia));       dt.setDiemTiengAnh(val(spTiengAnh));
            if (spNl1 != null) {
                dt.setNl1(null); dt.setNk1(null); dt.setNk2(null);
                dt.setCncn(null); dt.setCnnn(null); dt.setDiemKtpl(null);
            }
        }

        String result = target == null ? bus.addDiemThi(dt) : bus.updateDiemThi(dt);
        if (result.startsWith("Success")) {
            JOptionPane.showMessageDialog(this,
                    target == null ? "Them moi thanh cong!" : "Cap nhat thanh cong!",
                    "Thong bao", JOptionPane.INFORMATION_MESSAGE);
            saved = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, result.replace("Error: ", ""),
                    "Loi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private double val(JSpinner sp) {
        return ((Number) sp.getValue()).doubleValue();
    }

    public boolean isSaved() { return saved; }
}
