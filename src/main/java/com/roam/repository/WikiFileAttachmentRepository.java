package com.roam.repository;

import com.roam.model.WikiFileAttachment;
import com.roam.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

/**
 * Repository class for managing {@link WikiFileAttachment} entity persistence
 * operations.
 * <p>
 * This repository provides CRUD operations for wiki file attachments, which
 * represent
 * files (images, documents, etc.) attached to wiki entries. Supports querying
 * by
 * wiki ID and bulk deletion for cleanup operations.
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 * @see WikiFileAttachment
 * @see WikiRepository
 */
public class WikiFileAttachmentRepository {

    // ==================== CRUD Operations ====================

    /**
     * Saves a wiki file attachment to the database (insert or update).
     * <p>
     * If the attachment's ID is null, a new record is created. Otherwise, the
     * existing
     * record is updated. The operation is wrapped in a transaction with automatic
     * rollback on failure.
     * </p>
     *
     * @param attachment the wiki file attachment to save (must not be null)
     * @return the saved attachment with generated ID (for new attachments)
     * @throws RuntimeException if the save operation fails
     */
    public WikiFileAttachment save(WikiFileAttachment attachment) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            if (attachment.getId() == null) {
                em.persist(attachment);
            } else {
                attachment = em.merge(attachment);
            }
            em.getTransaction().commit();
            return attachment;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Failed to save WikiFileAttachment", e);
        } finally {
            em.close();
        }
    }

    /**
     * Finds a wiki file attachment by its unique identifier.
     *
     * @param id the unique identifier of the attachment
     * @return an Optional containing the attachment if found, or empty if not found
     */
    public Optional<WikiFileAttachment> findById(Long id) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            WikiFileAttachment attachment = em.find(WikiFileAttachment.class, id);
            return Optional.ofNullable(attachment);
        } finally {
            em.close();
        }
    }

    // ==================== Query Methods ====================

    /**
     * Finds all file attachments associated with a specific wiki.
     *
     * @param wikiId the ID of the wiki
     * @return a list of attachments for the wiki, ordered by creation date
     *         descending
     */
    public List<WikiFileAttachment> findByWikiId(Long wikiId) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<WikiFileAttachment> query = em.createQuery(
                    "SELECT w FROM WikiFileAttachment w WHERE w.wikiId = :wikiId ORDER BY w.createdAt DESC",
                    WikiFileAttachment.class);
            query.setParameter("wikiId", wikiId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    // ==================== Delete Operations ====================

    /**
     * Deletes a wiki file attachment from the database.
     * <p>
     * The operation is wrapped in a transaction with automatic rollback on failure.
     * </p>
     *
     * @param attachment the attachment to delete
     * @throws RuntimeException if the delete operation fails
     */
    public void delete(WikiFileAttachment attachment) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            WikiFileAttachment managed = em.merge(attachment);
            em.remove(managed);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Failed to delete WikiFileAttachment", e);
        } finally {
            em.close();
        }
    }

    /**
     * Deletes a wiki file attachment by its unique identifier.
     * <p>
     * The operation is wrapped in a transaction with automatic rollback on failure.
     * If no attachment exists with the given ID, the operation completes silently.
     * </p>
     *
     * @param id the unique identifier of the attachment to delete
     * @throws RuntimeException if the delete operation fails
     */
    public void deleteById(Long id) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            WikiFileAttachment attachment = em.find(WikiFileAttachment.class, id);
            if (attachment != null) {
                em.remove(attachment);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Failed to delete WikiFileAttachment", e);
        } finally {
            em.close();
        }
    }

    /**
     * Deletes all file attachments associated with a specific wiki.
     * <p>
     * This is typically used when deleting a wiki to clean up all associated
     * attachments.
     * The operation is wrapped in a transaction with automatic rollback on failure.
     * </p>
     *
     * @param wikiId the ID of the wiki whose attachments should be deleted
     * @throws RuntimeException if the delete operation fails
     */
    public void deleteByWikiId(Long wikiId) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM WikiFileAttachment w WHERE w.wikiId = :wikiId")
                    .setParameter("wikiId", wikiId)
                    .executeUpdate();
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Failed to delete WikiFileAttachments for wiki", e);
        } finally {
            em.close();
        }
    }
}
