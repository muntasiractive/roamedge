package com.roam.controller;

import com.roam.model.*;
import com.roam.service.CalendarService;
import com.roam.service.CalendarServiceImpl;
import com.roam.repository.*;
import com.roam.util.DialogUtils;
import com.roam.view.components.EventDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller responsible for managing calendar operations and events.
 * <p>
 * This controller handles all calendar-related functionality including:
 * <ul>
 * <li>Calendar source management and visibility</li>
 * <li>Event CRUD operations (create, read, update, delete)</li>
 * <li>Recurring event generation and management</li>
 * <li>Task synchronization with calendar events</li>
 * </ul>
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public class CalendarController {

    // ==================== Fields ====================

    private static final Logger logger = LoggerFactory.getLogger(CalendarController.class);

    private final CalendarService calendarService;
    private final CalendarEventRepository eventRepository; // Keep for direct queries
    private final CalendarSourceRepository sourceRepository;
    private final OperationRepository operationRepository;
    private final TaskRepository taskRepository;
    private final RegionRepository regionRepository;
    private final WikiRepository WikiRepository;

    private List<CalendarSource> calendarSources;
    private List<CalendarEvent> allEvents;

    private Runnable onDataChanged;

    // ==================== Constructor ====================

    public CalendarController() {
        this.calendarService = new CalendarServiceImpl();
        this.eventRepository = new CalendarEventRepository();
        this.sourceRepository = new CalendarSourceRepository();
        this.operationRepository = new OperationRepository();
        this.taskRepository = new TaskRepository();
        this.regionRepository = new RegionRepository();
        this.WikiRepository = new WikiRepository();

        initialize();
    }

    // ==================== Public Methods ====================

    public void setOnDataChanged(Runnable handler) {
        this.onDataChanged = handler;
    }

    private void initialize() {
        logger.info("🗓️ Initializing Calendar...");

        // Create default calendar sources if not exist
        createDefaultCalendarSources();

        // Load all calendar sources
        calendarSources = sourceRepository.findAll();
        logger.info("✓ Loaded {} calendar sources", calendarSources.size());

        // Load all events
        loadAllEvents();

        // Sync tasks to events
        syncTasksToEvents();

        logger.info("✓ Calendar initialized with {} events", allEvents.size());
    }

    // ==================== Private Helper Methods ====================

    private void createDefaultCalendarSources() {
        // Create calendar sources from Regions
        List<Region> regions = regionRepository.findAll();
        List<CalendarSource> existingSources = sourceRepository.findAll();
        List<String> existingNames = existingSources.stream()
                .map(CalendarSource::getName)
                .collect(Collectors.toList());

        for (Region region : regions) {
            createSourceIfNotExists(existingNames, region.getName(), region.getColor(), CalendarSourceType.REGION,
                    region.getIsDefault());
        }
    }

    private void createSourceIfNotExists(List<String> existingNames, String name, String color, CalendarSourceType type,
            boolean isDefault) {
        if (!existingNames.contains(name)) {
            logger.debug("Creating calendar source: {}", name);
            CalendarSource source = new CalendarSource(name, color, type);
            source.setIsDefault(isDefault);
            sourceRepository.save(source);
        }
    }

    private void loadAllEvents() {
        allEvents = calendarService.findAll();
    }

    private void syncTasksToEvents() {
        try {
            // Get the first region-based calendar source for task syncing
            CalendarSource regionCal = calendarSources.stream()
                    .filter(s -> s.getType() == CalendarSourceType.REGION)
                    .findFirst()
                    .orElse(null);

            if (regionCal == null) {
                return;
            }

            // Get all tasks with due dates
            // Wiki: You'll need to add a findAll() method to TaskRepository
            // For now, we'll skip this and implement it later

        } catch (Exception e) {
            logger.error("Failed to sync tasks: {}", e.getMessage(), e);
        }
    }

    public List<CalendarSource> getCalendarSources() {
        return calendarSources;
    }

    public CalendarSource getCalendarSourceById(Long id) {
        return calendarSources.stream()
                .filter(s -> s.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public List<CalendarEvent> getEventsForDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<CalendarEvent> result = new ArrayList<>();

        for (CalendarEvent e : allEvents) {
            CalendarSource source = getCalendarSourceById(e.getCalendarSourceId());
            if (source == null || !source.getIsVisible()) {
                continue;
            }

            // Check if this event has recurrence
            if (e.getRecurrenceRule() != null && !e.getRecurrenceRule().isEmpty()) {
                // Generate recurring instances for this date
                List<CalendarEvent> instances = generateRecurringInstances(e, date, date);
                result.addAll(instances);
            } else {
                // Non-recurring event - check if it overlaps with this day
                LocalDateTime eventStart = e.getStartDateTime();
                LocalDateTime eventEnd = e.getEndDateTime();

                if (!eventStart.isAfter(endOfDay) && !eventEnd.isBefore(startOfDay)) {
                    result.add(e);
                }
            }
        }

        return result;
    }

    public List<CalendarEvent> getAllEvents() {
        // For agenda view, we need to generate recurring instances for a reasonable
        // range
        LocalDate startDate = LocalDate.now().minusMonths(1);
        LocalDate endDate = LocalDate.now().plusMonths(3);

        List<CalendarEvent> result = new ArrayList<>();

        for (CalendarEvent e : allEvents) {
            CalendarSource source = getCalendarSourceById(e.getCalendarSourceId());
            if (source == null || !source.getIsVisible()) {
                continue;
            }

            // Check if this event has recurrence
            if (e.getRecurrenceRule() != null && !e.getRecurrenceRule().isEmpty()) {
                // Generate recurring instances
                List<CalendarEvent> instances = generateRecurringInstances(e, startDate, endDate);
                result.addAll(instances);
            } else {
                result.add(e);
            }
        }

        return result;
    }

    /**
     * Generate recurring event instances for a date range.
     */
    private List<CalendarEvent> generateRecurringInstances(CalendarEvent event, LocalDate rangeStart,
            LocalDate rangeEnd) {
        List<CalendarEvent> instances = new ArrayList<>();
        String rule = event.getRecurrenceRule();

        if (rule == null || rule.isEmpty()) {
            return instances;
        }

        LocalDateTime eventStart = event.getStartDateTime();
        LocalDateTime eventEnd = event.getEndDateTime();
        long durationMinutes = ChronoUnit.MINUTES.between(eventStart, eventEnd);

        LocalDate recurrenceEndDate = event.getRecurrenceEndDate() != null
                ? event.getRecurrenceEndDate().toLocalDate()
                : rangeEnd.plusYears(1); // Default to 1 year if no end date

        LocalDate currentDate = eventStart.toLocalDate();

        while (!currentDate.isAfter(rangeEnd) && !currentDate.isAfter(recurrenceEndDate)) {
            if (!currentDate.isBefore(rangeStart)) {
                // Create an instance for this date
                CalendarEvent instance = createRecurringInstance(event, currentDate, durationMinutes);
                instances.add(instance);
            }

            // Calculate next occurrence based on rule
            currentDate = getNextOccurrence(currentDate, rule);

            if (currentDate == null) {
                break;
            }
        }

        return instances;
    }

    /**
     * Create a recurring instance of an event for a specific date.
     */
    private CalendarEvent createRecurringInstance(CalendarEvent parent, LocalDate date, long durationMinutes) {
        CalendarEvent instance = new CalendarEvent();
        instance.setId(parent.getId()); // Use parent ID for editing
        instance.setParentEventId(parent.getId());
        instance.setIsRecurringInstance(true);
        instance.setTitle(parent.getTitle());
        instance.setDescription(parent.getDescription());
        instance.setLocation(parent.getLocation());
        instance.setCalendarSourceId(parent.getCalendarSourceId());
        instance.setOperationId(parent.getOperationId());
        instance.setTaskId(parent.getTaskId());
        instance.setWikiId(parent.getWikiId());
        instance.setIsAllDay(parent.getIsAllDay());
        instance.setColor(parent.getColor());
        instance.setRegion(parent.getRegion());
        instance.setRecurrenceRule(parent.getRecurrenceRule());
        instance.setRecurrenceEndDate(parent.getRecurrenceEndDate());

        // Set the start and end times for this instance
        LocalTime startTime = parent.getStartDateTime().toLocalTime();
        instance.setStartDateTime(date.atTime(startTime));
        instance.setEndDateTime(instance.getStartDateTime().plusMinutes(durationMinutes));
        instance.setOriginalStartDateTime(parent.getStartDateTime());

        return instance;
    }

    /**
     * Calculate the next occurrence date based on recurrence rule.
     */
    private LocalDate getNextOccurrence(LocalDate current, String rule) {
        if (rule == null || rule.isEmpty()) {
            return null;
        }

        String lowerRule = rule.toLowerCase();

        if (lowerRule.equals("daily") || lowerRule.contains("freq=daily")) {
            return current.plusDays(1);
        } else if (lowerRule.equals("weekly") || lowerRule.contains("freq=weekly")) {
            return current.plusWeeks(1);
        } else if (lowerRule.equals("monthly") || lowerRule.contains("freq=monthly")) {
            return current.plusMonths(1);
        } else if (lowerRule.equals("yearly") || lowerRule.contains("freq=yearly")) {
            return current.plusYears(1);
        } else if (lowerRule.equals("weekdays") || lowerRule.contains("byday=mo,tu,we,th,fr")) {
            // Skip weekends
            LocalDate next = current.plusDays(1);
            while (next.getDayOfWeek().getValue() > 5) {
                next = next.plusDays(1);
            }
            return next;
        }

        // Default to no recurrence if rule not recognized
        return null;
    }

    public void createEvent(LocalDate date) {
        try {
            CalendarEvent event = new CalendarEvent();

            if (date != null) {
                event.setStartDateTime(date.atTime(9, 0));
                event.setEndDateTime(date.atTime(10, 0));
            } else {
                event.setStartDateTime(LocalDateTime.now().plusHours(1));
                event.setEndDateTime(LocalDateTime.now().plusHours(2));
            }

            List<Operation> operations = operationRepository.findAll();

            EventDialog dialog = new EventDialog(
                    event,
                    calendarSources,
                    operations,
                    regionRepository.findAll(),
                    taskRepository.findAll(),
                    WikiRepository.findAll(),
                    null);
            dialog.showAndWait().ifPresent(newEvent -> {
                try {
                    calendarService.createEvent(newEvent);
                    loadAllEvents();
                    if (onDataChanged != null) {
                        onDataChanged.run();
                    }
                } catch (Exception e) {
                    DialogUtils.showError("Save Error", "Failed to create event", e.getMessage());
                }
            });

        } catch (Exception e) {
            DialogUtils.showError("Error", "Failed to open event dialog", e.getMessage());
        }
    }

    public void editEvent(CalendarEvent event) {
        try {
            List<Operation> operations = operationRepository.findAll();

            EventDialog dialog = new EventDialog(
                    event,
                    calendarSources,
                    operations,
                    regionRepository.findAll(),
                    taskRepository.findAll(),
                    WikiRepository.findAll(),
                    () -> deleteEvent(event));

            dialog.showAndWait().ifPresent(updatedEvent -> {
                logger.debug("Saving edited event: {}", updatedEvent.getTitle());
                try {
                    calendarService.updateEvent(updatedEvent);
                    loadAllEvents();
                    if (onDataChanged != null) {
                        onDataChanged.run();
                    }
                } catch (Exception e) {
                    DialogUtils.showError("Save Error", "Failed to update event", e.getMessage());
                }
            });

        } catch (Exception e) {
            DialogUtils.showError("Error", "Failed to open event dialog", e.getMessage());
        }
    }

    public void deleteEvent(CalendarEvent event) {
        boolean confirmed = DialogUtils.showConfirmation(
                "Delete Event",
                "Are you sure you want to delete this event?",
                "Event: " + event.getTitle());

        if (confirmed) {
            try {
                calendarService.deleteEvent(event.getId());
                loadAllEvents();
                if (onDataChanged != null) {
                    onDataChanged.run();
                }
            } catch (Exception e) {
                DialogUtils.showError("Delete Error", "Failed to delete event", e.getMessage());
            }
        }
    }

    public void toggleCalendarVisibility(Long sourceId, boolean visible) {
        try {
            Optional<CalendarSource> sourceOpt = sourceRepository.findById(sourceId);
            sourceOpt.ifPresent(source -> {
                source.setIsVisible(visible);
                sourceRepository.save(source);

                // Update local list
                calendarSources = sourceRepository.findAll();
            });
        } catch (Exception e) {
            logger.error("Failed to toggle calendar visibility: {}", e.getMessage(), e);
        }
    }
}
