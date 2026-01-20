package com.roam.repository.base;

import com.roam.model.base.BaseEntity;
import com.roam.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Generic repository providing common CRUD (Create, Read, Update, Delete)
 * operations.
 * <p>
 * This abstract base class implements the Repository pattern with
 * JPA/Hibernate,
 * providing type-safe database operations for any entity extending
 * {@link BaseEntity}.
 * Concrete repository classes should extend this class to inherit standard CRUD
 * functionality while adding entity-specific query methods as needed.
 * </p>
 * <p>
 * Features:
 * <ul>
 * <li>Transaction management with automatic rollback on failure</li>
 * <li>Entity persistence (create) and merge (update) operations</li>
 * <li>Type-safe find operations by ID</li>
 * <li>Bulk retrieval and count operations</li>
 * <li>Safe deletion with existence checks</li>
 * </ul>
 * </p>
 *
 * @param <T>  the entity type extending {@link BaseEntity}
 * @param <ID> the type of the entity's primary key (typically {@link Long})
 * 
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 * @see BaseEntity
 * @see HibernateUtil
 */
public abstract class GenericRepository<T extends BaseEntity, ID> {

    private static final Logger logger = LoggerFactory.getLogger(GenericRepository.class);

    /** The entity class type managed by this repository. */
    protected final Class<T> entityClass;

    // ==================== Constructor ====================

    /**
     * Constructs a new GenericRepository for the specified entity class.
     *
     * @param entityClass the Class object representing the entity type
     */
    protected GenericRepository(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    // ==================== Entity Manager ====================

    /**
     * Obtains an EntityManager instance from the HibernateUtil.
     * <p>
     * Subclasses should use this method to acquire an EntityManager for
     * custom query operations. The caller is responsible for closing
     * the EntityManager after use.
     * </p>
     *
     * @return a new EntityManager instance
     */
    protected EntityManager getEntityManager() {
        return HibernateUtil.getEntityManager();
    }

    // ==================== CRUD Operations ====================

    /**
     * Saves an entity to the database (insert or update).
     * <p>
     * If the entity's ID is null, a new record is created using persist.
     * Otherwise, the existing record is updated using merge.
     * The operation is wrapped in a transaction with automatic rollback on failure.
     * </p>
     *
     * @param entity the entity to save (must not be null)
     * @return the saved entity with generated ID (for new entities)
     * @throws RuntimeException if the save operation fails
     */
    public T save(T entity) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            if (entity.getId() == null) {
                em.persist(entity);
                logger.debug("{} created", entityClass.getSimpleName());
            } else {
                entity = em.merge(entity);
                logger.debug("{} updated", entityClass.getSimpleName());
            }
            em.getTransaction().commit();
            return entity;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("Failed to save {}: {}", entityClass.getSimpleName(), e.getMessage(), e);
            throw new RuntimeException("Failed to save " + entityClass.getSimpleName(), e);
        } finally {
            em.close();
        }
    }

    /**
     * Finds an entity by its primary key.
     *
     * @param id the primary key of the entity to find
     * @return an Optional containing the entity if found, or empty if not found
     */
    public Optional<T> findById(ID id) {
        EntityManager em = getEntityManager();
        try {
            T entity = em.find(entityClass, id);
            return Optional.ofNullable(entity);
        } finally {
            em.close();
        }
    }

    /**
     * Retrieves all entities of this type from the database.
     *
     * @return a list of all entities (may be empty, never null)
     */
    public List<T> findAll() {
        EntityManager em = getEntityManager();
        try {
            String query = "SELECT e FROM " + entityClass.getSimpleName() + " e";
            TypedQuery<T> typedQuery = em.createQuery(query, entityClass);
            return typedQuery.getResultList();
        } finally {
            em.close();
        }
    }

    // ==================== Delete Operations ====================

    /**
     * Deletes an entity by its primary key.
     * <p>
     * The operation is wrapped in a transaction with automatic rollback on failure.
     * If no entity exists with the given ID, the operation completes silently.
     * </p>
     *
     * @param id the primary key of the entity to delete
     * @throws RuntimeException if the delete operation fails
     */
    public void delete(ID id) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            T entity = em.find(entityClass, id);
            if (entity != null) {
                em.remove(entity);
                logger.debug("{} deleted", entityClass.getSimpleName());
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("Failed to delete {}: {}", entityClass.getSimpleName(), e.getMessage(), e);
            throw new RuntimeException("Failed to delete " + entityClass.getSimpleName(), e);
        } finally {
            em.close();
        }
    }

    // ==================== Count Operations ====================

    /**
     * Counts the total number of entities of this type in the database.
     *
     * @return the total count of entities
     */
    public long count() {
        EntityManager em = getEntityManager();
        try {
            String query = "SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e";
            return em.createQuery(query, Long.class).getSingleResult();
        } finally {
            em.close();
        }
    }

    // ==================== Utility Methods ====================

    /**
     * Checks whether an entity with the given ID exists in the database.
     *
     * @param id the primary key to check
     * @return true if an entity with the given ID exists, false otherwise
     */
    public boolean existsById(ID id) {
        return findById(id).isPresent();
    }
}
