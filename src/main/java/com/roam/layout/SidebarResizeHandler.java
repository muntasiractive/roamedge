package com.roam.layout;

import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles sidebar resizing functionality.
 * Manages the resize handle UI and drag events for adjusting sidebar width.
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public class SidebarResizeHandler {

    private static final Logger logger = LoggerFactory.getLogger(SidebarResizeHandler.class);

    // Resize handle dimensions
    private static final double RESIZE_HANDLE_WIDTH = 8;

    // Sidebar width constraints
    private static final double SIDEBAR_MIN_WIDTH = 180;
    private static final double SIDEBAR_MAX_WIDTH = 400;

    private final Region resizeHandle;
    private final VBox sidebar;
    private final Runnable onWidthChanged;

    private double dragStartX;
    private double dragStartWidth;

    /**
     * Creates a new sidebar resize handler.
     * 
     * @param sidebar        The sidebar VBox to resize
     * @param onWidthChanged Callback when sidebar width changes (for updating
     *                       button widths)
     */
    public SidebarResizeHandler(VBox sidebar, Runnable onWidthChanged) {
        this.sidebar = sidebar;
        this.onWidthChanged = onWidthChanged;
        this.resizeHandle = createResizeHandle();

        logger.debug("SidebarResizeHandler created");
    }

    /**
     * Creates the resize handle with hover effects and drag handlers.
     */
    private Region createResizeHandle() {
        Region handle = new Region();
        handle.setPrefWidth(RESIZE_HANDLE_WIDTH);
        handle.setMinWidth(RESIZE_HANDLE_WIDTH);
        handle.setMaxWidth(RESIZE_HANDLE_WIDTH);
        handle.setStyle("-fx-background-color: transparent; -fx-cursor: h-resize;");

        // Change appearance on hover
        handle.setOnMouseEntered(e -> {
            handle.setCursor(Cursor.H_RESIZE);
            handle.setStyle("-fx-background-color: rgba(66, 133, 244, 0.3); -fx-cursor: h-resize;");
        });

        handle.setOnMouseExited(e -> {
            handle.setCursor(Cursor.DEFAULT);
            handle.setStyle("-fx-background-color: transparent; -fx-cursor: h-resize;");
        });

        // Handle drag to resize
        handle.setOnMousePressed(this::handleResizeStart);
        handle.setOnMouseDragged(this::handleResizeDrag);

        return handle;
    }

    /**
     * Handles the start of a resize drag operation.
     */
    private void handleResizeStart(MouseEvent event) {
        dragStartX = event.getSceneX();
        dragStartWidth = sidebar.getWidth();
        logger.debug("Resize started: startX={}, startWidth={}", dragStartX, dragStartWidth);
    }

    /**
     * Handles the drag event for resizing the sidebar.
     */
    private void handleResizeDrag(MouseEvent event) {
        double deltaX = event.getSceneX() - dragStartX;
        double newWidth = dragStartWidth + deltaX;

        // Clamp to min/max width
        newWidth = Math.max(SIDEBAR_MIN_WIDTH, Math.min(SIDEBAR_MAX_WIDTH, newWidth));

        // Update sidebar width
        sidebar.setPrefWidth(newWidth);
        sidebar.setMinWidth(newWidth);
        sidebar.setMaxWidth(newWidth);

        // Notify callback to update button widths
        if (onWidthChanged != null) {
            onWidthChanged.run();
        }

        logger.trace("Sidebar resized to width: {}", newWidth);
    }

    /**
     * Gets the resize handle region.
     */
    public Region getResizeHandle() {
        return resizeHandle;
    }

    /**
     * Gets the minimum sidebar width.
     */
    public static double getMinWidth() {
        return SIDEBAR_MIN_WIDTH;
    }

    /**
     * Gets the maximum sidebar width.
     */
    public static double getMaxWidth() {
        return SIDEBAR_MAX_WIDTH;
    }
}
