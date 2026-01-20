package com.roam.util;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.StageStyle;
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Optional;

/**
 * Utility class for creating and displaying styled dialog boxes.
 * <p>
 * Provides consistent, theme-aware dialog implementations including:
 * </p>
 * <ul>
 * <li>Confirmation dialogs with OK/Cancel buttons</li>
 * <li>Error dialogs for displaying error messages</li>
 * <li>Information dialogs for general messages</li>
 * <li>Success dialogs with styled green checkmarks</li>
 * <li>Delete confirmation dialogs with warning styling</li>
 * </ul>
 * <p>
 * All dialogs automatically adapt to the current theme (light/dark mode)
 * and use consistent styling with rounded corners and drop shadows.
 * </p>
 * 
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 * @see ThemeManager
 */
public class DialogUtils {

        // ==================== Confirmation Dialogs ====================

        /**
         * Shows a confirmation dialog with proper theming.
         * 
         * @param title   the dialog window title
         * @param header  the header text displayed prominently
         * @param content the detailed message content
         * @return {@code true} if OK/Yes clicked, {@code false} if Cancel/No clicked
         */
        public static boolean showConfirmation(String title, String header, String content) {
                Alert alert = ThemeManager.getInstance().createAlert(
                                Alert.AlertType.CONFIRMATION, title, header, content);
                styleDialogPane(alert);
                Optional<ButtonType> result = alert.showAndWait();
                return result.isPresent() && result.get() == ButtonType.OK;
        }

        // ==================== Error Dialogs ====================

        /**
         * Shows an error dialog with proper theming.
         * 
         * @param title   the dialog window title
         * @param header  the header text displayed prominently
         * @param content the detailed error message
         */
        public static void showError(String title, String header, String content) {
                Alert alert = ThemeManager.getInstance().createAlert(
                                Alert.AlertType.ERROR, title, header, content);
                styleDialogPane(alert);
                alert.showAndWait();
        }

        // ==================== Information Dialogs ====================

        /**
         * Shows an information dialog with proper theming.
         * 
         * @param title   the dialog window title
         * @param header  the header text displayed prominently
         * @param content the detailed information message
         */
        public static void showInfo(String title, String header, String content) {
                Alert alert = ThemeManager.getInstance().createAlert(
                                Alert.AlertType.INFORMATION, title, header, content);
                styleDialogPane(alert);
                alert.showAndWait();
        }

        // ==================== Success Dialogs ====================

        /**
         * Shows a success message with a styled dialog.
         * <p>
         * Displays a custom-styled dialog with a green checkmark icon,
         * matching the visual style of settings dialogs.
         * </p>
         * 
         * @param message the success message to display
         */
        public static void showSuccess(String message) {
                boolean isDark = ThemeManager.getInstance().isDarkTheme();
                String bgColor = isDark ? "#2d2d2d" : "#ffffff";
                String textColor = isDark ? "#ffffff" : "#374151";
                String borderColor = isDark ? "#404040" : "#E5E7EB";

                Dialog<ButtonType> successDialog = new Dialog<>();
                successDialog.initStyle(StageStyle.TRANSPARENT);

                VBox successContent = new VBox(16);
                successContent.setAlignment(Pos.CENTER_LEFT);
                successContent.setPadding(new Insets(24));

                // Header row with message and checkmark
                HBox successHeaderRow = new HBox(12);
                successHeaderRow.setAlignment(Pos.CENTER_LEFT);

                FontIcon successIcon = new FontIcon(BootstrapIcons.CHECK_CIRCLE_FILL);
                successIcon.setIconSize(28);
                successIcon.setIconColor(Color.web("#22C55E"));

                Label successHeaderLabel = new Label(message);
                successHeaderLabel.setFont(Font.font("Poppins SemiBold", 15));
                successHeaderLabel.setStyle("-fx-text-fill: " + textColor + ";");
                successHeaderLabel.setWrapText(true);
                HBox.setHgrow(successHeaderLabel, Priority.ALWAYS);

                successHeaderRow.getChildren().addAll(successIcon, successHeaderLabel);

                // OK button
                HBox successBtnBox = new HBox();
                successBtnBox.setAlignment(Pos.CENTER_RIGHT);
                successBtnBox.setPadding(new Insets(8, 0, 0, 0));
                Button successOkBtn = new Button("OK");
                successOkBtn.setPrefWidth(100);
                successOkBtn.setPrefHeight(36);
                successOkBtn.setStyle(
                                "-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-background-radius: 8; " +
                                                "-fx-font-family: 'Poppins'; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;");
                successOkBtn.setOnAction(ev -> successDialog.setResult(ButtonType.OK));
                successBtnBox.getChildren().add(successOkBtn);

                successContent.getChildren().addAll(successHeaderRow, successBtnBox);

                VBox successContainer = new VBox();
                successContainer.setStyle(
                                "-fx-background-color: " + bgColor + "; -fx-background-radius: 16; " +
                                                "-fx-border-color: " + borderColor
                                                + "; -fx-border-width: 1; -fx-border-radius: 16;");
                successContainer.setEffect(new DropShadow(30, 0, 8, Color.rgb(0, 0, 0, 0.15)));
                successContainer.setPrefWidth(360);
                successContainer.setMaxWidth(400);
                successContainer.getChildren().add(successContent);

                StackPane successShadowContainer = new StackPane(successContainer);
                successShadowContainer.setPadding(new Insets(40));
                successShadowContainer.setStyle("-fx-background-color: transparent;");

                successDialog.getDialogPane().setContent(successShadowContainer);
                successDialog.getDialogPane().setStyle("-fx-background-color: transparent; -fx-padding: 0;");
                successDialog.getDialogPane().getScene().setFill(Color.TRANSPARENT);
                successDialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
                successDialog.getDialogPane().lookupButton(ButtonType.OK).setVisible(false);
                successDialog.getDialogPane().lookupButton(ButtonType.OK).setManaged(false);

                successDialog.showAndWait();
        }

        // ==================== Dialog Styling Helpers ====================

        /**
         * Applies consistent styling to any dialog.
         * <p>
         * Adds rounded corners and transparent stage style for
         * a modern, polished appearance.
         * </p>
         * 
         * @param dialog the dialog to style (may be null, in which case nothing
         *               happens)
         */
        public static void styleDialogPane(Dialog<?> dialog) {
                if (dialog == null)
                        return;

                DialogPane dialogPane = dialog.getDialogPane();

                // Apply rounded corners style
                dialogPane.setStyle(dialogPane.getStyle() +
                                "-fx-background-radius: 20px; -fx-border-radius: 20px;");

                // Make the dialog window itself have rounded corners
                dialog.initStyle(StageStyle.TRANSPARENT);
        }

        // ==================== Delete Confirmation Dialogs ====================

        /**
         * Shows a styled delete confirmation dialog.
         * <p>
         * Displays a warning dialog with a red trash icon, the item name,
         * and Cancel/Delete buttons. Automatically adapts to light/dark theme.
         * </p>
         * 
         * @param title       the title of the item being deleted
         * @param itemType    the type of item (e.g., "Task", "Operation")
         * @param description additional description text (defaults to "This action
         *                    cannot be undone.")
         * @return {@code true} if user confirmed deletion, {@code false} otherwise
         */
        public static boolean showDeleteConfirmation(String title, String itemType, String description) {
                boolean isDark = ThemeManager.getInstance().isDarkTheme();
                String bgColor = isDark ? "#2d2d2d" : "#ffffff";
                String textColor = isDark ? "#ffffff" : "#374151";
                String secondaryTextColor = isDark ? "#9ca3af" : "#6B7280";
                String borderColor = isDark ? "#404040" : "#E5E7EB";

                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.initStyle(StageStyle.TRANSPARENT);

                // Create dialog content
                VBox dialogContent = new VBox(16);
                dialogContent.setAlignment(Pos.CENTER);
                dialogContent.setPadding(new Insets(24));

                // Warning icon - red trash icon
                FontIcon warningIcon = new FontIcon(org.kordamp.ikonli.feather.Feather.TRASH_2);
                warningIcon.setIconSize(56);
                warningIcon.setIconColor(Color.web("#EF4444"));
                VBox.setMargin(warningIcon, new Insets(0, 0, 8, 0));

                // Title text
                Label titleLabel = new Label("Delete " + itemType + "?");
                titleLabel.setFont(Font.font("Poppins SemiBold", 18));
                titleLabel.setStyle("-fx-text-fill: " + textColor + ";");

                // Item name
                Label itemLabel = new Label("\"" + title + "\"");
                itemLabel.setFont(Font.font("Poppins Medium", 14));
                itemLabel.setStyle("-fx-text-fill: " + textColor + ";");
                itemLabel.setWrapText(true);
                itemLabel.setMaxWidth(280);
                itemLabel.setAlignment(Pos.CENTER);

                // Description text
                Label descLabel = new Label(description != null ? description : "This action cannot be undone.");
                descLabel.setFont(Font.font("Poppins", 13));
                descLabel.setStyle("-fx-text-fill: " + secondaryTextColor + ";");
                descLabel.setWrapText(true);
                descLabel.setMaxWidth(280);
                descLabel.setAlignment(Pos.CENTER);

                // Buttons
                HBox buttonBox = new HBox(12);
                buttonBox.setAlignment(Pos.CENTER);
                VBox.setMargin(buttonBox, new Insets(8, 0, 0, 0));

                Button cancelBtn = new Button("Cancel");
                cancelBtn.setPrefWidth(110);
                cancelBtn.setPrefHeight(38);
                cancelBtn.setStyle(
                                "-fx-background-color: " + (isDark ? "#404040" : "#F3F4F6") + "; " +
                                                "-fx-text-fill: " + textColor + "; " +
                                                "-fx-background-radius: 8; " +
                                                "-fx-font-family: 'Poppins'; -fx-font-size: 14px; -fx-cursor: hand;");
                cancelBtn.setOnAction(e -> dialog.setResult(ButtonType.CANCEL));

                Button deleteBtn = new Button("Delete");
                deleteBtn.setPrefWidth(110);
                deleteBtn.setPrefHeight(38);
                deleteBtn.setStyle(
                                "-fx-background-color: #EF4444; -fx-text-fill: white; -fx-background-radius: 8; " +
                                                "-fx-font-family: 'Poppins'; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;");
                deleteBtn.setOnAction(e -> dialog.setResult(ButtonType.OK));

                buttonBox.getChildren().addAll(cancelBtn, deleteBtn);

                dialogContent.getChildren().addAll(warningIcon, titleLabel, itemLabel, descLabel, buttonBox);

                // Main container with drop shadow
                VBox mainContainer = new VBox();
                mainContainer.setStyle(
                                "-fx-background-color: " + bgColor + "; -fx-background-radius: 16; " +
                                                "-fx-border-color: " + borderColor
                                                + "; -fx-border-width: 1; -fx-border-radius: 16;");
                mainContainer.setEffect(new DropShadow(30, 0, 8, Color.rgb(0, 0, 0, 0.15)));
                mainContainer.setPrefWidth(340);
                mainContainer.setMaxWidth(380);
                mainContainer.getChildren().add(dialogContent);

                // Wrap in StackPane with padding to allow shadow to render
                StackPane shadowContainer = new StackPane(mainContainer);
                shadowContainer.setPadding(new Insets(40));
                shadowContainer.setStyle("-fx-background-color: transparent;");

                dialog.getDialogPane().setContent(shadowContainer);
                dialog.getDialogPane().setStyle("-fx-background-color: transparent; -fx-padding: 0;");
                dialog.getDialogPane().getScene().setFill(Color.TRANSPARENT);

                // Hide default buttons
                dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
                dialog.getDialogPane().lookupButton(ButtonType.CANCEL).setVisible(false);
                dialog.getDialogPane().lookupButton(ButtonType.CANCEL).setManaged(false);

                Optional<ButtonType> result = dialog.showAndWait();
                return result.isPresent() && result.get() == ButtonType.OK;
        }
}
