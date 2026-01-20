package com.roam.model;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * Configuration model representing user preferences and application settings.
 * 
 * <p>
 * This class encapsulates all user-configurable settings for the Roam
 * application,
 * including security, display preferences, localization, and region management.
 * Settings are typically persisted to a JSON file for cross-session
 * persistence.
 * </p>
 * 
 * <h2>Features:</h2>
 * <ul>
 * <li>PIN-based application lock with hash storage for security</li>
 * <li>Theme selection (light/dark mode support)</li>
 * <li>User profile with name and avatar image</li>
 * <li>Customizable life regions for task/operation categorization</li>
 * <li>Timezone and calendar preferences (first day of week)</li>
 * <li>Automatic timezone detection option</li>
 * </ul>
 * 
 * <h2>Default Regions:</h2>
 * <ul>
 * <li>Lifestyle, Knowledge, Skill, Spirituality, Career</li>
 * <li>Finance, Social, Academic, Relationship</li>
 * </ul>
 * 
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public class Settings {

    // ==================== Security Settings ====================

    /** Flag indicating whether the application lock screen is enabled */
    private boolean isLockEnabled = false;

    /** Hashed PIN code for application unlock (empty string if no PIN set) */
    private String pinHash = "";

    // ==================== Display Settings ====================

    /** Current theme ("light" or "dark") */
    private String theme = "light";

    // ==================== User Profile Settings ====================

    /** User's display name shown in the application */
    private String userName = "";

    /** Path to the user's profile image file */
    private String profileImagePath = "";

    // ==================== Region Settings ====================

    /** List of life regions for categorizing operations and tasks */
    private List<String> regions = new ArrayList<>();

    // ==================== Localization Settings ====================

    /** First day of the week for calendar display ("Sunday" or "Monday") */
    private String firstDayOfWeek = "Sunday";

    /** User's preferred timezone ID (e.g., "America/New_York") */
    private String timezone = TimeZone.getDefault().getID();

    /** Flag to automatically detect and use system timezone */
    private boolean autoDetectTimezone = true;

    // ==================== Constructors ====================

    /**
     * Creates a new Settings instance with default values.
     * Initializes the default set of life regions.
     */
    public Settings() {
        // Default regions - matching database regions
        regions.add("Lifestyle");
        regions.add("Knowledge");
        regions.add("Skill");
        regions.add("Spirituality");
        regions.add("Career");
        regions.add("Finance");
        regions.add("Social");
        regions.add("Academic");
        regions.add("Relationship");
    }

    // ==================== Getters and Setters ====================

    public boolean isLockEnabled() {
        return isLockEnabled;
    }

    public void setLockEnabled(boolean lockEnabled) {
        isLockEnabled = lockEnabled;
    }

    public String getPinHash() {
        return pinHash;
    }

    public void setPinHash(String pinHash) {
        this.pinHash = pinHash;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public List<String> getRegions() {
        return new ArrayList<>(regions); // Return defensive copy
    }

    public void setRegions(List<String> regions) {
        this.regions = regions != null ? new ArrayList<>(regions) : new ArrayList<>();
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFirstDayOfWeek() {
        return firstDayOfWeek;
    }

    public void setFirstDayOfWeek(String firstDayOfWeek) {
        this.firstDayOfWeek = firstDayOfWeek;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public boolean isAutoDetectTimezone() {
        return autoDetectTimezone;
    }

    public void setAutoDetectTimezone(boolean autoDetectTimezone) {
        this.autoDetectTimezone = autoDetectTimezone;
    }

    public String getProfileImagePath() {
        return profileImagePath;
    }

    public void setProfileImagePath(String profileImagePath) {
        this.profileImagePath = profileImagePath;
    }
}
