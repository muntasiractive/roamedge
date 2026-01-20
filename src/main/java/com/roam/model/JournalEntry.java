package com.roam.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a journal entry entity in the Roam application.
 * <p>
 * This entity models daily journal entries for personal reflection,
 * note-taking, and documentation. Features include:
 * <ul>
 * <li>Date-based organization of entries</li>
 * <li>Rich text content support</li>
 * <li>Title and content fields for structured journaling</li>
 * <li>Automatic timestamp tracking for creation and updates</li>
 * </ul>
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "journal_entries")
public class JournalEntry {

    // ==================== Entity Fields ====================

    /** Unique identifier for the journal entry */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Title of the journal entry (required, max 255 characters) */
    @Column(nullable = false, length = 255)
    private String title;

    /** Main content/body of the journal entry */
    @Column(columnDefinition = "TEXT")
    private String content;

    /** Date associated with the journal entry */
    @Column(nullable = false)
    private LocalDate date;

    /** Timestamp when the entry was created */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Timestamp when the entry was last updated */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ==================== Constructors ====================

    /**
     * Default constructor.
     */
    public JournalEntry() {
    }

    /**
     * Constructs a new JournalEntry with essential properties.
     *
     * @param title the title of the journal entry
     * @param date  the date associated with the entry
     */
    public JournalEntry(String title, LocalDate date) {
        this.title = title;
        this.date = date;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
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
        JournalEntry that = (JournalEntry) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "JournalEntry{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", date=" + date +
                '}';
    }
}
