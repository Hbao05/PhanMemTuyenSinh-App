package gui.style;

import java.awt.*;

public class UIConstants {
    // ── Primary palette ──────────────────────────────────────────────
    public static final Color PRIMARY_COLOR   = new Color(41, 128, 185);   // Blue
    public static final Color PRIMARY_DARK    = new Color(31,  97, 141);   // Blue darker (hover)
    public static final Color PRIMARY_LIGHT   = new Color(214, 234, 248);  // Blue tint

    public static final Color SUCCESS_COLOR   = new Color(39, 174, 96);
    public static final Color SUCCESS_DARK    = new Color(30, 132, 73);

    public static final Color DANGER_COLOR    = new Color(231, 76, 60);
    public static final Color DANGER_DARK     = new Color(176, 58, 46);

    public static final Color WARNING_COLOR   = new Color(243, 156, 18);
    public static final Color WARNING_DARK    = new Color(185, 119, 14);

    public static final Color PURPLE_COLOR    = new Color(142, 68, 173);
    public static final Color PURPLE_DARK     = new Color(108, 52, 131);

    public static final Color TEAL_COLOR      = new Color(22, 160, 133);
    public static final Color TEAL_DARK       = new Color(17, 122, 101);

    public static final Color GRAY_COLOR      = new Color(127, 140, 141);
    public static final Color GRAY_DARK       = new Color(96, 107, 107);

    // ── Background / Surface ─────────────────────────────────────────
    public static final Color BACKGROUND_COLOR = new Color(242, 245, 250);
    public static final Color SURFACE_COLOR    = Color.WHITE;

    // ── Sidebar ──────────────────────────────────────────────────────
    public static final Color SIDEBAR_BG      = new Color(26,  32,  44);   // Very dark navy
    public static final Color SIDEBAR_HOVER   = new Color(45,  55,  72);   // Slightly lighter
    public static final Color SIDEBAR_ACTIVE  = new Color(41, 128, 185);   // Same as primary
    public static final Color SIDEBAR_TEXT    = new Color(160, 174, 192);  // Muted white
    public static final Color SIDEBAR_TEXT_ACTIVE = Color.WHITE;

    // ── Table ────────────────────────────────────────────────────────
    public static final Color TABLE_HEADER_COLOR = new Color(44, 62, 80);
    public static final Color TABLE_ROW_ODD      = Color.WHITE;
    public static final Color TABLE_ROW_EVEN     = new Color(245, 248, 252);
    public static final Color TABLE_SELECTION    = new Color(214, 234, 248);
    public static final Color TABLE_SELECTION_FG = new Color(31, 97, 141);
    public static final Color TABLE_GRID_COLOR   = new Color(220, 225, 232);

    // ── Border ───────────────────────────────────────────────────────
    public static final Color BORDER_COLOR    = new Color(213, 219, 229);

    // ── Typography ───────────────────────────────────────────────────
    public static final Font FONT_SMALL  = new Font("Segoe UI", Font.PLAIN,  12);
    public static final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN,  14);
    public static final Font FONT_BOLD   = new Font("Segoe UI", Font.BOLD,   14);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD,   16);
    public static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD,   20);

    // ── Shape ────────────────────────────────────────────────────────
    public static final int BUTTON_RADIUS = 8;
}
