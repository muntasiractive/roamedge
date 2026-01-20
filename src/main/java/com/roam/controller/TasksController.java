package com.roam.controller;

import com.roam.model.*;
import com.roam.service.TaskService;
import com.roam.service.TaskServiceImpl;
import com.roam.repository.CalendarEventRepository;
import com.roam.repository.WikiRepository;
import com.roam.repository.OperationRepository;
import com.roam.repository.RegionRepository;
import com.roam.repository.TaskRepository;
import com.roam.util.DialogUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/**
 * Manages tasks and their related operations.
 * <p>
 * Handles everything related to tasks:
 * - Creating, reading, updating, and deleting tasks
 * - Filtering and searching
 * - Selecting multiple tasks for batch actions
 * - Calculating statistics
 *
 * @author Roam Development Team
 * @version 1.0
 */
public class TasksController {

    // ==================== Fields ====================

    private static final Logger logger = LoggerFactory.getLogger(TasksController.class);

    private final TaskService taskService;
    private final TaskRepository taskRepository; // Keep for batch operations not yet in service
    private final OperationRepository operationRepository;
    private final RegionRepository regionRepository;
    private final CalendarEventRepository eventRepository;
    private final WikiRepository WikiRepository;
    private TaskFilter currentFilter;
    private final ObservableSet<Task> selectedTasks;
    private Runnable onDataChanged;

    public TasksController() {
        this.taskService = new TaskServiceImpl();
        this.taskRepository = new TaskRepository(); // Keep for batch operations
        this.operationRepository = new OperationRepository();
        this.regionRepository = new RegionRepository();
        this.eventRepository = new CalendarEventRepository();
        this.WikiRepository = new WikiRepository();
        this.currentFilter = new TaskFilter();
        this.selectedTasks = FXCollections.observableSet(new HashSet<>());
    }

    // ==================== CRUD Operations ====================

    public void createTask() {
        createTask(TaskStatus.TODO);
    }

    public void createTask(TaskStatus initialStatus) {
        Task task = new Task();
        task.setStatus(initialStatus != null ? initialStatus : TaskStatus.TODO);
        task.setPriority(Priority.MEDIUM);

        com.roam.view.components.TaskDialog dialog = new com.roam.view.components.TaskDialog(
                task,
                null,
                getAllOperations(),
                regionRepository.findAll(),
                eventRepository.findAll(),
                WikiRepository.findAll());
        dialog.showAndWait().ifPresent(newTask -> {
            try {
                if (newTask.getOperationId() == null) {
                    DialogUtils.showError("Validation Error", "Operation is required",
                            "Please select an operation for this task");
                    return;
                }
                taskService.createTask(newTask);
                refreshView();
                DialogUtils.showSuccess("Task created successfully");
            } catch (Exception e) {
                DialogUtils.showError("Error", "Failed to create task", e.getMessage());
            }
        });
    }

    public void editTask(Task task) {
        com.roam.view.components.TaskDialog dialog = new com.roam.view.components.TaskDialog(
                task,
                () -> deleteTask(task.getId()),
                getAllOperations(),
                regionRepository.findAll(),
                eventRepository.findAll(),
                WikiRepository.findAll());

        dialog.showAndWait().ifPresent(updatedTask -> {
            try {
                taskService.updateTask(updatedTask);
                refreshView();
                DialogUtils.showSuccess("Task updated successfully");
            } catch (Exception e) {
                DialogUtils.showError("Error", "Failed to update task", e.getMessage());
            }
        });
    }

    public void deleteTask(Long taskId) {
        try {
            taskService.deleteTask(taskId);
            selectedTasks.removeIf(t -> t.getId().equals(taskId));
            refreshView();
            DialogUtils.showSuccess("Task deleted successfully");
        } catch (Exception e) {
            DialogUtils.showError("Error", "Failed to delete task", e.getMessage());
        }
    }

    public void updateTaskStatus(Long taskId, TaskStatus newStatus) {
        try {
            taskService.updateStatus(taskId, newStatus);
            refreshView();
        } catch (Exception e) {
            DialogUtils.showError("Error", "Failed to update task status", e.getMessage());
        }
    }

    public void updateTaskStatus(Task task, TaskStatus newStatus) {
        try {
            task.setStatus(newStatus);
            taskService.updateTask(task);
            refreshView();
        } catch (Exception e) {
            DialogUtils.showError("Error", "Failed to update task status", e.getMessage());
        }
    }

    public void updateTask(Task task) {
        try {
            taskService.updateTask(task);
            refreshView();
        } catch (Exception e) {
            DialogUtils.showError("Error", "Failed to update task", e.getMessage());
        }
    }

    // ==================== Filtering & Search ====================

    public List<Task> loadTasks() {
        try {
            return taskRepository.findWithFilters(currentFilter);
        } catch (Exception e) {
            DialogUtils.showError("Error", "Failed to load tasks", e.getMessage());
            return new ArrayList<>();
        }
    }

    public void applyFilter(TaskFilter filter) {
        this.currentFilter = filter;
        refreshView();
    }

    public void clearFilters() {
        this.currentFilter.reset();
        refreshView();
    }

    public void searchTasks(String query) {
        currentFilter.setSearchQuery(query);
        refreshView();
    }

    // ==================== Selection Management ====================

    public void toggleTaskSelection(Task task) {
        if (selectedTasks.contains(task)) {
            selectedTasks.remove(task);
        } else {
            selectedTasks.add(task);
        }
    }

    public void selectAll(List<Task> tasks) {
        selectedTasks.addAll(tasks);
    }

    public void clearSelection() {
        selectedTasks.clear();
    }

    public ObservableSet<Task> getSelectedTasks() {
        return selectedTasks;
    }

    // ==================== Batch Operations ====================

    public void batchUpdateStatus(TaskStatus newStatus) {
        if (selectedTasks.isEmpty()) {
            DialogUtils.showInfo("No Selection", "No tasks selected", "Please select tasks to update");
            return;
        }

        try {
            List<Long> taskIds = selectedTasks.stream()
                    .map(Task::getId)
                    .toList();

            int count = taskRepository.batchUpdateStatus(taskIds, newStatus);
            clearSelection();
            refreshView();
            DialogUtils.showSuccess("Updated " + count + " tasks");
        } catch (Exception e) {
            DialogUtils.showError("Error", "Failed to batch update", e.getMessage());
        }
    }

    public void batchDelete() {
        if (selectedTasks.isEmpty()) {
            DialogUtils.showInfo("No Selection", "No tasks selected", "Please select tasks to delete");
            return;
        }

        try {
            List<Long> taskIds = selectedTasks.stream()
                    .map(Task::getId)
                    .toList();

            int count = taskRepository.batchDelete(taskIds);
            clearSelection();
            refreshView();
            DialogUtils.showSuccess("Deleted " + count + " tasks");
        } catch (Exception e) {
            DialogUtils.showError("Error", "Failed to batch delete", e.getMessage());
        }
    }

    public void batchUpdatePriority(Priority newPriority) {
        if (selectedTasks.isEmpty()) {
            DialogUtils.showInfo("No Selection", "No tasks selected", "Please select tasks to update");
            return;
        }

        try {
            List<Long> taskIds = selectedTasks.stream()
                    .map(Task::getId)
                    .toList();

            int count = taskRepository.batchUpdatePriority(taskIds, newPriority);
            clearSelection();
            refreshView();
            DialogUtils.showSuccess("Updated " + count + " tasks");
        } catch (Exception e) {
            DialogUtils.showError("Error", "Failed to batch update priority", e.getMessage());
        }
    }

    public void batchAssign(String assignee) {
        if (selectedTasks.isEmpty()) {
            DialogUtils.showInfo("No Selection", "No tasks selected", "Please select tasks to assign");
            return;
        }

        try {
            List<Long> taskIds = selectedTasks.stream()
                    .map(Task::getId)
                    .toList();

            int count = taskRepository.batchAssign(taskIds, assignee);
            clearSelection();
            refreshView();
            DialogUtils.showSuccess("Assigned " + count + " tasks");
        } catch (Exception e) {
            DialogUtils.showError("Error", "Failed to batch assign", e.getMessage());
        }
    }

    public void batchSetDueDate(java.time.LocalDateTime dueDate) {
        if (selectedTasks.isEmpty()) {
            DialogUtils.showInfo("No Selection", "No tasks selected", "Please select tasks to set due date");
            return;
        }

        try {
            List<Long> taskIds = selectedTasks.stream()
                    .map(Task::getId)
                    .toList();

            int count = taskRepository.batchSetDueDate(taskIds, dueDate);
            clearSelection();
            refreshView();
            DialogUtils.showSuccess("Updated due date for " + count + " tasks");
        } catch (Exception e) {
            DialogUtils.showError("Error", "Failed to batch set due date", e.getMessage());
        }
    }

    // ==================== Statistics ====================

    public TaskStats calculateStats() {
        try {
            long total = taskRepository.countAll();
            long todoCount = taskRepository.countByStatus(TaskStatus.TODO);
            long inProgressCount = taskRepository.countByStatus(TaskStatus.IN_PROGRESS);
            long doneCount = taskRepository.countByStatus(TaskStatus.DONE);
            long highPriorityCount = taskRepository.countHighPriority();
            long overdueCount = taskRepository.countOverdue();

            return new TaskStats(total, todoCount, inProgressCount, doneCount, highPriorityCount, overdueCount);
        } catch (Exception e) {
            DialogUtils.showError("Error", "Failed to calculate stats", e.getMessage());
            return new TaskStats(0, 0, 0, 0, 0, 0);
        }
    }

    // ==================== Utilities ====================

    public void refreshView() {
        if (onDataChanged != null) {
            onDataChanged.run();
        }
    }

    public void setOnDataChanged(Runnable callback) {
        this.onDataChanged = callback;
    }

    public TaskFilter getCurrentFilter() {
        return currentFilter;
    }

    public List<Operation> getAllOperations() {
        return operationRepository.findAll();
    }

    public Optional<Operation> getOperationById(Long operationId) {
        return operationRepository.findById(operationId);
    }

    public List<String> getAllAssignees() {
        return taskRepository.getAllAssignees();
    }

    // ==================== Inner Class: Task Statistics ====================

    public static class TaskStats {
        private final long total;
        private final long todoCount;
        private final long inProgressCount;
        private final long doneCount;
        private final long highPriorityCount;
        private final long overdueCount;

        public TaskStats(long total, long todoCount, long inProgressCount,
                long doneCount, long highPriorityCount, long overdueCount) {
            this.total = total;
            this.todoCount = todoCount;
            this.inProgressCount = inProgressCount;
            this.doneCount = doneCount;
            this.highPriorityCount = highPriorityCount;
            this.overdueCount = overdueCount;
        }

        public long getTotal() {
            return total;
        }

        public long getTodoCount() {
            return todoCount;
        }

        public long getInProgressCount() {
            return inProgressCount;
        }

        public long getDoneCount() {
            return doneCount;
        }

        public long getHighPriorityCount() {
            return highPriorityCount;
        }

        public long getOverdueCount() {
            return overdueCount;
        }
    }
}
