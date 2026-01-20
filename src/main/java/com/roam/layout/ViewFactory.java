package com.roam.layout;

import com.roam.controller.*;
import com.roam.view.*;
import javafx.scene.Node;

import java.util.function.Consumer;

/**
 * Factory for creating views with their controllers.
 * Uses Factory Pattern to centralize view instantiation.
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public class ViewFactory {

    public ViewFactory() {
    }

    // creates operations view with controller
    public Node createOperationsView(Consumer<com.roam.model.Operation> onOperationClick) {
        OperationsController controller = new OperationsController();
        OperationsView view = new OperationsView(controller);
        view.setOnOperationClick(onOperationClick);
        return view;
    }

    // creates detail view for a specific operation
    public Node createOperationDetailView(com.roam.model.Operation operation, Runnable onBackClick) {
        return new com.roam.view.OperationDetailView(operation, onBackClick);
    }

    // creates calendar view with controller
    public Node createCalendarView() {
        CalendarController controller = new CalendarController();
        return new CalendarView(controller);
    }

    // creates tasks view with controller
    public Node createTasksView() {
        TasksController controller = new TasksController();
        return new TasksView(controller);
    }

    // creates card-style wiki view
    public Node createWikiView() {
        WikiController controller = new WikiController();
        return new WikiKeepView(controller);
    }

    // creates journal view
    public Node createJournalView() {
        return new JournalView();
    }

    // creates assets view for file browsing
    public Node createAssetsView() {
        return new AssetsView();
    }

    // creates home dashboard view
    public HomeView createHomeView(Consumer<String> onNavigate) {
        HomeView homeView = new HomeView();
        homeView.setOnNavigate(onNavigate);
        return homeView;
    }

    // creates settings view
    public Node createSettingsView() {
        return new SettingsView();
    }

    // creates search view for displaying results
    public SearchView createSearchView(Consumer<com.roam.service.SearchService.SearchResult> onResultClick) {
        SearchView searchView = new SearchView(java.util.Collections.emptyList(), "");
        searchView.setOnResultSelected(onResultClick);
        return searchView;
    }
}
