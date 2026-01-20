package com.roam.service;

import com.roam.model.Task;
import com.roam.model.TaskStatus;
import com.roam.model.Priority;
import com.roam.repository.TaskRepository;
import com.roam.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of TaskService.
 * 
 * Manages the lifecycle of tasks with full transaction support.
 * Handles persistence, status updates, priority filtering, and search indexing.
 * 
 * @author Roam Development Team
 * @version 1.0
 */
public class TaskServiceImpl implements TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);
    private final TaskRepository repository;
    private final SearchService searchService;

    // ==================== Constructors ====================

    /**
     * Default constructor that initializes repository and services with default
     * implementations.
     */
    public TaskServiceImpl() {
        this.repository = new TaskRepository();
        this.searchService = SearchService.getInstance();
    }

    /**
     * Constructor for dependency injection, primarily used for testing.
     * 
     * @param repository    the task repository
     * @param searchService the search service for indexing tasks
     */
    public TaskServiceImpl(TaskRepository repository, SearchService searchService) {
        this.repository = repository;
        this.searchService = searchService;
    }

    // ==================== CRUD Operations ====================

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Creates a new task within a managed transaction and indexes it for search.
     * </p>
     * 
     * @throws IllegalArgumentException if the task is null
     * @throws RuntimeException         if the transaction fails
     */
    @Override
    public Task createTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }

        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            Task created = repository.save(task);

            tx.commit();
            logger.info("✓ Task created: {}", created.getTitle());

            indexTask(created);

            return created;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("✗ Failed to create task: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create task", e);
        } finally {
            em.close();
        }
    }

    @Override
    public Task updateTask(Task task) {
        if (task == null || task.getId() == null) {
            throw new IllegalArgumentException("Task and ID cannot be null");
        }

        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            Task updated = repository.save(task);

            tx.commit();
            logger.info("✓ Task updated: {}", updated.getTitle());

            indexTask(updated);

            return updated;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("✗ Failed to update task: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update task", e);
        } finally {
            em.close();
        }
    }

    @Override
    public void deleteTask(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Task ID cannot be null");
        }

        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            Optional<Task> task = repository.findById(id);
            if (task.isPresent()) {
                repository.delete(task.get());
                logger.info("✓ Task deleted: {}", task.get().getTitle());
            }

            tx.commit();

            searchService.deleteDocument(id);

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

    @Override
    public Optional<Task> findById(Long id) {
        try {
            return repository.findById(id);
        } catch (Exception e) {
            logger.error("✗ Failed to find task by ID {}: {}", id, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public List<Task> findAll() {
        try {
            return repository.findAll();
        } catch (Exception e) {
            logger.error("✗ Failed to find all tasks: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve tasks", e);
        }
    }

    // ==================== Query Operations ====================

    @Override
    public List<Task> findByOperationId(Long operationId) {
        try {
            return repository.findByOperationId(operationId);
        } catch (Exception e) {
            logger.error("✗ Failed to find tasks by operation ID: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve tasks", e);
        }
    }

    @Override
    public List<Task> findByStatus(TaskStatus status) {
        try {
            return repository.findAll().stream()
                    .filter(t -> status.equals(t.getStatus()))
                    .toList();
        } catch (Exception e) {
            logger.error("✗ Failed to find tasks by status: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve tasks", e);
        }
    }

    @Override
    public List<Task> findByPriority(Priority priority) {
        try {
            return repository.findAll().stream()
                    .filter(t -> priority.equals(t.getPriority()))
                    .toList();
        } catch (Exception e) {
            logger.error("✗ Failed to find tasks by priority: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve tasks", e);
        }
    }

    @Override
    public List<Task> findDueBefore(LocalDate date) {
        try {
            return repository.findAll().stream()
                    .filter(t -> t.getDueDate() != null && t.getDueDate().toLocalDate().isBefore(date))
                    .toList();
        } catch (Exception e) {
            logger.error("✗ Failed to find tasks due before date: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve tasks", e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Finds all tasks that are past their due date and not yet completed.
     * A task is considered overdue if its due date is before today and its status
     * is not {@link TaskStatus#DONE}.
     * </p>
     * 
     * @return list of overdue tasks
     */
    @Override
    public List<Task> findOverdue() {
        try {
            LocalDate today = LocalDate.now();
            return repository.findAll().stream()
                    .filter(t -> t.getDueDate() != null && t.getDueDate().toLocalDate().isBefore(today))
                    .filter(t -> t.getStatus() != TaskStatus.DONE)
                    .toList();
        } catch (Exception e) {
            logger.error("✗ Failed to find overdue tasks: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve overdue tasks", e);
        }
    }

    // ==================== Utility Methods ====================

    @Override
    public long count() {
        try {
            return repository.findAll().size();
        } catch (Exception e) {
            logger.error("✗ Failed to count tasks: {}", e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public long countByStatus(TaskStatus status) {
        try {
            return repository.countByStatus(status);
        } catch (Exception e) {
            logger.error("✗ Failed to count tasks by status: {}", e.getMessage(), e);
            return 0;
        }
    }

    // ==================== Status Management ====================

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Updates the status of a task within a managed transaction and re-indexes
     * the task for search.
     * </p>
     * 
     * @throws IllegalArgumentException if id or newStatus is null, or task not
     *                                  found
     * @throws RuntimeException         if the transaction fails
     */
    @Override
    public Task updateStatus(Long id, TaskStatus newStatus) {
        if (id == null || newStatus == null) {
            throw new IllegalArgumentException("Task ID and status cannot be null");
        }

        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            Optional<Task> taskOpt = repository.findById(id);
            if (taskOpt.isEmpty()) {
                throw new IllegalArgumentException("Task not found: " + id);
            }

            Task task = taskOpt.get();
            task.setStatus(newStatus);
            Task updated = repository.save(task);

            tx.commit();
            logger.info("✓ Task status updated: {} → {}", updated.getTitle(), newStatus);

            indexTask(updated);

            return updated;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("✗ Failed to update task status: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update task status", e);
        } finally {
            em.close();
        }
    }

    // ==================== Search Indexing ====================

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Indexes the task for full-text search functionality.
     * </p>
     */
    @Override
    public void indexTask(Task task) {
        try {
            searchService.indexTask(
                    task.getId(),
                    task.getTitle(),
                    task.getDescription(),
                    task.getPriority() != null ? task.getPriority().toString() : null,
                    task.getStatus() != null ? task.getStatus().toString() : null,
                    task.getOperationId(),
                    task.getDueDate());
            logger.debug("✓ Task indexed: {}", task.getTitle());
        } catch (Exception e) {
            logger.error("✗ Failed to index task: {}", e.getMessage(), e);
        }
    }
}
