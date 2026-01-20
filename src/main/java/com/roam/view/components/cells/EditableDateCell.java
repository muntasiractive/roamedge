package com.roam.view.components.cells;

import com.roam.controller.TasksController;
import com.roam.model.Task;
import javafx.geometry.Pos;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * A custom editable table cell for displaying and editing date values.
 * Provides inline date editing using a DatePicker control for Task due dates.
 * Supports keyboard navigation and automatic persistence of date changes.
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public class EditableDateCell extends TableCell<Task, LocalDateTime> {

    private final TasksController controller;
    private DatePicker datePicker;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    public EditableDateCell(TasksController controller) {
        this.controller = controller;
    }

    @Override
    public void startEdit() {
        if (!isEmpty()) {
            super.startEdit();
            createDatePicker();
            setText(null);
            setGraphic(datePicker);
            datePicker.show();
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setText(null);
        setGraphic(createDateDisplay(getItem()));
    }

    @Override
    public void updateItem(LocalDateTime date, boolean empty) {
        super.updateItem(date, empty);

        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (datePicker != null && date != null) {
                    datePicker.setValue(date.toLocalDate());
                }
                setText(null);
                setGraphic(datePicker);
            } else {
                setText(null);
                setGraphic(createDateDisplay(date));
            }
        }
    }

    private void createDatePicker() {
        datePicker = new DatePicker();
        if (getItem() != null) {
            datePicker.setValue(getItem().toLocalDate());
        } else {
            datePicker.setValue(LocalDate.now());
        }

        datePicker.setOnAction(event -> {
            LocalDate selectedDate = datePicker.getValue();
            if (selectedDate != null && getTableRow() != null && getTableRow().getItem() != null) {
                Task task = getTableRow().getItem();
                LocalDateTime newDueDate = selectedDate.atTime(LocalTime.of(23, 59));
                task.setDueDate(newDueDate);
                controller.updateTask(task);
                commitEdit(newDueDate);
            }
        });

        datePicker.setOnKeyReleased(event -> {
            switch (event.getCode()) {
                case ESCAPE:
                    cancelEdit();
                    break;
                case ENTER:
                    if (datePicker.getValue() != null) {
                        LocalDateTime newDueDate = datePicker.getValue().atTime(LocalTime.of(23, 59));
                        commitEdit(newDueDate);
                    }
                    break;
                default:
                    break;
            }
        });
    }

    private HBox createDateDisplay(LocalDateTime date) {
        HBox container = new HBox(5);
        container.setAlignment(Pos.CENTER_LEFT);

        if (date == null) {
            Label label = new Label("No date");
            label.setFont(Font.font("Poppins", 12));
            label.setTextFill(Color.GRAY);
            container.getChildren().add(label);
        } else {
            boolean isOverdue = date.isBefore(LocalDateTime.now());

            if (isOverdue) {
                Label warningIcon = new Label("⚠");
                warningIcon.setFont(Font.font(12));
                warningIcon.setTextFill(Color.rgb(198, 40, 40));
                container.getChildren().add(warningIcon);
            }

            Label dateLabel = new Label(date.format(FORMATTER));
            dateLabel.setFont(Font.font("Poppins", 12));

            if (isOverdue) {
                dateLabel.setTextFill(Color.rgb(198, 40, 40));
                dateLabel.setStyle("-fx-font-weight: 600;");
            } else {
                dateLabel.setTextFill(Color.rgb(50, 50, 50));
            }

            container.getChildren().add(dateLabel);
        }

        return container;
    }
}
