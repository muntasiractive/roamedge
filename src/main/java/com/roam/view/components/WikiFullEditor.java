package com.roam.view.components;

import com.roam.controller.WikiController;
import com.roam.model.*;
import com.roam.util.DialogUtils;
import com.roam.util.ExportUtils;
import com.roam.util.MarkdownUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.task.list.items.TaskListItemsExtension;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * A comprehensive wiki editor component with Markdown support and live preview.
 * <p>
 * This component provides a full-featured editor for creating and editing wiki
 * content.
 * It includes a title field, Markdown text editor with syntax support, live
 * HTML preview
 * using CommonMark, banner image support, metadata display, property
 * associations
 * (region, operation, task, event), export capabilities, and favorite marking.
 * The editor supports both edit and preview modes with toggle functionality.
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public class WikiFullEditor extends BorderPane {

    private final WikiController controller;

    private TextField titleField;
    private TextArea editorArea;
    private WebView previewPane;
    private Button favoriteBtn;
    private HBox metadataBar;
    private ToggleGroup editPreviewToggle;
    private Label wordCountLabel;
    private Label charCountLabel;
    private Label updatedLabel;
    private ImageView bannerView;

    // Property fields
    private ComboBox<com.roam.model.Region> regionCombo;
    private ComboBox<Operation> operationCombo;
    private ComboBox<Task> taskCombo;
    private ComboBox<CalendarEvent> eventCombo;
    private VBox propertiesSection;
    private TitledPane propertiesPane;

    private Wiki currentWiki;
    private Runnable onWikiDeletedCallback;

    public WikiFullEditor(WikiController controller) {
        this.controller = controller;

        initializeComponents();
        setupLayout();
        setupListeners();
        showEmptyState();
    }

    /**
     * Set a callback to be invoked when a wiki is deleted.
     */
    public void setOnWikiDeleted(Runnable callback) {
        this.onWikiDeletedCallback = callback;
    }

    private void initializeComponents() {
        // Will be created in respective methods
    }

    private void setupLayout() {
        bannerView = new ImageView();
        bannerView.setFitHeight(200);
        bannerView.setPreserveRatio(true);
        bannerView.fitWidthProperty().bind(widthProperty());
        bannerView.setManaged(false);
        bannerView.setVisible(false);

        HBox header = createWikiHeader();
        createPropertiesSection();
        StackPane centerPane = createCenterPane();
        metadataBar = createMetadataBar();

        VBox topContainer = new VBox(bannerView, header, propertiesPane);
        setTop(topContainer);
        setCenter(centerPane);
        setBottom(metadataBar);
    }

    private void setupListeners() {
        controller.addOnWikiChangedListener(Wiki -> {
            if (Wiki != null) {
                loadWiki(Wiki);
            } else {
                showEmptyState();
            }
        });
    }

    private HBox createWikiHeader() {
        HBox header = new HBox(15);
        header.setPrefHeight(60);
        header.getStyleClass().add("wiki-header");

        // Title field
        titleField = new TextField();
        titleField.setPromptText("Untitled Wiki");
        titleField.getStyleClass().add("wiki-title-field");
        titleField.setPrefWidth(400);
        HBox.setHgrow(titleField, javafx.scene.layout.Priority.ALWAYS);

        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        // Favorite button
        favoriteBtn = new Button();
        favoriteBtn.setGraphic(new FontIcon(Feather.STAR));
        favoriteBtn.setPrefSize(36, 36);
        favoriteBtn.getStyleClass().add("icon-button");
        favoriteBtn.setTooltip(new Tooltip("Add to favorites"));

        // Edit/Preview toggle
        editPreviewToggle = new ToggleGroup();

        ToggleButton editBtn = new ToggleButton("Edit");
        editBtn.setToggleGroup(editPreviewToggle);
        editBtn.setSelected(true);
        editBtn.setPrefSize(80, 36);
        editBtn.getStyleClass().add("toggle-button");
        editBtn.getStyleClass().add("left-pill");

        ToggleButton previewBtn = new ToggleButton("Preview");
        previewBtn.setToggleGroup(editPreviewToggle);
        previewBtn.setPrefSize(80, 36);
        previewBtn.getStyleClass().add("toggle-button");
        previewBtn.getStyleClass().add("right-pill");

        HBox toggleBox = new HBox(0);
        toggleBox.getChildren().addAll(editBtn, previewBtn);

        // More menu
        MenuButton moreBtn = new MenuButton();
        moreBtn.setGraphic(new FontIcon(Feather.MORE_VERTICAL));
        moreBtn.setPrefSize(36, 36);
        moreBtn.getStyleClass().add("icon-button");

        MenuItem duplicateItem = new MenuItem("Duplicate Wiki");
        MenuItem addBannerItem = new MenuItem("Add/Change Banner");
        MenuItem removeBannerItem = new MenuItem("Remove Banner");
        MenuItem exportMdItem = new MenuItem("Export as Markdown");
        MenuItem exportPdfItem = new MenuItem("Export as PDF");
        MenuItem deleteItem = new MenuItem("Delete Wiki");
        deleteItem.getStyleClass().add("danger");

        moreBtn.getItems().addAll(
                duplicateItem,
                new SeparatorMenuItem(),
                addBannerItem,
                removeBannerItem,
                new SeparatorMenuItem(),
                exportMdItem,
                exportPdfItem,
                new SeparatorMenuItem(),
                deleteItem);

        duplicateItem.setOnAction(e -> handleDuplicateWiki());
        addBannerItem.setOnAction(e -> handleAddBanner());
        removeBannerItem.setOnAction(e -> handleRemoveBanner());

        exportMdItem.setOnAction(e -> {
            if (currentWiki != null) {
                ExportUtils.exportWikiToMarkdown(currentWiki, this.getScene().getWindow());
            }
        });

        exportPdfItem.setOnAction(e -> {
            if (currentWiki != null) {
                DialogUtils.showInfo("PDF Export", "Coming Soon", "PDF export feature is coming soon!");
            }
        });

        deleteItem.setOnAction(e -> {
            if (currentWiki != null) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Delete Wiki");
                alert.setHeaderText("Are you sure you want to delete this Wiki?");
                alert.setContentText(currentWiki.getTitle());
                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        controller.deleteWiki(currentWiki);
                        showEmptyState();
                        // Notify sidebar to refresh
                        if (onWikiDeletedCallback != null) {
                            onWikiDeletedCallback.run();
                        }
                    }
                });
            }
        });

        header.getChildren().addAll(titleField, spacer, favoriteBtn, toggleBox, moreBtn);
        return header;
    }

    private StackPane createCenterPane() {
        StackPane pane = new StackPane();

        // Editor area
        editorArea = new TextArea();
        editorArea.setWrapText(true);
        editorArea.setPadding(new Insets(30));
        editorArea.getStyleClass().add("wiki-editor-area");

        // Preview pane
        previewPane = new WebView();
        previewPane.setContextMenuEnabled(false);
        previewPane.setVisible(false);

        pane.getChildren().addAll(editorArea, previewPane);

        // Toggle visibility based on selection
        editPreviewToggle.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                ToggleButton selected = (ToggleButton) newVal;
                if (selected.getText().equals("Edit")) {
                    editorArea.setVisible(true);
                    previewPane.setVisible(false);
                } else {
                    editorArea.setVisible(false);
                    previewPane.setVisible(true);
                    renderMarkdown();
                }
            }
        });

        return pane;
    }

    private HBox createMetadataBar() {
        HBox bar = new HBox(15);
        bar.setPrefHeight(40);
        bar.getStyleClass().add("wiki-metadata-bar");

        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        // Stats
        wordCountLabel = new Label("0 words");
        wordCountLabel.getStyleClass().add("wiki-metadata-label");

        charCountLabel = new Label("0 chars");
        charCountLabel.getStyleClass().add("wiki-metadata-label");

        Label separator = new Label("•");
        separator.getStyleClass().add("wiki-metadata-label");

        updatedLabel = new Label("Updated now");
        updatedLabel.getStyleClass().add("wiki-metadata-label");

        bar.getChildren().addAll(spacer, wordCountLabel, charCountLabel, separator, updatedLabel);
        return bar;
    }

    private void renderMarkdown() {
        if (currentWiki == null || currentWiki.getContent() == null) {
            previewPane.getEngine().loadContent("");
            return;
        }

        // Process wiki-links [[Wiki Title]] before rendering
        String rawContent = currentWiki.getContent();
        String processedContent = MarkdownUtils.processWikiLinks(rawContent);

        // Parse markdown using CommonMark with GFM extensions
        Parser parser = Parser.builder()
                .extensions(Arrays.asList(
                        TablesExtension.create(),
                        StrikethroughExtension.create(),
                        TaskListItemsExtension.create()))
                .build();
        Node document = parser.parse(processedContent);
        HtmlRenderer renderer = HtmlRenderer.builder()
                .extensions(Arrays.asList(
                        TablesExtension.create(),
                        StrikethroughExtension.create(),
                        TaskListItemsExtension.create()))
                .build();
        String markdownHtml = renderer.render(document);

        String html = """
                <!DOCTYPE html>
                <html>
                <head>
                <style>
                body {
                    font-family: 'Segoe UI', sans-serif;
                    font-size: 14px;
                    line-height: 1.6;
                    color: #000000;
                    padding: 30px;
                    max-width: 800px;
                }
                h1 { font-size: 32px; font-weight: bold; margin: 20px 0 10px; }
                h2 { font-size: 24px; font-weight: bold; margin: 18px 0 8px; }
                h3 { font-size: 20px; font-weight: bold; margin: 16px 0 6px; }
                p { margin: 10px 0; }
                code {
                    background-color: #F5F5F5;
                    padding: 2px 6px;
                    border-radius: 3px;
                    font-family: 'Courier New', monospace;
                }
                pre {
                    background-color: #F5F5F5;
                    padding: 12px;
                    border-radius: 6px;
                    overflow-x: auto;
                }
                pre code {
                    background-color: transparent;
                    padding: 0;
                }
                table {
                    border-collapse: collapse;
                    width: 100%;
                    margin: 15px 0;
                }
                th, td {
                    border: 1px solid #ddd;
                    padding: 8px 12px;
                    text-align: left;
                }
                th {
                    background-color: #f0f0f0;
                    font-weight: bold;
                }
                tr:nth-child(even) {
                    background-color: #f9f9f9;
                }
                del {
                    text-decoration: line-through;
                    color: #888;
                }
                ul.contains-task-list {
                    list-style: none;
                    padding-left: 20px;
                }
                input[type='checkbox'] {
                    margin-right: 8px;
                }
                a[href^='Wiki://'] {
                    color: #4285f4;
                    text-decoration: none;
                    border-bottom: 1px dashed #4285f4;
                    cursor: pointer;
                }
                a[href^='Wiki://']:hover {
                    background-color: #E3F2FD;
                }
                </style>
                </head>
                <body>
                """ + markdownHtml + """
                </body>
                </html>
                """;

        final String finalHtml = html;
        previewPane.getEngine().loadContent(html);

        // Intercept wiki-link clicks
        previewPane.getEngine().locationProperty().addListener((obs, oldLoc, newLoc) -> {
            if (newLoc != null && newLoc.startsWith("Wiki://")) {
                String wikiTitle = newLoc.substring(7).replace("%20", " ");
                javafx.application.Platform.runLater(() -> {
                    controller.openWikiByTitle(wikiTitle);
                    // Reload original content to prevent navigation
                    previewPane.getEngine().loadContent(finalHtml);
                });
            }
        });
    }

    public void loadWiki(Wiki Wiki) {
        this.currentWiki = Wiki;

        if (Wiki == null) {
            showEmptyState();
            return;
        }

        // Restore the editor UI if it was replaced by empty state
        StackPane centerPane = (StackPane) getCenter();
        if (centerPane != null && !centerPane.getChildren().contains(editorArea)) {
            centerPane.getChildren().clear();
            centerPane.getChildren().addAll(editorArea, previewPane);
            editorArea.setVisible(true);
            previewPane.setVisible(false);
        }

        // Enable components
        titleField.setDisable(false);
        editorArea.setDisable(false);
        favoriteBtn.setDisable(false);
        propertiesPane.setDisable(false);

        // Set values
        titleField.setText(Wiki.getTitle());
        editorArea.setText(Wiki.getContent());

        // Populate properties
        regionCombo.getItems().setAll(controller.loadAllRegions());
        operationCombo.getItems().setAll(controller.loadAllOperations());
        taskCombo.getItems().setAll(controller.loadAllTasks());
        eventCombo.getItems().setAll(controller.loadAllEvents());

        // Set selected values
        if (Wiki.getRegion() != null) {
            regionCombo.getItems().stream()
                    .filter(r -> r.getName().equals(Wiki.getRegion()))
                    .findFirst()
                    .ifPresent(regionCombo::setValue);
        } else {
            regionCombo.setValue(null);
        }

        if (Wiki.getOperationId() != null) {
            operationCombo.getItems().stream()
                    .filter(op -> op.getId().equals(Wiki.getOperationId()))
                    .findFirst()
                    .ifPresent(operationCombo::setValue);
        } else {
            operationCombo.setValue(null);
        }

        if (Wiki.getTaskId() != null) {
            taskCombo.getItems().stream()
                    .filter(t -> t.getId().equals(Wiki.getTaskId()))
                    .findFirst()
                    .ifPresent(taskCombo::setValue);
        } else {
            taskCombo.setValue(null);
        }

        if (Wiki.getCalendarEventId() != null) {
            eventCombo.getItems().stream()
                    .filter(e -> e.getId().equals(Wiki.getCalendarEventId()))
                    .findFirst()
                    .ifPresent(eventCombo::setValue);
        } else {
            eventCombo.setValue(null);
        }

        // Update favorite button
        updateFavoriteButton();

        // Update banner
        updateBannerDisplay();

        // Update stats
        updateStats();

        // Setup bindings
        titleField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (currentWiki != null) {
                currentWiki.setTitle(newVal);
                controller.scheduleAutoSave();
            }
        });

        editorArea.textProperty().addListener((obs, oldVal, newVal) -> {
            if (currentWiki != null) {
                currentWiki.setContent(newVal);
                controller.scheduleAutoSave();
                updateStats();
            }
        });

        favoriteBtn.setOnAction(e -> {
            if (currentWiki != null) {
                controller.toggleFavorite(currentWiki);
                updateFavoriteButton();
            }
        });
    }

    /**
     * Loads wikis for a specific operation. If wikis exist for the operation,
     * loads the first one. If no wikis exist, creates a new wiki for the operation.
     */
    public void loadWikisForOperation(Operation operation) {
        if (operation == null) {
            showEmptyState();
            return;
        }

        List<Wiki> operationWikis = controller.loadWikisForOperation(operation);

        if (operationWikis.isEmpty()) {
            // Show empty state - let user manually create wiki using "+ New Wiki" button
            showEmptyState();
        } else {
            // Load the first wiki
            loadWiki(operationWikis.get(0));
        }
    }

    // Public methods for external menu access
    public void handleDuplicateWikiFromMenu() {
        handleDuplicateWiki();
    }

    public void handleAddBannerFromMenu() {
        handleAddBanner();
    }

    public void handleRemoveBannerFromMenu() {
        handleRemoveBanner();
    }

    public void handleExportMarkdownFromMenu() {
        if (currentWiki != null) {
            com.roam.util.ExportUtils.exportWikiToMarkdown(currentWiki, getScene().getWindow());
        }
    }

    public void handleExportPdfFromMenu() {
        if (currentWiki != null) {
            com.roam.util.DialogUtils.showInfo("PDF Export", "Coming Soon", "PDF export feature is coming soon!");
        }
    }

    public void handleDeleteWikiFromMenu() {
        if (currentWiki != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Wiki");
            alert.setHeaderText("Are you sure you want to delete this Wiki?");
            alert.setContentText(currentWiki.getTitle());
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    controller.deleteWiki(currentWiki);
                    showEmptyState();
                    // Notify sidebar to refresh
                    if (onWikiDeletedCallback != null) {
                        onWikiDeletedCallback.run();
                    }
                }
            });
        }
    }

    private void updateFavoriteButton() {
        FontIcon starIcon = new FontIcon(Feather.STAR);
        if (currentWiki != null && currentWiki.getIsFavorite()) {
            starIcon.getStyleClass().add("warning");
            favoriteBtn.setGraphic(starIcon);
            favoriteBtn.setTooltip(new Tooltip("Remove from favorites"));
        } else {
            favoriteBtn.setGraphic(starIcon);
            favoriteBtn.setTooltip(new Tooltip("Add to favorites"));
        }
    }

    private void updateStats() {
        if (currentWiki != null) {
            String content = currentWiki.getContent();
            if (content != null) {
                int words = content.trim().isEmpty() ? 0 : content.trim().split("\\s+").length;
                int chars = content.length();
                wordCountLabel.setText(words + " words");
                charCountLabel.setText(chars + " chars");
            }

            if (currentWiki.getUpdatedAt() != null) {
                String formatted = currentWiki.getUpdatedAt().format(
                        DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a"));
                updatedLabel.setText("Updated " + formatted);
            }
        }
    }

    private void showEmptyState() {
        this.currentWiki = null;

        // Disable components
        titleField.setDisable(true);
        titleField.clear();
        editorArea.setDisable(true);
        editorArea.clear();
        favoriteBtn.setDisable(true);
        if (propertiesPane != null) {
            propertiesPane.setDisable(true);
            propertiesPane.setExpanded(false);
        }

        // Show empty message in editor
        VBox emptyBox = new VBox(20);
        emptyBox.setAlignment(Pos.CENTER);
        emptyBox.setPadding(new Insets(100));

        FontIcon icon = new FontIcon(Feather.FILE_TEXT);
        icon.setIconSize(64);
        icon.getStyleClass().add("icon-muted");

        Label title = new Label("Select a Wiki to get started");
        title.getStyleClass().add("wiki-empty-state-title");

        Label subtitle = new Label("Or create a new Wiki");
        subtitle.getStyleClass().add("wiki-empty-state-subtitle");

        Button newBtn = new Button("+ New Wiki");
        newBtn.getStyleClass().add("action-button");
        newBtn.setOnAction(e -> controller.createNewWiki());

        emptyBox.getChildren().addAll(icon, title, subtitle, newBtn);

        // Clear center and show empty state
        StackPane centerPane = (StackPane) getCenter();
        if (centerPane != null) {
            centerPane.getChildren().clear();
            centerPane.getChildren().add(emptyBox);
        }
    }

    private void handleDuplicateWiki() {
        if (currentWiki == null)
            return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Duplicate Wiki");
        alert.setHeaderText("Create a copy of this wiki?");
        alert.setContentText("A duplicate will be created with the title '" + currentWiki.getTitle() + " (Copy)'");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Wiki duplicate = controller.duplicateWiki(currentWiki);
                if (duplicate != null) {
                    loadWiki(duplicate);
                    DialogUtils.showInfo("Success", "Wiki Duplicated",
                            "The wiki has been successfully duplicated. You are now editing the copy.");
                }
            }
        });
    }

    private void handleAddBanner() {
        if (currentWiki == null)
            return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Banner Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));

        File selectedFile = fileChooser.showOpenDialog(getScene().getWindow());
        if (selectedFile != null) {
            String imageUrl = selectedFile.toURI().toString();
            currentWiki.setBannerUrl(imageUrl);
            controller.scheduleAutoSave();
            updateBannerDisplay();
        }
    }

    private void handleRemoveBanner() {
        if (currentWiki == null)
            return;
        currentWiki.setBannerUrl(null);
        controller.scheduleAutoSave();
        updateBannerDisplay();
    }

    private void updateBannerDisplay() {
        if (currentWiki != null && currentWiki.getBannerUrl() != null && !currentWiki.getBannerUrl().isEmpty()) {
            try {
                Image image = new Image(currentWiki.getBannerUrl());
                bannerView.setImage(image);
                bannerView.setManaged(true);
                bannerView.setVisible(true);
            } catch (Exception e) {
                System.err.println("Failed to load banner image: " + e.getMessage());
                bannerView.setManaged(false);
                bannerView.setVisible(false);
            }
        } else {
            bannerView.setImage(null);
            bannerView.setManaged(false);
            bannerView.setVisible(false);
        }
    }

    private void createPropertiesSection() {
        propertiesSection = new VBox(10);
        propertiesSection.setPadding(new Insets(10, 20, 10, 20));
        propertiesSection.getStyleClass().add("wiki-properties-section");

        // Region
        HBox regionRow = createPropertyRow("Region", regionCombo = new ComboBox<>());
        regionCombo.setPromptText("Select Region");
        regionCombo.setPrefWidth(250);
        regionCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(com.roam.model.Region object) {
                return object != null ? object.getName() : "";
            }

            @Override
            public com.roam.model.Region fromString(String string) {
                return null;
            }
        });

        // Operation
        HBox operationRow = createPropertyRow("Operation", operationCombo = new ComboBox<>());
        operationCombo.setPromptText("Select Operation");
        operationCombo.setPrefWidth(250);
        operationCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Operation object) {
                return object != null ? object.getName() : "";
            }

            @Override
            public Operation fromString(String string) {
                return null;
            }
        });

        // Task
        HBox taskRow = createPropertyRow("Task", taskCombo = new ComboBox<>());
        taskCombo.setPromptText("Select Task");
        taskCombo.setPrefWidth(250);
        taskCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Task object) {
                return object != null ? object.getTitle() : "";
            }

            @Override
            public Task fromString(String string) {
                return null;
            }
        });

        // Event
        HBox eventRow = createPropertyRow("Event", eventCombo = new ComboBox<>());
        eventCombo.setPromptText("Select Event");
        eventCombo.setPrefWidth(250);
        eventCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(CalendarEvent object) {
                return object != null ? object.getTitle() : "";
            }

            @Override
            public CalendarEvent fromString(String string) {
                return null;
            }
        });

        propertiesSection.getChildren().addAll(regionRow, operationRow, taskRow, eventRow);

        propertiesPane = new TitledPane("Properties", propertiesSection);
        propertiesPane.setExpanded(false);
        propertiesPane.setAnimated(true);
        propertiesPane.getStyleClass().add("wiki-properties-pane");

        // Listeners for property changes
        regionCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (currentWiki != null && newVal != null) {
                currentWiki.setRegion(newVal.getName()); // Storing region name for now as per Wiki model
                controller.saveCurrentWiki();
            }
        });

        operationCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (currentWiki != null) {
                currentWiki.setOperationId(newVal != null ? newVal.getId() : null);
                controller.saveCurrentWiki();
            }
        });

        taskCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (currentWiki != null) {
                currentWiki.setTaskId(newVal != null ? newVal.getId() : null);
                controller.saveCurrentWiki();
            }
        });

        eventCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (currentWiki != null) {
                currentWiki.setCalendarEventId(newVal != null ? newVal.getId() : null);
                controller.saveCurrentWiki();
            }
        });
    }

    private HBox createPropertyRow(String labelText, Control control) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label(labelText);
        label.getStyleClass().add("wiki-property-label");
        label.setPrefWidth(80);

        row.getChildren().addAll(label, control);
        return row;
    }
}
