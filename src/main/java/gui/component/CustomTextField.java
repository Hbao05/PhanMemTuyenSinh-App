package gui.component;

import gui.style.UIConstants;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class CustomTextField extends JTextField {
    public CustomTextField(int columns) {
        super(columns);
        setFont(UIConstants.FONT_NORMAL);
        setForeground(Color.DARK_GRAY);
        setCaretColor(UIConstants.PRIMARY_COLOR); // Màu con trỏ nhấp nháy

        // Tạo Padding (Khoảng cách từ chữ tới viền: Trên 5, Trái 10, Dưới 5, Phải 10)
        Border emptyBorder = new EmptyBorder(5, 10, 5, 10);

        // Viền mặc định màu xám nhạt
        Border defaultBorder = new LineBorder(Color.LIGHT_GRAY, 1, true);
        setBorder(new CompoundBorder(defaultBorder, emptyBorder));

        // Hiệu ứng Focus: Sáng viền xanh khi click vào để gõ chữ
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                Border activeBorder = new LineBorder(UIConstants.PRIMARY_COLOR, 2, true);
                setBorder(new CompoundBorder(activeBorder, emptyBorder));
            }

            @Override
            public void focusLost(FocusEvent e) {
                setBorder(new CompoundBorder(defaultBorder, emptyBorder));
            }
        });
    }
}
