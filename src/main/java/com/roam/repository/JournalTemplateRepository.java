package com.roam.repository;

import com.roam.model.JournalTemplate;
import com.roam.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

/**
 * Repository class for managing {@link JournalTemplate} entity persistence
 * operations.
 * <p>
 * This repository provides CRUD operations for journal templates, which define
 * reusable structures and content for journal entries. Templates help users
 * maintain consistent journaling formats and prompts.
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 * @see JournalTemplate
 * @see JournalEntryRepository
 */
public class JournalTemplateRepository {

    // ==================== CRUD Operations ====================

    /**
     * Saves a journal template to the database (insert or update).
     * <p>
     * If the template's ID is null, a new record is created. Otherwise, the
     * existing
     * record is updated. The operation is wrapped in a transaction with automatic
     * rollback on failure.
     * </p>
     *
     * @param template the journal template to save (must not be null)
     * @return the saved journal template with generated ID (for new templates)
     * @throws RuntimeException if the save operation fails
     */
    public JournalTemplate save(JournalTemplate template) {
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
            if (tx != null && tx.isActive())
                tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Finds a journal template by its unique identifier.
     *
     * @param id the unique identifier of the journal template
     * @return an Optional containing the template if found, or empty if not found
     */
    public Optional<JournalTemplate> findById(Long id) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(JournalTemplate.class, id));
        } finally {
            em.close();
        }
    }

    // ==================== Query Methods ====================

    /**
     * Retrieves all journal templates ordered by name ascending.
     *
     * @return a list of all journal templates (may be empty, never null)
     */
    public List<JournalTemplate> findAll() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<JournalTemplate> query = em.createQuery("SELECT t FROM JournalTemplate t ORDER BY t.name ASC",
                    JournalTemplate.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    // ==================== Delete Operations ====================

    /**
     * Deletes a journal template from the database.
     * <p>
     * The operation is wrapped in a transaction with automatic rollback on failure.
     * If the template does not exist, the operation completes silently.
     * </p>
     *
     * @param template the journal template to delete
     * @throws RuntimeException if the delete operation fails
     */
    public void delete(JournalTemplate template) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;
        try {
            tx = em.getTransaction();
            tx.begin();
            JournalTemplate managed = em.find(JournalTemplate.class, template.getId());
            if (managed != null) {
                em.remove(managed);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive())
                tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
