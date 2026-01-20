package com.roam.view;

import com.roam.controller.JournalController;
import com.roam.controller.WikiController;
import com.roam.model.JournalTemplate;
import com.roam.model.Settings;
import com.roam.model.WikiTemplate;
import com.roam.service.DataService;
import com.roam.service.SearchService;
import com.roam.service.SecurityContext;
import com.roam.service.SettingsService;
import com.roam.util.AnimationUtils;
import com.roam.util.ExportUtils;
import com.roam.util.ImportUtils;
import com.roam.util.ThemeManager;
import com.roam.util.ThreadPoolManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons;
import org.kordamp.ikonli.dashicons.Dashicons;
import org.kordamp.ikonli.boxicons.BoxiconsRegular;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static com.roam.util.UIConstants.*;

/**
 * Settings view with sidebar navigation - redesigned to match modern UI.
 * <p>
 * This view provides a comprehensive settings interface with sidebar navigation
 * for configuring application preferences including theme, security, data
 * management,
 * journal templates, wiki templates, and import/export functionality. The view
 * follows a modern card-based layout with smooth animations and transitions.
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public class SettingsView extends HBox {

    private final SettingsService settingsService;
    private final SecurityContext securityContext;
    private final DataService dataService;
    private final WikiController wikiController;
    private final JournalController journalController;
    private final com.roam.controller.CalendarController calendarController;
    private final com.roam.controller.OperationsController operationsController;

    private VBox contentArea;
    private String currentSection = "general";
    private ToggleGroup navGroup;

    public SettingsView() {
        this.settingsService = SettingsService.getInstance();
        this.securityContext = SecurityContext.getInstance();
        this.dataService = new DataService();
        this.wikiController = new WikiController();
        this.journalController = new JournalController();
        this.calendarController = new com.roam.controller.CalendarController();
        this.operationsController = new com.roam.controller.OperationsController();
        initialize();
    }

    private void initialize() {
        setStyle("-fx-background-color: -roam-bg-primary;");
        getStyleClass().add("settings-view");

        // Create sidebar
        VBox sidebar = createSidebar();

        // Create content area
        contentArea = new VBox();
        contentArea.setPadding(new Insets(40));
        contentArea.setSpacing(32);
        contentArea.setStyle("-fx-background-color: -roam-bg-primary;");
        HBox.setHgrow(contentArea, Priority.ALWAYS);

        // Wrap content in scroll pane
        ScrollPane scrollPane = new ScrollPane(contentArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        HBox.setHgrow(scrollPane, Priority.ALWAYS);

        getChildren().addAll(sidebar, scrollPane);

        // Load initial section
        showSection("general");

        // Animate on load
        Platform.runLater(() -> AnimationUtils.fadeIn(contentArea, Duration.millis(300)));
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(8);
        sidebar.setPrefWidth(240);
        sidebar.setMinWidth(240);
        sidebar.setPadding(new Insets(24, 16, 24, 16));
        sidebar.getStyleClass().add("settings-sidebar");

        navGroup = new ToggleGroup();

        // Navigation items with icons
        ToggleButton generalBtn = createNavButton("Preferences", Feather.SLIDERS, "general");
        ToggleButton timeBtn = createNavButton("Language & Time", Feather.CLOCK, "time");
        ToggleButton securityBtn = createNavButton("Security", Feather.SHIELD, "security");
        ToggleButton dataBtn = createNavButton("Backup", Feather.DATABASE, "data");

        generalBtn.setSelected(true);
        generalBtn.getStyleClass().add("settings-nav-selected");

        sidebar.getChildren().addAll(generalBtn, timeBtn, securityBtn, dataBtn);

        return sidebar;
    }

    private ToggleButton createNavButton(String label, Feather icon, String section) {
        ToggleButton btn = new ToggleButton(label);
        FontIcon navIcon = new FontIcon(icon);
        navIcon.setIconSize(18);
        btn.setGraphic(navIcon);
        btn.setToggleGroup(navGroup);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.getStyleClass().add("settings-nav-button");
        btn.setUserData(section);

        btn.setOnAction(e -> {
            // Update styles
            navGroup.getToggles().forEach(t -> {
                ((ToggleButton) t).getStyleClass().remove("settings-nav-selected");
            });
            btn.getStyleClass().add("settings-nav-selected");
            showSection(section);
        });

        return btn;
    }

    private void showSection(String section) {
        currentSection = section;
        contentArea.getChildren().clear();

        switch (section) {
            case "general":
                showGeneralSection();
                break;
            case "time":
                showTimeSection();
                break;
            case "security":
                showSecuritySection();
                break;
            case "data":
                showDataSection();
                break;
        }

        Platform.runLater(() -> AnimationUtils.fadeIn(contentArea, Duration.millis(200)));
    }

    private void showGeneralSection() {
        // Header
        Label header = new Label("Preferences");
        header.setFont(Font.font("Poppins Bold", 28));
        header.setStyle("-fx-text-fill: -roam-text-primary;");

        Settings settings = settingsService.getSettings();

        // Profile Section Card
        VBox profileCard = createSectionCard();

        HBox profileHeader = new HBox(8);
        profileHeader.setAlignment(Pos.CENTER_LEFT);
        FontIcon profileIcon = new FontIcon(Feather.USER);
        profileIcon.setIconSize(18);
        profileIcon.setStyle("-fx-icon-color: -roam-text-secondary;");
        Label profileLabel = new Label("Profile");
        profileLabel.setFont(Font.font("Poppins SemiBold", 16));
        profileLabel.setStyle("-fx-text-fill: -roam-text-primary;");
        profileHeader.getChildren().addAll(profileIcon, profileLabel);

        // Profile Image
        Label imageLabel = new Label("Profile Image");
        imageLabel.setFont(Font.font("Poppins", 13));
        imageLabel.setStyle("-fx-text-fill: -roam-text-secondary;");
        imageLabel.setPadding(new Insets(16, 0, 4, 0));

        HBox imageRow = new HBox(16);
        imageRow.setAlignment(Pos.CENTER_LEFT);

        StackPane avatarPane = new StackPane();
        avatarPane.setMinSize(64, 64);
        avatarPane.setMaxSize(64, 64);
        avatarPane.getStyleClass().add("profile-avatar");

        javafx.scene.shape.Circle avatarCircle = new javafx.scene.shape.Circle(32);
        avatarCircle.setFill(javafx.scene.paint.Color.web("#3b82f6"));

        Label initialsLabel = new Label(getInitials(settings.getUserName()));
        initialsLabel.setStyle("-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;");

        // Check if profile image exists and load it
        String profileImagePath = settings.getProfileImagePath();
        if (profileImagePath != null && !profileImagePath.isEmpty()) {
            java.io.File imgFile = new java.io.File(profileImagePath);
            if (imgFile.exists()) {
                try {
                    javafx.scene.image.Image originalImage = new javafx.scene.image.Image(imgFile.toURI().toString());
                    double size = Math.min(originalImage.getWidth(), originalImage.getHeight());
                    double x = (originalImage.getWidth() - size) / 2;
                    double y = (originalImage.getHeight() - size) / 2;

                    javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView(originalImage);
                    imageView.setViewport(new javafx.geometry.Rectangle2D(x, y, size, size));
                    imageView.setFitWidth(64);
                    imageView.setFitHeight(64);
                    imageView.setPreserveRatio(true);
                    imageView.setSmooth(true);

                    javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(32, 32, 32);
                    imageView.setClip(clip);

                    avatarPane.getChildren().add(imageView);
                } catch (Exception ex) {
                    avatarPane.getChildren().addAll(avatarCircle, initialsLabel);
                }
            } else {
                avatarPane.getChildren().addAll(avatarCircle, initialsLabel);
            }
        } else {
            avatarPane.getChildren().addAll(avatarCircle, initialsLabel);
        }

        Button changeImageBtn = new Button("Change Image");
        changeImageBtn.getStyleClass().addAll("pill-button", "secondary");
        FontIcon imgIcon = new FontIcon(Feather.IMAGE);
        imgIcon.setIconSize(14);
        changeImageBtn.setGraphic(imgIcon);
        changeImageBtn.setOnAction(e -> handleChangeProfileImage(avatarPane, avatarCircle, initialsLabel));

        imageRow.getChildren().addAll(avatarPane, changeImageBtn);

        Label nameLabel = new Label("Display Name");
        nameLabel.setFont(Font.font("Poppins", 13));
        nameLabel.setStyle("-fx-text-fill: -roam-text-secondary;");
        nameLabel.setPadding(new Insets(16, 0, 4, 0));

        TextField nameField = new TextField();
        nameField.setPromptText("Enter your name");
        nameField.setText(settings.getUserName());
        nameField.setMaxWidth(350);
        nameField.getStyleClass().add("settings-input");

        // Auto-save name on focus lost and update initials
        nameField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                settings.setUserName(nameField.getText().trim());
                settingsService.saveSettings();
                initialsLabel.setText(getInitials(nameField.getText().trim()));
            }
        });

        profileCard.getChildren().addAll(profileHeader, imageLabel, imageRow, nameLabel, nameField);

        // Appearance Section Card
        VBox appearanceCard = createSectionCard();

        HBox appearanceHeader = new HBox(8);
        appearanceHeader.setAlignment(Pos.CENTER_LEFT);
        FontIcon themeIcon = new FontIcon(BootstrapIcons.SUN_FILL);
        themeIcon.setIconSize(18);
        themeIcon.setIconColor(javafx.scene.paint.Color.web("#6B7280"));
        Label appearanceLabel = new Label("Appearance");
        appearanceLabel.setFont(Font.font("Poppins SemiBold", 16));
        appearanceLabel.setStyle("-fx-text-fill: -roam-text-primary;");
        appearanceHeader.getChildren().addAll(themeIcon, appearanceLabel);

        // Theme toggle cards
        HBox themeCards = new HBox(16);
        themeCards.setPadding(new Insets(16, 0, 0, 0));

        ToggleGroup themeGroup = new ToggleGroup();
        String currentTheme = settingsService.getSettings().getTheme();

        VBox lightCard = createThemeCard("Light Mode", Feather.SUN, "Light", themeGroup, currentTheme.equals("Light"));
        VBox darkCard = createThemeCard("Dark Mode", Feather.MOON, "Dark", themeGroup, currentTheme.equals("Dark"));
        VBox systemCard = createThemeCard("Follow System", Feather.MONITOR, "System", themeGroup,
                currentTheme.equals("System"));

        themeCards.getChildren().addAll(lightCard, darkCard, systemCard);
        appearanceCard.getChildren().addAll(appearanceHeader, themeCards);

        contentArea.getChildren().addAll(header, profileCard, appearanceCard);
    }

    private VBox createThemeCard(String label, Feather icon, String theme, ToggleGroup group, boolean selected) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(180);
        card.setPrefHeight(100);
        card.setPadding(new Insets(20));
        card.getStyleClass().add("theme-card");
        card.setCursor(javafx.scene.Cursor.HAND);

        if (selected) {
            card.getStyleClass().add("theme-card-selected");
        }

        FontIcon themeIcon = new FontIcon(icon);
        themeIcon.setIconSize(28);
        themeIcon.getStyleClass().add("theme-icon");

        Label themeLabel = new Label(label);
        themeLabel.setFont(Font.font("Poppins SemiBold", 13));
        themeLabel.getStyleClass().add("theme-label");

        card.getChildren().addAll(themeIcon, themeLabel);

        card.setOnMouseClicked(e -> {
            // Deselect all cards
            if (card.getParent() instanceof HBox parent) {
                parent.getChildren().forEach(child -> {
                    if (child instanceof VBox) {
                        child.getStyleClass().remove("theme-card-selected");
                    }
                });
            }
            card.getStyleClass().add("theme-card-selected");
            handleThemeChange(theme);
        });

        return card;
    }

    private void showTimeSection() {
        Label header = new Label("Language & Time");
        header.setFont(Font.font("Poppins Bold", 28));
        header.setStyle("-fx-text-fill: -roam-text-primary;");

        Settings settings = settingsService.getSettings();

        // Week Start Card
        VBox weekCard = createSectionCard();

        HBox weekHeader = new HBox(8);
        weekHeader.setAlignment(Pos.CENTER_LEFT);
        FontIcon weekIcon = new FontIcon(BootstrapIcons.CALENDAR2_WEEK_FILL);
        weekIcon.setIconSize(18);
        weekIcon.setIconColor(javafx.scene.paint.Color.web("#6B7280"));
        Label weekLabel = new Label("Week Settings");
        weekLabel.setFont(Font.font("Poppins SemiBold", 16));
        weekLabel.setStyle("-fx-text-fill: -roam-text-primary;");
        weekHeader.getChildren().addAll(weekIcon, weekLabel);

        Label startDayLabel = new Label("First day of week");
        startDayLabel.setFont(Font.font("Poppins", 13));
        startDayLabel.setStyle("-fx-text-fill: -roam-text-secondary;");
        startDayLabel.setPadding(new Insets(16, 0, 4, 0));

        ComboBox<String> weekStartCombo = new ComboBox<>();
        weekStartCombo.getItems().addAll("Sunday", "Monday");
        weekStartCombo.setValue(settings.getFirstDayOfWeek());
        weekStartCombo.setMaxWidth(200);
        weekStartCombo.getStyleClass().add("settings-combo");

        weekStartCombo.setOnAction(e -> {
            settings.setFirstDayOfWeek(weekStartCombo.getValue());
            settingsService.saveSettings();
        });

        weekCard.getChildren().addAll(weekHeader, startDayLabel, weekStartCombo);

        // Timezone Card
        VBox timezoneCard = createSectionCard();

        HBox tzHeader = new HBox(8);
        tzHeader.setAlignment(Pos.CENTER_LEFT);
        FontIcon tzIcon = new FontIcon(BootstrapIcons.GLOBE);
        tzIcon.setIconSize(18);
        tzIcon.setIconColor(javafx.scene.paint.Color.web("#6B7280"));
        Label tzLabel = new Label("Timezone");
        tzLabel.setFont(Font.font("Poppins SemiBold", 16));
        tzLabel.setStyle("-fx-text-fill: -roam-text-primary;");
        tzHeader.getChildren().addAll(tzIcon, tzLabel);

        // Auto-detect toggle
        HBox autoDetectRow = new HBox();
        autoDetectRow.setAlignment(Pos.CENTER_LEFT);
        autoDetectRow.setPadding(new Insets(16, 0, 0, 0));

        Label autoLabel = new Label("Automatically detect timezone");
        autoLabel.setFont(Font.font("Poppins", 14));
        autoLabel.setStyle("-fx-text-fill: -roam-text-primary;");

        javafx.scene.layout.Region tzSpacer = new javafx.scene.layout.Region();
        HBox.setHgrow(tzSpacer, Priority.ALWAYS);

        CheckBox autoDetectToggle = new CheckBox();
        autoDetectToggle.setSelected(settings.isAutoDetectTimezone());
        autoDetectToggle.getStyleClass().add("settings-toggle");

        autoDetectRow.getChildren().addAll(autoLabel, tzSpacer, autoDetectToggle);

        // Manual timezone selection
        Label tzSelectLabel = new Label("Select timezone manually");
        tzSelectLabel.setFont(Font.font("Poppins", 13));
        tzSelectLabel.setStyle("-fx-text-fill: -roam-text-secondary;");
        tzSelectLabel.setPadding(new Insets(16, 0, 4, 0));

        ComboBox<String> timezoneCombo = new ComboBox<>();
        String[] availableZones = java.util.TimeZone.getAvailableIDs();
        java.util.Arrays.sort(availableZones);
        timezoneCombo.getItems().addAll(availableZones);
        timezoneCombo.setValue(settings.getTimezone());
        timezoneCombo.setMaxWidth(350);
        timezoneCombo.getStyleClass().add("settings-combo");
        timezoneCombo.setDisable(settings.isAutoDetectTimezone());

        // Enable/disable manual selection based on auto-detect
        autoDetectToggle.setOnAction(e -> {
            boolean autoDetect = autoDetectToggle.isSelected();
            settings.setAutoDetectTimezone(autoDetect);
            timezoneCombo.setDisable(autoDetect);
            if (autoDetect) {
                String systemTz = java.util.TimeZone.getDefault().getID();
                settings.setTimezone(systemTz);
                timezoneCombo.setValue(systemTz);
            }
            settingsService.saveSettings();
        });

        timezoneCombo.setOnAction(e -> {
            if (!autoDetectToggle.isSelected()) {
                settings.setTimezone(timezoneCombo.getValue());
                settingsService.saveSettings();
            }
        });

        // Current time display
        Label currentTimeLabel = new Label();
        currentTimeLabel.setFont(Font.font("Poppins", 12));
        currentTimeLabel.getStyleClass().add("text-tertiary");
        currentTimeLabel.setPadding(new Insets(8, 0, 0, 0));

        // Update time display
        java.time.ZoneId zoneId = java.time.ZoneId.of(settings.getTimezone());
        java.time.ZonedDateTime now = java.time.ZonedDateTime.now(zoneId);
        currentTimeLabel
                .setText("Current time: " + now.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss z")));

        timezoneCard.getChildren().addAll(tzHeader, autoDetectRow, tzSelectLabel, timezoneCombo, currentTimeLabel);

        contentArea.getChildren().addAll(header, weekCard, timezoneCard);
    }

    private void showSecuritySection() {
        Label header = new Label("Security");
        header.setFont(Font.font("Poppins Bold", 28));
        header.setStyle("-fx-text-fill: -roam-text-primary;");

        // Lock screen card
        VBox lockCard = createSectionCard();

        HBox lockHeader = new HBox(8);
        lockHeader.setAlignment(Pos.CENTER_LEFT);
        FontIcon lockIcon = new FontIcon(BootstrapIcons.SHIELD_LOCK_FILL);
        lockIcon.setIconSize(18);
        lockIcon.setIconColor(javafx.scene.paint.Color.web("#6B7280"));
        Label lockLabel = new Label("Lock Screen");
        lockLabel.setFont(Font.font("Poppins SemiBold", 16));
        lockLabel.setStyle("-fx-text-fill: -roam-text-primary;");
        lockHeader.getChildren().addAll(lockIcon, lockLabel);

        HBox lockToggleRow = new HBox();
        lockToggleRow.setAlignment(Pos.CENTER_LEFT);
        lockToggleRow.setPadding(new Insets(16, 0, 0, 0));

        Label enableLabel = new Label("Enable Lock Screen");
        enableLabel.setFont(Font.font("Poppins", 14));
        enableLabel.setStyle("-fx-text-fill: -roam-text-primary;");

        javafx.scene.layout.Region toggleSpacer = new javafx.scene.layout.Region();
        HBox.setHgrow(toggleSpacer, Priority.ALWAYS);

        CheckBox lockToggle = new CheckBox();
        lockToggle.setSelected(securityContext.isLockEnabled());
        lockToggle.getStyleClass().add("settings-toggle");
        lockToggle.setOnAction(e -> handleLockToggle(lockToggle));

        lockToggleRow.getChildren().addAll(enableLabel, toggleSpacer, lockToggle);

        Button changePinBtn = new Button("Change PIN");
        FontIcon keyIcon = new FontIcon(BootstrapIcons.KEY);
        keyIcon.setIconSize(14);
        changePinBtn.setGraphic(keyIcon);
        changePinBtn.getStyleClass().addAll("pill-button", "secondary");
        changePinBtn.setOnAction(e -> handleChangePin());
        changePinBtn.disableProperty().bind(lockToggle.selectedProperty().not());
        VBox.setMargin(changePinBtn, new Insets(16, 0, 0, 0));

        lockCard.getChildren().addAll(lockHeader, lockToggleRow, changePinBtn);

        contentArea.getChildren().addAll(header, lockCard);
    }

    private void showDataSection() {
        Label header = new Label("Backup");
        header.setFont(Font.font("Poppins Bold", 28));
        header.setStyle("-fx-text-fill: -roam-text-primary;");

        // Export/Import card
        VBox dataCard = createSectionCard();

        HBox dataHeader = new HBox(8);
        dataHeader.setAlignment(Pos.CENTER_LEFT);
        FontIcon dataIcon = new FontIcon(Dashicons.BACKUP);
        dataIcon.setIconSize(18);
        dataIcon.setIconColor(javafx.scene.paint.Color.web("#6B7280"));
        Label dataLabel = new Label("Backup & Restore");
        dataLabel.setFont(Font.font("Poppins SemiBold", 16));
        dataLabel.setStyle("-fx-text-fill: -roam-text-primary;");
        dataHeader.getChildren().addAll(dataIcon, dataLabel);

        HBox mainButtons = new HBox(12);
        mainButtons.setPadding(new Insets(16, 0, 0, 0));

        Button exportBtn = new Button("Export Data");
        exportBtn.setGraphic(new FontIcon(Feather.DOWNLOAD));
        exportBtn.getStyleClass().addAll("pill-button", "primary");
        exportBtn.setOnAction(e -> handleExport());

        Button importBtn = new Button("Import Data");
        importBtn.setGraphic(new FontIcon(Feather.UPLOAD));
        importBtn.getStyleClass().addAll("pill-button", "secondary");
        importBtn.setOnAction(e -> handleImport());

        mainButtons.getChildren().addAll(exportBtn, importBtn);

        // Warning
        HBox warningBox = new HBox(8);
        warningBox.setAlignment(Pos.CENTER_LEFT);
        warningBox.setPadding(new Insets(12, 0, 0, 0));
        FontIcon warningIcon = new FontIcon(Feather.ALERT_TRIANGLE);
        warningIcon.setIconSize(14);
        warningIcon.setStyle("-fx-icon-color: -roam-yellow;");
        Label warningLabel = new Label("Import will merge with existing data. Backup first!");
        warningLabel.setFont(Font.font("Poppins", 12));
        warningLabel.setStyle("-fx-text-fill: -roam-yellow;");
        warningBox.getChildren().addAll(warningIcon, warningLabel);

        dataCard.getChildren().addAll(dataHeader, mainButtons, warningBox);

        // Search Index card
        VBox indexCard = createSectionCard();

        HBox indexHeader = new HBox(8);
        indexHeader.setAlignment(Pos.CENTER_LEFT);
        FontIcon searchIcon = new FontIcon(Feather.SEARCH);
        searchIcon.setIconSize(18);
        searchIcon.setStyle("-fx-icon-color: -roam-text-secondary;");
        Label indexLabel = new Label("Search Index");
        indexLabel.setFont(Font.font("Poppins SemiBold", 16));
        indexLabel.setStyle("-fx-text-fill: -roam-text-primary;");
        indexHeader.getChildren().addAll(searchIcon, indexLabel);

        Label indexDesc = new Label("Rebuild search index to update search results with all content.");
        indexDesc.setFont(Font.font("Poppins", 13));
        indexDesc.setStyle("-fx-text-fill: -roam-text-secondary;");
        indexDesc.setPadding(new Insets(8, 0, 12, 0));
        indexDesc.setWrapText(true);

        Button rebuildBtn = new Button("Rebuild Search Index");
        rebuildBtn.setGraphic(new FontIcon(Feather.REFRESH_CW));
        rebuildBtn.getStyleClass().addAll("pill-button", "warning");
        rebuildBtn.setOnAction(e -> handleRebuildIndex());

        indexCard.getChildren().addAll(indexHeader, indexDesc, rebuildBtn);

        // Specific exports card
        VBox exportsCard = createSectionCard();

        HBox exportHeader = new HBox(8);
        exportHeader.setAlignment(Pos.CENTER_LEFT);
        FontIcon exportIcon = new FontIcon(BootstrapIcons.DOWNLOAD);
        exportIcon.setIconSize(18);
        exportIcon.setIconColor(javafx.scene.paint.Color.web("#6B7280"));
        Label exportLabel = new Label("Export by Type");
        exportLabel.setFont(Font.font("Poppins SemiBold", 16));
        exportLabel.setStyle("-fx-text-fill: -roam-text-primary;");
        exportHeader.getChildren().addAll(exportIcon, exportLabel);

        FlowPane exportButtons = new FlowPane();
        exportButtons.setHgap(10);
        exportButtons.setVgap(10);
        exportButtons.setPadding(new Insets(12, 0, 0, 0));

        Button exportWikisBtn = createSmallButton("Wikis (MD)", Feather.FILE_TEXT);
        exportWikisBtn.setOnAction(e -> handleExportWikis());

        Button exportEventsBtn = createSmallButton("Events (ICS)", Feather.CALENDAR);
        exportEventsBtn.setOnAction(e -> handleExportEvents());

        Button exportOpsBtn = createSmallButton("Operations (JSON)", Feather.FOLDER);
        exportOpsBtn.setOnAction(e -> handleExportOperations());

        Button exportJournalsBtn = createSmallButton("Journals (MD)", Feather.BOOK);
        exportJournalsBtn.setOnAction(e -> handleExportJournals());

        exportButtons.getChildren().addAll(exportWikisBtn, exportEventsBtn, exportOpsBtn, exportJournalsBtn);
        exportsCard.getChildren().addAll(exportHeader, exportButtons);

        contentArea.getChildren().addAll(header, dataCard, indexCard, exportsCard);
    }

    private VBox createSectionCard() {
        VBox card = new VBox(8);
        card.setPadding(new Insets(24));
        card.getStyleClass().add("settings-section-card");
        return card;
    }

    private Button createSmallButton(String label, Feather icon) {
        Button btn = new Button(label);
        btn.setGraphic(new FontIcon(icon));
        btn.getStyleClass().add("pill-button");
        return btn;
    }

    private void handleLockToggle(CheckBox toggle) {
        boolean isEnabled = toggle.isSelected();
        if (isEnabled) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Set PIN");
            dialog.setHeaderText("Enter a new PIN to enable lock screen");
            dialog.setContentText("PIN (minimum " + SecurityContext.getMinPinLength() + " characters):");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent() && !result.get().isEmpty()) {
                try {
                    securityContext.setPin(result.get());
                    Alert alert = new Alert(Alert.AlertType.INFORMATION,
                            "PIN set successfully! Lock screen enabled.", ButtonType.OK);
                    alert.showAndWait();
                } catch (IllegalArgumentException e) {
                    // Show error if PIN is too short
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK);
                    errorAlert.setTitle("Invalid PIN");
                    errorAlert.showAndWait();
                    toggle.setSelected(false); // Revert toggle
                }
            } else {
                toggle.setSelected(false); // Revert if cancelled
            }
        } else {
            securityContext.disableLock();
        }
    }

    private void handleChangePin() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Change PIN");
        dialog.setHeaderText("Enter new PIN (minimum " + SecurityContext.getMinPinLength() + " characters)");
        dialog.setContentText("New PIN:");

        dialog.showAndWait().ifPresent(newPin -> {
            if (!newPin.isEmpty()) {
                try {
                    securityContext.setPin(newPin);
                    Alert alert = new Alert(Alert.AlertType.INFORMATION,
                            "PIN updated successfully!", ButtonType.OK);
                    alert.showAndWait();
                } catch (IllegalArgumentException e) {
                    // Show error if PIN is too short
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK);
                    errorAlert.setTitle("Invalid PIN");
                    errorAlert.showAndWait();
                }
            }
        });
    }

    private void handleThemeChange(String theme) {
        Settings settings = settingsService.getSettings();
        settings.setTheme(theme);
        settingsService.saveSettings();

        // Apply theme using ThemeManager (handles both AtlantaFX theme and dark class)
        ThemeManager themeManager = ThemeManager.getInstance();
        themeManager.applyTheme(theme);

        // Also update current scene's root
        if (getScene() != null) {
            themeManager.applyDarkClass(getScene().getRoot(), themeManager.isDarkTheme());
        }
    }

    private void handleExport() {
        // Create custom styled confirmation dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initStyle(javafx.stage.StageStyle.TRANSPARENT);
        dialog.initOwner(getScene().getWindow());

        // Create dialog content
        VBox dialogContent = new VBox(20);
        dialogContent.setAlignment(Pos.CENTER);
        dialogContent.setPadding(new Insets(0, 32, 24, 32));

        // Custom title bar
        HBox titleBar = new HBox(12);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(16, 16, 16, 20));

        FontIcon titleIcon = new FontIcon(BoxiconsRegular.EXPORT);
        titleIcon.setIconSize(18);
        titleIcon.setIconColor(javafx.scene.paint.Color.web("#374151"));

        Label titleLabel = new Label("Export Data");
        titleLabel.setFont(Font.font("Poppins SemiBold", 14));
        titleLabel.setStyle("-fx-text-fill: #374151;");

        javafx.scene.layout.Region titleSpacer = new javafx.scene.layout.Region();
        HBox.setHgrow(titleSpacer, Priority.ALWAYS);

        Button closeBtn = new Button();
        FontIcon closeIcon = new FontIcon(Feather.X);
        closeIcon.setIconSize(16);
        closeBtn.setGraphic(closeIcon);
        closeBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> dialog.setResult(ButtonType.CANCEL));

        titleBar.getChildren().addAll(titleIcon, titleLabel, titleSpacer, closeBtn);

        // Question mark icon - bigger and orange
        FontIcon questionIcon = new FontIcon(BootstrapIcons.PATCH_QUESTION_FILL);
        questionIcon.setIconSize(80);
        questionIcon.setIconColor(javafx.scene.paint.Color.web("#f18c1a"));
        VBox.setMargin(questionIcon, new Insets(8, 0, 8, 0));

        // Message text
        Label messageLabel = new Label(
                "This will export all your data (wikis, events,\ntasks, operations, journals) to a JSON file.");
        messageLabel.setFont(Font.font("Poppins", 14));
        messageLabel.setStyle("-fx-text-fill: #374151;");
        messageLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        messageLabel.setWrapText(true);

        // Question text
        Label questionLabel = new Label("Do you want to continue?");
        questionLabel.setFont(Font.font("Poppins", 14));
        questionLabel.setStyle("-fx-text-fill: #374151;");
        VBox.setMargin(questionLabel, new Insets(8, 0, 0, 0));

        // Buttons
        HBox buttonBox = new HBox(16);
        buttonBox.setAlignment(Pos.CENTER);
        VBox.setMargin(buttonBox, new Insets(8, 0, 0, 0));

        Button okBtn = new Button("OK");
        okBtn.setPrefWidth(120);
        okBtn.setPrefHeight(40);
        okBtn.setStyle(
                "-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-background-radius: 20; -fx-font-family: 'Poppins'; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand; -fx-focus-color: transparent; -fx-faint-focus-color: transparent; -fx-border-color: transparent;");
        okBtn.setOnAction(e -> dialog.setResult(ButtonType.OK));

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setPrefWidth(120);
        cancelBtn.setPrefHeight(40);
        cancelBtn.setStyle(
                "-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-background-radius: 20; -fx-font-family: 'Poppins'; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand; -fx-focus-color: transparent; -fx-faint-focus-color: transparent; -fx-border-color: transparent;");
        cancelBtn.setOnAction(e -> dialog.setResult(ButtonType.CANCEL));

        buttonBox.getChildren().addAll(okBtn, cancelBtn);

        dialogContent.getChildren().addAll(questionIcon, messageLabel, questionLabel, buttonBox);

        // Main container with title bar - with drop shadow
        VBox mainContainer = new VBox();
        mainContainer.setStyle(
                "-fx-background-color: white; -fx-background-radius: 16; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 16;");
        mainContainer
                .setEffect(new javafx.scene.effect.DropShadow(30, 0, 8, javafx.scene.paint.Color.rgb(0, 0, 0, 0.15)));
        mainContainer.getChildren().addAll(titleBar, dialogContent);

        // Wrap in StackPane with padding to allow shadow to render
        StackPane shadowContainer = new StackPane(mainContainer);
        shadowContainer.setPadding(new Insets(40));
        shadowContainer.setStyle("-fx-background-color: transparent;");

        dialog.getDialogPane().setContent(shadowContainer);
        dialog.getDialogPane().setStyle("-fx-background-color: transparent; -fx-padding: 0;");
        dialog.getDialogPane().getScene().setFill(javafx.scene.paint.Color.TRANSPARENT);

        // Hide default buttons
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        dialog.getDialogPane().lookupButton(ButtonType.CANCEL).setVisible(false);
        dialog.getDialogPane().lookupButton(ButtonType.CANCEL).setManaged(false);

        Optional<ButtonType> confirmResult = dialog.showAndWait();
        if (confirmResult.isEmpty() || confirmResult.get() != ButtonType.OK) {
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Data");
        fileChooser.setInitialFileName(
                "roam-backup-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmmss"))
                        + ".json");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON Files", "*.json"));

        File file = fileChooser.showSaveDialog(getScene().getWindow());
        if (file != null) {
            // Show custom progress dialog
            Dialog<Void> progressDialog = new Dialog<>();
            progressDialog.initStyle(javafx.stage.StageStyle.TRANSPARENT);
            progressDialog.initOwner(getScene().getWindow());

            VBox progressContent = new VBox(16);
            progressContent.setAlignment(Pos.CENTER_LEFT);
            progressContent.setPadding(new Insets(24));

            HBox progressTitleBar = new HBox(12);
            progressTitleBar.setAlignment(Pos.CENTER_LEFT);
            FontIcon progressTitleIcon = new FontIcon(BoxiconsRegular.EXPORT);
            progressTitleIcon.setIconSize(18);
            progressTitleIcon.setIconColor(javafx.scene.paint.Color.web("#374151"));
            Label progressTitleLabel = new Label("Exporting Data");
            progressTitleLabel.setFont(Font.font("Poppins SemiBold", 14));
            progressTitleLabel.setStyle("-fx-text-fill: #374151;");
            javafx.scene.layout.Region progressTitleSpacer = new javafx.scene.layout.Region();
            HBox.setHgrow(progressTitleSpacer, Priority.ALWAYS);
            Button progressCloseBtn = new Button();
            FontIcon progressCloseIcon = new FontIcon(Feather.X);
            progressCloseIcon.setIconSize(16);
            progressCloseBtn.setGraphic(progressCloseIcon);
            progressCloseBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
            progressTitleBar.getChildren().addAll(progressTitleIcon, progressTitleLabel, progressTitleSpacer,
                    progressCloseBtn);

            Label progressHeaderLabel = new Label("Please wait...");
            progressHeaderLabel.setFont(Font.font("Poppins SemiBold", 16));
            progressHeaderLabel.setStyle("-fx-text-fill: #374151;");

            ProgressBar progressBar = new ProgressBar();
            progressBar.setPrefWidth(300);
            progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);

            Label progressMsgLabel = new Label("Exporting your data to JSON file.");
            progressMsgLabel.setFont(Font.font("Poppins", 13));
            progressMsgLabel.setStyle("-fx-text-fill: #6B7280;");

            progressContent.getChildren().addAll(progressTitleBar, progressHeaderLabel, progressBar, progressMsgLabel);

            VBox progressContainer = new VBox();
            progressContainer.setStyle(
                    "-fx-background-color: white; -fx-background-radius: 16; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 16;");
            progressContainer.setEffect(
                    new javafx.scene.effect.DropShadow(30, 0, 8, javafx.scene.paint.Color.rgb(0, 0, 0, 0.15)));
            progressContainer.getChildren().add(progressContent);

            StackPane progressShadowContainer = new StackPane(progressContainer);
            progressShadowContainer.setPadding(new Insets(40));
            progressShadowContainer.setStyle("-fx-background-color: transparent;");

            progressDialog.getDialogPane().setContent(progressShadowContainer);
            progressDialog.getDialogPane().setStyle("-fx-background-color: transparent; -fx-padding: 0;");
            progressDialog.getDialogPane().getScene().setFill(javafx.scene.paint.Color.TRANSPARENT);
            progressDialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
            progressDialog.getDialogPane().lookupButton(ButtonType.CANCEL).setVisible(false);
            progressDialog.getDialogPane().lookupButton(ButtonType.CANCEL).setManaged(false);

            progressDialog.show();

            // Create JavaFX Task for background export
            Task<DataService.ExportResult> exportTask = new Task<>() {
                @Override
                protected DataService.ExportResult call() {
                    return dataService.exportData(file);
                }
            };

            // Handle task completion
            exportTask.setOnSucceeded(event -> {
                progressDialog.close();
                DataService.ExportResult result = exportTask.getValue();

                if (result.isSuccess()) {
                    // Create custom success dialog
                    Dialog<ButtonType> successDialog = new Dialog<>();
                    successDialog.initStyle(javafx.stage.StageStyle.TRANSPARENT);
                    successDialog.initOwner(getScene().getWindow());

                    VBox successContent = new VBox(16);
                    successContent.setAlignment(Pos.CENTER_LEFT);
                    successContent.setPadding(new Insets(24));

                    HBox successTitleBar = new HBox(12);
                    successTitleBar.setAlignment(Pos.CENTER_LEFT);
                    FontIcon successTitleIcon = new FontIcon(BoxiconsRegular.EXPORT);
                    successTitleIcon.setIconSize(18);
                    successTitleIcon.setIconColor(javafx.scene.paint.Color.web("#374151"));
                    Label successTitleLabel = new Label("Export Successful");
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
                    successTitleBar.getChildren().addAll(successTitleIcon, successTitleLabel, successTitleSpacer,
                            successCloseBtn);

                    HBox successHeaderRow = new HBox(12);
                    successHeaderRow.setAlignment(Pos.CENTER_LEFT);
                    Label successHeaderLabel = new Label("Data exported successfully!");
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

                    Label successCountLabel = new Label("Exported " + result.getRecordCount() + " records");
                    successCountLabel.setFont(Font.font("Poppins", 14));
                    successCountLabel.setStyle("-fx-text-fill: #374151;");

                    // Saved to badge - green styled
                    Label successMsgLabel = new Label("Saved to: " + result.getFilePath());
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
                } else {
                    showExportErrorDialog("Failed to export data", result.getMessage());
                }
            });

            exportTask.setOnFailed(event -> {
                progressDialog.close();
                showExportErrorDialog("An error occurred during export",
                        exportTask.getException() != null ? exportTask.getException().getMessage()
                                : "An unknown error occurred.");
            });

            // Submit to I/O thread pool
            ThreadPoolManager.getInstance().submitIoTask(exportTask);
        }
    }

    private void showExportErrorDialog(String header, String message) {
        Dialog<ButtonType> errorDialog = new Dialog<>();
        errorDialog.initStyle(javafx.stage.StageStyle.TRANSPARENT);
        errorDialog.initOwner(getScene().getWindow());

        VBox errorContent = new VBox(16);
        errorContent.setAlignment(Pos.CENTER_LEFT);
        errorContent.setPadding(new Insets(24));

        HBox errorTitleBar = new HBox(12);
        errorTitleBar.setAlignment(Pos.CENTER_LEFT);
        FontIcon errorTitleIcon = new FontIcon(BoxiconsRegular.EXPORT);
        errorTitleIcon.setIconSize(18);
        errorTitleIcon.setIconColor(javafx.scene.paint.Color.web("#374151"));
        Label errorTitleLabel = new Label("Export Failed");
        errorTitleLabel.setFont(Font.font("Poppins SemiBold", 14));
        errorTitleLabel.setStyle("-fx-text-fill: #374151;");
        javafx.scene.layout.Region errorTitleSpacer = new javafx.scene.layout.Region();
        HBox.setHgrow(errorTitleSpacer, Priority.ALWAYS);
        Button errorCloseBtn = new Button();
        FontIcon errorCloseIcon = new FontIcon(Feather.X);
        errorCloseIcon.setIconSize(16);
        errorCloseBtn.setGraphic(errorCloseIcon);
        errorCloseBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        errorCloseBtn.setOnAction(ev -> errorDialog.setResult(ButtonType.OK));
        errorTitleBar.getChildren().addAll(errorTitleIcon, errorTitleLabel, errorTitleSpacer, errorCloseBtn);

        HBox errorHeaderRow = new HBox(12);
        errorHeaderRow.setAlignment(Pos.CENTER_LEFT);
        Label errorHeaderLabel = new Label(header);
        errorHeaderLabel.setFont(Font.font("Poppins SemiBold", 16));
        errorHeaderLabel.setStyle("-fx-text-fill: #374151;");
        javafx.scene.layout.Region errorHeaderSpacer = new javafx.scene.layout.Region();
        HBox.setHgrow(errorHeaderSpacer, Priority.ALWAYS);
        FontIcon errorIcon = new FontIcon(BootstrapIcons.X_CIRCLE_FILL);
        errorIcon.setIconSize(32);
        errorIcon.setIconColor(javafx.scene.paint.Color.web("#EF4444"));
        errorHeaderRow.getChildren().addAll(errorHeaderLabel, errorHeaderSpacer, errorIcon);

        javafx.scene.layout.Region errorDivider = new javafx.scene.layout.Region();
        errorDivider.setPrefHeight(1);
        errorDivider.setStyle("-fx-background-color: #E5E7EB;");

        Label errorMsgLabel = new Label(message);
        errorMsgLabel.setFont(Font.font("Poppins", 13));
        errorMsgLabel.setStyle("-fx-text-fill: #6B7280;");
        errorMsgLabel.setWrapText(true);

        HBox errorBtnBox = new HBox();
        errorBtnBox.setAlignment(Pos.CENTER_RIGHT);
        Button errorOkBtn = new Button("OK");
        errorOkBtn.setPrefWidth(100);
        errorOkBtn.setPrefHeight(36);
        errorOkBtn.setStyle(
                "-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-family: 'Poppins'; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand; -fx-focus-color: transparent; -fx-faint-focus-color: transparent; -fx-border-color: transparent;");
        errorOkBtn.setOnAction(ev -> errorDialog.setResult(ButtonType.OK));
        errorBtnBox.getChildren().add(errorOkBtn);

        errorContent.getChildren().addAll(errorTitleBar, errorHeaderRow, errorDivider, errorMsgLabel, errorBtnBox);

        VBox errorContainer = new VBox();
        errorContainer.setStyle(
                "-fx-background-color: white; -fx-background-radius: 16; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 16;");
        errorContainer
                .setEffect(new javafx.scene.effect.DropShadow(30, 0, 8, javafx.scene.paint.Color.rgb(0, 0, 0, 0.15)));
        errorContainer.getChildren().add(errorContent);

        StackPane errorShadowContainer = new StackPane(errorContainer);
        errorShadowContainer.setPadding(new Insets(40));
        errorShadowContainer.setStyle("-fx-background-color: transparent;");

        errorDialog.getDialogPane().setContent(errorShadowContainer);
        errorDialog.getDialogPane().setStyle("-fx-background-color: transparent; -fx-padding: 0;");
        errorDialog.getDialogPane().getScene().setFill(javafx.scene.paint.Color.TRANSPARENT);
        errorDialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        errorDialog.getDialogPane().lookupButton(ButtonType.OK).setVisible(false);
        errorDialog.getDialogPane().lookupButton(ButtonType.OK).setManaged(false);

        errorDialog.showAndWait();
    }

    private void handleImport() {
        // Create custom styled confirmation dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initStyle(javafx.stage.StageStyle.TRANSPARENT);
        dialog.initOwner(getScene().getWindow());

        // Create dialog content
        VBox dialogContent = new VBox(20);
        dialogContent.setAlignment(Pos.CENTER);
        dialogContent.setPadding(new Insets(0, 32, 24, 32));

        // Custom title bar
        HBox titleBar = new HBox(12);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(16, 16, 16, 20));

        FontIcon titleIcon = new FontIcon(BoxiconsRegular.IMPORT);
        titleIcon.setIconSize(18);
        titleIcon.setIconColor(javafx.scene.paint.Color.web("#374151"));

        Label titleLabel = new Label("Import Data");
        titleLabel.setFont(Font.font("Poppins SemiBold", 14));
        titleLabel.setStyle("-fx-text-fill: #374151;");

        javafx.scene.layout.Region titleSpacer = new javafx.scene.layout.Region();
        HBox.setHgrow(titleSpacer, Priority.ALWAYS);

        Button closeBtn = new Button();
        FontIcon closeIcon = new FontIcon(Feather.X);
        closeIcon.setIconSize(16);
        closeBtn.setGraphic(closeIcon);
        closeBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> dialog.setResult(ButtonType.CANCEL));

        titleBar.getChildren().addAll(titleIcon, titleLabel, titleSpacer, closeBtn);

        // Question mark icon - bigger and orange
        FontIcon questionIcon = new FontIcon(BootstrapIcons.PATCH_QUESTION_FILL);
        questionIcon.setIconSize(80);
        questionIcon.setIconColor(javafx.scene.paint.Color.web("#f18c1a"));
        VBox.setMargin(questionIcon, new Insets(8, 0, 8, 0));

        // Message text
        Label messageLabel = new Label(
                "This will import data and merge it with your\nexisting data. Duplicates will be skipped.");
        messageLabel.setFont(Font.font("Poppins", 14));
        messageLabel.setStyle("-fx-text-fill: #374151;");
        messageLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        messageLabel.setWrapText(true);

        // Warning badge
        Label warningLabel = new Label("Make sure to backup your data before importing.");
        warningLabel.setFont(Font.font("Poppins", 12));
        warningLabel.setStyle(
                "-fx-background-color: #FEF3C7; -fx-text-fill: #92400E; -fx-padding: 8 16; -fx-background-radius: 8;");

        // Question text
        Label questionLabel = new Label("Do you want to continue?");
        questionLabel.setFont(Font.font("Poppins", 14));
        questionLabel.setStyle("-fx-text-fill: #374151;");
        VBox.setMargin(questionLabel, new Insets(8, 0, 0, 0));

        // Buttons
        HBox buttonBox = new HBox(16);
        buttonBox.setAlignment(Pos.CENTER);
        VBox.setMargin(buttonBox, new Insets(8, 0, 0, 0));

        Button okBtn = new Button("OK");
        okBtn.setPrefWidth(120);
        okBtn.setPrefHeight(40);
        okBtn.setStyle(
                "-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-background-radius: 20; -fx-font-family: 'Poppins'; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand; -fx-focus-color: transparent; -fx-faint-focus-color: transparent; -fx-border-color: transparent;");
        okBtn.setOnAction(e -> dialog.setResult(ButtonType.OK));

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setPrefWidth(120);
        cancelBtn.setPrefHeight(40);
        cancelBtn.setStyle(
                "-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-background-radius: 20; -fx-font-family: 'Poppins'; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand; -fx-focus-color: transparent; -fx-faint-focus-color: transparent; -fx-border-color: transparent;");
        cancelBtn.setOnAction(e -> dialog.setResult(ButtonType.CANCEL));

        buttonBox.getChildren().addAll(okBtn, cancelBtn);

        dialogContent.getChildren().addAll(questionIcon, messageLabel, warningLabel, questionLabel, buttonBox);

        // Main container with title bar - with drop shadow
        VBox mainContainer = new VBox();
        mainContainer.setStyle(
                "-fx-background-color: white; -fx-background-radius: 16; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 16;");
        mainContainer
                .setEffect(new javafx.scene.effect.DropShadow(30, 0, 8, javafx.scene.paint.Color.rgb(0, 0, 0, 0.15)));
        mainContainer.getChildren().addAll(titleBar, dialogContent);

        // Wrap in StackPane with padding to allow shadow to render
        StackPane shadowContainer = new StackPane(mainContainer);
        shadowContainer.setPadding(new Insets(40));
        shadowContainer.setStyle("-fx-background-color: transparent;");

        dialog.getDialogPane().setContent(shadowContainer);
        dialog.getDialogPane().setStyle("-fx-background-color: transparent; -fx-padding: 0;");
        dialog.getDialogPane().getScene().setFill(javafx.scene.paint.Color.TRANSPARENT);

        // Hide default buttons
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        dialog.getDialogPane().lookupButton(ButtonType.CANCEL).setVisible(false);
        dialog.getDialogPane().lookupButton(ButtonType.CANCEL).setManaged(false);

        Optional<ButtonType> confirmResult = dialog.showAndWait();
        if (confirmResult.isEmpty() || confirmResult.get() != ButtonType.OK) {
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Data");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON Files", "*.json"));

        File file = fileChooser.showOpenDialog(getScene().getWindow());
        if (file != null) {
            // Show custom progress dialog
            Dialog<Void> progressDialog = new Dialog<>();
            progressDialog.initStyle(javafx.stage.StageStyle.TRANSPARENT);
            progressDialog.initOwner(getScene().getWindow());

            VBox progressContent = new VBox(16);
            progressContent.setAlignment(Pos.CENTER_LEFT);
            progressContent.setPadding(new Insets(24));

            HBox progressTitleBar = new HBox(12);
            progressTitleBar.setAlignment(Pos.CENTER_LEFT);
            FontIcon progressTitleIcon = new FontIcon(BoxiconsRegular.IMPORT);
            progressTitleIcon.setIconSize(18);
            progressTitleIcon.setIconColor(javafx.scene.paint.Color.web("#374151"));
            Label progressTitleLabel = new Label("Importing Data");
            progressTitleLabel.setFont(Font.font("Poppins SemiBold", 14));
            progressTitleLabel.setStyle("-fx-text-fill: #374151;");
            javafx.scene.layout.Region progressTitleSpacer = new javafx.scene.layout.Region();
            HBox.setHgrow(progressTitleSpacer, Priority.ALWAYS);
            Button progressCloseBtn = new Button();
            FontIcon progressCloseIcon = new FontIcon(Feather.X);
            progressCloseIcon.setIconSize(16);
            progressCloseBtn.setGraphic(progressCloseIcon);
            progressCloseBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
            progressTitleBar.getChildren().addAll(progressTitleIcon, progressTitleLabel, progressTitleSpacer,
                    progressCloseBtn);

            Label progressHeaderLabel = new Label("Please wait...");
            progressHeaderLabel.setFont(Font.font("Poppins SemiBold", 16));
            progressHeaderLabel.setStyle("-fx-text-fill: #374151;");

            ProgressBar progressBar = new ProgressBar();
            progressBar.setPrefWidth(300);
            progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);

            Label progressMsgLabel = new Label("Importing data from JSON file.");
            progressMsgLabel.setFont(Font.font("Poppins", 13));
            progressMsgLabel.setStyle("-fx-text-fill: #6B7280;");

            progressContent.getChildren().addAll(progressTitleBar, progressHeaderLabel, progressBar, progressMsgLabel);

            VBox progressContainer = new VBox();
            progressContainer.setStyle(
                    "-fx-background-color: white; -fx-background-radius: 16; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 16;");
            progressContainer.setEffect(
                    new javafx.scene.effect.DropShadow(30, 0, 8, javafx.scene.paint.Color.rgb(0, 0, 0, 0.15)));
            progressContainer.getChildren().add(progressContent);

            StackPane progressShadowContainer = new StackPane(progressContainer);
            progressShadowContainer.setPadding(new Insets(40));
            progressShadowContainer.setStyle("-fx-background-color: transparent;");

            progressDialog.getDialogPane().setContent(progressShadowContainer);
            progressDialog.getDialogPane().setStyle("-fx-background-color: transparent; -fx-padding: 0;");
            progressDialog.getDialogPane().getScene().setFill(javafx.scene.paint.Color.TRANSPARENT);
            progressDialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
            progressDialog.getDialogPane().lookupButton(ButtonType.CANCEL).setVisible(false);
            progressDialog.getDialogPane().lookupButton(ButtonType.CANCEL).setManaged(false);

            progressDialog.show();

            // Create JavaFX Task for background import
            Task<DataService.ImportResult> importTask = new Task<>() {
                @Override
                protected DataService.ImportResult call() {
                    return dataService.importData(file, true); // true = merge mode
                }
            };

            // Handle task completion
            importTask.setOnSucceeded(event -> {
                progressDialog.close();
                DataService.ImportResult result = importTask.getValue();

                if (result.isSuccess()) {
                    // Create custom success dialog
                    Dialog<ButtonType> successDialog = new Dialog<>();
                    successDialog.initStyle(javafx.stage.StageStyle.TRANSPARENT);
                    successDialog.initOwner(getScene().getWindow());

                    VBox successContent = new VBox(16);
                    successContent.setAlignment(Pos.CENTER_LEFT);
                    successContent.setPadding(new Insets(24));

                    HBox successTitleBar = new HBox(12);
                    successTitleBar.setAlignment(Pos.CENTER_LEFT);
                    FontIcon successTitleIcon = new FontIcon(BoxiconsRegular.IMPORT);
                    successTitleIcon.setIconSize(18);
                    successTitleIcon.setIconColor(javafx.scene.paint.Color.web("#374151"));
                    Label successTitleLabel = new Label("Import Successful");
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
                    successTitleBar.getChildren().addAll(successTitleIcon, successTitleLabel, successTitleSpacer,
                            successCloseBtn);

                    HBox successHeaderRow = new HBox(12);
                    successHeaderRow.setAlignment(Pos.CENTER_LEFT);
                    Label successHeaderLabel = new Label("Data imported successfully!");
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

                    Label successCountLabel = new Label("Imported: " + result.getImportedCount() + " records\nSkipped: "
                            + result.getSkippedCount() + " duplicates");
                    successCountLabel.setFont(Font.font("Poppins", 14));
                    successCountLabel.setStyle("-fx-text-fill: #374151;");

                    Label successMsgLabel = new Label(result.getMessage());
                    successMsgLabel.setFont(Font.font("Poppins", 13));
                    successMsgLabel.setStyle("-fx-text-fill: #6B7280;");
                    successMsgLabel.setWrapText(true);

                    // Restart warning badge
                    Label restartWarningLabel = new Label("Please restart Roam to see the imported data.");
                    restartWarningLabel.setFont(Font.font("Poppins", 12));
                    restartWarningLabel.setStyle(
                            "-fx-background-color: #FEF3C7; -fx-text-fill: #92400E; -fx-padding: 8 16; -fx-background-radius: 8;");

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
                            successCountLabel,
                            successMsgLabel, restartWarningLabel, successBtnBox);

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
                } else {
                    // Create custom error dialog
                    showImportErrorDialog("Failed to import data", result.getMessage());
                }
            });

            importTask.setOnFailed(event -> {
                progressDialog.close();
                showImportErrorDialog("An error occurred during import",
                        importTask.getException() != null ? importTask.getException().getMessage()
                                : "An unknown error occurred.");
            });

            // Submit to I/O thread pool
            ThreadPoolManager.getInstance().submitIoTask(importTask);
        }
    }

    private void showImportErrorDialog(String header, String message) {
        Dialog<ButtonType> errorDialog = new Dialog<>();
        errorDialog.initStyle(javafx.stage.StageStyle.TRANSPARENT);
        errorDialog.initOwner(getScene().getWindow());

        VBox errorContent = new VBox(16);
        errorContent.setAlignment(Pos.CENTER_LEFT);
        errorContent.setPadding(new Insets(24));

        HBox errorTitleBar = new HBox(12);
        errorTitleBar.setAlignment(Pos.CENTER_LEFT);
        FontIcon errorTitleIcon = new FontIcon(BoxiconsRegular.IMPORT);
        errorTitleIcon.setIconSize(18);
        errorTitleIcon.setIconColor(javafx.scene.paint.Color.web("#374151"));
        Label errorTitleLabel = new Label("Import Failed");
        errorTitleLabel.setFont(Font.font("Poppins SemiBold", 14));
        errorTitleLabel.setStyle("-fx-text-fill: #374151;");
        javafx.scene.layout.Region errorTitleSpacer = new javafx.scene.layout.Region();
        HBox.setHgrow(errorTitleSpacer, Priority.ALWAYS);
        Button errorCloseBtn = new Button();
        FontIcon errorCloseIcon = new FontIcon(Feather.X);
        errorCloseIcon.setIconSize(16);
        errorCloseBtn.setGraphic(errorCloseIcon);
        errorCloseBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        errorCloseBtn.setOnAction(ev -> errorDialog.setResult(ButtonType.OK));
        errorTitleBar.getChildren().addAll(errorTitleIcon, errorTitleLabel, errorTitleSpacer, errorCloseBtn);

        HBox errorHeaderRow = new HBox(12);
        errorHeaderRow.setAlignment(Pos.CENTER_LEFT);
        Label errorHeaderLabel = new Label(header);
        errorHeaderLabel.setFont(Font.font("Poppins SemiBold", 16));
        errorHeaderLabel.setStyle("-fx-text-fill: #374151;");
        javafx.scene.layout.Region errorHeaderSpacer = new javafx.scene.layout.Region();
        HBox.setHgrow(errorHeaderSpacer, Priority.ALWAYS);
        FontIcon errorIcon = new FontIcon(BootstrapIcons.X_CIRCLE_FILL);
        errorIcon.setIconSize(32);
        errorIcon.setIconColor(javafx.scene.paint.Color.web("#EF4444"));
        errorHeaderRow.getChildren().addAll(errorHeaderLabel, errorHeaderSpacer, errorIcon);

        javafx.scene.layout.Region errorDivider = new javafx.scene.layout.Region();
        errorDivider.setPrefHeight(1);
        errorDivider.setStyle("-fx-background-color: #E5E7EB;");

        Label errorMsgLabel = new Label(message);
        errorMsgLabel.setFont(Font.font("Poppins", 13));
        errorMsgLabel.setStyle("-fx-text-fill: #6B7280;");
        errorMsgLabel.setWrapText(true);

        HBox errorBtnBox = new HBox();
        errorBtnBox.setAlignment(Pos.CENTER_RIGHT);
        Button errorOkBtn = new Button("OK");
        errorOkBtn.setPrefWidth(100);
        errorOkBtn.setPrefHeight(36);
        errorOkBtn.setStyle(
                "-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-family: 'Poppins'; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand; -fx-focus-color: transparent; -fx-faint-focus-color: transparent; -fx-border-color: transparent;");
        errorOkBtn.setOnAction(ev -> errorDialog.setResult(ButtonType.OK));
        errorBtnBox.getChildren().add(errorOkBtn);

        errorContent.getChildren().addAll(errorTitleBar, errorHeaderRow, errorDivider, errorMsgLabel, errorBtnBox);

        VBox errorContainer = new VBox();
        errorContainer.setStyle(
                "-fx-background-color: white; -fx-background-radius: 16; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 16;");
        errorContainer
                .setEffect(new javafx.scene.effect.DropShadow(30, 0, 8, javafx.scene.paint.Color.rgb(0, 0, 0, 0.15)));
        errorContainer.getChildren().add(errorContent);

        StackPane errorShadowContainer = new StackPane(errorContainer);
        errorShadowContainer.setPadding(new Insets(40));
        errorShadowContainer.setStyle("-fx-background-color: transparent;");

        errorDialog.getDialogPane().setContent(errorShadowContainer);
        errorDialog.getDialogPane().setStyle("-fx-background-color: transparent; -fx-padding: 0;");
        errorDialog.getDialogPane().getScene().setFill(javafx.scene.paint.Color.TRANSPARENT);
        errorDialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        errorDialog.getDialogPane().lookupButton(ButtonType.OK).setVisible(false);
        errorDialog.getDialogPane().lookupButton(ButtonType.OK).setManaged(false);

        errorDialog.showAndWait();
    }

    private void handleRebuildIndex() {
        // Create custom styled dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initStyle(javafx.stage.StageStyle.TRANSPARENT);
        dialog.initOwner(getScene().getWindow());

        // Create dialog content
        VBox dialogContent = new VBox(20);
        dialogContent.setAlignment(Pos.CENTER);
        dialogContent.setPadding(new Insets(0, 32, 24, 32));

        // Custom title bar
        HBox titleBar = new HBox(12);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(16, 16, 16, 20));

        FontIcon titleIcon = new FontIcon(BootstrapIcons.BUILDING);
        titleIcon.setIconSize(18);
        titleIcon.setIconColor(javafx.scene.paint.Color.web("#374151"));

        Label titleLabel = new Label("Rebuild search index");
        titleLabel.setFont(Font.font("Poppins SemiBold", 14));
        titleLabel.setStyle("-fx-text-fill: #374151;");

        javafx.scene.layout.Region titleSpacer = new javafx.scene.layout.Region();
        HBox.setHgrow(titleSpacer, Priority.ALWAYS);

        Button closeBtn = new Button();
        FontIcon closeIcon = new FontIcon(Feather.X);
        closeIcon.setIconSize(16);
        closeBtn.setGraphic(closeIcon);
        closeBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> dialog.setResult(ButtonType.CANCEL));

        titleBar.getChildren().addAll(titleIcon, titleLabel, titleSpacer, closeBtn);

        // Question mark icon - bigger and orange
        FontIcon questionIcon = new FontIcon(BootstrapIcons.PATCH_QUESTION_FILL);
        questionIcon.setIconSize(80);
        questionIcon.setIconColor(javafx.scene.paint.Color.web("#f18c1a"));
        VBox.setMargin(questionIcon, new Insets(8, 0, 8, 0));

        // Message text
        Label messageLabel = new Label("This will reindex all Wikis, Tasks, Operations, Events,\nand Journal entries.");
        messageLabel.setFont(Font.font("Poppins", 14));
        messageLabel.setStyle("-fx-text-fill: #374151;");
        messageLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        messageLabel.setWrapText(true);

        // Warning badge
        Label warningLabel = new Label("This may take a few moments depending on the amount of data.");
        warningLabel.setFont(Font.font("Poppins", 12));
        warningLabel.setStyle(
                "-fx-background-color: #FEF3C7; -fx-text-fill: #92400E; -fx-padding: 8 16; -fx-background-radius: 8;");

        // Question text
        Label questionLabel = new Label("Do you want to continue?");
        questionLabel.setFont(Font.font("Poppins", 14));
        questionLabel.setStyle("-fx-text-fill: #374151;");
        VBox.setMargin(questionLabel, new Insets(8, 0, 0, 0));

        // Buttons
        HBox buttonBox = new HBox(16);
        buttonBox.setAlignment(Pos.CENTER);
        VBox.setMargin(buttonBox, new Insets(8, 0, 0, 0));

        Button okBtn = new Button("OK");
        okBtn.setPrefWidth(120);
        okBtn.setPrefHeight(40);
        okBtn.setStyle(
                "-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-background-radius: 20; -fx-font-family: 'Poppins'; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand; -fx-focus-color: transparent; -fx-faint-focus-color: transparent; -fx-border-color: transparent;");
        okBtn.setOnAction(e -> dialog.setResult(ButtonType.OK));

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setPrefWidth(120);
        cancelBtn.setPrefHeight(40);
        cancelBtn.setStyle(
                "-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-background-radius: 20; -fx-font-family: 'Poppins'; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand; -fx-focus-color: transparent; -fx-faint-focus-color: transparent; -fx-border-color: transparent;");
        cancelBtn.setOnAction(e -> dialog.setResult(ButtonType.CANCEL));

        buttonBox.getChildren().addAll(okBtn, cancelBtn);

        dialogContent.getChildren().addAll(questionIcon, messageLabel, warningLabel, questionLabel, buttonBox);

        // Main container with title bar - with drop shadow
        VBox mainContainer = new VBox();
        mainContainer.setStyle(
                "-fx-background-color: white; -fx-background-radius: 16; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 16;");
        mainContainer
                .setEffect(new javafx.scene.effect.DropShadow(30, 0, 8, javafx.scene.paint.Color.rgb(0, 0, 0, 0.15)));
        mainContainer.getChildren().addAll(titleBar, dialogContent);

        // Wrap in StackPane with padding to allow shadow to render
        StackPane shadowContainer = new StackPane(mainContainer);
        shadowContainer.setPadding(new Insets(40));
        shadowContainer.setStyle("-fx-background-color: transparent;");

        dialog.getDialogPane().setContent(shadowContainer);
        dialog.getDialogPane().setStyle("-fx-background-color: transparent; -fx-padding: 0;");
        dialog.getDialogPane().getScene().setFill(javafx.scene.paint.Color.TRANSPARENT);

        // Hide default buttons
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        dialog.getDialogPane().lookupButton(ButtonType.CANCEL).setVisible(false);
        dialog.getDialogPane().lookupButton(ButtonType.CANCEL).setManaged(false);

        Optional<ButtonType> confirmResult = dialog.showAndWait();
        if (confirmResult.isEmpty() || confirmResult.get() != ButtonType.OK) {
            return;
        }

        // Show custom progress dialog
        Dialog<Void> progressDialog = new Dialog<>();
        progressDialog.initStyle(javafx.stage.StageStyle.TRANSPARENT);
        progressDialog.initOwner(getScene().getWindow());

        VBox progressContent = new VBox(16);
        progressContent.setAlignment(Pos.CENTER_LEFT);
        progressContent.setPadding(new Insets(24));

        HBox progressTitleBar = new HBox(12);
        progressTitleBar.setAlignment(Pos.CENTER_LEFT);
        FontIcon progressTitleIcon = new FontIcon(BootstrapIcons.BUILDING);
        progressTitleIcon.setIconSize(18);
        progressTitleIcon.setIconColor(javafx.scene.paint.Color.web("#374151"));
        Label progressTitleLabel = new Label("Rebuilding Index");
        progressTitleLabel.setFont(Font.font("Poppins SemiBold", 14));
        progressTitleLabel.setStyle("-fx-text-fill: #374151;");
        javafx.scene.layout.Region progressTitleSpacer = new javafx.scene.layout.Region();
        HBox.setHgrow(progressTitleSpacer, Priority.ALWAYS);
        Button progressCloseBtn = new Button();
        FontIcon progressCloseIcon = new FontIcon(Feather.X);
        progressCloseIcon.setIconSize(16);
        progressCloseBtn.setGraphic(progressCloseIcon);
        progressCloseBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        progressTitleBar.getChildren().addAll(progressTitleIcon, progressTitleLabel, progressTitleSpacer,
                progressCloseBtn);

        Label progressHeaderLabel = new Label("Please wait...");
        progressHeaderLabel.setFont(Font.font("Poppins SemiBold", 16));
        progressHeaderLabel.setStyle("-fx-text-fill: #374151;");

        ProgressBar progressBar = new ProgressBar();
        progressBar.setPrefWidth(300);
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);

        Label progressMsgLabel = new Label("Reindexing all content for search.");
        progressMsgLabel.setFont(Font.font("Poppins", 13));
        progressMsgLabel.setStyle("-fx-text-fill: #6B7280;");

        progressContent.getChildren().addAll(progressTitleBar, progressHeaderLabel, progressBar, progressMsgLabel);

        VBox progressContainer = new VBox();
        progressContainer.setStyle(
                "-fx-background-color: white; -fx-background-radius: 16; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 16;");
        progressContainer
                .setEffect(new javafx.scene.effect.DropShadow(30, 0, 8, javafx.scene.paint.Color.rgb(0, 0, 0, 0.15)));
        progressContainer.getChildren().add(progressContent);

        StackPane progressShadowContainer = new StackPane(progressContainer);
        progressShadowContainer.setPadding(new Insets(40));
        progressShadowContainer.setStyle("-fx-background-color: transparent;");

        progressDialog.getDialogPane().setContent(progressShadowContainer);
        progressDialog.getDialogPane().setStyle("-fx-background-color: transparent; -fx-padding: 0;");
        progressDialog.getDialogPane().getScene().setFill(javafx.scene.paint.Color.TRANSPARENT);
        progressDialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        progressDialog.getDialogPane().lookupButton(ButtonType.CANCEL).setVisible(false);
        progressDialog.getDialogPane().lookupButton(ButtonType.CANCEL).setManaged(false);

        progressDialog.show();

        // Create JavaFX Task for background index rebuild
        Task<Integer> rebuildTask = new Task<>() {
            @Override
            protected Integer call() throws Exception {
                SearchService searchService = SearchService.getInstance();

                // Clear existing index
                searchService.clearIndex();

                // Reindex all content
                com.roam.repository.WikiRepository wikiRepo = new com.roam.repository.WikiRepository();
                com.roam.repository.TaskRepository taskRepo = new com.roam.repository.TaskRepository();
                com.roam.repository.OperationRepository opRepo = new com.roam.repository.OperationRepository();
                com.roam.repository.CalendarEventRepository eventRepo = new com.roam.repository.CalendarEventRepository();
                com.roam.repository.JournalEntryRepository journalRepo = new com.roam.repository.JournalEntryRepository();

                int count = 0;

                // Index all wikis
                for (com.roam.model.Wiki wiki : wikiRepo.findAll()) {
                    searchService.indexWiki(
                            wiki.getId(),
                            wiki.getTitle(),
                            wiki.getContent(),
                            wiki.getRegion(),
                            wiki.getOperationId(),
                            wiki.getUpdatedAt());
                    count++;
                }

                // Index all tasks
                for (com.roam.model.Task task : taskRepo.findAll()) {
                    searchService.indexTask(
                            task.getId(),
                            task.getTitle(),
                            task.getDescription(),
                            task.getPriority() != null ? task.getPriority().toString() : null,
                            task.getStatus() != null ? task.getStatus().toString() : null,
                            task.getOperationId(),
                            task.getDueDate());
                    count++;
                }

                // Index all operations
                for (com.roam.model.Operation op : opRepo.findAll()) {
                    searchService.indexOperation(
                            op.getId(),
                            op.getName(),
                            op.getPurpose(),
                            op.getOutcome(),
                            op.getStatus() != null ? op.getStatus().toString() : null,
                            op.getPriority() != null ? op.getPriority().toString() : null);
                    count++;
                }

                // Index all events
                for (com.roam.model.CalendarEvent event : eventRepo.findAll()) {
                    searchService.indexEvent(
                            event.getId(),
                            event.getTitle(),
                            event.getDescription(),
                            event.getStartDateTime(),
                            event.getEndDateTime(),
                            event.getLocation());
                    count++;
                }

                // Index all journal entries
                for (com.roam.model.JournalEntry entry : journalRepo.findAll()) {
                    searchService.indexJournalEntry(
                            entry.getId(),
                            entry.getTitle(),
                            entry.getContent(),
                            entry.getDate() != null ? entry.getDate().toString() : null);
                    count++;
                }

                return count;
            }
        };

        // Handle task completion
        rebuildTask.setOnSucceeded(event -> {
            progressDialog.close();
            int totalCount = rebuildTask.getValue();

            // Create custom success dialog
            Dialog<ButtonType> successDialog = new Dialog<>();
            successDialog.initStyle(javafx.stage.StageStyle.TRANSPARENT);
            successDialog.initOwner(getScene().getWindow());

            VBox successContent = new VBox(16);
            successContent.setAlignment(Pos.CENTER_LEFT);
            successContent.setPadding(new Insets(24));

            HBox successTitleBar = new HBox(12);
            successTitleBar.setAlignment(Pos.CENTER_LEFT);
            FontIcon successTitleIcon = new FontIcon(BootstrapIcons.BUILDING);
            successTitleIcon.setIconSize(18);
            successTitleIcon.setIconColor(javafx.scene.paint.Color.web("#374151"));
            Label successTitleLabel = new Label("Index Rebuilt");
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
            successTitleBar.getChildren().addAll(successTitleIcon, successTitleLabel, successTitleSpacer,
                    successCloseBtn);

            HBox successHeaderRow = new HBox(12);
            successHeaderRow.setAlignment(Pos.CENTER_LEFT);
            Label successHeaderLabel = new Label("Search index rebuilt successfully!");
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

            Label successCountLabel = new Label("Indexed " + totalCount + " items.");
            successCountLabel.setFont(Font.font("Poppins", 14));
            successCountLabel.setStyle("-fx-text-fill: #374151;");

            Label successMsgLabel = new Label("You can now search across all your content.");
            successMsgLabel.setFont(Font.font("Poppins", 13));
            successMsgLabel.setStyle("-fx-text-fill: #6B7280;");

            HBox successBtnBox = new HBox();
            successBtnBox.setAlignment(Pos.CENTER_RIGHT);
            Button successOkBtn = new Button("OK");
            successOkBtn.setPrefWidth(100);
            successOkBtn.setPrefHeight(36);
            successOkBtn.setStyle(
                    "-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-family: 'Poppins'; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand; -fx-focus-color: transparent; -fx-faint-focus-color: transparent; -fx-border-color: transparent;");
            successOkBtn.setOnAction(ev -> successDialog.setResult(ButtonType.OK));
            successBtnBox.getChildren().add(successOkBtn);

            successContent.getChildren().addAll(successTitleBar, successHeaderRow, successDivider, successCountLabel,
                    successMsgLabel, successBtnBox);

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
        });

        rebuildTask.setOnFailed(event -> {
            progressDialog.close();

            // Create custom error dialog
            Dialog<ButtonType> errorDialog = new Dialog<>();
            errorDialog.initStyle(javafx.stage.StageStyle.TRANSPARENT);
            errorDialog.initOwner(getScene().getWindow());

            VBox errorContent = new VBox(16);
            errorContent.setAlignment(Pos.CENTER_LEFT);
            errorContent.setPadding(new Insets(24));

            HBox errorTitleBar = new HBox(12);
            errorTitleBar.setAlignment(Pos.CENTER_LEFT);
            FontIcon errorTitleIcon = new FontIcon(BootstrapIcons.BUILDING);
            errorTitleIcon.setIconSize(18);
            errorTitleIcon.setIconColor(javafx.scene.paint.Color.web("#374151"));
            Label errorTitleLabel = new Label("Rebuild Failed");
            errorTitleLabel.setFont(Font.font("Poppins SemiBold", 14));
            errorTitleLabel.setStyle("-fx-text-fill: #374151;");
            javafx.scene.layout.Region errorTitleSpacer = new javafx.scene.layout.Region();
            HBox.setHgrow(errorTitleSpacer, Priority.ALWAYS);
            Button errorCloseBtn = new Button();
            FontIcon errorCloseIcon = new FontIcon(Feather.X);
            errorCloseIcon.setIconSize(16);
            errorCloseBtn.setGraphic(errorCloseIcon);
            errorCloseBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
            errorCloseBtn.setOnAction(ev -> errorDialog.setResult(ButtonType.OK));
            errorTitleBar.getChildren().addAll(errorTitleIcon, errorTitleLabel, errorTitleSpacer, errorCloseBtn);

            HBox errorHeaderRow = new HBox(12);
            errorHeaderRow.setAlignment(Pos.CENTER_LEFT);
            Label errorHeaderLabel = new Label("Failed to rebuild search index");
            errorHeaderLabel.setFont(Font.font("Poppins SemiBold", 16));
            errorHeaderLabel.setStyle("-fx-text-fill: #374151;");
            javafx.scene.layout.Region errorHeaderSpacer = new javafx.scene.layout.Region();
            HBox.setHgrow(errorHeaderSpacer, Priority.ALWAYS);
            FontIcon errorIcon = new FontIcon(BootstrapIcons.X_CIRCLE_FILL);
            errorIcon.setIconSize(32);
            errorIcon.setIconColor(javafx.scene.paint.Color.web("#EF4444"));
            errorHeaderRow.getChildren().addAll(errorHeaderLabel, errorHeaderSpacer, errorIcon);

            javafx.scene.layout.Region errorDivider = new javafx.scene.layout.Region();
            errorDivider.setPrefHeight(1);
            errorDivider.setStyle("-fx-background-color: #E5E7EB;");

            Label errorMsgLabel = new Label(rebuildTask.getException() != null ? rebuildTask.getException().getMessage()
                    : "An unknown error occurred.");
            errorMsgLabel.setFont(Font.font("Poppins", 13));
            errorMsgLabel.setStyle("-fx-text-fill: #6B7280;");
            errorMsgLabel.setWrapText(true);

            HBox errorBtnBox = new HBox();
            errorBtnBox.setAlignment(Pos.CENTER_RIGHT);
            Button errorOkBtn = new Button("OK");
            errorOkBtn.setPrefWidth(100);
            errorOkBtn.setPrefHeight(36);
            errorOkBtn.setStyle(
                    "-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-family: 'Poppins'; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand; -fx-focus-color: transparent; -fx-faint-focus-color: transparent; -fx-border-color: transparent;");
            errorOkBtn.setOnAction(ev -> errorDialog.setResult(ButtonType.OK));
            errorBtnBox.getChildren().add(errorOkBtn);

            errorContent.getChildren().addAll(errorTitleBar, errorHeaderRow, errorDivider, errorMsgLabel, errorBtnBox);

            VBox errorContainer = new VBox();
            errorContainer.setStyle(
                    "-fx-background-color: white; -fx-background-radius: 16; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 16;");
            errorContainer.setEffect(
                    new javafx.scene.effect.DropShadow(30, 0, 8, javafx.scene.paint.Color.rgb(0, 0, 0, 0.15)));
            errorContainer.getChildren().add(errorContent);

            StackPane errorShadowContainer = new StackPane(errorContainer);
            errorShadowContainer.setPadding(new Insets(40));
            errorShadowContainer.setStyle("-fx-background-color: transparent;");

            errorDialog.getDialogPane().setContent(errorShadowContainer);
            errorDialog.getDialogPane().setStyle("-fx-background-color: transparent; -fx-padding: 0;");
            errorDialog.getDialogPane().getScene().setFill(javafx.scene.paint.Color.TRANSPARENT);
            errorDialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
            errorDialog.getDialogPane().lookupButton(ButtonType.OK).setVisible(false);
            errorDialog.getDialogPane().lookupButton(ButtonType.OK).setManaged(false);

            errorDialog.showAndWait();
        });

        // Submit to compute thread pool (CPU-intensive indexing)
        ThreadPoolManager.getInstance().submitComputeTask(rebuildTask);
    }

    private VBox createWikiTemplatesBox() {
        Label header = new Label("Wiki Templates");
        header.setFont(Font.font("Poppins Medium", 14));

        ListView<WikiTemplate> listView = new ListView<>();
        listView.getItems().addAll(wikiController.loadAllTemplates());
        listView.setPrefHeight(150);
        listView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(WikiTemplate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getIcon() + " " + item.getName() + (item.getIsDefault() ? " (Default)" : ""));
                }
            }
        });

        Button addBtn = new Button("Add");
        addBtn.setGraphic(new FontIcon(Feather.PLUS));
        addBtn.getStyleClass().add("pill-button");
        addBtn.setOnAction(e -> handleCreateWikiTemplate(listView));

        Button editBtn = new Button("Edit");
        editBtn.setGraphic(new FontIcon(Feather.EDIT_2));
        editBtn.getStyleClass().addAll("pill-button", "warning");
        editBtn.setOnAction(e -> handleEditWikiTemplate(listView));

        Button deleteBtn = new Button("Delete");
        deleteBtn.setGraphic(new FontIcon(Feather.TRASH_2));
        deleteBtn.getStyleClass().addAll("pill-button", "danger");
        deleteBtn.setOnAction(e -> handleDeleteWikiTemplate(listView));

        HBox buttons = new HBox(10, addBtn, editBtn, deleteBtn);

        return new VBox(5, header, listView, buttons);
    }

    private VBox createJournalTemplatesBox() {
        Label header = new Label("Journal Templates");
        header.setFont(Font.font("Poppins Medium", 14));

        ListView<JournalTemplate> listView = new ListView<>();
        listView.getItems().addAll(journalController.loadTemplates());
        listView.setPrefHeight(150);
        listView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(JournalTemplate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });

        Button addBtn = new Button("Add");
        addBtn.setGraphic(new FontIcon(Feather.PLUS));
        addBtn.getStyleClass().add("pill-button");
        addBtn.setOnAction(e -> handleCreateJournalTemplate(listView));

        Button editBtn = new Button("Edit");
        editBtn.setGraphic(new FontIcon(Feather.EDIT_2));
        editBtn.getStyleClass().addAll("pill-button", "warning");
        editBtn.setOnAction(e -> handleEditJournalTemplate(listView));

        Button deleteBtn = new Button("Delete");
        deleteBtn.setGraphic(new FontIcon(Feather.TRASH_2));
        deleteBtn.getStyleClass().addAll("pill-button", "danger");
        deleteBtn.setOnAction(e -> handleDeleteJournalTemplate(listView));

        HBox buttons = new HBox(10, addBtn, editBtn, deleteBtn);

        return new VBox(5, header, listView, buttons);
    }

    private void handleCreateWikiTemplate(ListView<WikiTemplate> listView) {
        Dialog<WikiTemplate> dialog = new Dialog<>();
        dialog.setTitle("New Wiki Template");
        dialog.setHeaderText("Create a new wiki template");

        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        javafx.scene.layout.ColumnConstraints col1 = new javafx.scene.layout.ColumnConstraints();
        col1.setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
        col1.setHgrow(javafx.scene.layout.Priority.NEVER);

        javafx.scene.layout.ColumnConstraints col2 = new javafx.scene.layout.ColumnConstraints();
        col2.setHgrow(javafx.scene.layout.Priority.ALWAYS);

        grid.getColumnConstraints().addAll(col1, col2);

        TextField name = new TextField();
        name.setPromptText("Template Name");
        TextField icon = new TextField();
        icon.setPromptText("Icon (emoji)");
        TextArea content = new TextArea();
        content.setPromptText("Content (supports {date}, {time}, {title})");
        content.setPrefRowCount(5);

        grid.add(new Label("Name:"), 0, 0);
        grid.add(createStyledInput(name, Feather.TAG), 1, 0);
        grid.add(new Label("Icon:"), 0, 1);
        grid.add(createStyledInput(icon, Feather.SMILE), 1, 1);
        grid.add(new Label("Content:"), 0, 2);
        grid.add(content, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                return wikiController.createTemplate(name.getText(), "", content.getText(), icon.getText());
            }
            return null;
        });

        Optional<WikiTemplate> result = dialog.showAndWait();
        result.ifPresent(template -> listView.getItems().add(template));
    }

    private void handleDeleteWikiTemplate(ListView<WikiTemplate> listView) {
        WikiTemplate selected = listView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a template to delete.");
            alert.showAndWait();
            return;
        }

        // Prevent deletion of default templates
        if (selected.getIsDefault()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Cannot delete default templates.");
            alert.showAndWait();
            return;
        }

        // Confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Template");
        confirmAlert.setHeaderText("Delete \"" + selected.getName() + "\"?");
        confirmAlert.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                wikiController.deleteTemplate(selected);
                listView.getItems().remove(selected);
                listView.refresh();
                listView.getSelectionModel().clearSelection();
            } catch (Exception e) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Failed to delete template: " + e.getMessage());
                errorAlert.showAndWait();
            }
        }
    }

    private void handleCreateJournalTemplate(ListView<JournalTemplate> listView) {
        Dialog<JournalTemplate> dialog = new Dialog<>();
        dialog.setTitle("New Journal Template");
        dialog.setHeaderText("Create a new journal template");

        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        javafx.scene.layout.ColumnConstraints col1 = new javafx.scene.layout.ColumnConstraints();
        col1.setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
        col1.setHgrow(javafx.scene.layout.Priority.NEVER);

        javafx.scene.layout.ColumnConstraints col2 = new javafx.scene.layout.ColumnConstraints();
        col2.setHgrow(javafx.scene.layout.Priority.ALWAYS);

        grid.getColumnConstraints().addAll(col1, col2);

        TextField name = new TextField();
        name.setPromptText("Template Name");
        TextArea content = new TextArea();
        content.setPromptText("Content");
        content.setPrefRowCount(5);

        grid.add(new Label("Name:"), 0, 0);
        grid.add(createStyledInput(name, Feather.TAG), 1, 0);
        grid.add(new Label("Content:"), 0, 1);
        grid.add(content, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                return journalController.createTemplate(name.getText(), content.getText());
            }
            return null;
        });

        Optional<JournalTemplate> result = dialog.showAndWait();
        result.ifPresent(template -> listView.getItems().add(template));
    }

    private void handleDeleteJournalTemplate(ListView<JournalTemplate> listView) {
        JournalTemplate selected = listView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a template to delete.");
            alert.showAndWait();
            return;
        }

        // Confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Template");
        confirmAlert.setHeaderText("Delete \"" + selected.getName() + "\"?");
        confirmAlert.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                journalController.deleteTemplate(selected);
                listView.getItems().remove(selected);
                listView.refresh();
                listView.getSelectionModel().clearSelection();
            } catch (Exception e) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Failed to delete template: " + e.getMessage());
                errorAlert.showAndWait();
            }
        }
    }

    private void handleEditWikiTemplate(ListView<WikiTemplate> listView) {
        WikiTemplate selected = listView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a template to edit.");
            alert.showAndWait();
            return;
        }

        Dialog<WikiTemplate> dialog = new Dialog<>();
        dialog.setTitle("Edit Wiki Template");
        dialog.setHeaderText("Edit wiki template");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        javafx.scene.layout.ColumnConstraints col1 = new javafx.scene.layout.ColumnConstraints();
        col1.setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
        col1.setHgrow(javafx.scene.layout.Priority.NEVER);

        javafx.scene.layout.ColumnConstraints col2 = new javafx.scene.layout.ColumnConstraints();
        col2.setHgrow(javafx.scene.layout.Priority.ALWAYS);

        grid.getColumnConstraints().addAll(col1, col2);

        TextField name = new TextField(selected.getName());
        name.setPromptText("Template Name");
        TextField icon = new TextField(selected.getIcon());
        icon.setPromptText("Icon (emoji)");
        TextArea content = new TextArea(selected.getContent());
        content.setPromptText("Content (supports {date}, {time}, {title})");
        content.setPrefRowCount(5);

        grid.add(new Label("Name:"), 0, 0);
        grid.add(createStyledInput(name, Feather.TAG), 1, 0);
        grid.add(new Label("Icon:"), 0, 1);
        grid.add(createStyledInput(icon, Feather.SMILE), 1, 1);
        grid.add(new Label("Content:"), 0, 2);
        grid.add(content, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                selected.setName(name.getText());
                selected.setIcon(icon.getText());
                selected.setContent(content.getText());
                return wikiController.updateTemplate(selected);
            }
            return null;
        });

        Optional<WikiTemplate> result = dialog.showAndWait();
        result.ifPresent(template -> listView.refresh());
    }

    private void handleEditJournalTemplate(ListView<JournalTemplate> listView) {
        JournalTemplate selected = listView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a template to edit.");
            alert.showAndWait();
            return;
        }

        Dialog<JournalTemplate> dialog = new Dialog<>();
        dialog.setTitle("Edit Journal Template");
        dialog.setHeaderText("Edit journal template");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        javafx.scene.layout.ColumnConstraints col1 = new javafx.scene.layout.ColumnConstraints();
        col1.setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
        col1.setHgrow(javafx.scene.layout.Priority.NEVER);

        javafx.scene.layout.ColumnConstraints col2 = new javafx.scene.layout.ColumnConstraints();
        col2.setHgrow(javafx.scene.layout.Priority.ALWAYS);

        grid.getColumnConstraints().addAll(col1, col2);

        TextField name = new TextField(selected.getName());
        name.setPromptText("Template Name");
        TextArea content = new TextArea(selected.getContent());
        content.setPromptText("Content");
        content.setPrefRowCount(5);

        grid.add(new Label("Name:"), 0, 0);
        grid.add(createStyledInput(name, Feather.TAG), 1, 0);
        grid.add(new Label("Content:"), 0, 1);
        grid.add(content, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                selected.setName(name.getText());
                selected.setContent(content.getText());
                return journalController.updateTemplate(selected);
            }
            return null;
        });

        Optional<JournalTemplate> result = dialog.showAndWait();
        result.ifPresent(template -> listView.refresh());
    }

    private void handleExportWikis() {
        // Create custom styled confirmation dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initStyle(javafx.stage.StageStyle.TRANSPARENT);
        dialog.initOwner(getScene().getWindow());

        VBox dialogContent = new VBox(20);
        dialogContent.setAlignment(Pos.CENTER);
        dialogContent.setPadding(new Insets(0, 32, 24, 32));

        HBox titleBar = new HBox(12);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(16, 16, 16, 20));

        FontIcon titleIcon = new FontIcon(BoxiconsRegular.EXPORT);
        titleIcon.setIconSize(18);
        titleIcon.setIconColor(javafx.scene.paint.Color.web("#374151"));

        Label titleLabel = new Label("Export Wikis");
        titleLabel.setFont(Font.font("Poppins SemiBold", 14));
        titleLabel.setStyle("-fx-text-fill: #374151;");

        javafx.scene.layout.Region titleSpacer = new javafx.scene.layout.Region();
        HBox.setHgrow(titleSpacer, Priority.ALWAYS);

        Button closeBtn = new Button();
        FontIcon closeIcon = new FontIcon(Feather.X);
        closeIcon.setIconSize(16);
        closeBtn.setGraphic(closeIcon);
        closeBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> dialog.setResult(ButtonType.CANCEL));

        titleBar.getChildren().addAll(titleIcon, titleLabel, titleSpacer, closeBtn);

        FontIcon questionIcon = new FontIcon(BootstrapIcons.PATCH_QUESTION_FILL);
        questionIcon.setIconSize(80);
        questionIcon.setIconColor(javafx.scene.paint.Color.web("#f18c1a"));
        VBox.setMargin(questionIcon, new Insets(8, 0, 8, 0));

        Label messageLabel = new Label("This will export all your wikis to\nMarkdown files in a selected folder.");
        messageLabel.setFont(Font.font("Poppins", 14));
        messageLabel.setStyle("-fx-text-fill: #374151;");
        messageLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        messageLabel.setWrapText(true);

        Label questionLabel = new Label("Do you want to continue?");
        questionLabel.setFont(Font.font("Poppins", 14));
        questionLabel.setStyle("-fx-text-fill: #374151;");
        VBox.setMargin(questionLabel, new Insets(8, 0, 0, 0));

        HBox buttonBox = new HBox(16);
        buttonBox.setAlignment(Pos.CENTER);
        VBox.setMargin(buttonBox, new Insets(8, 0, 0, 0));

        Button okBtn = new Button("OK");
        okBtn.setPrefWidth(120);
        okBtn.setPrefHeight(40);
        okBtn.setStyle(
                "-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-background-radius: 20; -fx-font-family: 'Poppins'; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand; -fx-focus-color: transparent; -fx-faint-focus-color: transparent; -fx-border-color: transparent;");
        okBtn.setOnAction(e -> dialog.setResult(ButtonType.OK));

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setPrefWidth(120);
        cancelBtn.setPrefHeight(40);
        cancelBtn.setStyle(
                "-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-background-radius: 20; -fx-font-family: 'Poppins'; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand; -fx-focus-color: transparent; -fx-faint-focus-color: transparent; -fx-border-color: transparent;");
        cancelBtn.setOnAction(e -> dialog.setResult(ButtonType.CANCEL));

        buttonBox.getChildren().addAll(okBtn, cancelBtn);
        dialogContent.getChildren().addAll(questionIcon, messageLabel, questionLabel, buttonBox);

        VBox mainContainer = new VBox();
        mainContainer.setStyle(
                "-fx-background-color: white; -fx-background-radius: 16; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 16;");
        mainContainer
                .setEffect(new javafx.scene.effect.DropShadow(30, 0, 8, javafx.scene.paint.Color.rgb(0, 0, 0, 0.15)));
        mainContainer.getChildren().addAll(titleBar, dialogContent);

        StackPane shadowContainer = new StackPane(mainContainer);
        shadowContainer.setPadding(new Insets(40));
        shadowContainer.setStyle("-fx-background-color: transparent;");

        dialog.getDialogPane().setContent(shadowContainer);
        dialog.getDialogPane().setStyle("-fx-background-color: transparent; -fx-padding: 0;");
        dialog.getDialogPane().getScene().setFill(javafx.scene.paint.Color.TRANSPARENT);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        dialog.getDialogPane().lookupButton(ButtonType.CANCEL).setVisible(false);
        dialog.getDialogPane().lookupButton(ButtonType.CANCEL).setManaged(false);

        Optional<ButtonType> confirmResult = dialog.showAndWait();
        if (confirmResult.isPresent() && confirmResult.get() == ButtonType.OK) {
            java.util.List<com.roam.model.Wiki> allWikis = wikiController.loadAllWikis();
            ExportUtils.exportAllWikisToMarkdown(getScene().getWindow(), allWikis);
        }
    }

    private void handleExportEvents() {
        // Create custom styled confirmation dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initStyle(javafx.stage.StageStyle.TRANSPARENT);
        dialog.initOwner(getScene().getWindow());

        VBox dialogContent = new VBox(20);
        dialogContent.setAlignment(Pos.CENTER);
        dialogContent.setPadding(new Insets(0, 32, 24, 32));

        HBox titleBar = new HBox(12);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(16, 16, 16, 20));

        FontIcon titleIcon = new FontIcon(BoxiconsRegular.EXPORT);
        titleIcon.setIconSize(18);
        titleIcon.setIconColor(javafx.scene.paint.Color.web("#374151"));

        Label titleLabel = new Label("Export Events");
        titleLabel.setFont(Font.font("Poppins SemiBold", 14));
        titleLabel.setStyle("-fx-text-fill: #374151;");

        javafx.scene.layout.Region titleSpacer = new javafx.scene.layout.Region();
        HBox.setHgrow(titleSpacer, Priority.ALWAYS);

        Button closeBtn = new Button();
        FontIcon closeIcon = new FontIcon(Feather.X);
        closeIcon.setIconSize(16);
        closeBtn.setGraphic(closeIcon);
        closeBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> dialog.setResult(ButtonType.CANCEL));

        titleBar.getChildren().addAll(titleIcon, titleLabel, titleSpacer, closeBtn);

        FontIcon questionIcon = new FontIcon(BootstrapIcons.PATCH_QUESTION_FILL);
        questionIcon.setIconSize(80);
        questionIcon.setIconColor(javafx.scene.paint.Color.web("#f18c1a"));
        VBox.setMargin(questionIcon, new Insets(8, 0, 8, 0));

        Label messageLabel = new Label("This will export all your calendar\nevents to an ICS file.");
        messageLabel.setFont(Font.font("Poppins", 14));
        messageLabel.setStyle("-fx-text-fill: #374151;");
        messageLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        messageLabel.setWrapText(true);

        Label questionLabel = new Label("Do you want to continue?");
        questionLabel.setFont(Font.font("Poppins", 14));
        questionLabel.setStyle("-fx-text-fill: #374151;");
        VBox.setMargin(questionLabel, new Insets(8, 0, 0, 0));

        HBox buttonBox = new HBox(16);
        buttonBox.setAlignment(Pos.CENTER);
        VBox.setMargin(buttonBox, new Insets(8, 0, 0, 0));

        Button okBtn = new Button("OK");
        okBtn.setPrefWidth(120);
        okBtn.setPrefHeight(40);
        okBtn.setStyle(
                "-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-background-radius: 20; -fx-font-family: 'Poppins'; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand; -fx-focus-color: transparent; -fx-faint-focus-color: transparent; -fx-border-color: transparent;");
        okBtn.setOnAction(e -> dialog.setResult(ButtonType.OK));

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setPrefWidth(120);
        cancelBtn.setPrefHeight(40);
        cancelBtn.setStyle(
                "-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-background-radius: 20; -fx-font-family: 'Poppins'; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand; -fx-focus-color: transparent; -fx-faint-focus-color: transparent; -fx-border-color: transparent;");
        cancelBtn.setOnAction(e -> dialog.setResult(ButtonType.CANCEL));

        buttonBox.getChildren().addAll(okBtn, cancelBtn);
        dialogContent.getChildren().addAll(questionIcon, messageLabel, questionLabel, buttonBox);

        VBox mainContainer = new VBox();
        mainContainer.setStyle(
                "-fx-background-color: white; -fx-background-radius: 16; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 16;");
        mainContainer
                .setEffect(new javafx.scene.effect.DropShadow(30, 0, 8, javafx.scene.paint.Color.rgb(0, 0, 0, 0.15)));
        mainContainer.getChildren().addAll(titleBar, dialogContent);

        StackPane shadowContainer = new StackPane(mainContainer);
        shadowContainer.setPadding(new Insets(40));
        shadowContainer.setStyle("-fx-background-color: transparent;");

        dialog.getDialogPane().setContent(shadowContainer);
        dialog.getDialogPane().setStyle("-fx-background-color: transparent; -fx-padding: 0;");
        dialog.getDialogPane().getScene().setFill(javafx.scene.paint.Color.TRANSPARENT);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        dialog.getDialogPane().lookupButton(ButtonType.CANCEL).setVisible(false);
        dialog.getDialogPane().lookupButton(ButtonType.CANCEL).setManaged(false);

        Optional<ButtonType> confirmResult = dialog.showAndWait();
        if (confirmResult.isPresent() && confirmResult.get() == ButtonType.OK) {
            java.util.List<com.roam.model.CalendarEvent> events = calendarController.getAllEvents();
            ExportUtils.exportEventsToICS(getScene().getWindow(), events);
        }
    }

    private void handleExportOperations() {
        // Create custom styled confirmation dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initStyle(javafx.stage.StageStyle.TRANSPARENT);
        dialog.initOwner(getScene().getWindow());

        VBox dialogContent = new VBox(20);
        dialogContent.setAlignment(Pos.CENTER);
        dialogContent.setPadding(new Insets(0, 32, 24, 32));

        HBox titleBar = new HBox(12);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(16, 16, 16, 20));

        FontIcon titleIcon = new FontIcon(BoxiconsRegular.EXPORT);
        titleIcon.setIconSize(18);
        titleIcon.setIconColor(javafx.scene.paint.Color.web("#374151"));

        Label titleLabel = new Label("Export Operations");
        titleLabel.setFont(Font.font("Poppins SemiBold", 14));
        titleLabel.setStyle("-fx-text-fill: #374151;");

        javafx.scene.layout.Region titleSpacer = new javafx.scene.layout.Region();
        HBox.setHgrow(titleSpacer, Priority.ALWAYS);

        Button closeBtn = new Button();
        FontIcon closeIcon = new FontIcon(Feather.X);
        closeIcon.setIconSize(16);
        closeBtn.setGraphic(closeIcon);
        closeBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> dialog.setResult(ButtonType.CANCEL));

        titleBar.getChildren().addAll(titleIcon, titleLabel, titleSpacer, closeBtn);

        FontIcon questionIcon = new FontIcon(BootstrapIcons.PATCH_QUESTION_FILL);
        questionIcon.setIconSize(80);
        questionIcon.setIconColor(javafx.scene.paint.Color.web("#f18c1a"));
        VBox.setMargin(questionIcon, new Insets(8, 0, 8, 0));

        Label messageLabel = new Label("This will export all your operations\nto a JSON file.");
        messageLabel.setFont(Font.font("Poppins", 14));
        messageLabel.setStyle("-fx-text-fill: #374151;");
        messageLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        messageLabel.setWrapText(true);

        Label questionLabel = new Label("Do you want to continue?");
        questionLabel.setFont(Font.font("Poppins", 14));
        questionLabel.setStyle("-fx-text-fill: #374151;");
        VBox.setMargin(questionLabel, new Insets(8, 0, 0, 0));

        HBox buttonBox = new HBox(16);
        buttonBox.setAlignment(Pos.CENTER);
        VBox.setMargin(buttonBox, new Insets(8, 0, 0, 0));

        Button okBtn = new Button("OK");
        okBtn.setPrefWidth(120);
        okBtn.setPrefHeight(40);
        okBtn.setStyle(
                "-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-background-radius: 20; -fx-font-family: 'Poppins'; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand; -fx-focus-color: transparent; -fx-faint-focus-color: transparent; -fx-border-color: transparent;");
        okBtn.setOnAction(e -> dialog.setResult(ButtonType.OK));

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setPrefWidth(120);
        cancelBtn.setPrefHeight(40);
        cancelBtn.setStyle(
                "-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-background-radius: 20; -fx-font-family: 'Poppins'; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand; -fx-focus-color: transparent; -fx-faint-focus-color: transparent; -fx-border-color: transparent;");
        cancelBtn.setOnAction(e -> dialog.setResult(ButtonType.CANCEL));

        buttonBox.getChildren().addAll(okBtn, cancelBtn);
        dialogContent.getChildren().addAll(questionIcon, messageLabel, questionLabel, buttonBox);

        VBox mainContainer = new VBox();
        mainContainer.setStyle(
                "-fx-background-color: white; -fx-background-radius: 16; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 16;");
        mainContainer
                .setEffect(new javafx.scene.effect.DropShadow(30, 0, 8, javafx.scene.paint.Color.rgb(0, 0, 0, 0.15)));
        mainContainer.getChildren().addAll(titleBar, dialogContent);

        StackPane shadowContainer = new StackPane(mainContainer);
        shadowContainer.setPadding(new Insets(40));
        shadowContainer.setStyle("-fx-background-color: transparent;");

        dialog.getDialogPane().setContent(shadowContainer);
        dialog.getDialogPane().setStyle("-fx-background-color: transparent; -fx-padding: 0;");
        dialog.getDialogPane().getScene().setFill(javafx.scene.paint.Color.TRANSPARENT);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        dialog.getDialogPane().lookupButton(ButtonType.CANCEL).setVisible(false);
        dialog.getDialogPane().lookupButton(ButtonType.CANCEL).setManaged(false);

        Optional<ButtonType> confirmResult = dialog.showAndWait();
        if (confirmResult.isPresent() && confirmResult.get() == ButtonType.OK) {
            java.util.List<com.roam.model.Operation> operations = operationsController.loadOperations();
            ExportUtils.exportOperationsToJSON(getScene().getWindow(), operations);
        }
    }

    private void handleExportJournals() {
        // Create custom styled confirmation dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initStyle(javafx.stage.StageStyle.TRANSPARENT);
        dialog.initOwner(getScene().getWindow());

        VBox dialogContent = new VBox(20);
        dialogContent.setAlignment(Pos.CENTER);
        dialogContent.setPadding(new Insets(0, 32, 24, 32));

        HBox titleBar = new HBox(12);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(16, 16, 16, 20));

        FontIcon titleIcon = new FontIcon(BoxiconsRegular.EXPORT);
        titleIcon.setIconSize(18);
        titleIcon.setIconColor(javafx.scene.paint.Color.web("#374151"));

        Label titleLabel = new Label("Export Journals");
        titleLabel.setFont(Font.font("Poppins SemiBold", 14));
        titleLabel.setStyle("-fx-text-fill: #374151;");

        javafx.scene.layout.Region titleSpacer = new javafx.scene.layout.Region();
        HBox.setHgrow(titleSpacer, Priority.ALWAYS);

        Button closeBtn = new Button();
        FontIcon closeIcon = new FontIcon(Feather.X);
        closeIcon.setIconSize(16);
        closeBtn.setGraphic(closeIcon);
        closeBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> dialog.setResult(ButtonType.CANCEL));

        titleBar.getChildren().addAll(titleIcon, titleLabel, titleSpacer, closeBtn);

        FontIcon questionIcon = new FontIcon(BootstrapIcons.PATCH_QUESTION_FILL);
        questionIcon.setIconSize(80);
        questionIcon.setIconColor(javafx.scene.paint.Color.web("#f18c1a"));
        VBox.setMargin(questionIcon, new Insets(8, 0, 8, 0));

        Label messageLabel = new Label(
                "This will export all your journal entries\nto Markdown files in a selected folder.");
        messageLabel.setFont(Font.font("Poppins", 14));
        messageLabel.setStyle("-fx-text-fill: #374151;");
        messageLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        messageLabel.setWrapText(true);

        Label questionLabel = new Label("Do you want to continue?");
        questionLabel.setFont(Font.font("Poppins", 14));
        questionLabel.setStyle("-fx-text-fill: #374151;");
        VBox.setMargin(questionLabel, new Insets(8, 0, 0, 0));

        HBox buttonBox = new HBox(16);
        buttonBox.setAlignment(Pos.CENTER);
        VBox.setMargin(buttonBox, new Insets(8, 0, 0, 0));

        Button okBtn = new Button("OK");
        okBtn.setPrefWidth(120);
        okBtn.setPrefHeight(40);
        okBtn.setStyle(
                "-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-background-radius: 20; -fx-font-family: 'Poppins'; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand; -fx-focus-color: transparent; -fx-faint-focus-color: transparent; -fx-border-color: transparent;");
        okBtn.setOnAction(e -> dialog.setResult(ButtonType.OK));

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setPrefWidth(120);
        cancelBtn.setPrefHeight(40);
        cancelBtn.setStyle(
                "-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-background-radius: 20; -fx-font-family: 'Poppins'; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand; -fx-focus-color: transparent; -fx-faint-focus-color: transparent; -fx-border-color: transparent;");
        cancelBtn.setOnAction(e -> dialog.setResult(ButtonType.CANCEL));

        buttonBox.getChildren().addAll(okBtn, cancelBtn);
        dialogContent.getChildren().addAll(questionIcon, messageLabel, questionLabel, buttonBox);

        VBox mainContainer = new VBox();
        mainContainer.setStyle(
                "-fx-background-color: white; -fx-background-radius: 16; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 16;");
        mainContainer
                .setEffect(new javafx.scene.effect.DropShadow(30, 0, 8, javafx.scene.paint.Color.rgb(0, 0, 0, 0.15)));
        mainContainer.getChildren().addAll(titleBar, dialogContent);

        StackPane shadowContainer = new StackPane(mainContainer);
        shadowContainer.setPadding(new Insets(40));
        shadowContainer.setStyle("-fx-background-color: transparent;");

        dialog.getDialogPane().setContent(shadowContainer);
        dialog.getDialogPane().setStyle("-fx-background-color: transparent; -fx-padding: 0;");
        dialog.getDialogPane().getScene().setFill(javafx.scene.paint.Color.TRANSPARENT);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        dialog.getDialogPane().lookupButton(ButtonType.CANCEL).setVisible(false);
        dialog.getDialogPane().lookupButton(ButtonType.CANCEL).setManaged(false);

        Optional<ButtonType> confirmResult = dialog.showAndWait();
        if (confirmResult.isPresent() && confirmResult.get() == ButtonType.OK) {
            java.util.List<com.roam.model.JournalEntry> entries = journalController.loadAllEntries();
            ExportUtils.exportJournalsToMarkdown(getScene().getWindow(), entries);
        }
    }

    private void handleImportWikis() {
        ImportUtils.importWikisFromMarkdown(getScene().getWindow());
    }

    private void handleImportEvents() {
        ImportUtils.importEventsFromICS(getScene().getWindow());
    }

    private void handleImportOperations() {
        ImportUtils.importOperationsFromJSON(getScene().getWindow());
    }

    private void handleImportJournals() {
        ImportUtils.importJournalsFromMarkdown(getScene().getWindow());
    }

    private StackPane createStyledInput(TextField textField, Feather icon) {
        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconColor(javafx.scene.paint.Color.GRAY);
        fontIcon.setIconSize(16);

        StackPane stackPane = new StackPane();
        stackPane.setAlignment(Pos.CENTER_LEFT);
        stackPane.getChildren().addAll(textField, fontIcon);

        // Add padding to TextField to make room for icon
        textField.setStyle("-fx-padding: 5 5 5 30;");

        // Add margin to icon
        StackPane.setMargin(fontIcon, new Insets(0, 0, 0, 10));

        // Make icon mouse transparent so clicks go to TextField
        fontIcon.setMouseTransparent(true);

        return stackPane;
    }

    private String getInitials(String name) {
        if (name == null || name.isEmpty()) {
            return "U";
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].charAt(0) + "" + parts[parts.length - 1].charAt(0)).toUpperCase();
        }
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }

    private void handleChangeProfileImage(StackPane avatarPane, javafx.scene.shape.Circle circle, Label initialsLabel) {
        javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
        chooser.setTitle("Select Profile Image");
        chooser.getExtensionFilters().addAll(
                new javafx.stage.FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));

        java.io.File file = chooser.showOpenDialog(getScene().getWindow());
        if (file != null) {
            try {
                // Load image and crop to square from center
                javafx.scene.image.Image originalImage = new javafx.scene.image.Image(file.toURI().toString());
                double size = Math.min(originalImage.getWidth(), originalImage.getHeight());
                double x = (originalImage.getWidth() - size) / 2;
                double y = (originalImage.getHeight() - size) / 2;

                // Create cropped image view
                javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView(originalImage);
                imageView.setViewport(new javafx.geometry.Rectangle2D(x, y, size, size));
                imageView.setFitWidth(64);
                imageView.setFitHeight(64);
                imageView.setPreserveRatio(true);
                imageView.setSmooth(true);

                // Clip to circle
                javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(32, 32, 32);
                imageView.setClip(clip);

                avatarPane.getChildren().clear();
                avatarPane.getChildren().add(imageView);

                // Save path
                Settings settings = settingsService.getSettings();
                settings.setProfileImagePath(file.getAbsolutePath());
                settingsService.saveSettings();
            } catch (Exception ex) {
                // Fallback to initials on error
                avatarPane.getChildren().clear();
                avatarPane.getChildren().addAll(circle, initialsLabel);
            }
        }
    }
}
