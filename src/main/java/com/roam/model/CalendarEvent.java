package com.roam.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Represents a calendar event entity in the Roam application.
 * <p>
 * This entity models calendar events with support for:
 * <ul>
 * <li>Basic event properties (title, description, location, time range)</li>
 * <li>All-day event support</li>
 * <li>Recurrence patterns with iCalendar RRULE format</li>
 * <li>Recurring instance tracking with parent event relationships</li>
 * <li>Integration with operations, tasks, and wiki pages</li>
 * <li>Calendar source and region categorization</li>
 * <li>Custom color coding</li>
 * </ul>
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 * @see CalendarSource
 * @see Region
 */
@Entity
@Table(name = "calendar_events")
public class CalendarEvent {

    // ==================== Entity Fields ====================

    /** Unique identifier for the calendar event */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Reference to the calendar source this event belongs to */
    @Column(name = "calendar_source_id", nullable = false)
    private Long calendarSourceId;

    /** Optional reference to an associated operation */
    @Column(name = "operation_id")
    private Long operationId;

    /** Optional reference to an associated task */
    @Column(name = "task_id")
    private Long taskId;

    /** The title/name of the event (required, max 255 characters) */
    @Column(nullable = false, length = 255)
    private String title;

    /** Detailed description of the event */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** Physical or virtual location of the event */
    @Column(length = 255)
    private String location;

    /** Start date and time of the event */
    @Column(name = "start_date_time", nullable = false)
    private LocalDateTime startDateTime;

    /** End date and time of the event */
    @Column(name = "end_date_time", nullable = false)
    private LocalDateTime endDateTime;

    /** Flag indicating if this is an all-day event */
    @Column(name = "is_all_day", nullable = false)
    private Boolean isAllDay;

    /** Hex color code for event display (e.g., "#FF5722") */
    @Column(length = 7)
    private String color;

    /** iCalendar RRULE format recurrence pattern */
    @Column(name = "recurrence_rule", columnDefinition = "TEXT")
    private String recurrenceRule;

    /** End date for the recurrence pattern */
    @Column(name = "recurrence_end_date")
    private LocalDateTime recurrenceEndDate;

    /** Reference to the parent event for recurring instances */
    @Column(name = "parent_event_id")
    private Long parentEventId;

    /** Flag indicating if this event is a generated recurring instance */
    @Column(name = "is_recurring_instance", nullable = false)
    private Boolean isRecurringInstance;

    /** Original start time for modified recurring instances */
    @Column(name = "original_start_date_time")
    private LocalDateTime originalStartDateTime;

    /** Timestamp when the event was created */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Timestamp when the event was last updated */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** Region/category for the event */
    @Column(length = 50)
    private String region;

    /** Optional reference to an associated wiki page */
    @Column(name = "wiki_id")
    private Long wikiId;

    // ==================== Constructors ====================

    /**
     * Default constructor.
     * Initializes isAllDay and isRecurringInstance to false.
     */
    public CalendarEvent() {
        this.isAllDay = false;
        this.isRecurringInstance = false;
    }

    /**
     * Constructs a new CalendarEvent with essential properties.
     *
     * @param title            the event title
     * @param startDateTime    the event start date and time
     * @param endDateTime      the event end date and time
     * @param calendarSourceId the ID of the calendar source
     */
    public CalendarEvent(String title, LocalDateTime startDateTime, LocalDateTime endDateTime, Long calendarSourceId) {
        this();
        this.title = title;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.calendarSourceId = calendarSourceId;
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

    public Long getCalendarSourceId() {
        return calendarSourceId;
    }

    public void setCalendarSourceId(Long calendarSourceId) {
        this.calendarSourceId = calendarSourceId;
    }

    public Long getOperationId() {
        return operationId;
    }

    public void setOperationId(Long operationId) {
        this.operationId = operationId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public Boolean getIsAllDay() {
        return isAllDay;
    }

    public void setIsAllDay(Boolean isAllDay) {
        this.isAllDay = isAllDay;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getRecurrenceRule() {
        return recurrenceRule;
    }

    public void setRecurrenceRule(String recurrenceRule) {
        this.recurrenceRule = recurrenceRule;
    }

    public LocalDateTime getRecurrenceEndDate() {
        return recurrenceEndDate;
    }

    public void setRecurrenceEndDate(LocalDateTime recurrenceEndDate) {
        this.recurrenceEndDate = recurrenceEndDate;
    }

    public Long getParentEventId() {
        return parentEventId;
    }

    public void setParentEventId(Long parentEventId) {
        this.parentEventId = parentEventId;
    }

    public Boolean getIsRecurringInstance() {
        return isRecurringInstance;
    }

    public void setIsRecurringInstance(Boolean isRecurringInstance) {
        this.isRecurringInstance = isRecurringInstance;
    }

    public LocalDateTime getOriginalStartDateTime() {
        return originalStartDateTime;
    }

    public void setOriginalStartDateTime(LocalDateTime originalStartDateTime) {
        this.originalStartDateTime = originalStartDateTime;
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

    public Long getWikiId() {
        return wikiId;
    }

    public void setWikiId(Long wikiId) {
        this.wikiId = wikiId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CalendarEvent that = (CalendarEvent) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "CalendarEvent{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", startDateTime=" + startDateTime +
                ", endDateTime=" + endDateTime +
                ", isAllDay=" + isAllDay +
                '}';
    }
}
