package com.roam.view.components;

import com.roam.controller.TasksController;
import com.roam.model.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;

/**
 * Filter panel component for the Tasks view providing filtering controls.
 * <p>
 * This panel allows users to filter tasks by various criteria including
 * operations, status, priority, and due date. It supports multi-select
 * filtering and provides a clear filters button to reset all selections.
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public class TasksFilterPanel extends HBox {

    private final TasksController controller;

    private ComboBox<String> operationsFilter;
    private ComboBox<String> statusFilter;
    private ComboBox<String> priorityFilter;
    private ComboBox<String> dueDateFilter;
    private Button clearFiltersButton;

    private List<Long> selectedOperationIds = new ArrayList<>();
    private List<TaskStatus> selectedStatuses = new ArrayList<>();
    private List<Priority> selectedPriorities = new ArrayList<>();

    private boolean isRefreshing = false;

    public TasksFilterPanel(TasksController controller) {
        this.controller = controller;

        setPrefHeight(56);
        setPadding(new Insets(12, 20, 12, 20));
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(12);
        setStyle("-fx-background-color: -roam-bg-primary; " +
                "-fx-border-color: -roam-border; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 12; " +
                "-fx-background-radius: 12;");

        createFilterControls();
    }

    private void createFilterControls() {
        // Filter label
        Label filterLabel = new Label("Filters:");
        filterLabel.setFont(Font.font("Poppins Medium", 13));
        filterLabel.setStyle("-fx-text-fill: -roam-text-secondary;");
        filterLabel.setPadding(new Insets(0, 10, 0, 0));

        // Operations filter
        operationsFilter = createFilterCombo("All Operations", 160);
        updateOperationsFilter();

        // Status filter
        statusFilter = createFilterCombo("All Statuses", 130);
        statusFilter.getItems().addAll(
                "All Statuses",
                "To Do",
                "In Progress",
                "Done");
        statusFilter.setValue("All Statuses");
        statusFilter.setOnAction(e -> applyFilters());

        // Priority filter
        priorityFilter = createFilterCombo("All Priorities", 130);
        priorityFilter.getItems().addAll(
                "All Priorities",
                "High",
                "Medium",
                "Low");
        priorityFilter.setValue("All Priorities");
        priorityFilter.setOnAction(e -> applyFilters());

        // Due date filter
        dueDateFilter = createFilterCombo("Due: Any", 140);
        dueDateFilter.getItems().addAll(
                "Due: Any",
                "Overdue",
                "Today",
                "Tomorrow",
                "This Week",
                "This Month",
                "No Due Date",
                "Has Due Date");
        dueDateFilter.setValue("Due: Any");
        dueDateFilter.setOnAction(e -> applyFilters());

        // Clear filters button
        clearFiltersButton = new Button("Clear Filters");
        clearFiltersButton.setFont(Font.font("Poppins", 12));
        clearFiltersButton.setPrefHeight(36);
        clearFiltersButton.setPadding(new Insets(8, 16, 8, 16));
        clearFiltersButton.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: -roam-blue; " +
                        "-fx-border-color: -roam-blue; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 6; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand;");
        clearFiltersButton.setOnMouseEntered(e -> clearFiltersButton.setStyle(
                "-fx-background-color: -roam-blue-light; " +
                        "-fx-text-fill: -roam-blue; " +
                        "-fx-border-color: -roam-blue; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 6; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand;"));
        clearFiltersButton.setOnMouseExited(e -> clearFiltersButton.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: -roam-blue; " +
                        "-fx-border-color: -roam-blue; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 6; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand;"));
        clearFiltersButton.setOnAction(e -> clearFilters());
        clearFiltersButton.setVisible(false);

        getChildren().addAll(
                filterLabel,
                operationsFilter,
                statusFilter,
                priorityFilter,
                dueDateFilter,
                clearFiltersButton);
    }

    private ComboBox<String> createFilterCombo(String prompt, int width) {
        ComboBox<String> combo = new ComboBox<>();
        combo.setPromptText(prompt);
        combo.setPrefWidth(width);
        combo.setPrefHeight(36);
        combo.setStyle(
                "-fx-background-color: -roam-bg-secondary; " +
                        "-fx-background-radius: 8; " +
                        "-fx-border-color: transparent; " +
                        "-fx-border-radius: 8; " +
                        "-fx-border-width: 0; " +
                        "-fx-font-family: 'Poppins'; " +
                        "-fx-font-size: 13px;");
        return combo;
    }

    private void updateOperationsFilter() {
        // Temporarily remove the handler to prevent infinite loop
        operationsFilter.setOnAction(null);

        operationsFilter.getItems().clear();
        operationsFilter.getItems().add("All Operations");

        List<Operation> operations = controller.getAllOperations();
        for (Operation op : operations) {
            operationsFilter.getItems().add(op.getName());
        }

        operationsFilter.setValue("All Operations");

        // Re-add the handler after setting the value
        operationsFilter.setOnAction(e -> {
            if (!isRefreshing) {
                applyFilters();
            }
        });
    }

    private void applyFilters() {
        TaskFilter filter = controller.getCurrentFilter();

        // Operations filter
        selectedOperationIds.clear();
        String selectedOp = operationsFilter.getValue();
        if (selectedOp != null && !selectedOp.equals("All Operations")) {
            List<Operation> operations = controller.getAllOperations();
            for (Operation op : operations) {
                if (op.getName().equals(selectedOp)) {
                    selectedOperationIds.add(op.getId());
                }
            }
        }
        filter.setOperationIds(selectedOperationIds);

        // Status filter
        selectedStatuses.clear();
        String selectedStatus = statusFilter.getValue();
        if (selectedStatus != null && !selectedStatus.equals("All Statuses")) {
            switch (selectedStatus) {
                case "To Do" -> selectedStatuses.add(TaskStatus.TODO);
                case "In Progress" -> selectedStatuses.add(TaskStatus.IN_PROGRESS);
                case "Done" -> selectedStatuses.add(TaskStatus.DONE);
            }
        }
        filter.setStatuses(selectedStatuses);

        // Priority filter
        selectedPriorities.clear();
        String selectedPriority = priorityFilter.getValue();
        if (selectedPriority != null && !selectedPriority.equals("All Priorities")) {
            switch (selectedPriority) {
                case "High" -> selectedPriorities.add(Priority.HIGH);
                case "Medium" -> selectedPriorities.add(Priority.MEDIUM);
                case "Low" -> selectedPriorities.add(Priority.LOW);
            }
        }
        filter.setPriorities(selectedPriorities);

        // Due date filter
        String selectedDueDate = dueDateFilter.getValue();
        if (selectedDueDate != null) {
            switch (selectedDueDate) {
                case "Overdue" -> filter.setDueDateFilter(TaskFilter.DueDateFilter.OVERDUE);
                case "Today" -> filter.setDueDateFilter(TaskFilter.DueDateFilter.TODAY);
                case "Tomorrow" -> filter.setDueDateFilter(TaskFilter.DueDateFilter.TOMORROW);
                case "This Week" -> filter.setDueDateFilter(TaskFilter.DueDateFilter.THIS_WEEK);
                case "This Month" -> filter.setDueDateFilter(TaskFilter.DueDateFilter.THIS_MONTH);
                case "No Due Date" -> filter.setDueDateFilter(TaskFilter.DueDateFilter.NO_DUE_DATE);
                case "Has Due Date" -> filter.setDueDateFilter(TaskFilter.DueDateFilter.HAS_DUE_DATE);
                default -> filter.setDueDateFilter(TaskFilter.DueDateFilter.ANY);
            }
        }

        controller.applyFilter(filter);
        updateClearButtonVisibility();
    }

    private void clearFilters() {
        operationsFilter.setValue("All Operations");
        statusFilter.setValue("All Statuses");
        priorityFilter.setValue("All Priorities");
        dueDateFilter.setValue("Due: Any");

        controller.clearFilters();
        clearFiltersButton.setVisible(false);
    }

    private void updateClearButtonVisibility() {
        boolean hasActiveFilters = !operationsFilter.getValue().equals("All Operations") ||
                !statusFilter.getValue().equals("All Statuses") ||
                !priorityFilter.getValue().equals("All Priorities") ||
                !dueDateFilter.getValue().equals("Due: Any");

        clearFiltersButton.setVisible(hasActiveFilters);
    }

    public void refresh() {
        isRefreshing = true;
        try {
            updateOperationsFilter();
        } finally {
            isRefreshing = false;
        }
    }
}
