package com.roam.util;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;

/**
 * FlywayManager handles database schema migrations using Flyway.
 * <p>
 * This replaces Hibernate's hbm2ddl.auto=update with a production-ready
 * versioned migration system.
 * </p>
 * 
 * <h2>Features:</h2>
 * <ul>
 * <li>Versioned SQL migrations stored in src/main/resources/db/migration</li>
 * <li>Automatic migration execution on application startup</li>
 * <li>Migration history tracking in flyway_schema_history table</li>
 * <li>Rollback support (manual via SQL scripts)</li>
 * <li>Schema validation and repair capabilities</li>
 * </ul>
 * 
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public class FlywayManager {

    // ==================== Constants ====================

    private static final Logger logger = LoggerFactory.getLogger(FlywayManager.class);

    // ==================== Fields ====================

    private static Flyway flyway;

    // ==================== Migration Operations ====================

    /**
     * Initializes and runs Flyway migrations for the application database.
     * This method should be called before initializing the Hibernate
     * EntityManagerFactory
     * to ensure the schema is up-to-date.
     * 
     * @param jdbcUrl  the JDBC URL for the database
     * @param username the database username
     * @param password the database password
     * @return true if migrations were successful, false otherwise
     */
    public static boolean runMigrations(String jdbcUrl, String username, String password) {
        try {
            logger.info("🔄 Initializing Flyway database migrations...");

            // Create H2 DataSource
            JdbcDataSource dataSource = new JdbcDataSource();
            dataSource.setURL(jdbcUrl);
            dataSource.setUser(username);
            dataSource.setPassword(password);

            // Configure Flyway
            flyway = Flyway.configure()
                    .dataSource(dataSource)
                    .locations("classpath:db/migration")
                    .baselineOnMigrate(true) // Baseline existing databases
                    .baselineVersion("0") // Start versioning from 0
                    .validateOnMigrate(false) // Disable strict validation for existing DBs
                    .cleanDisabled(true) // Disable clean command for safety
                    .outOfOrder(true) // Allow out-of-order migrations
                    .load();

            // Get current migration status
            MigrationInfoService info = flyway.info();
            MigrationInfo current = info.current();

            if (current == null) {
                logger.info("📊 No migrations applied yet. Running initial migration...");
            } else {
                logger.info("📊 Current database version: {}", current.getVersion());
            }

            // Attempt repair if there are failed migrations
            try {
                flyway.validate();
            } catch (Exception e) {
                logger.warn("⚠️ Validation failed. Attempting to repair schema history...");
                flyway.repair();
                logger.info("✅ Schema history repaired. Retrying migration...");
            }

            // Run migrations
            int migrationsExecuted = flyway.migrate().migrationsExecuted;
            if (migrationsExecuted == 0) {
                logger.info("✅ Database schema is up-to-date (no new migrations)");
            } else {
                logger.info("✅ Successfully applied {} migration(s)", migrationsExecuted);

                // Log all applied migrations
                info = flyway.info();
                for (MigrationInfo migration : info.all()) {
                    if (migration.getState().isApplied()) {
                        logger.debug("  ✓ {} - {} (installed on: {})",
                                migration.getVersion(),
                                migration.getDescription(),
                                migration.getInstalledOn());
                    }
                }
            }

            // Display final schema version
            current = flyway.info().current();
            if (current != null) {
                logger.info("📌 Final database version: {}", current.getVersion());
            }

            return true;

        } catch (Exception e) {
            logger.error("❌ Flyway migration failed: {}", e.getMessage(), e);
            logger.error("   Database schema may be inconsistent. Please check migration scripts.");
            return false;
        }
    }

    // ==================== Validation & Repair ====================

    /**
     * Validates that all migrations are applied correctly and the database schema
     * matches the expected state.
     * 
     * @return true if validation passes, false otherwise
     */
    public static boolean validate() {
        try {
            if (flyway == null) {
                logger.warn("⚠️ Flyway not initialized. Cannot validate.");
                return false;
            }

            flyway.validate();
            logger.info("✅ Database schema validation passed");
            return true;

        } catch (Exception e) {
            logger.error("❌ Database schema validation failed: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Repairs the Flyway schema history table by removing failed migration entries
     * and realigning checksums. Use with caution.
     * 
     * @return true if repair was successful, false otherwise
     */
    public static boolean repair() {
        try {
            if (flyway == null) {
                logger.warn("⚠️ Flyway not initialized. Cannot repair.");
                return false;
            }

            logger.warn("⚠️ Repairing Flyway schema history...");
            flyway.repair();
            logger.info("✅ Flyway schema history repaired");
            return true;

        } catch (Exception e) {
            logger.error("❌ Flyway repair failed: {}", e.getMessage(), e);
            return false;
        }
    }

    // ==================== Information ====================

    /**
     * Gets the current migration information including version and status.
     * 
     * @return String description of current migration status
     */
    public static String getInfo() {
        if (flyway == null) {
            return "Flyway not initialized";
        }

        try {
            MigrationInfoService info = flyway.info();
            MigrationInfo current = info.current();

            if (current == null) {
                return "No migrations applied";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("Current Version: ").append(current.getVersion()).append("\n");
            sb.append("Description: ").append(current.getDescription()).append("\n");
            sb.append("Installed On: ").append(current.getInstalledOn()).append("\n");
            sb.append("State: ").append(current.getState()).append("\n");

            int pending = info.pending().length;
            if (pending > 0) {
                sb.append("Pending Migrations: ").append(pending);
            }

            return sb.toString();

        } catch (Exception e) {
            return "Error retrieving migration info: " + e.getMessage();
        }
    }
}
