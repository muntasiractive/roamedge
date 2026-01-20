package com.roam.view;

import com.roam.controller.WikiController;
import com.roam.model.Wiki;
import com.roam.util.AnimationUtils;
import com.roam.view.components.WikiNoteCard;
import com.roam.view.components.WikiNoteModal;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Card-style wiki view with grid layout.
 * <p>
 * This view presents wiki notes in a Google Keep-style card layout with support
 * for
 * pinned notes displayed prominently at the top and other notes below. The view
 * provides quick note creation, search functionality, and modal editing
 * capabilities.
 * </p>
 * <p>
 * Features include:
 * </p>
 * <ul>
 * <li>Pinned and unpinned wiki sections</li>
 * <li>Quick wiki creation input field</li>
 * <li>Search and filtering capabilities</li>
 * <li>Modal-based wiki editing</li>
 * <li>Empty state handling with helpful prompts</li>
 * </ul>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 * @see com.roam.view.components.WikiNoteCard
 * @see com.roam.view.components.WikiNoteModal
 */
public class WikiKeepView extends StackPane {

    private static final Logger logger = LoggerFactory.getLogger(WikiKeepView.class);

    private final WikiController controller;

    // UI Components
    private TextField searchField;
    private TextField quickWikiField;
    private FlowPane pinnedWikisPane;
    private FlowPane otherWikisPane;
    private VBox pinnedSection;
    private VBox othersSection;
    private VBox emptyStateContainer;
    private VBox wikisContainer;
    private StackPane modalOverlay;
    private WikiNoteModal wikiModal;
    private VBox mainContent;

    public WikiKeepView(WikiController controller) {
        this.controller = controller;
        initialize();
        loadWikis();
    }

    private void initialize() {
        getStyleClass().add("wiki-keep-view");
        setStyle("-fx-background-color: -roam-bg-primary;");

        // Main content container
        mainContent = new VBox();
        mainContent.setStyle("-fx-background-color: -roam-bg-primary;");
        mainContent.setPadding(new Insets(24, 40, 24, 40));
        mainContent.setSpacing(24);
        mainContent.setAlignment(Pos.TOP_CENTER);

        // Search bar
        HBox searchBar = createSearchBar();

        // Quick wiki input
        HBox quickWikiBar = createquickWikiBar();

        // Wikis container with scroll
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        wikisContainer = new VBox(24);
        wikisContainer.setAlignment(Pos.TOP_CENTER);
        wikisContainer.setPadding(new Insets(16, 0, 16, 0));
        VBox.setVgrow(wikisContainer, Priority.ALWAYS);

        // Pinned section
        pinnedSection = createSection("PINNED");
        pinnedWikisPane = createWikisFlowPane();
        pinnedSection.getChildren().add(pinnedWikisPane);

        // Others section
        othersSection = createSection("OTHERS");
        otherWikisPane = createWikisFlowPane();
        othersSection.getChildren().add(otherWikisPane);

        // Empty state container (centered)
        emptyStateContainer = createEmptyStateContainer();
        emptyStateContainer.setVisible(false);
        emptyStateContainer.setManaged(false);

        wikisContainer.getChildren().addAll(pinnedSection, othersSection, emptyStateContainer);
        scrollPane.setContent(wikisContainer);

        mainContent.getChildren().addAll(searchBar, quickWikiBar, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // Modal overlay (initially hidden) - transparent background for drop shadow
        // effect
        modalOverlay = new StackPane();
        modalOverlay.setStyle("-fx-background-color: transparent;");
        modalOverlay.setVisible(false);
        modalOverlay.setManaged(false);
        modalOverlay.setOnMouseClicked(e -> {
            if (e.getTarget() == modalOverlay) {
                closeModal();
            }
        });

        // Add main content and overlay to root StackPane
        getChildren().addAll(mainContent, modalOverlay);

        // Animate on load
        Platform.runLater(() -> AnimationUtils.fadeIn(this, Duration.millis(300)));
    }

    private HBox createSearchBar() {
        HBox searchBar = new HBox();
        searchBar.setAlignment(Pos.CENTER);
        searchBar.setMaxWidth(600);

        searchField = new TextField();
        searchField.setPromptText("Search your wikis");
        searchField.getStyleClass().add("wiki-search-field");
        searchField.setPrefHeight(48);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        FontIcon searchIcon = new FontIcon(Feather.SEARCH);
        searchIcon.setIconSize(18);
        searchIcon.setStyle("-fx-icon-color: -roam-text-tertiary;");

        StackPane searchContainer = new StackPane();
        searchContainer.getStyleClass().add("wiki-search-container");
        searchContainer.getChildren().addAll(searchField, searchIcon);
        StackPane.setAlignment(searchIcon, Pos.CENTER_LEFT);
        StackPane.setMargin(searchIcon, new Insets(0, 0, 0, 16));
        searchField.setPadding(new Insets(0, 16, 0, 48));
        HBox.setHgrow(searchContainer, Priority.ALWAYS);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterWikis(newVal));

        searchBar.getChildren().add(searchContainer);
        return searchBar;
    }

    private HBox createquickWikiBar() {
        HBox quickWikiBar = new HBox(12);
        quickWikiBar.setAlignment(Pos.CENTER);
        quickWikiBar.setMaxWidth(600);
        quickWikiBar.getStyleClass().add("wiki-quick-note-bar");
        quickWikiBar.setPadding(new Insets(12, 16, 12, 16));

        FontIcon plusIcon = new FontIcon(Feather.PLUS);
        plusIcon.setIconSize(18);
        plusIcon.setStyle("-fx-icon-color: -roam-text-secondary;");

        quickWikiField = new TextField();
        quickWikiField.setPromptText("Create a wiki...");
        quickWikiField.getStyleClass().add("wiki-quick-note-field");
        HBox.setHgrow(quickWikiField, Priority.ALWAYS);

        quickWikiField.setOnAction(e -> createquickWiki());

        quickWikiBar.getChildren().addAll(plusIcon, quickWikiField);

        // Click on the bar to focus input
        quickWikiBar.setOnMouseClicked(e -> quickWikiField.requestFocus());

        return quickWikiBar;
    }

    private VBox createSection(String title) {
        VBox section = new VBox(12);
        section.setAlignment(Pos.TOP_LEFT);
        section.setMaxWidth(Double.MAX_VALUE);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("wiki-section-title");
        titleLabel.setStyle(
                "-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: -roam-text-tertiary; -fx-padding: 0 0 0 8;");

        section.getChildren().add(titleLabel);
        return section;
    }

    private FlowPane createWikisFlowPane() {
        FlowPane flowPane = new FlowPane();
        flowPane.setHgap(16);
        flowPane.setVgap(16);
        flowPane.setAlignment(Pos.TOP_LEFT);
        flowPane.setPrefWrapLength(900);
        return flowPane;
    }

    public void loadWikis() {
        List<Wiki> allWikis = controller.loadAllWikis();

        List<Wiki> pinned = allWikis.stream()
                .filter(w -> Boolean.TRUE.equals(w.getIsFavorite()))
                .collect(Collectors.toList());

        List<Wiki> others = allWikis.stream()
                .filter(w -> !Boolean.TRUE.equals(w.getIsFavorite()))
                .collect(Collectors.toList());

        displayWikis(pinned, others);
    }

    private void displayWikis(List<Wiki> pinned, List<Wiki> others) {
        pinnedWikisPane.getChildren().clear();
        otherWikisPane.getChildren().clear();

        // Show/hide pinned section based on content
        pinnedSection.setVisible(!pinned.isEmpty());
        pinnedSection.setManaged(!pinned.isEmpty());

        for (Wiki wiki : pinned) {
            WikiNoteCard card = createWikiCard(wiki);
            pinnedWikisPane.getChildren().add(card);
        }

        for (Wiki wiki : others) {
            WikiNoteCard card = createWikiCard(wiki);
            otherWikisPane.getChildren().add(card);
        }

        // Show empty state if no wikis, otherwise show othersSection
        if (pinned.isEmpty() && others.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
            othersSection.setVisible(true);
            othersSection.setManaged(true);
        }
    }

    private WikiNoteCard createWikiCard(Wiki wiki) {
        WikiNoteCard card = new WikiNoteCard(wiki);

        // Click to open modal
        card.setOnMouseClicked(e -> {
            if (!card.isActionClicked()) {
                openWikiModal(wiki);
            }
        });

        // Card action callbacks
        card.setOnPin(() -> {
            controller.toggleFavorite(wiki);
            loadWikis();
        });

        card.setOnDelete(() -> {
            controller.deleteWiki(wiki);
            loadWikis();
        });

        card.setOnColorChange(color -> {
            wiki.setBackgroundColor(color);
            controller.saveWiki(wiki);
            loadWikis();
        });

        card.setOnRegionChange(region -> {
            wiki.setRegion(region);
            controller.saveWiki(wiki);
            loadWikis();
        });

        return card;
    }

    private VBox createEmptyStateContainer() {
        VBox emptyState = new VBox(16);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPadding(new Insets(60));
        emptyState.setMaxWidth(500);
        emptyState.setMinHeight(300);
        emptyState.getStyleClass().add("empty-state");
        VBox.setVgrow(emptyState, Priority.ALWAYS);

        FontIcon icon = new FontIcon(Feather.EDIT_3);
        icon.setIconSize(72);
        icon.getStyleClass().add("icon");

        Label title = new Label("No wikis yet");
        title.getStyleClass().add("title");

        Label label = new Label("Start by creating a wiki above!");
        label.getStyleClass().add("description");

        emptyState.getChildren().addAll(icon, title, label);
        return emptyState;
    }

    private void showEmptyState() {
        othersSection.setVisible(false);
        othersSection.setManaged(false);
        emptyStateContainer.setVisible(true);
        emptyStateContainer.setManaged(true);
    }

    private void hideEmptyState() {
        emptyStateContainer.setVisible(false);
        emptyStateContainer.setManaged(false);
    }

    private void createquickWiki() {
        String text = quickWikiField.getText().trim();
        if (text.isEmpty()) {
            // Open modal for new wiki
            openWikiModal(null);
        } else {
            // Create wiki with the text as title
            Wiki wiki = controller.createNewWiki();
            wiki.setTitle(text);
            controller.saveWiki(wiki);
            quickWikiField.clear();
            loadWikis();
        }
    }

    private void openWikiModal(Wiki wiki) {
        if (wiki == null) {
            wiki = controller.createNewWiki();
        }

        wikiModal = new WikiNoteModal(wiki, controller);
        wikiModal.setOnClose(() -> {
            closeModal();
            loadWikis();
        });

        modalOverlay.getChildren().clear();
        modalOverlay.getChildren().add(wikiModal);
        modalOverlay.setVisible(true);
        modalOverlay.setManaged(true);

        // Animate in
        AnimationUtils.fadeIn(modalOverlay, Duration.millis(200));
    }

    private void closeModal() {
        if (wikiModal != null) {
            wikiModal.saveAndClose();
        }
        modalOverlay.setVisible(false);
        modalOverlay.setManaged(false);
        modalOverlay.getChildren().clear();
        wikiModal = null;
    }

    private void filterWikis(String query) {
        if (query == null || query.trim().isEmpty()) {
            loadWikis();
            return;
        }

        String lowerQuery = query.toLowerCase();
        List<Wiki> allWikis = controller.loadAllWikis();

        List<Wiki> filtered = allWikis.stream()
                .filter(w -> {
                    String title = w.getTitle() != null ? w.getTitle().toLowerCase() : "";
                    String content = w.getContent() != null ? w.getContent().toLowerCase() : "";
                    return title.contains(lowerQuery) || content.contains(lowerQuery);
                })
                .collect(Collectors.toList());

        List<Wiki> pinned = filtered.stream()
                .filter(w -> Boolean.TRUE.equals(w.getIsFavorite()))
                .collect(Collectors.toList());

        List<Wiki> others = filtered.stream()
                .filter(w -> !Boolean.TRUE.equals(w.getIsFavorite()))
                .collect(Collectors.toList());

        displayWikis(pinned, others);
    }

    public void refresh() {
        loadWikis();
    }
}
