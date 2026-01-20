package com.roam.view;

import com.roam.service.SearchService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Dedicated view for displaying search results.
 * <p>
 * This view presents search results in a filterable, scrollable layout with
 * category-based filtering options (Wiki, Task, Operation, Event, Journal).
 * It supports responsive scaling and provides callback mechanisms for result
 * selection and navigation actions.
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public class SearchView extends StackPane {

    private final List<SearchService.SearchResult> results;
    private final String query;
    private final BorderPane contentPane;
    private Consumer<SearchService.SearchResult> onResultSelected;
    private Runnable onBackAction;

    private VBox resultsContainer;
    private CheckBox wikiFilter;
    private CheckBox taskFilter;
    private CheckBox operationFilter;
    private CheckBox eventFilter;
    private CheckBox journalFilter;

    public SearchView(List<SearchService.SearchResult> results, String query) {
        this.results = results;
        this.query = query;
        this.contentPane = new BorderPane();
        getChildren().add(contentPane);

        initializeView();

        // Add listeners for responsive scaling
        this.widthProperty().addListener((obs, oldVal, newVal) -> scaleContent());
        this.heightProperty().addListener((obs, oldVal, newVal) -> scaleContent());
    }

    private void scaleContent() {
        double width = getWidth();
        double height = getHeight();

        // Use layout bounds to get the actual size of the content
        double contentWidth = contentPane.getLayoutBounds().getWidth();
        double contentHeight = contentPane.getLayoutBounds().getHeight();

        if (contentWidth == 0 || contentHeight == 0)
            return;

        // Calculate scale factors
        double scaleX = width < contentWidth ? width / contentWidth : 1.0;
        double scaleY = height < contentHeight ? height / contentHeight : 1.0;

        // Use the smaller scale to maintain aspect ratio and fit within bounds
        double scale = Math.min(scaleX, scaleY);

        contentPane.setScaleX(scale);
        contentPane.setScaleY(scale);
    }

    private void initializeView() {
        contentPane.setPadding(new Insets(30));
        contentPane.setStyle("-fx-background-color: -roam-bg-primary;");

        // Top: Header with back button and title
        HBox header = createHeader();
        contentPane.setTop(header);

        // Center: Filters and results
        VBox centerContent = new VBox(20);
        centerContent.setPadding(new Insets(20, 0, 0, 0));

        // Filters
        HBox filters = createFilters();

        // Results container
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        resultsContainer = new VBox(10);
        displayResults(results);

        scrollPane.setContent(resultsContainer);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        centerContent.getChildren().addAll(filters, scrollPane);
        contentPane.setCenter(centerContent);
    }

    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 20, 0));

        // Back button
        Button backBtn = new Button("← Back");
        backBtn.getStyleClass().add("button-secondary");
        backBtn.setStyle("-fx-font-size: 14px; -fx-padding: 8 16;");
        backBtn.setOnAction(e -> {
            if (onBackAction != null) {
                onBackAction.run();
            }
        });

        // Title and result count
        VBox titleBox = new VBox(5);

        Label titleLabel = new Label("Search Results");
        titleLabel.setFont(Font.font("Poppins Bold", 24));
        titleLabel.setStyle("-fx-text-fill: -roam-text-primary;");

        Label queryLabel = new Label("\"" + query + "\" - " + results.size() + " results found");
        queryLabel.setFont(Font.font("Poppins", 14));
        queryLabel.setStyle("-fx-text-fill: -roam-text-secondary;");

        titleBox.getChildren().addAll(titleLabel, queryLabel);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        header.getChildren().addAll(backBtn, titleBox);

        return header;
    }

    private HBox createFilters() {
        HBox filters = new HBox(15);
        filters.setAlignment(Pos.CENTER_LEFT);
        filters.setPadding(new Insets(10, 0, 10, 0));

        Label filterLabel = new Label("Filter by type:");
        filterLabel.setFont(Font.font("Poppins Bold", 13));

        wikiFilter = new CheckBox("Wiki");
        taskFilter = new CheckBox("Tasks");
        operationFilter = new CheckBox("Operations");
        eventFilter = new CheckBox("Events");
        journalFilter = new CheckBox("Journal");

        // All selected by default
        wikiFilter.setSelected(true);
        taskFilter.setSelected(true);
        operationFilter.setSelected(true);
        eventFilter.setSelected(true);
        journalFilter.setSelected(true);

        // Update results when filters change
        wikiFilter.setOnAction(e -> applyFilters());
        taskFilter.setOnAction(e -> applyFilters());
        operationFilter.setOnAction(e -> applyFilters());
        eventFilter.setOnAction(e -> applyFilters());
        journalFilter.setOnAction(e -> applyFilters());

        filters.getChildren().addAll(filterLabel, wikiFilter, taskFilter, operationFilter,
                eventFilter, journalFilter);

        return filters;
    }

    private void applyFilters() {
        List<SearchService.SearchResult> filtered = results.stream()
                .filter(r -> {
                    switch (r.type) {
                        case "wiki":
                            return wikiFilter.isSelected();
                        case "task":
                            return taskFilter.isSelected();
                        case "operation":
                            return operationFilter.isSelected();
                        case "event":
                            return eventFilter.isSelected();
                        case "journal":
                            return journalFilter.isSelected();
                        default:
                            return true;
                    }
                })
                .collect(Collectors.toList());

        displayResults(filtered);
    }

    private void displayResults(List<SearchService.SearchResult> filteredResults) {
        resultsContainer.getChildren().clear();

        if (filteredResults.isEmpty()) {
            Label noResults = new Label("No results found");
            noResults.setFont(Font.font("Poppins", 14));
            noResults.setStyle("-fx-text-fill: -roam-text-secondary; -fx-padding: 20;");
            resultsContainer.getChildren().add(noResults);
            return;
        }

        // Group by type
        List<SearchService.SearchResult> wikiResults = filteredResults.stream()
                .filter(r -> r.type.equals("wiki")).collect(Collectors.toList());
        List<SearchService.SearchResult> tasks = filteredResults.stream()
                .filter(r -> r.type.equals("task")).collect(Collectors.toList());
        List<SearchService.SearchResult> operations = filteredResults.stream()
                .filter(r -> r.type.equals("operation")).collect(Collectors.toList());
        List<SearchService.SearchResult> events = filteredResults.stream()
                .filter(r -> r.type.equals("event")).collect(Collectors.toList());
        List<SearchService.SearchResult> journals = filteredResults.stream()
                .filter(r -> r.type.equals("journal")).collect(Collectors.toList());

        // Display each group
        if (!wikiResults.isEmpty()) {
            resultsContainer.getChildren()
                    .add(createGroupHeader("Wiki (" + wikiResults.size() + ")", Feather.FILE_TEXT));
            wikiResults.forEach(r -> resultsContainer.getChildren().add(createResultItem(r)));
        }

        if (!tasks.isEmpty()) {
            resultsContainer.getChildren().add(createGroupHeader("Tasks (" + tasks.size() + ")", Feather.CHECK_SQUARE));
            tasks.forEach(r -> resultsContainer.getChildren().add(createResultItem(r)));
        }

        if (!operations.isEmpty()) {
            resultsContainer.getChildren()
                    .add(createGroupHeader("Operations (" + operations.size() + ")", Feather.CLIPBOARD));
            operations.forEach(r -> resultsContainer.getChildren().add(createResultItem(r)));
        }

        if (!events.isEmpty()) {
            resultsContainer.getChildren().add(createGroupHeader("Events (" + events.size() + ")", Feather.CALENDAR));
            events.forEach(r -> resultsContainer.getChildren().add(createResultItem(r)));
        }

        if (!journals.isEmpty()) {
            resultsContainer.getChildren().add(createGroupHeader("Journal (" + journals.size() + ")", Feather.BOOK));
            journals.forEach(r -> resultsContainer.getChildren().add(createResultItem(r)));
        }
    }

    private HBox createGroupHeader(String text, Feather iconType) {
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15, 0, 10, 0));

        FontIcon icon = new FontIcon(iconType);
        icon.setIconSize(16);

        Label label = new Label(text);
        label.setFont(Font.font("Poppins Bold", 16));
        label.setStyle("-fx-text-fill: -roam-text-primary;");

        header.getChildren().addAll(icon, label);
        return header;
    }

    private VBox createResultItem(SearchService.SearchResult result) {
        VBox item = new VBox(8);
        item.setPadding(new Insets(15));
        item.setStyle("-fx-background-color: -roam-bg-primary; -fx-background-radius: 8; " +
                "-fx-border-color: -roam-border; -fx-border-radius: 8; -fx-border-width: 1; -fx-cursor: hand;");

        // Title
        Label titleLabel = new Label(result.title != null ? result.title : "Untitled");
        titleLabel.setFont(Font.font("Poppins Bold", 14));
        titleLabel.setStyle("-fx-text-fill: -roam-text-primary;");

        // Snippet
        Label snippetLabel = new Label(result.snippet);
        snippetLabel.setFont(Font.font("Poppins", 12));
        snippetLabel.setStyle("-fx-text-fill: -roam-text-secondary;");
        snippetLabel.setWrapText(true);

        // Metadata
        HBox metadata = new HBox(15);
        metadata.setAlignment(Pos.CENTER_LEFT);

        // Type badge
        Label typeBadge = createTypeBadge(result.type);
        metadata.getChildren().add(typeBadge);

        // Additional metadata based on type
        if (result.priority != null) {
            Label priorityLabel = new Label("Priority: " + result.priority);
            priorityLabel.setFont(Font.font("Poppins", 11));
            priorityLabel.setStyle("-fx-text-fill: -roam-text-hint;");
            metadata.getChildren().add(priorityLabel);
        }

        if (result.status != null) {
            Label statusLabel = new Label("Status: " + result.status);
            statusLabel.setFont(Font.font("Poppins", 11));
            statusLabel.setStyle("-fx-text-fill: -roam-text-hint;");
            metadata.getChildren().add(statusLabel);
        }

        if (result.region != null && !result.region.isEmpty()) {
            Label regionLabel = new Label("Region: " + result.region);
            regionLabel.setFont(Font.font("Poppins", 11));
            regionLabel.setStyle("-fx-text-fill: -roam-text-hint;");
            metadata.getChildren().add(regionLabel);
        }

        item.getChildren().addAll(titleLabel, snippetLabel, metadata);

        // Hover effect
        item.setOnMouseEntered(e -> item.setStyle("-fx-background-color: -roam-gray-bg; -fx-background-radius: 8; " +
                "-fx-border-color: -roam-blue; -fx-border-radius: 8; -fx-border-width: 2; -fx-cursor: hand;"));
        item.setOnMouseExited(e -> item.setStyle("-fx-background-color: -roam-bg-primary; -fx-background-radius: 8; " +
                "-fx-border-color: -roam-border; -fx-border-radius: 8; -fx-border-width: 1; -fx-cursor: hand;"));

        // Click handler
        item.setOnMouseClicked(e -> {
            if (onResultSelected != null) {
                onResultSelected.accept(result);
            }
        });

        return item;
    }

    private Label createTypeBadge(String type) {
        Label badge = new Label(type.toUpperCase());
        badge.setFont(Font.font("Poppins Bold", 10));
        badge.setPadding(new Insets(4, 10, 4, 10));
        badge.setStyle("-fx-background-radius: 4; -fx-text-fill: white;");

        switch (type) {
            case "wiki":
                badge.setText("WIKI");
                badge.setStyle(badge.getStyle() + " -fx-background-color: -roam-blue;");
                break;
            case "task":
                badge.setStyle(badge.getStyle() + " -fx-background-color: -roam-green;");
                break;
            case "operation":
                badge.setStyle(badge.getStyle() + " -fx-background-color: -roam-red;");
                break;
            case "event":
                badge.setStyle(badge.getStyle() + " -fx-background-color: -roam-orange;");
                break;
            case "journal":
                badge.setStyle(badge.getStyle() + " -fx-background-color: -roam-purple;");
                break;
        }

        return badge;
    }

    public void setOnResultSelected(Consumer<SearchService.SearchResult> handler) {
        this.onResultSelected = handler;
    }

    public void setOnBackAction(Runnable handler) {
        this.onBackAction = handler;
    }
}
