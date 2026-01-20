package com.roam.view.components;

import com.roam.model.Wiki;
import com.roam.model.WikiFileAttachment;
import com.roam.repository.WikiFileAttachmentRepository;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * A sidebar component for managing file attachments associated with wikis.
 * <p>
 * This component displays a hierarchical list of file attachments for the
 * current wiki,
 * allowing users to add, open, and delete files. It features file type icons,
 * size
 * formatting, and date display. Files are stored in the application's data
 * directory
 * and can be opened using the system's default application.
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public class WikiFileHierarchy extends VBox {

    private final WikiFileAttachmentRepository attachmentRepository;
    private final Font poppinsRegular;
    private final Font poppinsBold;

    private Wiki currentWiki;
    private VBox fileListContainer;

    public WikiFileHierarchy(Font poppinsRegular, Font poppinsBold) {
        this.attachmentRepository = new WikiFileAttachmentRepository();
        this.poppinsRegular = poppinsRegular;
        this.poppinsBold = poppinsBold;

        configurePane();
        buildUI();
    }

    private void configurePane() {
        setMinWidth(250);
        setPrefWidth(250);
        setMaxWidth(250);
        setStyle("-fx-background-color: -roam-gray-bg; -fx-border-color: -roam-border; -fx-border-width: 0 1 0 0;");
        setPadding(new Insets(15));
        setSpacing(10);
    }

    private void buildUI() {
        // Header
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        FontIcon folderIcon = new FontIcon(Feather.FOLDER);
        folderIcon.setIconSize(14);

        Label titleLabel = new Label("Files");
        titleLabel.setFont(Font.font(poppinsBold.getFamily(), 15));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addFileBtn = new Button();
        addFileBtn.setGraphic(new FontIcon(Feather.PLUS));
        addFileBtn.setPrefSize(28, 28);
        addFileBtn.setStyle(
                "-fx-background-color: -roam-blue; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand;");
        addFileBtn.setTooltip(new Tooltip("Add file attachment"));
        addFileBtn.setOnAction(e -> handleAddFile());

        header.getChildren().addAll(folderIcon, titleLabel, spacer, addFileBtn);

        // File list container
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        fileListContainer = new VBox(8);
        fileListContainer.setPadding(new Insets(10, 0, 0, 0));
        scrollPane.setContent(fileListContainer);

        getChildren().addAll(header, scrollPane);
    }

    public void loadFiles(Wiki wiki) {
        this.currentWiki = wiki;
        fileListContainer.getChildren().clear();

        if (wiki == null || wiki.getId() == null) {
            Label emptyLabel = new Label("No wiki selected");
            emptyLabel.setFont(Font.font(poppinsRegular.getFamily(), 12));
            emptyLabel.setStyle("-fx-text-fill: -roam-text-hint;");
            fileListContainer.getChildren().add(emptyLabel);
            return;
        }

        List<WikiFileAttachment> attachments = attachmentRepository.findByWikiId(wiki.getId());

        if (attachments.isEmpty()) {
            Label emptyLabel = new Label("No files attached");
            emptyLabel.setFont(Font.font(poppinsRegular.getFamily(), 12));
            emptyLabel.setStyle("-fx-text-fill: -roam-text-hint;");
            fileListContainer.getChildren().add(emptyLabel);
        } else {
            for (WikiFileAttachment attachment : attachments) {
                fileListContainer.getChildren().add(createFileItem(attachment));
            }
        }
    }

    private VBox createFileItem(WikiFileAttachment attachment) {
        VBox item = new VBox(4);
        item.setPadding(new Insets(8));
        item.setStyle("-fx-background-color: transparent; -fx-background-radius: 6; -fx-cursor: hand;");

        item.setOnMouseEntered(e -> item.setStyle(
                "-fx-background-color: -roam-bg-primary; -fx-background-radius: 6; -fx-cursor: hand;"));
        item.setOnMouseExited(e -> item.setStyle(
                "-fx-background-color: transparent; -fx-background-radius: 6; -fx-cursor: hand;"));

        // File name and icon
        HBox nameRow = new HBox(6);
        nameRow.setAlignment(Pos.CENTER_LEFT);

        FontIcon fileIcon = new FontIcon(getFileIcon(attachment.getFileType()));
        fileIcon.setIconSize(14);
        fileIcon.setStyle("-fx-icon-color: -roam-blue;");

        Label nameLabel = new Label(attachment.getFileName());
        nameLabel.setFont(Font.font(poppinsRegular.getFamily(), 12));
        nameLabel.setMaxWidth(160);
        nameLabel.setStyle("-fx-text-fill: -roam-text-primary; -fx-text-overflow: ellipsis;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button deleteBtn = new Button();
        deleteBtn.setGraphic(new FontIcon(Feather.TRASH_2));
        deleteBtn.setPrefSize(20, 20);
        deleteBtn.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-cursor: hand;");
        deleteBtn.setOnAction(e -> {
            e.consume();
            handleDeleteFile(attachment);
        });

        nameRow.getChildren().addAll(fileIcon, nameLabel, spacer, deleteBtn);

        // File metadata
        HBox metaRow = new HBox(8);
        metaRow.setAlignment(Pos.CENTER_LEFT);
        metaRow.setPadding(new Insets(0, 0, 0, 20));

        Label sizeLabel = new Label(formatFileSize(attachment.getFileSize()));
        sizeLabel.setFont(Font.font(poppinsRegular.getFamily(), 10));
        sizeLabel.setStyle("-fx-text-fill: -roam-text-hint;");

        Label dateLabel = new Label(attachment.getCreatedAt() != null
                ? attachment.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM d"))
                : "");
        dateLabel.setFont(Font.font(poppinsRegular.getFamily(), 10));
        dateLabel.setStyle("-fx-text-fill: -roam-text-hint;");

        metaRow.getChildren().addAll(sizeLabel, new Label("•"), dateLabel);

        item.getChildren().addAll(nameRow, metaRow);

        // Click to open file
        item.setOnMouseClicked(e -> {
            if (!e.isConsumed()) {
                handleOpenFile(attachment);
            }
        });

        return item;
    }

    private Feather getFileIcon(String fileType) {
        if (fileType == null)
            return Feather.FILE;

        String type = fileType.toLowerCase();
        if (type.contains("pdf"))
            return Feather.FILE_TEXT;
        if (type.contains("image") || type.contains("png") || type.contains("jpg") || type.contains("jpeg"))
            return Feather.IMAGE;
        if (type.contains("word") || type.contains("doc"))
            return Feather.FILE_TEXT;
        if (type.contains("excel") || type.contains("xls") || type.contains("csv"))
            return Feather.FILE_TEXT;
        if (type.contains("zip") || type.contains("rar"))
            return Feather.ARCHIVE;
        if (type.contains("video") || type.contains("mp4"))
            return Feather.VIDEO;

        return Feather.FILE;
    }

    private String formatFileSize(Long bytes) {
        if (bytes == null || bytes == 0)
            return "0 B";

        DecimalFormat df = new DecimalFormat("#.##");
        if (bytes < 1024)
            return bytes + " B";
        if (bytes < 1024 * 1024)
            return df.format(bytes / 1024.0) + " KB";
        if (bytes < 1024 * 1024 * 1024)
            return df.format(bytes / (1024.0 * 1024.0)) + " MB";
        return df.format(bytes / (1024.0 * 1024.0 * 1024.0)) + " GB";
    }

    private void handleAddFile() {
        if (currentWiki == null || currentWiki.getId() == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Wiki Selected");
            alert.setHeaderText("Cannot add file");
            alert.setContentText("Please select or create a wiki first.");
            alert.showAndWait();
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File to Attach");
        File selectedFile = fileChooser.showOpenDialog(getScene().getWindow());

        if (selectedFile != null) {
            try {
                // Create attachments directory if it doesn't exist
                File attachmentsDir = new File("data/wiki-attachments");
                attachmentsDir.mkdirs();

                // Copy file to attachments directory with wiki-specific subdirectory
                File wikiDir = new File(attachmentsDir, "wiki-" + currentWiki.getId());
                wikiDir.mkdirs();

                File targetFile = new File(wikiDir, selectedFile.getName());
                Files.copy(selectedFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // Create attachment record
                WikiFileAttachment attachment = new WikiFileAttachment();
                attachment.setWikiId(currentWiki.getId());
                attachment.setFileName(selectedFile.getName());
                attachment.setFilePath(targetFile.getAbsolutePath());
                attachment.setFileSize(selectedFile.length());
                attachment.setFileType(Files.probeContentType(selectedFile.toPath()));

                attachmentRepository.save(attachment);

                // Refresh the file list
                loadFiles(currentWiki);

                // Show success message
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("File Added");
                alert.setHeaderText("File attached successfully");
                alert.setContentText(selectedFile.getName() + " has been attached to this wiki.");
                alert.showAndWait();

            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Failed to attach file");
                alert.setContentText("An error occurred while copying the file: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    private void handleOpenFile(WikiFileAttachment attachment) {
        File file = new File(attachment.getFilePath());

        if (!file.exists()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("File Not Found");
            alert.setHeaderText("Cannot open file");
            alert.setContentText("The file " + attachment.getFileName() + " could not be found.");
            alert.showAndWait();
            return;
        }

        try {
            Desktop.getDesktop().open(file);
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to open file");
            alert.setContentText("An error occurred while opening the file: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void handleDeleteFile(WikiFileAttachment attachment) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete File");
        confirm.setHeaderText("Delete " + attachment.getFileName() + "?");
        confirm.setContentText(
                "This will remove the file attachment from the wiki. The file will also be deleted from disk.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Delete file from disk
                    File file = new File(attachment.getFilePath());
                    if (file.exists()) {
                        file.delete();
                    }

                    // Delete from database
                    attachmentRepository.delete(attachment);

                    // Refresh the file list
                    loadFiles(currentWiki);

                } catch (Exception e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Failed to delete file");
                    alert.setContentText("An error occurred: " + e.getMessage());
                    alert.showAndWait();
                }
            }
        });
    }

    public void refresh() {
        if (currentWiki != null) {
            loadFiles(currentWiki);
        }
    }
}
