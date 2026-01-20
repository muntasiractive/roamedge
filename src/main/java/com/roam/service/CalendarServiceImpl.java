package com.roam.service;

import com.roam.model.CalendarEvent;
import com.roam.model.Task;
import com.roam.repository.CalendarEventRepository;
import com.roam.repository.TaskRepository;
import com.roam.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of the {@link CalendarService} interface providing calendar
 * event management
 * functionality with full transaction support.
 * 
 * <p>
 * This service handles all CRUD operations for calendar events, including
 * synchronization
 * with tasks and integration with the search indexing service. All public
 * methods execute
 * within managed transactions with automatic rollback on failure.
 * </p>
 * 
 * <p>
 * Key features:
 * </p>
 * <ul>
 * <li>Transaction-managed persistence operations</li>
 * <li>Task-to-calendar event synchronization</li>
 * <li>Full-text search indexing integration</li>
 * <li>Query operations by date range, source, task, and operation</li>
 * </ul>
 * 
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 * @see CalendarService
 * @see CalendarEvent
 * @see CalendarEventRepository
 */
public class CalendarServiceImpl implements CalendarService {

    private static final Logger logger = LoggerFactory.getLogger(CalendarServiceImpl.class);
    private final CalendarEventRepository repository;
    private final TaskRepository taskRepository;
    private final SearchService searchService;

    // ==================== Constructors ====================

    /**
     * Default constructor that initializes repositories and services with default
     * implementations.
     */
    public CalendarServiceImpl() {
        this.repository = new CalendarEventRepository();
        this.taskRepository = new TaskRepository();
        this.searchService = SearchService.getInstance();
    }

    /**
     * Constructor for dependency injection, primarily used for testing.
     * 
     * @param repository     the calendar event repository
     * @param taskRepository the task repository for task-calendar synchronization
     * @param searchService  the search service for indexing events
     */
    public CalendarServiceImpl(CalendarEventRepository repository, TaskRepository taskRepository,
            SearchService searchService) {
        this.repository = repository;
        this.taskRepository = taskRepository;
        this.searchService = searchService;
    }

    // ==================== CRUD Operations ====================

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Creates a new calendar event within a managed transaction and indexes it for
     * search.
     * </p>
     * 
     * @throws IllegalArgumentException if the event is null
     * @throws RuntimeException         if the transaction fails
     */
    @Override
    public CalendarEvent createEvent(CalendarEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("CalendarEvent cannot be null");
        }

        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            CalendarEvent created = repository.save(event);

            tx.commit();
            logger.info("✓ Calendar event created: {}", created.getTitle());

            indexEvent(created);

            return created;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("✗ Failed to create calendar event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create calendar event", e);
        } finally {
            em.close();
        }
    }

    @Override
    public CalendarEvent updateEvent(CalendarEvent event) {
        if (event == null || event.getId() == null) {
            throw new IllegalArgumentException("CalendarEvent and ID cannot be null");
        }

        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            CalendarEvent updated = repository.save(event);

            tx.commit();
            logger.info("✓ Calendar event updated: {}", updated.getTitle());

            indexEvent(updated);

            return updated;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("✗ Failed to update calendar event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update calendar event", e);
        } finally {
            em.close();
        }
    }

    @Override
    public void deleteEvent(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Event ID cannot be null");
        }

        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            Optional<CalendarEvent> event = repository.findById(id);
            if (event.isPresent()) {
                repository.delete(id);
                logger.info("✓ Calendar event deleted: {}", event.get().getTitle());
            }

            tx.commit();

            searchService.deleteDocument(id);

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("✗ Failed to delete calendar event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete calendar event", e);
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<CalendarEvent> findById(Long id) {
        try {
            return repository.findById(id);
        } catch (Exception e) {
            logger.error("✗ Failed to find calendar event by ID {}: {}", id, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public List<CalendarEvent> findAll() {
        try {
            return repository.findAll();
        } catch (Exception e) {
            logger.error("✗ Failed to find all calendar events: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve calendar events", e);
        }
    }

    @Override
    public List<CalendarEvent> findByDateRange(LocalDateTime start, LocalDateTime end) {
        try {
            return repository.findByDateRange(start, end);
        } catch (Exception e) {
            logger.error("✗ Failed to find calendar events by date range: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve calendar events", e);
        }
    }

    @Override
    public List<CalendarEvent> findBySourceId(Long sourceId) {
        try {
            return repository.findByCalendarSourceId(sourceId);
        } catch (Exception e) {
            logger.error("✗ Failed to find calendar events by source ID: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve calendar events", e);
        }
    }

    @Override
    public List<CalendarEvent> findByTaskId(Long taskId) {
        try {
            Optional<CalendarEvent> event = repository.findByTaskId(taskId);
            return event.map(List::of).orElse(List.of());
        } catch (Exception e) {
            logger.error("✗ Failed to find calendar event by task ID: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve calendar event", e);
        }
    }

    @Override
    public List<CalendarEvent> findByOperationId(Long operationId) {
        try {
            return repository.findByOperationId(operationId);
        } catch (Exception e) {
            logger.error("✗ Failed to find calendar events by operation ID: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve calendar events", e);
        }
    }

    // ==================== Task Synchronization ====================

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Synchronizes a task to the calendar by creating or updating the corresponding
     * calendar event. If the task has a due date, an event is created spanning one
     * hour
     * before the deadline.
     * </p>
     * 
     * @throws IllegalArgumentException if taskId is null or task not found
     * @throws RuntimeException         if synchronization fails
     */
    @Override
    public void syncTaskToCalendar(Long taskId) {
        if (taskId == null) {
            throw new IllegalArgumentException("Task ID cannot be null");
        }

        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            // Find the task
            Optional<Task> taskOpt = taskRepository.findById(taskId);
            if (taskOpt.isEmpty()) {
                throw new IllegalArgumentException("Task not found: " + taskId);
            }

            Task task = taskOpt.get();

            // Only sync if task has a due date
            if (task.getDueDate() == null) {
                logger.debug("⚠️ Task {} has no due date, skipping calendar sync", taskId);
                return;
            }

            // Check if calendar event already exists for this task
            Optional<CalendarEvent> existingEvent = repository.findByTaskId(taskId);

            if (existingEvent.isPresent()) {
                // Update existing event
                CalendarEvent event = existingEvent.get();
                event.setTitle(task.getTitle());
                event.setDescription(task.getDescription());

                // Set event time to the due date (already LocalDateTime)
                LocalDateTime dueDateTime = task.getDueDate();
                event.setStartDateTime(dueDateTime.minusHours(1)); // 1 hour before deadline
                event.setEndDateTime(dueDateTime);

                repository.save(event);
                logger.info("✓ Updated calendar event for task: {}", task.getTitle());

                indexEvent(event);
            } else {
                // Create new event
                CalendarEvent event = new CalendarEvent();
                event.setTaskId(taskId);
                event.setOperationId(task.getOperationId());
                event.setTitle(task.getTitle());
                event.setDescription(task.getDescription());

                // Set event time to the due date (already LocalDateTime)
                LocalDateTime dueDateTime = task.getDueDate();
                event.setStartDateTime(dueDateTime.minusHours(1));
                event.setEndDateTime(dueDateTime);
                event.setIsAllDay(false);
                event.setCalendarSourceId(1L); // Default calendar source

                CalendarEvent created = repository.save(event);
                logger.info("✓ Created calendar event for task: {}", task.getTitle());

                indexEvent(created);
            }

            tx.commit();

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("✗ Failed to sync task to calendar: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to sync task to calendar", e);
        } finally {
            em.close();
        }
    }

    // ==================== Utility Methods ====================

    @Override
    public long count() {
        try {
            return repository.findAll().size();
        } catch (Exception e) {
            logger.error("✗ Failed to count calendar events: {}", e.getMessage(), e);
            return 0;
        }
    }

    // ==================== Search Indexing ====================

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Indexes the calendar event for full-text search functionality.
     * </p>
     */
    @Override
    public void indexEvent(CalendarEvent event) {
        try {
            searchService.indexEvent(
                    event.getId(),
                    event.getTitle(),
                    event.getDescription(),
                    event.getStartDateTime(),
                    event.getEndDateTime(),
                    event.getLocation());
            logger.debug("✓ Calendar event indexed: {}", event.getTitle());
        } catch (Exception e) {
            logger.error("✗ Failed to index calendar event: {}", e.getMessage(), e);
        }
    }
}
