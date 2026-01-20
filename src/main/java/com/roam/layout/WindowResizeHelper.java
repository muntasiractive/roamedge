package com.roam.layout;

import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

/**
 * Handles window resizing for undecorated windows.
 * Provides resize handles on all edges and corners.
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public class WindowResizeHelper {

    private static final double RESIZE_MARGIN = 6;

    private final Stage stage;
    private final Region root;

    private double startX, startY;
    private double startWidth, startHeight;
    private double startStageX, startStageY;
    private ResizeDirection resizeDirection = ResizeDirection.NONE;

    private enum ResizeDirection {
        NONE, N, S, E, W, NE, NW, SE, SW
    }

    public WindowResizeHelper(Stage stage, Region root) {
        this.stage = stage;
        this.root = root;
        setupResizeHandlers();
    }

    private void setupResizeHandlers() {
        root.setOnMouseMoved(this::updateCursor);
        root.setOnMousePressed(this::handleMousePressed);
        root.setOnMouseDragged(this::handleMouseDragged);
        root.setOnMouseReleased(e -> resizeDirection = ResizeDirection.NONE);
    }

    private void updateCursor(MouseEvent event) {
        double x = event.getX();
        double y = event.getY();
        double width = root.getWidth();
        double height = root.getHeight();

        Cursor cursor = Cursor.DEFAULT;
        resizeDirection = ResizeDirection.NONE;

        // Check corners first
        if (x < RESIZE_MARGIN && y < RESIZE_MARGIN) {
            cursor = Cursor.NW_RESIZE;
            resizeDirection = ResizeDirection.NW;
        } else if (x > width - RESIZE_MARGIN && y < RESIZE_MARGIN) {
            cursor = Cursor.NE_RESIZE;
            resizeDirection = ResizeDirection.NE;
        } else if (x < RESIZE_MARGIN && y > height - RESIZE_MARGIN) {
            cursor = Cursor.SW_RESIZE;
            resizeDirection = ResizeDirection.SW;
        } else if (x > width - RESIZE_MARGIN && y > height - RESIZE_MARGIN) {
            cursor = Cursor.SE_RESIZE;
            resizeDirection = ResizeDirection.SE;
        }
        // Then check edges
        else if (x < RESIZE_MARGIN) {
            cursor = Cursor.W_RESIZE;
            resizeDirection = ResizeDirection.W;
        } else if (x > width - RESIZE_MARGIN) {
            cursor = Cursor.E_RESIZE;
            resizeDirection = ResizeDirection.E;
        } else if (y < RESIZE_MARGIN) {
            cursor = Cursor.N_RESIZE;
            resizeDirection = ResizeDirection.N;
        } else if (y > height - RESIZE_MARGIN) {
            cursor = Cursor.S_RESIZE;
            resizeDirection = ResizeDirection.S;
        }

        root.setCursor(cursor);
    }

    private void handleMousePressed(MouseEvent event) {
        if (resizeDirection == ResizeDirection.NONE)
            return;

        startX = event.getScreenX();
        startY = event.getScreenY();
        startWidth = stage.getWidth();
        startHeight = stage.getHeight();
        startStageX = stage.getX();
        startStageY = stage.getY();
    }

    private void handleMouseDragged(MouseEvent event) {
        if (resizeDirection == ResizeDirection.NONE)
            return;

        double dx = event.getScreenX() - startX;
        double dy = event.getScreenY() - startY;

        double minWidth = stage.getMinWidth();
        double minHeight = stage.getMinHeight();

        switch (resizeDirection) {
            case N:
                resizeNorth(dy, minHeight);
                break;
            case S:
                resizeSouth(dy, minHeight);
                break;
            case E:
                resizeEast(dx, minWidth);
                break;
            case W:
                resizeWest(dx, minWidth);
                break;
            case NE:
                resizeNorth(dy, minHeight);
                resizeEast(dx, minWidth);
                break;
            case NW:
                resizeNorth(dy, minHeight);
                resizeWest(dx, minWidth);
                break;
            case SE:
                resizeSouth(dy, minHeight);
                resizeEast(dx, minWidth);
                break;
            case SW:
                resizeSouth(dy, minHeight);
                resizeWest(dx, minWidth);
                break;
            default:
                break;
        }
    }

    private void resizeNorth(double dy, double minHeight) {
        double newHeight = startHeight - dy;
        if (newHeight >= minHeight) {
            stage.setY(startStageY + dy);
            stage.setHeight(newHeight);
        }
    }

    private void resizeSouth(double dy, double minHeight) {
        double newHeight = startHeight + dy;
        if (newHeight >= minHeight) {
            stage.setHeight(newHeight);
        }
    }

    private void resizeEast(double dx, double minWidth) {
        double newWidth = startWidth + dx;
        if (newWidth >= minWidth) {
            stage.setWidth(newWidth);
        }
    }

    private void resizeWest(double dx, double minWidth) {
        double newWidth = startWidth - dx;
        if (newWidth >= minWidth) {
            stage.setX(startStageX + dx);
            stage.setWidth(newWidth);
        }
    }
}
