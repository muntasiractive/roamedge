package com.roam.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * JPA entity representing a file attachment associated with a wiki page.
 * 
 * <p>
 * Wiki file attachments allow users to attach files (images, documents, etc.)
 * to wiki pages. Each attachment tracks file metadata including name, path,
 * size,
 * type, and an optional description.
 * </p>
 * 
 * <h2>Features:</h2>
 * <ul>
 * <li>Association with parent wiki via foreign key reference</li>
 * <li>Complete file metadata storage (name, path, size, type)</li>
 * <li>Bean Validation annotations for data integrity</li>
 * <li>Optional description for attachment context</li>
 * <li>Automatic creation timestamp via JPA lifecycle callback</li>
 * </ul>
 * 
 * <h2>Validation Rules:</h2>
 * <ul>
 * <li>Wiki ID is required and cannot be null</li>
 * <li>File name is required, max 255 characters</li>
 * <li>File path is required, max 512 characters</li>
 * <li>File type is optional, max 100 characters</li>
 * <li>Description is optional, max 500 characters</li>
 * </ul>
 * 
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 * @see Wiki
 * @see jakarta.persistence.Entity
 */
@Entity
@Table(name = "wiki_file_attachments")
public class WikiFileAttachment {

    // ==================== Fields ====================

    /** Unique identifier for the file attachment */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Foreign key reference to the parent wiki page */
    @NotNull(message = "Wiki ID cannot be null")
    @Column(name = "wiki_id", nullable = false)
    private Long wikiId;

    /** Original file name as uploaded by the user */
    @NotBlank(message = "File name cannot be blank")
    @Size(max = 255, message = "File name exceeds maximum length")
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    /** Absolute or relative path to the stored file */
    @NotBlank(message = "File path cannot be blank")
    @Size(max = 512, message = "File path exceeds maximum length")
    @Column(name = "file_path", nullable = false, length = 512)
    private String filePath;

    /** Size of the file in bytes */
    @Column(name = "file_size")
    private Long fileSize;

    /** MIME type or file extension (e.g., "image/png", "application/pdf") */
    @Size(max = 100, message = "File type exceeds maximum length")
    @Column(name = "file_type", length = 100)
    private String fileType;

    /** Timestamp when the attachment was created (immutable after creation) */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Optional user-provided description of the attachment */
    @Column(name = "description", length = 500)
    private String description;

    // ==================== Constructors ====================

    /**
     * Default constructor required by JPA.
     */
    public WikiFileAttachment() {
    }

    /**
     * Creates a new wiki file attachment with required fields.
     *
     * @param wikiId   the ID of the parent wiki page
     * @param fileName the original name of the uploaded file
     * @param filePath the storage path for the file
     */
    public WikiFileAttachment(Long wikiId, String fileName, String filePath) {
        this.wikiId = wikiId;
        this.fileName = fileName;
        this.filePath = filePath;
    }

    // ==================== Lifecycle Callbacks ====================

    /**
     * JPA lifecycle callback executed before entity insertion.
     * Automatically sets the creation timestamp.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // ==================== Getters and Setters ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWikiId() {
        return wikiId;
    }

    public void setWikiId(Long wikiId) {
        this.wikiId = wikiId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // ==================== Object Methods ====================

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        WikiFileAttachment that = (WikiFileAttachment) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "WikiFileAttachment{" +
                "id=" + id +
                ", fileName='" + fileName + '\'' +
                ", fileSize=" + fileSize +
                '}';
    }
}
