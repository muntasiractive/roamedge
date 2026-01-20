package com.roam.repository;

import com.roam.model.Priority;
import com.roam.model.Task;
import com.roam.model.TaskFilter;
import com.roam.model.TaskStatus;
import com.roam.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Repository class for managing {@link Task} entity persistence operations.
 * <p>
 * This repository provides comprehensive CRUD operations and specialized query
 * methods
 * for tasks, including filtering, batch operations, and various count methods.
 * Tasks are associated with operations and support features like priority,
 * status,
 * due dates, and assignees.
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 * @see Task
 * @see TaskFilter
 * @see OperationRepository
 */
public class TaskRepository {

    private static final Logger logger = LoggerFactory.getLogger(TaskRepository.class);

    // ==================== CRUD Operations ====================

    /**
     * Saves a task to the database (insert or update).
     * <p>
     * If the task's ID is null, a new record is created. Otherwise, the existing
     * record is updated. The operation is wrapped in a transaction with automatic
     * rollback on failure.
     * </p>
     *
     * @param task the task to save (must not be null)
     * @return the saved task with generated ID (for new tasks)
     * @throws RuntimeException if the save operation fails
     */
    public Task save(Task task) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            if (task.getId() == null) {
                em.persist(task);
                em.flush(); // Ensure ID is generated
                logger.debug("✓ Task created: {}", task.getTitle());
            } else {
                task = em.merge(task);
                logger.debug("✓ Task updated: {}", task.getTitle());
            }

            tx.commit();
            return task;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("✗ Failed to save task: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save task", e);
        } finally {
            em.close();
        }
    }

    /**
     * Finds a task by its unique identifier.
     *
     * @param id the unique identifier of the task
     * @return an Optional containing the task if found, or empty if not found
     */
    public Optional<Task> findById(Long id) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            Task task = em.find(Task.class, id);
            return Optional.ofNullable(task);
        } finally {
            em.close();
        }
    }

    // ==================== Query Methods ====================

    /**
     * Finds all tasks associated with a specific operation.
     *
     * @param operationId the ID of the operation
     * @return a list of tasks for the operation, ordered by position then creation
     *         date
     */
    public List<Task> findByOperationId(Long operationId) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<Task> query = em.createQuery(
                    "SELECT t FROM Task t WHERE t.operationId = :operationId ORDER BY t.position ASC, t.createdAt DESC",
                    Task.class);
            query.setParameter("operationId", operationId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Finds all tasks for an operation with a specific status.
     *
     * @param operationId the ID of the operation
     * @param status      the task status to filter by
     * @return a list of tasks matching the criteria, ordered by position then
     *         creation date
     */
    public List<Task> findByOperationIdAndStatus(Long operationId, TaskStatus status) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<Task> query = em.createQuery(
                    "SELECT t FROM Task t WHERE t.operationId = :operationId AND t.status = :status ORDER BY t.position ASC, t.createdAt DESC",
                    Task.class);
            query.setParameter("operationId", operationId);
            query.setParameter("status", status);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    // ==================== Delete Operations ====================

    /**
     * Deletes a task by its unique identifier.
     * <p>
     * The operation is wrapped in a transaction with automatic rollback on failure.
     * If no task exists with the given ID, the operation completes silently.
     * </p>
     *
     * @param id the unique identifier of the task to delete
     * @throws RuntimeException if the delete operation fails
     */
    public void delete(Long id) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            Task task = em.find(Task.class, id);
            if (task != null) {
                em.remove(task);
                logger.debug("✓ Task deleted: {}", task.getTitle());
            }

            tx.commit();

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("✗ Failed to delete task: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete task", e);
        } finally {
            em.close();
        }
    }

    /**
     * Deletes a task entity.
     * <p>
     * Convenience method that delegates to {@link #delete(Long)} if the task
     * and its ID are not null.
     * </p>
     *
     * @param task the task to delete
     */
    public void delete(Task task) {
        if (task != null && task.getId() != null) {
            delete(task.getId());
        }
    }

    // ==================== Position Management ====================

    /**
     * Updates the position of a task for ordering purposes.
     * <p>
     * The operation is wrapped in a transaction with automatic rollback on failure.
     * </p>
     *
     * @param id       the unique identifier of the task
     * @param position the new position value
     * @throws RuntimeException if the update operation fails
     */
    public void updatePosition(Long id, Integer position) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            Task task = em.find(Task.class, id);
            if (task != null) {
                task.setPosition(position);
                em.merge(task);
            }

            tx.commit();

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("✗ Failed to update task position: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update task position", e);
        } finally {
            em.close();
        }
    }

    /**
     * Retrieves all tasks across all operations.
     *
     * @return a list of all tasks ordered by creation date descending
     */
    public List<Task> findAll() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<Task> query = em.createQuery(
                    "SELECT t FROM Task t ORDER BY t.createdAt DESC",
                    Task.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Finds tasks with multiple filter criteria applied.
     * <p>
     * Retrieves all tasks and applies filters in-memory including:
     * operation IDs, statuses, priorities, assignees, search query,
     * due date filters, and completed task visibility.
     * </p>
     *
     * @param filter the filter criteria to apply
     * @return a list of tasks matching all filter criteria
     */
    public List<Task> findWithFilters(TaskFilter filter) {
        List<Task> allTasks = findAll();

        // Apply filters in Java (simplified - in production use JPQL WHERE clauses)
        return allTasks.stream()
                .filter(task -> {
                    // Operation filter
                    if (!filter.getOperationIds().isEmpty()) {
                        if (!filter.getOperationIds().contains(task.getOperationId())) {
                            return false;
                        }
                    }

                    // Status filter
                    if (!filter.getStatuses().isEmpty()) {
                        if (!filter.getStatuses().contains(task.getStatus())) {
                            return false;
                        }
                    }

                    // Priority filter
                    if (!filter.getPriorities().isEmpty()) {
                        if (!filter.getPriorities().contains(task.getPriority())) {
                            return false;
                        }
                    }

                    // Assignee filter
                    if (!filter.getAssignees().isEmpty()) {
                        String assignee = task.getAssignee();
                        if (assignee == null || !filter.getAssignees().contains(assignee)) {
                            return false;
                        }
                    }

                    // Search query
                    if (filter.getSearchQuery() != null && !filter.getSearchQuery().trim().isEmpty()) {
                        String query = filter.getSearchQuery().toLowerCase();
                        String title = task.getTitle() != null ? task.getTitle().toLowerCase() : "";
                        String desc = task.getDescription() != null ? task.getDescription().toLowerCase() : "";
                        if (!title.contains(query) && !desc.contains(query)) {
                            return false;
                        }
                    }

                    // Due date filter
                    if (filter.getDueDateFilter() != TaskFilter.DueDateFilter.ANY) {
                        LocalDateTime dueDate = task.getDueDate();
                        LocalDateTime now = LocalDateTime.now();

                        switch (filter.getDueDateFilter()) {
                            case OVERDUE:
                                if (dueDate == null || !dueDate.isBefore(now)) {
                                    return false;
                                }
                                break;
                            case TODAY:
                                if (dueDate == null || !dueDate.toLocalDate().equals(LocalDate.now())) {
                                    return false;
                                }
                                break;
                            case TOMORROW:
                                if (dueDate == null || !dueDate.toLocalDate().equals(LocalDate.now().plusDays(1))) {
                                    return false;
                                }
                                break;
                            case THIS_WEEK:
                                if (dueDate == null || dueDate.isAfter(now.plusDays(7))) {
                                    return false;
                                }
                                break;
                            case THIS_MONTH:
                                if (dueDate == null || dueDate.isAfter(now.plusDays(30))) {
                                    return false;
                                }
                                break;
                            case NO_DUE_DATE:
                                if (dueDate != null) {
                                    return false;
                                }
                                break;
                            case HAS_DUE_DATE:
                                if (dueDate == null) {
                                    return false;
                                }
                                break;
                        }
                    }

                    // Show completed filter
                    if (!filter.getShowCompleted() && task.getStatus() == TaskStatus.DONE) {
                        return false;
                    }

                    return true;
                })
                .collect(Collectors.toList());
    }

    // ==================== Count Operations ====================

    /**
     * Counts the number of tasks with a specific status.
     *
     * @param status the task status to count
     * @return the count of tasks with the specified status
     */
    public long countByStatus(TaskStatus status) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(t) FROM Task t WHERE t.status = :status",
                    Long.class);
            query.setParameter("status", status);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    /**
     * Counts the total number of tasks in the database.
     *
     * @return the total count of all tasks
     */
    public long countAll() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(t) FROM Task t",
                    Long.class);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    /**
     * Retrieves all unique assignee names from tasks.
     *
     * @return a list of unique assignee names, ordered alphabetically
     */
    public List<String> getAllAssignees() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<String> query = em.createQuery(
                    "SELECT DISTINCT t.assignee FROM Task t WHERE t.assignee IS NOT NULL ORDER BY t.assignee",
                    String.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Counts the number of high priority tasks that are not completed.
     *
     * @return the count of incomplete high priority tasks
     */
    public long countHighPriority() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(t) FROM Task t WHERE t.priority = :priority AND t.status != :status",
                    Long.class);
            query.setParameter("priority", Priority.HIGH);
            query.setParameter("status", TaskStatus.DONE);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    /**
     * Counts the number of overdue tasks that are not completed.
     * <p>
     * A task is considered overdue if its due date is before the current date/time.
     * </p>
     *
     * @return the count of incomplete overdue tasks
     */
    public long countOverdue() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(t) FROM Task t WHERE t.dueDate < :now AND t.status != :status",
                    Long.class);
            query.setParameter("now", LocalDateTime.now());
            query.setParameter("status", TaskStatus.DONE);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    // ==================== Batch Operations ====================

    /**
     * Updates the status of multiple tasks in a single transaction.
     *
     * @param taskIds   the list of task IDs to update
     * @param newStatus the new status to set
     * @return the number of tasks successfully updated
     * @throws RuntimeException if the batch update fails
     */
    public int batchUpdateStatus(List<Long> taskIds, TaskStatus newStatus) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            int count = 0;
            for (Long id : taskIds) {
                Task task = em.find(Task.class, id);
                if (task != null) {
                    task.setStatus(newStatus);
                    em.merge(task);
                    count++;
                }
            }

            tx.commit();
            logger.debug("✓ Batch updated {} tasks to status: {}", count, newStatus);
            return count;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("✗ Failed to batch update: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to batch update", e);
        } finally {
            em.close();
        }
    }

    /**
     * Deletes multiple tasks in a single transaction.
     *
     * @param taskIds the list of task IDs to delete
     * @return the number of tasks successfully deleted
     * @throws RuntimeException if the batch delete fails
     */
    public int batchDelete(List<Long> taskIds) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            int count = 0;
            for (Long id : taskIds) {
                Task task = em.find(Task.class, id);
                if (task != null) {
                    em.remove(task);
                    count++;
                }
            }

            tx.commit();
            logger.debug("✓ Batch deleted {} tasks", count);
            return count;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("✗ Failed to batch delete: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to batch delete", e);
        } finally {
            em.close();
        }
    }

    /**
     * Updates the priority of multiple tasks in a single transaction.
     *
     * @param taskIds     the list of task IDs to update
     * @param newPriority the new priority to set
     * @return the number of tasks successfully updated
     * @throws RuntimeException if the batch update fails
     */
    public int batchUpdatePriority(List<Long> taskIds, Priority newPriority) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            int count = 0;
            for (Long id : taskIds) {
                Task task = em.find(Task.class, id);
                if (task != null) {
                    task.setPriority(newPriority);
                    em.merge(task);
                    count++;
                }
            }

            tx.commit();
            logger.debug("✓ Batch updated priority for {} tasks to: {}", count, newPriority);
            return count;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("✗ Failed to batch update priority: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to batch update priority", e);
        } finally {
            em.close();
        }
    }

    /**
     * Assigns multiple tasks to an assignee in a single transaction.
     * <p>
     * Set assignee to null to unassign the tasks.
     * </p>
     *
     * @param taskIds  the list of task IDs to assign
     * @param assignee the assignee name, or null to unassign
     * @return the number of tasks successfully updated
     * @throws RuntimeException if the batch assign fails
     */
    public int batchAssign(List<Long> taskIds, String assignee) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            int count = 0;
            for (Long id : taskIds) {
                Task task = em.find(Task.class, id);
                if (task != null) {
                    task.setAssignee(assignee);
                    em.merge(task);
                    count++;
                }
            }

            tx.commit();
            String action = assignee == null ? "Unassigned" : "Assigned to " + assignee;
            logger.debug("✓ {} for {} tasks", action, count);
            return count;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("✗ Failed to batch assign: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to batch assign", e);
        } finally {
            em.close();
        }
    }

    /**
     * Sets the due date for multiple tasks in a single transaction.
     * <p>
     * Set dueDate to null to clear the due date for the tasks.
     * </p>
     *
     * @param taskIds the list of task IDs to update
     * @param dueDate the due date to set, or null to clear
     * @return the number of tasks successfully updated
     * @throws RuntimeException if the batch update fails
     */
    public int batchSetDueDate(List<Long> taskIds, LocalDateTime dueDate) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            int count = 0;
            for (Long id : taskIds) {
                Task task = em.find(Task.class, id);
                if (task != null) {
                    task.setDueDate(dueDate);
                    em.merge(task);
                    count++;
                }
            }

            tx.commit();
            String action = dueDate == null ? "Cleared due date" : "Set due date";
            logger.debug("✓ {} for {} tasks", action, count);
            return count;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("✗ Failed to batch set due date: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to batch set due date", e);
        } finally {
            em.close();
        }
    }
}
