package com.roam;

import com.roam.layout.CustomTitleBar;
import com.roam.layout.FloatingNavBar;
import com.roam.layout.NavigationManager;
import com.roam.layout.ViewFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main application layout.
 * 
 * Acts as the primary container for the application.
 * Manages the floating navigation bar and the main content area.
 *
 * @author Roam Development Team
 * @version 1.0
 */
public class MainLayout extends BorderPane {

    private static final Logger logger = LoggerFactory.getLogger(MainLayout.class);

    // Content area
    private StackPane contentArea;

    // Components
    private ViewFactory viewFactory;
    private NavigationManager navigationManager;
    private FloatingNavBar floatingNavBar;
    private CustomTitleBar titleBar;

    public MainLayout() {
        logger.debug("Initializing MainLayout");
        getStyleClass().add("main-layout");
        initializeLayout();
    }

    /**
     * Initialize with a stage reference for custom title bar
     */
    public MainLayout(Stage stage) {
        logger.debug("Initializing MainLayout with custom title bar");
        getStyleClass().add("main-layout");

        // Add padding for shadow space
        setPadding(new Insets(15));
        setStyle("-fx-background-color: transparent;");

        initializeLayoutWithTitleBar(stage);
    }

    private void initializeLayout() {
        // Create content area
        contentArea = new StackPane();
        contentArea.setPadding(new Insets(30, 30, 90, 30)); // Extra bottom padding for floating bar
        contentArea.getStyleClass().add("content-area");

        // Create view factory
        viewFactory = new ViewFactory();

        // Create floating nav bar
        floatingNavBar = new FloatingNavBar(
                this::handleNavigation,
                this::handleSearch);

        // Create navigation manager
        navigationManager = new NavigationManager(
                contentArea,
                viewFactory,
                floatingNavBar.getNavigationButtons(),
                this::updateButtonStates);

        // Layer content and floating bar - use BorderPane for proper positioning
        StackPane mainStack = new StackPane();
        mainStack.getChildren().add(contentArea);

        // Floating bar container at bottom
        HBox floatingBarContainer = new HBox();
        floatingBarContainer.setAlignment(Pos.BOTTOM_CENTER);
        floatingBarContainer.setPadding(new Insets(0, 0, 20, 0));
        floatingBarContainer.setPickOnBounds(false);
        floatingBarContainer.getChildren().add(floatingNavBar);

        mainStack.getChildren().add(floatingBarContainer);
        StackPane.setAlignment(floatingBarContainer, Pos.BOTTOM_CENTER);

        // Set layout regions
        setCenter(mainStack);

        // Show default view (home dashboard)
        navigationManager.navigateToView("home");

        logger.info("MainLayout initialized successfully");
    }

    private static final double CORNER_RADIUS = 12;

    private void initializeLayoutWithTitleBar(Stage stage) {
        // Create a wrapper VBox for the entire app content
        VBox appContainer = new VBox(0);
        appContainer.getStyleClass().add("app-container");
        VBox.setVgrow(appContainer, Priority.ALWAYS);

        // Create custom title bar
        titleBar = new CustomTitleBar(stage);

        // Create and apply rounded clip to container for proper corner clipping
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

        // Create content area
        contentArea = new StackPane();
        contentArea.setPadding(new Insets(30, 30, 90, 30)); // Extra bottom padding for floating bar
        contentArea.getStyleClass().add("content-area");

        // Create view factory
        viewFactory = new ViewFactory();

        // Create floating nav bar
        floatingNavBar = new FloatingNavBar(
                this::handleNavigation,
                this::handleSearch);

        // Create navigation manager
        navigationManager = new NavigationManager(
                contentArea,
                viewFactory,
                floatingNavBar.getNavigationButtons(),
                this::updateButtonStates);

        // Wire up title bar navigation callback for Settings button
        titleBar.setOnNavigate(this::handleNavigation);

        // Layer content and floating bar - use BorderPane for proper positioning
        StackPane mainStack = new StackPane();
        mainStack.setStyle("-fx-background-color: transparent;");
        mainStack.getChildren().add(contentArea);
        VBox.setVgrow(mainStack, Priority.ALWAYS);

        // Floating bar container at bottom
        HBox floatingBarContainer = new HBox();
        floatingBarContainer.setAlignment(Pos.BOTTOM_CENTER);
        floatingBarContainer.setPadding(new Insets(0, 0, 20, 0));
        floatingBarContainer.setPickOnBounds(false);
        floatingBarContainer.getChildren().add(floatingNavBar);

        mainStack.getChildren().add(floatingBarContainer);
        StackPane.setAlignment(floatingBarContainer, Pos.BOTTOM_CENTER);

        // Add title bar and main stack to the app container
        appContainer.getChildren().addAll(titleBar, mainStack);

        // Set the app container as center
        setCenter(appContainer);

        // Show default view (home dashboard)
        navigationManager.navigateToView("home");

        logger.info("MainLayout with custom title bar initialized successfully");
    }

    /**
     * Callback to handle navigation button clicks.
     */
    private void handleNavigation(String viewType) {
        logger.debug("Handle navigation request: {}", viewType);
        navigationManager.navigateToView(viewType);
    }

    /**
     * Callback to handle search requests.
     */
    private void handleSearch(String query) {
        logger.debug("Handle search request: {}", query);
        navigationManager.performSearch(query);
    }

    /**
     * Callback to update button states when navigation occurs.
     */
    private void updateButtonStates(String activeViewType) {
        logger.debug("Updating button states: active={}", activeViewType);
        floatingNavBar.setActiveButton(activeViewType);
    }

    /**
     * Get the custom title bar for theme updates
     */
    public CustomTitleBar getTitleBar() {
        return titleBar;
    }

    /**
     * Get the floating nav bar
     */
    public FloatingNavBar getFloatingNavBar() {
        return floatingNavBar;
    }

    /**
     * Refresh theme on all components
     */
    public void refreshTheme() {
        if (titleBar != null) {
            titleBar.refreshTheme();
        }
        if (floatingNavBar != null) {
            floatingNavBar.refreshTheme();
        }
    }

    /**
     * Navigate to a specific view programmatically
     */
    public void navigateTo(String viewType) {
        handleNavigation(viewType);
    }
}
