package com.roam.view;

import com.roam.controller.CalendarController;
import com.roam.controller.OperationDetailController;
import com.roam.controller.WikiController;
import com.roam.model.CalendarEvent;
import com.roam.model.CalendarSource;
import com.roam.model.Operation;
import com.roam.model.Priority;
import com.roam.model.Task;
import com.roam.model.TaskStatus;
import com.roam.model.Wiki;
import com.roam.util.AnimationUtils;
import com.roam.view.components.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;
import org.kordamp.ikonli.boxicons.BoxiconsSolid;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Detailed view for displaying and managing an individual operation.
 * <p>
 * This view provides a comprehensive interface for viewing and editing
 * operation details,
 * including associated events, tasks, and wiki notes. The view is organized
 * into tabbed
 * sections for easy navigation between different aspects of the operation.
 * </p>
 * <p>
 * Features include:
 * </p>
 * <ul>
 * <li>Operation information card with editable fields</li>
 * <li>Events tab with agenda-style calendar event listing</li>
 * <li>Tasks tab with status-based task grouping</li>
 * <li>Wiki tab for associated documentation</li>
 * <li>Navigation support with back action callback</li>
 * </ul>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 * @see com.roam.controller.OperationDetailController
 * @see com.roam.view.components.OperationInfoCard
 */
public class OperationDetailView extends StackPane {

    private final OperationDetailController controller;
    private final CalendarController calendarController;
    private final WikiController wikiController;
    private final Runnable onNavigateBack;
    private final BorderPane contentPane;

    private OperationInfoCard infoCard;
    private ScrollPane mainScrollPane;

    // Events Tab components
    private VBox eventsAgendaContainer;

    // Tasks Tab components
    private VBox tasksAgendaContainer;

    // Wiki Tab components
    private StackPane wikiContainer;
    private FlowPane pinnedWikisPane;
    private FlowPane otherWikisPane;
    private VBox pinnedSection;
    private VBox othersSection;
    private VBox wikiEmptyState;
    private StackPane wikiModalOverlay;
    private WikiNoteModal wikiModal;
    private TextField wikiSearchField;
    private TextField quickWikiField;

    public OperationDetailView(Operation operation, Runnable onNavigateBack) {
        this.controller = new OperationDetailController(operation);
        this.calendarController = new CalendarController();
        this.wikiController = new WikiController();
        this.onNavigateBack = onNavigateBack;
        this.contentPane = new BorderPane();
        getChildren().add(contentPane);

        initialize();

        // Add listeners for responsive scaling
        this.widthProperty().addListener((obs, oldVal, newVal) -> scaleContent());
        this.heightProperty().addListener((obs, oldVal, newVal) -> scaleContent());
    }

    private void scaleContent() {
        double width = getWidth();
        double height = getHeight();

        double contentWidth = contentPane.getLayoutBounds().getWidth();
        double contentHeight = contentPane.getLayoutBounds().getHeight();

        if (contentWidth == 0 || contentHeight == 0)
            return;

        double scaleX = width < contentWidth ? width / contentWidth : 1.0;
        double scaleY = height < contentHeight ? height / contentHeight : 1.0;
        double scale = Math.min(scaleX, scaleY);

        contentPane.setScaleX(scale);
        contentPane.setScaleY(scale);
    }

    private void initialize() {
        contentPane.setStyle("-fx-background-color: -roam-bg-primary;");

        // Set data change listener
        controller.setOnDataChanged(this::refreshData);

        // Create breadcrumb
        Breadcrumb breadcrumb = new Breadcrumb(controller.getOperation().getName(), onNavigateBack);
        contentPane.setTop(breadcrumb);

        // Create center content
        VBox centerContent = createCenterContent();

        // Wrap in ScrollPane for better UX
        mainScrollPane = new ScrollPane(centerContent);
        mainScrollPane.setFitToWidth(true);
        mainScrollPane.setFitToHeight(false);
        mainScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        mainScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        mainScrollPane.setStyle("-fx-background-color: -roam-bg-primary; -fx-background: -roam-bg-primary;");

        contentPane.setCenter(mainScrollPane);
    }

    private VBox createCenterContent() {
        VBox content = new VBox(0);
        content.setPadding(new Insets(20));

        // Operation info card
        infoCard = new OperationInfoCard(controller.getOperation(), this::editOperation);

        // TabPane for Tasks and Wiki
        TabPane tabPane = createTabPane();
        VBox.setVgrow(tabPane, javafx.scene.layout.Priority.ALWAYS);

        content.getChildren().addAll(infoCard, tabPane);
        return content;
    }

    private TabPane createTabPane() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-font-family: 'Poppins';");
        tabPane.setMinHeight(800);

        // Tab 1: Events Agenda
        Tab eventsTab = new Tab("Events");
        FontIcon eventsIcon = new FontIcon(Feather.CALENDAR);
        eventsIcon.setIconSize(16);
        eventsTab.setGraphic(eventsIcon);
        eventsTab.setContent(createEventsAgendaView());

        // Tab 2: Tasks Agenda
        Tab tasksTab = new Tab("Tasks");
        FontIcon tasksIcon = new FontIcon(Feather.CHECK_SQUARE);
        tasksIcon.setIconSize(16);
        tasksTab.setGraphic(tasksIcon);
        tasksTab.setContent(createTasksAgendaView());

        // Tab 3: Wiki (Card-based)
        Tab wikiTab = new Tab("Wiki");
        FontIcon wikiIcon = new FontIcon(Feather.FILE_TEXT);
        wikiIcon.setIconSize(16);
        wikiTab.setGraphic(wikiIcon);
        wikiTab.setContent(createWikiCardView());

        tabPane.getTabs().addAll(eventsTab, tasksTab, wikiTab);
        tabPane.getStyleClass().add("custom-tab-pane");

        return tabPane;
    }

    // ==================== EVENTS AGENDA TAB ====================

    private BorderPane createEventsAgendaView() {
        BorderPane view = new BorderPane();
        view.setStyle("-fx-background-color: -roam-bg-primary;");

        // Toolbar with new event button
        HBox toolbar = createEventsToolbar();
        view.setTop(toolbar);

        // Agenda container
        eventsAgendaContainer = new VBox(0);
        eventsAgendaContainer.setStyle("-fx-background-color: -roam-bg-primary;");

        ScrollPane scrollPane = new ScrollPane(eventsAgendaContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        view.setCenter(scrollPane);

        // Load events for this operation
        loadOperationEvents();

        return view;
    }

    private HBox createEventsToolbar() {
        HBox toolbar = new HBox(15);
        toolbar.setPadding(new Insets(15, 20, 15, 20));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle("-fx-background-color: -roam-gray-bg;");

        // New Event button
        Button newEventBtn = new Button("New Event");
        FontIcon plusIcon = new FontIcon(Feather.PLUS);
        plusIcon.setIconSize(16);
        newEventBtn.setGraphic(plusIcon);
        newEventBtn.getStyleClass().addAll("pill-button", "primary");
        newEventBtn.setPrefHeight(40);
        newEventBtn.setMinWidth(120);
        newEventBtn.setOnAction(e -> {
            calendarController.createEvent(LocalDate.now());
            loadOperationEvents();
        });
        AnimationUtils.addPressEffect(newEventBtn);

        toolbar.getChildren().add(newEventBtn);
        return toolbar;
    }

    private void loadOperationEvents() {
        eventsAgendaContainer.getChildren().clear();

        // Get events linked to this operation
        Long operationId = controller.getOperation().getId();
        List<CalendarEvent> operationEvents = calendarController.getAllEvents().stream()
                .filter(e -> operationId.equals(e.getOperationId()))
                .sorted(Comparator.comparing(CalendarEvent::getStartDateTime))
                .collect(Collectors.toList());

        if (operationEvents.isEmpty()) {
            showEventsEmptyState();
            return;
        }

        // Group events by date
        LocalDate currentGroupDate = null;

        for (CalendarEvent event : operationEvents) {
            LocalDate eventDate = event.getStartDateTime().toLocalDate();

            if (currentGroupDate == null || !currentGroupDate.equals(eventDate)) {
                currentGroupDate = eventDate;
                renderEventsDateHeader(currentGroupDate);
            }

            renderEventItem(event);
        }
    }

    private void showEventsEmptyState() {
        StackPane emptyWrapper = new StackPane();
        emptyWrapper.setAlignment(Pos.CENTER);
        emptyWrapper.prefWidthProperty().bind(eventsAgendaContainer.widthProperty());
        emptyWrapper.setMinHeight(400);

        VBox emptyState = new VBox(20);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPadding(new Insets(100));

        FontIcon icon = new FontIcon(Feather.CALENDAR);
        icon.setIconSize(72);
        icon.setIconColor(Color.web("#6b7280"));

        Label title = new Label("No events for this operation");
        title.setFont(Font.font("Poppins Bold", 20));
        title.setStyle("-fx-text-fill: -roam-text-secondary;");

        Label description = new Label("Click '+ New Event' to create your first event");
        description.setFont(Font.font("Poppins", 14));
        description.setStyle("-fx-text-fill: -roam-text-hint;");

        emptyState.getChildren().addAll(icon, title, description);
        emptyWrapper.getChildren().add(emptyState);
        eventsAgendaContainer.getChildren().add(emptyWrapper);
    }

    private void renderEventsDateHeader(LocalDate date) {
        HBox dateHeader = new HBox(15);
        dateHeader.setPadding(new Insets(20, 20, 10, 20));
        dateHeader.setAlignment(Pos.CENTER_LEFT);
        dateHeader.setStyle(
                "-fx-background-color: -roam-gray-bg; " +
                        "-fx-border-color: -roam-border; " +
                        "-fx-border-width: 1 0 0 0;");

        // Day number
        Label dayNum = new Label(String.valueOf(date.getDayOfMonth()));
        dayNum.setFont(Font.font("Poppins Bold", 24));

        boolean isToday = date.equals(LocalDate.now());
        if (isToday) {
            dayNum.setStyle(
                    "-fx-text-fill: #FFFFFF; " +
                            "-fx-background-color: -roam-blue; " +
                            "-fx-background-radius: 50%; " +
                            "-fx-min-width: 45; " +
                            "-fx-min-height: 45; " +
                            "-fx-alignment: center;");
        } else {
            dayNum.setStyle("-fx-text-fill: -roam-text-primary;");
        }

        // Date info
        VBox dateInfo = new VBox(2);
        Label dayName = new Label(date.getDayOfWeek().toString().substring(0, 3));
        dayName.setFont(Font.font("Poppins Medium", 12));
        dayName.setStyle("-fx-text-fill: -roam-text-secondary;");

        Label monthYear = new Label(date.format(DateTimeFormatter.ofPattern("MMM yyyy")));
        monthYear.setFont(Font.font("Poppins", 12));
        monthYear.setStyle("-fx-text-fill: -roam-text-hint;");

        dateInfo.getChildren().addAll(dayName, monthYear);

        dateHeader.getChildren().addAll(dayNum, dateInfo);
        eventsAgendaContainer.getChildren().add(dateHeader);
    }

    private void renderEventItem(CalendarEvent event) {
        HBox item = new HBox(15);
        item.setPadding(new Insets(15, 20, 15, 20));
        item.setAlignment(Pos.CENTER_LEFT);
        item.setStyle("-fx-background-color: -roam-bg-primary; -fx-cursor: hand;");

        // Calculate duration
        long durationMinutes = java.time.Duration.between(
                event.getStartDateTime(), event.getEndDateTime()).toMinutes();
        double durationHours = durationMinutes / 60.0;

        // Time box
        VBox timeBox = new VBox(2);
        timeBox.setMinWidth(80);
        timeBox.setMaxWidth(80);

        if (event.getIsAllDay()) {
            Label allDayLabel = new Label("All Day");
            allDayLabel.setFont(Font.font("Poppins Medium", 12));
            allDayLabel.setStyle("-fx-text-fill: -roam-text-secondary;");
            timeBox.getChildren().add(allDayLabel);
        } else {
            Label startTime = new Label(
                    event.getStartDateTime().toLocalTime().format(DateTimeFormatter.ofPattern("h:mm a")));
            startTime.setFont(Font.font("Poppins Medium", 13));
            startTime.setStyle("-fx-text-fill: -roam-text-primary;");

            Label endTime = new Label(
                    event.getEndDateTime().toLocalTime().format(DateTimeFormatter.ofPattern("h:mm a")));
            endTime.setFont(Font.font("Poppins", 11));
            endTime.setStyle("-fx-text-fill: -roam-text-hint;");

            // Show duration
            String durationStr;
            if (durationMinutes < 60) {
                durationStr = durationMinutes + " min";
            } else if (durationMinutes % 60 == 0) {
                durationStr = (int) durationHours + " hr" + (durationHours > 1 ? "s" : "");
            } else {
                durationStr = String.format("%.1f hrs", durationHours);
            }
            Label durationLabel = new Label(durationStr);
            durationLabel.setFont(Font.font("Poppins", 10));
            durationLabel.setStyle("-fx-text-fill: -roam-text-hint;");

            timeBox.getChildren().addAll(startTime, endTime, durationLabel);
        }

        // Color bar
        CalendarSource source = calendarController.getCalendarSourceById(event.getCalendarSourceId());
        String color = source != null ? source.getColor() : "#4285f4";

        double barHeight = event.getIsAllDay() ? 50 : Math.max(40, Math.min(120, durationHours * 30));
        Region colorBar = new Region();
        colorBar.setMinWidth(4);
        colorBar.setMaxWidth(4);
        colorBar.setPrefHeight(barHeight);
        colorBar.setMinHeight(barHeight);
        colorBar.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 2;");

        // Event details
        VBox detailsBox = new VBox(4);
        HBox.setHgrow(detailsBox, javafx.scene.layout.Priority.ALWAYS);

        Label titleLabel = new Label(event.getTitle());
        titleLabel.setFont(Font.font("Poppins Medium", 14));
        titleLabel.setStyle("-fx-text-fill: -roam-text-primary;");

        detailsBox.getChildren().add(titleLabel);

        // Location
        if (event.getLocation() != null && !event.getLocation().isEmpty()) {
            HBox locationBox = new HBox(6);
            locationBox.setAlignment(Pos.CENTER_LEFT);
            FontIcon locIcon = new FontIcon(Feather.MAP_PIN);
            locIcon.setIconSize(12);
            locIcon.setIconColor(Color.web("#6b7280"));
            Label locationLabel = new Label(event.getLocation());
            locationLabel.setFont(Font.font("Poppins", 12));
            locationLabel.setStyle("-fx-text-fill: -roam-text-secondary;");
            locationBox.getChildren().addAll(locIcon, locationLabel);
            detailsBox.getChildren().add(locationBox);
        }

        // Calendar source
        if (source != null) {
            Label sourceLabel = new Label(source.getName());
            sourceLabel.setFont(Font.font("Poppins", 11));
            sourceLabel.setStyle("-fx-text-fill: -roam-text-hint;");
            detailsBox.getChildren().add(sourceLabel);
        }

        // Edit button
        Button editBtn = new Button();
        FontIcon editIcon = new FontIcon(Feather.EDIT_2);
        editIcon.setIconSize(16);
        editIcon.setIconColor(Color.web("#9E9E9E"));
        editBtn.setGraphic(editIcon);
        editBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        editBtn.setVisible(false);
        editBtn.setOnAction(e -> {
            e.consume();
            calendarController.editEvent(event);
            loadOperationEvents();
        });

        item.getChildren().addAll(timeBox, colorBar, detailsBox, editBtn);

        // Hover effects
        item.setOnMouseEntered(e -> {
            item.setStyle("-fx-background-color: -roam-gray-bg; -fx-cursor: hand;");
            editBtn.setVisible(true);
        });
        item.setOnMouseExited(e -> {
            item.setStyle("-fx-background-color: -roam-bg-primary; -fx-cursor: hand;");
            editBtn.setVisible(false);
        });

        item.setOnMouseClicked(e -> {
            calendarController.editEvent(event);
            loadOperationEvents();
        });

        eventsAgendaContainer.getChildren().add(item);
    }

    // ==================== TASKS AGENDA TAB ====================

    private BorderPane createTasksAgendaView() {
        BorderPane view = new BorderPane();
        view.setStyle("-fx-background-color: -roam-bg-primary;");

        // Toolbar with new task button
        HBox toolbar = createTasksAgendaToolbar();
        view.setTop(toolbar);

        // Agenda container
        tasksAgendaContainer = new VBox(0);
        tasksAgendaContainer.setStyle("-fx-background-color: -roam-bg-primary;");

        ScrollPane scrollPane = new ScrollPane(tasksAgendaContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        view.setCenter(scrollPane);

        // Load tasks for this operation
        loadOperationTasks();

        return view;
    }

    private HBox createTasksAgendaToolbar() {
        HBox toolbar = new HBox(15);
        toolbar.setPadding(new Insets(15, 20, 15, 20));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle("-fx-background-color: -roam-gray-bg;");

        // New Task button
        Button newTaskBtn = new Button("New Task");
        FontIcon plusIcon = new FontIcon(Feather.PLUS);
        plusIcon.setIconSize(16);
        newTaskBtn.setGraphic(plusIcon);
        newTaskBtn.getStyleClass().addAll("pill-button", "primary");
        newTaskBtn.setPrefHeight(40);
        newTaskBtn.setMinWidth(120);
        newTaskBtn.setOnAction(e -> {
            controller.createTask(TaskStatus.TODO);
            loadOperationTasks();
        });
        AnimationUtils.addPressEffect(newTaskBtn);

        toolbar.getChildren().add(newTaskBtn);
        return toolbar;
    }

    private void loadOperationTasks() {
        tasksAgendaContainer.getChildren().clear();

        List<Task> tasks = controller.loadTasks();

        // Separate tasks with and without due dates
        List<Task> scheduledTasks = tasks.stream()
                .filter(t -> t.getDueDate() != null)
                .sorted(Comparator.comparing(Task::getDueDate))
                .collect(Collectors.toList());

        List<Task> unscheduledTasks = tasks.stream()
                .filter(t -> t.getDueDate() == null)
                .collect(Collectors.toList());

        if (scheduledTasks.isEmpty() && unscheduledTasks.isEmpty()) {
            showTasksEmptyState();
            return;
        }

        // Render unscheduled tasks first as "Backlog"
        if (!unscheduledTasks.isEmpty()) {
            renderUnscheduledTasksSection(unscheduledTasks);
        }

        // Group scheduled tasks by date
        LocalDate currentGroupDate = null;

        for (Task task : scheduledTasks) {
            LocalDate taskDate = task.getDueDate().toLocalDate();

            if (currentGroupDate == null || !currentGroupDate.equals(taskDate)) {
                currentGroupDate = taskDate;
                renderTasksDateHeader(currentGroupDate);
            }

            renderTaskItem(task);
        }
    }

    private void renderUnscheduledTasksSection(List<Task> tasks) {
        HBox header = new HBox(15);
        header.setPadding(new Insets(20, 20, 10, 20));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle(
                "-fx-background-color: -roam-gray-bg; " +
                        "-fx-border-color: -roam-border; " +
                        "-fx-border-width: 0 0 1 0;");

        Label title = new Label("Unscheduled / Backlog");
        title.setFont(Font.font("Poppins Bold", 16));
        title.setStyle("-fx-text-fill: -roam-text-secondary;");

        header.getChildren().add(title);
        tasksAgendaContainer.getChildren().add(header);

        for (Task task : tasks) {
            renderTaskItem(task);
        }
    }

    private void renderTasksDateHeader(LocalDate date) {
        HBox dateHeader = new HBox(15);
        dateHeader.setPadding(new Insets(20, 20, 10, 20));
        dateHeader.setAlignment(Pos.CENTER_LEFT);
        dateHeader.setStyle(
                "-fx-background-color: -roam-gray-bg; " +
                        "-fx-border-color: -roam-border; " +
                        "-fx-border-width: 1 0 0 0;");

        // Day number
        Label dayNum = new Label(String.valueOf(date.getDayOfMonth()));
        dayNum.setFont(Font.font("Poppins Bold", 24));

        boolean isToday = date.equals(LocalDate.now());
        if (isToday) {
            dayNum.setStyle(
                    "-fx-text-fill: #FFFFFF; " +
                            "-fx-background-color: -roam-blue; " +
                            "-fx-background-radius: 50%; " +
                            "-fx-min-width: 45; " +
                            "-fx-min-height: 45; " +
                            "-fx-alignment: center;");
        } else {
            dayNum.setStyle("-fx-text-fill: -roam-text-primary;");
        }

        // Date info
        VBox dateInfo = new VBox(2);
        Label dayName = new Label(date.getDayOfWeek().toString().substring(0, 3));
        dayName.setFont(Font.font("Poppins Medium", 12));
        dayName.setStyle("-fx-text-fill: -roam-text-secondary;");

        Label monthYear = new Label(date.format(DateTimeFormatter.ofPattern("MMM yyyy")));
        monthYear.setFont(Font.font("Poppins", 12));
        monthYear.setStyle("-fx-text-fill: -roam-text-hint;");

        dateInfo.getChildren().addAll(dayName, monthYear);

        dateHeader.getChildren().addAll(dayNum, dateInfo);
        tasksAgendaContainer.getChildren().add(dateHeader);
    }

    private void renderTaskItem(Task task) {
        HBox item = new HBox(15);
        item.setPadding(new Insets(15, 20, 15, 20));
        item.setAlignment(Pos.CENTER_LEFT);
        item.setStyle("-fx-background-color: -roam-bg-primary; -fx-cursor: hand;");

        // Status Checkbox
        CheckBox statusCb = new CheckBox();
        statusCb.setSelected(task.getStatus() == TaskStatus.DONE);
        statusCb.setOnAction(e -> {
            TaskStatus newStatus = statusCb.isSelected() ? TaskStatus.DONE : TaskStatus.TODO;
            controller.updateTaskStatus(task, newStatus);
            loadOperationTasks();
        });

        // Priority Color Bar
        Region colorBar = new Region();
        colorBar.setMinWidth(4);
        colorBar.setMaxWidth(4);
        colorBar.setPrefHeight(40);
        colorBar.setStyle(
                "-fx-background-color: " + getPriorityColor(task.getPriority()) + "; -fx-background-radius: 2;");

        // Task Details
        VBox detailsBox = new VBox(4);
        HBox.setHgrow(detailsBox, javafx.scene.layout.Priority.ALWAYS);

        Label titleLabel = new Label(task.getTitle());
        titleLabel.setFont(Font.font("Poppins Medium", 14));
        titleLabel.setStyle("-fx-text-fill: -roam-text-primary;");
        if (task.getStatus() == TaskStatus.DONE) {
            titleLabel.setStyle("-fx-text-fill: -roam-text-hint; -fx-strikethrough: true;");
        }

        detailsBox.getChildren().add(titleLabel);

        // Status and Priority tags row
        HBox tagsBox = new HBox(10);
        tagsBox.setAlignment(Pos.CENTER_LEFT);

        Label statusBadge = createStatusBadge(task.getStatus());
        tagsBox.getChildren().add(statusBadge);

        Label priorityBadge = createPriorityBadge(task.getPriority());
        tagsBox.getChildren().add(priorityBadge);

        if (task.getAssignee() != null && !task.getAssignee().isEmpty()) {
            Label assigneeLabel = new Label("@" + task.getAssignee());
            assigneeLabel.setFont(Font.font("Poppins", 10));
            assigneeLabel.setStyle("-fx-text-fill: -roam-text-secondary;");
            tagsBox.getChildren().add(assigneeLabel);
        }

        if (!tagsBox.getChildren().isEmpty()) {
            detailsBox.getChildren().add(tagsBox);
        }

        // Edit Button
        Button editBtn = new Button();
        FontIcon editIcon = new FontIcon(Feather.EDIT_2);
        editIcon.setIconSize(16);
        editIcon.setIconColor(Color.web("#9E9E9E"));
        editBtn.setGraphic(editIcon);
        editBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        editBtn.setVisible(false);
        editBtn.setOnAction(e -> {
            e.consume();
            controller.editTask(task);
            loadOperationTasks();
        });

        item.getChildren().addAll(statusCb, colorBar, detailsBox, editBtn);

        // Hover effects
        item.setOnMouseEntered(e -> {
            item.setStyle("-fx-background-color: -roam-gray-bg; -fx-cursor: hand;");
            editBtn.setVisible(true);
        });
        item.setOnMouseExited(e -> {
            item.setStyle("-fx-background-color: -roam-bg-primary; -fx-cursor: hand;");
            editBtn.setVisible(false);
        });

        item.setOnMouseClicked(e -> {
            if (e.getTarget() instanceof CheckBox || e.getTarget() instanceof Button)
                return;
            controller.editTask(task);
            loadOperationTasks();
        });

        tasksAgendaContainer.getChildren().add(item);
    }

    private void showTasksEmptyState() {
        StackPane emptyWrapper = new StackPane();
        emptyWrapper.setAlignment(Pos.CENTER);
        emptyWrapper.prefWidthProperty().bind(tasksAgendaContainer.widthProperty());
        emptyWrapper.setMinHeight(400);

        VBox emptyState = new VBox(20);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPadding(new Insets(100));

        FontIcon icon = new FontIcon(Feather.CHECK_SQUARE);
        icon.setIconSize(72);
        icon.setIconColor(Color.web("#6b7280"));

        Label title = new Label("No tasks for this operation");
        title.setFont(Font.font("Poppins Bold", 20));
        title.setStyle("-fx-text-fill: -roam-text-secondary;");

        Label description = new Label("Click '+ New Task' to create your first task");
        description.setFont(Font.font("Poppins", 14));
        description.setStyle("-fx-text-fill: -roam-text-hint;");

        emptyState.getChildren().addAll(icon, title, description);
        emptyWrapper.getChildren().add(emptyState);
        tasksAgendaContainer.getChildren().add(emptyWrapper);
    }

    private String getPriorityColor(Priority priority) {
        if (priority == null)
            return "-roam-gray-bg";
        return switch (priority) {
            case HIGH -> "-roam-priority-high";
            case MEDIUM -> "-roam-priority-medium";
            case LOW -> "-roam-priority-low";
        };
    }

    private Label createStatusBadge(TaskStatus status) {
        Label badge = new Label();
        badge.setStyle("-fx-font-size: 10px; -fx-font-weight: 600; -fx-padding: 2 8; -fx-background-radius: 8;");

        if (status == null) {
            badge.setText("Unknown");
            badge.setStyle(
                    badge.getStyle() + "-fx-background-color: -roam-gray-bg; -fx-text-fill: -roam-text-secondary;");
            return badge;
        }

        switch (status) {
            case TODO -> {
                badge.setText("To Do");
                badge.setStyle(badge.getStyle() + "-fx-background-color: -roam-blue-light; -fx-text-fill: -roam-blue;");
            }
            case IN_PROGRESS -> {
                badge.setText("In Progress");
                badge.setStyle(
                        badge.getStyle() + "-fx-background-color: -roam-orange-bg; -fx-text-fill: -roam-orange;");
            }
            case DONE -> {
                badge.setText("Done");
                badge.setStyle(badge.getStyle() + "-fx-background-color: -roam-green-bg; -fx-text-fill: -roam-green;");
            }
        }

        return badge;
    }

    private Label createPriorityBadge(Priority priority) {
        Label badge = new Label();
        badge.setStyle("-fx-font-size: 10px; -fx-padding: 2 8; -fx-background-radius: 8;");

        if (priority == null) {
            badge.setText("No Priority");
            badge.setStyle(
                    badge.getStyle() + "-fx-background-color: -roam-gray-bg; -fx-text-fill: -roam-text-secondary;");
            return badge;
        }

        switch (priority) {
            case HIGH -> {
                badge.setText("● High");
                badge.setStyle(badge.getStyle() + "-fx-background-color: -roam-red-bg; -fx-text-fill: -roam-red;");
            }
            case MEDIUM -> {
                badge.setText("● Medium");
                badge.setStyle(
                        badge.getStyle() + "-fx-background-color: -roam-yellow-bg; -fx-text-fill: -roam-yellow;");
            }
            case LOW -> {
                badge.setText("● Low");
                badge.setStyle(
                        badge.getStyle() + "-fx-background-color: -roam-gray-bg; -fx-text-fill: -roam-text-secondary;");
            }
        }

        return badge;
    }

    // ==================== WIKI TAB (Card-based like WikiKeepView)
    // ====================

    private StackPane createWikiCardView() {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: -roam-bg-primary;");

        // Main content
        VBox mainContent = new VBox();
        mainContent.setStyle("-fx-background-color: -roam-bg-primary;");
        mainContent.setPadding(new Insets(24, 40, 24, 40));
        mainContent.setSpacing(24);
        mainContent.setAlignment(Pos.TOP_CENTER);

        // Search bar
        HBox searchBar = createWikiSearchBar();

        // Quick wiki input
        HBox quickWikiBar = createQuickWikiBar();

        // Wikis container with scroll
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scrollPane, javafx.scene.layout.Priority.ALWAYS);

        VBox wikisContainer = new VBox(24);
        wikisContainer.setAlignment(Pos.TOP_CENTER);
        wikisContainer.setPadding(new Insets(16, 0, 16, 0));
        VBox.setVgrow(wikisContainer, javafx.scene.layout.Priority.ALWAYS);

        // Pinned section
        pinnedSection = createWikiSection("PINNED");
        pinnedWikisPane = createWikisFlowPane();
        pinnedSection.getChildren().add(pinnedWikisPane);

        // Others section
        othersSection = createWikiSection("OTHERS");
        otherWikisPane = createWikisFlowPane();
        othersSection.getChildren().add(otherWikisPane);

        // Empty state
        wikiEmptyState = createWikiEmptyState();
        wikiEmptyState.setVisible(false);
        wikiEmptyState.setManaged(false);

        wikisContainer.getChildren().addAll(pinnedSection, othersSection, wikiEmptyState);
        scrollPane.setContent(wikisContainer);

        mainContent.getChildren().addAll(searchBar, quickWikiBar, scrollPane);
        VBox.setVgrow(scrollPane, javafx.scene.layout.Priority.ALWAYS);

        // Modal overlay (initially hidden)
        wikiModalOverlay = new StackPane();
        wikiModalOverlay.setStyle("-fx-background-color: transparent;");
        wikiModalOverlay.setVisible(false);
        wikiModalOverlay.setManaged(false);
        wikiModalOverlay.setOnMouseClicked(e -> {
            if (e.getTarget() == wikiModalOverlay) {
                closeWikiModal();
            }
        });

        root.getChildren().addAll(mainContent, wikiModalOverlay);

        // Load wikis for this operation
        loadOperationWikis();

        return root;
    }

    private HBox createWikiSearchBar() {
        HBox searchBar = new HBox();
        searchBar.setAlignment(Pos.CENTER);
        searchBar.setMaxWidth(600);

        wikiSearchField = new TextField();
        wikiSearchField.setPromptText("Search wikis in this operation");
        wikiSearchField.getStyleClass().add("wiki-search-field");
        wikiSearchField.setPrefHeight(48);
        HBox.setHgrow(wikiSearchField, javafx.scene.layout.Priority.ALWAYS);

        FontIcon searchIcon = new FontIcon(Feather.SEARCH);
        searchIcon.setIconSize(18);
        searchIcon.setStyle("-fx-icon-color: -roam-text-tertiary;");

        StackPane searchContainer = new StackPane();
        searchContainer.getStyleClass().add("wiki-search-container");
        searchContainer.getChildren().addAll(wikiSearchField, searchIcon);
        StackPane.setAlignment(searchIcon, Pos.CENTER_LEFT);
        StackPane.setMargin(searchIcon, new Insets(0, 0, 0, 16));
        wikiSearchField.setPadding(new Insets(0, 16, 0, 48));
        HBox.setHgrow(searchContainer, javafx.scene.layout.Priority.ALWAYS);

        wikiSearchField.textProperty().addListener((obs, oldVal, newVal) -> filterOperationWikis(newVal));

        searchBar.getChildren().add(searchContainer);
        return searchBar;
    }

    private HBox createQuickWikiBar() {
        HBox quickWikiBar = new HBox(12);
        quickWikiBar.setAlignment(Pos.CENTER);
        quickWikiBar.setMaxWidth(600);
        quickWikiBar.getStyleClass().add("wiki-quick-note-bar");
        quickWikiBar.setPadding(new Insets(12, 16, 12, 16));

        FontIcon plusIcon = new FontIcon(Feather.PLUS);
        plusIcon.setIconSize(18);
        plusIcon.setStyle("-fx-icon-color: -roam-text-secondary;");

        quickWikiField = new TextField();
        quickWikiField.setPromptText("Create a wiki...");
        quickWikiField.getStyleClass().add("wiki-quick-note-field");
        HBox.setHgrow(quickWikiField, javafx.scene.layout.Priority.ALWAYS);

        quickWikiField.setOnAction(e -> createQuickWiki());

        quickWikiBar.getChildren().addAll(plusIcon, quickWikiField);
        quickWikiBar.setOnMouseClicked(e -> quickWikiField.requestFocus());

        return quickWikiBar;
    }

    private VBox createWikiSection(String title) {
        VBox section = new VBox(12);
        section.setAlignment(Pos.TOP_LEFT);
        section.setMaxWidth(Double.MAX_VALUE);

        Label titleLabel = new Label(title);
        titleLabel.setStyle(
                "-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: -roam-text-tertiary; -fx-padding: 0 0 0 8;");

        section.getChildren().add(titleLabel);
        return section;
    }

    private FlowPane createWikisFlowPane() {
        FlowPane flowPane = new FlowPane();
        flowPane.setHgap(16);
        flowPane.setVgap(16);
        flowPane.setAlignment(Pos.TOP_LEFT);
        flowPane.setPrefWrapLength(900);
        return flowPane;
    }

    private VBox createWikiEmptyState() {
        VBox emptyState = new VBox(16);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPadding(new Insets(100, 60, 60, 60));
        emptyState.setMaxWidth(Double.MAX_VALUE);
        emptyState.setMinHeight(300);
        VBox.setVgrow(emptyState, javafx.scene.layout.Priority.ALWAYS);

        FontIcon icon = new FontIcon(BoxiconsSolid.NOTEPAD);
        icon.setIconSize(48);
        icon.setStyle("-fx-icon-color: -roam-text-tertiary;");

        Label label = new Label("No wikis yet for this operation. Start by creating a wiki above!");
        label.setStyle("-fx-font-size: 14px; -fx-text-fill: -roam-text-tertiary;");

        emptyState.getChildren().addAll(icon, label);
        return emptyState;
    }

    private void loadOperationWikis() {
        List<Wiki> operationWikis = wikiController.loadWikisForOperation(controller.getOperation());

        List<Wiki> pinned = operationWikis.stream()
                .filter(w -> Boolean.TRUE.equals(w.getIsFavorite()))
                .collect(Collectors.toList());

        List<Wiki> others = operationWikis.stream()
                .filter(w -> !Boolean.TRUE.equals(w.getIsFavorite()))
                .collect(Collectors.toList());

        displayWikis(pinned, others);
    }

    private void displayWikis(List<Wiki> pinned, List<Wiki> others) {
        pinnedWikisPane.getChildren().clear();
        otherWikisPane.getChildren().clear();

        // Show/hide pinned section based on content
        pinnedSection.setVisible(!pinned.isEmpty());
        pinnedSection.setManaged(!pinned.isEmpty());

        for (Wiki wiki : pinned) {
            WikiNoteCard card = createWikiCard(wiki);
            pinnedWikisPane.getChildren().add(card);
        }

        for (Wiki wiki : others) {
            WikiNoteCard card = createWikiCard(wiki);
            otherWikisPane.getChildren().add(card);
        }

        // Show empty state if no wikis
        if (pinned.isEmpty() && others.isEmpty()) {
            othersSection.setVisible(false);
            othersSection.setManaged(false);
            wikiEmptyState.setVisible(true);
            wikiEmptyState.setManaged(true);
        } else {
            wikiEmptyState.setVisible(false);
            wikiEmptyState.setManaged(false);
            othersSection.setVisible(true);
            othersSection.setManaged(true);
        }
    }

    private WikiNoteCard createWikiCard(Wiki wiki) {
        WikiNoteCard card = new WikiNoteCard(wiki);

        // Click to open modal
        card.setOnMouseClicked(e -> {
            if (!card.isActionClicked()) {
                openWikiModal(wiki);
            }
        });

        // Card action callbacks
        card.setOnPin(() -> {
            wikiController.toggleFavorite(wiki);
            loadOperationWikis();
        });

        card.setOnDelete(() -> {
            wikiController.deleteWiki(wiki);
            loadOperationWikis();
        });

        card.setOnColorChange(color -> {
            wiki.setBackgroundColor(color);
            wikiController.saveWiki(wiki);
            loadOperationWikis();
        });

        card.setOnRegionChange(region -> {
            wiki.setRegion(region);
            wikiController.saveWiki(wiki);
            loadOperationWikis();
        });

        return card;
    }

    private void createQuickWiki() {
        String text = quickWikiField.getText().trim();
        Wiki wiki;

        if (text.isEmpty()) {
            // Open modal for new wiki
            wiki = wikiController.createNewWiki();
        } else {
            // Create wiki with the text as title
            wiki = wikiController.createNewWiki();
            wiki.setTitle(text);
        }

        // Link to this operation
        wiki.setOperationId(controller.getOperation().getId());
        wikiController.saveWiki(wiki);
        quickWikiField.clear();

        if (text.isEmpty()) {
            openWikiModal(wiki);
        } else {
            loadOperationWikis();
        }
    }

    private void openWikiModal(Wiki wiki) {
        if (wiki == null) {
            wiki = wikiController.createNewWiki();
            wiki.setOperationId(controller.getOperation().getId());
            wikiController.saveWiki(wiki);
        }

        wikiModal = new WikiNoteModal(wiki, wikiController);
        wikiModal.setOnClose(() -> {
            closeWikiModal();
            loadOperationWikis();
        });

        wikiModalOverlay.getChildren().clear();
        wikiModalOverlay.getChildren().add(wikiModal);
        wikiModalOverlay.setVisible(true);
        wikiModalOverlay.setManaged(true);

        // Animate in
        AnimationUtils.fadeIn(wikiModalOverlay, Duration.millis(200));
    }

    private void closeWikiModal() {
        if (wikiModal != null) {
            wikiModal.saveAndClose();
        }
        wikiModalOverlay.setVisible(false);
        wikiModalOverlay.setManaged(false);
        wikiModalOverlay.getChildren().clear();
        wikiModal = null;
    }

    private void filterOperationWikis(String query) {
        if (query == null || query.trim().isEmpty()) {
            loadOperationWikis();
            return;
        }

        String lowerQuery = query.toLowerCase();
        List<Wiki> operationWikis = wikiController.loadWikisForOperation(controller.getOperation());

        List<Wiki> filtered = operationWikis.stream()
                .filter(w -> {
                    String title = w.getTitle() != null ? w.getTitle().toLowerCase() : "";
                    String content = w.getContent() != null ? w.getContent().toLowerCase() : "";
                    return title.contains(lowerQuery) || content.contains(lowerQuery);
                })
                .collect(Collectors.toList());

        List<Wiki> pinned = filtered.stream()
                .filter(w -> Boolean.TRUE.equals(w.getIsFavorite()))
                .collect(Collectors.toList());

        List<Wiki> others = filtered.stream()
                .filter(w -> !Boolean.TRUE.equals(w.getIsFavorite()))
                .collect(Collectors.toList());

        displayWikis(pinned, others);
    }

    // ==================== COMMON METHODS ====================

    private void editOperation(Operation operation) {
        OperationDialog dialog = new OperationDialog(operation, controller.getAllRegions());
        dialog.showAndWait().ifPresent(updatedOp -> {
            controller.updateOperation(updatedOp);
            infoCard.refresh(updatedOp);
        });
    }

    private void refreshData() {
        // Refresh events agenda
        loadOperationEvents();

        // Refresh tasks agenda
        loadOperationTasks();

        // Refresh wikis
        loadOperationWikis();
    }
}
