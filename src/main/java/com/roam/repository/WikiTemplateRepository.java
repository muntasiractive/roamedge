package com.roam.repository;

import com.roam.model.WikiTemplate;
import com.roam.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Repository class for managing {@link WikiTemplate} entity persistence
 * operations.
 * <p>
 * This repository provides CRUD operations and specialized query methods for
 * wiki templates,
 * which define reusable structures and content for wiki entries. Supports both
 * system
 * default templates and custom user-created templates.
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 * @see WikiTemplate
 * @see WikiRepository
 */
public class WikiTemplateRepository {

    private static final Logger logger = LoggerFactory.getLogger(WikiTemplateRepository.class);

    // ==================== CRUD Operations ====================

    /**
     * Saves a wiki template to the database (insert or update).
     * <p>
     * If the template's ID is null, a new record is created. Otherwise, the
     * existing
     * record is updated. The operation is wrapped in a transaction with automatic
     * rollback on failure.
     * </p>
     *
     * @param template the wiki template to save (must not be null)
     * @return the saved wiki template with generated ID (for new templates)
     * @throws RuntimeException if the save operation fails
     */
    public WikiTemplate save(WikiTemplate template) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            if (template.getId() == null) {
                em.persist(template);
            } else {
                template = em.merge(template);
            }

            tx.commit();
            return template;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("✗ Failed to save template: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save template", e);
        } finally {
            em.close();
        }
    }

    /**
     * Finds a wiki template by its unique identifier.
     *
     * @param id the unique identifier of the wiki template
     * @return an Optional containing the template if found, or empty if not found
     */
    public Optional<WikiTemplate> findById(Long id) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            WikiTemplate template = em.find(WikiTemplate.class, id);
            return Optional.ofNullable(template);
        } finally {
            em.close();
        }
    }

    // ==================== Query Methods ====================

    /**
     * Retrieves all wiki templates ordered by default status (defaults first) then
     * by name.
     *
     * @return a list of all wiki templates (may be empty, never null)
     */
    public List<WikiTemplate> findAll() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<WikiTemplate> query = em.createQuery(
                    "SELECT t FROM WikiTemplate t ORDER BY t.isDefault DESC, t.name ASC",
                    WikiTemplate.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Retrieves all default (system) wiki templates.
     *
     * @return a list of default wiki templates ordered by name
     */
    public List<WikiTemplate> findDefaults() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<WikiTemplate> query = em.createQuery(
                    "SELECT t FROM WikiTemplate t WHERE t.isDefault = true ORDER BY t.name",
                    WikiTemplate.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Retrieves all custom (user-created) wiki templates.
     * <p>
     * Returns only templates where isDefault is false.
     * </p>
     *
     * @return a list of custom wiki templates ordered by name
     */
    public List<WikiTemplate> findCustom() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<WikiTemplate> query = em.createQuery(
                    "SELECT t FROM WikiTemplate t WHERE t.isDefault = false ORDER BY t.name",
                    WikiTemplate.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    // ==================== Delete Operations ====================

    /**
     * Deletes a wiki template by its unique identifier.
     * <p>
     * The operation is wrapped in a transaction with automatic rollback on failure.
     * If no template exists with the given ID, the operation completes silently.
     * </p>
     *
     * @param id the unique identifier of the template to delete
     * @throws RuntimeException if the delete operation fails
     */
    public void delete(Long id) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            WikiTemplate template = em.find(WikiTemplate.class, id);
            if (template != null) {
                em.remove(template);
            }

            tx.commit();

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("✗ Failed to delete template: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete template", e);
        } finally {
            em.close();
        }
    }
}
