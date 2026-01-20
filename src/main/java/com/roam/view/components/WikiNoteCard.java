package com.roam.view.components;

import com.roam.model.Wiki;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;
import org.kordamp.ikonli.material2.Material2OutlinedMZ;

import java.util.function.Consumer;

/**
 * Card component for displaying a wiki note preview in a grid layout.
 * <p>
 * This component renders a wiki entry as a compact card showing the title,
 * content preview, and action buttons. It supports customizable background
 * colors (similar to Google Keep), pinning, deletion, and region assignment.
 * The card provides hover-activated actions and click-through to the full
 * wiki modal for editing.
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public class WikiNoteCard extends VBox {

    private static final double CARD_WIDTH = 260;
    private static final double CARD_MIN_HEIGHT = 120;
    private static final double CARD_MAX_HEIGHT = 320;

    // Color options for wikis (Google Keep colors)
    public static final String[] WIKI_COLORS = {
            null, // Default (white)
            "#FFF8E1", // Light Yellow
            "#F1F8E9", // Light Green
            "#E3F2FD", // Light Blue
            "#FCE4EC", // Light Pink
            "#F3E5F5", // Light Purple
            "#FFF3E0", // Light Orange
            "#E8EAF6", // Light Indigo
            "#E0F2F1", // Light Teal
            "#FFEBEE" // Light Red
    };

    private final Wiki wiki;
    private HBox actionsBar;
    private boolean actionClicked = false;

    // Callbacks
    private Runnable onPin;
    private Runnable onDelete;
    private Consumer<String> onColorChange;
    private Consumer<String> onRegionChange;

    public WikiNoteCard(Wiki wiki) {
        this.wiki = wiki;
        initialize();
    }

    private void initialize() {
        getStyleClass().add("wiki-note-card");
        setPrefWidth(CARD_WIDTH);
        setMinHeight(CARD_MIN_HEIGHT);
        setMaxHeight(CARD_MAX_HEIGHT);
        setPadding(new Insets(16));
        setSpacing(10);

        // Apply background color
        applyBackgroundColor();

        // Pin indicator (if pinned)
        HBox topRow = new HBox();
        topRow.setAlignment(Pos.TOP_RIGHT);
        if (Boolean.TRUE.equals(wiki.getIsFavorite())) {
            FontIcon pinIcon = new FontIcon(Feather.BOOKMARK);
            pinIcon.setIconSize(14);
            pinIcon.setStyle("-fx-icon-color: #F59E0B;");
            topRow.getChildren().add(pinIcon);
        }

        // Title
        Label titleLabel = new Label(
                wiki.getTitle() != null && !wiki.getTitle().isEmpty() ? wiki.getTitle() : "Untitled");
        titleLabel.getStyleClass().add("wiki-note-card-title");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        titleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: 600; -fx-text-fill: #1F2937;");

        // Content preview
        Label contentLabel = new Label(getContentPreview());
        contentLabel.getStyleClass().add("wiki-note-card-content");
        contentLabel.setWrapText(true);
        contentLabel.setMaxWidth(Double.MAX_VALUE);
        contentLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7280; -fx-line-spacing: 2;");
        VBox.setVgrow(contentLabel, Priority.ALWAYS);

        // Region badge (if exists)
        HBox badgeRow = new HBox(6);
        badgeRow.setAlignment(Pos.CENTER_LEFT);
        if (wiki.getRegion() != null && !wiki.getRegion().isEmpty()) {
            Label regionBadge = new Label(wiki.getRegion());
            regionBadge.setStyle("-fx-font-size: 11px; -fx-padding: 3 10; -fx-background-color: #E0E7FF; " +
                    "-fx-background-radius: 12; -fx-text-fill: #4338CA;");
            badgeRow.getChildren().add(regionBadge);
        }

        // Actions bar (shown on hover)
        actionsBar = createActionsBar();
        actionsBar.setVisible(false);
        actionsBar.setManaged(false);

        // Add components
        getChildren().addAll(topRow, titleLabel, contentLabel, badgeRow, actionsBar);

        // Hover effects
        setOnMouseEntered(e -> showActions());
        setOnMouseExited(e -> hideActions());
    }

    private void applyBackgroundColor() {
        String bgColor = wiki.getBackgroundColor();
        String baseStyle = "-fx-background-radius: 12; -fx-border-radius: 12; " +
                "-fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 6, 0, 0, 2);";

        if (bgColor != null && !bgColor.isEmpty()) {
            setStyle("-fx-background-color: " + bgColor + "; " + baseStyle);
        } else {
            setStyle("-fx-background-color: white; " + baseStyle);
        }
    }

    private String getContentPreview() {
        String content = wiki.getContent();
        if (content == null || content.isEmpty()) {
            return "";
        }
        // Limit preview length
        if (content.length() > 200) {
            return content.substring(0, 200) + "...";
        }
        return content;
    }

    private HBox createActionsBar() {
        HBox bar = new HBox(2);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(10, 0, 0, 0));
        bar.setStyle("-fx-background-color: transparent;");
        bar.setPickOnBounds(false); // Allow clicks to pass through empty areas to the card

        // Pin button
        Button pinBtn = createActionButton(BootstrapIcons.PIN_ANGLE_FILL,
                Boolean.TRUE.equals(wiki.getIsFavorite()) ? "Unpin" : "Pin");
        if (Boolean.TRUE.equals(wiki.getIsFavorite())) {
            ((FontIcon) pinBtn.getGraphic()).setIconColor(Color.web("#F59E0B"));
        }
        pinBtn.setOnAction(e -> {
            actionClicked = true;
            if (onPin != null)
                onPin.run();
        });

        // Color picker button
        Button colorBtn = createActionButton(Material2OutlinedAL.COLOR_LENS, "Background color");
        colorBtn.setOnAction(e -> {
            actionClicked = true;
            showColorPicker(colorBtn);
        });

        // Add banner button
        Button bannerBtn = createActionButton(Material2OutlinedAL.IMAGE, "Add image");
        bannerBtn.setOnAction(e -> {
            actionClicked = true;
            // TODO: Implement banner picker
        });

        // More options menu
        Button moreBtn = createActionButton(Material2OutlinedMZ.MORE_VERT, "More options");
        moreBtn.setOnAction(e -> {
            actionClicked = true;
            showMoreMenu(moreBtn);
        });

        // Spacer - make it mouse transparent so clicks pass through to card
        Region spacer = new Region();
        spacer.setMouseTransparent(true);
        HBox.setHgrow(spacer, Priority.ALWAYS);

        bar.getChildren().addAll(pinBtn, colorBtn, bannerBtn, spacer, moreBtn);
        return bar;
    }

    private Button createActionButton(Ikon icon, String tooltip) {
        Button btn = new Button();
        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconSize(18);
        fontIcon.setIconColor(Color.web("#6B7280"));
        btn.setGraphic(fontIcon);
        btn.setStyle("-fx-background-color: transparent; -fx-background-radius: 50; -fx-padding: 6; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: rgba(0,0,0,0.08); -fx-background-radius: 50; -fx-padding: 6; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: transparent; -fx-background-radius: 50; -fx-padding: 6; -fx-cursor: hand;"));
        // Consume mouse click to prevent it from bubbling up to the card
        btn.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_CLICKED, e -> {
            actionClicked = true;
            e.consume();
        });
        Tooltip.install(btn, new Tooltip(tooltip));
        return btn;
    }

    private void showColorPicker(Button anchor) {
        ContextMenu colorMenu = new ContextMenu();
        colorMenu.getStyleClass().add("wiki-color-picker-menu");

        FlowPane colorPane = new FlowPane();
        colorPane.setHgap(8);
        colorPane.setVgap(8);
        colorPane.setPadding(new Insets(8));
        colorPane.setPrefWidth(160);

        for (String color : WIKI_COLORS) {
            Button colorBtn = new Button();
            colorBtn.setPrefSize(24, 24);
            colorBtn.setMinSize(24, 24);
            colorBtn.setMaxSize(24, 24);

            String bgStyle = color != null ? color : "white";
            colorBtn.setStyle("-fx-background-color: " + bgStyle + "; -fx-background-radius: 50; " +
                    "-fx-border-color: #d4d4d8; -fx-border-width: 1; -fx-border-radius: 50; -fx-cursor: hand;");

            // Add checkmark if this is the current color
            if ((color == null && wiki.getBackgroundColor() == null) ||
                    (color != null && color.equals(wiki.getBackgroundColor()))) {
                FontIcon checkIcon = new FontIcon(Feather.CHECK);
                checkIcon.setIconSize(12);
                colorBtn.setGraphic(checkIcon);
            }

            final String selectedColor = color;
            colorBtn.setOnAction(e -> {
                colorMenu.hide();
                if (onColorChange != null) {
                    onColorChange.accept(selectedColor);
                }
            });

            colorPane.getChildren().add(colorBtn);
        }

        CustomMenuItem colorItem = new CustomMenuItem(colorPane);
        colorItem.setHideOnClick(false);
        colorMenu.getItems().add(colorItem);

        colorMenu.show(anchor, javafx.geometry.Side.BOTTOM, 0, 0);
    }

    private void showMoreMenu(Button anchor) {
        ContextMenu menu = new ContextMenu();

        MenuItem deleteItem = new MenuItem("Delete wiki");
        FontIcon deleteIcon = new FontIcon(Feather.TRASH_2);
        deleteIcon.setIconSize(14);
        deleteItem.setGraphic(deleteIcon);
        deleteItem.setOnAction(e -> {
            if (onDelete != null) {
                onDelete.run();
            }
        });

        MenuItem duplicateItem = new MenuItem("Duplicate");
        FontIcon duplicateIcon = new FontIcon(Feather.COPY);
        duplicateIcon.setIconSize(14);
        duplicateItem.setGraphic(duplicateIcon);

        MenuItem regionItem = new MenuItem("Set region");
        FontIcon regionIcon = new FontIcon(Feather.MAP_PIN);
        regionIcon.setIconSize(14);
        regionItem.setGraphic(regionIcon);
        regionItem.setOnAction(e -> showRegionPicker(anchor));

        MenuItem exportItem = new MenuItem("Export");
        FontIcon exportIcon = new FontIcon(Feather.DOWNLOAD);
        exportIcon.setIconSize(14);
        exportItem.setGraphic(exportIcon);

        SeparatorMenuItem separator = new SeparatorMenuItem();

        menu.getItems().addAll(regionItem, duplicateItem, exportItem, separator, deleteItem);
        menu.show(anchor, javafx.geometry.Side.BOTTOM, 0, 0);
    }

    private void showRegionPicker(Button anchor) {
        ContextMenu menu = new ContextMenu();

        String[] regions = { "Work", "Personal", "Finance", "Health", "Learning", "Operations", "Ideas" };

        MenuItem clearItem = new MenuItem("No region");
        clearItem.setOnAction(e -> {
            if (onRegionChange != null)
                onRegionChange.accept(null);
        });
        menu.getItems().add(clearItem);
        menu.getItems().add(new SeparatorMenuItem());

        for (String region : regions) {
            MenuItem item = new MenuItem(region);
            if (region.equals(wiki.getRegion())) {
                FontIcon checkIcon = new FontIcon(Feather.CHECK);
                checkIcon.setIconSize(12);
                item.setGraphic(checkIcon);
            }
            item.setOnAction(e -> {
                if (onRegionChange != null)
                    onRegionChange.accept(region);
            });
            menu.getItems().add(item);
        }

        menu.show(anchor, javafx.geometry.Side.BOTTOM, 0, 0);
    }

    private void showActions() {
        actionsBar.setVisible(true);
        actionsBar.setManaged(true);
        // Add subtle elevation on hover
        String bgColor = wiki.getBackgroundColor();
        String color = bgColor != null && !bgColor.isEmpty() ? bgColor : "white";
        setStyle("-fx-background-color: " + color + "; -fx-background-radius: 12; -fx-border-radius: 12; " +
                "-fx-border-color: #D1D5DB; -fx-border-width: 1; -fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 12, 0, 0, 4);");
    }

    private void hideActions() {
        actionsBar.setVisible(false);
        actionsBar.setManaged(false);
        actionClicked = false;
        // Reset styling
        applyBackgroundColor();
    }

    public boolean isActionClicked() {
        boolean clicked = actionClicked;
        actionClicked = false;
        return clicked;
    }

    // Callback setters
    public void setOnPin(Runnable onPin) {
        this.onPin = onPin;
    }

    public void setOnDelete(Runnable onDelete) {
        this.onDelete = onDelete;
    }

    public void setOnColorChange(Consumer<String> onColorChange) {
        this.onColorChange = onColorChange;
    }

    public void setOnRegionChange(Consumer<String> onRegionChange) {
        this.onRegionChange = onRegionChange;
    }
}
