package com.roam.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Represents a calendar source entity in the Roam application.
 * <p>
 * A calendar source serves as a container/category for calendar events,
 * allowing users to organize and filter events by source. Features include:
 * <ul>
 * <li>Named calendar categories with custom colors</li>
 * <li>Visibility toggle for showing/hiding events</li>
 * <li>Default calendar designation</li>
 * <li>Type classification (PERSONAL, WORK, REGION, etc.)</li>
 * <li>Automatic timestamp tracking</li>
 * </ul>
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 * @see CalendarEvent
 * @see CalendarSourceType
 */
@Entity
@Table(name = "calendar_sources")
public class CalendarSource {

    // ==================== Entity Fields ====================

    /** Unique identifier for the calendar source */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Display name of the calendar source (required, max 100 characters) */
    @Column(nullable = false, length = 100)
    private String name;

    /** Hex color code for visual identification (e.g., "#3F51B5") */
    @Column(nullable = false, length = 7)
    private String color;

    /** Type classification of the calendar source */
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private CalendarSourceType type;

    /** Flag indicating if events from this source are visible */
    @Column(name = "is_visible", nullable = false)
    private Boolean isVisible;

    /** Flag indicating if this is the default calendar for new events */
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault;

    /** Timestamp when the calendar source was created */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Timestamp when the calendar source was last updated */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ==================== Constructors ====================

    /**
     * Default constructor.
     * Initializes isVisible to true and isDefault to false.
     */
    public CalendarSource() {
        this.isVisible = true;
        this.isDefault = false;
    }

    /**
     * Constructs a new CalendarSource with essential properties.
     *
     * @param name  the display name of the calendar source
     * @param color the hex color code for visual identification
     * @param type  the type classification of the calendar source
     */
    public CalendarSource(String name, String color, CalendarSourceType type) {
        this();
        this.name = name;
        this.color = color;
        this.type = type;
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

    public CalendarSourceType getType() {
        return type;
    }

    public void setType(CalendarSourceType type) {
        this.type = type;
    }

    public Boolean getIsVisible() {
        return isVisible;
    }

    public void setIsVisible(Boolean isVisible) {
        this.isVisible = isVisible;
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
        CalendarSource that = (CalendarSource) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "CalendarSource{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", color='" + color + '\'' +
                '}';
    }
}
