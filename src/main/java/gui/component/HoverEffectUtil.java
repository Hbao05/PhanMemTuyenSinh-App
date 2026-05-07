package gui.component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class HoverEffectUtil {
    /**
     * Gắn hiệu ứng hover (đổi màu nền khi chuột lướt qua) cho bất kỳ Component nào
     * * @param component     Thành phần cần gắn hiệu ứng (JButton, JPanel, JLabel...)
     * @param defaultColor  Màu nền mặc định
     * @param hoverColor    Màu nền khi chuột lướt qua
     */
    public static void applyHoverEffect(JComponent component, Color defaultColor, Color hoverColor) {
        component.setBackground(defaultColor);

        // Nếu là nút bấm thì thêm con trỏ hình bàn tay
        if (component instanceof JButton || component instanceof JLabel) {
            component.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                component.setBackground(hoverColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                component.setBackground(defaultColor);
            }
        });
    }

    /**
     * Gắn hiệu ứng hover đổi màu chữ (Text Color)
     */
    public static void applyTextHoverEffect(JComponent component, Color defaultColor, Color hoverColor) {
        component.setForeground(defaultColor);

        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                component.setForeground(hoverColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                component.setForeground(defaultColor);
            }
        });
    }
}
