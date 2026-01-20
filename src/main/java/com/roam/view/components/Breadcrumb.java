package com.roam.view.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;

/**
 * A navigation breadcrumb component for displaying hierarchical location
 * context.
 * <p>
 * This component shows the current navigation path with clickable links that
 * allow
 * users to navigate back to parent views. It displays an "Operations" link
 * followed
 * by the current operation name, providing clear visual hierarchy and
 * navigation.
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public class Breadcrumb extends HBox {

    private final Runnable onNavigateBack;

    public Breadcrumb(String operationName, Runnable onNavigateBack) {
        this.onNavigateBack = onNavigateBack;

        setSpacing(10);
        setAlignment(Pos.CENTER_LEFT);
        setPrefHeight(50);
        setPadding(new Insets(0, 20, 0, 20));
        setStyle("-fx-background-color: -roam-bg-primary;");

        // "Operations" link
        Hyperlink operationsLink = new Hyperlink("Operations");
        operationsLink.setFont(Font.font("Poppins", 14));
        operationsLink.setStyle("-fx-text-fill: -roam-blue; -fx-border-width: 0; -fx-underline: false;");
        operationsLink.setOnMouseEntered(
                e -> operationsLink.setStyle("-fx-text-fill: -roam-blue; -fx-border-width: 0; -fx-underline: true;"));
        operationsLink.setOnMouseExited(
                e -> operationsLink.setStyle("-fx-text-fill: -roam-blue; -fx-border-width: 0; -fx-underline: false;"));
        operationsLink.setOnAction(e -> {
            if (onNavigateBack != null) {
                onNavigateBack.run();
            }
        });

        // Separator
        Label separator = new Label(">");
        separator.setFont(Font.font("Poppins", 14));
        separator.setStyle("-fx-text-fill: -roam-text-hint;");

        // Operation name
        String displayName = operationName.length() > 40
                ? operationName.substring(0, 40) + "..."
                : operationName;
        Label nameLabel = new Label(displayName);
        nameLabel.setFont(Font.font("Poppins", 14));
        nameLabel.setStyle("-fx-text-fill: -roam-text-primary;");

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Back button
        Hyperlink backButton = new Hyperlink("← Back");
        backButton.setFont(Font.font("Poppins", 14));
        backButton.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: -roam-text-secondary; " +
                        "-fx-padding: 8 16 8 16; " +
                        "-fx-background-radius: 6; " +
                        "-fx-border-width: 0;");
        backButton.setOnMouseEntered(e -> backButton.setStyle(
                "-fx-background-color: -roam-gray-bg; " +
                        "-fx-text-fill: -roam-text-primary; " +
                        "-fx-padding: 8 16 8 16; " +
                        "-fx-background-radius: 6; " +
                        "-fx-border-width: 0;"));
        backButton.setOnMouseExited(e -> backButton.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: -roam-text-secondary; " +
                        "-fx-padding: 8 16 8 16; " +
                        "-fx-background-radius: 6; " +
                        "-fx-border-width: 0;"));
        backButton.setOnAction(e -> {
            if (onNavigateBack != null) {
                onNavigateBack.run();
            }
        });

        getChildren().addAll(operationsLink, separator, nameLabel, spacer, backButton);
    }
}
