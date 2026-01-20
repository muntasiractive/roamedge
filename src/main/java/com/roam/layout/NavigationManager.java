package com.roam.layout;

import com.roam.model.Operation;
import com.roam.service.SearchService;
import com.roam.util.AnimationUtils;
import com.roam.view.SearchView;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Manages view navigation and transitions between different application views.
 * Handles view switching, operation detail navigation, search results, and view
 * history.
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public class NavigationManager {

    private static final Logger logger = LoggerFactory.getLogger(NavigationManager.class);

    private final StackPane contentArea;
    private final ViewFactory viewFactory;
    private final Map<String, Button> navigationButtons;
    private final Consumer<String> buttonStateUpdater;

    private Operation currentOperation;
    private Node previousView;
    private String currentViewType;

    public NavigationManager(StackPane contentArea, ViewFactory viewFactory,
            Map<String, Button> navigationButtons,
            Consumer<String> buttonStateUpdater) {
        this.contentArea = contentArea;
        this.viewFactory = viewFactory;
        this.navigationButtons = navigationButtons;
        this.buttonStateUpdater = buttonStateUpdater;
        this.currentViewType = "wiki"; // Default view
    }

    /**
     * Switches to the specified view type with transition.
     */
    public void navigateToView(String viewType) {
        logger.debug("Navigating to view: {}", viewType);
        currentViewType = viewType;
        currentOperation = null;

        // Update button states
        buttonStateUpdater.accept(viewType);

        // Restore default padding for most views
        contentArea.setPadding(new Insets(30));

        Node view = null;

        switch (viewType) {
            case "home":
                view = viewFactory.createHomeView(this::navigateToView);
                break;
            case "operations":
                view = viewFactory.createOperationsView(this::navigateToOperationDetail);
                break;
            case "calendar":
                view = viewFactory.createCalendarView();
                break;
            case "tasks":
                view = viewFactory.createTasksView();
                break;
            case "wiki":
                view = viewFactory.createWikiView();
                contentArea.setPadding(new Insets(0)); // Remove padding for wiki
                break;
            case "journal":
                view = viewFactory.createJournalView();
                break;
            case "assets":
                view = viewFactory.createAssetsView();
                break;
            case "settings":
                view = viewFactory.createSettingsView();
                break;
            default:
                logger.warn("Unknown view type: {}, defaulting to home", viewType);
                view = viewFactory.createHomeView(this::navigateToView);
                break;
        }

        if (view != null) {
            showViewWithTransition(view);
        }
    }

    /**
     * Navigates to the detail view for a specific operation.
     */
    public void navigateToOperationDetail(Operation operation) {
        logger.debug("Navigating to operation detail: {}", operation.getName());
        currentOperation = operation;

        // Keep operations button active
        buttonStateUpdater.accept("operations");

        // Remove padding for detail view
        contentArea.setPadding(new Insets(0));

        Node detailView = viewFactory.createOperationDetailView(operation, this::navigateBackToOperations);
        showViewWithTransition(detailView);
    }

    /**
     * Navigates back to the operations list view.
     */
    public void navigateBackToOperations() {
        logger.debug("Navigating back to operations");
        currentOperation = null;
        contentArea.setPadding(new Insets(30)); // Restore padding
        navigateToView("operations");
    }

    /**
     * Performs a search and displays results in SearchView.
     */
    public void performSearch(String query) {
        if (query == null) {
            query = "";
        }

        // If we are in tasks view, delegate search to it
        if ("tasks".equals(currentViewType) && !contentArea.getChildren().isEmpty()) {
            Node currentView = contentArea.getChildren().get(0);
            if (currentView instanceof com.roam.view.TasksView) {
                ((com.roam.view.TasksView) currentView).performSearch(query);
                return;
            }
        }

        if (query.trim().isEmpty()) {
            logger.debug("Empty search query, ignoring");
            return;
        }

        try {
            logger.debug("Performing search for query: {}", query);
            SearchService searchService = SearchService.getInstance();
            SearchService.SearchFilter filter = new SearchService.SearchFilter();
            List<SearchService.SearchResult> results = searchService.search(query, filter);

            // Store the current view so we can return to it
            previousView = contentArea.getChildren().isEmpty() ? null : contentArea.getChildren().get(0);

            // Create search view with results
            SearchView searchView = new SearchView(results, query);
            searchView.setOnResultSelected(this::navigateToSearchResult);
            searchView.setOnBackAction(() -> {
                if (previousView != null) {
                    showViewWithTransition(previousView);
                } else {
                    // Default to current view type if no previous view
                    navigateToView(currentViewType);
                }
            });

            showViewWithTransition(searchView);
            logger.info("Search completed: {} results found for query '{}'", results.size(), query);

        } catch (Exception ex) {
            logger.error("Error performing search for query: {}", query, ex);
            // TODO: Show error dialog
        }
    }

    /**
     * Navigates to the appropriate view based on search result type.
     */
    public void navigateToSearchResult(SearchService.SearchResult result) {
        logger.debug("Navigating to search result: type={}, id={}", result.type, result.id);

        // Navigate to the appropriate view based on result type
        switch (result.type) {
            case "wiki":
                navigateToView("wiki");
                // TODO: Navigate to specific wiki
                break;
            case "task":
                navigateToView("tasks");
                // TODO: Navigate to specific task
                break;
            case "operation":
                navigateToView("operations");
                // TODO: Navigate to specific operation
                break;
            case "event":
                navigateToView("calendar");
                // TODO: Navigate to specific event
                break;
            case "journal":
                navigateToView("journal");
                // TODO: Navigate to specific journal entry
                break;
            default:
                logger.warn("Unknown search result type: {}", result.type);
                break;
        }
    }

    /**
     * Shows a view with a smooth animated transition.
     */
    private void showViewWithTransition(Node view) {
        if (contentArea.getChildren().isEmpty()) {
            // First view, just add with fade in
            view.setOpacity(0);
            contentArea.getChildren().add(view);
            AnimationUtils.fadeIn(view, Duration.millis(200));
        } else {
            // Transition from existing view
            Node oldView = contentArea.getChildren().get(0);

            // Fade out old view, then swap and fade in new view
            FadeTransition fadeOut = new FadeTransition(Duration.millis(150), oldView);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                contentArea.getChildren().clear();
                view.setOpacity(0);
                contentArea.getChildren().add(view);
                AnimationUtils.fadeIn(view, Duration.millis(150));
            });
            fadeOut.play();
        }
    }

    /**
     * Gets the current operation if viewing operation detail.
     */
    public Operation getCurrentOperation() {
        return currentOperation;
    }

    /**
     * Gets the current view type.
     */
    public String getCurrentViewType() {
        return currentViewType;
    }
}
