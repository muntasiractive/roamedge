package com.roam.controller.base;

/**
 * Interface defining the contract for controllers that support data refresh
 * operations.
 * <p>
 * This interface should be implemented by controllers that manage data which
 * may
 * change externally and need to be refreshed on demand. It provides a standard
 * pattern for:
 * <ul>
 * <li>Triggering data refresh/reload operations</li>
 * <li>Registering callbacks for data change notifications</li>
 * </ul>
 * </p>
 * <p>
 * Implementing controllers should ensure thread-safety when handling refresh
 * operations, especially when working with JavaFX UI components.
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public interface RefreshableController {

    // ==================== Refresh Operations ====================

    /**
     * Refreshes and reloads the data managed by this controller.
     * <p>
     * This method should be called when the underlying data source may have
     * changed and the controller's state needs to be synchronized. Implementations
     * should handle any necessary UI updates on the JavaFX Application Thread.
     * </p>
     */
    void refresh();

    // ==================== Event Handling ====================

    /**
     * Registers a callback to be executed when data changes in this controller.
     * <p>
     * The callback will be invoked whenever the controller's managed data
     * is modified through create, update, or delete operations. This allows
     * other components to react to data changes and update their state accordingly.
     * </p>
     *
     * @param callback the {@link Runnable} to execute when data changes;
     *                 may be {@code null} to clear the callback
     */
    void setOnDataChanged(Runnable callback);
}
