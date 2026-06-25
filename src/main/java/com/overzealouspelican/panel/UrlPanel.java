package com.overzealouspelican.panel;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.List;
import com.overzealouspelican.controller.ApiCallDragDropHandler;
import com.overzealouspelican.controller.SavedCallsListController;
import com.overzealouspelican.controller.SavedCallsListController.GroupedCalls;
import com.overzealouspelican.model.ApiCall;
import com.overzealouspelican.model.ApplicationState;
import com.overzealouspelican.service.ApiCallService;
import com.overzealouspelican.util.UITheme;

/**
 * UI panel for displaying saved API calls with grouping and drag-and-drop.
 * Single responsibility: render the list of saved calls and route interactions to controllers.
 */
public class UrlPanel extends JPanel {

    private final ApplicationState appState;
    private final SavedCallsListController listController;
    private final ApiCallDragDropHandler dragDropHandler;
    private JPanel listPanel;
    private JButton toggleAllButton;
    private CallConfigurationPanel configPanel;

    public UrlPanel() {
        ApiCallService apiCallService = new ApiCallService();
        this.appState = ApplicationState.getInstance();
        this.listController = new SavedCallsListController(apiCallService);
        this.dragDropHandler = new ApiCallDragDropHandler(apiCallService);
        initializePanel();
        setupListeners();
    }

    private void initializePanel() {
        setLayout(new BorderLayout());
        setBackground(UIManager.getColor("Panel.background"));

        add(createToolbar(), BorderLayout.NORTH);

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(UIManager.getColor("Panel.background"));
        listPanel.setBorder(BorderFactory.createEmptyBorder(
            UITheme.SPACING_XS, UITheme.SPACING_XS, UITheme.SPACING_XS, UITheme.SPACING_XS));

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        setPreferredSize(new Dimension(320, 0));

        refreshList();
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Component.borderColor")),
            BorderFactory.createEmptyBorder(UITheme.SPACING_MD, UITheme.SPACING_MD, UITheme.SPACING_MD, UITheme.SPACING_MD)
        ));
        toolbar.setBackground(UIManager.getColor("Panel.background"));

        JLabel titleLabel = new JLabel("Saved Calls");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, UITheme.FONT_SIZE_MD));
        toolbar.add(titleLabel, BorderLayout.WEST);

        toggleAllButton = new JButton("\u229F"); // ⊟
        toggleAllButton.setToolTipText("Collapse all groups");
        toggleAllButton.setFont(toggleAllButton.getFont().deriveFont(14f));
        toggleAllButton.setPreferredSize(new Dimension(28, 24));
        toggleAllButton.setFocusPainted(false);
        toggleAllButton.setContentAreaFilled(false);
        toggleAllButton.setBorderPainted(false);
        toggleAllButton.setMargin(new Insets(0, 0, 0, 0));
        toggleAllButton.addActionListener(e -> handleToggleAll());
        toolbar.add(toggleAllButton, BorderLayout.EAST);

        return toolbar;
    }

    public void setConfigurationPanel(CallConfigurationPanel configPanel) {
        this.configPanel = configPanel;
    }

    private void setupListeners() {
        appState.addPropertyChangeListener("apiCallSaved", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                refreshList();
            }
        });
    }

    // --- Action handlers ---

    private void handleToggleAll() {
        GroupedCalls grouped = listController.loadGroupedCalls();
        boolean anyExpanded = listController.isAnyGroupExpanded(grouped.getGroups().keySet());
        listController.setAllGroupsExpanded(!anyExpanded);
        updateToggleButtonIcon(!anyExpanded);
        refreshList();
    }

    private void handleLoadApiCall(String name) {
        if (name == null || configPanel == null) return;

        ApiCall apiCall = listController.loadApiCall(name);
        if (apiCall != null) {
            configPanel.loadApiCall(apiCall);
            appState.setStatus("Loaded: " + name, "\uD83D\uDCCB");
        }
    }

    private void handleDeleteApiCall(String name) {
        int result = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete '" + name + "'?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (result == JOptionPane.YES_OPTION) {
            try {
                listController.deleteApiCall(name);
                appState.setStatusSuccess("Deleted: " + name);
                refreshList();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Failed to delete API call: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                appState.setStatusError("Failed to delete call");
            }
        }
    }

    // --- List rendering ---

    private void refreshList() {
        listPanel.removeAll();
        GroupedCalls grouped = listController.loadGroupedCalls();

        // Update toggle button
        if (toggleAllButton != null && !grouped.getGroups().isEmpty()) {
            boolean anyExpanded = listController.isAnyGroupExpanded(grouped.getGroups().keySet());
            updateToggleButtonIcon(anyExpanded);
        }

        // Render grouped items
        for (Map.Entry<String, List<String>> group : grouped.getGroups().entrySet()) {
            String groupName = group.getKey();
            List<String> members = group.getValue();
            listPanel.add(createGroupHeader(groupName, members));

            if (listController.isGroupExpanded(groupName)) {
                for (String memberName : members) {
                    listPanel.add(createApiCallItem(memberName, groupName));
                }
            }
        }

        // Render ungrouped items
        for (String name : grouped.getUngrouped()) {
            listPanel.add(createApiCallItem(name, null));
        }

        listPanel.revalidate();
        listPanel.repaint();
    }

    private void updateToggleButtonIcon(boolean anyExpanded) {
        toggleAllButton.setText(anyExpanded ? "\u229F" : "\u229E"); // ⊟ or ⊞
        toggleAllButton.setToolTipText(anyExpanded ? "Collapse all groups" : "Expand all groups");
    }

    private JPanel createGroupHeader(String groupName, List<String> members) {
        JPanel headerPanel = new JPanel(new BorderLayout(6, 0));
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, UITheme.LIST_ITEM_HEIGHT));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(
            UITheme.SPACING_XS, UITheme.SPACING_SM, UITheme.SPACING_XS, UITheme.SPACING_SM));
        headerPanel.setBackground(UIManager.getColor("Panel.background"));
        headerPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        boolean expanded = listController.isGroupExpanded(groupName);

        JLabel iconLabel = new JLabel(expanded ? "\u25BC" : "\u25B6");
        iconLabel.setFont(iconLabel.getFont().deriveFont(UITheme.FONT_SIZE_XS));
        headerPanel.add(iconLabel, BorderLayout.WEST);

        JLabel nameLabel = new JLabel(groupName + " (" + members.size() + ")");
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, UITheme.FONT_SIZE_MD));
        headerPanel.add(nameLabel, BorderLayout.CENTER);

        // Click to expand/collapse
        headerPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                listController.toggleGroup(groupName);
                refreshList();
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                headerPanel.setBackground(UIManager.getColor("List.selectionBackground"));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                headerPanel.setBackground(UIManager.getColor("Panel.background"));
            }
        });

        // Drop target for group header
        new DropTarget(headerPanel, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_MOVE);
                    Transferable transferable = dtde.getTransferable();
                    String draggedName = (String) transferable.getTransferData(DataFlavor.stringFlavor);
                    dragDropHandler.dropOnGroup(draggedName, groupName);
                    dtde.dropComplete(true);
                    refreshList();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    dtde.dropComplete(false);
                }
            }

            @Override
            public void dragOver(DropTargetDragEvent dtde) {
                headerPanel.setBackground(new Color(100, 150, 255, 50));
            }

            @Override
            public void dragExit(DropTargetEvent dte) {
                headerPanel.setBackground(UIManager.getColor("Panel.background"));
            }
        });

        return headerPanel;
    }

    private JPanel createApiCallItem(String name, String groupName) {
        JPanel itemPanel = new JPanel(new BorderLayout(6, 0));
        itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, UITheme.LIST_ITEM_HEIGHT));
        int leftPadding = groupName != null ? UITheme.SPACING_XL : UITheme.SPACING_SM;
        itemPanel.setBorder(BorderFactory.createEmptyBorder(2, leftPadding, 2, UITheme.SPACING_SM));
        itemPanel.setBackground(UIManager.getColor("Panel.background"));
        itemPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Left: method badge + name
        JPanel leftContent = new JPanel();
        leftContent.setLayout(new BoxLayout(leftContent, BoxLayout.X_AXIS));
        leftContent.setOpaque(false);

        ApiCall call = listController.loadApiCall(name);
        if (call != null && call.getHttpMethod() != null) {
            JLabel methodBadge = new JLabel(call.getHttpMethod());
            methodBadge.setFont(methodBadge.getFont().deriveFont(Font.BOLD, 9f));
            methodBadge.setForeground(UITheme.getMethodColor(call.getHttpMethod()));
            methodBadge.setAlignmentY(Component.CENTER_ALIGNMENT);
            leftContent.add(methodBadge);
            leftContent.add(Box.createHorizontalStrut(UITheme.SPACING_SM));
        }

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.PLAIN, UITheme.FONT_SIZE_MD));
        nameLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
        leftContent.add(nameLabel);

        itemPanel.add(leftContent, BorderLayout.CENTER);

        // Delete button
        JButton deleteButton = new JButton("\u26D4");
        deleteButton.setPreferredSize(new Dimension(28, 24));
        deleteButton.setToolTipText("Delete this saved call");
        deleteButton.setFocusPainted(false);
        deleteButton.setContentAreaFilled(false);
        deleteButton.setBorderPainted(false);
        deleteButton.setFont(deleteButton.getFont().deriveFont(12f));
        deleteButton.setMargin(new Insets(0, 0, 0, 0));
        deleteButton.addActionListener(e -> handleDeleteApiCall(name));
        itemPanel.add(deleteButton, BorderLayout.EAST);

        // Click to load
        itemPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                handleLoadApiCall(name);
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                itemPanel.setBackground(UIManager.getColor("List.selectionBackground"));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                itemPanel.setBackground(UIManager.getColor("Panel.background"));
            }
        });

        // Drag source
        DragSource dragSource = new DragSource();
        dragSource.createDefaultDragGestureRecognizer(itemPanel, DnDConstants.ACTION_MOVE,
            dge -> {
                Transferable transferable = new StringSelection(name);
                dragSource.startDrag(dge, DragSource.DefaultMoveDrop, transferable, null);
            }
        );

        // Drop target
        new DropTarget(itemPanel, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_MOVE);
                    Transferable transferable = dtde.getTransferable();
                    String draggedName = (String) transferable.getTransferData(DataFlavor.stringFlavor);

                    if (!draggedName.equals(name)) {
                        if (dragDropHandler.dropOnItem(UrlPanel.this, draggedName, name)) {
                            refreshList();
                        }
                    }
                    dtde.dropComplete(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    dtde.dropComplete(false);
                }
            }

            @Override
            public void dragOver(DropTargetDragEvent dtde) {
                itemPanel.setBackground(new Color(100, 150, 255, 50));
            }

            @Override
            public void dragExit(DropTargetEvent dte) {
                itemPanel.setBackground(UIManager.getColor("Panel.background"));
            }
        });

        return itemPanel;
    }
}
