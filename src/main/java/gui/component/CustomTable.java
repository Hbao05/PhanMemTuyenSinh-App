package gui.component;

import gui.style.UIConstants;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class CustomTable extends JTable {
    public CustomTable(DefaultTableModel model) {
        super(model);
        setFont(UIConstants.FONT_NORMAL);
        setRowHeight(30); // Tăng chiều cao dòng cho dễ nhìn
        setSelectionBackground(new Color(189, 195, 199)); // Màu khi chọn 1 dòng

        // Custom tiêu đề bảng (Header)
        JTableHeader header = getTableHeader();
        header.setFont(UIConstants.FONT_BOLD);
        header.setBackground(UIConstants.TABLE_HEADER_COLOR);
        header.setForeground(Color.WHITE);
        header.setReorderingAllowed(false); // Không cho kéo thả đổi chỗ cột
    }
}
