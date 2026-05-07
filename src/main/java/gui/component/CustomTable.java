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
        setRowHeight(30);
        setSelectionBackground(new Color(189, 195, 199));
        setGridColor(new Color(230, 230, 230));
        setShowGrid(true);

        JTableHeader header = getTableHeader();
        header.setReorderingAllowed(false);
        header.setResizingAllowed(true);

        // Đặt custom renderer để OVERRIDE hoàn toàn Windows Look & Feel
        // (setBackground/setForeground bị L&F bỏ qua khi dùng SystemLookAndFeel)
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value,
                    boolean isSelected, boolean hasFocus,
                    int row, int column) {

                JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                        table, value, false, false, row, column);

                lbl.setBackground(UIConstants.TABLE_HEADER_COLOR);
                lbl.setForeground(Color.WHITE);
                lbl.setFont(UIConstants.FONT_BOLD);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setOpaque(true);

                // Viền mỏng giữa các cột
                lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 2, 1,
                                UIConstants.TABLE_HEADER_COLOR.darker()),
                        BorderFactory.createEmptyBorder(4, 6, 4, 6)
                ));

                return lbl;
            }
        });
    }
}
