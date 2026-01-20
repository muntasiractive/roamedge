package com.roam.repository;

import com.roam.model.Operation;
import com.roam.model.OperationStatus;
import com.roam.model.Priority;
import com.roam.service.ValidationService;
import com.roam.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Repository class for managing {@link Operation} entity persistence
 * operations.
 * <p>
 * This repository provides CRUD operations and specialized query methods for
 * operations,
 * including support for filtering by status and priority. Operations represent
 * high-level projects or initiatives that can contain tasks, wikis, and
 * calendar events.
 * </p>
 * <p>
 * This repository includes validation before persistence using
 * {@link ValidationService}.
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 * @see Operation
 * @see TaskRepository
 * @see WikiRepository
 */
public class OperationRepository {

    private static final Logger logger = LoggerFactory.getLogger(OperationRepository.class);

    /** Validation service for entity validation before persistence. */
    private final ValidationService validationService = ValidationService.getInstance();

    // ==================== CRUD Operations ====================

    /**
     * Save (create or update) an operation
     */
    public Operation save(Operation operation) {
        // Validate entity before persisting
        validationService.validate(operation);

        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            if (operation.getId() == null) {
                // New operation - persist
                em.persist(operation);
                em.flush(); // Ensure ID is generated
                logger.debug("✓ Operation created: {}", operation.getName());
            } else {
                // Existing operation - merge
                operation = em.merge(operation);
                logger.debug("✓ Operation updated: {}", operation.getName());
            }

            tx.commit();
            return operation;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("✗ Failed to save operation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save operation", e);
        } finally {
            em.close();
        }
    }

    /**
     * Finds an operation by its unique identifier.
     *
     * @param id the unique identifier of the operation
     * @return an Optional containing the operation if found, or empty if not found
     */
    public Optional<Operation> findById(Long id) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            Operation operation = em.find(Operation.class, id);
            return Optional.ofNullable(operation);
        } finally {
            em.close();
        }
    }

    // ==================== Query Methods ====================

    /**
     * Retrieves all operations ordered by creation date descending (newest first).
     *
     * @return a list of all operations (may be empty, never null)
     */
    public List<Operation> findAll() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<Operation> query = em.createQuery(
                    "SELECT o FROM Operation o ORDER BY o.createdAt DESC",
                    Operation.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Finds all operations with a specific status.
     *
     * @param status the operation status to filter by
     * @return a list of operations with the specified status, ordered by creation
     *         date descending
     */
    public List<Operation> findByStatus(OperationStatus status) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<Operation> query = em.createQuery(
                    "SELECT o FROM Operation o WHERE o.status = :status ORDER BY o.createdAt DESC",
                    Operation.class);
            query.setParameter("status", status);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Finds all operations with a specific priority level.
     *
     * @param priority the priority level to filter by
     * @return a list of operations with the specified priority, ordered by creation
     *         date descending
     */
    public List<Operation> findByPriority(Priority priority) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<Operation> query = em.createQuery(
                    "SELECT o FROM Operation o WHERE o.priority = :priority ORDER BY o.createdAt DESC",
                    Operation.class);
            query.setParameter("priority", priority);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    // ==================== Delete Operations ====================

    /**
     * Deletes an operation by its unique identifier.
     * <p>
     * The operation is wrapped in a transaction with automatic rollback on failure.
     * If no operation exists with the given ID, the operation completes silently.
     * Note: This may affect associated tasks, wikis, and calendar events.
     * </p>
     *
     * @param id the unique identifier of the operation to delete
     * @throws RuntimeException if the delete operation fails
     */
    public void delete(Long id) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            Operation operation = em.find(Operation.class, id);
            if (operation != null) {
                em.remove(operation);
                logger.debug("✓ Operation deleted: {}", operation.getName());
            }

            tx.commit();

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("✗ Failed to delete operation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete operation", e);
        } finally {
            em.close();
        }
    }

    /**
     * Deletes an operation entity.
     * <p>
     * Convenience method that delegates to {@link #delete(Long)} if the operation
     * and its ID are not null.
     * </p>
     *
     * @param operation the operation to delete
     */
    public void delete(Operation operation) {
        if (operation != null && operation.getId() != null) {
            delete(operation.getId());
        }
    }

    // ==================== Count Operations ====================

    /**
     * Counts the total number of operations in the database.
     *
     * @return the total count of operations
     */
    public long count() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(o) FROM Operation o",
                    Long.class);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }
}
