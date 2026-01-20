package com.roam.view.components;

import com.roam.controller.WikiController;
import com.roam.model.WikiTemplate;
import com.roam.util.ThemeManager;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Modality;

/**
 * A dialog component for managing wiki templates.
 * <p>
 * This dialog provides a comprehensive interface for creating, editing, and
 * deleting
 * wiki templates. It features a list view of existing templates with custom
 * cell
 * rendering, a toolbar with action buttons, and modal dialogs for template
 * editing.
 * Templates can be used to quickly create new wikis with predefined content and
 * structure.
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public class TemplateManagerDialog extends Dialog<Void> {

    private final WikiController controller;
    private ListView<WikiTemplate> templateListView;
    private Button editButton;
    private Button deleteButton;

    public TemplateManagerDialog(WikiController controller) {
        this.controller = controller;
        initializeDialog();
    }

    private void initializeDialog() {
        setTitle("Template Manager");
        setHeaderText("Manage Wiki Templates");
        initModality(Modality.APPLICATION_MODAL);
        setResizable(true);
        setWidth(700);
        setHeight(500);

        // Apply theme styling
        ThemeManager.getInstance().styleDialog(this);
        boolean isDark = ThemeManager.getInstance().isDarkTheme();
        String blueColor = "#4285f4";
        String redColor = "#e53935";

        // Create main layout
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(15));

        // Create toolbar
        HBox toolbar = createToolbar(isDark, blueColor, redColor);
        mainLayout.setTop(toolbar);

        // Create template list
        templateListView = new ListView<>();
        templateListView.setPrefHeight(350);
        templateListView.setCellFactory(lv -> new TemplateCell());
        refreshTemplateList();

        // Enable/disable buttons based on selection
        templateListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasSelection = newVal != null;
            boolean isDefault = hasSelection && newVal.getIsDefault();
            editButton.setDisable(!hasSelection);
            deleteButton.setDisable(!hasSelection || isDefault);
        });

        mainLayout.setCenter(templateListView);

        getDialogPane().setContent(mainLayout);
        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
    }

    private HBox createToolbar(boolean isDark, String blueColor, String redColor) {
        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(0, 0, 15, 0));

        String bgColor = isDark ? "#2d2d2d" : "#ffffff";

        Button newButton = new Button("+ New Template");
        newButton.setStyle(
                "-fx-background-color: " + blueColor + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 6; " +
                        "-fx-padding: 8 16; " +
                        "-fx-cursor: hand;");
        newButton.setOnAction(e -> createNewTemplate());

        editButton = new Button("Edit");
        editButton.setStyle(
                "-fx-background-color: " + bgColor + "; " +
                        "-fx-text-fill: " + blueColor + "; " +
                        "-fx-border-color: " + blueColor + "; " +
                        "-fx-border-width: 1; " +
                        "-fx-background-radius: 6; " +
                        "-fx-padding: 8 16; " +
                        "-fx-cursor: hand;");
        editButton.setDisable(true);
        editButton.setOnAction(e -> editSelectedTemplate());

        deleteButton = new Button("Delete");
        deleteButton.setStyle(
                "-fx-background-color: " + bgColor + "; " +
                        "-fx-text-fill: " + redColor + "; " +
                        "-fx-border-color: " + redColor + "; " +
                        "-fx-border-width: 1; " +
                        "-fx-background-radius: 6; " +
                        "-fx-padding: 8 16; " +
                        "-fx-cursor: hand;");
        deleteButton.setDisable(true);
        deleteButton.setOnAction(e -> deleteSelectedTemplate());

        toolbar.getChildren().addAll(newButton, editButton, deleteButton);
        return toolbar;
    }

    private void createNewTemplate() {
        TemplateEditDialog editDialog = new TemplateEditDialog(null);
        editDialog.showAndWait().ifPresent(template -> {
            controller.createTemplate(
                    template.getName(),
                    template.getDescription(),
                    template.getContent(),
                    template.getIcon());
            refreshTemplateList();
        });
    }

    private void editSelectedTemplate() {
        WikiTemplate selected = templateListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            TemplateEditDialog editDialog = new TemplateEditDialog(selected);
            editDialog.showAndWait().ifPresent(updated -> {
                selected.setName(updated.getName());
                selected.setDescription(updated.getDescription());
                selected.setContent(updated.getContent());
                selected.setIcon(updated.getIcon());
                controller.updateTemplate(selected);
                refreshTemplateList();
            });
        }
    }

    private void deleteSelectedTemplate() {
        WikiTemplate selected = templateListView.getSelectionModel().getSelectedItem();
        if (selected != null && !selected.getIsDefault()) {
            Alert confirm = ThemeManager.getInstance().createAlert(
                    Alert.AlertType.CONFIRMATION,
                    "Delete Template",
                    "Delete \"" + selected.getName() + "\"?",
                    "This action cannot be undone.");
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    controller.deleteTemplate(selected);
                    refreshTemplateList();
                }
            });
        }
    }

    private void refreshTemplateList() {
        templateListView.getItems().setAll(controller.loadAllTemplates());
    }

    private static class TemplateCell extends ListCell<WikiTemplate> {
        @Override
        protected void updateItem(WikiTemplate template, boolean empty) {
            super.updateItem(template, empty);
            if (empty || template == null) {
                setText(null);
                setGraphic(null);
            } else {
                boolean isDark = ThemeManager.getInstance().isDarkTheme();
                String textSecondary = isDark ? "#b0b0b0" : "#757575";
                String greenBg = isDark ? "#1b4332" : "#e8f5e9";
                String greenColor = isDark ? "#69b378" : "#2e7d32";

                HBox box = new HBox(15);
                box.setPadding(new Insets(10));

                Label icon = new Label(template.getIcon());
                icon.setFont(Font.font(24));
                icon.setPrefWidth(40);

                VBox textBox = new VBox(5);
                Label name = new Label(template.getName());
                name.setFont(Font.font("System Bold", 14));
                Label desc = new Label(template.getDescription());
                desc.setFont(Font.font(12));
                desc.setStyle("-fx-text-fill: " + textSecondary + ";");
                textBox.getChildren().addAll(name, desc);

                box.getChildren().addAll(icon, textBox);

                if (template.getIsDefault()) {
                    Label defaultBadge = new Label("DEFAULT");
                    defaultBadge.setFont(Font.font(10));
                    defaultBadge.setStyle(
                            "-fx-background-color: " + greenBg + "; " +
                                    "-fx-text-fill: " + greenColor + "; " +
                                    "-fx-padding: 2 8; " +
                                    "-fx-background-radius: 4;");
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    box.getChildren().addAll(spacer, defaultBadge);
                }

                setGraphic(box);
            }
        }
    }

    private static class TemplateEditDialog extends Dialog<WikiTemplate> {
        private final TextField nameField;
        private final TextField descField;
        private final TextArea contentArea;
        private final TextField iconField;

        public TemplateEditDialog(WikiTemplate template) {
            setTitle(template == null ? "New Template" : "Edit Template");
            setHeaderText(template == null ? "Create a new Wiki template" : "Edit template");

            // Apply theme styling
            ThemeManager.getInstance().styleDialog(this);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20));

            nameField = new TextField(template != null ? template.getName() : "");
            nameField.setPromptText("Template Name");

            descField = new TextField(template != null ? template.getDescription() : "");
            descField.setPromptText("Description");

            iconField = new TextField(template != null ? template.getIcon() : "file");
            iconField.setPromptText("Icon name (e.g., file, folder, star)");
            iconField.setPrefWidth(100);

            contentArea = new TextArea(template != null ? template.getContent() : "");
            contentArea.setPromptText("Template content...");
            contentArea.setPrefHeight(200);
            contentArea.setWrapText(true);

            grid.add(new Label("Name:"), 0, 0);
            grid.add(nameField, 1, 0);
            grid.add(new Label("Icon:"), 0, 1);
            grid.add(iconField, 1, 1);
            grid.add(new Label("Description:"), 0, 2);
            grid.add(descField, 1, 2);
            grid.add(new Label("Content:"), 0, 3);
            grid.add(contentArea, 1, 3);

            getDialogPane().setContent(grid);
            getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
            okButton.setDisable(true);

            nameField.textProperty().addListener((obs, oldVal, newVal) -> okButton.setDisable(newVal.trim().isEmpty()));

            setResultConverter(btn -> {
                if (btn == ButtonType.OK) {
                    WikiTemplate result = template != null ? template : new WikiTemplate();
                    result.setName(nameField.getText().trim());
                    result.setDescription(descField.getText().trim());
                    result.setContent(contentArea.getText());
                    result.setIcon(iconField.getText().isEmpty() ? "file" : iconField.getText());
                    return result;
                }
                return null;
            });
        }
    }
}
