package com.roam.view;

import com.roam.controller.JournalController;
import com.roam.model.JournalEntry;
import com.roam.model.JournalTemplate;
import com.roam.util.AnimationUtils;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.util.Duration;

import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Journal view that provides a personal diary and note-taking interface.
 * <p>
 * This view enables users to:
 * <ul>
 * <li>Create and edit daily journal entries</li>
 * <li>Track mood with customizable mood indicators</li>
 * <li>Navigate between dates to view past entries</li>
 * <li>Use templates for structured journaling</li>
 * <li>Auto-save functionality to prevent data loss</li>
 * </ul>
 * The view includes a rich text editor with word count tracking and
 * save status indication.
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public class JournalView extends StackPane {

    private final JournalController controller;
    private final VBox contentPane;
    private TextArea editorArea;
    private Label dateLabel;
    private Label wordCountLabel;
    private Button saveIndicator;
    private JournalEntry currentEntry;
    private LocalDate currentDate;
    private ToggleGroup moodGroup;
    private PauseTransition autoSaveTimer;
    private boolean hasUnsavedChanges = false;

    // Mood options
    private static final String[] MOOD_LABELS = { "Great", "Good", "Okay", "Bad", "Terrible" };
    private static final Feather[] MOOD_ICONS = {
            Feather.HEART, Feather.THUMBS_UP, Feather.SMILE, Feather.FROWN, Feather.MEH
    };

    public JournalView() {
        this.controller = new JournalController();
        this.contentPane = new VBox();
        this.currentDate = LocalDate.now();
        getChildren().add(contentPane);
        initialize();
    }

    private void initialize() {
        contentPane.setStyle("-fx-background-color: -roam-bg-primary;");
        contentPane.setAlignment(Pos.TOP_CENTER);
        contentPane.getStyleClass().add("journal-view");

        // Header with title and date navigation
        HBox header = createHeader();

        // Main content area (centered card)
        VBox mainContent = createMainContent();

        VBox.setVgrow(mainContent, Priority.ALWAYS);
        contentPane.getChildren().addAll(header, mainContent);

        // Setup autosave BEFORE loading entry (to avoid NPE in text change listener)
        setupAutoSave();

        // Load today's entry
        loadEntry(currentDate);
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(32, 40, 24, 40));
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color: -roam-bg-primary;");

        // Title
        Label title = new Label("Daily Journal");
        title.setFont(Font.font("Poppins Bold", 28));
        title.setStyle("-fx-text-fill: -roam-text-primary;");

        // Spacer to push date nav to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Date navigation
        HBox dateNav = createDateNavigation();

        header.getChildren().addAll(title, spacer, dateNav);
        return header;
    }

    private HBox createDateNavigation() {
        HBox dateNav = new HBox(8);
        dateNav.setAlignment(Pos.CENTER);
        dateNav.setPadding(new Insets(8, 16, 8, 16));
        dateNav.getStyleClass().add("date-nav-container");

        Button prevBtn = new Button();
        prevBtn.setGraphic(new FontIcon(Feather.CHEVRON_LEFT));
        prevBtn.getStyleClass().add("date-nav-button");
        prevBtn.setOnAction(e -> navigateDate(-1));

        dateLabel = new Label(formatDate(currentDate));
        dateLabel.setFont(Font.font("Poppins SemiBold", 14));
        dateLabel.setStyle("-fx-text-fill: -roam-text-primary;");
        dateLabel.setMinWidth(150);
        dateLabel.setAlignment(Pos.CENTER);

        Button nextBtn = new Button();
        nextBtn.setGraphic(new FontIcon(Feather.CHEVRON_RIGHT));
        nextBtn.getStyleClass().add("date-nav-button");
        nextBtn.setOnAction(e -> navigateDate(1));

        dateNav.getChildren().addAll(prevBtn, dateLabel, nextBtn);
        return dateNav;
    }

    private VBox createMainContent() {
        VBox container = new VBox();
        container.setAlignment(Pos.TOP_CENTER);
        container.setPadding(new Insets(0, 40, 40, 40));

        // Journal card
        VBox journalCard = new VBox(24);
        journalCard.setMaxWidth(800);
        journalCard.setPadding(new Insets(32));
        journalCard.getStyleClass().add("journal-card");
        VBox.setVgrow(journalCard, Priority.ALWAYS);

        // Mood selector
        HBox moodSelector = createMoodSelector();

        // Editor area
        editorArea = new TextArea();
        editorArea.setWrapText(true);
        editorArea.setFont(Font.font("Poppins", 15));
        editorArea.setPromptText("How was your day? Write your thoughts here...");
        editorArea.getStyleClass().add("journal-editor");
        VBox.setVgrow(editorArea, Priority.ALWAYS);
        editorArea.setPrefRowCount(12);

        // Track changes for autosave
        editorArea.textProperty().addListener((obs, oldVal, newVal) -> {
            hasUnsavedChanges = true;
            updateWordCount();
            updateSaveIndicator(false);
            restartAutoSaveTimer();
        });

        // Footer with word count and save indicator
        HBox footer = createFooter();

        journalCard.getChildren().addAll(moodSelector, editorArea, footer);
        container.getChildren().add(journalCard);

        return container;
    }

    private HBox createMoodSelector() {
        HBox moodSelector = new HBox(16);
        moodSelector.setAlignment(Pos.CENTER);
        moodSelector.setPadding(new Insets(0, 0, 16, 0));

        moodGroup = new ToggleGroup();

        for (int i = 0; i < MOOD_LABELS.length; i++) {
            VBox moodOption = createMoodOption(MOOD_LABELS[i], MOOD_ICONS[i], i);
            moodSelector.getChildren().add(moodOption);
        }

        return moodSelector;
    }

    private VBox createMoodOption(String label, Feather icon, int index) {
        VBox option = new VBox(4);
        option.setAlignment(Pos.CENTER);
        option.setCursor(javafx.scene.Cursor.HAND);
        option.getStyleClass().add("mood-option");

        ToggleButton iconBtn = new ToggleButton();
        FontIcon moodIcon = new FontIcon(icon);
        moodIcon.setIconSize(24);
        iconBtn.setGraphic(moodIcon);
        iconBtn.setToggleGroup(moodGroup);
        iconBtn.getStyleClass().add("mood-button");
        iconBtn.setUserData(index);

        // Apply mood-specific styling
        String moodClass = "mood-" + label.toLowerCase();
        iconBtn.getStyleClass().add(moodClass);

        iconBtn.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                iconBtn.getStyleClass().add("mood-selected");
                hasUnsavedChanges = true;
                updateSaveIndicator(false);
                restartAutoSaveTimer();
            } else {
                iconBtn.getStyleClass().remove("mood-selected");
            }
        });

        Label moodLabel = new Label(label);
        moodLabel.setFont(Font.font("Poppins", 11));
        moodLabel.getStyleClass().add("mood-label");

        option.getChildren().addAll(iconBtn, moodLabel);
        return option;
    }

    private HBox createFooter() {
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(16, 0, 0, 0));
        footer.setStyle("-fx-border-color: -roam-border; -fx-border-width: 1 0 0 0;");

        wordCountLabel = new Label("0 words");
        wordCountLabel.setFont(Font.font("Poppins", 12));
        wordCountLabel.setStyle("-fx-text-fill: -roam-text-hint;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        saveIndicator = new Button("Saved");
        saveIndicator.setGraphic(new FontIcon(Feather.CHECK));
        saveIndicator.getStyleClass().addAll("save-indicator", "saved");
        saveIndicator.setOnAction(e -> saveCurrent());

        footer.getChildren().addAll(wordCountLabel, spacer, saveIndicator);
        return footer;
    }

    private void setupAutoSave() {
        autoSaveTimer = new PauseTransition(Duration.seconds(2));
        autoSaveTimer.setOnFinished(e -> {
            if (hasUnsavedChanges) {
                saveCurrent();
            }
        });
    }

    private void restartAutoSaveTimer() {
        autoSaveTimer.stop();
        autoSaveTimer.playFromStart();
    }

    private void navigateDate(int days) {
        // Save current before navigating
        if (hasUnsavedChanges) {
            saveCurrent();
        }
        currentDate = currentDate.plusDays(days);
        dateLabel.setText(formatDate(currentDate));
        loadEntry(currentDate);
    }

    private String formatDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("EEE, MMMM d"));
    }

    private void loadEntry(LocalDate date) {
        // Try to get or create entry for this date
        currentEntry = controller.createEntry(date);

        if (currentEntry != null) {
            editorArea.setText(currentEntry.getContent() != null ? currentEntry.getContent() : "");
            // Load mood if stored (assuming mood is stored in entry)
            loadMoodFromEntry(currentEntry);
        } else {
            editorArea.setText("");
        }

        editorArea.setDisable(false);
        hasUnsavedChanges = false;
        updateWordCount();
        updateSaveIndicator(true);
    }

    private void loadMoodFromEntry(JournalEntry entry) {
        // Reset all mood selections
        moodGroup.selectToggle(null);

        // If entry has mood data, select it
        // Assuming mood might be stored in the content or a separate field
        // For now, we'll leave it unselected for existing entries
    }

    private void saveCurrent() {
        if (currentEntry != null) {
            currentEntry.setContent(editorArea.getText());
            // Save selected mood
            if (moodGroup.getSelectedToggle() != null) {
                int moodIndex = (int) moodGroup.getSelectedToggle().getUserData();
                // Store mood in entry (if there's a mood field, otherwise in content)
            }
            controller.saveEntry(currentEntry);
            hasUnsavedChanges = false;
            updateSaveIndicator(true);
        }
    }

    private void updateWordCount() {
        String text = editorArea.getText();
        int wordCount = 0;
        if (text != null && !text.trim().isEmpty()) {
            wordCount = text.trim().split("\\s+").length;
        }
        wordCountLabel.setText(wordCount + " words");
    }

    private void updateSaveIndicator(boolean saved) {
        Platform.runLater(() -> {
            saveIndicator.getStyleClass().removeAll("saved", "unsaved");
            if (saved) {
                saveIndicator.setText("Saved");
                saveIndicator.setGraphic(new FontIcon(Feather.CHECK));
                saveIndicator.getStyleClass().add("saved");
            } else {
                saveIndicator.setText("Save");
                saveIndicator.setGraphic(new FontIcon(Feather.SAVE));
                saveIndicator.getStyleClass().add("unsaved");
            }
        });
    }

    private void showTemplates() {
        if (currentEntry == null)
            return;

        List<JournalTemplate> templates = controller.loadTemplates();
        ChoiceDialog<JournalTemplate> dialog = new ChoiceDialog<>(null, templates);
        dialog.setTitle("Select Template");
        dialog.setHeaderText("Choose a template to apply");
        dialog.setContentText("Template:");

        if (templates.isEmpty()) {
            JournalTemplate daily = new JournalTemplate("Daily Reflection",
                    "## Daily Reflection\n\n* What went well today?\n* What could be improved?\n* Goals for tomorrow:");
            templates.add(daily);
            dialog.getItems().add(daily);
        }

        dialog.showAndWait().ifPresent(template -> {
            controller.applyTemplate(currentEntry, template);
            editorArea.setText(currentEntry.getContent());
        });
    }
}
