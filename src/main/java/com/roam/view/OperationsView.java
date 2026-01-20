package com.roam.view;

import com.roam.controller.OperationsController;
import com.roam.model.Operation;
import com.roam.model.OperationStatus;
import com.roam.util.AnimationUtils;
import com.roam.view.components.OperationTableView;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.util.Duration;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Operations view for managing and tracking operational workflows.
 * <p>
 * This view provides functionality to:
 * <ul>
 * <li>Display operations in both table and card layouts</li>
 * <li>Filter operations by status (All, Active, Completed, etc.)</li>
 * <li>View operation details and progress</li>
 * <li>Track operation metrics and statistics</li>
 * </ul>
 * The view integrates with {@link com.roam.controller.OperationsController} and
 * supports click handlers for navigation to detailed operation views.
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public class OperationsView extends StackPane {

    private final OperationsController controller;
    private final OperationTableView tableView;
    private final StackPane contentArea;
    private final VBox emptyState;
    private final VBox contentPane;
    private FlowPane cardsContainer;
    private ScrollPane scrollPane;
    private HBox filterBar;
    private String currentFilter = "All";
    private Consumer<Operation> onOperationClick;

    public OperationsView(OperationsController controller) {
        this.controller = controller;
        this.tableView = new OperationTableView();
        this.contentArea = new StackPane();
        this.emptyState = createEmptyState();
        this.contentPane = new VBox();
        getChildren().add(contentPane);

        initialize();
    }

    public void setOnOperationClick(Consumer<Operation> handler) {
        this.onOperationClick = handler;
        tableView.setOnOperationClick(handler);
    }

    private void initialize() {
        // Configure container
        contentPane.setStyle("-fx-background-color: -roam-bg-primary;");
        contentPane.setSpacing(0);
        contentPane.getStyleClass().add("operations-view");

        // Create header with title and subtitle
        VBox header = createHeader();

        // Create filter bar
        filterBar = createFilterBar();

        // Configure content area as ScrollPane with cards
        scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        cardsContainer = new FlowPane();
        cardsContainer.setHgap(24);
        cardsContainer.setVgap(24);
        cardsContainer.setPadding(new Insets(24, 32, 32, 32));
        cardsContainer.setStyle("-fx-background-color: transparent;");

        scrollPane.setContent(cardsContainer);

        // Add empty state to contentArea (StackPane) - it will be shown/hidden as
        // needed
        emptyState.setVisible(false);
        emptyState.setManaged(false);
        contentArea.getChildren().addAll(scrollPane, emptyState);
        StackPane.setAlignment(emptyState, Pos.CENTER);

        contentArea.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(contentArea, Priority.ALWAYS);

        // Configure table (keep for compatibility)
        controller.setTableView(tableView);
        controller.setOnDataChanged(this::updateContent);

        tableView.setOnEdit(controller::editOperation);
        tableView.setOnDelete(controller::deleteOperation);

        // Add components
        contentPane.getChildren().addAll(header, filterBar, contentArea);

        // Initial load
        loadData();
    }

    private VBox createHeader() {
        VBox header = new VBox(4);
        header.setPadding(new Insets(32, 32, 16, 32));
        header.setStyle("-fx-background-color: -roam-bg-primary;");

        // Title row with button
        HBox titleRow = new HBox();
        titleRow.setAlignment(Pos.CENTER_LEFT);

        // Title and subtitle
        VBox titleBox = new VBox(4);
        Label title = new Label("Operations");
        title.setFont(Font.font("Poppins Bold", 28));
        title.setStyle("-fx-text-fill: -roam-text-primary;");

        Label subtitle = new Label("Manage your goals and outcomes");
        subtitle.setFont(Font.font("Poppins", 14));
        subtitle.setStyle("-fx-text-fill: -roam-text-secondary;");

        titleBox.getChildren().addAll(title, subtitle);

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // New Operation button
        Button newBtn = createNewOperationButton();

        titleRow.getChildren().addAll(titleBox, spacer, newBtn);
        header.getChildren().add(titleRow);

        return header;
    }

    private HBox createFilterBar() {
        HBox filterBar = new HBox(8);
        filterBar.setPadding(new Insets(0, 32, 16, 32));
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.setStyle("-fx-background-color: -roam-bg-primary;");

        ToggleGroup filterGroup = new ToggleGroup();

        // Include all regions from Region.DEFAULT_REGIONS plus "All" filter
        String[] filters = { "All", "Lifestyle", "Knowledge", "Skill", "Spirituality", "Career", "Finance", "Social",
                "Academic", "Relationship" };

        for (String filter : filters) {
            ToggleButton btn = new ToggleButton(filter);
            btn.setToggleGroup(filterGroup);
            btn.getStyleClass().add("filter-chip");
            if (filter.equals("All")) {
                btn.setSelected(true);
                btn.getStyleClass().add("filter-chip-selected");
            }
            btn.setOnAction(e -> {
                // Update styles
                filterBar.getChildren().forEach(node -> {
                    if (node instanceof ToggleButton) {
                        node.getStyleClass().remove("filter-chip-selected");
                    }
                });
                btn.getStyleClass().add("filter-chip-selected");
                currentFilter = filter;
                updateContent();
            });
            filterBar.getChildren().add(btn);
        }

        return filterBar;
    }

    private Button createNewOperationButton() {
        Button btn = new Button("New Operation");
        btn.setGraphic(new FontIcon(Feather.PLUS));
        btn.setFont(Font.font("Poppins SemiBold", 14));
        btn.setMinWidth(140);
        btn.setPrefHeight(40);
        btn.getStyleClass().addAll("pill-button", "primary");
        btn.setOnAction(e -> controller.createOperation());
        AnimationUtils.addPressEffect(btn);
        return btn;
    }

    private VBox createEmptyState() {
        VBox container = new VBox(20);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(60));
        container.setMaxWidth(500);
        container.getStyleClass().add("empty-state");

        FontIcon icon = new FontIcon(Feather.FOLDER);
        icon.setIconSize(72);
        icon.getStyleClass().add("icon");

        Label title = new Label("No Operations Yet");
        title.getStyleClass().add("title");

        Label description = new Label("Click '+ New Operation' to create your first operation");
        description.getStyleClass().add("description");

        container.getChildren().addAll(icon, title, description);
        return container;
    }

    private VBox createOperationCard(Operation operation) {
        VBox card = new VBox(12);
        card.setPrefWidth(340);
        card.setMinWidth(300);
        card.setMaxWidth(380);
        card.setPadding(new Insets(20));
        card.getStyleClass().add("operation-card");
        card.setCursor(javafx.scene.Cursor.HAND);

        // Status badge and menu
        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label statusBadge = createStatusBadge(operation.getStatus());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button menuBtn = new Button();
        menuBtn.setGraphic(new FontIcon(Feather.MORE_HORIZONTAL));
        menuBtn.getStyleClass().add("icon-button");
        menuBtn.setOnAction(e -> {
            e.consume();
            showOperationMenu(operation, menuBtn);
        });

        topRow.getChildren().addAll(statusBadge, spacer, menuBtn);

        // Operation name
        Label nameLabel = new Label(operation.getName());
        nameLabel.setFont(Font.font("Poppins SemiBold", 18));
        nameLabel.setStyle("-fx-text-fill: -roam-text-primary;");
        nameLabel.setWrapText(true);

        // Description/Purpose
        Label purposeLabel = new Label(operation.getPurpose() != null ? truncateText(operation.getPurpose(), 60) : "");
        purposeLabel.setFont(Font.font("Poppins", 13));
        purposeLabel.setStyle("-fx-text-fill: -roam-text-secondary;");
        purposeLabel.setWrapText(true);

        // Progress section
        VBox progressSection = new VBox(6);
        progressSection.setPadding(new Insets(8, 0, 0, 0));

        HBox progressRow = new HBox();
        progressRow.setAlignment(Pos.CENTER_LEFT);

        Label progressLabel = new Label("Progress");
        progressLabel.setFont(Font.font("Poppins", 12));
        progressLabel.setStyle("-fx-text-fill: -roam-text-secondary;");

        Region progressSpacer = new Region();
        HBox.setHgrow(progressSpacer, Priority.ALWAYS);

        int progressValue = calculateProgress(operation);
        Label progressPercent = new Label(progressValue + "%");
        progressPercent.setFont(Font.font("Poppins SemiBold", 12));
        progressPercent.setStyle("-fx-text-fill: -roam-text-primary;");

        progressRow.getChildren().addAll(progressLabel, progressSpacer, progressPercent);

        ProgressBar progressBar = new ProgressBar(progressValue / 100.0);
        progressBar.setPrefWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(6);
        progressBar.getStyleClass().add("operation-progress-bar");
        applyProgressBarColor(progressBar, operation.getStatus());

        progressSection.getChildren().addAll(progressRow, progressBar);

        // Due date
        HBox dateRow = new HBox(6);
        dateRow.setAlignment(Pos.CENTER_LEFT);
        dateRow.setPadding(new Insets(8, 0, 0, 0));

        FontIcon calendarIcon = new FontIcon(Feather.CALENDAR);
        calendarIcon.setIconSize(14);
        calendarIcon.setStyle("-fx-icon-color: -roam-text-hint;");

        String dueDateText = operation.getDueDate() != null
                ? "Due " + operation.getDueDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                : "No due date";
        Label dateLabel = new Label(dueDateText);
        dateLabel.setFont(Font.font("Poppins", 12));
        dateLabel.setStyle("-fx-text-fill: -roam-text-hint;");

        dateRow.getChildren().addAll(calendarIcon, dateLabel);

        card.getChildren().addAll(topRow, nameLabel, purposeLabel, progressSection, dateRow);

        // Click handler
        card.setOnMouseClicked(e -> {
            if (onOperationClick != null) {
                onOperationClick.accept(operation);
            }
        });

        // Hover effect
        card.setOnMouseEntered(e -> card.getStyleClass().add("operation-card-hover"));
        card.setOnMouseExited(e -> card.getStyleClass().remove("operation-card-hover"));

        return card;
    }

    private Label createStatusBadge(OperationStatus status) {
        Label badge = new Label();
        badge.setFont(Font.font("Poppins SemiBold", 11));
        badge.setPadding(new Insets(4, 12, 4, 12));

        if (status == null)
            status = OperationStatus.ONGOING;

        switch (status) {
            case ONGOING:
                badge.setText("Ongoing");
                badge.getStyleClass().add("status-badge-in-progress");
                break;
            case IN_PROGRESS:
                badge.setText("In Progress");
                badge.getStyleClass().add("status-badge-on-hold");
                break;
            case END:
                badge.setText("Completed");
                badge.getStyleClass().add("status-badge-completed");
                break;
            default:
                badge.setText("Ongoing");
                badge.getStyleClass().add("status-badge-in-progress");
        }

        return badge;
    }

    private int calculateProgress(Operation operation) {
        if (operation.getStatus() == OperationStatus.END) {
            return 100;
        } else if (operation.getStatus() == OperationStatus.ONGOING) {
            // Calculate based on time elapsed if due date exists
            if (operation.getDueDate() != null && operation.getCreatedAt() != null) {
                long totalDays = java.time.temporal.ChronoUnit.DAYS.between(
                        operation.getCreatedAt().toLocalDate(), operation.getDueDate());
                long elapsedDays = java.time.temporal.ChronoUnit.DAYS.between(
                        operation.getCreatedAt().toLocalDate(), java.time.LocalDate.now());
                if (totalDays > 0) {
                    return Math.min(95, Math.max(5, (int) ((elapsedDays * 100) / totalDays)));
                }
            }
            return 45; // Default for ongoing
        } else {
            return 10; // On hold
        }
    }

    private void applyProgressBarColor(ProgressBar bar, OperationStatus status) {
        if (status == OperationStatus.END) {
            bar.getStyleClass().add("progress-bar-completed");
        } else if (status == OperationStatus.IN_PROGRESS) {
            bar.getStyleClass().add("progress-bar-on-hold");
        } else {
            bar.getStyleClass().add("progress-bar-in-progress");
        }
    }

    private String truncateText(String text, int maxLength) {
        if (text == null)
            return "";
        if (text.length() <= maxLength)
            return text;
        return text.substring(0, maxLength) + "...";
    }

    private void showOperationMenu(Operation operation, Button anchor) {
        javafx.scene.control.ContextMenu menu = new javafx.scene.control.ContextMenu();

        javafx.scene.control.MenuItem editItem = new javafx.scene.control.MenuItem("Edit");
        editItem.setGraphic(new FontIcon(Feather.EDIT_2));
        editItem.setOnAction(e -> controller.editOperation(operation));

        javafx.scene.control.MenuItem deleteItem = new javafx.scene.control.MenuItem("Delete");
        deleteItem.setGraphic(new FontIcon(Feather.TRASH_2));
        deleteItem.setOnAction(e -> controller.deleteOperation(operation));

        menu.getItems().addAll(editItem, deleteItem);
        menu.show(anchor, javafx.geometry.Side.BOTTOM, 0, 0);
    }

    private void loadData() {
        controller.refreshTable();
    }

    private void updateContent() {
        List<Operation> operations = controller.loadOperations();

        // Filter operations
        List<Operation> filteredOps = operations;
        if (!currentFilter.equals("All")) {
            filteredOps = operations.stream()
                    .filter(op -> currentFilter.equalsIgnoreCase(op.getRegion()))
                    .collect(Collectors.toList());
        }

        cardsContainer.getChildren().clear();

        if (filteredOps.isEmpty()) {
            // Show empty state, hide scrollPane
            scrollPane.setVisible(false);
            scrollPane.setManaged(false);
            emptyState.setVisible(true);
            emptyState.setManaged(true);
            Platform.runLater(() -> AnimationUtils.scaleIn(emptyState, Duration.millis(300)));
        } else {
            // Hide empty state and show cards
            emptyState.setVisible(false);
            emptyState.setManaged(false);
            scrollPane.setVisible(true);
            scrollPane.setManaged(true);
            for (Operation operation : filteredOps) {
                VBox card = createOperationCard(operation);
                cardsContainer.getChildren().add(card);
            }
            Platform.runLater(
                    () -> AnimationUtils.staggerSlideUp(cardsContainer, Duration.millis(100), Duration.millis(50)));
        }
    }
}
