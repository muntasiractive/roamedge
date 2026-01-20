package com.roam.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA entity representing a reusable journal entry template.
 * 
 * <p>
 * Journal templates allow users to create pre-defined structures for journal
 * entries,
 * enabling consistent formatting and quick entry creation. Templates can
 * contain
 * placeholder text, formatting, and structured sections.
 * </p>
 * 
 * <h2>Features:</h2>
 * <ul>
 * <li>Customizable template name for easy identification</li>
 * <li>Rich text content support via TEXT column type</li>
 * <li>Automatic timestamp management for creation and updates</li>
 * <li>JPA lifecycle callbacks for timestamp automation</li>
 * </ul>
 * 
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 * @see jakarta.persistence.Entity
 */
@Entity
@Table(name = "journal_templates")
public class JournalTemplate {

    // ==================== Fields ====================

    /** Unique identifier for the journal template */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Display name of the template (max 255 characters) */
    @Column(nullable = false, length = 255)
    private String name;

    /** Template content with placeholder text and formatting */
    @Column(columnDefinition = "TEXT")
    private String content;

    /** Timestamp when the template was created (immutable after creation) */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Timestamp of the last modification to the template */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ==================== Constructors ====================

    /**
     * Default constructor required by JPA.
     */
    public JournalTemplate() {
    }

    /**
     * Creates a new journal template with the specified name and content.
     *
     * @param name    the display name of the template
     * @param content the template content with placeholder text
     */
    public JournalTemplate(String name, String content) {
        this.name = name;
        this.content = content;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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
        JournalTemplate that = (JournalTemplate) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "JournalTemplate{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
