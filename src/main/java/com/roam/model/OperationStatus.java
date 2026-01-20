package com.roam.model;

/**
 * Represents the current status of an operation within the Roam application.
 * <p>
 * Operations are higher-level organizational units that may contain multiple
 * tasks.
 * This enum tracks the lifecycle of operations from initiation through
 * completion,
 * enabling effective project and workflow management.
 * </p>
 *
 * <h2>Enum Values:</h2>
 * <ul>
 * <li>{@link #ONGOING} - Operation is active and running continuously</li>
 * <li>{@link #IN_PROGRESS} - Operation is actively being executed</li>
 * <li>{@link #END} - Operation has been completed or terminated</li>
 * </ul>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public enum OperationStatus {

    /**
     * Operation is ongoing and running continuously.
     * <p>
     * This status indicates a persistent operation that continues over an extended
     * period. Ongoing operations may not have a defined end date and represent
     * continuous or recurring activities.
     * </p>
     */
    ONGOING,

    /**
     * Operation is actively being executed.
     * <p>
     * This status indicates that work on the operation has started and is currently
     * underway. The operation is moving toward completion with active engagement
     * from team members or automated processes.
     * </p>
     */
    IN_PROGRESS,

    /**
     * Operation has been completed or terminated.
     * <p>
     * This is the terminal status indicating the operation has concluded.
     * All associated work is finished, and the operation is now closed.
     * Ended operations may be archived for historical reference.
     * </p>
     */
    END
}
