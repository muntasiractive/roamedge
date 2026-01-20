package com.roam.model;

/**
 * Represents the current status of a task within the Roam application.
 * <p>
 * Task statuses track the lifecycle of individual tasks from creation to
 * completion,
 * providing clear visibility into work progress and helping users manage their
 * workload.
 * </p>
 *
 * <h2>Enum Values:</h2>
 * <ul>
 * <li>{@link #TODO} - Task has been created but work has not started</li>
 * <li>{@link #IN_PROGRESS} - Task is actively being worked on</li>
 * <li>{@link #DONE} - Task has been completed</li>
 * </ul>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public enum TaskStatus {

    /**
     * Task is pending and has not been started.
     * <p>
     * This is the initial status for newly created tasks. Items in this state
     * are queued for future work and await assignment or scheduling.
     * </p>
     */
    TODO,

    /**
     * Task is currently being worked on.
     * <p>
     * This status indicates active engagement with the task. Work has commenced
     * but is not yet complete. Tasks remain in this state until all required
     * work is finished.
     * </p>
     */
    IN_PROGRESS,

    /**
     * Task has been completed.
     * <p>
     * This is the terminal status indicating all work on the task is finished.
     * Completed tasks may be archived or reviewed but require no further action.
     * </p>
     */
    DONE
}
