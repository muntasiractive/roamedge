package com.roam.repository;

import com.roam.model.CalendarEvent;
import com.roam.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository class for managing {@link CalendarEvent} entity persistence
 * operations.
 * <p>
 * This repository provides CRUD operations and specialized query methods for
 * calendar events,
 * including support for date range queries, recurring events, and associations
 * with
 * calendar sources, operations, and tasks.
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 * @see CalendarEvent
 * @see CalendarSourceRepository
 */
public class CalendarEventRepository {

    private static final Logger logger = LoggerFactory.getLogger(CalendarEventRepository.class);

    // ==================== CRUD Operations ====================

    /**
     * Saves a calendar event to the database (insert or update).
     * <p>
     * If the event's ID is null, a new record is created. Otherwise, the existing
     * record is updated. The operation is wrapped in a transaction with automatic
     * rollback on failure.
     * </p>
     *
     * @param event the calendar event to save (must not be null)
     * @return the saved calendar event with generated ID (for new events)
     * @throws RuntimeException if the save operation fails
     */
    public CalendarEvent save(CalendarEvent event) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            if (event.getId() == null) {
                em.persist(event);
                em.flush(); // Ensure ID is generated
                logger.debug("✓ Calendar event created: {}", event.getTitle());
            } else {
                event = em.merge(event);
                logger.debug("✓ Calendar event updated: {}", event.getTitle());
            }

            tx.commit();
            return event;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("✗ Failed to save calendar event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save calendar event", e);
        } finally {
            em.close();
        }
    }

    /**
     * Finds a calendar event by its unique identifier.
     *
     * @param id the unique identifier of the calendar event
     * @return an Optional containing the event if found, or empty if not found
     */
    public Optional<CalendarEvent> findById(Long id) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            CalendarEvent event = em.find(CalendarEvent.class, id);
            return Optional.ofNullable(event);
        } finally {
            em.close();
        }
    }

    // ==================== Query Methods ====================

    /**
     * Retrieves all calendar events ordered by start date/time ascending.
     *
     * @return a list of all calendar events (may be empty, never null)
     */
    public List<CalendarEvent> findAll() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<CalendarEvent> query = em.createQuery(
                    "SELECT e FROM CalendarEvent e ORDER BY e.startDateTime ASC",
                    CalendarEvent.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Finds calendar events within a specified date/time range.
     * <p>
     * Returns events where the start date/time falls within the specified range
     * (inclusive of start, exclusive of end).
     * </p>
     *
     * @param start the start of the date range (inclusive)
     * @param end   the end of the date range (exclusive)
     * @return a list of events within the range, ordered by start date/time
     */
    public List<CalendarEvent> findByDateRange(LocalDateTime start, LocalDateTime end) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<CalendarEvent> query = em.createQuery(
                    "SELECT e FROM CalendarEvent e WHERE e.startDateTime >= :start AND e.startDateTime < :end ORDER BY e.startDateTime ASC",
                    CalendarEvent.class);
            query.setParameter("start", start);
            query.setParameter("end", end);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Finds all calendar events associated with a specific calendar source.
     *
     * @param sourceId the ID of the calendar source
     * @return a list of events for the specified source, ordered by start date/time
     */
    public List<CalendarEvent> findByCalendarSourceId(Long sourceId) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<CalendarEvent> query = em.createQuery(
                    "SELECT e FROM CalendarEvent e WHERE e.calendarSourceId = :sourceId ORDER BY e.startDateTime ASC",
                    CalendarEvent.class);
            query.setParameter("sourceId", sourceId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Finds all calendar events associated with a specific operation.
     *
     * @param operationId the ID of the operation
     * @return a list of events for the specified operation, ordered by start
     *         date/time
     */
    public List<CalendarEvent> findByOperationId(Long operationId) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<CalendarEvent> query = em.createQuery(
                    "SELECT e FROM CalendarEvent e WHERE e.operationId = :operationId ORDER BY e.startDateTime ASC",
                    CalendarEvent.class);
            query.setParameter("operationId", operationId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Finds a calendar event associated with a specific task.
     *
     * @param taskId the ID of the task
     * @return an Optional containing the event if found, or empty if not found
     */
    public Optional<CalendarEvent> findByTaskId(Long taskId) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<CalendarEvent> query = em.createQuery(
                    "SELECT e FROM CalendarEvent e WHERE e.taskId = :taskId",
                    CalendarEvent.class);
            query.setParameter("taskId", taskId);
            List<CalendarEvent> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } finally {
            em.close();
        }
    }

    // ==================== Recurring Event Methods ====================

    /**
     * Finds all recurring calendar events (parent events with recurrence rules).
     * <p>
     * Returns only the parent recurring events (where isRecurringInstance is false
     * and recurrenceRule is set), not the generated instances.
     * </p>
     *
     * @return a list of parent recurring events
     */
    public List<CalendarEvent> findRecurringEvents() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<CalendarEvent> query = em.createQuery(
                    "SELECT e FROM CalendarEvent e WHERE e.recurrenceRule IS NOT NULL AND e.isRecurringInstance = false",
                    CalendarEvent.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    // ==================== Delete Operations ====================

    /**
     * Deletes a calendar event by its unique identifier.
     * <p>
     * The operation is wrapped in a transaction with automatic rollback on failure.
     * If no event exists with the given ID, the operation completes silently.
     * </p>
     *
     * @param id the unique identifier of the event to delete
     * @throws RuntimeException if the delete operation fails
     */
    public void delete(Long id) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            CalendarEvent event = em.find(CalendarEvent.class, id);
            if (event != null) {
                em.remove(event);
                logger.debug("✓ Calendar event deleted: {}", event.getTitle());
            }

            tx.commit();

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

    /**
     * Deletes an entire recurring event series including the parent and all
     * instances.
     * <p>
     * This method removes the parent recurring event and all generated instances
     * associated with it. The operation is wrapped in a transaction with automatic
     * rollback on failure.
     * </p>
     *
     * @param parentEventId the ID of the parent recurring event
     * @throws RuntimeException if the delete operation fails
     */
    public void deleteRecurringSeries(Long parentEventId) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            // Delete parent event
            CalendarEvent parent = em.find(CalendarEvent.class, parentEventId);
            if (parent != null) {
                em.remove(parent);
            }

            // Delete all instances
            TypedQuery<CalendarEvent> query = em.createQuery(
                    "SELECT e FROM CalendarEvent e WHERE e.parentEventId = :parentId",
                    CalendarEvent.class);
            query.setParameter("parentId", parentEventId);
            List<CalendarEvent> instances = query.getResultList();

            for (CalendarEvent instance : instances) {
                em.remove(instance);
            }

            tx.commit();
            logger.debug("✓ Recurring series deleted (parent and {} instances)", instances.size());

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("✗ Failed to delete recurring series: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete recurring series", e);
        } finally {
            em.close();
        }
    }
}
