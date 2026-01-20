package com.roam.view.components;

import com.roam.controller.TasksController;
import com.roam.model.Operation;
import com.roam.model.Priority;
import com.roam.model.Task;
import com.roam.model.TaskStatus;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Timeline/Agenda view for displaying tasks organized by date.
 * <p>
 * This component presents tasks in a chronological timeline format,
 * grouped by their due dates. It separates scheduled tasks from
 * unscheduled ones and provides an intuitive agenda-style interface
 * for viewing upcoming deadlines and task schedules.
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public class TasksTimelineView extends BorderPane {

    private final TasksController controller;
    private VBox agendaContainer;
    private ScrollPane scrollPane;

    public TasksTimelineView(TasksController controller) {
        this.controller = controller;
        initialize();
    }

    private void initialize() {
        setStyle("-fx-background-color: -roam-bg-primary;");

        agendaContainer = new VBox(0);
        agendaContainer.setStyle("-fx-background-color: -roam-bg-primary;");

        scrollPane = new ScrollPane(agendaContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        setCenter(scrollPane);
    }

    public void loadTasks(List<Task> tasks) {
        agendaContainer.getChildren().clear();

        // Separate tasks with and without due dates
        List<Task> scheduledTasks = tasks.stream()
                .filter(t -> t.getDueDate() != null)
                .sorted(Comparator.comparing(Task::getDueDate))
                .collect(Collectors.toList());

        List<Task> unscheduledTasks = tasks.stream()
                .filter(t -> t.getDueDate() == null)
                .collect(Collectors.toList());

        if (scheduledTasks.isEmpty() && unscheduledTasks.isEmpty()) {
            showEmptyState();
            return;
        }

        // Render unscheduled tasks first (or last, depending on preference. Let's do
        // first as "Backlog")
        if (!unscheduledTasks.isEmpty()) {
            renderUnscheduledSection(unscheduledTasks);
        }

        // Group scheduled tasks by date
        LocalDate currentGroupDate = null;

        for (Task task : scheduledTasks) {
            LocalDate taskDate = task.getDueDate().toLocalDate();

            if (currentGroupDate == null || !currentGroupDate.equals(taskDate)) {
                currentGroupDate = taskDate;
                renderDateHeader(currentGroupDate);
            }

            renderTaskItem(task);
        }
    }

    private void renderUnscheduledSection(List<Task> tasks) {
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
        agendaContainer.getChildren().add(header);

        for (Task task : tasks) {
            renderTaskItem(task);
        }
    }

    private void renderDateHeader(LocalDate date) {
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
                    "-fx-text-fill: -roam-white; " +
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

        Label monthYear = new Label(
                date.format(DateTimeFormatter.ofPattern("MMM yyyy")));
        monthYear.setFont(Font.font("Poppins", 12));
        monthYear.setStyle("-fx-text-fill: -roam-text-hint;");

        dateInfo.getChildren().addAll(dayName, monthYear);

        dateHeader.getChildren().addAll(dayNum, dateInfo);
        agendaContainer.getChildren().add(dateHeader);
    }

    private void renderTaskItem(Task task) {
        HBox item = new HBox(15);
        item.setPadding(new Insets(15, 20, 15, 20));
        item.setAlignment(Pos.CENTER_LEFT);
        item.setStyle("-fx-background-color: -roam-bg-primary; -fx-cursor: hand;");

        // Status Indicator / Checkbox
        CheckBox statusCb = new CheckBox();
        statusCb.setSelected(task.getStatus() == TaskStatus.DONE);
        statusCb.setOnAction(e -> {
            TaskStatus newStatus = statusCb.isSelected() ? TaskStatus.DONE : TaskStatus.TODO;
            controller.updateTaskStatus(task, newStatus);
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

        // Operation / Tags
        HBox tagsBox = new HBox(10);
        Optional<Operation> operation = controller.getOperationById(task.getOperationId());
        if (operation.isPresent()) {
            Label opLabel = new Label(operation.get().getName());
            opLabel.setFont(Font.font("Poppins", 10));
            opLabel.setStyle(
                    "-fx-background-color: -roam-blue-light; " +
                            "-fx-text-fill: -roam-blue; " +
                            "-fx-padding: 2 6 2 6; " +
                            "-fx-background-radius: 8;");
            tagsBox.getChildren().add(opLabel);
        }

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
        });

        agendaContainer.getChildren().add(item);
    }

    private void showEmptyState() {
        // Use a StackPane to center the empty state in the available space
        StackPane emptyWrapper = new StackPane();
        emptyWrapper.setAlignment(Pos.CENTER);
        emptyWrapper.prefWidthProperty().bind(agendaContainer.widthProperty());
        emptyWrapper.setMinHeight(400);

        VBox emptyState = new VBox(20);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPadding(new Insets(100));

        FontIcon icon = new FontIcon(Feather.CHECK_CIRCLE);
        icon.setIconSize(72);
        icon.setIconColor(Color.web("#6b7280"));

        Label title = new Label("No tasks found");
        title.setFont(Font.font("Poppins Bold", 20));
        title.setStyle("-fx-text-fill: -roam-text-secondary;");

        Button createBtn = new Button("+ Create Task");
        createBtn.setPrefWidth(150);
        createBtn.setPrefHeight(40);
        createBtn.getStyleClass().add("pill-button");
        createBtn.setOnAction(e -> controller.createTask());

        emptyState.getChildren().addAll(icon, title, createBtn);
        emptyWrapper.getChildren().add(emptyState);
        agendaContainer.getChildren().add(emptyWrapper);
    }

    private String getPriorityColor(Priority priority) {
        return switch (priority) {
            case HIGH -> "-roam-priority-high";
            case MEDIUM -> "-roam-priority-medium";
            case LOW -> "-roam-priority-low";
        };
    }
}
