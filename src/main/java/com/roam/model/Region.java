package com.roam.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Represents a region/life area category entity in the Roam application.
 * <p>
 * Regions are used to categorize and organize various entities (operations,
 * tasks, events, wikis) into different life areas. Features include:
 * <ul>
 * <li>Predefined default regions covering major life areas</li>
 * <li>Custom color coding for visual distinction</li>
 * <li>Unique naming constraint for region identification</li>
 * <li>Default region flagging for system-created regions</li>
 * <li>Automatic timestamp tracking</li>
 * </ul>
 * </p>
 * <p>
 * Default regions include: Lifestyle, Knowledge, Skill, Spirituality,
 * Career, Finance, Social, Academic, and Relationship.
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "regions")
public class Region {

    // ==================== Entity Fields ====================

    /** Unique identifier for the region */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique name of the region (required, max 50 characters) */
    @Column(unique = true, nullable = false, length = 50)
    private String name;

    /** Hex color code for visual identification (e.g., "#FF5722") */
    @Column(nullable = false, length = 7)
    private String color;

    /** Flag indicating if this is a system-created default region */
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault;

    /** Timestamp when the region was created */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Timestamp when the region was last updated */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ==================== Default Constants ====================

    /**
     * Array of default region names covering major life areas.
     * These regions are automatically created during application initialization.
     */
    public static final String[] DEFAULT_REGIONS = {
            "Lifestyle", "Knowledge", "Skill", "Spirituality",
            "Career", "Finance", "Social", "Academic", "Relationship"
    };

    /**
     * Array of default colors corresponding to DEFAULT_REGIONS.
     * Each color is a hex code matched by index to the region names.
     */
    public static final String[] DEFAULT_COLORS = {
            "#FF5722", // Lifestyle - Orange/Red
            "#9C27B0", // Knowledge - Purple
            "#795548", // Skill - Brown
            "#607D8B", // Spirituality - Blue Grey
            "#3F51B5", // Career - Indigo
            "#8BC34A", // Finance - Light Green
            "#FF9800", // Social - Orange
            "#00BCD4", // Academic - Cyan
            "#E91E63" // Relationship - Pink
    };

    // ==================== Constructors ====================

    /**
     * Default constructor.
     * Initializes isDefault to false.
     */
    public Region() {
        this.isDefault = false;
    }

    /**
     * Constructs a new Region with all essential properties.
     *
     * @param name      the unique name of the region
     * @param color     the hex color code for visual identification
     * @param isDefault whether this is a system-created default region
     */
    public Region(String name, String color, boolean isDefault) {
        this.name = name;
        this.color = color;
        this.isDefault = isDefault;
    }

    // ==================== Lifecycle Callbacks ====================

    /**
     * JPA lifecycle callback executed before entity persistence.
     * Sets creation and update timestamps.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * JPA lifecycle callback executed before entity update.
     * Updates the modification timestamp.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ==================== Getters and Setters ====================
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Region region = (Region) o;
        return id != null && id.equals(region.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Region{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", color='" + color + '\'' +
                ", isDefault=" + isDefault +
                '}';
    }
}
