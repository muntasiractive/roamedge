package com.roam.view;

import com.roam.model.CalendarEvent;
import com.roam.model.Operation;
import com.roam.model.OperationStatus;
import com.roam.model.Task;
import com.roam.model.TaskStatus;
import com.roam.model.Wiki;
import com.roam.service.CalendarService;
import com.roam.service.CalendarServiceImpl;
import com.roam.service.OperationService;
import com.roam.service.OperationServiceImpl;
import com.roam.service.TaskService;
import com.roam.service.TaskServiceImpl;
import com.roam.service.WikiService;
import com.roam.service.WikiServiceImpl;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;

/**
 * Global search dialog for searching across all application entities.
 * <p>
 * This dialog provides a unified search interface that queries across operations,
 * tasks, calendar events, and wiki notes. It features real-time search with
 * debouncing, category-based result grouping, and keyboard navigation support.
 * </p>
 * <p>
 * Features include:
 * </p>
 * <ul>
 *   <li>Cross-entity search (Operations, Tasks, Events, Wikis)</li>
 *   <li>Recent search history with persistence</li>
 *   <li>Keyboard navigation (Up/Down arrows, Enter, Escape)</li>
 *   <li>Category-based result grouping with icons</li>
 *   <li>Debounced search input for performance</li>
 * </ul>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Global Search Dialog - Spotlight/Command Palette style search
 * Features: Fuzzy matching, type filters, quick actions, keyboard navigation
 */
public class GlobalSearchDialog extends Stage {

    private final TaskService taskService;
    private final OperationService operationService;
    private final WikiService wikiService;
    private final CalendarService calendarService;

    private TextField searchField;
    private VBox resultsContainer;
    private ScrollPane resultsScroll;
    private VBox quickActionsSection;
    private VBox recentSearchesSection;

    private List<SearchResultItem> currentResults = new ArrayList<>();
    private int selectedIndex = -1;

    private Consumer<String> onNavigate;
    private Consumer<Task> onTaskSelect;
    private Consumer<Wiki> onWikiSelect;
    private Consumer<Operation> onOperationSelect;
    private Consumer<CalendarEvent> onEventSelect;

    private PauseTransition searchDebounce;
    private static final int MAX_RECENT_SEARCHES = 5;
    private static final String RECENT_SEARCHES_KEY = "recentSearches";

    // Filter patterns
    private static final Pattern OPERATION_FILTER = Pattern.compile("operation:([\\w\\s]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern PRIORITY_FILTER = Pattern.compile("priority:(high|medium|low)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern DUE_FILTER = Pattern.compile("due:(today|tomorrow|week|overdue)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern REGION_FILTER = Pattern.compile("region:([\\w\\s]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern TYPE_FILTER = Pattern.compile("^(task|wiki|operation|event):",
            Pattern.CASE_INSENSITIVE);

    public GlobalSearchDialog(Stage owner) {
        this.taskService = new TaskServiceImpl();
        this.operationService = new OperationServiceImpl();
        this.wikiService = new WikiServiceImpl();
        this.calendarService = new CalendarServiceImpl();

        initializeStage(owner);
        initializeUI();
        setupKeyboardNavigation();
        setupSearchDebounce();
    }

    private void initializeStage(Stage owner) {
        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.TRANSPARENT);

        setWidth(650);
        setMinHeight(400);
        setMaxHeight(600);

        // Center on owner
        if (owner != null) {
            setX(owner.getX() + (owner.getWidth() - 650) / 2);
            setY(owner.getY() + 100);
        }
    }

    private void initializeUI() {
        VBox root = new VBox();
        root.getStyleClass().add("search-dialog");
        root.setMaxWidth(650);

        // Search header
        HBox searchHeader = createSearchHeader();

        // Results container
        resultsContainer = new VBox(8);
        resultsContainer.getStyleClass().add("search-results");
        resultsContainer.setPadding(new Insets(16));

        resultsScroll = new ScrollPane(resultsContainer);
        resultsScroll.setFitToWidth(true);
        resultsScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        resultsScroll.getStyleClass().add("search-results-scroll");
        resultsScroll.setMaxHeight(450);
        VBox.setVgrow(resultsScroll, Priority.ALWAYS);

        // Initial state - show recent searches and quick actions
        showInitialState();

        root.getChildren().addAll(searchHeader, resultsScroll);

        // Wrap in stack pane for overlay effect
        StackPane overlay = new StackPane(root);
        overlay.getStyleClass().add("search-overlay");
        overlay.setAlignment(Pos.TOP_CENTER);
        overlay.setPadding(new Insets(0));

        // Close on clicking outside
        overlay.setOnMouseClicked(e -> {
            if (e.getTarget() == overlay) {
                close();
            }
        });

        Scene scene = new Scene(overlay);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(getClass().getResource("/styles/application.css").toExternalForm());

        // Handle escape key
        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                close();
                e.consume();
            }
        });

        setScene(scene);
    }

    private HBox createSearchHeader() {
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16, 20, 16, 20));
        header.getStyleClass().add("search-header");

        // Search icon
        FontIcon searchIcon = new FontIcon(Feather.SEARCH);
        searchIcon.setIconSize(20);
        searchIcon.getStyleClass().add("search-icon");

        // Search field
        searchField = new TextField();
        searchField.setPromptText("Search everything...");
        searchField.getStyleClass().add("search-input");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        // Close button
        Button closeBtn = new Button();
        closeBtn.getStyleClass().add("search-close-btn");
        FontIcon closeIcon = new FontIcon(Feather.X);
        closeIcon.setIconSize(18);
        closeBtn.setGraphic(closeIcon);
        closeBtn.setOnAction(e -> close());

        header.getChildren().addAll(searchIcon, searchField, closeBtn);

        return header;
    }

    private void showInitialState() {
        resultsContainer.getChildren().clear();

        // Recent searches section
        recentSearchesSection = createRecentSearchesSection();
        if (recentSearchesSection != null) {
            resultsContainer.getChildren().add(recentSearchesSection);
        }

        // Quick actions section
        quickActionsSection = createQuickActionsSection();
        resultsContainer.getChildren().add(quickActionsSection);
    }

    private VBox createRecentSearchesSection() {
        List<String> recentSearches = getRecentSearches();
        if (recentSearches.isEmpty()) {
            return null;
        }

        VBox section = new VBox(8);
        section.getStyleClass().add("search-section");

        Label title = new Label("Recent Searches");
        title.getStyleClass().add("search-section-title");
        section.getChildren().add(title);

        for (String search : recentSearches) {
            HBox item = createRecentSearchItem(search);
            section.getChildren().add(item);
        }

        return section;
    }

    private HBox createRecentSearchItem(String searchText) {
        HBox item = new HBox(12);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(10, 12, 10, 12));
        item.getStyleClass().add("search-result-item");
        item.setCursor(javafx.scene.Cursor.HAND);

        FontIcon icon = new FontIcon(Feather.CLOCK);
        icon.setIconSize(16);
        icon.getStyleClass().add("recent-search-icon");

        Label label = new Label(searchText);
        label.getStyleClass().add("recent-search-text");
        HBox.setHgrow(label, Priority.ALWAYS);

        // Remove button
        Button removeBtn = new Button();
        removeBtn.getStyleClass().add("recent-remove-btn");
        FontIcon removeIcon = new FontIcon(Feather.X);
        removeIcon.setIconSize(12);
        removeBtn.setGraphic(removeIcon);
        removeBtn.setVisible(false);

        item.setOnMouseEntered(e -> removeBtn.setVisible(true));
        item.setOnMouseExited(e -> removeBtn.setVisible(false));

        removeBtn.setOnAction(e -> {
            removeRecentSearch(searchText);
            showInitialState();
            e.consume();
        });

        item.setOnMouseClicked(e -> {
            if (e.getTarget() != removeBtn) {
                searchField.setText(searchText);
                performSearch(searchText);
            }
        });

        item.getChildren().addAll(icon, label, removeBtn);
        return item;
    }

    private VBox createQuickActionsSection() {
        VBox section = new VBox(8);
        section.getStyleClass().add("search-section");

        Label title = new Label("Quick Actions");
        title.getStyleClass().add("search-section-title");
        section.getChildren().add(title);

        // Quick action items
        section.getChildren().add(createQuickActionItem("Create new task", Feather.PLUS_CIRCLE, "Ctrl+Shift+T", () -> {
            close();
            if (onNavigate != null)
                onNavigate.accept("tasks");
        }));

        section.getChildren().add(createQuickActionItem("Create new wiki", Feather.FILE_PLUS, "Ctrl+Shift+N", () -> {
            close();
            if (onNavigate != null)
                onNavigate.accept("wiki");
        }));

        section.getChildren()
                .add(createQuickActionItem("Create new operation", Feather.FOLDER_PLUS, "Ctrl+Shift+P", () -> {
                    close();
                    if (onNavigate != null)
                        onNavigate.accept("operations");
                }));

        section.getChildren().add(createQuickActionItem("Open settings", Feather.SETTINGS, "Ctrl+,", () -> {
            close();
            if (onNavigate != null)
                onNavigate.accept("settings");
        }));

        section.getChildren().add(createQuickActionItem("Open calendar", Feather.CALENDAR, "Ctrl+E", () -> {
            close();
            if (onNavigate != null)
                onNavigate.accept("calendar");
        }));

        section.getChildren().add(createQuickActionItem("Open journal", Feather.BOOK, "Ctrl+J", () -> {
            close();
            if (onNavigate != null)
                onNavigate.accept("journal");
        }));

        return section;
    }

    private HBox createQuickActionItem(String text, Feather icon, String shortcut, Runnable action) {
        HBox item = new HBox(12);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(10, 12, 10, 12));
        item.getStyleClass().add("search-result-item");
        item.setCursor(javafx.scene.Cursor.HAND);

        FontIcon actionIcon = new FontIcon(icon);
        actionIcon.setIconSize(16);
        actionIcon.getStyleClass().add("quick-action-icon");

        Label label = new Label(text);
        label.getStyleClass().add("quick-action-text");
        HBox.setHgrow(label, Priority.ALWAYS);

        Label shortcutLabel = new Label(shortcut);
        shortcutLabel.getStyleClass().add("shortcut-hint");

        item.setOnMouseClicked(e -> action.run());

        item.getChildren().addAll(actionIcon, label, shortcutLabel);
        return item;
    }

    private void setupSearchDebounce() {
        searchDebounce = new PauseTransition(Duration.millis(150));
        searchDebounce.setOnFinished(e -> {
            String query = searchField.getText().trim();
            if (query.isEmpty()) {
                showInitialState();
            } else {
                performSearch(query);
            }
        });

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            searchDebounce.stop();
            searchDebounce.playFromStart();
        });
    }

    private void setupKeyboardNavigation() {
        searchField.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            switch (e.getCode()) {
                case DOWN:
                    navigateResults(1);
                    e.consume();
                    break;
                case UP:
                    navigateResults(-1);
                    e.consume();
                    break;
                case ENTER:
                    selectCurrentResult();
                    e.consume();
                    break;
                default:
                    break;
            }
        });
    }

    private void navigateResults(int delta) {
        if (currentResults.isEmpty())
            return;

        // Clear previous selection
        if (selectedIndex >= 0 && selectedIndex < currentResults.size()) {
            currentResults.get(selectedIndex).setSelected(false);
        }

        // Update selection
        selectedIndex += delta;
        if (selectedIndex < 0)
            selectedIndex = currentResults.size() - 1;
        if (selectedIndex >= currentResults.size())
            selectedIndex = 0;

        // Apply new selection
        currentResults.get(selectedIndex).setSelected(true);

        // Scroll to visible
        SearchResultItem selected = currentResults.get(selectedIndex);
        resultsScroll.setVvalue(
                (selectedIndex * 50.0)
                        / Math.max(1, resultsContainer.getHeight() - resultsScroll.getViewportBounds().getHeight()));
    }

    private void selectCurrentResult() {
        if (selectedIndex >= 0 && selectedIndex < currentResults.size()) {
            currentResults.get(selectedIndex).activate();
        } else if (!searchField.getText().isEmpty()) {
            // Save search to recent
            addRecentSearch(searchField.getText().trim());
        }
    }

    private void performSearch(String query) {
        resultsContainer.getChildren().clear();
        currentResults.clear();
        selectedIndex = -1;

        // Parse filters
        SearchFilters filters = parseFilters(query);
        String searchTerm = filters.cleanQuery.toLowerCase();

        if (searchTerm.isEmpty() && !filters.hasFilters()) {
            showInitialState();
            return;
        }

        // Search across all types (unless filtered)
        List<Operation> operations = new ArrayList<>();
        List<Task> tasks = new ArrayList<>();
        List<Wiki> wikis = new ArrayList<>();
        List<CalendarEvent> events = new ArrayList<>();

        try {
            // Search operations
            if (filters.typeFilter == null || filters.typeFilter.equals("operation")) {
                operations = searchOperations(searchTerm, filters);
            }

            // Search tasks
            if (filters.typeFilter == null || filters.typeFilter.equals("task")) {
                tasks = searchTasks(searchTerm, filters);
            }

            // Search wikis
            if (filters.typeFilter == null || filters.typeFilter.equals("wiki")) {
                wikis = searchWikis(searchTerm, filters);
            }

            // Search events
            if (filters.typeFilter == null || filters.typeFilter.equals("event")) {
                events = searchEvents(searchTerm, filters);
            }
        } catch (Exception e) {
            // Handle search errors gracefully
        }

        // Display results by category
        if (!operations.isEmpty()) {
            addResultSection("Operations", Feather.FOLDER, operations.size());
            for (Operation op : operations) {
                addProjectResult(op);
            }
        }

        if (!tasks.isEmpty()) {
            addResultSection("Tasks", Feather.CHECK_CIRCLE, tasks.size());
            for (Task task : tasks) {
                addTaskResult(task);
            }
        }

        if (!wikis.isEmpty()) {
            addResultSection("Wikis", Feather.FILE_TEXT, wikis.size());
            for (Wiki wiki : wikis) {
                addWikiResult(wiki);
            }
        }

        if (!events.isEmpty()) {
            addResultSection("Calendar Events", Feather.CALENDAR, events.size());
            for (CalendarEvent event : events) {
                addEventResult(event);
            }
        }

        // No results
        if (resultsContainer.getChildren().isEmpty()) {
            addNoResultsMessage(query);
        }
    }

    private SearchFilters parseFilters(String query) {
        SearchFilters filters = new SearchFilters();
        String cleanQuery = query;

        // Check for type filter (task:, wiki:, operation:, event:)
        Matcher typeMatcher = TYPE_FILTER.matcher(query);
        if (typeMatcher.find()) {
            filters.typeFilter = typeMatcher.group(1).toLowerCase();
            cleanQuery = cleanQuery.replaceFirst(TYPE_FILTER.pattern(), "").trim();
        }

        // Check for operation filter
        Matcher operationMatcher = OPERATION_FILTER.matcher(cleanQuery);
        if (operationMatcher.find()) {
            filters.projectFilter = operationMatcher.group(1).trim();
            cleanQuery = cleanQuery.replaceFirst(OPERATION_FILTER.pattern(), "").trim();
        }

        // Check for priority filter
        Matcher priorityMatcher = PRIORITY_FILTER.matcher(cleanQuery);
        if (priorityMatcher.find()) {
            filters.priorityFilter = priorityMatcher.group(1).toLowerCase();
            cleanQuery = cleanQuery.replaceFirst(PRIORITY_FILTER.pattern(), "").trim();
        }

        // Check for due filter
        Matcher dueMatcher = DUE_FILTER.matcher(cleanQuery);
        if (dueMatcher.find()) {
            filters.dueFilter = dueMatcher.group(1).toLowerCase();
            cleanQuery = cleanQuery.replaceFirst(DUE_FILTER.pattern(), "").trim();
        }

        // Check for region filter
        Matcher regionMatcher = REGION_FILTER.matcher(cleanQuery);
        if (regionMatcher.find()) {
            filters.regionFilter = regionMatcher.group(1).trim();
            cleanQuery = cleanQuery.replaceFirst(REGION_FILTER.pattern(), "").trim();
        }

        filters.cleanQuery = cleanQuery.trim();
        return filters;
    }

    private List<Operation> searchOperations(String term, SearchFilters filters) {
        return operationService.findAll().stream()
                .filter(op -> fuzzyMatch(op.getName(), term) ||
                        (op.getPurpose() != null && fuzzyMatch(op.getPurpose(), term)))
                .filter(op -> filters.regionFilter == null ||
                        (op.getRegion() != null
                                && op.getRegion().toLowerCase().contains(filters.regionFilter.toLowerCase())))
                .limit(5)
                .collect(Collectors.toList());
    }

    private List<Task> searchTasks(String term, SearchFilters filters) {
        return taskService.findAll().stream()
                .filter(t -> fuzzyMatch(t.getTitle(), term) ||
                        (t.getDescription() != null && fuzzyMatch(t.getDescription(), term)))
                .filter(t -> filters.priorityFilter == null ||
                        (t.getPriority() != null && t.getPriority().name().equalsIgnoreCase(filters.priorityFilter)))
                .filter(t -> filters.regionFilter == null ||
                        (t.getRegion() != null
                                && t.getRegion().toLowerCase().contains(filters.regionFilter.toLowerCase())))
                .filter(t -> matchesDueFilter(t, filters.dueFilter))
                .limit(5)
                .collect(Collectors.toList());
    }

    private List<Wiki> searchWikis(String term, SearchFilters filters) {
        return wikiService.findAll().stream()
                .filter(w -> fuzzyMatch(w.getTitle(), term) ||
                        (w.getContent() != null && w.getContent().toLowerCase().contains(term)))
                .limit(5)
                .collect(Collectors.toList());
    }

    private List<CalendarEvent> searchEvents(String term, SearchFilters filters) {
        return calendarService.findAll().stream()
                .filter(e -> fuzzyMatch(e.getTitle(), term) ||
                        (e.getDescription() != null && fuzzyMatch(e.getDescription(), term)))
                .limit(5)
                .collect(Collectors.toList());
    }

    private boolean fuzzyMatch(String text, String pattern) {
        if (text == null || pattern == null || pattern.isEmpty())
            return pattern == null || pattern.isEmpty();

        text = text.toLowerCase();
        pattern = pattern.toLowerCase();

        // Exact substring match
        if (text.contains(pattern))
            return true;

        // Fuzzy match - check if pattern characters appear in order
        int patternIndex = 0;
        for (int i = 0; i < text.length() && patternIndex < pattern.length(); i++) {
            if (text.charAt(i) == pattern.charAt(patternIndex)) {
                patternIndex++;
            }
        }
        return patternIndex == pattern.length();
    }

    private boolean matchesDueFilter(Task task, String dueFilter) {
        if (dueFilter == null)
            return true;
        if (task.getDueDate() == null)
            return false;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dueDate = task.getDueDate();

        switch (dueFilter) {
            case "today":
                return dueDate.toLocalDate().equals(now.toLocalDate());
            case "tomorrow":
                return dueDate.toLocalDate().equals(now.toLocalDate().plusDays(1));
            case "week":
                return dueDate.isAfter(now) && dueDate.isBefore(now.plusDays(7));
            case "overdue":
                return dueDate.isBefore(now) && task.getStatus() != TaskStatus.DONE;
            default:
                return true;
        }
    }

    private void addResultSection(String title, Feather icon, int count) {
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(12, 0, 4, 0));

        FontIcon sectionIcon = new FontIcon(icon);
        sectionIcon.setIconSize(14);
        sectionIcon.getStyleClass().add("section-icon");

        Label titleLabel = new Label(title + " (" + count + ")");
        titleLabel.getStyleClass().add("search-section-title");

        header.getChildren().addAll(sectionIcon, titleLabel);
        resultsContainer.getChildren().add(header);
    }

    private void addProjectResult(Operation op) {
        SearchResultItem item = new SearchResultItem();

        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 12, 10, 12));
        row.getStyleClass().add("search-result-item");
        row.setCursor(javafx.scene.Cursor.HAND);

        FontIcon icon = new FontIcon(Feather.FOLDER);
        icon.setIconSize(16);
        icon.getStyleClass().add("result-icon");

        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label title = new Label(op.getName());
        title.getStyleClass().add("result-title");

        info.getChildren().add(title);

        Label status = new Label(formatStatus(op.getStatus()));
        status.getStyleClass().addAll("result-badge", "status-" + op.getStatus().name().toLowerCase());

        row.getChildren().addAll(icon, info, status);

        item.node = row;
        item.onActivate = () -> {
            addRecentSearch(searchField.getText().trim());
            close();
            if (onOperationSelect != null) {
                onOperationSelect.accept(op);
            } else if (onNavigate != null) {
                onNavigate.accept("operations");
            }
        };

        row.setOnMouseClicked(e -> item.activate());
        row.setOnMouseEntered(e -> row.getStyleClass().add("selected"));
        row.setOnMouseExited(e -> row.getStyleClass().remove("selected"));

        currentResults.add(item);
        resultsContainer.getChildren().add(row);
    }

    private void addTaskResult(Task task) {
        SearchResultItem item = new SearchResultItem();

        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 12, 10, 12));
        row.getStyleClass().add("search-result-item");
        row.setCursor(javafx.scene.Cursor.HAND);

        // Checkbox icon
        FontIcon icon = new FontIcon(task.getStatus() == TaskStatus.DONE ? Feather.CHECK_SQUARE : Feather.SQUARE);
        icon.setIconSize(16);
        icon.getStyleClass().add("result-icon");
        if (task.getStatus() == TaskStatus.DONE) {
            icon.getStyleClass().add("completed");
        }

        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label title = new Label(task.getTitle());
        title.getStyleClass().add("result-title");
        if (task.getStatus() == TaskStatus.DONE) {
            title.getStyleClass().add("completed");
        }

        info.getChildren().add(title);

        // Due date badge
        if (task.getDueDate() != null) {
            Label dueLabel = new Label(formatDueDate(task.getDueDate()));
            dueLabel.getStyleClass().add("result-badge");
            if (task.getDueDate().isBefore(LocalDateTime.now()) && task.getStatus() != TaskStatus.DONE) {
                dueLabel.getStyleClass().add("overdue");
            } else if (task.getDueDate().toLocalDate().equals(LocalDateTime.now().toLocalDate())) {
                dueLabel.getStyleClass().add("today");
            }
            row.getChildren().addAll(icon, info, dueLabel);
        } else {
            row.getChildren().addAll(icon, info);
        }

        item.node = row;
        item.onActivate = () -> {
            addRecentSearch(searchField.getText().trim());
            close();
            if (onTaskSelect != null) {
                onTaskSelect.accept(task);
            } else if (onNavigate != null) {
                onNavigate.accept("tasks");
            }
        };

        row.setOnMouseClicked(e -> item.activate());
        row.setOnMouseEntered(e -> row.getStyleClass().add("selected"));
        row.setOnMouseExited(e -> row.getStyleClass().remove("selected"));

        currentResults.add(item);
        resultsContainer.getChildren().add(row);
    }

    private void addWikiResult(Wiki wiki) {
        SearchResultItem item = new SearchResultItem();

        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 12, 10, 12));
        row.getStyleClass().add("search-result-item");
        row.setCursor(javafx.scene.Cursor.HAND);

        FontIcon icon = new FontIcon(Feather.FILE_TEXT);
        icon.setIconSize(16);
        icon.getStyleClass().add("result-icon");

        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label title = new Label(wiki.getTitle());
        title.getStyleClass().add("result-title");

        // Preview
        if (wiki.getContent() != null && !wiki.getContent().isEmpty()) {
            String preview = wiki.getContent().replaceAll("\\s+", " ").trim();
            if (preview.length() > 50) {
                preview = preview.substring(0, 50) + "...";
            }
            Label previewLabel = new Label(preview);
            previewLabel.getStyleClass().add("result-preview");
            info.getChildren().addAll(title, previewLabel);
        } else {
            info.getChildren().add(title);
        }

        Label dateLabel = new Label();
        if (wiki.getUpdatedAt() != null) {
            dateLabel.setText(wiki.getUpdatedAt().format(DateTimeFormatter.ofPattern("MMM d")));
            dateLabel.getStyleClass().add("result-date");
        }

        row.getChildren().addAll(icon, info, dateLabel);

        item.node = row;
        item.onActivate = () -> {
            addRecentSearch(searchField.getText().trim());
            close();
            if (onWikiSelect != null) {
                onWikiSelect.accept(wiki);
            } else if (onNavigate != null) {
                onNavigate.accept("wiki");
            }
        };

        row.setOnMouseClicked(e -> item.activate());
        row.setOnMouseEntered(e -> row.getStyleClass().add("selected"));
        row.setOnMouseExited(e -> row.getStyleClass().remove("selected"));

        currentResults.add(item);
        resultsContainer.getChildren().add(row);
    }

    private void addEventResult(CalendarEvent event) {
        SearchResultItem item = new SearchResultItem();

        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 12, 10, 12));
        row.getStyleClass().add("search-result-item");
        row.setCursor(javafx.scene.Cursor.HAND);

        FontIcon icon = new FontIcon(Feather.CALENDAR);
        icon.setIconSize(16);
        icon.getStyleClass().add("result-icon");

        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label title = new Label(event.getTitle());
        title.getStyleClass().add("result-title");

        info.getChildren().add(title);

        Label dateLabel = new Label(event.getStartDateTime().format(DateTimeFormatter.ofPattern("MMM d, h:mm a")));
        dateLabel.getStyleClass().add("result-date");

        row.getChildren().addAll(icon, info, dateLabel);

        item.node = row;
        item.onActivate = () -> {
            addRecentSearch(searchField.getText().trim());
            close();
            if (onEventSelect != null) {
                onEventSelect.accept(event);
            } else if (onNavigate != null) {
                onNavigate.accept("calendar");
            }
        };

        row.setOnMouseClicked(e -> item.activate());
        row.setOnMouseEntered(e -> row.getStyleClass().add("selected"));
        row.setOnMouseExited(e -> row.getStyleClass().remove("selected"));

        currentResults.add(item);
        resultsContainer.getChildren().add(row);
    }

    private void addNoResultsMessage(String query) {
        VBox noResults = new VBox(12);
        noResults.setAlignment(Pos.CENTER);
        noResults.setPadding(new Insets(40, 20, 40, 20));
        noResults.getStyleClass().add("no-results");

        FontIcon icon = new FontIcon(Feather.SEARCH);
        icon.setIconSize(32);
        icon.getStyleClass().add("no-results-icon");

        Label message = new Label("No results for \"" + query + "\"");
        message.getStyleClass().add("no-results-text");

        Label hint = new Label("Try different keywords or use filters like task:, operation:, wiki:");
        hint.getStyleClass().add("no-results-hint");

        noResults.getChildren().addAll(icon, message, hint);
        resultsContainer.getChildren().add(noResults);
    }

    private String formatStatus(OperationStatus status) {
        if (status == null)
            return "Unknown";
        switch (status) {
            case ONGOING:
                return "In Progress";
            case IN_PROGRESS:
                return "On Hold";
            case END:
                return "Completed";
            default:
                return status.name();
        }
    }

    private String formatDueDate(LocalDateTime dueDate) {
        if (dueDate.toLocalDate().equals(LocalDateTime.now().toLocalDate())) {
            return "Due Today";
        } else if (dueDate.toLocalDate().equals(LocalDateTime.now().toLocalDate().plusDays(1))) {
            return "Due Tomorrow";
        } else if (dueDate.isBefore(LocalDateTime.now())) {
            return "Overdue";
        } else {
            return "Due " + dueDate.format(DateTimeFormatter.ofPattern("MMM d"));
        }
    }

    // Recent searches persistence
    private List<String> getRecentSearches() {
        Preferences prefs = Preferences.userNodeForPackage(GlobalSearchDialog.class);
        String saved = prefs.get(RECENT_SEARCHES_KEY, "");
        if (saved.isEmpty())
            return new ArrayList<>();

        List<String> searches = new ArrayList<>();
        for (String s : saved.split("\\|\\|")) {
            if (!s.trim().isEmpty()) {
                searches.add(s.trim());
            }
        }
        return searches;
    }

    private void addRecentSearch(String search) {
        if (search == null || search.isEmpty())
            return;

        Set<String> searches = new LinkedHashSet<>();
        searches.add(search);
        searches.addAll(getRecentSearches());

        List<String> limited = searches.stream().limit(MAX_RECENT_SEARCHES).collect(Collectors.toList());
        saveRecentSearches(limited);
    }

    private void removeRecentSearch(String search) {
        List<String> searches = getRecentSearches();
        searches.remove(search);
        saveRecentSearches(searches);
    }

    private void saveRecentSearches(List<String> searches) {
        Preferences prefs = Preferences.userNodeForPackage(GlobalSearchDialog.class);
        prefs.put(RECENT_SEARCHES_KEY, String.join("||", searches));
    }

    // Setters for callbacks
    public void setOnNavigate(Consumer<String> onNavigate) {
        this.onNavigate = onNavigate;
    }

    public void setOnTaskSelect(Consumer<Task> onTaskSelect) {
        this.onTaskSelect = onTaskSelect;
    }

    public void setOnWikiSelect(Consumer<Wiki> onWikiSelect) {
        this.onWikiSelect = onWikiSelect;
    }

    public void setOnOperationSelect(Consumer<Operation> onOperationSelect) {
        this.onOperationSelect = onOperationSelect;
    }

    public void setOnEventSelect(Consumer<CalendarEvent> onEventSelect) {
        this.onEventSelect = onEventSelect;
    }

    @Override
    public void showAndWait() {
        Platform.runLater(() -> searchField.requestFocus());
        super.showAndWait();
    }

    // Helper classes
    private static class SearchFilters {
        String typeFilter;
        String projectFilter;
        String priorityFilter;
        String dueFilter;
        String regionFilter;
        String cleanQuery = "";

        boolean hasFilters() {
            return typeFilter != null || projectFilter != null ||
                    priorityFilter != null || dueFilter != null || regionFilter != null;
        }
    }

    private static class SearchResultItem {
        javafx.scene.Node node;
        Runnable onActivate;

        void setSelected(boolean selected) {
            if (selected) {
                node.getStyleClass().add("selected");
            } else {
                node.getStyleClass().remove("selected");
            }
        }

        void activate() {
            if (onActivate != null) {
                onActivate.run();
            }
        }
    }
}
