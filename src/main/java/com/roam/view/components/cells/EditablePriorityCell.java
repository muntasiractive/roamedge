package com.roam.view.components.cells;

import com.roam.controller.TasksController;
import com.roam.model.Priority;
import com.roam.model.Task;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * A custom editable table cell for displaying and editing task priority levels.
 * Renders priority values as styled badges with icons and provides inline
 * editing
 * via a ComboBox. Supports all priority levels defined in the Priority enum.
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public class EditablePriorityCell extends TableCell<Task, Priority> {

    private final TasksController controller;
    private ComboBox<Priority> comboBox;

    public EditablePriorityCell(TasksController controller) {
        this.controller = controller;
    }

    @Override
    public void startEdit() {
        if (!isEmpty()) {
            super.startEdit();
            createComboBox();
            setText(null);
            setGraphic(comboBox);
            comboBox.requestFocus();
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setText(null);
        setGraphic(createPriorityBadge(getItem()));
    }

    @Override
    public void updateItem(Priority priority, boolean empty) {
        super.updateItem(priority, empty);

        if (empty || priority == null) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (comboBox != null) {
                    comboBox.setValue(priority);
                }
                setText(null);
                setGraphic(comboBox);
            } else {
                setText(null);
                setGraphic(createPriorityBadge(priority));
            }
        }
    }

    private void createComboBox() {
        comboBox = new ComboBox<>();
        comboBox.getItems().addAll(Priority.values());
        comboBox.setValue(getItem());
        comboBox.setMaxWidth(Double.MAX_VALUE);

        // Custom cell factory for ComboBox items
        comboBox.setCellFactory(param -> new javafx.scene.control.ListCell<Priority>() {
            @Override
            protected void updateItem(Priority priority, boolean empty) {
                super.updateItem(priority, empty);
                if (empty || priority == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(null);
                    setGraphic(createPriorityBadge(priority));
                }
            }
        });

        // Custom button cell for selected value
        comboBox.setButtonCell(new javafx.scene.control.ListCell<Priority>() {
            @Override
            protected void updateItem(Priority priority, boolean empty) {
                super.updateItem(priority, empty);
                if (empty || priority == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(null);
                    setGraphic(createPriorityBadge(priority));
                }
            }
        });

        comboBox.setOnAction(event -> {
            Priority newPriority = comboBox.getValue();
            if (newPriority != null && getTableRow() != null && getTableRow().getItem() != null) {
                Task task = getTableRow().getItem();
                task.setPriority(newPriority);
                controller.updateTask(task);
                commitEdit(newPriority);
            }
        });

        comboBox.setOnKeyReleased(event -> {
            switch (event.getCode()) {
                case ESCAPE:
                    cancelEdit();
                    break;
                case ENTER:
                    commitEdit(comboBox.getValue());
                    break;
                default:
                    break;
            }
        });
    }

    private HBox createPriorityBadge(Priority priority) {
        HBox badge = new HBox(6);
        badge.setAlignment(javafx.geometry.Pos.CENTER);
        badge.setPadding(new Insets(4, 12, 4, 12));
        badge.setMaxWidth(100);

        FontIcon icon = new FontIcon(Feather.ALERT_CIRCLE);
        icon.setIconSize(12);

        Label label = new Label();
        label.setFont(Font.font("Poppins Medium", 12));

        switch (priority) {
            case HIGH:
                icon.setIconColor(Color.web("#ef4444"));
                label.setText("High");
                badge.setStyle("-fx-background-color: -roam-red-bg; -fx-background-radius: 12;");
                label.setStyle("-fx-text-fill: -roam-red;");
                break;
            case MEDIUM:
                icon.setIconColor(Color.web("#f59e0b"));
                label.setText("Medium");
                badge.setStyle("-fx-background-color: -roam-yellow-bg; -fx-background-radius: 12;");
                label.setStyle("-fx-text-fill: -roam-yellow;");
                break;
            case LOW:
                icon.setIconColor(Color.web("#9ca3af"));
                label.setText("Low");
                badge.setStyle("-fx-background-color: -roam-gray-bg; -fx-background-radius: 12;");
                label.setStyle("-fx-text-fill: -roam-text-secondary;");
                break;
        }

        badge.getChildren().addAll(icon, label);
        return badge;
    }
}
