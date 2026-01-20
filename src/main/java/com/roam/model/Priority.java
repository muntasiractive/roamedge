package com.roam.model;

/**
 * Represents the priority level assigned to tasks and operations within the
 * Roam application.
 * <p>
 * Priority levels help users organize and focus on the most important items
 * first,
 * enabling effective time management and task prioritization.
 * </p>
 *
 * <h2>Enum Values:</h2>
 * <ul>
 * <li>{@link #HIGH} - Highest priority, requires immediate attention</li>
 * <li>{@link #MEDIUM} - Standard priority, should be addressed in due
 * course</li>
 * <li>{@link #LOW} - Lowest priority, can be deferred if necessary</li>
 * </ul>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public enum Priority {

    /**
     * Highest priority level.
     * <p>
     * Items marked with HIGH priority require immediate attention and should be
     * addressed before any other tasks. Typically used for urgent deadlines,
     * critical issues, or time-sensitive matters.
     * </p>
     */
    HIGH,

    /**
     * Standard priority level.
     * <p>
     * Items marked with MEDIUM priority represent regular tasks that should be
     * completed in a reasonable timeframe. These are important but not urgent,
     * and can be scheduled around higher priority items.
     * </p>
     */
    MEDIUM,

    /**
     * Lowest priority level.
     * <p>
     * Items marked with LOW priority can be deferred when necessary. These tasks
     * are still valuable but do not require immediate action and can be addressed
     * when higher priority work is complete.
     * </p>
     */
    LOW
}
