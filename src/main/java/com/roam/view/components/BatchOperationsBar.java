package com.roam.view.components;

import com.roam.controller.TasksController;
import com.roam.model.Priority;
import com.roam.model.Task;
import com.roam.model.TaskStatus;
import com.roam.util.DialogUtils;
import javafx.animation.TranslateTransition;
import javafx.collections.SetChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * A floating action bar component for performing batch operations on selected
 * tasks.
 * <p>
 * This component provides a visually prominent bar that appears when multiple
 * tasks
 * are selected, offering bulk actions such as status changes, priority updates,
 * due date modifications, and deletion. The bar features smooth
 * slide-in/slide-out
 * animations and displays the current selection count.
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public class BatchOperationsBar extends HBox {

    private final TasksController controller;
    private final Label selectionCountLabel;

    public BatchOperationsBar(TasksController controller) {
        this.controller = controller;

        setPrefHeight(60);
        setPadding(new Insets(15, 20, 15, 20));
        setSpacing(15);
        setAlignment(Pos.CENTER_LEFT);
        setStyle("-fx-background-color: -roam-blue;");

        // Drop shadow effect
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.2));
        shadow.setOffsetY(-2);
        shadow.setRadius(10);
        setEffect(shadow);

        // Initially hidden
        setVisible(false);
        setManaged(false);
        setTranslateY(60);

        // Selection counter
        selectionCountLabel = new Label("0 tasks selected");
        selectionCountLabel.setFont(Font.font("Poppins Medium", 14));
        selectionCountLabel.setStyle("-fx-text-fill: -roam-white;");

        // Separator
        Region separator1 = createSeparator();

        // Change Status
        HBox statusControl = createStatusControl();

        // Change Priority
        HBox priorityControl = createPriorityControl();

        // Assign
        HBox assignControl = createAssignControl();

        // Set Due Date
        HBox dueDateControl = createDueDateControl();

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        // Delete button
        Button deleteBtn = createDeleteButton();

        // Cancel button
        Button cancelBtn = createCancelButton();

        getChildren().addAll(
                selectionCountLabel,
                separator1,
                statusControl,
                priorityControl,
                assignControl,
                dueDateControl,
                spacer,
                deleteBtn,
                cancelBtn);

        // Listen to selection changes
        controller.getSelectedTasks().addListener((SetChangeListener<Task>) c -> {
            updateSelectionCount();
            updateVisibility();
        });
    }

    private Region createSeparator() {
        Region separator = new Region();
        separator.setPrefWidth(1);
        separator.setPrefHeight(30);
        separator.setStyle("-fx-background-color: rgba(255, 255, 255, 0.3);");
        return separator;
    }

    private HBox createStatusControl() {
        HBox container = new HBox(8);
        container.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label("Status:");
        label.setFont(Font.font("Poppins", 13));
        label.setStyle("-fx-text-fill: rgba(255, 255, 255, 0.9);");

        MenuButton statusBtn = new MenuButton("Change to...");
        statusBtn.setFont(Font.font("Poppins", 13));
        statusBtn.setPrefWidth(140);
        statusBtn.setPrefHeight(36);
        statusBtn.setStyle(
                "-fx-background-color: -roam-bg-primary; " +
                        "-fx-text-fill: -roam-blue; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand;");

        MenuItem todoItem = new MenuItem("To Do");
        FontIcon todoIcon = new FontIcon(Feather.CIRCLE);
        todoIcon.setIconSize(14);
        todoItem.setGraphic(todoIcon);
        todoItem.setOnAction(e -> batchUpdateStatus(TaskStatus.TODO));

        MenuItem inProgressItem = new MenuItem("In Progress");
        FontIcon progressIcon = new FontIcon(Feather.LOADER);
        progressIcon.setIconSize(14);
        inProgressItem.setGraphic(progressIcon);
        inProgressItem.setOnAction(e -> batchUpdateStatus(TaskStatus.IN_PROGRESS));

        MenuItem doneItem = new MenuItem("Done");
        FontIcon doneIcon = new FontIcon(Feather.CHECK_CIRCLE);
        doneIcon.setIconSize(14);
        doneItem.setGraphic(doneIcon);
        doneItem.setOnAction(e -> batchUpdateStatus(TaskStatus.DONE));

        statusBtn.getItems().addAll(todoItem, inProgressItem, doneItem);

        container.getChildren().addAll(label, statusBtn);
        return container;
    }

    private HBox createPriorityControl() {
        HBox container = new HBox(8);
        container.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label("Priority:");
        label.setFont(Font.font("Poppins", 13));
        label.setStyle("-fx-text-fill: rgba(255, 255, 255, 0.9);");

        MenuButton priorityBtn = new MenuButton("Change to...");
        priorityBtn.setFont(Font.font("Poppins", 13));
        priorityBtn.setPrefWidth(140);
        priorityBtn.setPrefHeight(36);
        priorityBtn.setStyle(
                "-fx-background-color: -roam-bg-primary; " +
                        "-fx-text-fill: -roam-blue; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand;");

        MenuItem highItem = new MenuItem("High");
        FontIcon highIcon = new FontIcon(Feather.ALERT_CIRCLE);
        highIcon.setIconSize(14);
        highIcon.setIconColor(Color.web("#ef4444"));
        highItem.setGraphic(highIcon);
        highItem.setOnAction(e -> batchUpdatePriority(Priority.HIGH));

        MenuItem mediumItem = new MenuItem("Medium");
        FontIcon medIcon = new FontIcon(Feather.ALERT_CIRCLE);
        medIcon.setIconSize(14);
        medIcon.setIconColor(Color.web("#f59e0b"));
        mediumItem.setGraphic(medIcon);
        mediumItem.setOnAction(e -> batchUpdatePriority(Priority.MEDIUM));

        MenuItem lowItem = new MenuItem("Low");
        FontIcon lowIcon = new FontIcon(Feather.ALERT_CIRCLE);
        lowIcon.setIconSize(14);
        lowIcon.setIconColor(Color.web("#9ca3af"));
        lowItem.setGraphic(lowIcon);
        lowItem.setOnAction(e -> batchUpdatePriority(Priority.LOW));

        priorityBtn.getItems().addAll(highItem, mediumItem, lowItem);

        container.getChildren().addAll(label, priorityBtn);
        return container;
    }

    private HBox createAssignControl() {
        HBox container = new HBox(8);
        container.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label("Assign:");
        label.setFont(Font.font("Poppins", 13));
        label.setStyle("-fx-text-fill: rgba(255, 255, 255, 0.9);");

        MenuButton assignBtn = new MenuButton("Assign to...");
        assignBtn.setFont(Font.font("Poppins", 13));
        assignBtn.setPrefWidth(150);
        assignBtn.setPrefHeight(36);
        assignBtn.setStyle(
                "-fx-background-color: -roam-bg-primary; " +
                        "-fx-text-fill: -roam-blue; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand;");

        MenuItem unassignItem = new MenuItem("(Unassign)");
        unassignItem.setOnAction(e -> batchAssign(null));

        assignBtn.getItems().add(unassignItem);
        assignBtn.getItems().add(new SeparatorMenuItem());

        // Add assignees dynamically
        for (String assignee : controller.getAllAssignees()) {
            MenuItem item = new MenuItem(assignee);
            item.setOnAction(e -> batchAssign(assignee));
            assignBtn.getItems().add(item);
        }

        assignBtn.getItems().add(new SeparatorMenuItem());

        MenuItem newAssigneeItem = new MenuItem("(New assignee...)");
        newAssigneeItem.setOnAction(e -> showNewAssigneeDialog());
        assignBtn.getItems().add(newAssigneeItem);

        container.getChildren().addAll(label, assignBtn);
        return container;
    }

    private HBox createDueDateControl() {
        HBox container = new HBox(8);
        container.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label("Due:");
        label.setFont(Font.font("Poppins", 13));
        label.setStyle("-fx-text-fill: rgba(255, 255, 255, 0.9);");

        Button dueDateBtn = new Button("Set date...");
        FontIcon calIcon = new FontIcon(Feather.CALENDAR);
        calIcon.setIconSize(14);
        dueDateBtn.setGraphic(calIcon);
        dueDateBtn.setFont(Font.font("Poppins", 13));
        dueDateBtn.setPrefWidth(120);
        dueDateBtn.setPrefHeight(36);
        dueDateBtn.setStyle(
                "-fx-background-color: -roam-bg-primary; " +
                        "-fx-text-fill: -roam-blue; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand;");
        dueDateBtn.setOnAction(e -> showDueDateDialog());

        container.getChildren().addAll(label, dueDateBtn);
        return container;
    }

    private Button createDeleteButton() {
        Button deleteBtn = new Button("Delete");
        FontIcon trashIcon = new FontIcon(Feather.TRASH_2);
        trashIcon.setIconSize(14);
        deleteBtn.setGraphic(trashIcon);
        deleteBtn.setFont(Font.font("Poppins Medium", 14));
        deleteBtn.setPrefWidth(110);
        deleteBtn.setPrefHeight(36);
        deleteBtn.setStyle(
                "-fx-background-color: -roam-red; " +
                        "-fx-text-fill: -roam-white; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand;");
        deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle(
                "-fx-background-color: -roam-red-hover; " +
                        "-fx-text-fill: -roam-white; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand;"));
        deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle(
                "-fx-background-color: -roam-red; " +
                        "-fx-text-fill: -roam-white; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand;"));
        deleteBtn.setOnAction(e -> batchDelete());

        return deleteBtn;
    }

    private Button createCancelButton() {
        Button cancelBtn = new Button("×");
        cancelBtn.setFont(Font.font("Poppins", 18));
        cancelBtn.setPrefWidth(36);
        cancelBtn.setPrefHeight(36);
        cancelBtn.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: -roam-white; " +
                        "-fx-border-color: rgba(255, 255, 255, 0.5); " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 6; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand;");
        cancelBtn.setOnMouseEntered(e -> cancelBtn.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.2); " +
                        "-fx-text-fill: -roam-white; " +
                        "-fx-border-color: rgba(255, 255, 255, 0.5); " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 6; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand;"));
        cancelBtn.setOnMouseExited(e -> cancelBtn.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: -roam-white; " +
                        "-fx-border-color: rgba(255, 255, 255, 0.5); " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 6; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand;"));
        cancelBtn.setOnAction(e -> controller.clearSelection());

        return cancelBtn;
    }

    private void updateSelectionCount() {
        int count = controller.getSelectedTasks().size();
        String text = count == 1 ? "1 task selected" : count + " tasks selected";
        selectionCountLabel.setText(text);
    }

    private void updateVisibility() {
        boolean hasSelection = !controller.getSelectedTasks().isEmpty();

        if (hasSelection && !isVisible()) {
            show();
        } else if (!hasSelection && isVisible()) {
            hide();
        }
    }

    private void show() {
        setVisible(true);
        setManaged(true);

        TranslateTransition transition = new TranslateTransition(Duration.millis(200), this);
        transition.setFromY(60);
        transition.setToY(0);
        transition.play();
    }

    private void hide() {
        TranslateTransition transition = new TranslateTransition(Duration.millis(200), this);
        transition.setFromY(0);
        transition.setToY(60);
        transition.setOnFinished(e -> {
            setVisible(false);
            setManaged(false);
        });
        transition.play();
    }

    private void batchUpdateStatus(TaskStatus newStatus) {
        int count = controller.getSelectedTasks().size();
        if (count > 10) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Batch Update");
            confirm.setHeaderText("Update " + count + " tasks?");
            confirm.setContentText("Set status to " + newStatus + " for all selected tasks?");

            if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                return;
            }
        }

        controller.batchUpdateStatus(newStatus);
    }

    private void batchUpdatePriority(Priority newPriority) {
        int count = controller.getSelectedTasks().size();
        if (count > 10) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Batch Update");
            confirm.setHeaderText("Update " + count + " tasks?");
            confirm.setContentText("Set priority to " + newPriority + " for all selected tasks?");

            if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                return;
            }
        }

        controller.batchUpdatePriority(newPriority);
    }

    private void batchAssign(String assignee) {
        int count = controller.getSelectedTasks().size();
        String action = assignee == null ? "Unassign" : "Assign to " + assignee;

        if (count > 10) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Batch Update");
            confirm.setHeaderText(action + " for " + count + " tasks?");

            if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                return;
            }
        }

        controller.batchAssign(assignee);
    }

    private void showNewAssigneeDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Assignee");
        dialog.setHeaderText("Enter Assignee Name");
        dialog.setContentText("Name:");

        dialog.showAndWait().ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                batchAssign(name.trim());
            }
        });
    }

    private void showDueDateDialog() {
        Dialog<LocalDateTime> dialog = new Dialog<>();
        dialog.setTitle("Set Due Date");
        dialog.setHeaderText("Set due date for " + controller.getSelectedTasks().size() + " tasks");

        DatePicker datePicker = new DatePicker(LocalDate.now());
        CheckBox clearCheckBox = new CheckBox("Clear due date");

        VBox content = new VBox(10, datePicker, clearCheckBox);
        content.setPadding(new Insets(20));
        dialog.getDialogPane().setContent(content);

        ButtonType setButtonType = new ButtonType("Set", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(setButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == setButtonType) {
                if (clearCheckBox.isSelected()) {
                    return null;
                } else {
                    return datePicker.getValue().atTime(LocalTime.of(23, 59));
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(dueDate -> controller.batchSetDueDate(dueDate));
    }

    private void batchDelete() {
        int count = controller.getSelectedTasks().size();

        boolean confirmed = DialogUtils.showDeleteConfirmation(
                count + " selected tasks", "Tasks", "This action cannot be undone.");

        if (confirmed) {
            controller.batchDelete();
        }
    }
}
