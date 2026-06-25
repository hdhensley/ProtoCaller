package com.overzealouspelican.util;

import javax.swing.*;
import java.awt.*;

/**
 * Utility for styling a save button based on dirty/clean state.
 * Single responsibility: apply visual styling to indicate whether there are unsaved changes.
 */
public class SaveButtonStyler {

    /**
     * Style the button to indicate unsaved changes (warning appearance).
     */
    public static void styleAsChanged(JButton button) {
        button.setEnabled(true);

        Color warningColor = UIManager.getColor("Actions.Yellow");
        if (warningColor == null) {
            warningColor = UIManager.getColor("Component.warningBorderColor");
        }
        if (warningColor == null) {
            warningColor = new Color(255, 193, 7);
        }

        button.setBackground(warningColor);
        button.setForeground(Color.BLACK);
        button.putClientProperty("JButton.buttonType", null);
        button.setBorder(BorderFactory.createLineBorder(warningColor.darker(), 1));
        button.setToolTipText("Save changes to environment");
        button.putClientProperty("JComponent.opacity", 1.0f);

        forceRepaint(button);
    }

    /**
     * Style the button to indicate no unsaved changes (success/saved appearance).
     */
    public static void styleAsSaved(JButton button) {
        button.setEnabled(true);

        Color successColor = UIManager.getColor("Actions.Green");
        if (successColor == null) {
            successColor = UIManager.getColor("Component.focusColor");
        }
        if (successColor == null) {
            successColor = new Color(40, 167, 69);
        }

        button.setBackground(successColor);
        button.setForeground(Color.WHITE);
        button.putClientProperty("JButton.buttonType", null);
        button.setBorder(BorderFactory.createLineBorder(successColor.darker(), 1));
        button.setToolTipText("No changes to save");
        button.putClientProperty("JComponent.opacity", 0.7f);

        forceRepaint(button);
    }

    private static void forceRepaint(JButton button) {
        button.invalidate();
        button.revalidate();
        button.repaint();
        if (button.getParent() != null) {
            button.getParent().repaint();
        }
    }
}
