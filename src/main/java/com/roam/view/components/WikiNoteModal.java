package com.roam.view.components;

import com.roam.controller.WikiController;
import com.roam.model.Wiki;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.FileChooser;
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;
import org.kordamp.ikonli.material2.Material2OutlinedMZ;
import org.kordamp.ikonli.Ikon;

import java.io.File;
import java.util.List;

/**
 * Modal dialog for viewing and editing wiki notes.
 * <p>
 * This component provides a full-featured editing interface for wiki entries,
 * supporting title editing, rich content input, banner image selection,
 * background color customization, and region assignment. The modal includes
 * an actions bar with options for pinning, color selection, file attachments,
 * and deletion. Changes are persisted through the WikiController.
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public class WikiNoteModal extends VBox {

    private static final double MODAL_WIDTH = 620;
    private static final double MODAL_MAX_HEIGHT = 550;
    private static final double BANNER_HEIGHT = 120;

    private final Wiki wiki;
    private final WikiController controller;

    private TextField titleField;
    private TextArea contentArea;
    private HBox actionsBar;
    private StackPane bannerContainer;
    private ImageView bannerView;
    private Label regionBadge;

    private Runnable onClose;
    private boolean deleted = false;

    public WikiNoteModal(Wiki wiki, WikiController controller) {
        this.wiki = wiki;
        this.controller = controller;
        initialize();
    }

    private void initialize() {
        getStyleClass().add("wiki-note-modal");
        setMaxWidth(MODAL_WIDTH);
        setMaxHeight(MODAL_MAX_HEIGHT);
        setPadding(new Insets(0));
        setSpacing(0);
        setAlignment(Pos.TOP_CENTER);

        // Apply background color
        applyBackgroundColor();

        // Banner container (shown at top if banner exists)
        bannerContainer = createBannerContainer();

        // Header with close button
        HBox header = createHeader();

        // Title field
        titleField = new TextField(wiki.getTitle());
        titleField.setPromptText("Title");
        titleField.getStyleClass().add("wiki-modal-title-field");

        // Content area
        contentArea = new TextArea(wiki.getContent());
        contentArea.setPromptText("Take a wiki...");
        contentArea.getStyleClass().add("wiki-modal-content-area");
        contentArea.setWrapText(true);
        VBox.setVgrow(contentArea, Priority.ALWAYS);

        // Region badge (shown below content if region is set)
        regionBadge = createRegionBadge();

        // Actions bar
        actionsBar = createActionsBar();

        // Content container
        VBox contentBox = new VBox(8);
        contentBox.setPadding(new Insets(16));
        contentBox.getChildren().addAll(titleField, contentArea, regionBadge);
        VBox.setVgrow(contentBox, Priority.ALWAYS);

        getChildren().addAll(bannerContainer, header, contentBox, actionsBar);

        // Auto-save on typing
        titleField.textProperty().addListener((obs, oldVal, newVal) -> scheduleAutoSave());
        contentArea.textProperty().addListener((obs, oldVal, newVal) -> scheduleAutoSave());
    }

    private void applyBackgroundColor() {
        String bgColor = wiki.getBackgroundColor();
        String colorStyle = bgColor != null && !bgColor.isEmpty() ? bgColor : "white";
        setStyle("-fx-background-color: " + colorStyle + "; -fx-background-radius: 16; " +
                "-fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 40, 0, 0, 10);");
    }

    private StackPane createBannerContainer() {
        StackPane container = new StackPane();
        container.setMinHeight(0);
        container.setMaxHeight(BANNER_HEIGHT);
        container.setAlignment(Pos.TOP_CENTER);

        // Banner ImageView
        bannerView = new ImageView();
        bannerView.setFitWidth(MODAL_WIDTH);
        bannerView.setFitHeight(BANNER_HEIGHT);
        bannerView.setPreserveRatio(false);
        bannerView.setSmooth(true);

        // Remove banner button (shown on hover)
        Button removeBannerBtn = new Button();
        FontIcon removeIcon = new FontIcon(Material2OutlinedAL.CLOSE);
        removeIcon.setIconSize(16);
        removeIcon.setIconColor(Color.WHITE);
        removeBannerBtn.setGraphic(removeIcon);
        removeBannerBtn.setStyle("-fx-background-color: rgba(0,0,0,0.5); -fx-background-radius: 50; " +
                "-fx-padding: 4; -fx-cursor: hand;");
        removeBannerBtn.setOnMouseEntered(e -> removeBannerBtn.setStyle(
                "-fx-background-color: rgba(0,0,0,0.7); -fx-background-radius: 50; -fx-padding: 4; -fx-cursor: hand;"));
        removeBannerBtn.setOnMouseExited(e -> removeBannerBtn.setStyle(
                "-fx-background-color: rgba(0,0,0,0.5); -fx-background-radius: 50; -fx-padding: 4; -fx-cursor: hand;"));
        removeBannerBtn.setVisible(false);
        removeBannerBtn.setOnAction(e -> handleRemoveBanner());
        StackPane.setAlignment(removeBannerBtn, Pos.TOP_RIGHT);
        StackPane.setMargin(removeBannerBtn, new Insets(8, 8, 0, 0));

        container.getChildren().addAll(bannerView, removeBannerBtn);

        // Show/hide remove button on hover
        container.setOnMouseEntered(e -> {
            if (wiki.getBannerUrl() != null && !wiki.getBannerUrl().isEmpty()) {
                removeBannerBtn.setVisible(true);
            }
        });
        container.setOnMouseExited(e -> removeBannerBtn.setVisible(false));

        // Clip for rounded top corners
        container.setStyle("-fx-background-radius: 16 16 0 0;");

        // Assign to field before calling updateBannerDisplay
        bannerContainer = container;

        // Load existing banner if available
        updateBannerDisplay();

        return container;
    }

    private void updateBannerDisplay() {
        String bannerUrl = wiki.getBannerUrl();
        if (bannerUrl != null && !bannerUrl.isEmpty()) {
            try {
                Image bannerImage = new Image(bannerUrl, MODAL_WIDTH, BANNER_HEIGHT, false, true);
                bannerView.setImage(bannerImage);
                bannerContainer.setMinHeight(BANNER_HEIGHT);
                bannerContainer.setVisible(true);
                bannerContainer.setManaged(true);
            } catch (Exception e) {
                // Invalid image URL
                bannerContainer.setMinHeight(0);
                bannerContainer.setVisible(false);
                bannerContainer.setManaged(false);
            }
        } else {
            bannerView.setImage(null);
            bannerContainer.setMinHeight(0);
            bannerContainer.setVisible(false);
            bannerContainer.setManaged(false);
        }
    }

    private void handleAddBanner() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Banner Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp"));

        File selectedFile = fileChooser.showOpenDialog(getScene().getWindow());
        if (selectedFile != null) {
            String imageUrl = selectedFile.toURI().toString();
            wiki.setBannerUrl(imageUrl);
            updateBannerDisplay();
            scheduleAutoSave();
        }
    }

    private void handleRemoveBanner() {
        wiki.setBannerUrl(null);
        updateBannerDisplay();
        scheduleAutoSave();
    }

    private Label createRegionBadge() {
        Label badge = new Label();
        badge.setVisible(false);
        badge.setManaged(false);
        updateRegionBadge(badge);
        return badge;
    }

    private void updateRegionBadge(Label badge) {
        String region = wiki.getRegion();
        if (region != null && !region.isEmpty()) {
            badge.setText(region);

            // Find region color from database regions
            String regionColor = getRegionColor(region);

            FontIcon locationIcon = new FontIcon(Material2OutlinedAL.LOCATION_ON);
            locationIcon.setIconSize(12);
            locationIcon.setIconColor(Color.web(regionColor));
            badge.setGraphic(locationIcon);

            badge.setStyle("-fx-background-color: " + regionColor + "20; " +
                    "-fx-background-radius: 12; -fx-padding: 4 10; " +
                    "-fx-font-size: 12px; -fx-text-fill: " + regionColor + "; " +
                    "-fx-font-weight: bold;");
            badge.setVisible(true);
            badge.setManaged(true);
        } else {
            badge.setVisible(false);
            badge.setManaged(false);
        }
    }

    private String getRegionColor(String regionName) {
        // Try to get color from database regions
        List<com.roam.model.Region> regions = controller.loadAllRegions();
        for (com.roam.model.Region r : regions) {
            if (r.getName().equals(regionName)) {
                return r.getColor();
            }
        }
        // Default color if not found
        return "#6B7280";
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_RIGHT);
        header.setPadding(new Insets(12, 16, 0, 16));

        // Pin button with custom SVG
        Button pinBtn = createPinButton(Boolean.TRUE.equals(wiki.getIsFavorite()));
        Tooltip.install(pinBtn, new Tooltip(Boolean.TRUE.equals(wiki.getIsFavorite()) ? "Unpin" : "Pin"));
        pinBtn.setOnAction(e -> togglePin());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Close button
        Button closeBtn = createActionButton(Material2OutlinedAL.CLOSE, "Close");
        closeBtn.setOnAction(e -> {
            saveAndClose();
            if (onClose != null)
                onClose.run();
        });

        header.getChildren().addAll(pinBtn, spacer, closeBtn);
        return header;
    }

    private HBox createActionsBar() {
        HBox bar = new HBox(4);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(10, 16, 10, 16));
        bar.setStyle("-fx-border-color: #E5E7EB; -fx-border-width: 1 0 0 0;");

        // Color picker - custom SVG palette icon
        Button colorBtn = createColorPaletteButton();
        Tooltip.install(colorBtn, new Tooltip("Background color"));
        colorBtn.setOnAction(e -> showColorPicker(colorBtn));

        // Image button
        Button bannerBtn = createActionButton(Material2OutlinedAL.IMAGE, "Add image");
        bannerBtn.setOnAction(e -> handleAddBanner());

        // Location/Region button
        Button regionBtn = createActionButton(Material2OutlinedAL.LOCATION_ON, "Set region");
        regionBtn.setOnAction(e -> showRegionPicker(regionBtn));

        // More options
        Button moreBtn = createActionButton(Material2OutlinedMZ.MORE_VERT, "More options");
        moreBtn.setOnAction(e -> showMoreMenu(moreBtn));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Close button
        Button closeTextBtn = new Button("Close");
        closeTextBtn.setStyle("-fx-background-color: #F3F4F6; -fx-background-radius: 8; -fx-padding: 8 16; " +
                "-fx-font-size: 13px; -fx-text-fill: #374151; -fx-cursor: hand;");
        closeTextBtn.setOnMouseEntered(e -> closeTextBtn
                .setStyle("-fx-background-color: #E5E7EB; -fx-background-radius: 8; -fx-padding: 8 16; " +
                        "-fx-font-size: 13px; -fx-text-fill: #374151; -fx-cursor: hand;"));
        closeTextBtn.setOnMouseExited(e -> closeTextBtn
                .setStyle("-fx-background-color: #F3F4F6; -fx-background-radius: 8; -fx-padding: 8 16; " +
                        "-fx-font-size: 13px; -fx-text-fill: #374151; -fx-cursor: hand;"));
        closeTextBtn.setOnAction(e -> {
            saveAndClose();
            if (onClose != null)
                onClose.run();
        });

        bar.getChildren().addAll(colorBtn, bannerBtn, regionBtn, moreBtn, spacer, closeTextBtn);
        return bar;
    }

    private Button createActionButton(Ikon icon, String tooltip) {
        Button btn = new Button();
        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconSize(20);
        fontIcon.setIconColor(Color.web("#5F6368"));
        btn.setGraphic(fontIcon);
        btn.setStyle("-fx-background-color: transparent; -fx-background-radius: 50; -fx-padding: 6; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: rgba(0,0,0,0.08); -fx-background-radius: 50; -fx-padding: 6; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: transparent; -fx-background-radius: 50; -fx-padding: 6; -fx-cursor: hand;"));
        Tooltip.install(btn, new Tooltip(tooltip));
        return btn;
    }

    private Button createColorPaletteButton() {
        Button btn = new Button();

        // Create SVG palette icon
        StackPane iconPane = new StackPane();
        iconPane.setPrefSize(20, 20);

        // Main palette shape
        SVGPath palette = new SVGPath();
        palette.setContent(
                "M5.596 1.93a1.127 1.127 0 0 1-.287.97 4.878 4.878 0 0 0-.372.444c-.112.153-.209.31-.274.464a2.402 2.402 0 0 0-.144.519c-.033.19-.052.388-.062.576a1.131 1.131 0 0 1-.5.88 1.124 1.124 0 0 1-1.003.125 3.13 3.13 0 0 1-.153-.06H2.8a2.597 2.597 0 0 1-1.398-1.423 2.598 2.598 0 0 1 .02-1.994V2.43a2.599 2.599 0 0 1 1.425-1.398c.872-.351 2.543-.25 2.749.898z");
        palette.setFill(Color.web("#9CA3AF"));
        palette.setScaleX(2.8);
        palette.setScaleY(2.8);

        iconPane.getChildren().add(palette);
        btn.setGraphic(iconPane);

        btn.setStyle("-fx-background-color: transparent; -fx-background-radius: 50; -fx-padding: 6; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: rgba(0,0,0,0.08); -fx-background-radius: 50; -fx-padding: 6; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: transparent; -fx-background-radius: 50; -fx-padding: 6; -fx-cursor: hand;"));

        return btn;
    }

    private Button createPinButton(boolean isPinned) {
        Button btn = new Button();
        FontIcon pinIcon = new FontIcon(BootstrapIcons.PIN_ANGLE_FILL);
        pinIcon.setIconSize(20);
        pinIcon.setIconColor(isPinned ? Color.web("#F59E0B") : Color.web("#9CA3AF"));
        btn.setGraphic(pinIcon);
        btn.setStyle("-fx-background-color: transparent; -fx-background-radius: 50; -fx-padding: 6; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: rgba(0,0,0,0.08); -fx-background-radius: 50; -fx-padding: 6; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: transparent; -fx-background-radius: 50; -fx-padding: 6; -fx-cursor: hand;"));
        return btn;
    }

    private void showColorPicker(Button anchor) {
        ContextMenu colorMenu = new ContextMenu();

        FlowPane colorPane = new FlowPane();
        colorPane.setHgap(8);
        colorPane.setVgap(8);
        colorPane.setPadding(new Insets(12));
        colorPane.setPrefWidth(180);

        for (String color : WikiNoteCard.WIKI_COLORS) {
            Button colorBtn = new Button();
            colorBtn.setPrefSize(28, 28);
            colorBtn.setMinSize(28, 28);
            colorBtn.setMaxSize(28, 28);

            String bgStyle = color != null ? color : "white";
            colorBtn.setStyle("-fx-background-color: " + bgStyle + "; -fx-background-radius: 50; " +
                    "-fx-border-color: #d4d4d8; -fx-border-width: 1; -fx-border-radius: 50; -fx-cursor: hand;");

            // Add checkmark if this is the current color
            if ((color == null && wiki.getBackgroundColor() == null) ||
                    (color != null && color.equals(wiki.getBackgroundColor()))) {
                FontIcon checkIcon = new FontIcon(Material2OutlinedAL.CHECK);
                checkIcon.setIconSize(14);
                checkIcon.setIconColor(Color.web("#374151"));
                colorBtn.setGraphic(checkIcon);
            }

            final String selectedColor = color;
            colorBtn.setOnAction(e -> {
                colorMenu.hide();
                wiki.setBackgroundColor(selectedColor);
                applyBackgroundColor();
                scheduleAutoSave();
            });

            colorPane.getChildren().add(colorBtn);
        }

        CustomMenuItem colorItem = new CustomMenuItem(colorPane);
        colorItem.setHideOnClick(false);
        colorMenu.getItems().add(colorItem);

        colorMenu.show(anchor, javafx.geometry.Side.TOP, 0, 0);
    }

    private void showRegionPicker(Button anchor) {
        ContextMenu menu = new ContextMenu();

        // Load regions from database
        List<com.roam.model.Region> regions = controller.loadAllRegions();

        MenuItem clearItem = new MenuItem("No region");
        clearItem.setOnAction(e -> {
            wiki.setRegion(null);
            updateRegionBadge(regionBadge);
            scheduleAutoSave();
        });
        menu.getItems().add(clearItem);
        menu.getItems().add(new SeparatorMenuItem());

        for (com.roam.model.Region region : regions) {
            MenuItem item = new MenuItem(region.getName());

            // Show color indicator
            FontIcon colorIndicator = new FontIcon(Material2OutlinedAL.LENS);
            colorIndicator.setIconSize(12);
            colorIndicator.setIconColor(Color.web(region.getColor()));
            item.setGraphic(colorIndicator);

            // Add checkmark if this is the current region
            if (region.getName().equals(wiki.getRegion())) {
                FontIcon checkIcon = new FontIcon(Material2OutlinedAL.CHECK);
                checkIcon.setIconSize(14);
                checkIcon.setIconColor(Color.web("#374151"));
                item.setGraphic(checkIcon);
            }

            item.setOnAction(e -> {
                wiki.setRegion(region.getName());
                updateRegionBadge(regionBadge);
                scheduleAutoSave();
            });
            menu.getItems().add(item);
        }

        menu.show(anchor, javafx.geometry.Side.TOP, 0, 0);
    }

    private void showMoreMenu(Button anchor) {
        ContextMenu menu = new ContextMenu();

        MenuItem deleteItem = new MenuItem("Delete wiki");
        FontIcon deleteIcon = new FontIcon(Material2OutlinedAL.DELETE_OUTLINE);
        deleteIcon.setIconSize(16);
        deleteIcon.setIconColor(Color.web("#5F6368"));
        deleteItem.setGraphic(deleteIcon);
        deleteItem.setOnAction(e -> {
            deleted = true;
            controller.deleteWiki(wiki);
            if (onClose != null)
                onClose.run();
        });

        MenuItem duplicateItem = new MenuItem("Duplicate");
        FontIcon duplicateIcon = new FontIcon(Material2OutlinedAL.CONTENT_COPY);
        duplicateIcon.setIconSize(16);
        duplicateIcon.setIconColor(Color.web("#5F6368"));
        duplicateItem.setGraphic(duplicateIcon);
        duplicateItem.setOnAction(e -> {
            controller.duplicateWiki(wiki);
            if (onClose != null)
                onClose.run();
        });

        MenuItem exportItem = new MenuItem("Export as Markdown");
        FontIcon exportIcon = new FontIcon(Material2OutlinedAL.GET_APP);
        exportIcon.setIconSize(16);
        exportIcon.setIconColor(Color.web("#5F6368"));
        exportItem.setGraphic(exportIcon);

        SeparatorMenuItem separator = new SeparatorMenuItem();

        menu.getItems().addAll(duplicateItem, exportItem, separator, deleteItem);
        menu.show(anchor, javafx.geometry.Side.TOP, 0, 0);
    }

    private void togglePin() {
        controller.toggleFavorite(wiki);
        // Update local wiki reference with the toggled state
        wiki.setIsFavorite(!Boolean.TRUE.equals(wiki.getIsFavorite()));
        // Refresh the pin button by recreating it with new state
        HBox header = (HBox) getChildren().get(0);
        Button newPinBtn = createPinButton(Boolean.TRUE.equals(wiki.getIsFavorite()));
        Tooltip.install(newPinBtn, new Tooltip(Boolean.TRUE.equals(wiki.getIsFavorite()) ? "Unpin" : "Pin"));
        newPinBtn.setOnAction(e -> togglePin());
        header.getChildren().set(0, newPinBtn);
    }

    private void scheduleAutoSave() {
        // Update wiki object
        wiki.setTitle(titleField.getText());
        wiki.setContent(contentArea.getText());
        controller.scheduleAutoSave();
    }

    public void saveAndClose() {
        if (deleted) {
            return; // Don't save if wiki was deleted
        }
        wiki.setTitle(titleField.getText());
        wiki.setContent(contentArea.getText());
        controller.saveWiki(wiki);
    }

    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }
}
