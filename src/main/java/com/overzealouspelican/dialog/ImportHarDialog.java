package com.overzealouspelican.dialog;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;
import com.overzealouspelican.model.ApiCall;
import com.overzealouspelican.model.ApplicationState;
import com.overzealouspelican.util.HarParser;

/**
 * Dialog for importing API calls from a HAR file.
 * Single responsibility: present HAR file selection UI, parse, and return ApiCall.
 */
public class ImportHarDialog {

    private final ApplicationState appState;

    public ImportHarDialog() {
        this.appState = ApplicationState.getInstance();
    }

    /**
     * Show the HAR import file chooser and selection dialog.
     *
     * @param parent the parent component for dialog positioning
     * @param onImport callback invoked with the selected ApiCall on success
     */
    public void show(Component parent, Consumer<ApiCall> onImport) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select HAR File");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(java.io.File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".har");
            }

            @Override
            public String getDescription() {
                return "HAR Files (*.har)";
            }
        });

        int result = fileChooser.showOpenDialog(parent);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        java.io.File selectedFile = fileChooser.getSelectedFile();

        try {
            String harContent = new String(java.nio.file.Files.readAllBytes(selectedFile.toPath()));
            List<ApiCall> apiCalls = HarParser.parseHar(harContent);

            if (apiCalls.isEmpty()) {
                JOptionPane.showMessageDialog(parent,
                    "No API calls found in the HAR file.",
                    "No Data",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (apiCalls.size() == 1) {
                onImport.accept(apiCalls.get(0));
                appState.setStatusSuccess("HAR file imported successfully");
                JOptionPane.showMessageDialog(parent,
                    "API call imported successfully!\nYou can now edit and save it.",
                    "Import Successful",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                showSelectionDialog(parent, apiCalls, onImport);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent,
                "Failed to import HAR file:\n" + ex.getMessage(),
                "Import Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showSelectionDialog(Component parent, List<ApiCall> apiCalls, Consumer<ApiCall> onImport) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent), "Select API Call", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(800, 500);
        dialog.setLocationRelativeTo(parent);

        JLabel instructions = new JLabel("<html><b>Multiple API calls found.</b> Select one to import:</html>");
        instructions.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        dialog.add(instructions, BorderLayout.NORTH);

        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (ApiCall apiCall : apiCalls) {
            String displayText = String.format("%s %s - %s",
                apiCall.getHttpMethod(),
                apiCall.getName(),
                apiCall.getUrl());
            listModel.addElement(displayText);
        }

        JList<String> apiCallList = new JList<>(listModel);
        apiCallList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        apiCallList.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));

        JScrollPane scrollPane = new JScrollPane(apiCallList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        dialog.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dialog.dispose());

        JButton importButton = new JButton("Import Selected");
        importButton.addActionListener(e -> {
            int selectedIndex = apiCallList.getSelectedIndex();
            if (selectedIndex == -1) {
                JOptionPane.showMessageDialog(dialog,
                    "Please select an API call to import.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            ApiCall selectedCall = apiCalls.get(selectedIndex);
            dialog.dispose();
            onImport.accept(selectedCall);

            appState.setStatusSuccess("API call imported from HAR");
            JOptionPane.showMessageDialog(parent,
                "API call imported successfully!\nYou can now edit and save it.",
                "Import Successful",
                JOptionPane.INFORMATION_MESSAGE);
        });

        buttonsPanel.add(cancelButton);
        buttonsPanel.add(importButton);
        dialog.add(buttonsPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
}
