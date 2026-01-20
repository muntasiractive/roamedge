package com.roam.service;

import com.roam.model.CalendarEvent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for CalendarEvent business logic.
 * <p>
 * Provides transaction management and business operations for calendar events,
 * including CRUD operations, date-range queries, and synchronization with
 * tasks.
 * </p>
 * 
 * <h2>Key Operations:</h2>
 * <ul>
 * <li>Create, update, and delete calendar events</li>
 * <li>Find events by ID, date range, source, task, or operation</li>
 * <li>Synchronize task deadlines with calendar events</li>
 * <li>Index events for search functionality</li>
 * </ul>
 * 
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public interface CalendarService {

    /**
     * Creates a new calendar event.
     *
     * @param event The event to create
     * @return The created event with ID
     * @throws IllegalArgumentException if event is null
     * @throws RuntimeException         if creation fails
     */
    CalendarEvent createEvent(CalendarEvent event);

    /**
     * Updates an existing calendar event.
     *
     * @param event The event to update
     * @return The updated event
     * @throws IllegalArgumentException if event or ID is null
     * @throws RuntimeException         if update fails
     */
    CalendarEvent updateEvent(CalendarEvent event);

    /**
     * Deletes a calendar event by ID.
     *
     * @param id The event ID
     * @throws IllegalArgumentException if id is null
     * @throws RuntimeException         if deletion fails
     */
    void deleteEvent(Long id);

    /**
     * Finds a calendar event by ID.
     *
     * @param id The event ID
     * @return Optional containing the event if found
     */
    Optional<CalendarEvent> findById(Long id);

    /**
     * Retrieves all calendar events.
     *
     * @return List of all events
     * @throws RuntimeException if retrieval fails
     */
    List<CalendarEvent> findAll();

    /**
     * Finds events within a date range.
     *
     * @param start Start date/time
     * @param end   End date/time
     * @return List of events in range
     * @throws RuntimeException if retrieval fails
     */
    List<CalendarEvent> findByDateRange(LocalDateTime start, LocalDateTime end);

    /**
     * Finds events by calendar source.
     *
     * @param sourceId The calendar source ID
     * @return List of events from that source
     * @throws RuntimeException if retrieval fails
     */
    List<CalendarEvent> findBySourceId(Long sourceId);

    /**
     * Finds events associated with a task.
     *
     * @param taskId The task ID
     * @return List of events linked to the task
     * @throws RuntimeException if retrieval fails
     */
    List<CalendarEvent> findByTaskId(Long taskId);

    /**
     * Finds events associated with an operation.
     *
     * @param operationId The operation ID
     * @return List of events linked to the operation
     * @throws RuntimeException if retrieval fails
     */
    List<CalendarEvent> findByOperationId(Long operationId);

    /**
     * Synchronizes calendar events with task deadlines.
     * Creates/updates calendar events for tasks with due dates.
     *
     * @param taskId The task to sync
     * @throws RuntimeException if sync fails
     */
    void syncTaskToCalendar(Long taskId);

    /**
     * Counts all calendar events.
     *
     * @return Total count of events
     */
    long count();

    /**
     * Indexes a calendar event for search.
     *
     * @param event The event to index
     */
    void indexEvent(CalendarEvent event);
}
