package com.roam.repository;

import com.roam.model.Wiki;
import com.roam.service.ValidationService;
import com.roam.util.HibernateUtil;
import com.roam.util.InputSanitizer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Repository class for managing {@link Wiki} entity persistence operations.
 * <p>
 * This repository provides CRUD operations and specialized query methods for
 * wikis,
 * including support for favorites, recent entries, full-text search, and word
 * count
 * statistics. Wikis are documentation pages associated with operations.
 * </p>
 * <p>
 * This repository includes validation before persistence using
 * {@link ValidationService}
 * and input sanitization for search queries using {@link InputSanitizer}.
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 * @see Wiki
 * @see WikiFileAttachmentRepository
 * @see WikiTemplateRepository
 */
public class WikiRepository {

    private static final Logger logger = LoggerFactory.getLogger(WikiRepository.class);

    /** Validation service for entity validation before persistence. */
    private final ValidationService validationService = ValidationService.getInstance();

    // ==================== CRUD Operations ====================

    /**
     * Saves a wiki to the database (insert or update).
     * <p>
     * Validates the wiki before persistence. If the wiki's ID is null, a new record
     * is created. Otherwise, the existing record is updated. The operation is
     * wrapped
     * in a transaction with automatic rollback on failure.
     * </p>
     *
     * @param wiki the wiki to save (must not be null)
     * @return the saved wiki with generated ID (for new wikis)
     * @throws RuntimeException if validation or save operation fails
     */
    public Wiki save(Wiki wiki) {
        // Validate entity before persisting
        validationService.validate(wiki);

        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            if (wiki.getId() == null) {
                em.persist(wiki);
                em.flush(); // Ensure ID is generated
                logger.debug("✓ Wiki created: {}", wiki.getTitle());
            } else {
                wiki = em.merge(wiki);
                logger.debug("✓ Wiki updated: {}", wiki.getTitle());
            }

            tx.commit();
            return wiki;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("✗ Failed to save Wiki: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save Wiki", e);
        } finally {
            em.close();
        }
    }

    /**
     * Finds a wiki by its unique identifier.
     *
     * @param id the unique identifier of the wiki
     * @return an Optional containing the wiki if found, or empty if not found
     */
    public Optional<Wiki> findById(Long id) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            Wiki wiki = em.find(Wiki.class, id);
            return Optional.ofNullable(wiki);
        } finally {
            em.close();
        }
    }

    // ==================== Query Methods ====================

    /**
     * Finds all wikis associated with a specific operation.
     *
     * @param operationId the ID of the operation
     * @return a list of wikis for the operation, ordered by update date descending
     */
    public List<Wiki> findByOperationId(Long operationId) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<Wiki> query = em.createQuery(
                    "SELECT n FROM Wiki n WHERE n.operationId = :operationId ORDER BY n.updatedAt DESC",
                    Wiki.class);
            query.setParameter("operationId", operationId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    // ==================== Delete Operations ====================

    /**
     * Deletes a wiki by its unique identifier.
     * <p>
     * The operation is wrapped in a transaction with automatic rollback on failure.
     * If no wiki exists with the given ID, the operation completes silently.
     * Note: Associated file attachments should be deleted separately.
     * </p>
     *
     * @param id the unique identifier of the wiki to delete
     * @throws RuntimeException if the delete operation fails
     */
    public void delete(Long id) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            Wiki wiki = em.find(Wiki.class, id);
            if (wiki != null) {
                em.remove(wiki);
                logger.debug("✓ Wiki deleted: {}", wiki.getTitle());
            }

            tx.commit();

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("✗ Failed to delete Wiki: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete Wiki", e);
        } finally {
            em.close();
        }
    }

    /**
     * Deletes a wiki entity.
     * <p>
     * Convenience method that delegates to {@link #delete(Long)} if the wiki
     * and its ID are not null.
     * </p>
     *
     * @param wiki the wiki to delete
     */
    public void delete(Wiki wiki) {
        if (wiki != null && wiki.getId() != null) {
            delete(wiki.getId());
        }
    }

    /**
     * Retrieves all wikis ordered by update date descending (most recent first).
     *
     * @return a list of all wikis (may be empty, never null)
     */
    public List<Wiki> findAll() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<Wiki> query = em.createQuery(
                    "SELECT n FROM Wiki n ORDER BY n.updatedAt DESC",
                    Wiki.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Retrieves all wikis marked as favorites.
     *
     * @return a list of favorite wikis ordered by update date descending
     */
    public List<Wiki> findFavorites() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<Wiki> query = em.createQuery(
                    "SELECT n FROM Wiki n WHERE n.isFavorite = true ORDER BY n.updatedAt DESC",
                    Wiki.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Retrieves the most recently updated wikis.
     *
     * @param limit the maximum number of wikis to return
     * @return a list of recent wikis ordered by update date descending
     */
    public List<Wiki> findRecent(int limit) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<Wiki> query = em.createQuery(
                    "SELECT n FROM Wiki n ORDER BY n.updatedAt DESC",
                    Wiki.class);
            query.setMaxResults(limit);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    // ==================== Search Methods ====================

    /**
     * Searches wikis by title and content using full-text matching.
     * <p>
     * The search query is sanitized to prevent JPQL injection. The search is
     * case-insensitive and matches partial strings in both title and content.
     * </p>
     *
     * @param query the search query string
     * @return a list of matching wikis ordered by update date descending, or empty
     *         list if query is blank
     */
    public List<Wiki> searchFullText(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        // Sanitize input to prevent JPQL injection
        String sanitizedQuery = InputSanitizer.sanitizeForJPQL(query);

        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<Wiki> q = em.createQuery(
                    "SELECT n FROM Wiki n WHERE LOWER(n.title) LIKE LOWER(:query) OR LOWER(n.content) LIKE LOWER(:query) ORDER BY n.updatedAt DESC",
                    Wiki.class);
            q.setParameter("query", "%" + sanitizedQuery + "%");
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    // ==================== Statistics Methods ====================

    /**
     * Calculates the total word count across all wikis.
     *
     * @return the sum of word counts from all wikis, or 0 if no wikis exist
     */
    public long getTotalWordCount() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT SUM(n.wordCount) FROM Wiki n",
                    Long.class);
            Long result = query.getSingleResult();
            return result != null ? result : 0L;
        } finally {
            em.close();
        }
    }
}
