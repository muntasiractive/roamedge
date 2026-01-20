package com.roam.view.components;

import com.roam.controller.TasksController;
import com.roam.model.Operation;
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

import java.util.List;

/**
 * A global Kanban board component for visualizing tasks across all operations.
 * <p>
 * This component provides a three-column Kanban view (To Do, In Progress, Done)
 * that aggregates tasks from all operations in the system. Each column displays
 * task cards with their associated operation context, allowing users to manage
 * tasks at a global level with drag-and-drop functionality.
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public class GlobalTasksKanban extends HBox {

    private final TasksController controller;

    private final VBox todoColumn;
    private final VBox inProgressColumn;
    private final VBox doneColumn;

    private final VBox todoTasksContainer;
    private final VBox inProgressTasksContainer;
    private final VBox doneTasksContainer;

    private final Label todoCountLabel;
    private final Label inProgressCountLabel;
    private final Label doneCountLabel;

    public GlobalTasksKanban(TasksController controller) {
        this.controller = controller;

        setSpacing(20);
        setPadding(new Insets(20));
        setStyle("-fx-background-color: -roam-bg-primary;");
        setFillHeight(true);

        // Create task containers
        todoTasksContainer = new VBox(10);
        inProgressTasksContainer = new VBox(10);
        doneTasksContainer = new VBox(10);

        // Create count labels
        todoCountLabel = new Label("0");
        inProgressCountLabel = new Label("0");
        doneCountLabel = new Label("0");

        // Create columns
        todoColumn = createColumn("To Do", "-roam-blue-light", "-roam-blue", TaskStatus.TODO,
                todoTasksContainer, todoCountLabel);
        inProgressColumn = createColumn("In Progress", "-roam-orange-bg", "-roam-orange", TaskStatus.IN_PROGRESS,
                inProgressTasksContainer, inProgressCountLabel);
        doneColumn = createColumn("Done", "-roam-green-bg", "-roam-green", TaskStatus.DONE,
                doneTasksContainer, doneCountLabel);

        // Make columns equal width
        HBox.setHgrow(todoColumn, Priority.ALWAYS);
        HBox.setHgrow(inProgressColumn, Priority.ALWAYS);
        HBox.setHgrow(doneColumn, Priority.ALWAYS);

        getChildren().addAll(todoColumn, inProgressColumn, doneColumn);
    }

    private VBox createColumn(String title, String headerBg, String textColor,
            TaskStatus status, VBox tasksContainer, Label countLabel) {
        VBox column = new VBox();
        column.setMinWidth(300);
        column.setStyle(
                "-fx-background-color: transparent;");

        // Header with colored dot
        HBox header = new HBox(10);
        header.setPrefHeight(40);
        header.setPadding(new Insets(10, 0, 10, 0));
        header.setAlignment(Pos.CENTER_LEFT);

        // Colored dot
        Region dot = new Region();
        dot.setPrefSize(10, 10);
        dot.setMinSize(10, 10);
        dot.setMaxSize(10, 10);
        dot.setStyle("-fx-background-color: " + textColor + "; -fx-background-radius: 5;");

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Poppins SemiBold", 15));
        titleLabel.setStyle("-fx-text-fill: -roam-text-primary;");
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        // Count badge
        countLabel.setFont(Font.font("Poppins", 12));
        countLabel.setStyle(
                "-fx-background-color: -roam-gray-bg; " +
                        "-fx-text-fill: -roam-text-secondary; " +
                        "-fx-padding: 2 10 2 10; " +
                        "-fx-background-radius: 10;");

        header.getChildren().addAll(dot, titleLabel, countLabel);

        // Add task button
        Button addBtn = new Button("+ Add Task");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setPrefHeight(40);
        addBtn.setFont(Font.font("Poppins", 13));
        addBtn.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-border-color: -roam-border; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-style: dashed; " +
                        "-fx-border-radius: 6; " +
                        "-fx-background-radius: 6; " +
                        "-fx-text-fill: -roam-text-secondary; " +
                        "-fx-cursor: hand;");
        addBtn.setOnMouseEntered(e -> addBtn.setStyle(
                "-fx-background-color: -roam-gray-bg; " +
                        "-fx-border-color: -roam-border; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-style: dashed; " +
                        "-fx-border-radius: 6; " +
                        "-fx-background-radius: 6; " +
                        "-fx-text-fill: -roam-text-secondary; " +
                        "-fx-cursor: hand;"));
        addBtn.setOnMouseExited(e -> addBtn.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-border-color: -roam-border; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-style: dashed; " +
                        "-fx-border-radius: 6; " +
                        "-fx-background-radius: 6; " +
                        "-fx-text-fill: -roam-text-secondary; " +
                        "-fx-cursor: hand;"));
        addBtn.setOnAction(e -> createTaskWithStatus(status));
        VBox.setMargin(addBtn, new Insets(10));

        // Scrollable tasks container
        tasksContainer.setPadding(new Insets(10));
        tasksContainer.setSpacing(10);

        ScrollPane scrollPane = new ScrollPane(tasksContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // Enable drag and drop
        setupDragAndDrop(tasksContainer, status);

        column.getChildren().addAll(header, addBtn, scrollPane);
        return column;
    }

    private void createTaskWithStatus(TaskStatus status) {
        controller.createTask(status);
    }

    private void setupDragAndDrop(VBox container, TaskStatus targetStatus) {
        container.setOnDragOver(event -> {
            if (event.getGestureSource() != container && event.getDragboard().hasString()) {
                event.acceptTransferModes(javafx.scene.input.TransferMode.MOVE);
            }
            event.consume();
        });

        container.setOnDragDropped(event -> {
            javafx.scene.input.Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasString()) {
                try {
                    Long taskId = Long.parseLong(db.getString());
                    Task task = findTaskById(taskId);
                    if (task != null) {
                        controller.updateTaskStatus(taskId, targetStatus);
                        success = true;
                    }
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }

            event.setDropCompleted(success);
            event.consume();
        });
    }

    private Task findTaskById(Long taskId) {
        for (javafx.scene.Node node : todoTasksContainer.getChildren()) {
            if (node instanceof GlobalTaskCard card && card.getTask().getId().equals(taskId)) {
                return card.getTask();
            }
        }
        for (javafx.scene.Node node : inProgressTasksContainer.getChildren()) {
            if (node instanceof GlobalTaskCard card && card.getTask().getId().equals(taskId)) {
                return card.getTask();
            }
        }
        for (javafx.scene.Node node : doneTasksContainer.getChildren()) {
            if (node instanceof GlobalTaskCard card && card.getTask().getId().equals(taskId)) {
                return card.getTask();
            }
        }
        return null;
    }

    public void loadTasks(List<Task> tasks) {
        // Clear all containers
        todoTasksContainer.getChildren().clear();
        inProgressTasksContainer.getChildren().clear();
        doneTasksContainer.getChildren().clear();

        int todoCount = 0;
        int inProgressCount = 0;
        int doneCount = 0;

        // Sort tasks by status
        for (Task task : tasks) {
            GlobalTaskCard card = new GlobalTaskCard(task, controller);

            // Enable drag
            card.setOnDragDetected(event -> {
                javafx.scene.input.Dragboard db = card.startDragAndDrop(javafx.scene.input.TransferMode.MOVE);
                javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
                content.putString(task.getId().toString());
                db.setContent(content);
                card.setOpacity(0.6);
                event.consume();
            });

            card.setOnDragDone(event -> {
                card.setOpacity(1.0);
                event.consume();
            });

            switch (task.getStatus()) {
                case TODO -> {
                    todoTasksContainer.getChildren().add(card);
                    todoCount++;
                }
                case IN_PROGRESS -> {
                    inProgressTasksContainer.getChildren().add(card);
                    inProgressCount++;
                }
                case DONE -> {
                    doneTasksContainer.getChildren().add(card);
                    doneCount++;
                }
            }
        }

        // Update counts
        todoCountLabel.setText(String.valueOf(todoCount));
        inProgressCountLabel.setText(String.valueOf(inProgressCount));
        doneCountLabel.setText(String.valueOf(doneCount));
    }

    // Enhanced task card matching screenshot design
    private static class GlobalTaskCard extends VBox {
        private final Task task;

        public GlobalTaskCard(Task task, TasksController controller) {
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

            // Hover effect
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

            // Priority badge with flag
            Label priorityBadge = createPriorityBadge(task.getPriority());
            topRow.getChildren().add(priorityBadge);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            topRow.getChildren().add(spacer);

            // Due date on right
            if (task.getDueDate() != null) {
                FontIcon clockIcon = new FontIcon(Feather.CLOCK);
                clockIcon.setIconSize(12);
                clockIcon.setStyle("-fx-icon-color: -roam-text-hint;");

                Label dueDateLabel = new Label(formatDueDate(task.getDueDate()));
                dueDateLabel.setFont(Font.font("Poppins", 12));
                dueDateLabel.setStyle("-fx-text-fill: -roam-text-hint;");

                HBox dueDateBox = new HBox(4);
                dueDateBox.setAlignment(Pos.CENTER_RIGHT);
                dueDateBox.getChildren().addAll(clockIcon, dueDateLabel);
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

        private Label createPriorityBadge(com.roam.model.Priority priority) {
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

        private String formatDueDate(java.time.LocalDateTime dueDate) {
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MMM d");
            return formatter.format(dueDate);
        }
    }
}
