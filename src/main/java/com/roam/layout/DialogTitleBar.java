package com.roam.layout;

import com.roam.util.ThemeManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Objects;

/**
 * Custom title bar for dialogs with just a close button.
 * Provides a consistent look with the main application title bar.
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public class DialogTitleBar extends HBox {

    private static final double TITLE_BAR_HEIGHT = 36;
    private static final double ICON_SIZE = 18;
    private static final double BUTTON_SIZE = 32;

    private final Stage stage;
    private final Label titleLabel;
    private double xOffset = 0;
    private double yOffset = 0;

    public DialogTitleBar(Stage stage, String title) {
        this.stage = stage;

        getStyleClass().add("dialog-title-bar");
        setAlignment(Pos.CENTER_LEFT);
        setPrefHeight(TITLE_BAR_HEIGHT);
        setMinHeight(TITLE_BAR_HEIGHT);
        setMaxHeight(TITLE_BAR_HEIGHT);
        setPadding(new Insets(0, 4, 0, 12));
        setSpacing(10);

        // App icon
        ImageView iconView = createAppIcon();

        // Title
        titleLabel = new Label(title);
        titleLabel.getStyleClass().add("dialog-title-text");

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Close button
        Button closeBtn = createCloseButton();

        getChildren().addAll(iconView, titleLabel, spacer, closeBtn);

        // Enable dragging
        setupWindowDragging();

        // Apply theme
        applyTheme();
    }

    private ImageView createAppIcon() {
        ImageView imageView = new ImageView();
        try {
            Image icon = new Image(Objects.requireNonNull(
                    getClass().getResourceAsStream("/icons/roam-icon.png")));
            imageView.setImage(icon);
            imageView.setFitWidth(ICON_SIZE);
            imageView.setFitHeight(ICON_SIZE);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
        } catch (Exception e) {
            // Ignore if icon not found
        }
        return imageView;
    }

    private Button createCloseButton() {
        Button button = new Button();
        FontIcon fontIcon = new FontIcon(Feather.X);
        fontIcon.setIconSize(14);
        fontIcon.getStyleClass().add("dialog-close-icon");
        button.setGraphic(fontIcon);
        button.getStyleClass().addAll("dialog-close-button");
        button.setPrefSize(BUTTON_SIZE, BUTTON_SIZE);
        button.setMinSize(BUTTON_SIZE, BUTTON_SIZE);
        button.setMaxSize(BUTTON_SIZE, BUTTON_SIZE);
        button.setOnAction(e -> stage.close());
        return button;
    }

    private void setupWindowDragging() {
        setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    private void applyTheme() {
        boolean isDark = ThemeManager.getInstance().isDarkTheme();
        if (isDark) {
            setStyle(
                    "-fx-background-color: #1e1e1e; -fx-background-radius: 8 8 0 0; -fx-border-color: #333333; -fx-border-width: 0 0 1 0;");
        } else {
            setStyle(
                    "-fx-background-color: #f5f5f5; -fx-background-radius: 8 8 0 0; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
        }
    }

    public void refreshTheme() {
        applyTheme();
    }
}
