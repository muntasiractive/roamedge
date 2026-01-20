package com.roam.layout;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Represents the sidebar component with navigation buttons.
 * Manages sidebar creation and button states.
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public class SidebarComponent {

    private static final Logger logger = LoggerFactory.getLogger(SidebarComponent.class);

    // Sidebar dimensions
    private static final double SIDEBAR_WIDTH_EXPANDED = 250;
    private static final double SIDEBAR_WIDTH_COLLAPSED = 80;

    private final VBox sidebar;
    private final Map<String, Button> navigationButtons;

    // UI Components
    private HBox headerBox;
    private VBox headerCol;
    private Button menuBtn;
    private StackPane searchGroup;
    private TextField searchInput;
    private Region spacer;

    private boolean collapsed = false;

    // Callbacks
    private final Consumer<String> onNavigate;
    private final Consumer<String> onSearch;
    private final Runnable onToggle;

    /**
     * Creates a new sidebar component.
     * 
     * @param onNavigate   Callback when navigation button is clicked (receives view
     *                     type)
     * @param onSearch     Callback when search is performed
     * @param onToggle     Ignored (Toggle removed)
     * @param resizeHandle Ignored (Fixed width)
     */
    public SidebarComponent(Consumer<String> onNavigate, Consumer<String> onSearch,
            Runnable onToggle, Region resizeHandle) {
        this.onNavigate = onNavigate;
        this.onSearch = onSearch;
        this.onToggle = onToggle;
        this.navigationButtons = new HashMap<>();

        // Create sidebar UI components
        sidebar = new VBox(10);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(SIDEBAR_WIDTH_EXPANDED);
        sidebar.setMinWidth(SIDEBAR_WIDTH_EXPANDED);
        sidebar.setMaxWidth(SIDEBAR_WIDTH_EXPANDED);
        sidebar.setPadding(new Insets(20));
        sidebar.setStyle("-fx-background-color: -color-bg-default;");

        // Create header components
        createHeaderComponents();

        // Create search component
        createSearchComponent();

        // Create navigation buttons
        Button operationsBtn = createNavButton("Operations", Feather.ACTIVITY, "operations", true);
        Button tasksBtn = createNavButton("Tasks", Feather.CHECK_SQUARE, "tasks", false);
        Button calendarBtn = createNavButton("Calendar", Feather.CALENDAR, "calendar", false);
        Button journalBtn = createNavButton("Journal", Feather.BOOK, "journal", false);
        Button wikiBtn = createNavButton("Wiki", Feather.BOOK_OPEN, "wiki", false);

        // Store references
        navigationButtons.put("operations", operationsBtn);
        navigationButtons.put("calendar", calendarBtn);
        navigationButtons.put("tasks", tasksBtn);
        navigationButtons.put("wiki", wikiBtn);
        navigationButtons.put("journal", journalBtn);

        // Spacer to push content to top
        spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Initial state
        updateSidebarState();

        logger.debug("Sidebar component created");
    }

    private void createHeaderComponents() {
        // Menu button
        menuBtn = new Button(null, new FontIcon(Feather.MENU));
        menuBtn.getStyleClass().add("sidebar-button");
        menuBtn.setAlignment(Pos.CENTER);
        menuBtn.setOnAction(e -> toggle());

        // Horizontal Header (Expanded)
        headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(headerBox, new Insets(0, 0, 20, 0));

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        headerBox.getChildren().addAll(headerSpacer, menuBtn);

        // Vertical Header (Collapsed)
        headerCol = new VBox(15);
        headerCol.setAlignment(Pos.CENTER);
        VBox.setMargin(headerCol, new Insets(0, 0, 20, 0));
        // Buttons will be moved between containers in updateSidebarState
    }

    private void toggle() {
        collapsed = !collapsed;
        updateSidebarState();
        if (onToggle != null) {
            onToggle.run();
        }
    }

    private void updateSidebarState() {
        sidebar.getChildren().clear();
        headerBox.getChildren().clear();
        headerCol.getChildren().clear();

        if (collapsed) {
            sidebar.setPrefWidth(SIDEBAR_WIDTH_COLLAPSED);
            sidebar.setMinWidth(SIDEBAR_WIDTH_COLLAPSED);
            sidebar.setMaxWidth(SIDEBAR_WIDTH_COLLAPSED);
            sidebar.setPadding(new Insets(20, 10, 20, 10));

            // Stack header buttons vertically
            headerCol.getChildren().add(menuBtn);
            sidebar.getChildren().add(headerCol);

            // Hide search in collapsed mode
            searchGroup.setVisible(false);
            searchGroup.setManaged(false);

            // Update nav buttons
            for (Button btn : navigationButtons.values()) {
                btn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                btn.setAlignment(Pos.CENTER);
            }

        } else {
            sidebar.setPrefWidth(SIDEBAR_WIDTH_EXPANDED);
            sidebar.setMinWidth(SIDEBAR_WIDTH_EXPANDED);
            sidebar.setMaxWidth(SIDEBAR_WIDTH_EXPANDED);
            sidebar.setPadding(new Insets(20));

            // Horizontal header
            Region headerSpacer = new Region();
            HBox.setHgrow(headerSpacer, Priority.ALWAYS);
            headerBox.getChildren().addAll(headerSpacer, menuBtn);
            sidebar.getChildren().add(headerBox);

            // Show search in expanded mode
            searchGroup.setVisible(true);
            searchGroup.setManaged(true);

            // Update nav buttons
            for (Button btn : navigationButtons.values()) {
                btn.setContentDisplay(ContentDisplay.LEFT);
                btn.setAlignment(Pos.CENTER_LEFT);
            }
        }

        // Add common elements
        sidebar.getChildren().addAll(
                searchGroup,
                navigationButtons.get("operations"),
                navigationButtons.get("tasks"),
                navigationButtons.get("calendar"),
                navigationButtons.get("journal"),
                navigationButtons.get("wiki"),
                spacer);
    }

    private void createSearchComponent() {
        searchGroup = new StackPane();
        searchGroup.getStyleClass().add("sidebar-search-container");
        searchGroup.setAlignment(Pos.CENTER_LEFT);

        searchInput = new TextField();
        searchInput.setPromptText("Search...");
        searchInput.getStyleClass().add("sidebar-search-field");

        // Handle search action
        searchInput.setOnAction(e -> {
            if (onSearch != null) {
                onSearch.accept(searchInput.getText());
            }
        });

        searchGroup.getChildren().addAll(searchInput);
    }

    /**
     * Creates a navigation button.
     */
    private Button createNavButton(String text, Feather iconType, String viewType, boolean active) {
        Button button = new Button(text);
        button.setGraphic(new FontIcon(iconType));
        button.getStyleClass().add("sidebar-button");
        button.setAlignment(Pos.CENTER_LEFT);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setGraphicTextGap(20);

        // Set button action
        button.setOnAction(e -> {
            if (onNavigate != null) {
                logger.debug("Navigation button clicked: {}", viewType);
                onNavigate.accept(viewType);
                setActiveButton(viewType);
            }
        });

        // Apply initial state
        updateButtonState(button, active);

        return button;
    }

    /**
     * Updates the visual state of a navigation button.
     */
    private void updateButtonState(Button button, boolean active) {
        if (active) {
            if (!button.getStyleClass().contains("selected")) {
                button.getStyleClass().add("selected");
            }
        } else {
            button.getStyleClass().remove("selected");
        }
    }

    /**
     * Sets the active navigation button by view type.
     */
    public void setActiveButton(String viewType) {
        logger.debug("Setting active button: {}", viewType);

        // Deactivate all buttons
        for (Button btn : navigationButtons.values()) {
            updateButtonState(btn, false);
        }

        // Activate the selected button
        Button activeButton = navigationButtons.get(viewType);
        if (activeButton != null) {
            updateButtonState(activeButton, true);
        }
    }

    /**
     * Gets the sidebar VBox for adding to layout.
     */
    public VBox getSidebar() {
        return sidebar;
    }

    /**
     * Gets the navigation buttons map.
     */
    public Map<String, Button> getNavigationButtons() {
        return navigationButtons;
    }

    /**
     * Updates button widths (No-op for fixed sidebar).
     */
    public void updateButtonWidths(double sidebarWidth) {
        // No-op
    }
}
