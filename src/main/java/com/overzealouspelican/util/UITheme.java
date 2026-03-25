package com.overzealouspelican.util;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * Centralized modern theme constants and utilities.
 * Provides consistent styling across the application.
 */
public class UITheme {

    // Spacing
    public static final int SPACING_XS = 4;
    public static final int SPACING_SM = 8;
    public static final int SPACING_MD = 12;
    public static final int SPACING_LG = 16;
    public static final int SPACING_XL = 24;

    // Corner radius
    public static final int ARC = 8;
    public static final int ARC_LARGE = 12;

    // Font sizes
    public static final float FONT_SIZE_XS = 10f;
    public static final float FONT_SIZE_SM = 11f;
    public static final float FONT_SIZE_MD = 12f;
    public static final float FONT_SIZE_LG = 13f;
    public static final float FONT_SIZE_XL = 14f;
    public static final float FONT_SIZE_TITLE = 16f;

    // Component heights
    public static final int INPUT_HEIGHT = 32;
    public static final int BUTTON_HEIGHT = 32;
    public static final int TOOLBAR_HEIGHT = 44;
    public static final int STATUS_BAR_HEIGHT = 28;
    public static final int LIST_ITEM_HEIGHT = 36;

    // Accent color for primary actions
    public static final Color ACCENT = new Color(47, 128, 237);
    public static final Color ACCENT_HOVER = new Color(35, 110, 215);
    public static final Color ACCENT_FOREGROUND = Color.WHITE;

    // HTTP method colors
    public static final Color HTTP_GET = new Color(97, 175, 239);
    public static final Color HTTP_POST = new Color(152, 195, 121);
    public static final Color HTTP_PUT = new Color(229, 192, 123);
    public static final Color HTTP_DELETE = new Color(224, 108, 117);
    public static final Color HTTP_PATCH = new Color(198, 120, 221);
    public static final Color HTTP_DEFAULT = new Color(171, 178, 191);

    /**
     * Apply global FlatLaf UI defaults for a modern feel.
     * Call this BEFORE creating any Swing components.
     */
    public static void applyGlobalDefaults() {
        // Rounded corners everywhere
        UIManager.put("Button.arc", ARC);
        UIManager.put("Component.arc", ARC);
        UIManager.put("TextComponent.arc", ARC);
        UIManager.put("CheckBox.arc", ARC);
        UIManager.put("ProgressBar.arc", ARC);
        UIManager.put("ScrollBar.thumbArc", ARC);
        UIManager.put("ScrollBar.trackArc", ARC);

        // Focus ring
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Component.innerFocusWidth", 0);
        UIManager.put("Component.focusColor", ACCENT);

        // Button defaults
        UIManager.put("Button.margin", new Insets(4, 14, 4, 14));

        // Scroll bar
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
        UIManager.put("ScrollBar.showButtons", false);

        // Split pane
        UIManager.put("SplitPane.dividerSize", 4);
        UIManager.put("SplitPaneDivider.gripDotCount", 0);

        // Tabbed pane
        UIManager.put("TabbedPane.selectedBackground", UIManager.getColor("Panel.background"));
        UIManager.put("TabbedPane.tabHeight", 36);
        UIManager.put("TabbedPane.tabSelectionArc", ARC);
        UIManager.put("TabbedPane.cardTabSelectionHeight", 3);
        UIManager.put("TabbedPane.tabInsets", new Insets(4, 16, 4, 16));

        // Menu bar
        UIManager.put("MenuBar.borderColor", UIManager.getColor("Component.borderColor"));
        UIManager.put("PopupMenu.arc", ARC);

        // Table / List
        UIManager.put("List.selectionArc", ARC);
        UIManager.put("Table.selectionArc", ARC);

        // Tooltip
        UIManager.put("ToolTip.background", UIManager.getColor("Panel.background"));
        UIManager.put("ToolTip.arc", ARC);

        // ComboBox
        UIManager.put("ComboBox.padding", new Insets(4, 8, 4, 8));
    }

    /**
     * Get a method color for the given HTTP method string.
     */
    public static Color getMethodColor(String method) {
        if (method == null) return HTTP_DEFAULT;
        return switch (method.toUpperCase()) {
            case "GET" -> HTTP_GET;
            case "POST" -> HTTP_POST;
            case "PUT" -> HTTP_PUT;
            case "DELETE" -> HTTP_DELETE;
            case "PATCH" -> HTTP_PATCH;
            case "HEAD", "OPTIONS" -> HTTP_DEFAULT;
            default -> HTTP_DEFAULT;
        };
    }

    /**
     * Create a section header label with consistent styling.
     */
    public static JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD, FONT_SIZE_LG));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    /**
     * Create consistent padding border.
     */
    public static Border contentPadding() {
        return BorderFactory.createEmptyBorder(SPACING_LG, SPACING_LG, SPACING_LG, SPACING_LG);
    }

    /**
     * Style a button as a primary (accent) action.
     */
    public static void stylePrimaryButton(JButton button) {
        button.setBackground(ACCENT);
        button.setForeground(ACCENT_FOREGROUND);
        button.setFocusPainted(false);
        button.putClientProperty("JButton.buttonType", "roundRect");
    }

    /**
     * Create a rounded panel with a subtle card-like background.
     */
    public static JPanel createCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARC_LARGE, ARC_LARGE);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBackground(UIManager.getColor("Panel.background"));
        return card;
    }
}
