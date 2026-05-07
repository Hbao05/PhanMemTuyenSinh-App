package gui.component;

import gui.style.UIConstants;

import javax.swing.*;
import java.awt.*;

public class CustomComboBox<E> extends JComboBox {
    public CustomComboBox(E[] items) {
        super(items);
        setFont(UIConstants.FONT_NORMAL);
        setBackground(Color.WHITE);
        setForeground(Color.DARK_GRAY);
        setFocusable(false); // Bỏ cái viền chấm chấm bao quanh chữ khi click
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public CustomComboBox() {
        this(null);
    }
}
