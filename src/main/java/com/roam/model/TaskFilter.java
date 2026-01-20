package com.roam.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Filter and sorting configuration for task list queries.
 * 
 * <p>
 * TaskFilter encapsulates all filtering and sorting criteria used when
 * retrieving tasks from the data layer. It supports multi-criteria filtering
 * by operation, status, priority, assignee, and due date ranges, along with
 * text search and configurable sorting.
 * </p>
 * 
 * <h2>Features:</h2>
 * <ul>
 * <li>Multi-select filtering by operation, status, priority, and assignee</li>
 * <li>Predefined due date range filters (today, this week, overdue, etc.)</li>
 * <li>Full-text search query support</li>
 * <li>Configurable sort field and direction</li>
 * <li>Toggle for showing/hiding completed tasks</li>
 * <li>Filter state detection and reset functionality</li>
 * <li>Clone support for filter state preservation</li>
 * </ul>
 * 
 * <h2>Default Configuration:</h2>
 * <ul>
 * <li>No filters applied (all tasks shown)</li>
 * <li>Sorted by creation date, descending</li>
 * <li>Completed tasks visible</li>
 * </ul>
 * 
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 * @see Task
 * @see TaskStatus
 * @see Priority
 */
public class TaskFilter {

    // ==================== Filter Fields ====================

    /** List of operation IDs to filter by (empty = all operations) */
    private List<Long> operationIds;

    /** List of task statuses to include (empty = all statuses) */
    private List<TaskStatus> statuses;

    /** List of priority levels to include (empty = all priorities) */
    private List<Priority> priorities;

    /** List of assignee names to filter by (empty = all assignees) */
    private List<String> assignees;

    /** Due date range filter criterion */
    private DueDateFilter dueDateFilter;

    /** Text search query for task title/description matching */
    private String searchQuery;

    // ==================== Sort Fields ====================

    /** Field to sort results by */
    private TaskSortField sortBy;

    /** Sort direction (ascending or descending) */
    private SortOrder sortOrder;

    // ==================== Display Options ====================

    /** Flag to include or exclude completed tasks from results */
    private Boolean showCompleted;

    // ==================== Enumerations ====================

    /**
     * Predefined due date filter options for quick date range selection.
     */
    public enum DueDateFilter {
        /** No due date filtering applied */
        ANY,
        /** Tasks past their due date */
        OVERDUE,
        /** Tasks due today */
        TODAY,
        /** Tasks due tomorrow */
        TOMORROW,
        /** Tasks due within the current week */
        THIS_WEEK,
        /** Tasks due within the current month */
        THIS_MONTH,
        /** Tasks without a due date set */
        NO_DUE_DATE,
        /** Tasks that have a due date set */
        HAS_DUE_DATE
    }

    /**
     * Available fields for sorting task results.
     */
    public enum TaskSortField {
        /** Sort by task creation timestamp */
        CREATED_AT,
        /** Sort by last update timestamp */
        UPDATED_AT,
        /** Sort by due date */
        DUE_DATE,
        /** Sort by priority level */
        PRIORITY,
        /** Sort alphabetically by title */
        TITLE,
        /** Sort by task status */
        STATUS,
        /** Sort by associated operation */
        OPERATION
    }

    /**
     * Sort direction options.
     */
    public enum SortOrder {
        /** Ascending order (A-Z, oldest first, lowest priority first) */
        ASC,
        /** Descending order (Z-A, newest first, highest priority first) */
        DESC
    }

    // ==================== Constructors ====================

    /**
     * Creates a new TaskFilter with default settings.
     * All filter lists are empty (no filtering), sorted by creation date
     * descending,
     * and completed tasks are visible.
     */
    public TaskFilter() {
        this.operationIds = new ArrayList<>();
        this.statuses = new ArrayList<>();
        this.priorities = new ArrayList<>();
        this.assignees = new ArrayList<>();
        this.dueDateFilter = DueDateFilter.ANY;
        this.searchQuery = null;
        this.sortBy = TaskSortField.CREATED_AT;
        this.sortOrder = SortOrder.DESC;
        this.showCompleted = true;
    }

    // ==================== Filter State Methods ====================

    /**
     * Checks if any filter criteria is currently active.
     * 
     * @return {@code true} if any filter is applied, {@code false} if using
     *         defaults
     */
    public boolean isFilterActive() {
        return !operationIds.isEmpty() ||
                !statuses.isEmpty() ||
                !priorities.isEmpty() ||
                !assignees.isEmpty() ||
                dueDateFilter != DueDateFilter.ANY ||
                (searchQuery != null && !searchQuery.trim().isEmpty()) ||
                !showCompleted;
    }

    /**
     * Resets all filter criteria to their default values.
     * Clears all filter lists, resets due date filter to ANY,
     * clears search query, and restores default sort settings.
     */
    public void reset() {
        operationIds.clear();
        statuses.clear();
        priorities.clear();
        assignees.clear();
        dueDateFilter = DueDateFilter.ANY;
        searchQuery = null;
        sortBy = TaskSortField.CREATED_AT;
        sortOrder = SortOrder.DESC;
        showCompleted = true;
    }

    /**
     * Creates a deep copy of this TaskFilter.
     * All filter lists are copied to new ArrayList instances.
     * 
     * @return a new TaskFilter with the same filter settings
     */
    public TaskFilter clone() {
        TaskFilter copy = new TaskFilter();
        copy.operationIds = new ArrayList<>(this.operationIds);
        copy.statuses = new ArrayList<>(this.statuses);
        copy.priorities = new ArrayList<>(this.priorities);
        copy.assignees = new ArrayList<>(this.assignees);
        copy.dueDateFilter = this.dueDateFilter;
        copy.searchQuery = this.searchQuery;
        copy.sortBy = this.sortBy;
        copy.sortOrder = this.sortOrder;
        copy.showCompleted = this.showCompleted;
        return copy;
    }

    // ==================== Getters and Setters ====================

    public List<Long> getOperationIds() {
        return operationIds;
    }

    public void setOperationIds(List<Long> operationIds) {
        this.operationIds = operationIds;
    }

    public List<TaskStatus> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<TaskStatus> statuses) {
        this.statuses = statuses;
    }

    public List<Priority> getPriorities() {
        return priorities;
    }

    public void setPriorities(List<Priority> priorities) {
        this.priorities = priorities;
    }

    public List<String> getAssignees() {
        return assignees;
    }

    public void setAssignees(List<String> assignees) {
        this.assignees = assignees;
    }

    public DueDateFilter getDueDateFilter() {
        return dueDateFilter;
    }

    public void setDueDateFilter(DueDateFilter dueDateFilter) {
        this.dueDateFilter = dueDateFilter;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public TaskSortField getSortBy() {
        return sortBy;
    }

    public void setSortBy(TaskSortField sortBy) {
        this.sortBy = sortBy;
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Boolean getShowCompleted() {
        return showCompleted;
    }

    public void setShowCompleted(Boolean showCompleted) {
        this.showCompleted = showCompleted;
    }

    // ==================== Object Methods ====================

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        TaskFilter that = (TaskFilter) o;
        return java.util.Objects.equals(operationIds, that.operationIds) &&
                java.util.Objects.equals(statuses, that.statuses) &&
                java.util.Objects.equals(priorities, that.priorities) &&
                java.util.Objects.equals(assignees, that.assignees) &&
                dueDateFilter == that.dueDateFilter &&
                java.util.Objects.equals(searchQuery, that.searchQuery) &&
                sortBy == that.sortBy &&
                sortOrder == that.sortOrder &&
                java.util.Objects.equals(showCompleted, that.showCompleted);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(operationIds, statuses, priorities, assignees,
                dueDateFilter, searchQuery, sortBy, sortOrder, showCompleted);
    }

    @Override
    public String toString() {
        return "TaskFilter{" +
                "operationIds=" + operationIds +
                ", statuses=" + statuses +
                ", priorities=" + priorities +
                ", dueDateFilter=" + dueDateFilter +
                ", searchQuery='" + searchQuery + '\'' +
                ", sortBy=" + sortBy +
                ", sortOrder=" + sortOrder +
                ", showCompleted=" + showCompleted +
                '}';
    }
}
