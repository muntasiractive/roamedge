package com.roam.view;

import com.roam.layout.CustomTitleBar;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

/**
 * Lock screen layout with custom title bar.
 * <p>
 * This layout component wraps the {@link LockScreen} with the application's
 * custom
 * title bar to provide a consistent look and feel across the application. It
 * handles
 * rounded corners, scrolling behavior for small windows, and keyboard event
 * forwarding
 * to the underlying lock screen component.
 * </p>
 * <p>
 * The layout maintains the same visual styling as the main application layout,
 * including shadow effects and corner radius for a polished appearance.
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 * @see LockScreen
 * @see com.roam.layout.CustomTitleBar
 */
public class LockScreenLayout extends BorderPane {

    private final CustomTitleBar titleBar;
    private final LockScreen lockScreen;
    private final ScrollPane scrollPane;
    private final VBox appContainer;
    private static final double CORNER_RADIUS = 12;

    public LockScreenLayout(Stage stage, Runnable onUnlock) {
        // Add style class for rounded corners
        getStyleClass().add("main-layout");

        // Add padding for shadow space (same as MainLayout)
        setPadding(new Insets(15));
        setStyle("-fx-background-color: transparent;");

        // Create custom title bar
        titleBar = new CustomTitleBar(stage);

        // Create lock screen content
        lockScreen = new LockScreen(onUnlock);

        // Add rounded corner style to lock screen
        lockScreen.getStyleClass().add("lock-screen-content");

        // Wrap lock screen in a ScrollPane for when window is small - hide scrollbars
        scrollPane = new ScrollPane(lockScreen);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.getStyleClass().add("lock-screen-scroll");

        // Forward keyboard events to the lock screen
        scrollPane.setOnKeyPressed(event -> lockScreen.fireEvent(event));
        scrollPane.setOnKeyTyped(event -> lockScreen.fireEvent(event));

        // Create a container with shadow effect (same as MainLayout)
        appContainer = new VBox(0);
        appContainer.getStyleClass().add("lock-screen-container");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        appContainer.getChildren().addAll(titleBar, scrollPane);

        // Create and apply rounded clip to container
        Rectangle clip = new Rectangle();
        clip.setArcWidth(CORNER_RADIUS * 2);
        clip.setArcHeight(CORNER_RADIUS * 2);
        clip.widthProperty().bind(appContainer.widthProperty());
        clip.heightProperty().bind(appContainer.heightProperty());
        appContainer.setClip(clip);

        // Handle maximize state changes - adjust padding and styling
        titleBar.setOnMaximizeStateChange(isMaximized -> {
            if (isMaximized) {
                // Remove padding when maximized
                setPadding(new Insets(0));
                appContainer.getStyleClass().add("maximized");
                // Remove rounded clip when maximized
                clip.setArcWidth(0);
                clip.setArcHeight(0);
            } else {
                // Restore padding when windowed
                setPadding(new Insets(15));
                appContainer.getStyleClass().remove("maximized");
                // Restore rounded clip
                clip.setArcWidth(CORNER_RADIUS * 2);
                clip.setArcHeight(CORNER_RADIUS * 2);
            }
        });

        // Set layout
        setCenter(appContainer);
    }

    /**
     * Get the title bar for theme updates
     */
    public CustomTitleBar getTitleBar() {
        return titleBar;
    }

    /**
     * Refresh theme on title bar
     */
    public void refreshTheme() {
        titleBar.refreshTheme();
    }
}
