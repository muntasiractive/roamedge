package com.roam.view;

import com.roam.util.AnimationUtils;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.boxicons.BoxiconsRegular;
import org.kordamp.ikonli.boxicons.BoxiconsSolid;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

/**
 * Assets View - File browser and preview component.
 * <p>
 * This view provides an IDE-like file browser with integrated media preview
 * capabilities.
 * It supports browsing folders and previewing various file types including:
 * </p>
 * <ul>
 * <li>Markdown files (.md)</li>
 * <li>Images (.jpg, .png, .gif, .bmp)</li>
 * <li>Videos (.mp4, .mkv)</li>
 * <li>Audio files (.mp3, .wav)</li>
 * <li>PDF documents (.pdf)</li>
 * </ul>
 * <p>
 * The view features a tree-based folder navigation panel and a content preview
 * area
 * with support for filtering, sorting, and date-based grouping of files.
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public class AssetsView extends HBox {

    private static final Logger logger = LoggerFactory.getLogger(AssetsView.class);
    private static final String PREF_LAST_FOLDER = "assets.last.folder";

    // Supported file extensions
    private static final Set<String> IMAGE_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp");
    private static final Set<String> VIDEO_EXTENSIONS = Set.of(".mp4", ".mkv", ".avi", ".mov", ".webm");
    private static final Set<String> AUDIO_EXTENSIONS = Set.of(".mp3", ".wav", ".ogg", ".flac", ".aac");
    private static final Set<String> DOCUMENT_EXTENSIONS = Set.of(".md", ".txt", ".pdf");

    // Filter types
    private enum FilterType {
        ALL, DOCUMENTS, IMAGES, VIDEOS
    }

    // UI Components
    private VBox sidebar;
    private StackPane contentArea;
    private VBox mainContent;
    private FlowPane fileCardsPane;
    private VBox emptyState;
    private TextField searchField;
    private ToggleGroup filterGroup;

    // State
    private File currentFolder;
    private MediaPlayer currentMediaPlayer;
    private Stage viewerStage;
    private Preferences prefs;
    private FilterType currentFilter = FilterType.ALL;
    private List<File> allFiles = new ArrayList<>();

    public AssetsView() {
        this.prefs = Preferences.userNodeForPackage(AssetsView.class);
        initialize();

        // Try to restore last opened folder
        String lastFolder = prefs.get(PREF_LAST_FOLDER, null);
        if (lastFolder != null) {
            File folder = new File(lastFolder);
            if (folder.exists() && folder.isDirectory()) {
                Platform.runLater(() -> openFolder(folder));
            }
        }
    }

    private void initialize() {
        getStyleClass().add("assets-view");
        setStyle("-fx-background-color: -roam-bg-secondary;");
        setPadding(new Insets(20));
        setSpacing(20);

        // Create sidebar
        sidebar = createSidebar();
        sidebar.setMinWidth(220);
        sidebar.setMaxWidth(220);

        // Create content area
        contentArea = createContentArea();
        HBox.setHgrow(contentArea, Priority.ALWAYS);

        getChildren().addAll(sidebar, contentArea);

        // Animate on load
        Platform.runLater(() -> AnimationUtils.fadeIn(this, Duration.millis(300)));
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(16);
        sidebar.getStyleClass().add("assets-sidebar");

        // Open Folder Button
        Button openFolderBtn = new Button("Open Folder");
        openFolderBtn.getStyleClass().addAll("btn", "btn-primary");
        openFolderBtn.setMaxWidth(Double.MAX_VALUE);
        openFolderBtn.setPrefHeight(42);
        FontIcon uploadIcon = new FontIcon(Feather.UPLOAD);
        uploadIcon.setIconSize(16);
        openFolderBtn.setGraphic(uploadIcon);
        openFolderBtn.setOnAction(e -> showFolderChooser());

        // Library Section
        Label libraryLabel = new Label("LIBRARY");
        libraryLabel.getStyleClass().add("assets-library-label");

        // Filter Toggle Buttons
        filterGroup = new ToggleGroup();

        VBox filterButtons = new VBox(2);
        filterButtons.getChildren().addAll(
                createFilterButton("All Files", BoxiconsSolid.FOLDER_MINUS, FilterType.ALL, true),
                createFilterButton("Documents", BoxiconsSolid.FILE_BLANK, FilterType.DOCUMENTS, false),
                createFilterButton("Images", BoxiconsRegular.IMAGE_ALT, FilterType.IMAGES, false),
                createFilterButton("Videos", BoxiconsSolid.VIDEO, FilterType.VIDEOS, false));

        sidebar.getChildren().addAll(openFolderBtn, libraryLabel, filterButtons);

        return sidebar;
    }

    private ToggleButton createFilterButton(String text, Ikon icon, FilterType filterType, boolean selected) {
        ToggleButton btn = new ToggleButton(text);
        btn.setToggleGroup(filterGroup);
        btn.setSelected(selected);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(10, 12, 10, 12));

        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconSize(16);
        fontIcon.getStyleClass().add("assets-filter-icon");
        btn.setGraphic(fontIcon);

        btn.getStyleClass().add("assets-filter-button");

        btn.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                btn.getStyleClass().add("selected");
                fontIcon.getStyleClass().add("selected");
                currentFilter = filterType;
                applyFilter();
            } else {
                btn.getStyleClass().remove("selected");
                fontIcon.getStyleClass().remove("selected");
            }
        });

        // Initial selection styling
        if (selected) {
            btn.getStyleClass().add("selected");
            fontIcon.getStyleClass().add("selected");
        }

        return btn;
    }

    private StackPane createContentArea() {
        StackPane contentPane = new StackPane();
        contentPane.setStyle("-fx-background-color: transparent;");

        mainContent = new VBox(0);
        mainContent.setAlignment(Pos.TOP_CENTER);
        mainContent.setStyle("-fx-background-color: transparent;");

        // Search bar - always visible
        HBox searchBar = createSearchBar();

        // File cards container wrapped in StackPane for empty state overlay
        StackPane cardsArea = new StackPane();
        VBox.setVgrow(cardsArea, Priority.ALWAYS);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        fileCardsPane = new FlowPane();
        fileCardsPane.setHgap(16);
        fileCardsPane.setVgap(16);
        fileCardsPane.setPadding(new Insets(20));
        fileCardsPane.setAlignment(Pos.TOP_LEFT);
        fileCardsPane.setStyle("-fx-background-color: transparent;");

        scrollPane.setContent(fileCardsPane);

        // Empty state
        emptyState = createEmptyState();

        // Add scrollPane and emptyState to cardsArea - toggle visibility between them
        cardsArea.getChildren().addAll(scrollPane, emptyState);
        StackPane.setAlignment(emptyState, Pos.CENTER);

        mainContent.getChildren().addAll(searchBar, cardsArea);

        contentPane.getChildren().add(mainContent);

        return contentPane;
    }

    private HBox createSearchBar() {
        HBox searchBar = new HBox(12);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        searchBar.setPadding(new Insets(16, 20, 16, 20));
        searchBar.getStyleClass().add("assets-search-bar");

        FontIcon searchIcon = new FontIcon(Feather.SEARCH);
        searchIcon.setIconSize(18);
        searchIcon.getStyleClass().add("assets-search-icon");

        searchField = new TextField();
        searchField.setPromptText("Search assets...");
        searchField.getStyleClass().add("assets-search-field");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter());

        searchBar.getChildren().addAll(searchIcon, searchField);

        return searchBar;
    }

    private VBox createEmptyState() {
        VBox container = new VBox(20);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(60));
        container.setMaxWidth(500);
        container.getStyleClass().add("empty-state");

        FontIcon icon = new FontIcon(Feather.FOLDER);
        icon.setIconSize(72);
        icon.getStyleClass().add("icon");

        Label titleLabel = new Label("No Folder Selected");
        titleLabel.getStyleClass().add("title");

        Label descLabel = new Label("Open a folder to browse and preview your files.");
        descLabel.setWrapText(true);
        descLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        descLabel.getStyleClass().add("description");

        Button openBtn = new Button("Open Folder");
        openBtn.getStyleClass().addAll("pill-button", "primary");
        FontIcon folderIcon = new FontIcon(Feather.FOLDER_PLUS);
        folderIcon.setIconSize(16);
        openBtn.setGraphic(folderIcon);
        openBtn.setOnAction(e -> showFolderChooser());

        container.getChildren().addAll(icon, titleLabel, descLabel, openBtn);

        return container;
    }

    private void showFolderChooser() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Folder to Browse");

        String lastFolder = prefs.get(PREF_LAST_FOLDER, System.getProperty("user.home"));
        File initialDir = new File(lastFolder);
        if (initialDir.exists() && initialDir.isDirectory()) {
            chooser.setInitialDirectory(initialDir);
        }

        File selectedFolder = chooser.showDialog(getScene().getWindow());
        if (selectedFolder != null) {
            openFolder(selectedFolder);
        }
    }

    private void openFolder(File folder) {
        if (folder == null || !folder.exists() || !folder.isDirectory()) {
            return;
        }

        currentFolder = folder;
        prefs.put(PREF_LAST_FOLDER, folder.getAbsolutePath());

        // Load all files recursively
        allFiles.clear();
        loadFilesRecursively(folder, 3); // Max depth of 3

        applyFilter();

        // Toggle visibility of empty state based on file count
        emptyState.setVisible(allFiles.isEmpty());
        emptyState.setManaged(allFiles.isEmpty());

        logger.info("Opened folder: {} with {} files", folder.getAbsolutePath(), allFiles.size());
    }

    private void loadFilesRecursively(File directory, int depth) {
        if (depth <= 0)
            return;

        try {
            File[] files = directory.listFiles();
            if (files == null)
                return;

            for (File file : files) {
                if (file.isHidden())
                    continue;

                if (file.isFile() && isSupportedFile(file)) {
                    allFiles.add(file);
                } else if (file.isDirectory()) {
                    loadFilesRecursively(file, depth - 1);
                }
            }
        } catch (Exception e) {
            logger.error("Error loading directory: {}", directory.getAbsolutePath(), e);
        }
    }

    private void applyFilter() {
        fileCardsPane.getChildren().clear();

        String searchQuery = searchField.getText().toLowerCase().trim();

        List<File> filtered = allFiles.stream()
                .filter(file -> matchesFilter(file))
                .filter(file -> searchQuery.isEmpty() || file.getName().toLowerCase().contains(searchQuery))
                .sorted(Comparator.comparing(File::getName))
                .collect(Collectors.toList());

        for (File file : filtered) {
            VBox card = createFileCard(file);
            fileCardsPane.getChildren().add(card);
        }

        emptyState.setVisible(filtered.isEmpty() && allFiles.isEmpty());
        emptyState.setManaged(filtered.isEmpty() && allFiles.isEmpty());
    }

    private boolean matchesFilter(File file) {
        String name = file.getName().toLowerCase();

        switch (currentFilter) {
            case DOCUMENTS:
                return DOCUMENT_EXTENSIONS.stream().anyMatch(name::endsWith);
            case IMAGES:
                return IMAGE_EXTENSIONS.stream().anyMatch(name::endsWith);
            case VIDEOS:
                return VIDEO_EXTENSIONS.stream().anyMatch(name::endsWith) ||
                        AUDIO_EXTENSIONS.stream().anyMatch(name::endsWith);
            case ALL:
            default:
                return true;
        }
    }

    private boolean isSupportedFile(File file) {
        String name = file.getName().toLowerCase();
        return IMAGE_EXTENSIONS.stream().anyMatch(name::endsWith) ||
                VIDEO_EXTENSIONS.stream().anyMatch(name::endsWith) ||
                AUDIO_EXTENSIONS.stream().anyMatch(name::endsWith) ||
                DOCUMENT_EXTENSIONS.stream().anyMatch(name::endsWith);
    }

    private VBox createFileCard(File file) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(200);
        card.setPrefHeight(160);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: #F9FAFB; -fx-background-radius: 12; -fx-cursor: hand;");

        // More options button (top right)
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.TOP_RIGHT);

        Button moreBtn = new Button();
        FontIcon moreIcon = new FontIcon(Feather.MORE_VERTICAL);
        moreIcon.setIconSize(14);
        moreIcon.setIconColor(javafx.scene.paint.Color.web("#9CA3AF"));
        moreBtn.setGraphic(moreIcon);
        moreBtn.setStyle("-fx-background-color: transparent; -fx-padding: 4; -fx-cursor: hand;");
        moreBtn.setOnAction(e -> showFileContextMenu(file, moreBtn));

        topBar.getChildren().add(moreBtn);

        // File icon
        StackPane iconContainer = new StackPane();
        iconContainer.setMinSize(48, 48);
        iconContainer.setMaxSize(48, 48);

        FontIcon fileIcon = new FontIcon(getFileIcon(file));
        fileIcon.setIconSize(32);
        fileIcon.setIconColor(javafx.scene.paint.Color.web(getFileIconColor(file)));

        iconContainer.getChildren().add(fileIcon);

        // File name
        Label nameLabel = new Label(truncateFileName(file.getName(), 22));
        nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 500; -fx-text-fill: #1F2937;");
        nameLabel.setWrapText(false);
        nameLabel.setMaxWidth(180);
        Tooltip.install(nameLabel, new Tooltip(file.getName()));

        // File info (size and date)
        HBox infoRow = new HBox(8);
        infoRow.setAlignment(Pos.CENTER);

        Label sizeLabel = new Label(formatFileSize(file.length()));
        sizeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #9CA3AF;");

        Label dateLabel = new Label(getFileDate(file));
        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #9CA3AF;");

        infoRow.getChildren().addAll(sizeLabel, dateLabel);

        card.getChildren().addAll(topBar, iconContainer, nameLabel, infoRow);

        // Hover effects
        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: #F3F4F6; -fx-background-radius: 12; -fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);"));
        card.setOnMouseExited(
                e -> card.setStyle("-fx-background-color: #F9FAFB; -fx-background-radius: 12; -fx-cursor: hand;"));

        // Click to preview
        card.setOnMouseClicked(e -> {
            if (e.getTarget() != moreBtn && !moreBtn.isHover()) {
                showFilePreview(file);
            }
        });

        return card;
    }

    private void showFileContextMenu(File file, Button anchor) {
        ContextMenu menu = new ContextMenu();

        MenuItem openItem = new MenuItem("Open");
        FontIcon openIcon = new FontIcon(Feather.EXTERNAL_LINK);
        openIcon.setIconSize(14);
        openItem.setGraphic(openIcon);
        openItem.setOnAction(e -> openExternally(file));

        MenuItem previewItem = new MenuItem("Preview");
        FontIcon previewIcon = new FontIcon(Feather.EYE);
        previewIcon.setIconSize(14);
        previewItem.setGraphic(previewIcon);
        previewItem.setOnAction(e -> showFilePreview(file));

        MenuItem deleteItem = new MenuItem("Delete");
        FontIcon deleteIcon = new FontIcon(Feather.TRASH_2);
        deleteIcon.setIconSize(14);
        deleteItem.setGraphic(deleteIcon);
        deleteItem.setOnAction(e -> deleteFile(file));

        menu.getItems().addAll(previewItem, openItem, new SeparatorMenuItem(), deleteItem);
        menu.show(anchor, javafx.geometry.Side.BOTTOM, 0, 0);
    }

    private void showFilePreview(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return;
        }

        stopCurrentMedia();
        closeViewerWindow();

        // Create new viewer window
        viewerStage = new Stage();
        viewerStage.initStyle(StageStyle.TRANSPARENT);
        viewerStage.setTitle("File Viewer - " + file.getName());
        viewerStage.setMinWidth(600);
        viewerStage.setMinHeight(450);

        // Root container with shadow padding
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: transparent;");
        root.setPadding(new Insets(20));

        // Main content with shadow
        VBox mainContainer = new VBox(0);
        mainContainer.getStyleClass().add("assets-preview-main");
        VBox.setVgrow(mainContainer, Priority.ALWAYS);

        // Drop shadow effect
        javafx.scene.effect.DropShadow shadow = new javafx.scene.effect.DropShadow();
        shadow.setRadius(25);
        shadow.setOffsetY(6);
        shadow.setColor(Color.rgb(0, 0, 0, 0.20));
        mainContainer.setEffect(shadow);

        String name = file.getName().toLowerCase();

        // Create custom title bar for the window
        HBox titleBar = createViewerTitleBar(file, name.endsWith(".md") || name.endsWith(".txt"));
        mainContainer.getChildren().add(titleBar);

        // Content area
        VBox contentContainer = new VBox(0);
        VBox.setVgrow(contentContainer, Priority.ALWAYS);
        contentContainer.getStyleClass().add("assets-content-container");

        try {
            if (IMAGE_EXTENSIONS.stream().anyMatch(name::endsWith)) {
                createImagePreviewContent(file, contentContainer);
            } else if (VIDEO_EXTENSIONS.stream().anyMatch(name::endsWith)) {
                createVideoPreviewContent(file, contentContainer);
            } else if (AUDIO_EXTENSIONS.stream().anyMatch(name::endsWith)) {
                createAudioPreviewContent(file, contentContainer);
            } else if (name.endsWith(".md") || name.endsWith(".txt")) {
                createMarkdownPreviewContent(file, contentContainer, titleBar);
            } else if (name.endsWith(".pdf")) {
                createPdfPreviewContent(file, contentContainer);
            } else {
                createUnsupportedPreviewContent(file, contentContainer);
            }
        } catch (Exception e) {
            logger.error("Error creating preview: {}", e.getMessage(), e);
            createErrorPreviewContent(e.getMessage(), contentContainer);
        }

        mainContainer.getChildren().add(contentContainer);
        root.getChildren().add(mainContainer);

        // Make window draggable via title bar
        final double[] dragOffset = new double[2];
        titleBar.setOnMousePressed(e -> {
            dragOffset[0] = e.getSceneX();
            dragOffset[1] = e.getSceneY();
        });
        titleBar.setOnMouseDragged(e -> {
            viewerStage.setX(e.getScreenX() - dragOffset[0]);
            viewerStage.setY(e.getScreenY() - dragOffset[1]);
        });

        // Add resize functionality
        addResizeHandlers(root, mainContainer);

        Scene scene = new Scene(root, 900, 650);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().addAll(getScene().getStylesheets());
        viewerStage.setScene(scene);

        // Cleanup on close
        viewerStage.setOnHidden(e -> {
            stopCurrentMedia();
            viewerStage = null;
        });

        viewerStage.show();
        viewerStage.centerOnScreen();
    }

    private void closeViewerWindow() {
        if (viewerStage != null) {
            stopCurrentMedia();
            viewerStage.close();
            viewerStage = null;
        }
    }

    private void addResizeHandlers(StackPane root, VBox mainContainer) {
        final int RESIZE_MARGIN = 8;
        final double[] startSize = new double[4]; // startX, startY, startWidth, startHeight

        root.setOnMouseMoved(e -> {
            double x = e.getX();
            double y = e.getY();
            double width = root.getWidth();
            double height = root.getHeight();

            boolean onRightEdge = x > width - RESIZE_MARGIN - 20;
            boolean onBottomEdge = y > height - RESIZE_MARGIN - 20;
            boolean onLeftEdge = x < RESIZE_MARGIN + 20;
            boolean onTopEdge = y < RESIZE_MARGIN + 20;

            if (onRightEdge && onBottomEdge) {
                root.setCursor(javafx.scene.Cursor.SE_RESIZE);
            } else if (onRightEdge) {
                root.setCursor(javafx.scene.Cursor.E_RESIZE);
            } else if (onBottomEdge) {
                root.setCursor(javafx.scene.Cursor.S_RESIZE);
            } else if (onLeftEdge) {
                root.setCursor(javafx.scene.Cursor.W_RESIZE);
            } else if (onTopEdge) {
                root.setCursor(javafx.scene.Cursor.N_RESIZE);
            } else {
                root.setCursor(javafx.scene.Cursor.DEFAULT);
            }
        });

        root.setOnMousePressed(e -> {
            startSize[0] = e.getScreenX();
            startSize[1] = e.getScreenY();
            startSize[2] = viewerStage.getWidth();
            startSize[3] = viewerStage.getHeight();
        });

        root.setOnMouseDragged(e -> {
            javafx.scene.Cursor cursor = root.getCursor();
            if (cursor == javafx.scene.Cursor.DEFAULT)
                return;

            double deltaX = e.getScreenX() - startSize[0];
            double deltaY = e.getScreenY() - startSize[1];

            if (cursor == javafx.scene.Cursor.SE_RESIZE || cursor == javafx.scene.Cursor.E_RESIZE) {
                double newWidth = Math.max(viewerStage.getMinWidth(), startSize[2] + deltaX);
                viewerStage.setWidth(newWidth);
            }
            if (cursor == javafx.scene.Cursor.SE_RESIZE || cursor == javafx.scene.Cursor.S_RESIZE) {
                double newHeight = Math.max(viewerStage.getMinHeight(), startSize[3] + deltaY);
                viewerStage.setHeight(newHeight);
            }
            if (cursor == javafx.scene.Cursor.W_RESIZE) {
                double newWidth = Math.max(viewerStage.getMinWidth(), startSize[2] - deltaX);
                if (newWidth > viewerStage.getMinWidth()) {
                    viewerStage.setWidth(newWidth);
                    viewerStage.setX(e.getScreenX());
                }
            }
            if (cursor == javafx.scene.Cursor.N_RESIZE) {
                double newHeight = Math.max(viewerStage.getMinHeight(), startSize[3] - deltaY);
                if (newHeight > viewerStage.getMinHeight()) {
                    viewerStage.setHeight(newHeight);
                    viewerStage.setY(e.getScreenY());
                }
            }
        });
    }

    private HBox createViewerTitleBar(File file, boolean isTextFile) {
        HBox titleBar = new HBox(12);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(12, 16, 12, 16));
        titleBar.setStyle(
                "-fx-background-color: #F9FAFB; -fx-background-radius: 12 12 0 0; -fx-border-color: #E5E7EB; -fx-border-width: 0 0 1 0; -fx-cursor: move;");

        // File icon
        FontIcon fileIcon = new FontIcon(getFileIcon(file));
        fileIcon.setIconSize(20);
        fileIcon.setIconColor(Color.web("#6B7280"));

        // File info
        VBox fileInfo = new VBox(1);
        HBox.setHgrow(fileInfo, Priority.ALWAYS);

        Label fileNameLabel = new Label(file.getName());
        fileNameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1F2937;");

        Label fileSizeLabel = new Label(formatFileSize(file.length()) + " • " + getFileDate(file));
        fileSizeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #9CA3AF;");

        fileInfo.getChildren().addAll(fileNameLabel, fileSizeLabel);

        // Window control buttons
        HBox controlsBox = new HBox(4);
        controlsBox.setAlignment(Pos.CENTER_RIGHT);

        Button openExternalBtn = createWindowButton(Feather.EXTERNAL_LINK, "Open External", "#6B7280");
        openExternalBtn.setOnAction(e -> openExternally(file));

        Button minimizeBtn = createWindowButton(Feather.MINUS, "Minimize", "#6B7280");
        minimizeBtn.setOnAction(e -> viewerStage.setIconified(true));

        Button closeBtn = createWindowButton(Feather.X, "Close", "#EF4444");
        closeBtn.setOnAction(e -> closeViewerWindow());

        controlsBox.getChildren().addAll(openExternalBtn, minimizeBtn, closeBtn);

        titleBar.getChildren().addAll(fileIcon, fileInfo, controlsBox);
        return titleBar;
    }

    private Button createWindowButton(Feather icon, String tooltip, String color) {
        Button btn = new Button();
        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconSize(14);
        fontIcon.setIconColor(Color.web(color));
        btn.setGraphic(fontIcon);
        btn.setStyle("-fx-background-color: transparent; -fx-background-radius: 6; -fx-padding: 6; -fx-cursor: hand;");
        Tooltip.install(btn, new Tooltip(tooltip));

        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: #E5E7EB; -fx-background-radius: 6; -fx-padding: 6; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: transparent; -fx-background-radius: 6; -fx-padding: 6; -fx-cursor: hand;"));

        return btn;
    }

    private HBox createPreviewToolbar(File file, boolean isTextFile, TextArea textArea, WebView webView) {
        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(16, 20, 16, 20));
        toolbar.setStyle(
                "-fx-background-color: #F9FAFB; -fx-background-radius: 16 16 0 0; -fx-border-color: #E5E7EB; -fx-border-width: 0 0 1 0;");

        // File info
        VBox fileInfo = new VBox(2);
        HBox.setHgrow(fileInfo, Priority.ALWAYS);

        Label fileNameLabel = new Label(file.getName());
        fileNameLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1F2937;");

        Label fileSizeLabel = new Label(formatFileSize(file.length()) + " • " + getFileDate(file));
        fileSizeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");

        fileInfo.getChildren().addAll(fileNameLabel, fileSizeLabel);

        // Buttons
        HBox buttonsBox = new HBox(8);
        buttonsBox.setAlignment(Pos.CENTER_RIGHT);

        Button openExternalBtn = createToolbarButton(Feather.EXTERNAL_LINK, "Open External");
        openExternalBtn.setOnAction(e -> openExternally(file));

        Button closeBtn = createToolbarButton(Feather.X, "Close");
        closeBtn.setOnAction(e -> {
            stopCurrentMedia();
            // Find and remove the overlay
            contentArea.getChildren().removeIf(node -> node instanceof StackPane &&
                    ((StackPane) node).getChildren().stream().anyMatch(c -> c instanceof VBox));
        });

        buttonsBox.getChildren().addAll(openExternalBtn, closeBtn);

        toolbar.getChildren().addAll(fileInfo, buttonsBox);
        return toolbar;
    }

    private Button createToolbarButton(Feather icon, String tooltip) {
        Button btn = new Button();
        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconSize(16);
        fontIcon.setStyle("-fx-icon-color: #6B7280;");
        btn.setGraphic(fontIcon);
        btn.setStyle("-fx-background-color: transparent; -fx-background-radius: 8; -fx-padding: 8; -fx-cursor: hand;");
        Tooltip.install(btn, new Tooltip(tooltip));

        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: #E5E7EB; -fx-background-radius: 8; -fx-padding: 8; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: transparent; -fx-background-radius: 8; -fx-padding: 8; -fx-cursor: hand;"));

        return btn;
    }

    private void createImagePreviewContent(File file, VBox container) {
        Image image = new Image(file.toURI().toString(), true);
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        // Set fixed max dimensions - don't allow growth
        imageView.setFitWidth(850);
        imageView.setFitHeight(550);

        StackPane imageContainer = new StackPane();
        imageContainer.setPadding(new Insets(20));
        imageContainer.setStyle("-fx-background-color: #F3F4F6; -fx-background-radius: 0 0 12 12;");
        imageContainer.setAlignment(Pos.CENTER);
        VBox.setVgrow(imageContainer, Priority.ALWAYS);

        imageContainer.getChildren().add(imageView);
        container.getChildren().add(imageContainer);
    }

    private void createVideoPreviewContent(File file, VBox container) {
        Media media = new Media(file.toURI().toString());
        currentMediaPlayer = new MediaPlayer(media);

        MediaView mediaView = new MediaView(currentMediaPlayer);
        mediaView.setPreserveRatio(true);

        StackPane videoContainer = new StackPane();
        videoContainer.setStyle("-fx-background-color: #000000; -fx-background-radius: 0 0 12 12;");
        videoContainer.setMinHeight(400);
        VBox.setVgrow(videoContainer, Priority.ALWAYS);

        // Bind media view to container size
        mediaView.fitWidthProperty().bind(videoContainer.widthProperty().subtract(40));
        mediaView.fitHeightProperty().bind(videoContainer.heightProperty().subtract(120));

        VBox controlsOverlay = new VBox(8);
        controlsOverlay.setAlignment(Pos.BOTTOM_CENTER);
        controlsOverlay.setPadding(new Insets(20));
        controlsOverlay.setPickOnBounds(false);

        HBox controls = createEnhancedMediaControls(currentMediaPlayer);
        controlsOverlay.getChildren().add(controls);

        videoContainer.getChildren().addAll(mediaView, controlsOverlay);
        StackPane.setAlignment(mediaView, Pos.CENTER);
        StackPane.setAlignment(controlsOverlay, Pos.BOTTOM_CENTER);

        // Handle media errors
        currentMediaPlayer.setOnError(() -> {
            logger.error("Media player error: {}", currentMediaPlayer.getError().getMessage());
        });

        container.getChildren().add(videoContainer);
    }

    private void createAudioPreviewContent(File file, VBox container) {
        Media media = new Media(file.toURI().toString());
        currentMediaPlayer = new MediaPlayer(media);

        VBox audioContent = new VBox(24);
        audioContent.setAlignment(Pos.CENTER);
        audioContent.setPadding(new Insets(40));
        audioContent.getStyleClass().add("assets-content-container");
        VBox.setVgrow(audioContent, Priority.ALWAYS);

        StackPane visualArea = new StackPane();
        visualArea.setPrefSize(160, 160);
        visualArea.setMaxSize(160, 160);
        visualArea.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #4F46E5, #7C3AED); -fx-background-radius: 100;");

        FontIcon audioIcon = new FontIcon(Feather.MUSIC);
        audioIcon.setIconSize(56);
        audioIcon.setStyle("-fx-icon-color: white;");
        visualArea.getChildren().add(audioIcon);

        Label nameLabel = new Label(file.getName());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1F2937;");
        nameLabel.setWrapText(true);
        nameLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        HBox controls = createEnhancedMediaControls(currentMediaPlayer);

        audioContent.getChildren().addAll(visualArea, nameLabel, controls);
        container.getChildren().add(audioContent);
    }

    private void createMarkdownPreviewContent(File file, VBox container, HBox toolbar) throws IOException {
        String content = Files.readString(file.toPath());

        TextArea textArea = new TextArea(content);
        textArea.setWrapText(true);
        textArea.setStyle("-fx-font-family: 'JetBrains Mono', 'Consolas', monospace; -fx-font-size: 14px;");
        textArea.setVisible(false);
        textArea.setManaged(false);
        VBox.setVgrow(textArea, Priority.ALWAYS);

        WebView webView = new WebView();
        String html = convertMarkdownToHtml(content, file.getName());
        webView.getEngine().loadContent(html);
        VBox.setVgrow(webView, Priority.ALWAYS);

        // Add edit button to toolbar
        Button editBtn = new Button("Edit");
        FontIcon editIcon = new FontIcon(Feather.EDIT_2);
        editIcon.setIconSize(14);
        editBtn.setGraphic(editIcon);
        editBtn.getStyleClass().addAll("btn", "btn-primary");

        final boolean[] isEditing = { false };

        editBtn.setOnAction(e -> {
            if (!isEditing[0]) {
                isEditing[0] = true;
                webView.setVisible(false);
                webView.setManaged(false);
                textArea.setVisible(true);
                textArea.setManaged(true);
                editBtn.setText("Save");
                ((FontIcon) editBtn.getGraphic()).setIconCode(Feather.SAVE);
            } else {
                try {
                    Files.writeString(file.toPath(), textArea.getText());
                    String newHtml = convertMarkdownToHtml(textArea.getText(), file.getName());
                    webView.getEngine().loadContent(newHtml);

                    isEditing[0] = false;
                    textArea.setVisible(false);
                    textArea.setManaged(false);
                    webView.setVisible(true);
                    webView.setManaged(true);
                    editBtn.setText("Edit");
                    ((FontIcon) editBtn.getGraphic()).setIconCode(Feather.EDIT_2);
                } catch (IOException ex) {
                    logger.error("Failed to save file: {}", file.getAbsolutePath(), ex);
                }
            }
        });

        // Insert edit button into the title bar controls
        // Title bar structure: [fileIcon, fileInfo, controlsBox]
        if (toolbar.getChildren().size() >= 3 && toolbar.getChildren().get(2) instanceof HBox) {
            HBox controlsBox = (HBox) toolbar.getChildren().get(2);
            controlsBox.getChildren().add(0, editBtn);
        } else if (toolbar.getChildren().size() >= 2 && toolbar.getChildren().get(1) instanceof HBox) {
            // Fallback for old toolbar structure
            HBox buttonsBox = (HBox) toolbar.getChildren().get(1);
            buttonsBox.getChildren().add(0, editBtn);
        }

        StackPane contentPane = new StackPane();
        contentPane.setStyle("-fx-background-radius: 0 0 12 12;");
        VBox.setVgrow(contentPane, Priority.ALWAYS);
        contentPane.getChildren().addAll(webView, textArea);

        container.getChildren().add(contentPane);
    }

    private void createPdfPreviewContent(File file, VBox container) {
        VBox pdfContent = new VBox(20);
        pdfContent.setAlignment(Pos.CENTER);
        pdfContent.setPadding(new Insets(40));
        pdfContent.getStyleClass().add("assets-content-container");
        VBox.setVgrow(pdfContent, Priority.ALWAYS);

        StackPane iconContainer = new StackPane();
        iconContainer.setPrefSize(100, 100);
        iconContainer.setMaxSize(100, 100);
        iconContainer.setStyle("-fx-background-color: #FEE2E2; -fx-background-radius: 16;");

        FontIcon pdfIcon = new FontIcon(Feather.FILE);
        pdfIcon.setIconSize(40);
        pdfIcon.setStyle("-fx-icon-color: #DC2626;");
        iconContainer.getChildren().add(pdfIcon);

        Label titleLabel = new Label("PDF Document");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1F2937;");

        Label infoLabel = new Label("PDF preview is not available.\nClick below to open in your default PDF viewer.");
        infoLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7280; -fx-text-alignment: center;");
        infoLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        infoLabel.setWrapText(true);

        Button openBtn = new Button("Open in PDF Viewer");
        openBtn.getStyleClass().addAll("btn", "btn-primary");
        FontIcon externalIcon = new FontIcon(Feather.EXTERNAL_LINK);
        externalIcon.setIconSize(16);
        openBtn.setGraphic(externalIcon);
        openBtn.setOnAction(e -> openExternally(file));

        pdfContent.getChildren().addAll(iconContainer, titleLabel, infoLabel, openBtn);
        container.getChildren().add(pdfContent);
    }

    private void createUnsupportedPreviewContent(File file, VBox container) {
        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(40));
        content.getStyleClass().add("assets-content-container");
        VBox.setVgrow(content, Priority.ALWAYS);

        FontIcon icon = new FontIcon(Feather.FILE);
        icon.setIconSize(48);
        icon.setStyle("-fx-icon-color: #9CA3AF;");

        Label titleLabel = new Label("Preview Not Available");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #6B7280;");

        Button openBtn = new Button("Open in External App");
        openBtn.getStyleClass().addAll("btn", "btn-secondary");
        openBtn.setOnAction(e -> openExternally(file));

        content.getChildren().addAll(icon, titleLabel, openBtn);
        container.getChildren().add(content);
    }

    private void createErrorPreviewContent(String message, VBox container) {
        VBox content = new VBox(16);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(40));
        content.getStyleClass().add("assets-content-container");
        VBox.setVgrow(content, Priority.ALWAYS);

        FontIcon icon = new FontIcon(Feather.ALERT_TRIANGLE);
        icon.setIconSize(48);
        icon.setStyle("-fx-icon-color: #EF4444;");

        Label titleLabel = new Label("Error");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1F2937;");

        Label msgLabel = new Label(message);
        msgLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7280;");
        msgLabel.setWrapText(true);

        content.getChildren().addAll(icon, titleLabel, msgLabel);
        container.getChildren().add(content);
    }

    private HBox createEnhancedMediaControls(MediaPlayer player) {
        VBox controlsContainer = new VBox(12);
        controlsContainer.setAlignment(Pos.CENTER);
        controlsContainer.setMaxWidth(500);
        controlsContainer.setPadding(new Insets(16));
        controlsContainer.setStyle("-fx-background-color: rgba(0,0,0,0.7); -fx-background-radius: 12;");

        Label currentTimeLabel = new Label("0:00");
        currentTimeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
        Label totalTimeLabel = new Label("0:00");
        totalTimeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");

        Slider progressSlider = new Slider(0, 100, 0);
        progressSlider.getStyleClass().add("media-progress-slider");
        HBox.setHgrow(progressSlider, Priority.ALWAYS);

        HBox progressRow = new HBox(12);
        progressRow.setAlignment(Pos.CENTER);
        progressRow.getChildren().addAll(currentTimeLabel, progressSlider, totalTimeLabel);

        player.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
            if (!progressSlider.isValueChanging() && player.getTotalDuration() != null
                    && !player.getTotalDuration().isUnknown()) {
                double progress = newTime.toSeconds() / player.getTotalDuration().toSeconds() * 100;
                progressSlider.setValue(progress);
                currentTimeLabel.setText(formatTime(newTime));
            }
        });

        player.setOnReady(() -> {
            if (player.getTotalDuration() != null && !player.getTotalDuration().isUnknown()) {
                totalTimeLabel.setText(formatTime(player.getTotalDuration()));
            }
        });

        HBox buttonsRow = new HBox(16);
        buttonsRow.setAlignment(Pos.CENTER);

        Button rewindBtn = createMediaButton(Feather.SKIP_BACK, "Rewind 10s");
        rewindBtn.setOnAction(e -> {
            Duration current = player.getCurrentTime();
            player.seek(current.subtract(Duration.seconds(10)));
        });

        Button playPauseBtn = createMediaButton(Feather.PLAY, "Play");
        playPauseBtn.setPrefSize(52, 52);
        playPauseBtn.setMinSize(52, 52);
        playPauseBtn.setMaxSize(52, 52);
        playPauseBtn.setStyle(
                "-fx-background-color: #4F46E5; -fx-background-radius: 50; -fx-cursor: hand;");
        ((FontIcon) playPauseBtn.getGraphic()).setIconSize(22);

        // Add hover effect for play/pause button
        playPauseBtn.setOnMouseEntered(e -> playPauseBtn.setStyle(
                "-fx-background-color: #4338CA; -fx-background-radius: 50; -fx-cursor: hand;"));
        playPauseBtn.setOnMouseExited(e -> playPauseBtn.setStyle(
                "-fx-background-color: #4F46E5; -fx-background-radius: 50; -fx-cursor: hand;"));

        playPauseBtn.setOnAction(e -> {
            if (player.getStatus() == MediaPlayer.Status.PLAYING) {
                player.pause();
                ((FontIcon) playPauseBtn.getGraphic()).setIconCode(Feather.PLAY);
            } else {
                player.play();
                ((FontIcon) playPauseBtn.getGraphic()).setIconCode(Feather.PAUSE);
            }
        });

        Button forwardBtn = createMediaButton(Feather.SKIP_FORWARD, "Forward 10s");
        forwardBtn.setOnAction(e -> {
            Duration current = player.getCurrentTime();
            if (player.getTotalDuration() != null) {
                Duration newTime = current.add(Duration.seconds(10));
                if (newTime.lessThan(player.getTotalDuration())) {
                    player.seek(newTime);
                }
            }
        });

        FontIcon volumeIcon = new FontIcon(Feather.VOLUME_2);
        volumeIcon.setIconSize(16);
        volumeIcon.setIconColor(Color.WHITE);

        Slider volumeSlider = new Slider(0, 1, 0.7);
        volumeSlider.setPrefWidth(100);
        player.volumeProperty().bind(volumeSlider.valueProperty());

        // Update volume icon based on level
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double vol = newVal.doubleValue();
            if (vol == 0) {
                volumeIcon.setIconCode(Feather.VOLUME_X);
            } else if (vol < 0.33) {
                volumeIcon.setIconCode(Feather.VOLUME);
            } else if (vol < 0.66) {
                volumeIcon.setIconCode(Feather.VOLUME_1);
            } else {
                volumeIcon.setIconCode(Feather.VOLUME_2);
            }
        });

        HBox volumeBox = new HBox(8);
        volumeBox.setAlignment(Pos.CENTER);
        volumeBox.getChildren().addAll(volumeIcon, volumeSlider);

        buttonsRow.getChildren().addAll(rewindBtn, playPauseBtn, forwardBtn, volumeBox);

        progressSlider.setOnMousePressed(e -> player.pause());
        progressSlider.setOnMouseReleased(e -> {
            if (player.getTotalDuration() != null && !player.getTotalDuration().isUnknown()) {
                player.seek(Duration.seconds(progressSlider.getValue() / 100 * player.getTotalDuration().toSeconds()));
                player.play();
                ((FontIcon) playPauseBtn.getGraphic()).setIconCode(Feather.PAUSE);
            }
        });

        controlsContainer.getChildren().addAll(progressRow, buttonsRow);

        HBox wrapper = new HBox();
        wrapper.setAlignment(Pos.CENTER);
        wrapper.getChildren().add(controlsContainer);
        return wrapper;
    }

    private Button createMediaButton(Feather icon, String tooltip) {
        Button btn = new Button();
        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconSize(18);
        fontIcon.setIconColor(Color.WHITE);
        btn.setGraphic(fontIcon);
        btn.setPrefSize(40, 40);
        btn.setMinSize(40, 40);
        btn.setMaxSize(40, 40);
        btn.setStyle(
                "-fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 50; -fx-cursor: hand;");
        Tooltip.install(btn, new Tooltip(tooltip));

        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: rgba(255,255,255,0.3); -fx-background-radius: 50; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 50; -fx-cursor: hand;"));

        return btn;
    }

    private void deleteFile(File file) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete File");
        confirm.setHeaderText("Delete " + file.getName() + "?");
        confirm.setContentText("This action cannot be undone.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    Files.delete(file.toPath());
                    allFiles.remove(file);
                    applyFilter();
                } catch (IOException ex) {
                    logger.error("Failed to delete file: {}", file.getAbsolutePath(), ex);
                }
            }
        });
    }

    private void openExternally(File file) {
        try {
            java.awt.Desktop.getDesktop().open(file);
        } catch (Exception e) {
            logger.error("Failed to open file externally: {}", file.getAbsolutePath(), e);
        }
    }

    private void stopCurrentMedia() {
        if (currentMediaPlayer != null) {
            currentMediaPlayer.stop();
            currentMediaPlayer.dispose();
            currentMediaPlayer = null;
        }
    }

    private Feather getFileIcon(File file) {
        String name = file.getName().toLowerCase();
        if (IMAGE_EXTENSIONS.stream().anyMatch(name::endsWith))
            return Feather.IMAGE;
        if (VIDEO_EXTENSIONS.stream().anyMatch(name::endsWith))
            return Feather.VIDEO;
        if (AUDIO_EXTENSIONS.stream().anyMatch(name::endsWith))
            return Feather.MUSIC;
        if (name.endsWith(".md") || name.endsWith(".txt"))
            return Feather.FILE_TEXT;
        if (name.endsWith(".pdf"))
            return Feather.FILE;
        return Feather.FILE;
    }

    private String getFileIconColor(File file) {
        String name = file.getName().toLowerCase();
        if (IMAGE_EXTENSIONS.stream().anyMatch(name::endsWith))
            return "#A855F7"; // Purple
        if (VIDEO_EXTENSIONS.stream().anyMatch(name::endsWith))
            return "#EF4444"; // Red
        if (AUDIO_EXTENSIONS.stream().anyMatch(name::endsWith))
            return "#EC4899"; // Pink
        if (name.endsWith(".md") || name.endsWith(".txt"))
            return "#3B82F6"; // Blue
        if (name.endsWith(".pdf"))
            return "#3B82F6"; // Blue
        return "#6B7280"; // Gray
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024)
            return bytes + " B";
        if (bytes < 1024 * 1024)
            return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024)
            return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }

    private String formatTime(Duration duration) {
        int seconds = (int) duration.toSeconds();
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    private String getFileDate(File file) {
        try {
            BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            LocalDate date = attrs.lastModifiedTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (IOException e) {
            return "";
        }
    }

    private String truncateFileName(String name, int maxLength) {
        if (name.length() <= maxLength)
            return name;
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex > 0 && name.length() - dotIndex <= 5) {
            String ext = name.substring(dotIndex);
            String base = name.substring(0, dotIndex);
            int baseMax = maxLength - ext.length() - 3;
            if (baseMax > 0) {
                return base.substring(0, Math.min(base.length(), baseMax)) + "..." + ext;
            }
        }
        return name.substring(0, maxLength - 3) + "...";
    }

    private String convertMarkdownToHtml(String markdown, String title) {
        String html = markdown
                .replaceAll("(?m)^### (.+)$", "<h3>$1</h3>")
                .replaceAll("(?m)^## (.+)$", "<h2>$1</h2>")
                .replaceAll("(?m)^# (.+)$", "<h1>$1</h1>")
                .replaceAll("\\*\\*(.+?)\\*\\*", "<strong>$1</strong>")
                .replaceAll("__(.+?)__", "<strong>$1</strong>")
                .replaceAll("\\*(.+?)\\*", "<em>$1</em>")
                .replaceAll("_(.+?)_", "<em>$1</em>")
                .replaceAll("```([\\s\\S]*?)```", "<pre><code>$1</code></pre>")
                .replaceAll("`(.+?)`", "<code>$1</code>")
                .replaceAll("\\[(.+?)\\]\\((.+?)\\)", "<a href=\"$2\">$1</a>")
                .replaceAll("(?m)^- (.+)$", "<li>$1</li>")
                .replaceAll("(?m)^\\* (.+)$", "<li>$1</li>")
                .replaceAll("(?m)^(.+)$(?!\\s*<)", "<p>$1</p>");

        html = html.replaceAll("(<li>.*</li>\\s*)+", "<ul>$0</ul>");

        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body {
                            font-family: 'Poppins', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
                            line-height: 1.6;
                            padding: 24px;
                            max-width: 800px;
                            margin: 0 auto;
                            color: #1f2937;
                            background: #ffffff;
                        }
                        h1, h2, h3 { color: #111827; margin-top: 24px; }
                        h1 { font-size: 28px; border-bottom: 2px solid #e5e7eb; padding-bottom: 8px; }
                        h2 { font-size: 22px; }
                        h3 { font-size: 18px; }
                        code {
                            background: #f3f4f6;
                            padding: 2px 6px;
                            border-radius: 4px;
                            font-family: 'JetBrains Mono', 'Fira Code', monospace;
                            font-size: 14px;
                        }
                        pre {
                            background: #1f2937;
                            color: #f9fafb;
                            padding: 16px;
                            border-radius: 8px;
                            overflow-x: auto;
                        }
                        pre code { background: none; color: inherit; }
                        a { color: #3b82f6; text-decoration: none; }
                        a:hover { text-decoration: underline; }
                        ul { padding-left: 24px; }
                        li { margin: 4px 0; }
                        p { margin: 12px 0; }
                    </style>
                </head>
                <body>
                    %s
                </body>
                </html>
                """, html);
    }

    public void cleanup() {
        stopCurrentMedia();
    }
}
