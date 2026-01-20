package com.roam.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.roam.model.CalendarEvent;
import com.roam.model.JournalEntry;
import com.roam.model.Operation;
import com.roam.model.Wiki;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons;
import org.kordamp.ikonli.boxicons.BoxiconsRegular;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Utility class for exporting application data to various file formats.
 * <p>
 * Provides export functionality for different data types:
 * </p>
 * <ul>
 * <li><strong>Wikis</strong> - Export to Markdown (.md) with YAML front
 * matter</li>
 * <li><strong>Calendar Events</strong> - Export to iCalendar (.ics) format</li>
 * <li><strong>Operations</strong> - Export to JSON format</li>
 * <li><strong>Journal Entries</strong> - Export to Markdown (.md) files</li>
 * </ul>
 * <p>
 * All export operations display a file chooser dialog and show a styled
 * success dialog upon completion with the export location.
 * </p>
 * 
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 * @see DialogUtils
 */
public class ExportUtils {

    // ==================== Constants ====================

    private static final Logger logger = LoggerFactory.getLogger(ExportUtils.class);

    /** Jackson ObjectMapper configured for JSON export with Java 8 time support. */
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // ==================== Wiki Export Methods ====================

    /**
     * Exports a single wiki to a Markdown file.
     * <p>
     * The exported file includes YAML front matter with metadata (title, date,
     * updated, region) followed by the wiki content.
     * </p>
     * 
     * @param Wiki  the wiki to export
     * @param owner the parent window for the file chooser dialog
     */
    public static void exportWikiToMarkdown(Wiki Wiki, Window owner) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Wiki");
        fileChooser.setInitialFileName(Wiki.getTitle() + ".md");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Markdown Files", "*.md"));

        File file = fileChooser.showSaveDialog(owner);

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                // Add metadata header (YAML front matter style)
                writer.write("---\n");
                writer.write("title: " + Wiki.getTitle() + "\n");
                writer.write("date: " + Wiki.getCreatedAt() + "\n");
                writer.write("updated: " + Wiki.getUpdatedAt() + "\n");
                // Tags disabled - replaced with regions
                // if (!Wiki.getTags().isEmpty()) {
                // writer.write("tags: [");
                // writer.write(String.join(", ", Wiki.getTags().stream()
                // .map(tag -> tag.getName()).toArray(String[]::new)));
                // writer.write("]\n");
                // }
                if (Wiki.getRegion() != null && !Wiki.getRegion().isEmpty()) {
                    writer.write("region: " + Wiki.getRegion() + "\n");
                }
                writer.write("---\n\n");

                writer.write(Wiki.getContent() != null ? Wiki.getContent() : "");
                showExportSuccessDialog(owner, "Export Successful", "Wiki exported successfully!", 1, "Exported ",
                        file.getAbsolutePath());
            } catch (IOException e) {
                DialogUtils.showError("Export Error", "Failed to save file", e.getMessage());
            }
        }
    }

    /**
     * Exports all wikis to Markdown files in a selected directory.
     * <p>
     * Each wiki is exported to a separate .md file with a sanitized filename
     * based on the wiki title. Files include YAML front matter with basic metadata.
     * </p>
     * 
     * @param owner the parent window for the directory chooser dialog
     * @param wikis the list of wikis to export
     */
    public static void exportAllWikisToMarkdown(Window owner, java.util.List<Wiki> wikis) {
        javafx.stage.DirectoryChooser dirChooser = new javafx.stage.DirectoryChooser();
        dirChooser.setTitle("Select Export Folder");

        File dir = dirChooser.showDialog(owner);
        if (dir != null) {
            int count = 0;
            for (Wiki wiki : wikis) {
                try {
                    String filename = wiki.getTitle().replaceAll("[^a-zA-Z0-9.-]", "_") + ".md";
                    File file = new File(dir, filename);
                    try (FileWriter writer = new FileWriter(file)) {
                        writer.write("---\n");
                        writer.write("title: " + wiki.getTitle() + "\n");
                        writer.write("date: " + wiki.getCreatedAt() + "\n");
                        writer.write("---\n\n");
                        writer.write(wiki.getContent() != null ? wiki.getContent() : "");
                    }
                    count++;
                } catch (IOException e) {
                    logger.error("Failed to export: {}", wiki.getTitle(), e);
                }
            }
            showExportSuccessDialog(owner, "Export Successful", "Wikis exported successfully!", count, "Exported ",
                    dir.getAbsolutePath());
        }
    }

    // ==================== Calendar Export Methods ====================

    /**
     * Exports calendar events to an iCalendar (.ics) file.
     * <p>
     * Creates a standard iCalendar file compatible with most calendar
     * applications. Each event includes UID, timestamps, summary,
     * description, and location.
     * </p>
     * 
     * @param owner  the parent window for the file chooser dialog
     * @param events the list of calendar events to export
     */
    public static void exportEventsToICS(Window owner, List<CalendarEvent> events) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Events");
        fileChooser.setInitialFileName("events.ics");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("ICS Files", "*.ics"));

        File file = fileChooser.showSaveDialog(owner);
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("BEGIN:VCALENDAR\n");
                writer.write("VERSION:2.0\n");
                writer.write("PRODID:-//Roam//Roam Calendar//EN\n");

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

                for (CalendarEvent event : events) {
                    writer.write("BEGIN:VEVENT\n");
                    writer.write("UID:" + event.getId() + "@roam.app\n");
                    writer.write("DTSTAMP:" + java.time.LocalDateTime.now().format(formatter) + "\n");
                    writer.write("DTSTART:" + event.getStartDateTime().format(formatter) + "\n");
                    writer.write("DTEND:" + event.getEndDateTime().format(formatter) + "\n");
                    writer.write("SUMMARY:" + escapeICS(event.getTitle()) + "\n");
                    if (event.getDescription() != null && !event.getDescription().isEmpty()) {
                        writer.write("DESCRIPTION:" + escapeICS(event.getDescription()) + "\n");
                    }
                    if (event.getLocation() != null && !event.getLocation().isEmpty()) {
                        writer.write("LOCATION:" + escapeICS(event.getLocation()) + "\n");
                    }
                    writer.write("END:VEVENT\n");
                }

                writer.write("END:VCALENDAR\n");
                showExportSuccessDialog(owner, "Export Successful", "Events exported successfully!", events.size(),
                        "Exported ", file.getAbsolutePath());
            } catch (IOException e) {
                DialogUtils.showError("Export Error", "Failed to save file", e.getMessage());
            }
        }
    }

    /**
     * Escapes special characters for iCalendar format.
     * 
     * @param value the string to escape
     * @return the escaped string safe for ICS files
     */
    private static String escapeICS(String value) {
        if (value == null)
            return "";
        return value.replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace("\n", "\\n");
    }

    // ==================== Operations Export Methods ====================

    /**
     * Exports operations to a JSON file.
     * <p>
     * Uses Jackson ObjectMapper with pretty printing and proper date/time
     * serialization for human-readable output.
     * </p>
     * 
     * @param owner      the parent window for the file chooser dialog
     * @param operations the list of operations to export
     */
    public static void exportOperationsToJSON(Window owner, List<Operation> operations) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Operations");
        fileChooser.setInitialFileName("operations.json");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));

        File file = fileChooser.showSaveDialog(owner);
        if (file != null) {
            try {
                objectMapper.writeValue(file, operations);
                showExportSuccessDialog(owner, "Export Successful", "Operations exported successfully!",
                        operations.size(), "Exported ", file.getAbsolutePath());
            } catch (IOException e) {
                DialogUtils.showError("Export Error", "Failed to save file", e.getMessage());
            }
        }
    }

    // ==================== Journal Export Methods ====================

    /**
     * Exports journal entries to Markdown files in a selected directory.
     * <p>
     * Each journal entry is exported to a separate .md file named with the
     * format "Journal_YYYY-MM-DD.md". Files include YAML front matter with
     * title and date metadata.
     * </p>
     * 
     * @param owner   the parent window for the directory chooser dialog
     * @param entries the list of journal entries to export
     */
    public static void exportJournalsToMarkdown(Window owner, List<JournalEntry> entries) {
        javafx.stage.DirectoryChooser dirChooser = new javafx.stage.DirectoryChooser();
        dirChooser.setTitle("Select Export Folder for Journals");

        File dir = dirChooser.showDialog(owner);
        if (dir != null) {
            int count = 0;
            for (JournalEntry entry : entries) {
                try {
                    String filename = "Journal_" + entry.getDate().toString() + ".md";
                    File file = new File(dir, filename);
                    try (FileWriter writer = new FileWriter(file)) {
                        writer.write("---\n");
                        writer.write("title: " + entry.getTitle() + "\n");
                        writer.write("date: " + entry.getDate() + "\n");
                        writer.write("---\n\n");
                        writer.write(entry.getContent() != null ? entry.getContent() : "");
                    }
                    count++;
                } catch (IOException e) {
                    logger.error("Failed to export journal: {}", entry.getTitle(), e);
                }
            }
            showExportSuccessDialog(owner, "Export Successful", "Journals exported successfully!", count, "Exported ",
                    dir.getAbsolutePath());
        }
    }

    // ==================== Private Helper Methods ====================

    /**
     * Shows a custom styled export success dialog with green "Saved to" badge.
     * <p>
     * Displays a polished success dialog with export details including
     * item count and the saved file/directory path.
     * </p>
     * 
     * @param owner      the parent window for the dialog
     * @param title      the dialog title
     * @param header     the header message
     * @param count      the number of items exported
     * @param countLabel the label prefix for the count (e.g., "Exported ")
     * @param savedPath  the full path where items were saved
     */
    private static void showExportSuccessDialog(Window owner, String title, String header, int count, String countLabel,
            String savedPath) {
        Dialog<ButtonType> successDialog = new Dialog<>();
        successDialog.initStyle(javafx.stage.StageStyle.TRANSPARENT);
        successDialog.initOwner(owner);

        VBox successContent = new VBox(16);
        successContent.setAlignment(Pos.CENTER_LEFT);
        successContent.setPadding(new Insets(24));

        HBox successTitleBar = new HBox(12);
        successTitleBar.setAlignment(Pos.CENTER_LEFT);
        FontIcon successTitleIcon = new FontIcon(BoxiconsRegular.EXPORT);
        successTitleIcon.setIconSize(18);
        successTitleIcon.setIconColor(javafx.scene.paint.Color.web("#374151"));
        Label successTitleLabel = new Label(title);
        successTitleLabel.setFont(Font.font("Poppins SemiBold", 14));
        successTitleLabel.setStyle("-fx-text-fill: #374151;");
        javafx.scene.layout.Region successTitleSpacer = new javafx.scene.layout.Region();
        HBox.setHgrow(successTitleSpacer, Priority.ALWAYS);
        Button successCloseBtn = new Button();
        FontIcon successCloseIcon = new FontIcon(Feather.X);
        successCloseIcon.setIconSize(16);
        successCloseBtn.setGraphic(successCloseIcon);
        successCloseBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        successCloseBtn.setOnAction(ev -> successDialog.setResult(ButtonType.OK));
        successTitleBar.getChildren().addAll(successTitleIcon, successTitleLabel, successTitleSpacer, successCloseBtn);

        HBox successHeaderRow = new HBox(12);
        successHeaderRow.setAlignment(Pos.CENTER_LEFT);
        Label successHeaderLabel = new Label(header);
        successHeaderLabel.setFont(Font.font("Poppins SemiBold", 16));
        successHeaderLabel.setStyle("-fx-text-fill: #374151;");
        javafx.scene.layout.Region successHeaderSpacer = new javafx.scene.layout.Region();
        HBox.setHgrow(successHeaderSpacer, Priority.ALWAYS);
        FontIcon successIcon = new FontIcon(BootstrapIcons.CHECK_CIRCLE_FILL);
        successIcon.setIconSize(32);
        successIcon.setIconColor(javafx.scene.paint.Color.web("#22C55E"));
        successHeaderRow.getChildren().addAll(successHeaderLabel, successHeaderSpacer, successIcon);

        javafx.scene.layout.Region successDivider = new javafx.scene.layout.Region();
        successDivider.setPrefHeight(1);
        successDivider.setStyle("-fx-background-color: #E5E7EB;");

        Label successCountLabel = new Label(countLabel + count + (count == 1 ? " item" : " items"));
        successCountLabel.setFont(Font.font("Poppins", 14));
        successCountLabel.setStyle("-fx-text-fill: #374151;");

        // Green styled "Saved to" badge
        Label successMsgLabel = new Label("Saved to: " + savedPath);
        successMsgLabel.setFont(Font.font("Poppins", 13));
        successMsgLabel.setStyle(
                "-fx-background-color: #16A34A; -fx-text-fill: white; -fx-padding: 10 16; -fx-background-radius: 20;");
        successMsgLabel.setWrapText(true);
        successMsgLabel.setMinWidth(300);
        successMsgLabel.setPrefWidth(400);
        successMsgLabel.setMaxWidth(Double.MAX_VALUE);

        HBox successBtnBox = new HBox();
        successBtnBox.setAlignment(Pos.CENTER_RIGHT);
        Button successOkBtn = new Button("OK");
        successOkBtn.setPrefWidth(100);
        successOkBtn.setPrefHeight(36);
        successOkBtn.setStyle(
                "-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-family: 'Poppins'; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand; -fx-focus-color: transparent; -fx-faint-focus-color: transparent; -fx-border-color: transparent;");
        successOkBtn.setOnAction(ev -> successDialog.setResult(ButtonType.OK));
        successBtnBox.getChildren().add(successOkBtn);

        successContent.getChildren().addAll(successTitleBar, successHeaderRow, successDivider,
                successCountLabel, successMsgLabel, successBtnBox);

        VBox successContainer = new VBox();
        successContainer.setStyle(
                "-fx-background-color: white; -fx-background-radius: 16; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 16;");
        successContainer.setEffect(
                new javafx.scene.effect.DropShadow(30, 0, 8, javafx.scene.paint.Color.rgb(0, 0, 0, 0.15)));
        successContainer.getChildren().add(successContent);

        StackPane successShadowContainer = new StackPane(successContainer);
        successShadowContainer.setPadding(new Insets(40));
        successShadowContainer.setStyle("-fx-background-color: transparent;");

        successDialog.getDialogPane().setContent(successShadowContainer);
        successDialog.getDialogPane().setStyle("-fx-background-color: transparent; -fx-padding: 0;");
        successDialog.getDialogPane().getScene().setFill(javafx.scene.paint.Color.TRANSPARENT);
        successDialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        successDialog.getDialogPane().lookupButton(ButtonType.OK).setVisible(false);
        successDialog.getDialogPane().lookupButton(ButtonType.OK).setManaged(false);

        successDialog.showAndWait();
    }
}
