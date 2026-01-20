package com.roam.repository;

import com.roam.model.CalendarSource;
import com.roam.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Repository class for managing {@link CalendarSource} entity persistence
 * operations.
 * <p>
 * This repository provides CRUD operations and specialized query methods for
 * calendar sources,
 * including support for visibility filtering and default source identification.
 * Calendar sources represent different calendars that can contain events.
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 * @see CalendarSource
 * @see CalendarEventRepository
 */
public class CalendarSourceRepository {

    private static final Logger logger = LoggerFactory.getLogger(CalendarSourceRepository.class);

    // ==================== CRUD Operations ====================

    /**
     * Saves a calendar source to the database (insert or update).
     * <p>
     * If the source's ID is null, a new record is created. Otherwise, the existing
     * record is updated. The operation is wrapped in a transaction with automatic
     * rollback on failure.
     * </p>
     *
     * @param source the calendar source to save (must not be null)
     * @return the saved calendar source with generated ID (for new sources)
     * @throws RuntimeException if the save operation fails
     */
    public CalendarSource save(CalendarSource source) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            if (source.getId() == null) {
                em.persist(source);
                logger.debug("✓ Calendar source created: {}", source.getName());
            } else {
                source = em.merge(source);
                logger.debug("✓ Calendar source updated: {}", source.getName());
            }

            tx.commit();
            return source;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("✗ Failed to save calendar source: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save calendar source", e);
        } finally {
            em.close();
        }
    }

    /**
     * Finds a calendar source by its unique identifier.
     *
     * @param id the unique identifier of the calendar source
     * @return an Optional containing the source if found, or empty if not found
     */
    public Optional<CalendarSource> findById(Long id) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            CalendarSource source = em.find(CalendarSource.class, id);
            return Optional.ofNullable(source);
        } finally {
            em.close();
        }
    }

    // ==================== Query Methods ====================

    /**
     * Retrieves all calendar sources ordered by creation date ascending.
     *
     * @return a list of all calendar sources (may be empty, never null)
     */
    public List<CalendarSource> findAll() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<CalendarSource> query = em.createQuery(
                    "SELECT c FROM CalendarSource c ORDER BY c.createdAt ASC",
                    CalendarSource.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Retrieves all visible calendar sources.
     * <p>
     * Returns only calendar sources where the isVisible flag is true,
     * ordered by creation date ascending.
     * </p>
     *
     * @return a list of visible calendar sources
     */
    public List<CalendarSource> findVisible() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<CalendarSource> query = em.createQuery(
                    "SELECT c FROM CalendarSource c WHERE c.isVisible = true ORDER BY c.createdAt ASC",
                    CalendarSource.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Finds the default calendar source.
     * <p>
     * Returns the calendar source marked as the default for new events.
     * There should typically be only one default source.
     * </p>
     *
     * @return an Optional containing the default source if found, or empty if none
     *         exists
     */
    public Optional<CalendarSource> findDefault() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<CalendarSource> query = em.createQuery(
                    "SELECT c FROM CalendarSource c WHERE c.isDefault = true",
                    CalendarSource.class);
            List<CalendarSource> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } finally {
            em.close();
        }
    }

    // ==================== Delete Operations ====================

    /**
     * Deletes a calendar source by its unique identifier.
     * <p>
     * The operation is wrapped in a transaction with automatic rollback on failure.
     * If no source exists with the given ID, the operation completes silently.
     * Note: This may fail if there are calendar events associated with this source.
     * </p>
     *
     * @param id the unique identifier of the source to delete
     * @throws RuntimeException if the delete operation fails
     */
    public void delete(Long id) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            CalendarSource source = em.find(CalendarSource.class, id);
            if (source != null) {
                em.remove(source);
                logger.debug("✓ Calendar source deleted: {}", source.getName());
            }

            tx.commit();

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("✗ Failed to delete calendar source: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete calendar source", e);
        } finally {
            em.close();
        }
    }
}
