package com.roam.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.roam.model.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Service for managing application settings persistence.
 * <p>
 * This singleton service handles loading, saving, and accessing application
 * settings
 * stored in a JSON file. It uses Jackson ObjectMapper for JSON
 * serialization/deserialization
 * with support for Java 8+ date/time types.
 * </p>
 * 
 * <h2>Features:</h2>
 * <ul>
 * <li><b>JSON Persistence:</b> Settings are stored in a human-readable JSON
 * format</li>
 * <li><b>Auto-initialization:</b> Creates default settings if no configuration
 * file exists</li>
 * <li><b>Java Time Support:</b> Full support for Java 8+ date/time API via
 * JavaTimeModule</li>
 * <li><b>Pretty Printing:</b> JSON output is indented for readability</li>
 * </ul>
 * 
 * <h2>Usage Example:</h2>
 * 
 * <pre>{@code
 * SettingsService service = SettingsService.getInstance();
 * Settings settings = service.getSettings();
 * settings.setTheme("dark");
 * service.saveSettings();
 * }</pre>
 * 
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 * @see Settings
 */
public class SettingsService {

    // ==================== Constants ====================

    private static final Logger logger = LoggerFactory.getLogger(SettingsService.class);

    /** The filename for the settings JSON file. */
    private static final String SETTINGS_FILE = "settings.json";

    // ==================== Singleton Instance ====================

    private static SettingsService instance;

    // ==================== Instance Fields ====================

    /** The current application settings. */
    private Settings settings;

    /** Jackson ObjectMapper configured for JSON serialization. */
    private final ObjectMapper mapper;

    // ==================== Constructor ====================

    /**
     * Private constructor to enforce singleton pattern.
     * <p>
     * Initializes the ObjectMapper with JavaTimeModule support and
     * loads settings from the configuration file.
     * </p>
     */
    private SettingsService() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        loadSettings();
    }

    // ==================== Singleton Access ====================

    /**
     * Returns the singleton instance of the SettingsService.
     * <p>
     * This method is thread-safe and uses lazy initialization.
     * </p>
     *
     * @return the singleton SettingsService instance
     */
    public static synchronized SettingsService getInstance() {
        if (instance == null) {
            instance = new SettingsService();
        }
        return instance;
    }

    // ==================== Settings Access ====================

    /**
     * Gets the current application settings.
     *
     * @return the current {@link Settings} object
     */
    public Settings getSettings() {
        return settings;
    }

    // ==================== Persistence Operations ====================

    /**
     * Saves the current settings to the configuration file.
     * <p>
     * The settings are serialized to JSON format and written to the settings file.
     * Any errors during saving are logged but do not throw exceptions.
     * </p>
     */
    public void saveSettings() {
        try {
            mapper.writeValue(new File(SETTINGS_FILE), settings);
        } catch (IOException e) {
            logger.error("Failed to save settings: {}", e.getMessage());
        }
    }

    /**
     * Loads settings from the configuration file.
     * <p>
     * If the file exists, settings are deserialized from JSON.
     * If the file doesn't exist or cannot be read, default settings are created
     * and saved to disk.
     * </p>
     */
    private void loadSettings() {
        File file = new File(SETTINGS_FILE);
        if (file.exists()) {
            try {
                settings = mapper.readValue(file, Settings.class);
            } catch (IOException e) {
                logger.error("Failed to load settings, using defaults: {}", e.getMessage());
                settings = new Settings();
            }
        } else {
            settings = new Settings();
            saveSettings();
        }
    }
}
