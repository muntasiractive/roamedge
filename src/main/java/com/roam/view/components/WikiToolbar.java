package com.roam.view.components;

import com.roam.controller.WikiController;
import com.roam.model.WikiTemplate;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;

/**
 * A toolbar component for the wiki view providing quick access to wiki actions.
 * <p>
 * This toolbar includes buttons for creating new wikis and a templates menu
 * for quickly creating wikis from predefined templates.
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public class WikiToolbar extends HBox {

    private final WikiController controller;

    private Button newWikiBtn;
    private MenuButton templatesMenu;

    public WikiToolbar(WikiController controller) {
        this.controller = controller;

        configureToolbar();
        createToolbarElements();
        setupEventHandlers();
    }

    private void configureToolbar() {
        setPrefHeight(60);
        setPadding(new Insets(15, 20, 15, 20));
        setSpacing(15);
        setAlignment(Pos.CENTER_LEFT);
        getStyleClass().add("wiki-toolbar");
        setStyle("-fx-background-color: -roam-bg-primary; -fx-border-color: -roam-border; -fx-border-width: 0 0 1 0;");
    }

    private void createToolbarElements() {
        newWikiBtn = createNewWikiButton();
        templatesMenu = createTemplatesMenu();

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        getChildren().addAll(newWikiBtn, templatesMenu, spacer);
    }

    private Button createNewWikiButton() {
        Button btn = new Button("New Wiki");
        btn.setGraphic(new FontIcon(Feather.PLUS));
        btn.getStyleClass().add("pill-button");
        btn.setPrefWidth(150);
        btn.setPrefHeight(40);
        return btn;
    }

    private MenuButton createTemplatesMenu() {
        MenuButton menu = new MenuButton("Templates");
        menu.setGraphic(new FontIcon(Feather.LAYOUT));
        menu.getStyleClass().add("pill-button");
        menu.setPrefWidth(140);
        menu.setPrefHeight(40);

        // Populate on show
        menu.setOnShowing(e -> populateTemplatesMenu(menu));

        return menu;
    }

    private void populateTemplatesMenu(MenuButton menu) {
        menu.getItems().clear();

        // Load all templates
        List<WikiTemplate> allTemplates = controller.loadAllTemplates();
        for (WikiTemplate template : allTemplates) {
            MenuItem item = new MenuItem(template.getIcon() + " " + template.getName());
            item.setOnAction(e -> {
                controller.createWikiFromTemplate(template);
            });
            menu.getItems().add(item);
        }

        if (!allTemplates.isEmpty()) {
            menu.getItems().add(new SeparatorMenuItem());
        }

        MenuItem manageItem = new MenuItem("Manage Templates...");
        manageItem.setOnAction(e -> {
            TemplateManagerDialog dialog = new TemplateManagerDialog(controller);
            dialog.showAndWait();
        });
        menu.getItems().add(manageItem);
    }

    private void setupEventHandlers() {
        // New Wiki button
        newWikiBtn.setOnAction(e -> {
            controller.createNewWiki();
        });
    }
}
