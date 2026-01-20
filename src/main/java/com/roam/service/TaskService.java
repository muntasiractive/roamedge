package com.roam.service;

import com.roam.model.Task;
import com.roam.model.TaskStatus;
import com.roam.model.Priority;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for Task business logic and transaction management.
 * <p>
 * Provides comprehensive task management capabilities including CRUD
 * operations,
 * status tracking, priority management, and deadline monitoring.
 * </p>
 * 
 * <h2>Key Operations:</h2>
 * <ul>
 * <li>Create, update, and delete tasks</li>
 * <li>Find tasks by ID, operation, status, or priority</li>
 * <li>Track overdue tasks and upcoming deadlines</li>
 * <li>Update task status with workflow support</li>
 * <li>Index tasks for search functionality</li>
 * </ul>
 * 
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public interface TaskService {

    /**
     * Creates a new task.
     *
     * @param task the task to create; must not be {@code null}
     * @return the created task with generated ID
     * @throws IllegalArgumentException if task is {@code null} or invalid
     */
    Task createTask(Task task);

    /**
     * Updates an existing task.
     *
     * @param task the task to update; must not be {@code null} and must have a
     *             valid ID
     * @return the updated task
     * @throws IllegalArgumentException if task is {@code null} or ID is
     *                                  {@code null}
     */
    Task updateTask(Task task);

    /**
     * Deletes a task by its ID.
     *
     * @param id the ID of the task to delete; must not be {@code null}
     * @throws IllegalArgumentException if id is {@code null}
     */
    void deleteTask(Long id);

    /**
     * Finds a task by its ID.
     *
     * @param id the ID of the task to find
     * @return an {@link Optional} containing the task if found, or empty if not
     *         found
     */
    Optional<Task> findById(Long id);

    /**
     * Retrieves all tasks.
     *
     * @return a list of all tasks, ordered by creation date (newest first)
     */
    List<Task> findAll();

    /**
     * Finds all tasks associated with a specific operation.
     *
     * @param operationId the ID of the operation
     * @return a list of tasks belonging to the specified operation
     */
    List<Task> findByOperationId(Long operationId);

    /**
     * Finds all tasks with a specific status.
     *
     * @param status the task status to filter by
     * @return a list of tasks with the specified status
     */
    List<Task> findByStatus(TaskStatus status);

    /**
     * Finds all tasks with a specific priority.
     *
     * @param priority the priority level to filter by
     * @return a list of tasks with the specified priority
     */
    List<Task> findByPriority(Priority priority);

    /**
     * Finds all tasks with due dates before the specified date.
     *
     * @param date the date threshold
     * @return a list of tasks due before the specified date
     */
    List<Task> findDueBefore(LocalDate date);

    /**
     * Finds all overdue tasks (tasks with due dates in the past and not completed).
     *
     * @return a list of overdue tasks
     */
    List<Task> findOverdue();

    /**
     * Counts the total number of tasks.
     *
     * @return the total count of tasks
     */
    long count();

    /**
     * Counts tasks with a specific status.
     *
     * @param status the task status to count
     * @return the number of tasks with the specified status
     */
    long countByStatus(TaskStatus status);

    /**
     * Updates the status of a task.
     *
     * @param id        the ID of the task to update
     * @param newStatus the new status to set
     * @return the updated task
     * @throws IllegalArgumentException if id or newStatus is {@code null}
     */
    Task updateStatus(Long id, TaskStatus newStatus);

    /**
     * Indexes a task for search functionality.
     * <p>
     * This method is typically called automatically after create/update operations.
     * </p>
     *
     * @param task the task to index
     */
    void indexTask(Task task);
}
