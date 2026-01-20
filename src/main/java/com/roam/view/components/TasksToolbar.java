package com.roam.view.components;

import com.roam.controller.TasksController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;

import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.function.Consumer;

/**
 * Toolbar component for the Tasks view providing view toggle buttons and action
 * controls.
 * <p>
 * This toolbar allows users to switch between different task visualization
 * modes
 * including Kanban, Cards, Timeline, and Eisenhower Matrix views. It provides
 * a consistent navigation experience across all task views.
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public class TasksToolbar extends HBox {

    private static final String POSITION_FIRST = "first";
    private static final String POSITION_MIDDLE = "middle";
    private static final String POSITION_LAST = "last";

    private final TasksController controller;
    private final ToggleGroup viewToggleGroup;
    private Consumer<String> onViewChanged;

    public TasksToolbar(TasksController controller) {
        this.controller = controller;

        setPrefHeight(60);
        setPadding(new Insets(15, 20, 15, 20));
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(15);
        // Remove border - filter panel has its own rounded border now
        setStyle("-fx-background-color: transparent;");

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // View toggle buttons
        viewToggleGroup = new ToggleGroup();
        ToggleButton kanbanBtn = createViewToggleButton("Kanban", POSITION_FIRST);
        ToggleButton cardsBtn = createViewToggleButton("Cards", POSITION_MIDDLE);
        ToggleButton timelineBtn = createViewToggleButton("Timeline", POSITION_MIDDLE);
        ToggleButton matrixBtn = createViewToggleButton("Matrix", POSITION_LAST);

        kanbanBtn.setToggleGroup(viewToggleGroup);
        cardsBtn.setToggleGroup(viewToggleGroup);
        timelineBtn.setToggleGroup(viewToggleGroup);
        matrixBtn.setToggleGroup(viewToggleGroup);
        kanbanBtn.setSelected(true);

        kanbanBtn.setOnAction(e -> {
            if (onViewChanged != null)
                onViewChanged.accept("kanban");
        });
        cardsBtn.setOnAction(e -> {
            if (onViewChanged != null)
                onViewChanged.accept("cards");
        });
        timelineBtn.setOnAction(e -> {
            if (onViewChanged != null)
                onViewChanged.accept("timeline");
        });
        matrixBtn.setOnAction(e -> {
            if (onViewChanged != null)
                onViewChanged.accept("matrix");
        });

        HBox viewToggle = new HBox(0, kanbanBtn, cardsBtn, timelineBtn, matrixBtn);
        viewToggle.setAlignment(Pos.CENTER);

        // New Task button
        Button newTaskBtn = new Button("New Task");
        newTaskBtn.setGraphic(new FontIcon(Feather.PLUS_CIRCLE));
        newTaskBtn.setPrefWidth(150);
        newTaskBtn.setPrefHeight(40);
        newTaskBtn.getStyleClass().add("pill-button");
        newTaskBtn.setOnAction(e -> {
            controller.createTask();
        });

        getChildren().addAll(spacer, viewToggle, newTaskBtn);
    }

    private ToggleButton createViewToggleButton(String text, String position) {
        ToggleButton btn = new ToggleButton(text);
        btn.setFont(Font.font("Poppins", 13));
        btn.setMinWidth(70);
        btn.setPrefHeight(36);

        String baseStylePrefix = "-fx-background-color: -roam-bg-primary; " +
                "-fx-text-fill: -roam-text-secondary; " +
                "-fx-border-color: -roam-border; " +
                "-fx-border-width: 1; " +
                "-fx-cursor: hand;";

        String selectedStylePrefix = "-fx-background-color: -roam-blue-light; " +
                "-fx-text-fill: -roam-blue; " +
                "-fx-border-color: -roam-blue; " +
                "-fx-border-width: 1; " +
                "-fx-cursor: hand;";

        String radiusSuffix;
        if (POSITION_FIRST.equals(position)) {
            radiusSuffix = " -fx-background-radius: 6 0 0 6; -fx-border-radius: 6 0 0 6;";
        } else if (POSITION_LAST.equals(position)) {
            radiusSuffix = " -fx-background-radius: 0 6 6 0; -fx-border-radius: 0 6 6 0;";
        } else {
            radiusSuffix = " -fx-background-radius: 0; -fx-border-radius: 0;";
        }

        final String baseStyle = baseStylePrefix + radiusSuffix;
        final String selectedStyle = selectedStylePrefix + radiusSuffix;

        btn.setStyle(btn.isSelected() ? selectedStyle : baseStyle);

        btn.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                btn.setStyle(selectedStyle);
            } else {
                btn.setStyle(baseStyle);
            }
        });

        return btn;
    }

    public void setOnViewChanged(Consumer<String> callback) {
        this.onViewChanged = callback;
    }

    public String getSelectedView() {
        if (viewToggleGroup.getSelectedToggle() != null) {
            String text = ((ToggleButton) viewToggleGroup.getSelectedToggle()).getText();
            return text.toLowerCase();
        }
        return "kanban";
    }
}
