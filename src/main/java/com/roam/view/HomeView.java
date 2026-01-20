package com.roam.view;

import com.roam.model.CalendarEvent;
import com.roam.model.JournalEntry;
import com.roam.model.Operation;
import com.roam.model.OperationStatus;
import com.roam.model.Task;
import com.roam.model.TaskStatus;
import com.roam.model.Wiki;
import com.roam.repository.RegionRepository;
import com.roam.repository.TaskRepository;
import com.roam.service.CalendarService;
import com.roam.service.CalendarServiceImpl;
import com.roam.service.JournalService;
import com.roam.service.JournalServiceImpl;
import com.roam.service.OperationService;
import com.roam.service.OperationServiceImpl;
import com.roam.service.SettingsService;
import com.roam.service.TaskService;
import com.roam.service.TaskServiceImpl;
import com.roam.service.WikiService;
import com.roam.service.WikiServiceImpl;
import com.roam.util.AnimationUtils;

import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.Map;

import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Home view that serves as the main dashboard of the Roam application.
 * <p>
 * This view provides an overview of the user's activities including:
 * <ul>
 * <li>Today's calendar events and upcoming schedule</li>
 * <li>Active tasks and their completion status</li>
 * <li>Recent journal entries</li>
 * <li>Operation summaries and statistics</li>
 * <li>Quick access to wiki pages</li>
 * </ul>
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public class HomeView extends StackPane {

    private final TaskService taskService;
    private final OperationService operationService;
    private final WikiService wikiService;
    private final CalendarService calendarService;
    private final JournalService journalService;
    private final RegionRepository regionRepository;
    private final TaskRepository taskRepository;

    private Consumer<String> onNavigate;
    private Consumer<Task> onTaskClick;
    private Consumer<Wiki> onWikiClick;
    private Consumer<Operation> onOperationClick;
    private Consumer<CalendarEvent> onEventClick;

    private VBox contentContainer;

    // Motivational quotes
    private static final String[] QUOTES = {
            "The secret of getting ahead is getting started. — Mark Twain",
            "Focus on being productive instead of busy. — Tim Ferriss",
            "Small daily improvements lead to stunning results. — Robin Sharma",
            "Your limitation—it's only your imagination.",
            "Push yourself, because no one else is going to do it for you.",
            "Great things never come from comfort zones.",
            "Dream it. Wish it. Do it.",
            "Success doesn't just find you. You have to go out and get it.",
            "The harder you work for something, the greater you'll feel when you achieve it.",
            "Don't stop when you're tired. Stop when you're done."
    };

    public HomeView() {
        this.taskService = new TaskServiceImpl();
        this.operationService = new OperationServiceImpl();
        this.wikiService = new WikiServiceImpl();
        this.calendarService = new CalendarServiceImpl();
        this.journalService = new JournalServiceImpl();
        this.regionRepository = new RegionRepository();
        this.taskRepository = new TaskRepository();

        getStyleClass().add("home-view");
        initializeUI();
        loadData();
    }

    public void setOnNavigate(Consumer<String> onNavigate) {
        this.onNavigate = onNavigate;
    }

    public void setOnTaskClick(Consumer<Task> onTaskClick) {
        this.onTaskClick = onTaskClick;
    }

    public void setOnWikiClick(Consumer<Wiki> onWikiClick) {
        this.onWikiClick = onWikiClick;
    }

    public void setOnOperationClick(Consumer<Operation> onOperationClick) {
        this.onOperationClick = onOperationClick;
    }

    public void setOnEventClick(Consumer<CalendarEvent> onEventClick) {
        this.onEventClick = onEventClick;
    }

    private void initializeUI() {
        contentContainer = new VBox(24);
        contentContainer.setPadding(new Insets(32, 40, 40, 40));
        contentContainer.getStyleClass().add("home-content");

        // Build all sections
        VBox greetingSection = createGreetingSection();
        HBox quickActionsSection = createQuickActionsBar();
        HBox focusStripSection = createTodaysFocusStrip();
        VBox regionsSection = createLifeRegionsSection();
        GridPane priorityGridSection = createPriorityItemsGrid();
        VBox statisticsSection = createStatisticsSection();

        contentContainer.getChildren().addAll(
                greetingSection,
                quickActionsSection,
                focusStripSection,
                regionsSection,
                priorityGridSection,
                statisticsSection);

        // Wrap in scroll pane
        ScrollPane scrollPane = new ScrollPane(contentContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.getStyleClass().add("home-scroll-pane");

        getChildren().add(scrollPane);
    }

    // ==================== GREETING SECTION ====================

    private VBox createGreetingSection() {
        VBox section = new VBox(12);
        section.getStyleClass().add("greeting-section");

        // Top row: Avatar + Greeting + Date/Actions
        HBox topRow = new HBox(20);
        topRow.setAlignment(Pos.CENTER_LEFT);

        // Profile Avatar (circular, clickable)
        StackPane avatar = createProfileAvatar();

        // Greeting and quote
        VBox greetingBox = new VBox(4);
        HBox.setHgrow(greetingBox, Priority.ALWAYS);

        Label greetingLabel = new Label(getGreeting());
        greetingLabel.getStyleClass().add("dashboard-greeting");

        // Motivational quote
        String quote = QUOTES[new Random().nextInt(QUOTES.length)];
        Label quoteLabel = new Label("\"" + quote + "\"");
        quoteLabel.getStyleClass().add("motivational-quote");
        quoteLabel.setWrapText(true);

        greetingBox.getChildren().addAll(greetingLabel, quoteLabel);

        // Date and celestial icon
        VBox dateBox = new VBox(8);
        dateBox.setAlignment(Pos.TOP_RIGHT);

        HBox dateRow = new HBox(10);
        dateRow.setAlignment(Pos.CENTER_RIGHT);

        StackPane celestialIcon = createCelestialIcon();

        LocalDate today = LocalDate.now();
        String dateStr = today.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));
        Label dateLabel = new Label(dateStr);
        dateLabel.getStyleClass().add("dashboard-date");

        dateRow.getChildren().addAll(dateLabel, celestialIcon);
        dateBox.getChildren().add(dateRow);

        topRow.getChildren().addAll(avatar, greetingBox, dateBox);
        section.getChildren().add(topRow);

        return section;
    }

    private StackPane createProfileAvatar() {
        StackPane avatarContainer = new StackPane();
        avatarContainer.setMinSize(64, 64);
        avatarContainer.setMaxSize(64, 64);
        avatarContainer.getStyleClass().add("profile-avatar");
        avatarContainer.setCursor(javafx.scene.Cursor.HAND);

        // Avatar circle (fallback)
        Circle avatarCircle = new Circle(32);
        avatarCircle.getStyleClass().add("avatar-circle");
        avatarCircle.setFill(Color.web("#3b82f6"));

        // User initials
        String userName = SettingsService.getInstance().getSettings().getUserName();
        String initials = getInitials(userName);
        Label initialsLabel = new Label(initials);
        initialsLabel.getStyleClass().add("avatar-initials");
        initialsLabel.setStyle("-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;");

        // Check if profile image exists and load it
        String profileImagePath = SettingsService.getInstance().getSettings().getProfileImagePath();
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

                    avatarContainer.getChildren().add(imageView);
                } catch (Exception ex) {
                    avatarContainer.getChildren().addAll(avatarCircle, initialsLabel);
                }
            } else {
                avatarContainer.getChildren().addAll(avatarCircle, initialsLabel);
            }
        } else {
            avatarContainer.getChildren().addAll(avatarCircle, initialsLabel);
        }

        // Click to open settings
        avatarContainer.setOnMouseClicked(e -> {
            if (onNavigate != null) {
                onNavigate.accept("settings");
            }
        });

        Tooltip.install(avatarContainer, new Tooltip("Click to edit profile"));

        return avatarContainer;
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

    private StackPane createCelestialIcon() {
        int hour = LocalDateTime.now().getHour();
        StackPane container = new StackPane();
        container.setMinSize(48, 48);
        container.setMaxSize(48, 48);
        container.setAlignment(Pos.CENTER);
        container.getStyleClass().add("celestial-container");

        Feather iconType;
        String timeClass;

        if (hour >= 5 && hour < 12) {
            iconType = Feather.SUN;
            timeClass = "morning";
        } else if (hour >= 12 && hour < 17) {
            iconType = Feather.SUN;
            timeClass = "afternoon";
        } else if (hour >= 17 && hour < 21) {
            iconType = Feather.SUNSET;
            timeClass = "evening";
        } else {
            iconType = Feather.MOON;
            timeClass = "night";
        }

        container.getStyleClass().add(timeClass);
        FontIcon icon = new FontIcon(iconType);
        icon.setIconSize(28);
        icon.getStyleClass().add("celestial-icon");
        container.getChildren().add(icon);

        addGentlePulse(icon);

        return container;
    }

    private String getGreeting() {
        int hour = LocalDateTime.now().getHour();
        String userName = SettingsService.getInstance().getSettings().getUserName();
        String namePart = (userName != null && !userName.isEmpty()) ? ", " + userName : "";

        if (hour >= 5 && hour < 12) {
            return "Good morning" + namePart + ".";
        } else if (hour >= 12 && hour < 17) {
            return "Good afternoon" + namePart + ".";
        } else if (hour >= 17 && hour < 21) {
            return "Good evening" + namePart + ".";
        } else {
            return "Good night" + namePart + ".";
        }
    }

    // ==================== QUICK ACTIONS BAR ====================

    private HBox createQuickActionsBar() {
        HBox section = new HBox(16);
        section.setAlignment(Pos.CENTER_LEFT);
        section.getStyleClass().add("quick-actions-bar");
        section.setPadding(new Insets(16, 0, 16, 0));

        // Create large, touch-friendly action buttons
        Button newTaskBtn = createQuickActionButton("New Task", Feather.PLUS_CIRCLE, "Ctrl+T", "action-primary");
        newTaskBtn.setOnAction(e -> navigateTo("tasks"));

        Button newWikiBtn = createQuickActionButton("New Wiki", Feather.FILE_PLUS, "Ctrl+N", "action-secondary");
        newWikiBtn.setOnAction(e -> navigateTo("wiki"));

        Button calendarBtn = createQuickActionButton("Calendar", Feather.CALENDAR, "Ctrl+E", "action-default");
        calendarBtn.setOnAction(e -> navigateTo("calendar"));

        Button journalBtn = createQuickActionButton("Journal", Feather.BOOK, "Ctrl+J", "action-default");
        journalBtn.setOnAction(e -> navigateTo("journal"));

        Button projectsBtn = createQuickActionButton("Operations", Feather.FOLDER, "Ctrl+P", "action-default");
        projectsBtn.setOnAction(e -> navigateTo("operations"));

        section.getChildren().addAll(newTaskBtn, newWikiBtn, calendarBtn, journalBtn, projectsBtn);

        return section;
    }

    private Button createQuickActionButton(String text, Feather icon, String shortcut, String styleClass) {
        Button btn = new Button(text);
        btn.getStyleClass().addAll("quick-action-btn", styleClass);
        btn.setMinHeight(48);
        btn.setPrefHeight(48);

        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconSize(20);
        fontIcon.getStyleClass().add("quick-action-icon");
        btn.setGraphic(fontIcon);

        // Tooltip with keyboard shortcut
        Tooltip tooltip = new Tooltip(text + " (" + shortcut + ")");
        Tooltip.install(btn, tooltip);

        AnimationUtils.addPressEffect(btn);

        return btn;
    }

    // ==================== TODAY'S FOCUS STRIP ====================

    private HBox createTodaysFocusStrip() {
        HBox section = new HBox(16);
        section.setAlignment(Pos.CENTER_LEFT);
        section.getStyleClass().add("focus-strip");

        // Get counts
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        // Today's events count
        long eventsToday = 0;
        try {
            eventsToday = calendarService.findByDateRange(startOfDay, endOfDay).size();
        } catch (Exception e) {
            // Ignore
        }

        // Tasks due today
        long tasksDueToday = 0;
        try {
            tasksDueToday = taskService.findDueBefore(today.plusDays(1)).stream()
                    .filter(t -> t.getStatus() != TaskStatus.DONE)
                    .filter(t -> t.getDueDate() != null && t.getDueDate().toLocalDate().equals(today))
                    .count();
        } catch (Exception e) {
            // Ignore
        }

        // Overdue tasks
        long overdueTasks = 0;
        try {
            overdueTasks = taskService.findOverdue().size();
        } catch (Exception e) {
            // Ignore
        }

        // Journal status
        boolean journalWrittenToday = false;
        try {
            List<JournalEntry> entries = journalService.findByDate(today);
            journalWrittenToday = !entries.isEmpty() && entries.get(0).getContent() != null
                    && !entries.get(0).getContent().trim().isEmpty();
        } catch (Exception e) {
            // Ignore
        }

        // Create focus cards
        VBox eventsCard = createFocusCard("Events Today", String.valueOf(eventsToday),
                Feather.CALENDAR, "#3b82f6", "calendar");

        VBox tasksCard = createFocusCard("Tasks Due", String.valueOf(tasksDueToday),
                Feather.CHECK_CIRCLE, "#10b981", "tasks");

        VBox overdueCard = createFocusCard("Overdue", String.valueOf(overdueTasks),
                Feather.ALERT_CIRCLE, overdueTasks > 0 ? "#ef4444" : "#9ca3af", "tasks");
        if (overdueTasks > 0) {
            overdueCard.getStyleClass().add("focus-card-alert");
        }

        VBox journalCard = createFocusCard("Journal",
                journalWrittenToday ? "Written ✓" : "Not yet",
                Feather.BOOK, journalWrittenToday ? "#10b981" : "#f59e0b", "journal");

        section.getChildren().addAll(eventsCard, tasksCard, overdueCard, journalCard);

        return section;
    }

    private VBox createFocusCard(String title, String value, Feather icon, String color, String navigateTo) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20, 32, 20, 32));
        card.getStyleClass().add("focus-card");
        card.setCursor(javafx.scene.Cursor.HAND);
        card.setMinWidth(140);

        // Icon
        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconSize(24);
        fontIcon.setIconColor(Color.web(color));

        // Value
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("focus-value");
        valueLabel.setStyle("-fx-text-fill: " + color + ";");

        // Title
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("focus-title");

        card.getChildren().addAll(fontIcon, valueLabel, titleLabel);

        card.setOnMouseClicked(e -> navigateTo(navigateTo));
        AnimationUtils.addLiftEffect(card);

        return card;
    }

    // ==================== LIFE REGIONS CARDS ====================

    private VBox createLifeRegionsSection() {
        VBox section = new VBox(16);
        section.getStyleClass().add("regions-section");

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label sectionTitle = new Label("Life Regions");
        sectionTitle.getStyleClass().add("section-title");

        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button viewAllBtn = createViewAllButton("operations");
        header.getChildren().addAll(sectionTitle, spacer, viewAllBtn);

        // Horizontal scrollable region cards
        ScrollPane regionsScroll = new ScrollPane();
        regionsScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        regionsScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        regionsScroll.setFitToHeight(true);
        regionsScroll.getStyleClass().add("regions-scroll");
        regionsScroll.setPannable(true);

        HBox regionsContainer = new HBox(16);
        regionsContainer.setAlignment(Pos.CENTER_LEFT);
        regionsContainer.setPadding(new Insets(8, 0, 8, 0));

        // Load regions and create cards
        try {
            List<com.roam.model.Region> regions = regionRepository.findAll();
            List<Operation> allOperations = operationService.findAll();
            List<Task> allTasks = taskService.findAll();

            for (com.roam.model.Region region : regions) {
                VBox regionCard = createRegionCard(region, allOperations, allTasks);
                regionsContainer.getChildren().add(regionCard);
            }
        } catch (Exception e) {
            Label errorLabel = new Label("Unable to load regions");
            errorLabel.getStyleClass().add("error-text");
            regionsContainer.getChildren().add(errorLabel);
        }

        regionsScroll.setContent(regionsContainer);
        section.getChildren().addAll(header, regionsScroll);

        return section;
    }

    private VBox createRegionCard(com.roam.model.Region region, List<Operation> allOps, List<Task> allTasks) {
        VBox card = new VBox(12);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new Insets(20));
        card.setMinWidth(200);
        card.setPrefWidth(220);
        card.getStyleClass().add("region-card");
        card.setCursor(javafx.scene.Cursor.HAND);

        // Region icon and name
        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Circle iconBg = new Circle(16);
        iconBg.setFill(Color.web(region.getColor(), 0.15));

        FontIcon regionIcon = new FontIcon(Feather.FOLDER);
        regionIcon.setIconSize(16);
        regionIcon.setIconColor(Color.web(region.getColor()));

        StackPane iconContainer = new StackPane(iconBg, regionIcon);

        Label nameLabel = new Label(region.getName());
        nameLabel.getStyleClass().add("region-name");

        titleRow.getChildren().addAll(iconContainer, nameLabel);

        // Counts
        long projectCount = allOps.stream()
                .filter(op -> region.getName().equalsIgnoreCase(op.getRegion()))
                .count();

        long taskCount = allTasks.stream()
                .filter(t -> region.getName().equalsIgnoreCase(t.getRegion()))
                .filter(t -> t.getStatus() != TaskStatus.DONE)
                .count();

        long completedTasks = allTasks.stream()
                .filter(t -> region.getName().equalsIgnoreCase(t.getRegion()))
                .filter(t -> t.getStatus() == TaskStatus.DONE)
                .count();

        long totalTasks = taskCount + completedTasks;
        double progress = totalTasks > 0 ? (double) completedTasks / totalTasks : 0;

        HBox statsRow = new HBox(16);
        statsRow.setAlignment(Pos.CENTER_LEFT);

        Label projectsLabel = new Label(projectCount + " operations");
        projectsLabel.getStyleClass().add("region-stat");

        Label tasksLabel = new Label(taskCount + " tasks");
        tasksLabel.getStyleClass().add("region-stat");

        statsRow.getChildren().addAll(projectsLabel, tasksLabel);

        // Progress bar
        ProgressBar progressBar = new ProgressBar(progress);
        progressBar.setPrefWidth(Double.MAX_VALUE);
        progressBar.getStyleClass().add("region-progress");

        Label progressLabel = new Label((int) (progress * 100) + "% completed");
        progressLabel.getStyleClass().add("region-progress-label");

        card.getChildren().addAll(titleRow, statsRow, progressBar, progressLabel);

        // Click handler
        card.setOnMouseClicked(e -> {
            if (onNavigate != null) {
                onNavigate.accept("operations");
            }
        });

        // Right-click context menu
        ContextMenu contextMenu = new ContextMenu();
        MenuItem newProjectItem = new MenuItem("New Operation");
        newProjectItem.setGraphic(new FontIcon(Feather.FOLDER_PLUS));
        newProjectItem.setOnAction(e -> navigateTo("operations"));

        MenuItem newTaskItem = new MenuItem("New Task");
        newTaskItem.setGraphic(new FontIcon(Feather.PLUS_CIRCLE));
        newTaskItem.setOnAction(e -> navigateTo("tasks"));

        contextMenu.getItems().addAll(newProjectItem, newTaskItem);
        card.setOnContextMenuRequested(e -> contextMenu.show(card, e.getScreenX(), e.getScreenY()));

        AnimationUtils.addLiftEffect(card);

        return card;
    }

    // ==================== PRIORITY ITEMS GRID (2x2) ====================

    private GridPane createPriorityItemsGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(24);
        grid.setVgap(24);
        grid.getStyleClass().add("priority-grid");

        // Make columns equal width
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        col1.setHgrow(Priority.ALWAYS);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        col2.setHgrow(Priority.ALWAYS);

        grid.getColumnConstraints().addAll(col1, col2);

        // Top Operations (0,0)
        VBox topProjectsCard = createPriorityCard("Top Operations", Feather.FOLDER, "operations");
        loadTopProjects(topProjectsCard);
        grid.add(topProjectsCard, 0, 0);

        // Tasks Due Soon (1,0)
        VBox tasksDueCard = createPriorityCard("Tasks Due Soon", Feather.CHECK_CIRCLE, "tasks");
        loadTasksDueSoon(tasksDueCard);
        grid.add(tasksDueCard, 1, 0);

        // Upcoming Events (0,1)
        VBox eventsCard = createPriorityCard("Upcoming Events", Feather.CALENDAR, "calendar");
        loadUpcomingEvents(eventsCard);
        grid.add(eventsCard, 0, 1);

        // Recent Wikis (1,1)
        VBox wikisCard = createPriorityCard("Recent Wikis", Feather.FILE_TEXT, "wiki");
        loadRecentWikis(wikisCard);
        grid.add(wikisCard, 1, 1);

        return grid;
    }

    private VBox createPriorityCard(String title, Feather icon, String navigateTo) {
        VBox card = new VBox(16);
        card.setPadding(new Insets(20));
        card.getStyleClass().add("priority-card");

        // Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        FontIcon titleIcon = new FontIcon(icon);
        titleIcon.setIconSize(18);
        titleIcon.getStyleClass().add("card-title-icon");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("card-title");

        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button viewAllBtn = new Button("See All");
        viewAllBtn.getStyleClass().add("see-all-button");
        FontIcon arrowIcon = new FontIcon(Feather.ARROW_RIGHT);
        arrowIcon.setIconSize(12);
        viewAllBtn.setGraphic(arrowIcon);
        viewAllBtn.setContentDisplay(javafx.scene.control.ContentDisplay.RIGHT);
        viewAllBtn.setOnAction(e -> navigateTo(navigateTo));

        header.getChildren().addAll(titleIcon, titleLabel, spacer, viewAllBtn);
        card.getChildren().add(header);

        return card;
    }

    private void loadTopProjects(VBox card) {
        VBox content = new VBox(8);
        content.getStyleClass().add("priority-content");

        try {
            List<Operation> topProjects = operationService.findAll().stream()
                    .filter(op -> op.getStatus() != OperationStatus.END)
                    .sorted((a, b) -> {
                        // Sort by priority, then by due date
                        if (a.getPriority() != null && b.getPriority() != null) {
                            return a.getPriority().compareTo(b.getPriority());
                        }
                        return 0;
                    })
                    .limit(4)
                    .collect(Collectors.toList());

            if (topProjects.isEmpty()) {
                content.getChildren().add(createEmptyState("No active operations", Feather.FOLDER));
            } else {
                for (Operation op : topProjects) {
                    content.getChildren().add(createProjectItem(op));
                }
            }
        } catch (Exception e) {
            content.getChildren().add(createErrorState());
        }

        card.getChildren().add(content);
    }

    private HBox createProjectItem(Operation op) {
        HBox item = new HBox(12);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(10, 12, 10, 12));
        item.getStyleClass().add("priority-item");
        item.setCursor(javafx.scene.Cursor.HAND);

        // Status indicator
        Circle statusDot = new Circle(5);
        statusDot.setFill(getOperationStatusColor(op.getStatus()));

        // Info
        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label nameLabel = new Label(op.getName());
        nameLabel.getStyleClass().add("item-title");

        Label statusLabel = new Label(formatOperationStatus(op.getStatus()));
        statusLabel.getStyleClass().add("item-subtitle");

        info.getChildren().addAll(nameLabel, statusLabel);

        // Due date
        if (op.getDueDate() != null) {
            Label dueLabel = new Label(op.getDueDate().format(DateTimeFormatter.ofPattern("MMM d")));
            dueLabel.getStyleClass().add("item-date");
            item.getChildren().addAll(statusDot, info, dueLabel);
        } else {
            item.getChildren().addAll(statusDot, info);
        }

        item.setOnMouseClicked(e -> {
            if (onOperationClick != null) {
                onOperationClick.accept(op);
            } else {
                navigateTo("operations");
            }
        });

        return item;
    }

    private void loadTasksDueSoon(VBox card) {
        VBox content = new VBox(8);
        content.getStyleClass().add("priority-content");

        try {
            LocalDate today = LocalDate.now();
            List<Task> tasksDue = taskService.findDueBefore(today.plusDays(7)).stream()
                    .filter(t -> t.getStatus() != TaskStatus.DONE)
                    .sorted((a, b) -> {
                        if (a.getDueDate() == null)
                            return 1;
                        if (b.getDueDate() == null)
                            return -1;
                        return a.getDueDate().compareTo(b.getDueDate());
                    })
                    .limit(4)
                    .collect(Collectors.toList());

            if (tasksDue.isEmpty()) {
                content.getChildren().add(createEmptyState("No tasks due soon", Feather.CHECK));
            } else {
                for (Task task : tasksDue) {
                    content.getChildren().add(createTaskItem(task));
                }
            }
        } catch (Exception e) {
            content.getChildren().add(createErrorState());
        }

        card.getChildren().add(content);
    }

    private HBox createTaskItem(Task task) {
        HBox item = new HBox(12);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(10, 12, 10, 12));
        item.getStyleClass().add("priority-item");
        item.setCursor(javafx.scene.Cursor.HAND);

        // Priority indicator
        Circle priorityDot = new Circle(5);
        setPriorityColor(priorityDot, task.getPriority());

        // Info
        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label titleLabel = new Label(task.getTitle());
        titleLabel.getStyleClass().add("item-title");

        Label statusLabel = new Label(formatStatus(task.getStatus()));
        statusLabel.getStyleClass().add("item-subtitle");

        info.getChildren().addAll(titleLabel, statusLabel);

        // Due date
        Label dueLabel = new Label();
        dueLabel.getStyleClass().add("item-date");
        if (task.getDueDate() != null) {
            dueLabel.setText(formatDueDate(task.getDueDate()));
            if (task.getDueDate().isBefore(LocalDateTime.now())) {
                dueLabel.getStyleClass().add("overdue");
            }
        }

        item.getChildren().addAll(priorityDot, info, dueLabel);

        item.setOnMouseClicked(e -> {
            if (onTaskClick != null) {
                onTaskClick.accept(task);
            } else {
                navigateTo("tasks");
            }
        });

        return item;
    }

    private void loadUpcomingEvents(VBox card) {
        VBox content = new VBox(8);
        content.getStyleClass().add("priority-content");

        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime weekFromNow = now.plusDays(7);

            List<CalendarEvent> events = calendarService.findByDateRange(now, weekFromNow).stream()
                    .sorted((a, b) -> a.getStartDateTime().compareTo(b.getStartDateTime()))
                    .limit(4)
                    .collect(Collectors.toList());

            if (events.isEmpty()) {
                content.getChildren().add(createEmptyState("No upcoming events", Feather.CALENDAR));
            } else {
                for (CalendarEvent event : events) {
                    content.getChildren().add(createEventItem(event));
                }
            }
        } catch (Exception e) {
            content.getChildren().add(createErrorState());
        }

        card.getChildren().add(content);
    }

    private HBox createEventItem(CalendarEvent event) {
        HBox item = new HBox(12);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(10, 12, 10, 12));
        item.getStyleClass().add("priority-item");
        item.setCursor(javafx.scene.Cursor.HAND);

        // Event icon
        FontIcon icon = new FontIcon(Feather.CALENDAR);
        icon.setIconSize(16);
        icon.getStyleClass().add("event-icon");

        // Info
        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label titleLabel = new Label(event.getTitle());
        titleLabel.getStyleClass().add("item-title");

        String timeStr = event.getStartDateTime().format(DateTimeFormatter.ofPattern("MMM d, h:mm a"));
        Label timeLabel = new Label(timeStr);
        timeLabel.getStyleClass().add("item-subtitle");

        info.getChildren().addAll(titleLabel, timeLabel);

        item.getChildren().addAll(icon, info);

        item.setOnMouseClicked(e -> {
            if (onEventClick != null) {
                onEventClick.accept(event);
            } else {
                navigateTo("calendar");
            }
        });

        return item;
    }

    private void loadRecentWikis(VBox card) {
        VBox content = new VBox(8);
        content.getStyleClass().add("priority-content");

        try {
            List<Wiki> recentWikis = wikiService.findAll().stream()
                    .sorted((a, b) -> {
                        if (b.getUpdatedAt() == null)
                            return -1;
                        if (a.getUpdatedAt() == null)
                            return 1;
                        return b.getUpdatedAt().compareTo(a.getUpdatedAt());
                    })
                    .limit(4)
                    .collect(Collectors.toList());

            if (recentWikis.isEmpty()) {
                content.getChildren().add(createEmptyState("No wikis yet", Feather.FILE_TEXT));
            } else {
                for (Wiki wiki : recentWikis) {
                    content.getChildren().add(createWikiItem(wiki));
                }
            }
        } catch (Exception e) {
            content.getChildren().add(createErrorState());
        }

        card.getChildren().add(content);
    }

    private HBox createWikiItem(Wiki wiki) {
        HBox item = new HBox(12);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(10, 12, 10, 12));
        item.getStyleClass().add("priority-item");
        item.setCursor(javafx.scene.Cursor.HAND);

        // Note icon
        FontIcon icon = new FontIcon(Feather.FILE_TEXT);
        icon.setIconSize(16);
        icon.getStyleClass().add("note-icon");

        // Info
        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label titleLabel = new Label(wiki.getTitle());
        titleLabel.getStyleClass().add("item-title");

        String preview = "";
        if (wiki.getContent() != null && !wiki.getContent().isEmpty()) {
            preview = wiki.getContent().replaceAll("\\s+", " ").trim();
            if (preview.length() > 40) {
                preview = preview.substring(0, 40) + "...";
            }
        }
        Label previewLabel = new Label(preview);
        previewLabel.getStyleClass().add("item-subtitle");

        info.getChildren().addAll(titleLabel, previewLabel);

        // Date
        Label dateLabel = new Label();
        dateLabel.getStyleClass().add("item-date");
        if (wiki.getUpdatedAt() != null) {
            dateLabel.setText(formatRelativeDate(wiki.getUpdatedAt()));
        }

        item.getChildren().addAll(icon, info, dateLabel);

        item.setOnMouseClicked(e -> {
            if (onWikiClick != null) {
                onWikiClick.accept(wiki);
            } else {
                navigateTo("wiki");
            }
        });

        return item;
    }

    // ==================== STATISTICS SECTION ====================

    private VBox createStatisticsSection() {
        VBox section = new VBox(20);
        section.getStyleClass().add("statistics-section");
        section.setPadding(new Insets(10, 0, 20, 0));

        // Section Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label sectionTitle = new Label("Statistics Overview");
        sectionTitle.getStyleClass().add("section-title");

        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(sectionTitle, spacer);
        section.getChildren().add(header);

        // Fetch all data
        List<Wiki> allWikis = wikiService.findAll();
        List<JournalEntry> allJournals = journalService.findAll();
        List<Operation> allOps = operationService.findAll();
        List<Task> allTasks = taskService.findAll();

        long totalWikis = allWikis.size();
        long totalJournals = allJournals.size();
        long totalEvents = calendarService.findAll().size();
        long totalOperations = allOps.size();
        long totalTasks = allTasks.size();

        long doneTasks = taskRepository.countByStatus(TaskStatus.DONE);
        long todoTasks = taskRepository.countByStatus(TaskStatus.TODO);
        long inProgressTasks = taskRepository.countByStatus(TaskStatus.IN_PROGRESS);
        long overdueTasks = taskRepository.countOverdue();
        long highPriorityTasks = taskRepository.countHighPriority();

        // Calculate completion rate
        double completionRate = totalTasks > 0 ? (double) doneTasks / totalTasks * 100 : 0;

        // Count operations by status
        long ongoingOps = allOps.stream().filter(o -> o.getStatus() == OperationStatus.ONGOING).count();
        long inProgressOps = allOps.stream().filter(o -> o.getStatus() == OperationStatus.IN_PROGRESS).count();
        long endedOps = allOps.stream().filter(o -> o.getStatus() == OperationStatus.END).count();

        // ========== OVERVIEW CARDS ==========
        Label overviewLabel = new Label("Overview");
        overviewLabel.getStyleClass().add("stats-subsection-title");
        section.getChildren().add(overviewLabel);

        FlowPane overviewGrid = new FlowPane();
        overviewGrid.setHgap(16);
        overviewGrid.setVgap(16);
        overviewGrid.getStyleClass().add("stats-grid");

        overviewGrid.getChildren().addAll(
                createStatCard("Operations", formatStatCount(totalOperations), Feather.GIT_BRANCH, "#4285f4"),
                createStatCard("Tasks", formatStatCount(totalTasks), Feather.CHECK_SQUARE, "#388E3C"),
                createStatCard("Wikis", formatStatCount(totalWikis), Feather.FILE_TEXT, "#9c27b0"),
                createStatCard("Journals", formatStatCount(totalJournals), Feather.BOOK, "#F57C00"),
                createStatCard("Events", formatStatCount(totalEvents), Feather.CALENDAR, "#D32F2F"));
        section.getChildren().add(overviewGrid);

        // ========== TASK METRICS ==========
        Label tasksLabel = new Label("Task Metrics");
        tasksLabel.getStyleClass().add("stats-subsection-title");
        section.getChildren().add(tasksLabel);

        FlowPane taskMetricsGrid = new FlowPane();
        taskMetricsGrid.setHgap(16);
        taskMetricsGrid.setVgap(16);
        taskMetricsGrid.getStyleClass().add("stats-grid");

        taskMetricsGrid.getChildren().addAll(
                createCompletionCard(completionRate, doneTasks, totalTasks),
                createStatCard("To Do", formatStatCount(todoTasks), Feather.CIRCLE, "#F57C00"),
                createStatCard("In Progress", formatStatCount(inProgressTasks), Feather.LOADER, "#1976D2"),
                createStatCard("Completed", formatStatCount(doneTasks), Feather.CHECK_CIRCLE, "#388E3C"),
                createAlertCard("Overdue", formatStatCount(overdueTasks), Feather.ALERT_TRIANGLE, "#D32F2F"),
                createAlertCard("High Priority", formatStatCount(highPriorityTasks), Feather.FLAG, "#C62828"));
        section.getChildren().add(taskMetricsGrid);

        // ========== CHARTS SECTION ==========
        Label chartsLabel = new Label("Visual Breakdown");
        chartsLabel.getStyleClass().add("stats-subsection-title");
        section.getChildren().add(chartsLabel);

        HBox chartsBox = new HBox(20);
        chartsBox.setAlignment(Pos.TOP_LEFT);
        chartsBox.getStyleClass().add("charts-container");

        // Task Status Chart
        VBox taskChartBox = createTaskStatusChart(todoTasks, inProgressTasks, doneTasks);

        // Tasks by Operation Chart
        VBox opsChartBox = createOperationsChart(allTasks, allOps);

        // Tasks by Priority Chart
        VBox priorityChartBox = createPriorityChart(allTasks);

        chartsBox.getChildren().addAll(taskChartBox, opsChartBox, priorityChartBox);
        section.getChildren().add(chartsBox);

        // ========== OPERATIONS STATUS ==========
        Label opsLabel = new Label("Operation Status");
        opsLabel.getStyleClass().add("stats-subsection-title");
        section.getChildren().add(opsLabel);

        FlowPane opsGrid = new FlowPane();
        opsGrid.setHgap(16);
        opsGrid.setVgap(16);
        opsGrid.getStyleClass().add("stats-grid");

        opsGrid.getChildren().addAll(
                createStatCard("Ongoing", formatStatCount(ongoingOps), Feather.PLAY, "#4285f4"),
                createStatCard("In Progress", formatStatCount(inProgressOps), Feather.CLOCK, "#F57C00"),
                createStatCard("Completed", formatStatCount(endedOps), Feather.CHECK_CIRCLE, "#388E3C"));
        section.getChildren().add(opsGrid);

        return section;
    }

    /**
     * Creates a stat card with icon, value, and label
     */
    private VBox createStatCard(String label, String value, Feather icon, String colorHex) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(20));
        card.setPrefWidth(180);
        card.setMinWidth(160);
        card.getStyleClass().add("stat-card");

        // Icon in colored circle
        StackPane iconContainer = new StackPane();
        Circle iconBg = new Circle(20);
        iconBg.setFill(Color.web(colorHex, 0.15));

        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconSize(20);
        fontIcon.setIconColor(Color.web(colorHex));

        iconContainer.getChildren().addAll(iconBg, fontIcon);

        // Value
        Label valLabel = new Label(value);
        valLabel.getStyleClass().add("stat-value");

        // Label
        Label textLabel = new Label(label);
        textLabel.getStyleClass().add("stat-label");

        card.getChildren().addAll(iconContainer, valLabel, textLabel);
        return card;
    }

    /**
     * Creates an alert-style card for warning metrics (overdue, high priority)
     */
    private VBox createAlertCard(String label, String value, Feather icon, String colorHex) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(20));
        card.setPrefWidth(180);
        card.setMinWidth(160);
        card.getStyleClass().add("stat-alert-card");
        card.setStyle(card.getStyle() + "-stat-alert-color: " + colorHex + ";");

        // Icon in colored circle
        StackPane iconContainer = new StackPane();
        Circle iconBg = new Circle(20);
        iconBg.setFill(Color.web(colorHex, 0.25));

        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconSize(20);
        fontIcon.setIconColor(Color.web(colorHex));

        iconContainer.getChildren().addAll(iconBg, fontIcon);

        // Value
        Label valLabel = new Label(value);
        valLabel.getStyleClass().add("stat-alert-value");
        valLabel.setStyle("-fx-text-fill: " + colorHex + ";");

        // Label
        Label textLabel = new Label(label);
        textLabel.getStyleClass().add("stat-alert-label");
        textLabel.setStyle("-fx-text-fill: " + colorHex + ";");

        card.getChildren().addAll(iconContainer, valLabel, textLabel);
        return card;
    }

    /**
     * Creates a completion rate card with progress bar
     */
    private VBox createCompletionCard(double completionRate, long completed, long total) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(20));
        card.setPrefWidth(220);
        card.setMinWidth(200);
        card.getStyleClass().add("stat-completion-card");

        // Header with icon
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        StackPane iconContainer = new StackPane();
        Circle iconBg = new Circle(16);
        iconBg.setFill(Color.web("#388E3C", 0.15));

        FontIcon icon = new FontIcon(Feather.TRENDING_UP);
        icon.setIconSize(16);
        icon.setIconColor(Color.web("#388E3C"));

        iconContainer.getChildren().addAll(iconBg, icon);

        Label titleLabel = new Label("Completion Rate");
        titleLabel.getStyleClass().add("stat-completion-title");

        header.getChildren().addAll(iconContainer, titleLabel);

        // Percentage value
        Label valLabel = new Label(String.format("%.0f%%", completionRate));
        valLabel.getStyleClass().add("stat-completion-value");

        // Progress bar
        ProgressBar progressBar = new ProgressBar(completionRate / 100.0);
        progressBar.setPrefWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(8);
        progressBar.getStyleClass().add("stat-progress-bar");

        // Completed count
        Label countLabel = new Label(completed + " of " + total + " tasks");
        countLabel.getStyleClass().add("stat-completion-count");

        card.getChildren().addAll(header, valLabel, progressBar, countLabel);
        return card;
    }

    private String formatStatCount(long count) {
        if (count >= 1000000) {
            return String.format("%.1fM", count / 1000000.0);
        } else if (count >= 1000) {
            return String.format("%.1fK", count / 1000.0);
        }
        return String.valueOf(count);
    }

    private VBox createTaskStatusChart(long todo, long inProgress, long done) {
        VBox container = new VBox(12);
        container.setPadding(new Insets(20));
        container.getStyleClass().add("chart-card");

        Label header = new Label("Task Status");
        header.getStyleClass().add("chart-title");

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("To Do", todo),
                new PieChart.Data("In Progress", inProgress),
                new PieChart.Data("Done", done));

        PieChart chart = new PieChart(pieChartData);
        chart.setLabelsVisible(true);
        chart.setLegendVisible(true);
        chart.setPrefSize(280, 250);
        chart.setMaxSize(280, 250);
        chart.getStyleClass().add("stats-pie-chart");

        container.getChildren().addAll(header, chart);
        return container;
    }

    private VBox createOperationsChart(List<Task> allTasks, List<Operation> allOps) {
        VBox container = new VBox(12);
        container.setPadding(new Insets(20));
        container.getStyleClass().add("chart-card");

        Label header = new Label("Tasks by Operation");
        header.getStyleClass().add("chart-title");

        Map<Long, String> opNames = allOps.stream()
                .collect(Collectors.toMap(Operation::getId, Operation::getName));

        Map<String, Long> tasksByOp = allTasks.stream()
                .filter(t -> t.getOperationId() != null)
                .collect(Collectors.groupingBy(
                        t -> opNames.getOrDefault(t.getOperationId(), "Unknown"),
                        Collectors.counting()));

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        tasksByOp.forEach((name, count) -> pieChartData.add(new PieChart.Data(name, count)));

        // Show empty state if no data
        if (pieChartData.isEmpty()) {
            Label emptyLabel = new Label("No tasks assigned to operations");
            emptyLabel.getStyleClass().add("chart-empty-label");
            container.getChildren().addAll(header, emptyLabel);
            return container;
        }

        PieChart chart = new PieChart(pieChartData);
        chart.setLabelsVisible(true);
        chart.setLegendVisible(true);
        chart.setPrefSize(280, 250);
        chart.setMaxSize(280, 250);
        chart.getStyleClass().add("stats-pie-chart");

        container.getChildren().addAll(header, chart);
        return container;
    }

    private VBox createPriorityChart(List<Task> allTasks) {
        VBox container = new VBox(12);
        container.setPadding(new Insets(20));
        container.getStyleClass().add("chart-card");

        Label header = new Label("Tasks by Priority");
        header.getStyleClass().add("chart-title");

        // Count tasks by priority
        long highCount = allTasks.stream()
                .filter(t -> t.getPriority() == com.roam.model.Priority.HIGH).count();
        long mediumCount = allTasks.stream()
                .filter(t -> t.getPriority() == com.roam.model.Priority.MEDIUM).count();
        long lowCount = allTasks.stream()
                .filter(t -> t.getPriority() == com.roam.model.Priority.LOW).count();
        long noPriorityCount = allTasks.stream()
                .filter(t -> t.getPriority() == null).count();

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        if (highCount > 0)
            pieChartData.add(new PieChart.Data("High", highCount));
        if (mediumCount > 0)
            pieChartData.add(new PieChart.Data("Medium", mediumCount));
        if (lowCount > 0)
            pieChartData.add(new PieChart.Data("Low", lowCount));
        if (noPriorityCount > 0)
            pieChartData.add(new PieChart.Data("None", noPriorityCount));

        // Show empty state if no data
        if (pieChartData.isEmpty()) {
            Label emptyLabel = new Label("No tasks with priority");
            emptyLabel.getStyleClass().add("chart-empty-label");
            container.getChildren().addAll(header, emptyLabel);
            return container;
        }

        PieChart chart = new PieChart(pieChartData);
        chart.setLabelsVisible(true);
        chart.setLegendVisible(true);
        chart.setPrefSize(280, 250);
        chart.setMaxSize(280, 250);
        chart.getStyleClass().add("stats-pie-chart");

        container.getChildren().addAll(header, chart);
        return container;
    }

    // ==================== HELPER METHODS ====================

    private Button createViewAllButton(String navigateTo) {
        Button btn = new Button("View all");
        btn.getStyleClass().add("view-all-button");
        FontIcon arrowIcon = new FontIcon(Feather.ARROW_RIGHT);
        arrowIcon.setIconSize(12);
        btn.setGraphic(arrowIcon);
        btn.setContentDisplay(javafx.scene.control.ContentDisplay.RIGHT);
        btn.setOnAction(e -> navigateTo(navigateTo));
        return btn;
    }

    private HBox createEmptyState(String message, Feather icon) {
        HBox emptyState = new HBox(10);
        emptyState.setAlignment(Pos.CENTER_LEFT);
        emptyState.getStyleClass().add("empty-state");
        emptyState.setPadding(new Insets(12));

        FontIcon emptyIcon = new FontIcon(icon);
        emptyIcon.setIconSize(18);
        emptyIcon.getStyleClass().add("empty-state-icon");

        Label emptyLabel = new Label(message);
        emptyLabel.getStyleClass().add("empty-state-text");

        emptyState.getChildren().addAll(emptyIcon, emptyLabel);
        return emptyState;
    }

    private Label createErrorState() {
        Label errorLabel = new Label("Unable to load data");
        errorLabel.getStyleClass().add("error-text");
        return errorLabel;
    }

    private void navigateTo(String destination) {
        if (onNavigate != null) {
            onNavigate.accept(destination);
        }
    }

    private void addGentlePulse(javafx.scene.Node node) {
        ScaleTransition pulse = new ScaleTransition(Duration.seconds(2.5), node);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.08);
        pulse.setToY(1.08);
        pulse.setCycleCount(ScaleTransition.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();
    }

    private void setPriorityColor(Circle dot, com.roam.model.Priority priority) {
        if (priority == null) {
            dot.setFill(Color.web("#9E9E9E"));
            return;
        }
        switch (priority) {
            case HIGH:
                dot.setFill(Color.web("#ef4444"));
                break;
            case MEDIUM:
                dot.setFill(Color.web("#f59e0b"));
                break;
            case LOW:
                dot.setFill(Color.web("#10b981"));
                break;
            default:
                dot.setFill(Color.web("#9E9E9E"));
        }
    }

    private Color getOperationStatusColor(OperationStatus status) {
        if (status == null)
            return Color.web("#3b82f6");
        switch (status) {
            case ONGOING:
                return Color.web("#3b82f6");
            case IN_PROGRESS:
                return Color.web("#f59e0b");
            case END:
                return Color.web("#10b981");
            default:
                return Color.web("#3b82f6");
        }
    }

    private String formatOperationStatus(OperationStatus status) {
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

    private String formatStatus(TaskStatus status) {
        if (status == null)
            return "Todo";
        switch (status) {
            case TODO:
                return "Todo";
            case IN_PROGRESS:
                return "In Progress";
            case DONE:
                return "Done";
            default:
                return status.name();
        }
    }

    private String formatDueDate(LocalDateTime dueDate) {
        LocalDate today = LocalDate.now();
        LocalDate dueLocalDate = dueDate.toLocalDate();

        if (dueLocalDate.equals(today)) {
            return "Today";
        } else if (dueLocalDate.equals(today.plusDays(1))) {
            return "Tomorrow";
        } else if (dueLocalDate.isBefore(today)) {
            return "Overdue";
        } else {
            return dueDate.format(DateTimeFormatter.ofPattern("MMM d"));
        }
    }

    private String formatRelativeDate(LocalDateTime dateTime) {
        LocalDate today = LocalDate.now();
        LocalDate date = dateTime.toLocalDate();

        if (date.equals(today)) {
            return "Today";
        } else if (date.equals(today.minusDays(1))) {
            return "Yesterday";
        } else {
            return dateTime.format(DateTimeFormatter.ofPattern("MMM d"));
        }
    }

    private void loadData() {
        // Data is loaded in each section's create method
        // Animate entrance after render
        Platform.runLater(this::animateEntrance);
    }

    private void animateEntrance() {
        AnimationUtils.staggerSlideUp(contentContainer, Duration.millis(100), Duration.millis(50));
    }

    /**
     * Refresh all dashboard data
     */
    public void refresh() {
        contentContainer.getChildren().clear();

        VBox greetingSection = createGreetingSection();
        HBox quickActionsSection = createQuickActionsBar();
        HBox focusStripSection = createTodaysFocusStrip();
        VBox regionsSection = createLifeRegionsSection();
        GridPane priorityGridSection = createPriorityItemsGrid();
        VBox statisticsSection = createStatisticsSection();

        contentContainer.getChildren().addAll(
                greetingSection,
                quickActionsSection,
                focusStripSection,
                regionsSection,
                priorityGridSection,
                statisticsSection);

        Platform.runLater(this::animateEntrance);
    }
}
