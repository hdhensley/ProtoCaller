package com.overzealouspelican.panel;

import javax.swing.*;
import java.awt.*;
import com.overzealouspelican.util.IconUtils;
import com.overzealouspelican.util.UITheme;

/**
 * IntelliJ-style modern toolbar with icon buttons.
 */
public class ToolbarPanel extends JPanel {

    private JButton toggleSidebarButton;
    private JLabel environmentLabel;

    public ToolbarPanel(Runnable toggleSidebarAction) {
        initializePanel(toggleSidebarAction);
    }

    private void initializePanel(Runnable toggleSidebarAction) {
        setLayout(new BorderLayout());
        setBackground(UIManager.getColor("Panel.background"));
        setPreferredSize(new Dimension(0, UITheme.TOOLBAR_HEIGHT));

        // Left side - toggle sidebar button
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, UITheme.SPACING_SM, 6));
        leftPanel.setOpaque(false);

        toggleSidebarButton = new JButton("☰");
        toggleSidebarButton.setToolTipText("Toggle Sidebar (⌘1)");
        toggleSidebarButton.setFont(toggleSidebarButton.getFont().deriveFont(UITheme.FONT_SIZE_TITLE));
        toggleSidebarButton.setFocusPainted(false);
        toggleSidebarButton.setPreferredSize(new Dimension(40, UITheme.BUTTON_HEIGHT));
        toggleSidebarButton.addActionListener(e -> toggleSidebarAction.run());
        leftPanel.add(toggleSidebarButton);

        // Environment indicator
        environmentLabel = new JLabel("");
        environmentLabel.setFont(environmentLabel.getFont().deriveFont(Font.PLAIN, UITheme.FONT_SIZE_SM));
        environmentLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        leftPanel.add(environmentLabel);

        add(leftPanel, BorderLayout.WEST);

        // Add bottom border
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,
            UIManager.getColor("Component.borderColor")));
    }

    public void setEnvironmentLabel(String environment) {
        if (environment != null && !environment.isEmpty()) {
            environmentLabel.setText("Environment: " + environment);
        } else {
            environmentLabel.setText("");
        }
    }
}

