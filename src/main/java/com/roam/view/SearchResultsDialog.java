package com.roam.view;

import com.roam.service.SearchService;
import com.roam.util.ThemeManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Dialog to display search results with filtering capabilities.
 * <p>
 * This dialog presents search results in a modal window with category-based
 * filtering options. Users can filter results by entity type (Wiki, Task,
 * Operation, Event, Journal) and select individual results for navigation.
 * </p>
 * <p>
 * The dialog adapts to the current application theme and provides a consistent
 * visual experience with styled result cards and filter checkboxes.
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 * @see com.roam.service.SearchService
 */
public class SearchResultsDialog {

    private final Stage stage;
    private final List<SearchService.SearchResult> results;
    private final String query;
    private Consumer<SearchService.SearchResult> onResultSelected;

    private VBox resultsContainer;
    private CheckBox wikiFilter;
    private CheckBox taskFilter;
    private CheckBox operationFilter;
    private CheckBox eventFilter;
    private CheckBox journalFilter;

    public SearchResultsDialog(List<SearchService.SearchResult> results, String query) {
        this.results = results;
        this.query = query;
        this.stage = new Stage();

        initializeDialog();
    }

    private void initializeDialog() {
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UTILITY);
        stage.setTitle("Search Results");

        // Get theme state for styling
        boolean isDark = ThemeManager.getInstance().isDarkTheme();
        String bgPrimary = isDark ? "#1e1e1e" : "#ffffff";
        String textPrimary = isDark ? "#ffffff" : "#212121";
        String textSecondary = isDark ? "#b0b0b0" : "#757575";
        String grayBg = isDark ? "#2d2d2d" : "#f5f5f5";

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: " + bgPrimary + ";");

        // Top: Title and filters
        VBox top = new VBox(15);

        // Title
        Label titleLabel = new Label("Search Results for \"" + query + "\"");
        titleLabel.setFont(Font.font("Poppins Bold", 18));
        titleLabel.setStyle("-fx-text-fill: " + textPrimary + ";");

        // Result count
        Label countLabel = new Label(results.size() + " results found");
        countLabel.setFont(Font.font("Poppins", 12));
        countLabel.setStyle("-fx-text-fill: " + textSecondary + ";");

        // Filters
        HBox filters = createFilters();

        top.getChildren().addAll(titleLabel, countLabel, filters);

        // Center: Results
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        resultsContainer = new VBox(10);
        resultsContainer.setPadding(new Insets(10, 0, 10, 0));
        displayResults(results);

        scrollPane.setContent(resultsContainer);

        // Bottom: Close button
        HBox bottom = new HBox();
        bottom.setAlignment(Pos.CENTER_RIGHT);
        bottom.setPadding(new Insets(15, 0, 0, 0));

        Button closeBtn = new Button("Close");
        closeBtn.setStyle(
                "-fx-background-color: " + grayBg + "; " +
                        "-fx-text-fill: " + textPrimary + "; " +
                        "-fx-font-family: 'Poppins'; " +
                        "-fx-font-size: 14px; " +
                        "-fx-padding: 8 20; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand;");
        closeBtn.setOnAction(e -> stage.close());

        bottom.getChildren().add(closeBtn);

        root.setTop(top);
        root.setCenter(scrollPane);
        root.setBottom(bottom);

        // Apply dark class if needed
        if (isDark) {
            root.getStyleClass().add("dark");
        }

        Scene scene = new Scene(root, 700, 600);
        scene.getStylesheets().add(getClass().getResource("/styles/application.css").toExternalForm());

        stage.setScene(scene);
    }

    private HBox createFilters() {
        HBox filters = new HBox(15);
        filters.setAlignment(Pos.CENTER_LEFT);
        filters.setPadding(new Insets(10, 0, 0, 0));

        Label filterLabel = new Label("Filter by type:");
        filterLabel.setFont(Font.font("Poppins", 12));

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

        boolean isDark = ThemeManager.getInstance().isDarkTheme();
        String textSecondary = isDark ? "#b0b0b0" : "#757575";

        if (filteredResults.isEmpty()) {
            Label noResults = new Label("No results found");
            noResults.setFont(Font.font("Poppins", 14));
            noResults.setStyle("-fx-text-fill: " + textSecondary + ";");
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

    private HBox createGroupHeader(String text, Feather icon) {
        boolean isDark = ThemeManager.getInstance().isDarkTheme();
        String textPrimary = isDark ? "#ffffff" : "#212121";

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10, 0, 5, 0));

        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconSize(16);
        fontIcon.setIconColor(Color.web(textPrimary));

        Label label = new Label(text);
        label.setFont(Font.font("Poppins Bold", 14));
        label.setStyle("-fx-text-fill: " + textPrimary + ";");

        header.getChildren().addAll(fontIcon, label);
        return header;
    }

    private VBox createResultItem(SearchService.SearchResult result) {
        boolean isDark = ThemeManager.getInstance().isDarkTheme();
        String textPrimary = isDark ? "#ffffff" : "#212121";
        String textSecondary = isDark ? "#b0b0b0" : "#757575";
        String textHint = isDark ? "#808080" : "#9e9e9e";
        String grayBg = isDark ? "#2d2d2d" : "#f5f5f5";
        String grayLight = isDark ? "#3d3d3d" : "#e8e8e8";

        VBox item = new VBox(5);
        item.setPadding(new Insets(10));
        item.setStyle("-fx-background-color: " + grayBg + "; -fx-background-radius: 8; -fx-cursor: hand;");

        // Title
        Label titleLabel = new Label(result.title != null ? result.title : "Untitled");
        titleLabel.setFont(Font.font("Poppins Bold", 13));
        titleLabel.setStyle("-fx-text-fill: " + textPrimary + ";");

        // Snippet
        Label snippetLabel = new Label(result.snippet);
        snippetLabel.setFont(Font.font("Poppins", 11));
        snippetLabel.setStyle("-fx-text-fill: " + textSecondary + ";");
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
            priorityLabel.setFont(Font.font("Poppins", 10));
            priorityLabel.setStyle("-fx-text-fill: " + textHint + ";");
            metadata.getChildren().add(priorityLabel);
        }

        if (result.status != null) {
            Label statusLabel = new Label("Status: " + result.status);
            statusLabel.setFont(Font.font("Poppins", 10));
            statusLabel.setStyle("-fx-text-fill: " + textHint + ";");
            metadata.getChildren().add(statusLabel);
        }

        if (result.region != null && !result.region.isEmpty()) {
            Label regionLabel = new Label("Region: " + result.region);
            regionLabel.setFont(Font.font("Poppins", 10));
            regionLabel.setStyle("-fx-text-fill: " + textHint + ";");
            metadata.getChildren().add(regionLabel);
        }

        item.getChildren().addAll(titleLabel, snippetLabel, metadata);

        // Hover effect
        item.setOnMouseEntered(
                e -> item.setStyle(
                        "-fx-background-color: " + grayLight + "; -fx-background-radius: 8; -fx-cursor: hand;"));
        item.setOnMouseExited(
                e -> item
                        .setStyle("-fx-background-color: " + grayBg + "; -fx-background-radius: 8; -fx-cursor: hand;"));

        // Click handler
        item.setOnMouseClicked(e -> {
            if (onResultSelected != null) {
                onResultSelected.accept(result);
                stage.close();
            }
        });

        return item;
    }

    private Label createTypeBadge(String type) {
        Label badge = new Label(type.toUpperCase());
        badge.setFont(Font.font("Poppins Bold", 9));
        badge.setPadding(new Insets(3, 8, 3, 8));
        badge.setStyle("-fx-background-radius: 3; -fx-text-fill: white;");

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

    public void show() {
        stage.show();
    }
}
