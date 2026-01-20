package com.roam.view.components;

import com.roam.model.Task;
import com.roam.model.TaskStatus;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A Kanban board component for visualizing and managing tasks across different
 * status columns.
 * <p>
 * This component displays tasks in three columns: To Do, In Progress, and Done.
 * Users can drag and drop tasks between columns to change their status,
 * and each column shows a count of the tasks it contains.
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public class KanbanBoard extends HBox {

    private final VBox todoColumn;
    private final VBox inProgressColumn;
    private final VBox doneColumn;

    private final VBox todoTasksContainer;
    private final VBox inProgressTasksContainer;
    private final VBox doneTasksContainer;

    private final Label todoCountLabel;
    private final Label inProgressCountLabel;
    private final Label doneCountLabel;

    private Consumer<TaskStatus> onAddTask;
    private Consumer<Task> onEditTask;
    private BiConsumer<Task, TaskStatus> onTaskStatusChanged;

    public KanbanBoard() {
        setSpacing(20);
        setPadding(new Insets(20));
        setStyle("-fx-background-color: -roam-gray-bg;");
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
        todoColumn = createColumn("To Do", "-roam-blue-tag-bg", "-roam-blue-tag", TaskStatus.TODO, todoTasksContainer,
                todoCountLabel);
        inProgressColumn = createColumn("In Progress", "-roam-orange-bg", "-roam-orange", TaskStatus.IN_PROGRESS,
                inProgressTasksContainer, inProgressCountLabel);
        doneColumn = createColumn("Done", "-roam-green-bg", "-roam-green", TaskStatus.DONE, doneTasksContainer,
                doneCountLabel);

        // Make columns equal width
        HBox.setHgrow(todoColumn, Priority.ALWAYS);
        HBox.setHgrow(inProgressColumn, Priority.ALWAYS);
        HBox.setHgrow(doneColumn, Priority.ALWAYS);

        getChildren().addAll(todoColumn, inProgressColumn, doneColumn);
    }

    private VBox createColumn(String title, String headerBg, String textColor, TaskStatus status,
            VBox tasksContainer, Label countLabel) {
        VBox column = new VBox();
        column.setMinWidth(280);
        column.setStyle(
                "-fx-background-color: -roam-bg-primary; " +
                        "-fx-border-color: -roam-border; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8;");

        // Header
        HBox header = new HBox(10);
        header.setPrefHeight(50);
        header.setPadding(new Insets(15));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: " + headerBg + "; " +
                "-fx-background-radius: 8 8 0 0;");

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Poppins Bold", 16));
        titleLabel.setStyle("-fx-text-fill: " + textColor + ";");
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        // Count badge
        countLabel.setFont(Font.font("Poppins", 12));
        countLabel.setStyle(
                "-fx-background-color: " + headerBg + "; " +
                        "-fx-text-fill: " + textColor + "; " +
                        "-fx-border-color: " + textColor + "; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 12; " +
                        "-fx-background-radius: 12; " +
                        "-fx-padding: 2 8 2 8;");

        header.getChildren().addAll(titleLabel, countLabel);

        // Add task button
        Button addBtn = new Button("+ Add Task");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setPrefHeight(40);
        addBtn.setFont(Font.font("Poppins", 13));
        addBtn.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-border-color: -roam-text-hint; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-style: dashed; " +
                        "-fx-border-radius: 6; " +
                        "-fx-background-radius: 6; " +
                        "-fx-text-fill: -roam-text-secondary; " +
                        "-fx-cursor: hand;");
        addBtn.setOnMouseEntered(e -> addBtn.setStyle(
                "-fx-background-color: -roam-gray-bg; " +
                        "-fx-border-color: -roam-text-hint; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-style: dashed; " +
                        "-fx-border-radius: 6; " +
                        "-fx-background-radius: 6; " +
                        "-fx-text-fill: -roam-text-secondary; " +
                        "-fx-cursor: hand;"));
        addBtn.setOnMouseExited(e -> addBtn.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-border-color: -roam-text-hint; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-style: dashed; " +
                        "-fx-border-radius: 6; " +
                        "-fx-background-radius: 6; " +
                        "-fx-text-fill: -roam-text-secondary; " +
                        "-fx-cursor: hand;"));
        addBtn.setOnAction(e -> {
            if (onAddTask != null) {
                onAddTask.accept(status);
            }
        });
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
                    // Find task and update status
                    Task task = findTaskById(taskId);
                    if (task != null && onTaskStatusChanged != null) {
                        onTaskStatusChanged.accept(task, targetStatus);
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
        // This is a helper method - will be populated when loading tasks
        for (javafx.scene.Node node : todoTasksContainer.getChildren()) {
            if (node instanceof TaskCard card && card.getTask().getId().equals(taskId)) {
                return card.getTask();
            }
        }
        for (javafx.scene.Node node : inProgressTasksContainer.getChildren()) {
            if (node instanceof TaskCard card && card.getTask().getId().equals(taskId)) {
                return card.getTask();
            }
        }
        for (javafx.scene.Node node : doneTasksContainer.getChildren()) {
            if (node instanceof TaskCard card && card.getTask().getId().equals(taskId)) {
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
            TaskCard card = new TaskCard(task, onEditTask, t -> {
                if (onTaskStatusChanged != null) {
                    onTaskStatusChanged.accept(t, TaskStatus.DONE);
                }
            });

            // Track mouse press position to distinguish click from drag
            final double[] startX = new double[1];
            final double[] startY = new double[1];
            final boolean[] isDragging = new boolean[1];

            card.setOnMousePressed(event -> {
                startX[0] = event.getSceneX();
                startY[0] = event.getSceneY();
                isDragging[0] = false;
            });

            // Only start drag if mouse moved significantly (> 5 pixels)
            card.setOnMouseDragged(event -> {
                double deltaX = Math.abs(event.getSceneX() - startX[0]);
                double deltaY = Math.abs(event.getSceneY() - startY[0]);

                if (deltaX > 5 || deltaY > 5) {
                    isDragging[0] = true;
                    // This is a drag, not a click
                    javafx.scene.input.Dragboard db = card
                            .startDragAndDrop(javafx.scene.input.TransferMode.MOVE);
                    javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
                    content.putString(task.getId().toString());
                    db.setContent(content);
                    card.setOpacity(0.6);
                    event.consume();
                }
            });

            card.setOnMouseReleased(event -> {
                // If we didn't drag, treat it as a click to edit
                if (!isDragging[0] && onEditTask != null) {
                    onEditTask.accept(task);
                }
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

    public void setOnAddTask(Consumer<TaskStatus> handler) {
        this.onAddTask = handler;
    }

    public void setOnEditTask(Consumer<Task> handler) {
        this.onEditTask = handler;
    }

    public void setOnTaskStatusChanged(BiConsumer<Task, TaskStatus> handler) {
        this.onTaskStatusChanged = handler;
    }
}
