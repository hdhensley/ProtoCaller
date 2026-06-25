package com.overzealouspelican.panel;

import javax.swing.*;
import java.awt.*;
import com.overzealouspelican.component.KeyValueInputGroup;
import com.overzealouspelican.component.LabeledTextField;
import com.overzealouspelican.component.UrlWithMethodInput;
import com.overzealouspelican.controller.CallExecutionHandler;
import com.overzealouspelican.controller.CallFormController;
import com.overzealouspelican.dialog.ImportCurlDialog;
import com.overzealouspelican.dialog.ImportHarDialog;
import com.overzealouspelican.model.ApiCall;
import com.overzealouspelican.model.ApplicationState;
import com.overzealouspelican.service.ApiCallService;
import com.overzealouspelican.util.UITheme;

/**
 * UI panel for configuring API calls.
 * Single responsibility: lay out form components and delegate actions to controllers.
 */
public class CallConfigurationPanel extends JPanel {

    private LabeledTextField nameField;
    private JTextArea descriptionArea;
    private UrlWithMethodInput urlInput;
    private KeyValueInputGroup headersGroup;
    private KeyValueInputGroup bodyGroup;

    private final CallExecutionHandler executionHandler;
    private final CallFormController formController;
    private final ImportCurlDialog importCurlDialog;
    private final ImportHarDialog importHarDialog;
    private final ApplicationState appState;

    public CallConfigurationPanel() {
        ApiCallService apiCallService = new ApiCallService();
        this.executionHandler = new CallExecutionHandler(apiCallService);
        this.formController = new CallFormController(apiCallService);
        this.importCurlDialog = new ImportCurlDialog();
        this.importHarDialog = new ImportHarDialog();
        this.appState = ApplicationState.getInstance();
        initializePanel();
    }

    private void initializePanel() {
        setLayout(new BorderLayout());
        setBackground(UIManager.getColor("Panel.background"));

        add(createToolbar(), BorderLayout.NORTH);

        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.setBackground(UIManager.getColor("Panel.background"));
        contentWrapper.setBorder(UITheme.contentPadding());
        contentWrapper.add(createContentPanel(), BorderLayout.CENTER);

        add(contentWrapper, BorderLayout.CENTER);
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(UIManager.getColor("Panel.background"));
        toolbar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Component.borderColor")),
            BorderFactory.createEmptyBorder(UITheme.SPACING_MD, UITheme.SPACING_LG, UITheme.SPACING_MD, UITheme.SPACING_LG)
        ));

        JLabel titleLabel = new JLabel("API Request");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, UITheme.FONT_SIZE_LG));
        toolbar.add(titleLabel, BorderLayout.WEST);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, UITheme.SPACING_SM, 0));
        buttonsPanel.setOpaque(false);

        JButton clearButton = new JButton("Clear");
        clearButton.setToolTipText("Clear all form fields");
        clearButton.addActionListener(e -> handleClear());

        JButton saveButton = new JButton("Save");
        saveButton.setToolTipText("Save this API call");
        saveButton.addActionListener(e -> handleSave());

        JButton callButton = new JButton("Send");
        callButton.setToolTipText("Execute the API call");
        UITheme.stylePrimaryButton(callButton);
        callButton.addActionListener(e -> handleCall());

        buttonsPanel.add(clearButton);
        buttonsPanel.add(saveButton);
        buttonsPanel.add(callButton);

        toolbar.add(buttonsPanel, BorderLayout.EAST);

        return toolbar;
    }

    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel(new BorderLayout(0, UITheme.SPACING_MD));
        contentPanel.setBackground(UIManager.getColor("Panel.background"));

        // Top section: Name + URL (fixed height)
        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setBackground(UIManager.getColor("Panel.background"));

        nameField = new LabeledTextField("Name", "Enter a name for this call");
        topSection.add(nameField);
        topSection.add(Box.createVerticalStrut(UITheme.SPACING_MD));

        urlInput = new UrlWithMethodInput();
        topSection.add(urlInput);

        contentPanel.add(topSection, BorderLayout.NORTH);

        // Description panel (resizable via split pane)
        JPanel descriptionPanel = new JPanel(new BorderLayout(0, UITheme.SPACING_XS));
        descriptionPanel.setBackground(UIManager.getColor("Panel.background"));

        JLabel descLabel = new JLabel("Description");
        descLabel.setFont(descLabel.getFont().deriveFont(Font.PLAIN, UITheme.FONT_SIZE_SM));
        descLabel.setForeground(UIManager.getColor("Label.foreground"));
        descriptionPanel.add(descLabel, BorderLayout.NORTH);

        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setToolTipText("Optional description for this call");
        descriptionArea.setFont(UIManager.getFont("TextField.font"));

        JScrollPane descScrollPane = new JScrollPane(descriptionArea);
        descScrollPane.setMinimumSize(new Dimension(0, 40));
        descriptionPanel.add(descScrollPane, BorderLayout.CENTER);

        // Headers and Body split evenly
        JPanel kvPanel = new JPanel(new GridLayout(2, 1, 0, UITheme.SPACING_MD));
        kvPanel.setBackground(UIManager.getColor("Panel.background"));

        headersGroup = new KeyValueInputGroup("Headers", "+ Add Header", "Remove this header");
        bodyGroup = new KeyValueInputGroup("Body", "+ Add Parameter", "Remove this body parameter");

        kvPanel.add(headersGroup);
        kvPanel.add(bodyGroup);

        // Split pane: description on top, headers/body on bottom
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, descriptionPanel, kvPanel);
        splitPane.setDividerLocation(80);
        splitPane.setDividerSize(6);
        splitPane.setContinuousLayout(true);
        splitPane.setBorder(null);
        splitPane.setBackground(UIManager.getColor("Panel.background"));

        contentPanel.add(splitPane, BorderLayout.CENTER);

        return contentPanel;
    }

    // --- Action handlers (thin delegates) ---

    private void handleCall() {
        ApiCall apiCall = formController.buildApiCall(
            nameField.getText(),
            urlInput.getUrl(),
            urlInput.getHttpMethod(),
            descriptionArea.getText(),
            headersGroup.getKeyValuePairs(),
            bodyGroup.getKeyValuePairs()
        );
        executionHandler.execute(apiCall);
    }

    private void handleSave() {
        ApiCall apiCall = formController.buildApiCall(
            nameField.getText(),
            urlInput.getUrl(),
            urlInput.getHttpMethod(),
            descriptionArea.getText(),
            headersGroup.getKeyValuePairs(),
            bodyGroup.getKeyValuePairs()
        );
        formController.save(this, apiCall);
    }

    private void handleClear() {
        nameField.setText("");
        urlInput.setUrl("");
        urlInput.setHttpMethod("GET");
        descriptionArea.setText("");
        headersGroup.clear();
        bodyGroup.clear();
        formController.clearGroupName();
        appState.setStatus("Ready", "\u2705");
    }

    // --- Public API ---

    /**
     * Load an API call into the form fields.
     */
    public void loadApiCall(ApiCall apiCall) {
        if (apiCall == null) return;

        nameField.setText(apiCall.getName());
        urlInput.setUrl(apiCall.getUrl());
        urlInput.setHttpMethod(apiCall.getHttpMethod());
        descriptionArea.setText(apiCall.getDescription() != null ? apiCall.getDescription() : "");
        headersGroup.setKeyValuePairs(apiCall.getHeaders());
        bodyGroup.setKeyValuePairs(apiCall.getBody());
        formController.setCurrentGroupName(apiCall.getGroupName());
    }

    /**
     * Show the import from cURL dialog.
     */
    public void showImportCurlDialog() {
        importCurlDialog.show(this, this::loadApiCall);
    }

    /**
     * Show the import from HAR dialog.
     */
    public void showImportHarDialog() {
        importHarDialog.show(this, this::loadApiCall);
    }

    // --- Accessors for external consumers ---

    public String getFriendlyName() {
        return nameField.getText();
    }

    public void setFriendlyName(String name) {
        nameField.setText(name);
    }

    public String getUrl() {
        return urlInput.getUrl();
    }

    public void setUrl(String url) {
        urlInput.setUrl(url);
    }

    public String getHttpMethod() {
        return urlInput.getHttpMethod();
    }

    public void setHttpMethod(String httpMethod) {
        urlInput.setHttpMethod(httpMethod);
    }

    public KeyValueInputGroup getHeadersGroup() {
        return headersGroup;
    }

    public KeyValueInputGroup getBodyGroup() {
        return bodyGroup;
    }
}
