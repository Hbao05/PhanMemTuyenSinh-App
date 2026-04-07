package gui.component;

import gui.style.UIConstants;

import javax.swing.*;
import java.awt.*;

public class CustomButton extends JButton {
    public CustomButton(String text, Color bgColor) {
        super(text);
        setFont(UIConstants.FONT_BOLD);
        setBackground(bgColor);
        setForeground(Color.WHITE);
        setFocusPainted(false); // Bỏ viền chấm chấm khi click
        setCursor(new Cursor(Cursor.HAND_CURSOR)); // Đổi con trỏ thành hình bàn tay
        setPreferredSize(new Dimension(120, 35));
    }
}
