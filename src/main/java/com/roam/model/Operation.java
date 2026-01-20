package com.roam.model;

import com.roam.validation.SafeTitle;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Operation entity representing a high-level project or initiative.
 * 
 * <p>
 * Operations are the top-level organizational units in Roam. They serve as
 * containers for related Tasks, Wiki pages, and Calendar events. Each operation
 * represents a significant endeavor with a defined purpose and measurable
 * outcome.
 * </p>
 * 
 * <h2>Key Features:</h2>
 * <ul>
 * <li>Status tracking (ONGOING, COMPLETED, ARCHIVED)</li>
 * <li>Priority levels for importance ranking</li>
 * <li>Region-based life area categorization</li>
 * <li>Purpose and outcome documentation</li>
 * <li>Due date tracking for deadlines</li>
 * </ul>
 * 
 * <h2>Validation:</h2>
 * <p>
 * Uses Jakarta Bean Validation with custom {@link SafeTitle} annotation
 * for security-conscious input handling.
 * </p>
 * 
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 * @see Task
 * @see Wiki
 * @see OperationStatus
 */
@Entity
@Table(name = "operations")
public class Operation {

    /** Unique identifier for the operation, auto-generated. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Operation name. Required, validated for safe content. */
    @NotBlank(message = "Operation name cannot be blank")
    @SafeTitle(max = 255)
    @Column(nullable = false, length = 255)
    private String name;

    /**
     * Detailed description of the operation's purpose. Supports up to 100K
     * characters.
     */
    @Size(max = 100000, message = "Purpose exceeds maximum length of 100,000 characters")
    @Column(columnDefinition = "TEXT")
    private String purpose;

    /** Target completion date for the operation. */
    @Column(name = "due_date")
    private LocalDate dueDate;

    /** Current lifecycle status of the operation. */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private OperationStatus status;

    /** Documentation of results achieved upon completion. */
    @Column(columnDefinition = "TEXT")
    private String outcome;

    /** Priority level for sorting and filtering. */
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Priority priority;

    /** Timestamp of initial creation. Immutable. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Timestamp of last modification. */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** Life region category (Career, Finance, Health, etc.). */
    @Column(length = 50)
    private String region;

    // ==================== Constructors ====================

    /**
     * Default constructor required by JPA.
     */
    public Operation() {
    }

    /**
     * Creates an operation with the specified name.
     * Initializes with ONGOING status and MEDIUM priority.
     * 
     * @param name the operation name
     */
    public Operation(String name) {
        this.name = name;
        this.status = OperationStatus.ONGOING;
        this.priority = Priority.MEDIUM;
    }

    // ==================== JPA Lifecycle Callbacks ====================

    /**
     * Sets creation and update timestamps before initial persist.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * Updates the modification timestamp before update.
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

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public OperationStatus getStatus() {
        return status;
    }

    public void setStatus(OperationStatus status) {
        this.status = status;
    }

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
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

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Operation operation = (Operation) o;
        return id != null && id.equals(operation.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Operation{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", priority=" + priority +
                ", dueDate=" + dueDate +
                ", region='" + region + '\'' +
                '}';
    }
}
