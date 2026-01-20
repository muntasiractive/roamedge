package com.roam.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * JPA entity representing a reusable wiki page template.
 * 
 * <p>
 * Wiki templates provide pre-defined page structures with dynamic placeholder
 * substitution for creating consistent wiki documentation. Templates support
 * various placeholders that are automatically replaced with contextual values.
 * </p>
 * 
 * <h2>Features:</h2>
 * <ul>
 * <li>Dynamic placeholder substitution for date, time, title, and
 * operation</li>
 * <li>Icon support for visual template identification</li>
 * <li>Default template designation for quick wiki creation</li>
 * <li>Rich text content with TEXT column support</li>
 * <li>Automatic timestamp management via JPA lifecycle callbacks</li>
 * </ul>
 * 
 * <h2>Supported Placeholders:</h2>
 * <ul>
 * <li>{@code {date}} - Current date in yyyy-MM-dd format</li>
 * <li>{@code {time}} - Current time in HH:mm format</li>
 * <li>{@code {datetime}} - Current date and time in yyyy-MM-dd HH:mm
 * format</li>
 * <li>{@code {title}} - Wiki page title</li>
 * <li>{@code {operation}} - Associated operation name</li>
 * </ul>
 * 
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 * @see jakarta.persistence.Entity
 */
@Entity
@Table(name = "Wiki_templates")
public class WikiTemplate {

    // ==================== Fields ====================

    /** Unique identifier for the wiki template */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Display name of the template (max 100 characters) */
    @Column(nullable = false, length = 100)
    private String name;

    /** Optional description explaining the template's purpose */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** Template content with placeholders for dynamic substitution */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    /** Emoji or icon character for visual identification (max 10 characters) */
    @Column(length = 10)
    private String icon;

    /** Flag indicating if this is the default template for new wikis */
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    /** Timestamp when the template was created (immutable after creation) */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Timestamp of the last modification to the template */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ==================== Template Placeholder Constants ====================

    /** Placeholder for current date in yyyy-MM-dd format */
    public static final String PLACEHOLDER_DATE = "{date}";

    /** Placeholder for current time in HH:mm format */
    public static final String PLACEHOLDER_TIME = "{time}";

    /** Placeholder for current date and time in yyyy-MM-dd HH:mm format */
    public static final String PLACEHOLDER_DATETIME = "{datetime}";

    /** Placeholder for the wiki page title */
    public static final String PLACEHOLDER_TITLE = "{title}";

    /** Placeholder for the associated operation name */
    public static final String PLACEHOLDER_OPERATION = "{operation}";

    // ==================== Constructors ====================

    /**
     * Default constructor required by JPA.
     */
    public WikiTemplate() {
    }

    /**
     * Creates a new wiki template with all configurable properties.
     *
     * @param name        the display name of the template
     * @param description optional description of the template's purpose
     * @param content     the template content with placeholders
     * @param icon        emoji or icon character for visual identification
     * @param isDefault   whether this should be the default template
     */
    public WikiTemplate(String name, String description, String content, String icon, boolean isDefault) {
        this.name = name;
        this.description = description;
        this.content = content;
        this.icon = icon;
        this.isDefault = isDefault;
    }

    // ==================== Lifecycle Callbacks ====================

    /**
     * JPA lifecycle callback executed before entity insertion.
     * Automatically sets creation and update timestamps.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * JPA lifecycle callback executed before entity update.
     * Automatically updates the modification timestamp.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ==================== Template Processing ====================

    /**
     * Processes the template content by replacing all placeholders with actual
     * values.
     * 
     * <p>
     * Substitutes the following placeholders:
     * </p>
     * <ul>
     * <li>{@code {date}} - Current date</li>
     * <li>{@code {time}} - Current time</li>
     * <li>{@code {datetime}} - Current date and time</li>
     * <li>{@code {title}} - Provided wiki title</li>
     * <li>{@code {operation}} - Provided operation name</li>
     * </ul>
     *
     * @param title         the wiki page title to substitute
     * @param operationName the operation name to substitute
     * @return the processed template content with all placeholders replaced
     */
    public String processTemplate(String title, String operationName) {
        String processed = content;

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        processed = processed.replace(PLACEHOLDER_DATE, now.format(dateFormatter));
        processed = processed.replace(PLACEHOLDER_TIME, now.format(timeFormatter));
        processed = processed.replace(PLACEHOLDER_DATETIME, now.format(dateTimeFormatter));
        processed = processed.replace(PLACEHOLDER_TITLE, title != null ? title : "");
        processed = processed.replace(PLACEHOLDER_OPERATION, operationName != null ? operationName : "");

        return processed;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
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

    // ==================== Object Methods ====================

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        WikiTemplate that = (WikiTemplate) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "WikiTemplate{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", icon='" + icon + '\'' +
                '}';
    }
}
