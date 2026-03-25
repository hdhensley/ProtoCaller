package com.overzealouspelican.component;

import javax.swing.*;
import java.awt.*;
import com.overzealouspelican.util.UITheme;

/**
 * Reusable component for a labeled text input field with IntelliJ-style appearance.
 */
public class LabeledTextField extends JPanel {

    private final JTextField textField;
    private final JLabel label;

    public LabeledTextField(String labelText, String tooltipText) {
        this.label = new JLabel(labelText);
        this.textField = new JTextField();

        initializePanel(tooltipText);
    }

    private void initializePanel(String tooltipText) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setBackground(UIManager.getColor("Panel.background"));

        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setFont(label.getFont().deriveFont(Font.PLAIN, UITheme.FONT_SIZE_SM));
        label.setForeground(UIManager.getColor("Label.foreground"));

        textField.setToolTipText(tooltipText);
        textField.setMaximumSize(new Dimension(Integer.MAX_VALUE, UITheme.INPUT_HEIGHT));
        textField.setPreferredSize(new Dimension(0, UITheme.INPUT_HEIGHT));
        textField.setAlignmentX(Component.LEFT_ALIGNMENT);

        add(label);
        add(Box.createVerticalStrut(UITheme.SPACING_XS));
        add(textField);
    }

    public String getText() {
        return textField.getText().trim();
    }

    public void setText(String text) {
        textField.setText(text);
    }

    public JTextField getTextField() {
        return textField;
    }
}
