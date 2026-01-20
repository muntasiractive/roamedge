package com.roam.service;

import com.roam.repository.WikiTemplateRepository;
import com.roam.repository.RegionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service responsible for initializing default application data.
 * <p>
 * This service is called during application startup to ensure that essential
 * default data exists in the database. It handles the creation of:
 * <ul>
 * <li>Default regions for geographical categorization</li>
 * <li>Default wiki templates (if enabled)</li>
 * </ul>
 * </p>
 * <p>
 * The initialization is idempotent - it checks for existing data before
 * creating defaults to prevent duplicate entries.
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 * @see DatabaseService
 * @see WikiTemplateRepository
 * @see RegionRepository
 */
public class DataInitializer {

        // ==================== Constants ====================

        private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

        // ==================== Fields ====================

        private final WikiTemplateRepository templateRepository;
        private final RegionRepository regionRepository;

        // ==================== Constructor ====================

        /**
         * Constructs a new DataInitializer instance.
         * <p>
         * Initializes the required repositories for template and region management.
         * </p>
         */
        public DataInitializer() {
                this.templateRepository = new WikiTemplateRepository();
                this.regionRepository = new RegionRepository();
        }

        // ==================== Initialization Methods ====================

        /**
         * Initializes default templates and regions.
         * <p>
         * This method first initializes default regions, then checks if templates
         * already exist before attempting to create defaults. The template creation
         * is currently disabled pending user configuration.
         * </p>
         */
        public void initializeDefaultTemplates() {
                // Initialize regions first
                initializeDefaultRegions();

                // Check if templates already exist
                if (!templateRepository.findAll().isEmpty()) {
                        logger.info("✓ Templates already initialized");
                        return;
                }

                // Default templates creation removed as per user request
                /*
                 * logger.info("📝 Creating default Wiki templates...");
                 * 
                 * // Template 1: Blank Wiki
                 * WikiTemplate blank = new WikiTemplate(
                 * "Blank Wiki",
                 * "Start with a blank canvas",
                 * "",
                 * "📄",
                 * true);
                 * templateRepository.save(blank);
                 * 
                 * // ... (other templates) ...
                 * 
                 * logger.info("✓ Default templates created successfully");
                 */
        }

        /**
         * Initializes the default geographical regions.
         * <p>
         * Delegates to the {@link RegionRepository} to create the standard set
         * of regions used for categorizing operations and other entities.
         * </p>
         */
        private void initializeDefaultRegions() {
                logger.info("🌍 Creating default regions...");
                regionRepository.createDefaultRegions();
                logger.info("✓ Default regions created successfully");
        }
}