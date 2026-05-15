package gui.component;

import gui.style.UIConstants;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class CustomTable extends JTable {

    public CustomTable(DefaultTableModel model) {
        super(model);
        setFont(UIConstants.FONT_NORMAL);
        setRowHeight(32);
        setShowGrid(true);
        setGridColor(UIConstants.TABLE_GRID_COLOR);
        setIntercellSpacing(new Dimension(0, 1));
        setSelectionBackground(UIConstants.TABLE_SELECTION);
        setSelectionForeground(UIConstants.TABLE_SELECTION_FG);
        setFillsViewportHeight(true);
        setBackground(Color.WHITE);

        // Zebra-striped row renderer
        setDefaultRenderer(Object.class, new ZebraRenderer(SwingConstants.LEFT));

        configureHeader();
    }

    private void configureHeader() {
        JTableHeader header = getTableHeader();
        header.setReorderingAllowed(false);
        header.setResizingAllowed(true);
        header.setPreferredSize(new Dimension(header.getWidth(), 36));

        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {

                JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                        table, value, false, false, row, col);
                lbl.setBackground(UIConstants.TABLE_HEADER_COLOR);
                lbl.setForeground(Color.WHITE);
                lbl.setFont(UIConstants.FONT_BOLD);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setOpaque(true);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 2, 1, UIConstants.TABLE_HEADER_COLOR.darker()),
                        BorderFactory.createEmptyBorder(4, 8, 4, 8)));
                return lbl;
            }
        });
    }

    // ── Zebra renderer ──────────────────────────────────────────────
    public static class ZebraRenderer extends DefaultTableCellRenderer {
        private final int alignment;

        public ZebraRenderer(int alignment) {
            this.alignment = alignment;
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int col) {

            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            setHorizontalAlignment(alignment);
            setFont(UIConstants.FONT_NORMAL);
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

            if (isSelected) {
                setBackground(UIConstants.TABLE_SELECTION);
                setForeground(UIConstants.TABLE_SELECTION_FG);
            } else {
                setBackground(row % 2 == 0 ? UIConstants.TABLE_ROW_ODD : UIConstants.TABLE_ROW_EVEN);
                setForeground(new Color(44, 62, 80));
            }
            return this;
        }
    }

    /** Convenience: get a center-aligned zebra renderer */
    public static ZebraRenderer centerRenderer() {
        return new ZebraRenderer(SwingConstants.CENTER);
    }
}
