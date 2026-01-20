package com.roam.model;

import com.roam.validation.SafeTitle;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Wiki entity representing a knowledge article or note.
 * 
 * <p>
 * Wikis are the documentation and knowledge management units in Roam.
 * They can be standalone or linked to Operations, Tasks, and Calendar events.
 * Content is stored as Markdown-compatible text.
 * </p>
 * 
 * <h2>Key Features:</h2>
 * <ul>
 * <li>Rich text content with Markdown support</li>
 * <li>Favorites system for quick access</li>
 * <li>Template-based creation</li>
 * <li>Automatic word count tracking</li>
 * <li>Wiki-to-wiki linking capability</li>
 * <li>Banner images and background customization</li>
 * </ul>
 * 
 * <h2>Cross-Linking:</h2>
 * <p>
 * Wikis can be associated with Tasks and Calendar events to provide
 * contextual documentation for actionable items.
 * </p>
 * 
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 * @see WikiTemplate
 * @see WikiFileAttachment
 */
@Entity
@Table(name = "wikis")
public class Wiki {

    /** Unique identifier, auto-generated. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Optional reference to parent Operation. */
    @Column(name = "operation_id")
    private Long operationId;

    /** Wiki title. Required, validated for safe content. */
    @NotBlank(message = "Wiki title cannot be blank")
    @SafeTitle(max = 255)
    @Column(nullable = false, length = 255)
    private String title;

    /** Main content body. Supports Markdown, up to 100K characters. */
    @Size(max = 100000, message = "Content exceeds maximum length of 100,000 characters")
    @Column(columnDefinition = "TEXT")
    private String content;

    /** Creation timestamp. Immutable after creation. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Last modification timestamp. */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** Flag for favorites/starred status. */
    @Column(name = "is_favorite", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isFavorite = false;

    /** Reference to the template used for creation. */
    @Column(name = "template_id")
    private Long templateId;

    /** Automatically calculated word count for statistics. */
    @Column(name = "word_count", columnDefinition = "INTEGER DEFAULT 0")
    private Integer wordCount = 0;

    /** Comma-separated list of linked wiki IDs for cross-referencing. */
    @Column(name = "linked_wiki_ids", columnDefinition = "TEXT")
    private String linkedWikiIds;

    /** Life region category. */
    @Column(length = 50)
    private String region;

    /** Optional link to an associated task. */
    @Column(name = "task_id")
    private Long taskId;

    /** Optional link to an associated calendar event. */
    @Column(name = "calendar_event_id")
    private Long calendarEventId;

    /** URL for header banner image. */
    @Column(name = "banner_url", length = 512)
    private String bannerUrl;

    /** Custom background color (hex format). */
    @Column(name = "background_color", length = 20)
    private String backgroundColor;

    // ==================== Constructors ====================

    /**
     * Default constructor with sensible defaults.
     */
    public Wiki() {
        this.title = "Untitled Wiki";
        this.content = "";
        this.isFavorite = false;
        this.wordCount = 0;
    }

    /**
     * Creates a wiki with title and optional operation link.
     * 
     * @param title       the wiki title
     * @param operationId optional parent operation ID
     */
    public Wiki(String title, Long operationId) {
        this.title = title;
        this.operationId = operationId;
        this.content = "";
        this.isFavorite = false;
        this.wordCount = 0;
    }

    // ==================== JPA Lifecycle Callbacks ====================

    /**
     * Sets timestamps and calculates word count before initial persist.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        calculateWordCount();
    }

    /**
     * Updates timestamp and recalculates word count before update.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calculateWordCount();
    }

    // ==================== Business Logic ====================

    /**
     * Calculates and updates the word count based on current content.
     * 
     * @return the calculated word count
     */
    public Integer calculateWordCount() {
        if (content == null || content.trim().isEmpty()) {
            this.wordCount = 0;
            return 0;
        }

        String[] words = content.trim().split("\\s+");
        this.wordCount = words.length;
        return this.wordCount;
    }

    // ==================== Getters and Setters ====================
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOperationId() {
        return operationId;
    }

    public void setOperationId(Long operationId) {
        this.operationId = operationId;
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

    public Boolean getIsFavorite() {
        return isFavorite;
    }

    public void setIsFavorite(Boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public Integer getWordCount() {
        return wordCount;
    }

    public void setWordCount(Integer wordCount) {
        this.wordCount = wordCount;
    }

    public String getLinkedWikiIds() {
        return linkedWikiIds;
    }

    public void setLinkedWikiIds(String linkedWikiIds) {
        this.linkedWikiIds = linkedWikiIds;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getCalendarEventId() {
        return calendarEventId;
    }

    public void setCalendarEventId(Long calendarEventId) {
        this.calendarEventId = calendarEventId;
    }

    public String getBannerUrl() {
        return bannerUrl;
    }

    public void setBannerUrl(String bannerUrl) {
        this.bannerUrl = bannerUrl;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Wiki wiki = (Wiki) o;
        return id != null && id.equals(wiki.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Wiki{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", operationId=" + operationId +
                '}';
    }
}
