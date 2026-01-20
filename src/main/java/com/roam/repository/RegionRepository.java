package com.roam.repository;

import com.roam.model.Region;
import com.roam.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Repository class for managing {@link Region} entity persistence operations.
 * <p>
 * This repository provides CRUD operations and specialized query methods for
 * regions,
 * which represent geographical or logical groupings for calendar sources and
 * events.
 * Supports both default system regions and custom user-defined regions.
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 * @see Region
 * @see CalendarSourceRepository
 */
public class RegionRepository {

    private static final Logger logger = LoggerFactory.getLogger(RegionRepository.class);

    // ==================== CRUD Operations ====================

    /**
     * Saves a region to the database (insert or update).
     * <p>
     * If the region's ID is null, a new record is created. Otherwise, the existing
     * record is updated. The operation is wrapped in a transaction with automatic
     * rollback on failure.
     * </p>
     *
     * @param region the region to save (must not be null)
     * @return the saved region with generated ID (for new regions)
     * @throws RuntimeException if the save operation fails
     */
    public Region save(Region region) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            if (region.getId() == null) {
                em.persist(region);
            } else {
                region = em.merge(region);
            }

            tx.commit();
            return region;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("✗ Failed to save region: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save region", e);
        } finally {
            em.close();
        }
    }

    /**
     * Finds a region by its unique identifier.
     *
     * @param id the unique identifier of the region
     * @return an Optional containing the region if found, or empty if not found
     */
    public Optional<Region> findById(Long id) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            Region region = em.find(Region.class, id);
            return Optional.ofNullable(region);
        } finally {
            em.close();
        }
    }

    // ==================== Query Methods ====================

    /**
     * Retrieves all regions ordered by default status (defaults first) then by
     * name.
     *
     * @return a list of all regions (may be empty, never null)
     */
    public List<Region> findAll() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<Region> query = em.createQuery(
                    "SELECT r FROM Region r ORDER BY r.isDefault DESC, r.name ASC",
                    Region.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Finds a region by its name.
     *
     * @param name the name of the region to find
     * @return an Optional containing the region if found, or empty if not found
     */
    public Optional<Region> findByName(String name) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<Region> query = em.createQuery(
                    "SELECT r FROM Region r WHERE r.name = :name",
                    Region.class);
            query.setParameter("name", name);
            List<Region> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } finally {
            em.close();
        }
    }

    /**
     * Retrieves all custom (non-default) regions.
     * <p>
     * Returns only user-created regions, excluding system default regions.
     * </p>
     *
     * @return a list of custom regions ordered by name
     */
    public List<Region> findCustomRegions() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<Region> query = em.createQuery(
                    "SELECT r FROM Region r WHERE r.isDefault = false ORDER BY r.name ASC",
                    Region.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    // ==================== Delete Operations ====================

    /**
     * Deletes a region by its unique identifier.
     * <p>
     * The operation is wrapped in a transaction with automatic rollback on failure.
     * If no region exists with the given ID, the operation completes silently.
     * Note: This may affect associated calendar sources.
     * </p>
     *
     * @param id the unique identifier of the region to delete
     * @throws RuntimeException if the delete operation fails
     */
    public void delete(Long id) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            Region region = em.find(Region.class, id);
            if (region != null) {
                em.remove(region);
            }

            tx.commit();

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("✗ Failed to delete region: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete region", e);
        } finally {
            em.close();
        }
    }

    // ==================== Initialization Methods ====================

    /**
     * Creates default system regions if they don't already exist.
     * <p>
     * Iterates through the predefined default regions and colors from
     * {@link Region},
     * creating any that are not already present in the database. This is typically
     * called during application initialization.
     * </p>
     */
    public void createDefaultRegions() {
        for (int i = 0; i < Region.DEFAULT_REGIONS.length; i++) {
            String name = Region.DEFAULT_REGIONS[i];
            String color = Region.DEFAULT_COLORS[i];

            Optional<Region> existing = findByName(name);
            if (existing.isEmpty()) {
                Region region = new Region(name, color, true);
                save(region);
                logger.debug("✓ Created default region: {}", name);
            }
        }
    }
}
