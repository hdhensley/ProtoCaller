package com.overzealouspelican.component;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.overzealouspelican.util.UITheme;

/**
 * Reusable component for key-value input pairs with IntelliJ-style appearance.
 */
public class KeyValueInputGroup extends JPanel {

    private final String groupLabel;
    private final String addButtonText;
    private final String removeTooltip;
    private final List<JTextField> keyFields;
    private final List<JTextField> valueFields;
    private final List<JButton> removeButtons;
    private final JPanel rowsContainer;

    public KeyValueInputGroup(String groupLabel, String addButtonText, String removeTooltip) {
        this.groupLabel = groupLabel;
        this.addButtonText = addButtonText;
        this.removeTooltip = removeTooltip;
        this.keyFields = new ArrayList<>();
        this.valueFields = new ArrayList<>();
        this.removeButtons = new ArrayList<>();
        this.rowsContainer = new JPanel();

        initializePanel();
    }

    private void initializePanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UIManager.getColor("Panel.background"));

        // Top section: label + column headers
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(UIManager.getColor("Panel.background"));

        // Group label
        JLabel label = new JLabel(groupLabel);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setFont(label.getFont().deriveFont(Font.BOLD, UITheme.FONT_SIZE_MD));
        topPanel.add(label);
        topPanel.add(Box.createVerticalStrut(UITheme.SPACING_SM));

        // Column labels
        JPanel columnLabelsPanel = new JPanel(new BorderLayout(8, 0));
        columnLabelsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        columnLabelsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        columnLabelsPanel.setBackground(UIManager.getColor("Panel.background"));

        JLabel keyLabel = new JLabel("Key");
        keyLabel.setFont(keyLabel.getFont().deriveFont(Font.PLAIN, UITheme.FONT_SIZE_SM));
        keyLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        keyLabel.setPreferredSize(new Dimension(180, 20));

        JLabel valueLabel = new JLabel("Value");
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.PLAIN, UITheme.FONT_SIZE_SM));
        valueLabel.setForeground(UIManager.getColor("Label.disabledForeground"));

        JLabel spacer = new JLabel("");
        spacer.setPreferredSize(new Dimension(36, 20));

        columnLabelsPanel.add(keyLabel, BorderLayout.WEST);
        columnLabelsPanel.add(valueLabel, BorderLayout.CENTER);
        columnLabelsPanel.add(spacer, BorderLayout.EAST);

        topPanel.add(columnLabelsPanel);
        topPanel.add(Box.createVerticalStrut(UITheme.SPACING_XS));

        add(topPanel, BorderLayout.NORTH);

        // Center: Rows container with scroll (fills available space)
        rowsContainer.setLayout(new BoxLayout(rowsContainer, BoxLayout.Y_AXIS));
        rowsContainer.setBackground(UIManager.getColor("Panel.background"));
        addRow(); // Start with 1 row

        JScrollPane scrollPane = new JScrollPane(rowsContainer);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor"), 1));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);

        // Bottom: Add Row button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(UITheme.SPACING_SM, 0, 0, 0));
        bottomPanel.setBackground(UIManager.getColor("Panel.background"));

        JButton addRowButton = new JButton(addButtonText);
        addRowButton.setPreferredSize(new Dimension(150, 28));
        addRowButton.addActionListener(e -> {
            addRow();
            rowsContainer.revalidate();
            rowsContainer.repaint();
        });
        bottomPanel.add(addRowButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void addRow() {
        JPanel rowPanel = new JPanel();
        rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.X_AXIS));
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, UITheme.INPUT_HEIGHT));
        rowPanel.setMinimumSize(new Dimension(620, UITheme.INPUT_HEIGHT));
        rowPanel.setBorder(BorderFactory.createEmptyBorder(2, UITheme.SPACING_XS, 2, UITheme.SPACING_XS));
        rowPanel.setBackground(UIManager.getColor("Panel.background"));

        JTextField keyField = new JTextField();
        keyField.setPreferredSize(new Dimension(180, 26));
        keyField.setMinimumSize(new Dimension(180, 26));
        keyField.setMaximumSize(new Dimension(180, 26));

        JTextField valueField = new JTextField();
        valueField.setPreferredSize(new Dimension(400, 26));
        valueField.setMinimumSize(new Dimension(400, 26));
        valueField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

        JButton removeButton = new JButton("×");
        removeButton.setPreferredSize(new Dimension(36, 26));
        removeButton.setMinimumSize(new Dimension(36, 26));
        removeButton.setMaximumSize(new Dimension(36, 26));
        removeButton.setToolTipText(removeTooltip);
        removeButton.setFont(removeButton.getFont().deriveFont(UITheme.FONT_SIZE_TITLE));
        removeButton.setMargin(new Insets(0, 0, 0, 0));

        keyFields.add(keyField);
        valueFields.add(valueField);
        removeButtons.add(removeButton);

        removeButton.addActionListener(e -> {
            int index = removeButtons.indexOf(removeButton);
            if (index >= 0) {
                keyFields.remove(index);
                valueFields.remove(index);
                removeButtons.remove(index);
            }

            Component[] components = rowsContainer.getComponents();
            for (int i = 0; i < components.length; i++) {
                if (components[i] == rowPanel) {
                    rowsContainer.remove(i);
                    break;
                }
            }

            rowsContainer.revalidate();
            rowsContainer.repaint();
        });

        rowPanel.add(keyField);
        rowPanel.add(Box.createHorizontalStrut(4));
        rowPanel.add(valueField);
        rowPanel.add(Box.createHorizontalStrut(4));
        rowPanel.add(removeButton);

        rowsContainer.add(rowPanel);
    }

    public Map<String, String> getKeyValuePairs() {
        Map<String, String> pairs = new HashMap<>();
        for (int i = 0; i < keyFields.size(); i++) {
            String key = keyFields.get(i).getText().trim();
            String value = valueFields.get(i).getText().trim();

            if (!key.isEmpty() && !value.isEmpty()) {
                pairs.put(key, value);
            }
        }
        return pairs;
    }

    public void setKeyValuePairs(Map<String, String> pairs) {
        rowsContainer.removeAll();
        keyFields.clear();
        valueFields.clear();
        removeButtons.clear();

        if (pairs.isEmpty()) {
            addRow();
        } else {
            for (Map.Entry<String, String> entry : pairs.entrySet()) {
                addRow();
                int lastIndex = keyFields.size() - 1;
                keyFields.get(lastIndex).setText(entry.getKey());
                valueFields.get(lastIndex).setText(entry.getValue());
            }
        }

        rowsContainer.revalidate();
        rowsContainer.repaint();
    }

    public void clear() {
        rowsContainer.removeAll();
        keyFields.clear();
        valueFields.clear();
        removeButtons.clear();
        addRow();
        rowsContainer.revalidate();
        rowsContainer.repaint();
    }
}
