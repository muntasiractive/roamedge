package com.roam.view;

import com.roam.controller.TasksController;
import com.roam.view.components.BatchOperationsBar;
import com.roam.view.components.GlobalTasksKanban;
import com.roam.view.components.TasksFilterPanel;
import com.roam.view.components.TasksCardView;
import com.roam.view.components.TasksTimelineView;
import com.roam.view.components.TasksEisenhowerView;
import com.roam.view.components.TasksToolbar;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Tasks view providing comprehensive task management capabilities.
 * <p>
 * This view supports multiple task visualization modes:
 * <ul>
 * <li>Kanban board - Drag-and-drop task organization by status</li>
 * <li>Card view - Visual card-based task display</li>
 * <li>Timeline view - Chronological task arrangement</li>
 * <li>Eisenhower matrix - Priority-based quadrant organization</li>
 * </ul>
 * The view includes a filter panel, toolbar for view switching, and
 * batch operations bar for bulk task management.
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public class TasksView extends StackPane {

    private final TasksController controller;
    private final TasksToolbar toolbar;
    private final TasksFilterPanel filterPanel;
    private final GlobalTasksKanban kanbanView;
    private final TasksCardView cardView;
    private final TasksTimelineView timelineView;
    private final TasksEisenhowerView matrixView;
    private final BatchOperationsBar batchBar;
    private final BorderPane contentPane;

    public TasksView(TasksController controller) {
        this.controller = controller;
        this.contentPane = new BorderPane();
        getChildren().add(contentPane);

        // Create toolbar
        toolbar = new TasksToolbar(controller);
        toolbar.setOnViewChanged(this::switchView);

        // Create filter panel
        filterPanel = new TasksFilterPanel(controller);

        // Create kanban view
        kanbanView = new GlobalTasksKanban(controller);

        // Create card view (replaces list view)
        cardView = new TasksCardView(controller);

        // Create timeline view
        timelineView = new TasksTimelineView(controller);

        // Create Eisenhower matrix view
        matrixView = new TasksEisenhowerView(controller);

        // Create batch operations bar
        batchBar = new BatchOperationsBar(controller);

        // Combine toolbar and filter panel (removed stats bar)
        VBox topContainer = new VBox(toolbar, filterPanel);

        contentPane.setTop(topContainer);

        // Use StackPane for center to allow batch bar overlay
        StackPane centerStack = new StackPane();
        centerStack.getChildren().add(kanbanView);
        centerStack.getChildren().add(batchBar);
        StackPane.setAlignment(batchBar, Pos.BOTTOM_CENTER);

        contentPane.setCenter(centerStack);

        // Set up data change listener
        controller.setOnDataChanged(this::refreshView);

        // Initial load
        refreshView();
    }

    private void switchView(String viewName) {
        StackPane centerStack = new StackPane();

        switch (viewName) {
            case "kanban" -> {
                ScrollPane scrollPane = new ScrollPane(kanbanView);
                scrollPane.setFitToHeight(true);
                scrollPane.setFitToWidth(true);
                scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
                centerStack.getChildren().addAll(scrollPane, batchBar);
            }
            case "cards" -> {
                centerStack.getChildren().addAll(cardView, batchBar);
            }
            case "timeline" -> {
                centerStack.getChildren().add(timelineView);
            }
            case "matrix" -> {
                ScrollPane scrollPane = new ScrollPane(matrixView);
                scrollPane.setFitToWidth(true);
                scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
                centerStack.getChildren().add(scrollPane);
            }
        }

        StackPane.setAlignment(batchBar, Pos.BOTTOM_CENTER);
        contentPane.setCenter(centerStack);
        refreshView();
    }

    public void performSearch(String query) {
        controller.searchTasks(query);
    }

    private void refreshView() {
        String selectedView = toolbar.getSelectedView();

        switch (selectedView) {
            case "kanban" -> kanbanView.loadTasks(controller.loadTasks());
            case "cards" -> cardView.loadTasks(controller.loadTasks());
            case "timeline" -> timelineView.loadTasks(controller.loadTasks());
            case "matrix" -> matrixView.loadTasks(controller.loadTasks());
        }

        filterPanel.refresh();
    }
}
