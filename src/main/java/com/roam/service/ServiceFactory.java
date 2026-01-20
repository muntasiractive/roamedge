package com.roam.service;

/**
 * Factory for creating and managing service instances.
 * <p>
 * This singleton factory provides centralized service instantiation and
 * management,
 * following the Factory and Service Locator patterns. It simplifies dependency
 * management
 * and enables easier testing through service substitution.
 * </p>
 * 
 * <h2>Features:</h2>
 * <ul>
 * <li><b>Centralized Instantiation:</b> All service instances are created and
 * managed
 * in one place</li>
 * <li><b>Testability:</b> Services can be replaced with mock implementations
 * for testing</li>
 * <li><b>Future DI Ready:</b> Architecture supports migration to dependency
 * injection
 * frameworks like Spring or Guice</li>
 * <li><b>Lazy Initialization:</b> Services are created on first access</li>
 * </ul>
 * 
 * <h2>Usage Examples:</h2>
 * 
 * <pre>{@code
 * // Get a service instance
 * TaskService taskService = ServiceFactory.getInstance().getTaskService();
 * 
 * // Replace with mock for testing
 * ServiceFactory.getInstance().setTaskService(mockTaskService);
 * 
 * // Reset factory state (typically in test teardown)
 * ServiceFactory.resetInstance();
 * }</pre>
 * 
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 * @see TaskService
 * @see OperationService
 * @see WikiService
 * @see CalendarService
 * @see JournalService
 */
public class ServiceFactory {

    // ==================== Singleton Instance ====================

    private static ServiceFactory instance;

    // ==================== Service Instances ====================

    /** Task management service instance. */
    private TaskService taskService;

    /** Operation management service instance. */
    private OperationService operationService;

    /** Wiki/documentation service instance. */
    private WikiService wikiService;

    /** Calendar and scheduling service instance. */
    private CalendarService calendarService;

    /** Journal/diary service instance. */
    private JournalService journalService;

    // ==================== Constructor ====================

    /**
     * Private constructor to enforce singleton pattern.
     * <p>
     * Initializes all service instances with their default implementations.
     * </p>
     */
    private ServiceFactory() {
        // Initialize with default implementations
        this.taskService = new TaskServiceImpl();
        this.operationService = new OperationServiceImpl();
        this.wikiService = new WikiServiceImpl();
        this.calendarService = new CalendarServiceImpl();
        this.journalService = new JournalServiceImpl();
    }

    // ==================== Singleton Access ====================

    /**
     * Returns the singleton instance of the ServiceFactory.
     * <p>
     * This method is thread-safe and uses lazy initialization.
     * </p>
     *
     * @return the singleton ServiceFactory instance
     */
    public static synchronized ServiceFactory getInstance() {
        if (instance == null) {
            instance = new ServiceFactory();
        }
        return instance;
    }

    /**
     * Resets the singleton instance to {@code null}.
     * <p>
     * This method is primarily intended for testing purposes to ensure
     * a clean state between test cases. It allows tests to inject mock
     * services without affecting other tests.
     * </p>
     * 
     * <p>
     * <b>Warning:</b> Do not call this method in production code.
     * </p>
     */
    public static synchronized void resetInstance() {
        instance = null;
    }

    // ==================== Task Service ====================

    /**
     * Gets the task service instance.
     *
     * @return the current {@link TaskService} implementation
     */
    public TaskService getTaskService() {
        return taskService;
    }

    /**
     * Sets the task service implementation.
     * <p>
     * Use this method to inject a custom or mock implementation for testing.
     * </p>
     *
     * @param taskService the task service implementation to use
     */
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    // ==================== Operation Service ====================

    /**
     * Gets the operation service instance.
     *
     * @return the current {@link OperationService} implementation
     */
    public OperationService getOperationService() {
        return operationService;
    }

    /**
     * Sets the operation service implementation.
     * <p>
     * Use this method to inject a custom or mock implementation for testing.
     * </p>
     *
     * @param operationService the operation service implementation to use
     */
    public void setOperationService(OperationService operationService) {
        this.operationService = operationService;
    }

    // ==================== Wiki Service ====================

    /**
     * Gets the wiki service instance.
     *
     * @return the current {@link WikiService} implementation
     */
    public WikiService getWikiService() {
        return wikiService;
    }

    /**
     * Sets the wiki service implementation.
     * <p>
     * Use this method to inject a custom or mock implementation for testing.
     * </p>
     *
     * @param wikiService the wiki service implementation to use
     */
    public void setWikiService(WikiService wikiService) {
        this.wikiService = wikiService;
    }

    // ==================== Calendar Service ====================

    /**
     * Gets the calendar service instance.
     *
     * @return the current {@link CalendarService} implementation
     */
    public CalendarService getCalendarService() {
        return calendarService;
    }

    /**
     * Sets the calendar service implementation.
     * <p>
     * Use this method to inject a custom or mock implementation for testing.
     * </p>
     *
     * @param calendarService the calendar service implementation to use
     */
    public void setCalendarService(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    // ==================== Journal Service ====================

    /**
     * Gets the journal service instance.
     *
     * @return the current {@link JournalService} implementation
     */
    public JournalService getJournalService() {
        return journalService;
    }

    /**
     * Sets the journal service implementation.
     * <p>
     * Use this method to inject a custom or mock implementation for testing.
     * </p>
     *
     * @param journalService the journal service implementation to use
     */
    public void setJournalService(JournalService journalService) {
        this.journalService = journalService;
    }
}
