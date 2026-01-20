package com.roam.view.components;

import com.roam.controller.WikiController;
import com.roam.model.Wiki;
import com.roam.model.Operation;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * A sidebar component for the wiki view displaying favorites, recent wikis, and
 * operation-related wikis.
 * <p>
 * This sidebar provides quick navigation to favorite wikis, recently accessed
 * wikis,
 * and wikis associated with specific operations. Each section can be expanded
 * or collapsed.
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public class WikiSidebar extends VBox {

    private final WikiController controller;

    private VBox favoritesContent;
    private VBox recentContent;
    private VBox operationWikisContent;
    private VBox operationWikisSection;

    public WikiSidebar(WikiController controller) {
        this.controller = controller;

        configureSidebar();
        buildSections();
    }

    private void configureSidebar() {
        setMinWidth(280);
        setPrefWidth(280);
        setMaxWidth(280);
        getStyleClass().add("wiki-sidebar");
    }

    private void buildSections() {
        VBox favoritesSection = createFavoritesSection();
        VBox recentSection = createRecentSection();
        operationWikisSection = createOperationWikisSection();

        // Initially hide operation wikis section
        operationWikisSection.setVisible(false);
        operationWikisSection.setManaged(false);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        getChildren().addAll(
                favoritesSection,
                recentSection,
                operationWikisSection,
                spacer);
    }

    private VBox createOperationWikisSection() {
        VBox section = new VBox(10);

        // Header
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        FontIcon icon = new FontIcon(Feather.LIST);
        icon.setIconSize(14);

        Label title = new Label("Operation Wikis");
        title.getStyleClass().add("wiki-sidebar-section-title");

        header.getChildren().addAll(icon, title);

        // Content container
        operationWikisContent = new VBox(5);
        operationWikisContent.setPadding(new Insets(10, 0, 0, 0));

        section.getChildren().addAll(header, operationWikisContent);
        return section;
    }

    private VBox createFavoritesSection() {
        VBox section = new VBox(10);

        // Header
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        FontIcon icon = new FontIcon(Feather.STAR);
        icon.setIconSize(14);

        Label title = new Label("Favorites");
        title.getStyleClass().add("wiki-sidebar-section-title");

        header.getChildren().addAll(icon, title);

        // Content container
        favoritesContent = new VBox(5);
        favoritesContent.setPadding(new Insets(10, 0, 0, 0));

        section.getChildren().addAll(header, favoritesContent);
        return section;
    }

    private VBox createRecentSection() {
        VBox section = new VBox(10);

        // Header
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        FontIcon icon = new FontIcon(Feather.CLOCK);
        icon.setIconSize(14);

        Label title = new Label("Recent");
        title.getStyleClass().add("wiki-sidebar-section-title");

        header.getChildren().addAll(icon, title);

        // Content container
        recentContent = new VBox(5);
        recentContent.setPadding(new Insets(10, 0, 0, 0));

        section.getChildren().addAll(header, recentContent);
        return section;
    }

    private HBox createWikiItem(Wiki wiki) {
        HBox item = new HBox();
        item.setPrefHeight(60);
        item.getStyleClass().add("wiki-sidebar-item");

        VBox content = new VBox(4);
        content.setAlignment(Pos.TOP_LEFT);

        // Title
        Label titleLabel = new Label(wiki.getTitle());
        titleLabel.getStyleClass().add("wiki-sidebar-item-title");
        titleLabel.setMaxWidth(250);

        // Meta info
        HBox meta = new HBox(8);
        meta.setAlignment(Pos.CENTER_LEFT);

        Label timeLabel = new Label(formatRelativeTime(wiki.getUpdatedAt()));
        timeLabel.getStyleClass().add("wiki-sidebar-item-time");

        // Add region if exists (replaces tags)
        if (wiki.getRegion() != null && !wiki.getRegion().isEmpty()) {
            Label regionLabel = new Label(wiki.getRegion());
            regionLabel.getStyleClass().add("wiki-sidebar-item-region");
            meta.getChildren().add(regionLabel);
        }

        meta.getChildren().addAll(timeLabel);

        content.getChildren().addAll(titleLabel, meta);
        item.getChildren().add(content);

        // Click action
        item.setOnMouseClicked(e -> {
            controller.setCurrentWiki(wiki);
        });

        return item;
    }

    private String formatRelativeTime(LocalDateTime dateTime) {
        if (dateTime == null)
            return "";

        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(dateTime, now);

        if (minutes < 60) {
            return minutes + "m ago";
        }

        long hours = ChronoUnit.HOURS.between(dateTime, now);
        if (hours < 24) {
            return hours + "h ago";
        }

        long days = ChronoUnit.DAYS.between(dateTime, now);
        if (days < 7) {
            return days + "d ago";
        }

        return dateTime.format(DateTimeFormatter.ofPattern("MMM d"));
    }

    // Public refresh methods
    public void refreshFavorites() {
        favoritesContent.getChildren().clear();
        List<Wiki> favorites = controller.loadFavoriteWikis();

        if (favorites.isEmpty()) {
            Label emptyLabel = new Label("No favorites yet");
            emptyLabel.getStyleClass().add("wiki-sidebar-empty-label");
            favoritesContent.getChildren().add(emptyLabel);
        } else {
            int limit = Math.min(favorites.size(), 5);
            for (int i = 0; i < limit; i++) {
                favoritesContent.getChildren().add(createWikiItem(favorites.get(i)));
            }
        }
    }

    public void refreshRecent() {
        recentContent.getChildren().clear();
        List<Wiki> recent = controller.loadRecentWikis(10);

        if (recent.isEmpty()) {
            Label emptyLabel = new Label("No wikis yet");
            emptyLabel.getStyleClass().add("wiki-sidebar-empty-label");
            recentContent.getChildren().add(emptyLabel);
        } else {
            for (Wiki wiki : recent) {
                recentContent.getChildren().add(createWikiItem(wiki));
            }
        }
    }

    public void refreshAll() {
        refreshFavorites();
        refreshRecent();
    }

    public void switchToOperationMode(Operation operation) {
        // Hide standard sections
        favoritesContent.getParent().setVisible(false);
        favoritesContent.getParent().setManaged(false);
        recentContent.getParent().setVisible(false);
        recentContent.getParent().setManaged(false);

        // Show operation wikis section
        operationWikisSection.setVisible(true);
        operationWikisSection.setManaged(true);

        // Update title
        if (!operationWikisSection.getChildren().isEmpty()
                && operationWikisSection.getChildren().get(0) instanceof HBox) {
            HBox header = (HBox) operationWikisSection.getChildren().get(0);
            if (header.getChildren().size() > 1 && header.getChildren().get(1) instanceof Label) {
                ((Label) header.getChildren().get(1)).setText(operation.getName());
            }
        }

        refreshOperationWikis(operation);
    }

    public void refreshOperationWikis(Operation operation) {
        operationWikisContent.getChildren().clear();
        List<Wiki> wikis = controller.loadWikisForOperation(operation);

        if (wikis.isEmpty()) {
            Label emptyLabel = new Label("No wikis yet");
            emptyLabel.getStyleClass().add("wiki-sidebar-empty-label");
            operationWikisContent.getChildren().add(emptyLabel);
        } else {
            for (Wiki wiki : wikis) {
                operationWikisContent.getChildren().add(createWikiItem(wiki));
            }
        }
    }
}
