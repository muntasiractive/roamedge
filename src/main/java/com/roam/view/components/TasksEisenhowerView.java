package com.roam.view.components;

import com.roam.controller.TasksController;
import com.roam.model.Operation;
import com.roam.model.Priority;
import com.roam.model.Task;
import com.roam.model.TaskStatus;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Eisenhower Matrix View for task prioritization and time management.
 * <p>
 * This component classifies tasks into 4 quadrants based on urgency and
 * importance:
 * </p>
 * <ul>
 * <li><strong>Quadrant 1 - Do First:</strong> Urgent &amp; Important (High
 * Priority + Due Soon)</li>
 * <li><strong>Quadrant 2 - Schedule:</strong> Not Urgent but Important
 * (High/Medium Priority + Due Later)</li>
 * <li><strong>Quadrant 3 - Delegate:</strong> Urgent but Not Important (Low
 * Priority + Due Soon)</li>
 * <li><strong>Quadrant 4 - Eliminate:</strong> Not Urgent &amp; Not Important
 * (Low Priority + Due Later)</li>
 * </ul>
 * <p>
 * Tasks are automatically categorized based on their priority level and due
 * date,
 * with a configurable urgency threshold (default: 3 days).
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public class TasksEisenhowerView extends GridPane {

    private final TasksController controller;

    // Quadrant containers
    private VBox urgentImportantContainer;
    private VBox notUrgentImportantContainer;
    private VBox urgentNotImportantContainer;
    private VBox notUrgentNotImportantContainer;

    // Count labels
    private Label urgentImportantCount;
    private Label notUrgentImportantCount;
    private Label urgentNotImportantCount;
    private Label notUrgentNotImportantCount;

    // Urgency threshold (tasks due within 3 days are considered urgent)
    private static final int URGENT_DAYS_THRESHOLD = 3;

    public TasksEisenhowerView(TasksController controller) {
        this.controller = controller;
        initialize();
    }

    private void initialize() {
        setHgap(20);
        setVgap(20);
        setPadding(new Insets(20));
        setStyle("-fx-background-color: transparent;");

        // Create quadrants
        VBox urgentImportant = createQuadrant(
                "Do First",
                "Urgent & Important",
                "danger",
                urgentImportantContainer = new VBox(10),
                urgentImportantCount = new Label("0"));

        VBox notUrgentImportant = createQuadrant(
                "Schedule",
                "Less Urgent, Important",
                "accent",
                notUrgentImportantContainer = new VBox(10),
                notUrgentImportantCount = new Label("0"));

        VBox urgentNotImportant = createQuadrant(
                "Delegate",
                "Urgent, Less Important",
                "warning",
                urgentNotImportantContainer = new VBox(10),
                urgentNotImportantCount = new Label("0"));

        VBox notUrgentNotImportant = createQuadrant(
                "Don't Do (Later)",
                "Not Urgent & Not Important",
                "neutral",
                notUrgentNotImportantContainer = new VBox(10),
                notUrgentNotImportantCount = new Label("0"));

        // Add to grid (2x2)
        add(urgentImportant, 0, 0);
        add(notUrgentImportant, 1, 0);
        add(urgentNotImportant, 0, 1);
        add(notUrgentNotImportant, 1, 1);

        // Make all quadrants equal size
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        getColumnConstraints().addAll(col1, col2);

        RowConstraints row1 = new RowConstraints();
        row1.setPercentHeight(50);
        RowConstraints row2 = new RowConstraints();
        row2.setPercentHeight(50);
        getRowConstraints().addAll(row1, row2);
    }

    private VBox createQuadrant(String title, String subtitle, String colorStyle,
            VBox tasksContainer, Label countLabel) {
        VBox quadrant = new VBox();
        quadrant.setStyle(
                "-fx-background-color: -roam-gray-bg; " +
                        "-fx-border-color: -roam-border; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 12; " +
                        "-fx-background-radius: 12;");

        // Header
        VBox header = new VBox(4);
        header.setPadding(new Insets(16));
        header.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Poppins SemiBold", 16));
        titleLabel.setStyle("-fx-text-fill: -roam-text-primary;");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setFont(Font.font("Poppins", 12));
        subtitleLabel.setStyle("-fx-text-fill: -roam-text-hint;");

        header.getChildren().addAll(titleLabel, subtitleLabel);

        // Tasks container with scroll
        tasksContainer.setPadding(new Insets(8, 16, 16, 16));
        tasksContainer.setSpacing(12);

        ScrollPane scrollPane = new ScrollPane(tasksContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scrollPane, javafx.scene.layout.Priority.ALWAYS);

        quadrant.getChildren().addAll(header, scrollPane);
        return quadrant;
    }

    public void loadTasks(List<Task> tasks) {
        // Clear all quadrants
        urgentImportantContainer.getChildren().clear();
        notUrgentImportantContainer.getChildren().clear();
        urgentNotImportantContainer.getChildren().clear();
        notUrgentNotImportantContainer.getChildren().clear();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime urgentThreshold = now.plusDays(URGENT_DAYS_THRESHOLD);

        // Filter out completed tasks
        List<Task> activeTasks = tasks.stream()
                .filter(t -> t.getStatus() != TaskStatus.DONE)
                .collect(Collectors.toList());

        int urgentImportantTotal = 0;
        int notUrgentImportantTotal = 0;
        int urgentNotImportantTotal = 0;
        int notUrgentNotImportantTotal = 0;

        for (Task task : activeTasks) {
            boolean isImportant = isImportant(task);
            boolean isUrgent = isUrgent(task, urgentThreshold);

            EisenhowerTaskCard card = new EisenhowerTaskCard(task, controller);

            if (isUrgent && isImportant) {
                urgentImportantContainer.getChildren().add(card);
                urgentImportantTotal++;
            } else if (!isUrgent && isImportant) {
                notUrgentImportantContainer.getChildren().add(card);
                notUrgentImportantTotal++;
            } else if (isUrgent && !isImportant) {
                urgentNotImportantContainer.getChildren().add(card);
                urgentNotImportantTotal++;
            } else {
                notUrgentNotImportantContainer.getChildren().add(card);
                notUrgentNotImportantTotal++;
            }
        }

        // Update counts
        urgentImportantCount.setText(String.valueOf(urgentImportantTotal));
        notUrgentImportantCount.setText(String.valueOf(notUrgentImportantTotal));
        urgentNotImportantCount.setText(String.valueOf(urgentNotImportantTotal));
        notUrgentNotImportantCount.setText(String.valueOf(notUrgentNotImportantTotal));

        // Show empty state if needed
        showEmptyStateIfNeeded(urgentImportantContainer, urgentImportantTotal, "No urgent important tasks");
        showEmptyStateIfNeeded(notUrgentImportantContainer, notUrgentImportantTotal,
                "No important tasks to schedule");
        showEmptyStateIfNeeded(urgentNotImportantContainer, urgentNotImportantTotal, "No tasks to delegate");
        showEmptyStateIfNeeded(notUrgentNotImportantContainer, notUrgentNotImportantTotal, "No tasks to eliminate");
    }

    private boolean isImportant(Task task) {
        // Important = High or Medium priority
        return task.getPriority() == Priority.HIGH || task.getPriority() == Priority.MEDIUM;
    }

    private boolean isUrgent(Task task, LocalDateTime urgentThreshold) {
        // Urgent = due date is within threshold OR overdue
        // No due date = Not Urgent (Schedule or Eliminate)
        if (task.getDueDate() == null) {
            return false;
        }
        return task.getDueDate().isBefore(urgentThreshold);
    }

    private void showEmptyStateIfNeeded(VBox container, int count, String message) {
        if (count == 0) {
            Label emptyLabel = new Label(message);
            emptyLabel.setFont(Font.font("Poppins", 12));
            emptyLabel.setStyle("-fx-text-fill: -roam-text-hint; -fx-font-style: italic;");
            emptyLabel.setWrapText(true);
            emptyLabel.setMaxWidth(Double.MAX_VALUE);
            emptyLabel.setAlignment(Pos.CENTER);
            container.getChildren().add(emptyLabel);
        }
    }

    // Task card for Eisenhower Matrix - matches screenshot design
    private static class EisenhowerTaskCard extends VBox {
        private final Task task;

        public EisenhowerTaskCard(Task task, TasksController controller) {
            this.task = task;

            setSpacing(8);
            setPadding(new Insets(14));
            setStyle(
                    "-fx-background-color: -roam-bg-primary; " +
                            "-fx-border-color: -roam-border; " +
                            "-fx-border-width: 1; " +
                            "-fx-border-radius: 10; " +
                            "-fx-background-radius: 10; " +
                            "-fx-cursor: hand;");

            setOnMouseEntered(e -> setStyle(
                    "-fx-background-color: -roam-bg-primary; " +
                            "-fx-border-color: -roam-border; " +
                            "-fx-border-width: 1; " +
                            "-fx-border-radius: 10; " +
                            "-fx-background-radius: 10; " +
                            "-fx-cursor: hand; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);"));

            setOnMouseExited(e -> setStyle(
                    "-fx-background-color: -roam-bg-primary; " +
                            "-fx-border-color: -roam-border; " +
                            "-fx-border-width: 1; " +
                            "-fx-border-radius: 10; " +
                            "-fx-background-radius: 10; " +
                            "-fx-cursor: hand;"));

            setOnMouseClicked(e -> controller.editTask(task));

            // Top row: Priority badge + spacer + Due date
            HBox topRow = new HBox(8);
            topRow.setAlignment(Pos.CENTER_LEFT);

            // Priority badge with flag icon
            Label priorityBadge = createPriorityBadge(task.getPriority());
            topRow.getChildren().add(priorityBadge);

            Region spacer = new Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            topRow.getChildren().add(spacer);

            // Due date on right
            if (task.getDueDate() != null) {
                FontIcon calendarIcon = new FontIcon(Feather.CLOCK);
                calendarIcon.setIconSize(12);
                calendarIcon.setStyle("-fx-icon-color: -roam-text-hint;");

                Label dueDateLabel = new Label(formatDueDate(task.getDueDate()));
                dueDateLabel.setFont(Font.font("Poppins", 12));
                dueDateLabel.setStyle("-fx-text-fill: -roam-text-hint;");

                HBox dueDateBox = new HBox(4);
                dueDateBox.setAlignment(Pos.CENTER_RIGHT);
                dueDateBox.getChildren().addAll(calendarIcon, dueDateLabel);
                topRow.getChildren().add(dueDateBox);
            }

            // Title
            Label titleLabel = new Label(task.getTitle());
            titleLabel.setFont(Font.font("Poppins SemiBold", 14));
            titleLabel.setStyle("-fx-text-fill: -roam-text-primary;");
            titleLabel.setWrapText(true);
            titleLabel.setMaxWidth(Double.MAX_VALUE);

            getChildren().addAll(topRow, titleLabel);

            // Description (if exists)
            if (task.getDescription() != null && !task.getDescription().isEmpty()) {
                String desc = task.getDescription();
                if (desc.length() > 80) {
                    desc = desc.substring(0, 80) + "...";
                }
                Label descLabel = new Label(desc);
                descLabel.setFont(Font.font("Poppins", 12));
                descLabel.setStyle("-fx-text-fill: -roam-text-secondary;");
                descLabel.setWrapText(true);
                descLabel.setMaxWidth(Double.MAX_VALUE);
                getChildren().add(descLabel);
            }

            // Tags row (region badges)
            HBox tagsRow = new HBox(6);
            tagsRow.setAlignment(Pos.CENTER_LEFT);

            if (task.getRegion() != null && !task.getRegion().isEmpty()) {
                String[] regions = task.getRegion().split(",");
                for (String region : regions) {
                    String trimmedRegion = region.trim().toUpperCase();
                    if (!trimmedRegion.isEmpty()) {
                        Label tagBadge = new Label(trimmedRegion);
                        tagBadge.setFont(Font.font("Poppins", 10));
                        tagBadge.setStyle(
                                "-fx-background-color: -roam-gray-bg; " +
                                        "-fx-text-fill: -roam-text-secondary; " +
                                        "-fx-padding: 3 8 3 8; " +
                                        "-fx-background-radius: 4; " +
                                        "-fx-border-color: -roam-border; " +
                                        "-fx-border-width: 1; " +
                                        "-fx-border-radius: 4;");
                        tagsRow.getChildren().add(tagBadge);
                    }
                }
            }

            if (!tagsRow.getChildren().isEmpty()) {
                getChildren().add(tagsRow);
            }
        }

        public Task getTask() {
            return task;
        }

        private Label createPriorityBadge(Priority priority) {
            HBox badgeBox = new HBox(4);
            badgeBox.setAlignment(Pos.CENTER_LEFT);

            FontIcon flagIcon = new FontIcon(Feather.FLAG);
            flagIcon.setIconSize(12);

            Label badge = new Label();
            badge.setFont(Font.font("Poppins", 11));

            String bgColor, textColor;
            switch (priority) {
                case HIGH -> {
                    badge.setText("\u2691 High");
                    bgColor = "-roam-red-bg";
                    textColor = "-roam-red";
                }
                case MEDIUM -> {
                    badge.setText("\u2691 Medium");
                    bgColor = "-roam-yellow-bg";
                    textColor = "-roam-yellow";
                }
                default -> {
                    badge.setText("\u2691 Low");
                    bgColor = "-roam-green-bg";
                    textColor = "-roam-green";
                }
            }

            badge.setStyle(
                    "-fx-background-color: " + bgColor + "; " +
                            "-fx-text-fill: " + textColor + "; " +
                            "-fx-padding: 3 8 3 8; " +
                            "-fx-background-radius: 4; " +
                            "-fx-border-color: " + textColor + "; " +
                            "-fx-border-width: 1; " +
                            "-fx-border-radius: 4;");

            return badge;
        }

        private String formatDueDate(LocalDateTime dueDate) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d");
            return formatter.format(dueDate);
        }
    }
}
