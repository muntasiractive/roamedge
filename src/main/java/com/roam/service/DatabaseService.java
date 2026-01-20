package com.roam.service;

import com.roam.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service class responsible for managing database operations and lifecycle.
 * <p>
 * This service handles the initialization of the database connection, schema
 * creation,
 * and provides utility methods for testing database connectivity. It uses
 * Hibernate
 * as the ORM framework and integrates with the {@link HibernateUtil} for entity
 * manager factory management.
 * </p>
 * <p>
 * The service automatically initializes default templates and regions during
 * database setup through the {@link DataInitializer}.
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 * @see HibernateUtil
 * @see DataInitializer
 */
public class DatabaseService {

    // ==================== Constants ====================

    private static final Logger logger = LoggerFactory.getLogger(DatabaseService.class);

    // ==================== Database Initialization ====================

    /**
     * Initializes the database connection and schema.
     * <p>
     * This method performs the following operations:
     * <ul>
     * <li>Triggers EntityManagerFactory creation</li>
     * <li>Creates the database file and tables if they don't exist</li>
     * <li>Tests the database connection</li>
     * <li>Initializes default templates via {@link DataInitializer}</li>
     * </ul>
     * </p>
     *
     * @throws RuntimeException if database initialization fails
     */
    public static void initializeDatabase() {
        try {
            logger.info("📦 Initializing database...");

            // This will trigger EntityManagerFactory creation
            // which will create the database file and tables
            HibernateUtil.getEntityManagerFactory();

            // Test connection
            if (testConnection()) {
                logger.info("✓ Database initialized successfully");
                logger.info("📍 Database location: {}", getDatabasePath());

                // Initialize default templates
                DataInitializer initializer = new DataInitializer();
                initializer.initializeDefaultTemplates();
            } else {
                logger.error("✗ Database connection test failed");
            }

        } catch (Exception e) {
            logger.error("✗ Database initialization failed: {}", e.getMessage(), e);
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    // ==================== Connection Management ====================

    /**
     * Tests the database connection by executing a simple query.
     * <p>
     * This method creates an entity manager, executes a simple SELECT 1 query,
     * and returns whether the connection was successful.
     * </p>
     *
     * @return {@code true} if the connection test succeeds, {@code false} otherwise
     */
    public static boolean testConnection() {
        EntityManager em = null;
        try {
            em = HibernateUtil.getEntityManager();
            // Simple query to test connection
            em.createNativeQuery("SELECT 1").getSingleResult();
            return true;
        } catch (Exception e) {
            logger.error("Connection test failed: {}", e.getMessage());
            return false;
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    // ==================== Utility Methods ====================

    /**
     * Returns the absolute path to the database file.
     * <p>
     * The database is stored in the user's home directory under the roam folder.
     * </p>
     *
     * @return the absolute path to the H2 database file
     */
    public static String getDatabasePath() {
        String userHome = System.getProperty("user.home");
        return userHome + "/roam/roamdb.mv.db";
    }
}
