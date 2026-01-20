package com.roam.view.components;

import com.roam.controller.TasksController;
import com.roam.model.*;
import com.roam.util.DialogUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Card-style view for displaying tasks in a visual card layout.
 * <p>
 * This component presents tasks as individual cards in a flowing grid layout,
 * similar to the Operations card view. Each card displays task details
 * including
 * title, description, status, priority, due date, and associated operation.
 * Cards support hover effects and provide quick access to task actions.
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public class TasksCardView extends StackPane {

    private final TasksController controller;
    private final FlowPane cardsContainer;
    private final ScrollPane scrollPane;
    private final VBox emptyState;

    public TasksCardView(TasksController controller) {
        this.controller = controller;

        // Create cards container
        cardsContainer = new FlowPane();
        cardsContainer.setHgap(24);
        cardsContainer.setVgap(24);
        cardsContainer.setPadding(new Insets(24, 32, 32, 32));
        cardsContainer.setStyle("-fx-background-color: transparent;");

        // Configure ScrollPane
        scrollPane = new ScrollPane(cardsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        // Create empty state
        emptyState = createEmptyState();
        emptyState.setVisible(false);
        emptyState.setMouseTransparent(true);

        // Add both to StackPane - empty state overlays the scroll pane
        getChildren().addAll(scrollPane, emptyState);
        StackPane.setAlignment(emptyState, Pos.CENTER);
    }

    private VBox createEmptyState() {
        VBox container = new VBox(20);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(60));
        container.setMaxWidth(500);
        container.getStyleClass().add("empty-state");

        FontIcon icon = new FontIcon(Feather.CHECK_SQUARE);
        icon.setIconSize(72);
        icon.getStyleClass().add("empty-state-icon");

        Label title = new Label("No Tasks Yet");
        title.getStyleClass().add("empty-state-title");

        Label description = new Label("Click '+ New Task' to create your first task");
        description.getStyleClass().add("empty-state-description");

        container.getChildren().addAll(icon, title, description);
        return container;
    }

    public void loadTasks(List<Task> tasks) {
        cardsContainer.getChildren().clear();

        if (tasks == null || tasks.isEmpty()) {
            // Show empty state, hide scrollPane
            scrollPane.setVisible(false);
            scrollPane.setManaged(false);
            emptyState.setVisible(true);
            emptyState.setManaged(true);
        } else {
            // Hide empty state and show cards
            emptyState.setVisible(false);
            emptyState.setManaged(false);
            scrollPane.setVisible(true);
            scrollPane.setManaged(true);
            for (Task task : tasks) {
                VBox card = createTaskCard(task);
                cardsContainer.getChildren().add(card);
            }
        }
    }

    private VBox createTaskCard(Task task) {
        VBox card = new VBox(12);
        card.setPrefWidth(340);
        card.setMinWidth(300);
        card.setMaxWidth(380);
        card.setPadding(new Insets(20));
        card.getStyleClass().add("operation-card");
        card.setCursor(javafx.scene.Cursor.HAND);

        // Status badge and menu row
        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label statusBadge = createStatusBadge(task.getStatus());

        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Button menuBtn = new Button();
        menuBtn.setGraphic(new FontIcon(Feather.MORE_HORIZONTAL));
        menuBtn.getStyleClass().add("icon-button");
        menuBtn.setOnAction(e -> {
            e.consume();
            showTaskMenu(task, menuBtn);
        });

        topRow.getChildren().addAll(statusBadge, spacer, menuBtn);

        // Task title
        Label titleLabel = new Label(task.getTitle());
        titleLabel.setFont(Font.font("Poppins SemiBold", 18));
        titleLabel.setStyle("-fx-text-fill: -roam-text-primary;");
        titleLabel.setWrapText(true);

        // Description
        String descText = task.getDescription() != null ? truncateText(task.getDescription(), 80) : "";
        Label descLabel = new Label(descText);
        descLabel.setFont(Font.font("Poppins", 13));
        descLabel.setStyle("-fx-text-fill: -roam-text-secondary;");
        descLabel.setWrapText(true);
        descLabel.setMinHeight(40);

        // Priority and Tags row
        HBox metaRow = new HBox(10);
        metaRow.setAlignment(Pos.CENTER_LEFT);
        metaRow.setPadding(new Insets(8, 0, 0, 0));

        Label priorityBadge = createPriorityBadge(task.getPriority());
        metaRow.getChildren().add(priorityBadge);

        // Add region tag if exists
        if (task.getRegion() != null && !task.getRegion().isEmpty()) {
            Label regionBadge = new Label(task.getRegion());
            regionBadge.setFont(Font.font("Poppins", 11));
            regionBadge.setStyle(
                    "-fx-background-color: transparent; " +
                            "-fx-text-fill: -roam-text-secondary; " +
                            "-fx-padding: 4 10 4 10; " +
                            "-fx-background-radius: 4; " +
                            "-fx-border-color: -roam-border; " +
                            "-fx-border-width: 1; " +
                            "-fx-border-radius: 4;");
            metaRow.getChildren().add(regionBadge);
        }

        // Due date row
        HBox dateRow = new HBox(6);
        dateRow.setAlignment(Pos.CENTER_LEFT);
        dateRow.setPadding(new Insets(8, 0, 0, 0));

        FontIcon calendarIcon = new FontIcon(Feather.CALENDAR);
        calendarIcon.setIconSize(14);
        calendarIcon.setStyle("-fx-icon-color: -roam-text-hint;");

        String dueDateText = formatDueDate(task.getDueDate());
        Label dateLabel = new Label(dueDateText);
        dateLabel.setFont(Font.font("Poppins", 12));

        // Color based on due date status
        if (task.getDueDate() != null && task.getDueDate().isBefore(LocalDateTime.now())
                && task.getStatus() != TaskStatus.DONE) {
            dateLabel.setStyle("-fx-text-fill: -roam-red;");
            calendarIcon.setStyle("-fx-icon-color: -roam-red;");
        } else {
            dateLabel.setStyle("-fx-text-fill: -roam-text-hint;");
        }

        dateRow.getChildren().addAll(calendarIcon, dateLabel);

        // Operation info if linked
        if (task.getOperationId() != null) {
            controller.getOperationById(task.getOperationId()).ifPresent(operation -> {
                HBox opRow = new HBox(6);
                opRow.setAlignment(Pos.CENTER_LEFT);
                opRow.setPadding(new Insets(4, 0, 0, 0));

                FontIcon folderIcon = new FontIcon(Feather.FOLDER);
                folderIcon.setIconSize(12);
                folderIcon.setStyle("-fx-icon-color: -roam-blue;");

                Label opLabel = new Label(operation.getName());
                opLabel.setFont(Font.font("Poppins", 11));
                opLabel.setStyle("-fx-text-fill: -roam-blue;");

                opRow.getChildren().addAll(folderIcon, opLabel);
                card.getChildren().add(opRow);
            });
        }

        card.getChildren().addAll(topRow, titleLabel, descLabel, metaRow, dateRow);

        // Click handler
        card.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1) {
                controller.editTask(task);
            }
        });

        // Hover effect
        card.setOnMouseEntered(e -> card.getStyleClass().add("operation-card-hover"));
        card.setOnMouseExited(e -> card.getStyleClass().remove("operation-card-hover"));

        return card;
    }

    private Label createStatusBadge(TaskStatus status) {
        Label badge = new Label();
        badge.setFont(Font.font("Poppins SemiBold", 11));
        badge.setPadding(new Insets(4, 12, 4, 12));

        if (status == null) {
            badge.setText("Unknown");
            badge.setStyle(
                    "-fx-background-color: -roam-gray-bg; -fx-background-radius: 12; -fx-text-fill: -roam-text-secondary;");
            return badge;
        }

        switch (status) {
            case TODO -> {
                badge.setText("To Do");
                badge.setStyle(
                        "-fx-background-color: -roam-blue-light; -fx-background-radius: 12; -fx-text-fill: -roam-blue;");
            }
            case IN_PROGRESS -> {
                badge.setText("In Progress");
                badge.setStyle(
                        "-fx-background-color: -roam-orange-bg; -fx-background-radius: 12; -fx-text-fill: -roam-orange;");
            }
            case DONE -> {
                badge.setText("Done");
                badge.setStyle(
                        "-fx-background-color: -roam-green-bg; -fx-background-radius: 12; -fx-text-fill: -roam-green;");
            }
        }

        return badge;
    }

    private Label createPriorityBadge(com.roam.model.Priority priority) {
        Label badge = new Label();
        badge.setFont(Font.font("Poppins", 11));
        badge.setPadding(new Insets(4, 10, 4, 10));

        if (priority == null) {
            badge.setText("No Priority");
            badge.setStyle(
                    "-fx-background-color: -roam-gray-bg; -fx-background-radius: 4; -fx-text-fill: -roam-text-secondary;");
            return badge;
        }

        switch (priority) {
            case HIGH -> {
                badge.setText("● High");
                badge.setStyle(
                        "-fx-background-color: -roam-red-bg; -fx-background-radius: 4; -fx-text-fill: -roam-red;");
            }
            case MEDIUM -> {
                badge.setText("● Medium");
                badge.setStyle(
                        "-fx-background-color: -roam-yellow-bg; -fx-background-radius: 4; -fx-text-fill: -roam-yellow;");
            }
            case LOW -> {
                badge.setText("● Low");
                badge.setStyle(
                        "-fx-background-color: -roam-gray-bg; -fx-background-radius: 4; -fx-text-fill: -roam-text-secondary;");
            }
        }

        return badge;
    }

    private String formatDueDate(LocalDateTime dueDate) {
        if (dueDate == null) {
            return "No due date";
        }
        return "Due " + dueDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
    }

    private String truncateText(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }

    private void showTaskMenu(Task task, Button anchor) {
        ContextMenu menu = new ContextMenu();

        MenuItem editItem = new MenuItem("Edit");
        editItem.setGraphic(new FontIcon(Feather.EDIT_2));
        editItem.setOnAction(e -> controller.editTask(task));

        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setGraphic(new FontIcon(Feather.TRASH_2));
        deleteItem.setStyle("-fx-text-fill: -roam-red;");
        deleteItem.setOnAction(e -> {
            boolean confirmed = DialogUtils.showDeleteConfirmation(
                    task.getTitle(), "Task", "This action cannot be undone.");
            if (confirmed) {
                controller.deleteTask(task.getId());
            }
        });

        menu.getItems().addAll(editItem, new SeparatorMenuItem(), deleteItem);
        menu.show(anchor, javafx.geometry.Side.BOTTOM, 0, 0);
    }
}
