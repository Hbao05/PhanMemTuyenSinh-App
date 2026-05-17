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

    // Spinners THPT (thang 10)
    private JSpinner spThptToan, spThptLy, spThptHoa, spThptSinh;
    private JSpinner spThptVan, spThptSu, spThptDia, spThptN1;

    // Spinners VSAT (thang 150, Tiếng Anh → N1_THI)
    private JSpinner spVsatToan, spVsatLy, spVsatHoa, spVsatSinh;
    private JSpinner spVsatVan, spVsatSu, spVsatDia, spVsatN1;

    // Spinners ĐGNL
    private JSpinner spNl1, spNk1, spNk2, spNk3, spNk4, spNk5, spNk6, spNk7, spNk8, spNk9, spNk10;
    private JSpinner spCncn, spCnnn, spKtpl;

    public DiemThiDialog(Window parent, DiemThiXetTuyen target, DiemThiBUS bus) {
        super(parent,
                target == null ? "Thêm mới điểm thi" : "Sửa điểm thi",
                ModalityType.APPLICATION_MODAL);
        this.bus    = bus;
        this.target = target;
        setSize(560, 620);
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
                target == null ? "THÊM MỚI ĐIỂM THI" : "SỬA ĐIỂM THI",
                SwingConstants.CENTER);
        lblTitle.setFont(UIConstants.FONT_HEADER);
        lblTitle.setForeground(UIConstants.TABLE_HEADER_COLOR);
        lblTitle.setBorder(new EmptyBorder(14, 0, 8, 0));
        root.add(lblTitle, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 6));
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(0, 20, 0, 20));

        // ── Thông tin chung ──
        JPanel pnlInfo = new JPanel(new GridBagLayout());
        pnlInfo.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.anchor = GridBagConstraints.WEST;

        txtCccd = new CustomTextField(18);
        txtSbd  = new CustomTextField(18);
        addRow(pnlInfo, gc, 0, "CCCD *",      txtCccd);
        addRow(pnlInfo, gc, 1, "Số báo danh", txtSbd);

        // Khóa CCCD và phương thức khi đang sửa
        if (target != null) {
            txtCccd.setEditable(false);
            txtCccd.setBackground(new Color(230, 230, 230));
        }

        // ── Radio phương thức ──
        JPanel pnlPt = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        pnlPt.setOpaque(false);
        rdoThpt = new JRadioButton("THPT");
        rdoVsat = new JRadioButton("VSAT");
        rdoDgnl = new JRadioButton("ĐGNL");
        rdoThpt.setFont(UIConstants.FONT_BOLD); rdoThpt.setOpaque(false);
        rdoVsat.setFont(UIConstants.FONT_BOLD); rdoVsat.setOpaque(false);
        rdoDgnl.setFont(UIConstants.FONT_BOLD); rdoDgnl.setOpaque(false);
        ButtonGroup bg = new ButtonGroup();
        bg.add(rdoThpt); bg.add(rdoVsat); bg.add(rdoDgnl);
        rdoThpt.setSelected(true);

        // Khi sửa: khóa radio, không cho đổi phương thức
        if (target != null) {
            rdoThpt.setEnabled(false);
            rdoVsat.setEnabled(false);
            rdoDgnl.setEnabled(false);
        }

        pnlPt.add(new JLabel("Phương thức *:") {{ setFont(UIConstants.FONT_BOLD); }});
        pnlPt.add(rdoThpt); pnlPt.add(rdoVsat); pnlPt.add(rdoDgnl);
        addRow(pnlInfo, gc, 2, null, pnlPt);
        center.add(pnlInfo, BorderLayout.NORTH);

        // ── Card panels ──
        cardScore = new CardLayout();
        pnlScore  = new JPanel(cardScore);
        pnlScore.setOpaque(false);

        // Tạo TẤT CẢ spinners trước khi build panels
        buildAllSpinners();

        pnlScore.add(buildThptPanel(), "THPT");
        pnlScore.add(buildVsatPanel(), "VSAT");
        pnlScore.add(buildDgnlPanel(), "DGNL");

        cardScore.show(pnlScore, "THPT");
        center.add(pnlScore, BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);

        // ── Buttons ──
        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        pnlBtn.setOpaque(false);
        CustomButton btnSave   = new CustomButton("Lưu", UIConstants.SUCCESS_COLOR);
        CustomButton btnCancel = new CustomButton("Hủy", UIConstants.GRAY_COLOR);
        btnSave.setPreferredSize(new Dimension(110, 36));
        btnCancel.setPreferredSize(new Dimension(90, 36));
        btnSave.addActionListener(e -> doSave());
        btnCancel.addActionListener(e -> dispose());
        pnlBtn.add(btnSave); pnlBtn.add(btnCancel);
        root.add(pnlBtn, BorderLayout.SOUTH);

        // ── Radio listeners (chỉ khi thêm mới) ──
        rdoThpt.addActionListener(e -> cardScore.show(pnlScore, "THPT"));
        rdoVsat.addActionListener(e -> cardScore.show(pnlScore, "VSAT"));
        rdoDgnl.addActionListener(e -> cardScore.show(pnlScore, "DGNL"));
    }

    /** Khởi tạo tất cả spinners trước - tách khỏi buildPanel để fillForm luôn có object */
    private void buildAllSpinners() {
        // THPT: thang 10
        spThptToan = makeSpinner(0, 10, 0.25);
        spThptLy   = makeSpinner(0, 10, 0.25);
        spThptHoa  = makeSpinner(0, 10, 0.25);
        spThptSinh = makeSpinner(0, 10, 0.25);
        spThptVan  = makeSpinner(0, 10, 0.25);
        spThptSu   = makeSpinner(0, 10, 0.25);
        spThptDia  = makeSpinner(0, 10, 0.25);
        spThptN1   = makeSpinner(0, 10, 0.25);  // N1_THI

        // VSAT: thang 150
        spVsatToan = makeSpinner(0, 150, 0.25);
        spVsatLy   = makeSpinner(0, 150, 0.25);
        spVsatHoa  = makeSpinner(0, 150, 0.25);
        spVsatSinh = makeSpinner(0, 150, 0.25);
        spVsatVan  = makeSpinner(0, 150, 0.25);
        spVsatSu   = makeSpinner(0, 150, 0.25);
        spVsatDia  = makeSpinner(0, 150, 0.25);
        spVsatN1   = makeSpinner(0, 150, 0.25); // N1_THI

        // ĐGNL: thang 1000
        spNl1  = makeSpinner(0, 1200, 1);
        spNk1  = makeSpinner(0, 1200, 1);
        spNk2  = makeSpinner(0, 1200, 1);
        spNk3  = makeSpinner(0, 1200, 1);
        spNk4  = makeSpinner(0, 1200, 1);
        spNk5  = makeSpinner(0, 1200, 1);
        spNk6  = makeSpinner(0, 1200, 1);
        spNk7  = makeSpinner(0, 1200, 1);
        spNk8  = makeSpinner(0, 1200, 1);
        spNk9  = makeSpinner(0, 1200, 1);
        spNk10 = makeSpinner(0, 1200, 1);
        spCncn = makeSpinner(0, 1200, 1);
        spCnnn = makeSpinner(0, 1200, 1);
        spKtpl = makeSpinner(0, 1200, 1);
    }

    private JPanel buildThptPanel() {
        return buildScoreGrid(new Object[][]{
                {"Toán (0-10)",      spThptToan}, {"Lý (0-10)",       spThptLy},
                {"Hóa (0-10)",       spThptHoa},  {"Sinh (0-10)",     spThptSinh},
                {"Văn (0-10)",       spThptVan},  {"Sử (0-10)",       spThptSu},
                {"Địa (0-10)",       spThptDia},  {"Tiếng Anh (0-10)",spThptN1},
        }, "Điểm thi THPT");
    }

    private JPanel buildVsatPanel() {
        return buildScoreGrid(new Object[][]{
                {"Toán (0-150)",      spVsatToan}, {"Lý (0-150)",        spVsatLy},
                {"Hóa (0-150)",       spVsatHoa},  {"Sinh (0-150)",      spVsatSinh},
                {"Văn (0-150)",       spVsatVan},  {"Sử (0-150)",        spVsatSu},
                {"Địa (0-150)",       spVsatDia},  {"Tiếng Anh (0-150)", spVsatN1},
        }, "Điểm thi V-SAT");
    }

    private JPanel buildDgnlPanel() {
        return buildScoreGrid(new Object[][]{
                {"NL1 (0-1200)",  spNl1},  {"CNCN",          spCncn},
                {"CNNN",          spCnnn}, {"KTPL",           spKtpl},
                {"NK1",           spNk1},  {"NK2",            spNk2},
                {"NK3",           spNk3},  {"NK4",            spNk4},
                {"NK5",           spNk5},  {"NK6",            spNk6},
                {"NK7",           spNk7},  {"NK8",            spNk8},
                {"NK9",           spNk9},  {"NK10",           spNk10},
        }, "Điểm thi ĐGNL");
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
        txtCccd.setText(nvl(target.getCccd()));
        txtSbd.setText(nvl(target.getSoBaoDanh()));

        String pt = target.getPhuongThuc();
        if ("3".equals(pt)) {
            rdoVsat.setSelected(true);
            cardScore.show(pnlScore, "VSAT");
            // VSAT: điểm các môn lưu ở cùng cột THPT (TO, LI, HO, SI, VA, SU, DI)
            // Tiếng Anh VSAT lưu ở N1_THI
            safeSet(spVsatToan, target.getDiemToan());
            safeSet(spVsatLy,   target.getDiemLy());
            safeSet(spVsatHoa,  target.getDiemHoa());
            safeSet(spVsatSinh, target.getDiemSinh());
            safeSet(spVsatVan,  target.getDiemVan());
            safeSet(spVsatSu,   target.getDiemSu());
            safeSet(spVsatDia,  target.getDiemDia());
            safeSet(spVsatN1,   target.getN1Thi());  // N1_THI ← đúng cột

        } else if ("2".equals(pt)) {
            rdoDgnl.setSelected(true);
            cardScore.show(pnlScore, "DGNL");
            safeSet(spNl1,  target.getNl1());
            safeSet(spNk1,  target.getNk1());
            safeSet(spNk2,  target.getNk2());
            safeSet(spNk3,  target.getNk3());
            safeSet(spNk4,  target.getNk4());
            safeSet(spNk5,  target.getNk5());
            safeSet(spNk6,  target.getNk6());
            safeSet(spNk7,  target.getNk7());
            safeSet(spNk8,  target.getNk8());
            safeSet(spNk9,  target.getNk9());
            safeSet(spNk10, target.getNk10());
            safeSet(spCncn, target.getCncn());
            safeSet(spCnnn, target.getCnnn());
            safeSet(spKtpl, target.getDiemKtpl());

        } else {
            // THPT (pt = "4")
            rdoThpt.setSelected(true);
            cardScore.show(pnlScore, "THPT");
            safeSet(spThptToan, target.getDiemToan());
            safeSet(spThptLy,   target.getDiemLy());
            safeSet(spThptHoa,  target.getDiemHoa());
            safeSet(spThptSinh, target.getDiemSinh());
            safeSet(spThptVan,  target.getDiemVan());
            safeSet(spThptSu,   target.getDiemSu());
            safeSet(spThptDia,  target.getDiemDia());
            safeSet(spThptN1,   target.getN1Thi()); // N1_THI
        }
    }

    private void safeSet(JSpinner sp, Double val) {
        if (val != null) {
            // Clamp vào đúng range của spinner để tránh crash
            SpinnerNumberModel m = (SpinnerNumberModel) sp.getModel();
            double min = ((Number) m.getMinimum()).doubleValue();
            double max = ((Number) m.getMaximum()).doubleValue();
            sp.setValue(Math.max(min, Math.min(max, val)));
        }
    }

    private void doSave() {
        DiemThiXetTuyen dt = target != null ? target : new DiemThiXetTuyen();
        dt.setCccd(txtCccd.getText().trim());
        dt.setSoBaoDanh(txtSbd.getText().trim());

        // pt lưu mã số "4"/"3"/"2"
        String pt = rdoVsat.isSelected() ? "3" : rdoDgnl.isSelected() ? "2" : "4";
        dt.setPhuongThuc(pt);

        // Null hết trước, rồi set đúng theo phương thức
        clearAllScores(dt);

        if ("4".equals(pt)) {
            // THPT: thang 10, Tiếng Anh → N1_THI
            dt.setDiemToan(val(spThptToan));
            dt.setDiemLy(val(spThptLy));
            dt.setDiemHoa(val(spThptHoa));
            dt.setDiemSinh(val(spThptSinh));
            dt.setDiemVan(val(spThptVan));
            dt.setDiemSu(val(spThptSu));
            dt.setDiemDia(val(spThptDia));
            dt.setN1Thi(val(spThptN1));

        } else if ("3".equals(pt)) {
            // VSAT: thang 150, Tiếng Anh → N1_THI
            dt.setDiemToan(val(spVsatToan));
            dt.setDiemLy(val(spVsatLy));
            dt.setDiemHoa(val(spVsatHoa));
            dt.setDiemSinh(val(spVsatSinh));
            dt.setDiemVan(val(spVsatVan));
            dt.setDiemSu(val(spVsatSu));
            dt.setDiemDia(val(spVsatDia));
            dt.setN1Thi(val(spVsatN1));

        } else {
            // ĐGNL (pt = "2")
            dt.setNl1(val(spNl1));
            dt.setNk1(val(spNk1));
            dt.setNk2(val(spNk2));
            dt.setNk3(val(spNk3));
            dt.setNk4(val(spNk4));
            dt.setNk5(val(spNk5));
            dt.setNk6(val(spNk6));
            dt.setNk7(val(spNk7));
            dt.setNk8(val(spNk8));
            dt.setNk9(val(spNk9));
            dt.setNk10(val(spNk10));
            dt.setCncn(val(spCncn));
            dt.setCnnn(val(spCnnn));
            dt.setDiemKtpl(val(spKtpl));
        }

        String result = target == null ? bus.addDiemThi(dt) : bus.updateDiemThi(dt);
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

    /** Null toàn bộ điểm trước khi set theo phương thức */
    private void clearAllScores(DiemThiXetTuyen dt) {
        dt.setDiemToan(null); dt.setDiemLy(null);  dt.setDiemHoa(null);
        dt.setDiemSinh(null); dt.setDiemVan(null);  dt.setDiemSu(null);
        dt.setDiemDia(null);  dt.setDiemGdcd(null); dt.setN1Thi(null);
        dt.setN1Cc(null);     dt.setDiemTiengAnh(null); dt.setDiemKtpl(null);
        dt.setNl1(null);
        dt.setNk1(null); dt.setNk2(null); dt.setNk3(null); dt.setNk4(null);
        dt.setNk5(null); dt.setNk6(null); dt.setNk7(null); dt.setNk8(null);
        dt.setNk9(null); dt.setNk10(null);
        dt.setCncn(null); dt.setCnnn(null);
    }

    private double val(JSpinner sp) {
        return ((Number) sp.getValue()).doubleValue();
    }

    private String nvl(String s) { return s != null ? s : ""; }

    public boolean isSaved() { return saved; }
}