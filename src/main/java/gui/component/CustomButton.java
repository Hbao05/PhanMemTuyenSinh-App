package gui.component;

import gui.style.UIConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class CustomButton extends JButton {

    private Color baseColor;
    private Color hoverColor;
    private Color pressColor;
    private boolean hovered  = false;
    private boolean pressed  = false;

    public CustomButton(String text, Color bgColor) {
        super(text);
        this.baseColor  = bgColor;
        this.hoverColor  = bgColor.darker();
        this.pressColor  = bgColor.darker().darker();

        setFont(UIConstants.FONT_BOLD);
        setForeground(Color.WHITE);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(120, 36));

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
            @Override public void mouseExited (MouseEvent e) { hovered = false; pressed = false; repaint(); }
            @Override public void mousePressed(MouseEvent e) { pressed = true;  repaint(); }
            @Override public void mouseReleased(MouseEvent e){ pressed = false; repaint(); }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        boolean enabled = isEnabled();
        Color fill = !enabled ? baseColor.darker()
                   : pressed  ? pressColor
                   : hovered  ? hoverColor
                   :             baseColor;
        g2.setColor(fill);
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), UIConstants.BUTTON_RADIUS, UIConstants.BUTTON_RADIUS));

        if (!pressed && enabled) {
            g2.setColor(new Color(0, 0, 0, 40));
            g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
        }

        // Always paint white text so L&F disabled-gray doesn't override
        g2.setFont(getFont());
        g2.setColor(enabled ? Color.WHITE : new Color(255, 255, 255, 160));
        FontMetrics fm = g2.getFontMetrics();
        String text = getText();
        int x = (getWidth()  - fm.stringWidth(text)) / 2;
        int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(text, x, y);

        g2.dispose();
    }

    @Override
    protected void paintBorder(Graphics g) { /* no border */ }

    /** Allow changing color at runtime (e.g. toggle state) */
    public void setBaseColor(Color color) {
        this.baseColor   = color;
        this.hoverColor  = color.darker();
        this.pressColor  = color.darker().darker();
        repaint();
    }
}
