package com.overzealouspelican.component;

import javax.swing.*;
import java.awt.*;
import com.overzealouspelican.util.UITheme;

/**
 * Reusable component for URL input with HTTP method selector with IntelliJ-style appearance.
 */
public class UrlWithMethodInput extends JPanel {

    private final JTextField urlField;
    private final JComboBox<String> httpMethodDropdown;
    private final JLabel label;

    public UrlWithMethodInput() {
        this.label = new JLabel("URL");
        this.urlField = new JTextField();

        String[] httpMethods = {"GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS"};
        this.httpMethodDropdown = new JComboBox<>(httpMethods);

        initializePanel();
    }

    private void initializePanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setBackground(UIManager.getColor("Panel.background"));

        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setFont(label.getFont().deriveFont(Font.PLAIN, UITheme.FONT_SIZE_SM));
        label.setForeground(UIManager.getColor("Label.foreground"));

        // Panel for URL field and dropdown on same line
        JPanel urlInputPanel = new JPanel(new BorderLayout(UITheme.SPACING_SM, 0));
        urlInputPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, UITheme.INPUT_HEIGHT));
        urlInputPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        urlInputPanel.setBackground(UIManager.getColor("Panel.background"));

        urlField.setToolTipText("Enter the URL for this call");

        // Style the method dropdown with color coding
        httpMethodDropdown.setPreferredSize(new Dimension(110, UITheme.INPUT_HEIGHT));
        httpMethodDropdown.setToolTipText("Select HTTP method");
        httpMethodDropdown.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value != null && !isSelected) {
                    setForeground(UITheme.getMethodColor(value.toString()));
                }
                setFont(getFont().deriveFont(Font.BOLD, UITheme.FONT_SIZE_MD));
                return this;
            }
        });

        urlInputPanel.add(httpMethodDropdown, BorderLayout.WEST);
        urlInputPanel.add(urlField, BorderLayout.CENTER);

        add(label);
        add(Box.createVerticalStrut(UITheme.SPACING_XS));
        add(urlInputPanel);
    }

    public String getUrl() {
        return urlField.getText().trim();
    }

    public void setUrl(String url) {
        urlField.setText(url);
    }

    public String getHttpMethod() {
        return (String) httpMethodDropdown.getSelectedItem();
    }

    public void setHttpMethod(String httpMethod) {
        httpMethodDropdown.setSelectedItem(httpMethod);
    }
}
