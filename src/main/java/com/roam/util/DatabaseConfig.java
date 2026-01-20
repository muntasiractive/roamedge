package com.roam.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Database configuration manager that loads credentials from environment
 * variables or a properties file outside version control.
 * <p>
 * This singleton class manages database connection settings with a secure
 * priority-based configuration loading mechanism:
 * </p>
 * <ol>
 * <li><strong>Environment variables</strong> (ROAM_DB_USER, ROAM_DB_PASSWORD,
 * ROAM_DB_URL, ROAM_DB_DRIVER)</li>
 * <li><strong>User home config file</strong> (~/.roam/database.properties)</li>
 * <li><strong>Fallback defaults</strong> (for development only - not
 * recommended for production)</li>
 * </ol>
 * <p>
 * For production deployments, always use environment variables or a properly
 * secured properties file to store database credentials.
 * </p>
 * 
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 * @see #getInstance()
 * @see #createSampleConfigFile()
 */
public class DatabaseConfig {

    // ==================== Constants ====================

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    /** Configuration directory name in user home. */
    private static final String CONFIG_DIR = ".roam";
    /** Configuration file name. */
    private static final String CONFIG_FILE = "database.properties";

    // ==================== Instance Fields ====================

    /** The JDBC connection URL. */
    private final String jdbcUrl;
    /** The database username. */
    private final String username;
    /** The database password. */
    private final String password;
    /** The JDBC driver class name. */
    private final String driver;

    // ==================== Singleton Instance ====================

    /** The singleton instance of DatabaseConfig. */
    private static DatabaseConfig instance;

    // ==================== Constructor ====================

    /**
     * Private constructor that initializes database configuration.
     * Attempts to load configuration from environment variables first,
     * then from properties file, and finally falls back to defaults.
     */
    private DatabaseConfig() {
        // Try environment variables first (highest priority)
        String envUser = System.getenv("ROAM_DB_USER");
        String envPassword = System.getenv("ROAM_DB_PASSWORD");
        String envUrl = System.getenv("ROAM_DB_URL");
        String envDriver = System.getenv("ROAM_DB_DRIVER");

        if (envUser != null && envPassword != null) {
            this.username = envUser;
            this.password = envPassword;
            this.jdbcUrl = envUrl != null ? envUrl : getDefaultJdbcUrl();
            this.driver = envDriver != null ? envDriver : "org.h2.Driver";
            logger.info("✓ Database configuration loaded from environment variables");
        } else {
            // Try loading from properties file
            Properties props = loadPropertiesFile();
            if (props != null && props.containsKey("db.username")) {
                this.username = props.getProperty("db.username");
                this.password = props.getProperty("db.password");
                this.jdbcUrl = props.getProperty("db.url", getDefaultJdbcUrl());
                this.driver = props.getProperty("db.driver", "org.h2.Driver");
                logger.info("✓ Database configuration loaded from properties file");
            } else {
                // Fallback to defaults (development only - maintains backward compatibility)
                logger.warn("⚠️  WARNING: Using default database credentials!");
                logger.warn("⚠️  Set ROAM_DB_USER and ROAM_DB_PASSWORD environment variables for production");
                this.username = "MirFaiyazSir";
                // Use legacy password for backward compatibility with existing databases
                // Change this in production by setting environment variables
                this.password = "MarkKaitenNaSir";
                this.jdbcUrl = getDefaultJdbcUrl();
                this.driver = "org.h2.Driver";
            }
        }
    }

    // ==================== Public API ====================

    /**
     * Returns the singleton instance of DatabaseConfig.
     * Thread-safe lazy initialization.
     * 
     * @return the singleton DatabaseConfig instance
     */
    public static synchronized DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }

    // ==================== Private Helper Methods ====================

    /**
     * Loads database properties from the user's config file.
     * 
     * @return Properties object if file exists and is readable, null otherwise
     */
    private Properties loadPropertiesFile() {
        Path configPath = getConfigPath();
        if (!Files.exists(configPath)) {
            return null;
        }

        Properties props = new Properties();
        try (InputStream input = Files.newInputStream(configPath)) {
            props.load(input);
            return props;
        } catch (IOException e) {
            logger.error("Failed to load database properties: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Gets the path to the configuration file in user's home directory.
     * 
     * @return the Path to ~/.roam/database.properties
     */
    private Path getConfigPath() {
        String userHome = System.getProperty("user.home");
        return Paths.get(userHome, CONFIG_DIR, CONFIG_FILE);
    }

    /**
     * Generates the default JDBC URL for the H2 database.
     * The database file is stored in the user's home directory.
     * 
     * @return the default JDBC URL string
     */
    private String getDefaultJdbcUrl() {
        String userHome = System.getProperty("user.home");
        return String.format(
                "jdbc:h2:file:%s/roam/roamdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
                userHome.replace("\\", "/"));
    }

    /**
     * Generates a secure random password for development use.
     * 
     * @return a 16-character random password string
     */
    private String generateRandomPassword() {
        // Generate a secure random password for development
        return java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    // ==================== Getters ====================

    /**
     * Gets the JDBC connection URL.
     * 
     * @return the JDBC URL string
     */
    public String getJdbcUrl() {
        return jdbcUrl;
    }

    /**
     * Gets the database username.
     * 
     * @return the database username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets the database password.
     * 
     * @return the database password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Gets the JDBC driver class name.
     * 
     * @return the driver class name
     */
    public String getDriver() {
        return driver;
    }

    // ==================== Static Utility Methods ====================

    /**
     * Creates a sample configuration file in user's home directory.
     * <p>
     * The file is created at ~/.roam/database.properties with template
     * values that should be customized for the user's environment.
     * If the file already exists, this method does nothing.
     * </p>
     */
    public static void createSampleConfigFile() {
        try {
            String userHome = System.getProperty("user.home");
            Path configDir = Paths.get(userHome, CONFIG_DIR);
            Path configFile = configDir.resolve(CONFIG_FILE);

            if (Files.exists(configFile)) {
                logger.info("Configuration file already exists: {}", configFile);
                return;
            }

            Files.createDirectories(configDir);

            String sampleConfig = "# Roam Database Configuration\n" +
                    "# DO NOT commit this file to version control!\n" +
                    "\n" +
                    "# Database connection settings\n" +
                    "db.driver=org.h2.Driver\n" +
                    "db.url=jdbc:h2:file:~/roam/roamdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE\n" +
                    "db.username=MirFaiyazSir\n" +
                    "db.password=MarkKaitenNaSir\n" +
                    "\n" +
                    "# For PostgreSQL (production):\n" +
                    "# db.driver=org.postgresql.Driver\n" +
                    "# db.url=jdbc:postgresql://localhost:5432/roamdb\n" +
                    "# db.username=roam_user\n" +
                    "# db.password=your_secure_password\n";

            Files.write(configFile, sampleConfig.getBytes());
            logger.info("✓ Sample configuration file created: {}", configFile);
            logger.warn("⚠️  Please update the password in this file!");

        } catch (IOException e) {
            logger.error("Failed to create configuration file: {}", e.getMessage());
        }
    }
}
