package com.roam.view.components;

import com.roam.util.ThemeManager;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

/**
 * A dialog component for configuring wiki and application preferences.
 * <p>
 * This dialog provides user interface controls for customizing various settings
 * including editor preferences (font family, font size), auto-save behavior,
 * display options (word count visibility), and confirmation dialogs. Settings
 * are organized into logical sections for easy navigation.
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public class PreferencesDialog extends Dialog<Void> {

    private ComboBox<Integer> autoSaveIntervalCombo;
    private ComboBox<String> editorFontCombo;
    private Spinner<Integer> fontSizeSpinner;
    private CheckBox enableAutoSaveCheck;
    private CheckBox showWordCountCheck;
    private CheckBox confirmDeleteCheck;

    public PreferencesDialog() {
        setTitle("Wiki Preferences");
        setHeaderText("Configure your wiki settings");
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Apply theme styling
        ThemeManager.getInstance().styleDialog(this);
        boolean isDark = ThemeManager.getInstance().isDarkTheme();
        String blueColor = "#4285f4";
        String textSecondary = isDark ? "#b0b0b0" : "#757575";

        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(20));

        // Editor Settings Section
        Label editorHeader = new Label("Editor Settings");
        editorHeader.setFont(Font.font("Poppins Bold", 16));
        editorHeader.setStyle("-fx-text-fill: " + blueColor + ";");

        GridPane editorGrid = new GridPane();
        editorGrid.setHgap(20);
        editorGrid.setVgap(15);
        editorGrid.setPadding(new Insets(10, 0, 0, 0));

        // Font family
        Label fontLabel = new Label("Editor Font:");
        fontLabel.setFont(Font.font("Poppins", 13));
        editorFontCombo = new ComboBox<>();
        editorFontCombo.getItems().addAll("Consolas", "Courier New", "Monaco", "Menlo", "Source Code Pro");
        editorFontCombo.setValue("Consolas");
        editorFontCombo.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 13px;");

        // Font size
        Label fontSizeLabel = new Label("Font Size:");
        fontSizeLabel.setFont(Font.font("Poppins", 13));
        fontSizeSpinner = new Spinner<>(10, 24, 14, 1);
        fontSizeSpinner.setEditable(true);
        fontSizeSpinner.setPrefWidth(80);
        fontSizeSpinner.setStyle("-fx-font-family: 'Poppins';");

        editorGrid.add(fontLabel, 0, 0);
        editorGrid.add(editorFontCombo, 1, 0);
        editorGrid.add(fontSizeLabel, 0, 1);
        editorGrid.add(fontSizeSpinner, 1, 1);

        // Auto-save Settings Section
        Label autoSaveHeader = new Label("Auto-Save Settings");
        autoSaveHeader.setFont(Font.font("Poppins Bold", 16));
        autoSaveHeader.setStyle("-fx-text-fill: " + blueColor + ";");

        VBox autoSaveBox = new VBox(10);
        autoSaveBox.setPadding(new Insets(10, 0, 0, 0));

        enableAutoSaveCheck = new CheckBox("Enable auto-save");
        enableAutoSaveCheck.setFont(Font.font("Poppins", 13));
        enableAutoSaveCheck.setSelected(true);

        GridPane autoSaveGrid = new GridPane();
        autoSaveGrid.setHgap(20);
        autoSaveGrid.setVgap(10);

        Label intervalLabel = new Label("Auto-save interval:");
        intervalLabel.setFont(Font.font("Poppins", 13));
        autoSaveIntervalCombo = new ComboBox<>();
        autoSaveIntervalCombo.getItems().addAll(1, 2, 3, 5, 10);
        autoSaveIntervalCombo.setValue(2);
        autoSaveIntervalCombo.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 13px;");

        Label secondsLabel = new Label("seconds");
        secondsLabel.setFont(Font.font("Poppins", 13));
        secondsLabel.setStyle("-fx-text-fill: " + textSecondary + ";");

        autoSaveGrid.add(intervalLabel, 0, 0);
        autoSaveGrid.add(autoSaveIntervalCombo, 1, 0);
        autoSaveGrid.add(secondsLabel, 2, 0);

        // Bind auto-save controls to checkbox
        enableAutoSaveCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            autoSaveIntervalCombo.setDisable(!newVal);
        });

        autoSaveBox.getChildren().addAll(enableAutoSaveCheck, autoSaveGrid);

        // Display Settings Section
        Label displayHeader = new Label("Display Settings");
        displayHeader.setFont(Font.font("Poppins Bold", 16));
        displayHeader.setStyle("-fx-text-fill: " + blueColor + ";");

        VBox displayBox = new VBox(10);
        displayBox.setPadding(new Insets(10, 0, 0, 0));

        showWordCountCheck = new CheckBox("Show word count in editor");
        showWordCountCheck.setFont(Font.font("Poppins", 13));
        showWordCountCheck.setSelected(true);

        confirmDeleteCheck = new CheckBox("Confirm before deleting wikis");
        confirmDeleteCheck.setFont(Font.font("Poppins", 13));
        confirmDeleteCheck.setSelected(true);

        displayBox.getChildren().addAll(showWordCountCheck, confirmDeleteCheck);

        // Add all sections to main content
        mainContent.getChildren().addAll(
                editorHeader, editorGrid,
                new Separator(),
                autoSaveHeader, autoSaveBox,
                new Separator(),
                displayHeader, displayBox);

        getDialogPane().setContent(mainContent);
        getDialogPane().setPrefWidth(450);

        // Handle result
        setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                savePreferences();
            }
            return null;
        });
    }

    private void savePreferences() {
        // In a real application, save these preferences to a properties file or
        // database
        // For now, just show a success message
        System.out.println("Preferences saved:");
        System.out.println("  Font: " + editorFontCombo.getValue());
        System.out.println("  Font Size: " + fontSizeSpinner.getValue());
        System.out.println("  Auto-save enabled: " + enableAutoSaveCheck.isSelected());
        System.out.println("  Auto-save interval: " + autoSaveIntervalCombo.getValue() + " seconds");
        System.out.println("  Show word count: " + showWordCountCheck.isSelected());
        System.out.println("  Confirm delete: " + confirmDeleteCheck.isSelected());
    }
}
