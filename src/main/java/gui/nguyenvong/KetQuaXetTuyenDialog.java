package gui.nguyenvong;

import bus.NguyenVongBUS;
import bus.XetTuyenEngine;
import entity.NguyenVongXetTuyen;
import gui.component.CustomButton;
import gui.component.CustomTextField;
import gui.style.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class KetQuaXetTuyenDialog extends JDialog {

    private final NguyenVongBUS bus;
    private CustomTextField     txtCccd;
    private DefaultTableModel   tableModel;
    private JLabel              lblHoTen;

    public KetQuaXetTuyenDialog(Window parent, NguyenVongBUS bus) {
        super(parent, "Xem Ket Qua Xet Tuyen Theo Thi Sinh", ModalityType.APPLICATION_MODAL);
        this.bus = bus;
        setSize(800, 480);
        setLocationRelativeTo(parent);
        setResizable(true);
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setBackground(UIConstants.BACKGROUND_COLOR);
        root.setBorder(new EmptyBorder(14, 16, 10, 16));
        setContentPane(root);

        // ── Tra cứu ──
        JPanel pnlTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlTop.setOpaque(false);
        txtCccd = new CustomTextField(18);
        txtCccd.setPreferredSize(new Dimension(200, 34));
        CustomButton btnTim = new CustomButton("Tim", UIConstants.PRIMARY_COLOR);
        btnTim.setPreferredSize(new Dimension(80, 34));
        btnTim.addActionListener(e -> loadKetQua());
        txtCccd.addActionListener(e -> loadKetQua());
        lblHoTen = new JLabel(" ");
        lblHoTen.setFont(UIConstants.FONT_BOLD);
        lblHoTen.setForeground(UIConstants.TABLE_HEADER_COLOR);
        pnlTop.add(new JLabel("CCCD:") {{ setFont(UIConstants.FONT_BOLD); }});
        pnlTop.add(txtCccd);
        pnlTop.add(btnTim);
        pnlTop.add(lblHoTen);
        root.add(pnlTop, BorderLayout.NORTH);

        // ── Bảng kết quả ──
        String[] cols = {"TT", "Ma nganh", "Ten nganh", "To hop", "PT",
                         "DTHGXT", "Diem cong", "Uu tien", "Diem XT", "Ket qua"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tbl = new JTable(tableModel);
        tbl.setFont(UIConstants.FONT_NORMAL);
        tbl.setRowHeight(28);
        tbl.getTableHeader().setFont(UIConstants.FONT_BOLD);
        tbl.getTableHeader().setBackground(UIConstants.TABLE_HEADER_COLOR);
        tbl.getTableHeader().setForeground(Color.WHITE);

        // Cột kết quả có màu
        tbl.getColumnModel().getColumn(9).setCellRenderer(new KetQuaCellRenderer());

        int[] widths = {40, 80, 160, 65, 55, 70, 70, 65, 70, 120};
        for (int i = 0; i < widths.length; i++)
            tbl.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JScrollPane scroll = new JScrollPane(tbl);
        scroll.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR));
        root.add(scroll, BorderLayout.CENTER);

        // ── Đóng ──
        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlBtn.setOpaque(false);
        CustomButton btnClose = new CustomButton("Dong", UIConstants.GRAY_COLOR);
        btnClose.setPreferredSize(new Dimension(90, 34));
        btnClose.addActionListener(e -> dispose());
        pnlBtn.add(btnClose);
        root.add(pnlBtn, BorderLayout.SOUTH);
    }

    private void loadKetQua() {
        String cccd = txtCccd.getText().trim();
        tableModel.setRowCount(0);
        if (cccd.isEmpty()) return;

        List<NguyenVongXetTuyen> list = bus.getByCccd(cccd);
        if (list.isEmpty()) {
            lblHoTen.setText("Khong tim thay nguyen vong!");
            return;
        }

        // Hiện tên từ bản ghi đầu tiên (lazy-loaded nganh)
        NguyenVongXetTuyen first = list.get(0);
        lblHoTen.setText(first.getThiSinh() != null
                ? "→ " + first.getThiSinh().getHo() + " " + first.getThiSinh().getTen() : "");

        for (NguyenVongXetTuyen nv : list) {
            String tenNganh = nv.getNganh() != null ? nv.getNganh().getTenNganh() : "";
            tableModel.addRow(new Object[]{
                nv.getThuTuNguyenVong(),
                s(nv.getMaNganh()),
                tenNganh,
                s(nv.getToHopMon()),
                s(nv.getPhuongThuc()),
                f(nv.getDiemThxt()),
                f(nv.getDiemCong()),
                f(nv.getDiemUtqd()),
                f(nv.getDiemXetTuyen()),
                s(nv.getKetQua()),
            });
        }
    }

    private String s(String v) { return v != null ? v : ""; }
    private String f(Double v) { return v != null ? String.format("%.2f", v) : "-"; }

    // ── Renderer màu kết quả ─────────────────────────────
    static class KetQuaCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int col) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            setHorizontalAlignment(CENTER);
            setFont(UIConstants.FONT_BOLD);
            String kq = value != null ? value.toString() : "";
            if (!isSelected) {
                switch (kq) {
                    case XetTuyenEngine.KQ_TRUNG_TUYEN  -> { setBackground(new Color(209, 250, 229)); setForeground(new Color(6, 95, 70)); }
                    case XetTuyenEngine.KQ_KHONG_XET    -> { setBackground(new Color(243, 244, 246)); setForeground(new Color(107, 114, 128)); }
                    case XetTuyenEngine.KQ_TRUOT_NV,
                         XetTuyenEngine.KQ_TRUOT_NGANH  -> { setBackground(new Color(254, 226, 226)); setForeground(new Color(153, 27, 27)); }
                    default                              -> { setBackground(Color.WHITE); setForeground(Color.BLACK); }
                }
            }
            return this;
        }
    }
}
