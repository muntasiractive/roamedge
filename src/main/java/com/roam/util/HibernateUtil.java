package com.roam.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Utilities for managing Hibernate and the EntityManagerFactory.
 * <p>
 * Handles the lazy initialization of the database connection.
 * It also makes sure Flyway migrations run before Hibernate starts up.
 * </p>
 * 
 * Usage:
 * 
 * <pre>
 * EntityManager em = HibernateUtil.getEntityManager();
 * // ... use em ...
 * HibernateUtil.shutdown();
 * </pre>
 * 
 * @author Roam Development Team
 * @version 1.0
 */
public class HibernateUtil {

    // ==================== Constants ====================

    private static final Logger logger = LoggerFactory.getLogger(HibernateUtil.class);
    private static final String PERSISTENCE_UNIT_NAME = "roam-pu";

    // ==================== Fields ====================

    private static volatile EntityManagerFactory entityManagerFactory;

    // ==================== Constructor ====================

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private HibernateUtil() {
    }

    // ==================== EntityManager Factory ====================

    /**
     * Gets the singleton EntityManagerFactory instance using thread-safe lazy
     * initialization.
     * <p>
     * On first invocation, this method:
     * <ol>
     * <li>Loads database configuration from {@link DatabaseConfig}</li>
     * <li>Runs Flyway migrations via {@link FlywayManager}</li>
     * <li>Creates the EntityManagerFactory with runtime properties</li>
     * </ol>
     * </p>
     * 
     * @return the singleton EntityManagerFactory instance
     * @throws ExceptionInInitializerError if initialization fails
     */
    public static EntityManagerFactory getEntityManagerFactory() {
        if (entityManagerFactory == null) {
            synchronized (HibernateUtil.class) {
                if (entityManagerFactory == null) {
                    try {
                        logger.info("🔧 Initializing Hibernate EntityManagerFactory...");

                        // Load database configuration dynamically
                        DatabaseConfig dbConfig = DatabaseConfig.getInstance();

                        // ===== FLYWAY MIGRATIONS =====
                        // Run database migrations BEFORE initializing Hibernate
                        logger.info("🔄 Running Flyway database migrations...");
                        boolean migrationSuccess = FlywayManager.runMigrations(
                                dbConfig.getJdbcUrl(),
                                dbConfig.getUsername(),
                                dbConfig.getPassword());

                        if (!migrationSuccess) {
                            throw new RuntimeException("Database migration failed. Cannot initialize Hibernate.");
                        }

                        // Override persistence.xml properties with runtime values
                        Map<String, String> properties = new HashMap<>();
                        properties.put("jakarta.persistence.jdbc.driver", dbConfig.getDriver());
                        properties.put("jakarta.persistence.jdbc.url", dbConfig.getJdbcUrl());
                        properties.put("jakarta.persistence.jdbc.user", dbConfig.getUsername());
                        properties.put("jakarta.persistence.jdbc.password", dbConfig.getPassword());

                        entityManagerFactory = Persistence.createEntityManagerFactory(
                                PERSISTENCE_UNIT_NAME,
                                properties);
                        logger.info("✓ Hibernate initialized successfully");
                    } catch (Exception e) {
                        logger.error("✗ Failed to initialize Hibernate: {}", e.getMessage(), e);
                        e.printStackTrace();
                        throw new ExceptionInInitializerError(e);
                    }
                }
            }
        }
        return entityManagerFactory;
    }

    // ==================== EntityManager Operations ====================

    /**
     * Creates and returns a new EntityManager instance.
     * <p>
     * Each call creates a new EntityManager that should be closed after use.
     * Consider using try-with-resources or ensuring proper cleanup.
     * </p>
     * 
     * @return a new EntityManager instance
     */
    public static EntityManager getEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    // ==================== Lifecycle Management ====================

    /**
     * Shuts down the Hibernate EntityManagerFactory.
     * <p>
     * This method should be called on application exit to properly release
     * database connections and other resources.
     * </p>
     */
    public static void shutdown() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            logger.info("🔒 Shutting down Hibernate...");
            entityManagerFactory.close();
            logger.info("✓ Hibernate shutdown complete");
        }
    }
}
