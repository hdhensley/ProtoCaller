package com.overzealouspelican.dialog;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;
import com.overzealouspelican.model.ApiCall;
import com.overzealouspelican.model.ApplicationState;
import com.overzealouspelican.util.CurlParser;

/**
 * Dialog for importing an API call from a cURL command.
 * Single responsibility: present cURL input UI and parse to ApiCall.
 */
public class ImportCurlDialog {

    private final ApplicationState appState;

    public ImportCurlDialog() {
        this.appState = ApplicationState.getInstance();
    }

    /**
     * Show the import dialog.
     *
     * @param parent the parent component for dialog positioning
     * @param onImport callback invoked with the parsed ApiCall on success
     */
    public void show(Component parent, Consumer<ApiCall> onImport) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent), "Import from cURL", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(700, 400);
        dialog.setLocationRelativeTo(parent);

        // Instructions
        JLabel instructions = new JLabel("<html>Paste your cURL command below:</html>");
        instructions.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        dialog.add(instructions, BorderLayout.NORTH);

        // Text area for cURL input
        JTextArea curlInput = new JTextArea();
        curlInput.setLineWrap(true);
        curlInput.setWrapStyleWord(true);
        curlInput.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(curlInput);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        dialog.add(scrollPane, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dialog.dispose());

        JButton importButton = new JButton("Import");
        importButton.addActionListener(e -> {
            String curlCommand = curlInput.getText().trim();
            if (curlCommand.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                    "Please paste a cURL command.",
                    "Empty Input",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                ApiCall apiCall = CurlParser.parseCurl(curlCommand);
                String suggestedName = CurlParser.generateName(apiCall.getUrl());
                apiCall.setName(suggestedName);

                dialog.dispose();
                onImport.accept(apiCall);
                appState.setStatusSuccess("cURL command imported successfully");

                JOptionPane.showMessageDialog(parent,
                    "API call imported successfully!\nYou can now edit and save it.",
                    "Import Successful",
                    JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                    "Failed to parse cURL command:\n" + ex.getMessage(),
                    "Parse Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonsPanel.add(cancelButton);
        buttonsPanel.add(importButton);
        dialog.add(buttonsPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
}
