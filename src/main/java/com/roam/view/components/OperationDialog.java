package com.roam.view.components;

import com.roam.model.Operation;
import com.roam.model.OperationStatus;
import com.roam.model.Priority;
import com.roam.model.Region;
import com.roam.util.ThemeManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.StageStyle;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDate;
import java.util.List;

/**
 * A dialog component for creating and editing operations.
 * <p>
 * This dialog provides a form for operation management, including fields for
 * name, purpose, due date, status, priority, region, and outcome.
 * Supports both create and edit modes with validation.
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public class OperationDialog extends Dialog<Operation> {

    private final TextField nameField;
    private final TextArea purposeField;
    private final DatePicker dueDatePicker;
    private final ComboBox<OperationStatus> statusCombo;
    private final ComboBox<Priority> priorityCombo;
    private final ComboBox<Region> regionCombo;
    private final TextArea outcomeField;
    private final Label errorLabel;

    private final Operation operation;
    private final boolean isEditMode;
    private Button createButton;
    private Button cancelButton;

    public OperationDialog(Operation operation, List<Region> regions) {
        this.operation = operation;
        this.isEditMode = operation != null;

        // Set transparent stage style for drop shadow effect
        initStyle(StageStyle.TRANSPARENT);
        setTitle(isEditMode ? "Edit Operation" : "Create New Operation");

        // Create form fields
        nameField = createTextField("Enter operation name", 255);
        purposeField = createTextArea("Describe the purpose of this operation", 1000);
        dueDatePicker = createDatePicker();
        statusCombo = createStatusComboBox();
        priorityCombo = createPriorityComboBox();
        regionCombo = createRegionComboBox(regions);
        outcomeField = createTextArea("Enter operation outcome (optional)", 1000);
        errorLabel = createErrorLabel();

        // Setup dialog with drop shadow
        setupDialogWithShadow();

        // Pre-fill data if editing
        if (isEditMode) {
            populateFields();
        }
    }

    private Operation resultOperation = null;

    private void setupDialogWithShadow() {
        boolean isDark = ThemeManager.getInstance().isDarkTheme();
        String bgColor = isDark ? "#2d2d2d" : "#ffffff";
        String textColor = isDark ? "#ffffff" : "#374151";
        String borderColor = isDark ? "#404040" : "#E5E7EB";
        String secondaryTextColor = isDark ? "#9ca3af" : "#6B7280";

        // Create form layout
        VBox formLayout = createFormLayout();
        formLayout.setStyle("-fx-background-color: " + bgColor + ";");

        // Wrap in ScrollPane
        ScrollPane scrollPane = new ScrollPane(formLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setMaxHeight(450);
        scrollPane.setPrefHeight(400);

        // Create dialog header with title bar
        HBox titleBar = new HBox(12);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(16, 20, 16, 20));
        titleBar.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 16 16 0 0;");

        FontIcon titleIcon = new FontIcon(isEditMode ? Feather.EDIT_3 : Feather.PLUS_CIRCLE);
        titleIcon.setIconSize(18);
        titleIcon.setIconColor(Color.web("#4285f4"));

        Label titleLabel = new Label(isEditMode ? "Edit Operation" : "Create New Operation");
        titleLabel.setFont(Font.font("Poppins SemiBold", 14));
        titleLabel.setStyle("-fx-text-fill: " + textColor + ";");

        javafx.scene.layout.Region titleSpacer = new javafx.scene.layout.Region();
        HBox.setHgrow(titleSpacer, javafx.scene.layout.Priority.ALWAYS);

        Button closeBtn = new Button();
        FontIcon closeIcon = new FontIcon(Feather.X);
        closeIcon.setIconSize(16);
        closeIcon.setIconColor(Color.web(secondaryTextColor));
        closeBtn.setGraphic(closeIcon);
        closeBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> {
            resultOperation = null;
            hide();
        });

        titleBar.getChildren().addAll(titleIcon, titleLabel, titleSpacer, closeBtn);

        // Create button bar
        HBox buttonBar = new HBox(12);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        buttonBar.setPadding(new Insets(16, 24, 20, 24));
        buttonBar.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 0 0 16 16;");

        cancelButton = new Button("Cancel");
        cancelButton.setPrefWidth(100);
        cancelButton.setPrefHeight(40);
        String cancelBtnBg = isDark ? "#3d3d3d" : "#f5f5f5";
        cancelButton.setStyle(
                "-fx-background-color: " + cancelBtnBg + "; -fx-text-fill: " + textColor + "; " +
                        "-fx-font-family: 'Poppins'; -fx-font-size: 14px; -fx-font-weight: bold; " +
                        "-fx-background-radius: 8; -fx-cursor: hand;");
        cancelButton.setOnAction(e -> {
            resultOperation = null;
            hide();
        });

        createButton = new Button(isEditMode ? "Update" : "Create");
        createButton.setPrefWidth(100);
        createButton.setPrefHeight(40);
        createButton.setStyle(
                "-fx-background-color: #4285f4; -fx-text-fill: white; " +
                        "-fx-font-family: 'Poppins'; -fx-font-size: 14px; -fx-font-weight: bold; " +
                        "-fx-background-radius: 8; -fx-cursor: hand;");
        createButton.setDisable(true);
        createButton.setOnAction(e -> {
            Operation result = validateAndCreateOperation();
            if (result != null) {
                resultOperation = result;
                hide();
            }
        });

        // Enable button when name is not empty
        nameField.textProperty().addListener((obs, old, newVal) -> {
            createButton.setDisable(newVal == null || newVal.trim().isEmpty());
            if (newVal != null && !newVal.trim().isEmpty()) {
                errorLabel.setVisible(false);
            }
        });

        // Enable submit button if editing with pre-filled name
        if (isEditMode && operation.getName() != null && !operation.getName().trim().isEmpty()) {
            createButton.setDisable(false);
        }

        buttonBar.getChildren().addAll(cancelButton, createButton);

        // Main dialog content
        VBox dialogContent = new VBox();
        dialogContent.setStyle("-fx-background-color: " + bgColor + ";");
        dialogContent.getChildren().add(scrollPane);

        // Main container with shadow
        VBox mainContainer = new VBox();
        mainContainer.setStyle(
                "-fx-background-color: " + bgColor + "; -fx-background-radius: 16; " +
                        "-fx-border-color: " + borderColor + "; -fx-border-width: 1; -fx-border-radius: 16;");
        mainContainer.setEffect(new DropShadow(30, 0, 8, Color.rgb(0, 0, 0, 0.15)));
        mainContainer.setPrefWidth(500);
        mainContainer.setMaxWidth(500);
        mainContainer.getChildren().addAll(titleBar, dialogContent, buttonBar);

        // Wrap in StackPane with padding to allow shadow to render
        StackPane shadowContainer = new StackPane(mainContainer);
        shadowContainer.setPadding(new Insets(40));
        shadowContainer.setStyle("-fx-background-color: transparent;");

        getDialogPane().setContent(shadowContainer);
        getDialogPane().setStyle("-fx-background-color: transparent; -fx-padding: 0;");

        // Make dialog pane background transparent
        getDialogPane().getScene().setFill(Color.TRANSPARENT);

        // Hide default buttons but add one to satisfy Dialog requirements
        getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        getDialogPane().lookupButton(ButtonType.CANCEL).setVisible(false);
        getDialogPane().lookupButton(ButtonType.CANCEL).setManaged(false);

        // Set result converter to return the result operation
        setResultConverter(dialogButton -> resultOperation);
    }

    private TextField createTextField(String prompt, int maxLength) {
        boolean isDark = ThemeManager.getInstance().isDarkTheme();
        String inputBg = isDark ? "#3d3d3d" : "#f8f9fa";
        String inputBorder = isDark ? "#4d4d4d" : "#e9ecef";
        String textColor = isDark ? "#ffffff" : "#212529";

        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setPrefHeight(42);
        field.setFont(Font.font("Poppins", 14));
        field.setStyle(
                "-fx-border-color: " + inputBorder + "; -fx-border-radius: 8; -fx-background-radius: 8; " +
                        "-fx-text-fill: " + textColor + "; -fx-background-color: " + inputBg + ";");

        // Limit character count
        field.textProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && newVal.length() > maxLength) {
                field.setText(old);
            }
        });

        return field;
    }

    private TextArea createTextArea(String prompt, int maxLength) {
        boolean isDark = ThemeManager.getInstance().isDarkTheme();
        String inputBg = isDark ? "#3d3d3d" : "#f8f9fa";
        String inputBorder = isDark ? "#4d4d4d" : "#e9ecef";
        String textColor = isDark ? "#ffffff" : "#212529";

        TextArea area = new TextArea();
        area.setPromptText(prompt);
        area.setPrefHeight(80);
        area.setWrapText(true);
        area.setFont(Font.font("Poppins", 14));
        area.setStyle(
                "-fx-border-color: " + inputBorder + "; -fx-border-radius: 8; -fx-background-radius: 8; " +
                        "-fx-text-fill: " + textColor + "; -fx-control-inner-background: " + inputBg + ";");

        // Limit character count
        area.textProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && newVal.length() > maxLength) {
                area.setText(old);
            }
        });

        return area;
    }

    private DatePicker createDatePicker() {
        boolean isDark = ThemeManager.getInstance().isDarkTheme();
        String inputBg = isDark ? "#3d3d3d" : "#f8f9fa";
        String inputBorder = isDark ? "#4d4d4d" : "#e9ecef";

        DatePicker picker = new DatePicker();
        picker.setPromptText("Select due date");
        picker.setPrefHeight(42);
        picker.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 14px; " +
                "-fx-background-color: " + inputBg + "; -fx-border-color: " + inputBorder + "; " +
                "-fx-border-radius: 8; -fx-background-radius: 8;");
        return picker;
    }

    private ComboBox<OperationStatus> createStatusComboBox() {
        boolean isDark = ThemeManager.getInstance().isDarkTheme();
        String inputBg = isDark ? "#3d3d3d" : "#f8f9fa";
        String inputBorder = isDark ? "#4d4d4d" : "#e9ecef";

        ComboBox<OperationStatus> combo = new ComboBox<>();
        combo.getItems().addAll(OperationStatus.values());
        combo.setValue(OperationStatus.ONGOING);
        combo.setPrefHeight(42);
        combo.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 14px; " +
                "-fx-background-color: " + inputBg + "; -fx-border-color: " + inputBorder + "; " +
                "-fx-border-radius: 8; -fx-background-radius: 8;");

        // Custom display
        combo.setButtonCell(new StatusListCell());
        combo.setCellFactory(lv -> new StatusListCell());

        return combo;
    }

    private ComboBox<Priority> createPriorityComboBox() {
        boolean isDark = ThemeManager.getInstance().isDarkTheme();
        String inputBg = isDark ? "#3d3d3d" : "#f8f9fa";
        String inputBorder = isDark ? "#4d4d4d" : "#e9ecef";

        ComboBox<Priority> combo = new ComboBox<>();
        combo.getItems().addAll(Priority.values());
        combo.setValue(Priority.MEDIUM);
        combo.setPrefHeight(42);
        combo.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 14px; " +
                "-fx-background-color: " + inputBg + "; -fx-border-color: " + inputBorder + "; " +
                "-fx-border-radius: 8; -fx-background-radius: 8;");

        // Custom display
        combo.setButtonCell(new PriorityListCell());
        combo.setCellFactory(lv -> new PriorityListCell());

        return combo;
    }

    private ComboBox<Region> createRegionComboBox(List<Region> regions) {
        boolean isDark = ThemeManager.getInstance().isDarkTheme();
        String inputBg = isDark ? "#3d3d3d" : "#f8f9fa";
        String inputBorder = isDark ? "#4d4d4d" : "#e9ecef";

        ComboBox<Region> combo = new ComboBox<>();
        combo.getItems().addAll(regions);
        combo.setPromptText("Select Region");
        combo.setPrefHeight(42);
        combo.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 14px; " +
                "-fx-background-color: " + inputBg + "; -fx-border-color: " + inputBorder + "; " +
                "-fx-border-radius: 8; -fx-background-radius: 8;");

        combo.setButtonCell(new RegionListCell());
        combo.setCellFactory(lv -> new RegionListCell());

        return combo;
    }

    private Label createErrorLabel() {
        Label label = new Label("Name is required");
        label.setFont(Font.font("Poppins", 12));
        label.setStyle("-fx-text-fill: #ef4444;");
        label.setVisible(false);
        return label;
    }

    private VBox createFormLayout() {
        boolean isDark = ThemeManager.getInstance().isDarkTheme();
        String bgColor = isDark ? "#2d2d2d" : "#ffffff";

        VBox layout = new VBox(16);
        layout.setPadding(new Insets(20, 24, 20, 24));
        layout.setStyle("-fx-background-color: " + bgColor + ";");

        layout.getChildren().addAll(
                createFieldGroup("Name *", nameField),
                errorLabel,
                createFieldGroup("Purpose", purposeField),
                createFieldGroup("Region", regionCombo),
                createFieldGroup("Due Date", dueDatePicker),
                createFieldGroup("Status *", statusCombo),
                createFieldGroup("Priority *", priorityCombo),
                createFieldGroup("Outcome", outcomeField));

        return layout;
    }

    private VBox createFieldGroup(String labelText, Control field) {
        boolean isDark = ThemeManager.getInstance().isDarkTheme();
        String labelColor = isDark ? "#9ca3af" : "#6B7280";

        VBox group = new VBox(6);

        Label label = new Label(labelText);
        label.setFont(Font.font("Poppins", 13));
        label.setStyle("-fx-text-fill: " + labelColor + ";");

        group.getChildren().addAll(label, field);
        return group;
    }

    private void populateFields() {
        if (operation != null) {
            nameField.setText(operation.getName());
            purposeField.setText(operation.getPurpose());
            dueDatePicker.setValue(operation.getDueDate());
            statusCombo.setValue(operation.getStatus());
            priorityCombo.setValue(operation.getPriority());
            outcomeField.setText(operation.getOutcome());

            if (operation.getRegion() != null) {
                regionCombo.getItems().stream()
                        .filter(r -> r.getName().equals(operation.getRegion()))
                        .findFirst()
                        .ifPresent(regionCombo::setValue);
            }
        }
    }

    private Operation validateAndCreateOperation() {
        // Validate name
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            errorLabel.setVisible(true);
            return null;
        }

        // Create or update operation
        Operation op = isEditMode ? operation : new Operation();

        op.setName(name);
        op.setPurpose(purposeField.getText() != null ? purposeField.getText().trim() : "");
        op.setDueDate(dueDatePicker.getValue());
        op.setStatus(statusCombo.getValue());
        op.setPriority(priorityCombo.getValue());
        op.setOutcome(outcomeField.getText() != null ? outcomeField.getText().trim() : "");

        if (regionCombo.getValue() != null) {
            op.setRegion(regionCombo.getValue().getName());
        } else {
            op.setRegion(null);
        }

        return op;
    }

    // Custom cell for Status ComboBox
    private static class StatusListCell extends ListCell<OperationStatus> {
        @Override
        protected void updateItem(OperationStatus item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                switch (item) {
                    case ONGOING -> setText("Ongoing");
                    case IN_PROGRESS -> setText("In Progress");
                    case END -> setText("Completed");
                }
            }
        }
    }

    // Custom cell for Priority ComboBox
    private static class PriorityListCell extends ListCell<Priority> {
        @Override
        protected void updateItem(Priority item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                switch (item) {
                    case HIGH -> setText("High");
                    case MEDIUM -> setText("Medium");
                    case LOW -> setText("Low");
                }
            }
        }
    }

    // Custom cell for Region ComboBox
    private static class RegionListCell extends ListCell<Region> {
        @Override
        protected void updateItem(Region item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                setText(item.getName());
                setStyle("-fx-text-fill: " + item.getColor() + ";");
            }
        }
    }
}
