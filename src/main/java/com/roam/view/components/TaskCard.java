package com.roam.view.components;

import com.roam.model.Priority;
import com.roam.model.Task;
import com.roam.model.TaskStatus;
import com.roam.util.StyleBuilder;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

import static com.roam.util.UIConstants.*;

/**
 * A styled card component for displaying task information.
 * <p>
 * Features priority-based left border, hover effects, and action buttons.
 * Each card displays the task title, description, due date, and priority
 * indicator.
 * Provides edit and complete actions through interactive buttons.
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public class TaskCard extends VBox {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd");

    private final Task task;
    private final Consumer<Task> onEdit;
    private final Consumer<Task> onComplete;
    private final Button editBtn;
    private final Button completeBtn;

    public TaskCard(Task task, Consumer<Task> onEdit, Consumer<Task> onComplete) {
        this.task = task;
        this.onEdit = onEdit;
        this.onComplete = onComplete;
        this.editBtn = new Button();
        this.completeBtn = new Button();

        setSpacing(SPACING_SM);
        setPadding(new Insets(SPACING_STANDARD));
        setMinHeight(CARD_MIN_HEIGHT);
        getStyleClass().add("task-card");
        setStyle(buildCardStyle(false));

        // Hover effect
        setOnMouseEntered(e -> {
            setStyle(buildCardStyle(true));
            editBtn.setVisible(true);
            if (task.getStatus() != TaskStatus.DONE) {
                completeBtn.setVisible(true);
            }
        });

        setOnMouseExited(e -> {
            setStyle(buildCardStyle(false));
            editBtn.setVisible(false);
            completeBtn.setVisible(false);
        });

        // Header Row (Title + Buttons)
        HBox header = new HBox(10);
        header.setAlignment(Pos.TOP_LEFT);

        // Title
        Label titleLabel = new Label(task.getTitle());
        titleLabel.setFont(Font.font("Poppins Medium", 14));
        titleLabel.setStyle("-fx-text-fill: -roam-text-primary;");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(titleLabel, javafx.scene.layout.Priority.ALWAYS);

        // Edit button
        FontIcon editIcon = new FontIcon(Feather.EDIT_2);
        editIcon.setIconSize(14);
        editBtn.setGraphic(editIcon);
        editBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 2;");
        editBtn.setVisible(false);
        editBtn.setOnAction(e -> {
            e.consume();
            if (onEdit != null)
                onEdit.accept(task);
        });

        // Complete button
        FontIcon completeIcon = new FontIcon(Feather.CHECK_CIRCLE);
        completeIcon.setIconSize(14);
        completeIcon.setIconColor(javafx.scene.paint.Color.web("#388E3C"));
        completeBtn.setGraphic(completeIcon);
        completeBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 2;");
        completeBtn.setVisible(false);
        completeBtn.setOnAction(e -> {
            e.consume();
            if (onComplete != null)
                onComplete.accept(task);
        });

        header.getChildren().add(titleLabel);
        header.getChildren().add(editBtn);
        if (task.getStatus() != TaskStatus.DONE) {
            header.getChildren().add(completeBtn);
        }

        getChildren().add(header);

        // Description (if exists)
        if (task.getDescription() != null && !task.getDescription().isEmpty()) {
            Label descLabel = new Label(task.getDescription());
            descLabel.setFont(Font.font("Poppins", 12));
            descLabel.setStyle("-fx-text-fill: -roam-text-secondary;");
            descLabel.setWrapText(true);
            descLabel.setMaxWidth(Double.MAX_VALUE);

            // Limit to 3 lines
            String desc = task.getDescription();
            if (desc.length() > 100) {
                desc = desc.substring(0, 100) + "...";
            }
            descLabel.setText(desc);

            getChildren().add(descLabel);
        }

        // Spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        getChildren().add(spacer);

        // Footer
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_LEFT);

        // Due date with icon
        if (task.getDueDate() != null) {
            FontIcon calendarIcon = new FontIcon(Feather.CALENDAR);
            calendarIcon.setIconSize(12);
            calendarIcon.setStyle("-fx-icon-color: -roam-text-hint;");

            Label dueDateLabel = new Label(DATE_FORMATTER.format(task.getDueDate()));
            dueDateLabel.setFont(Font.font("Poppins", 11));
            dueDateLabel.setStyle("-fx-text-fill: -roam-text-hint;");

            HBox dueDateBox = new HBox(4);
            dueDateBox.setAlignment(Pos.CENTER_LEFT);
            dueDateBox.getChildren().addAll(calendarIcon, dueDateLabel);
            footer.getChildren().add(dueDateBox);
        }

        getChildren().add(footer);
    }

    private String getPriorityColor(Priority priority) {
        // Using CSS variables for theme-aware colors
        return switch (priority) {
            case HIGH -> PRIORITY_HIGH;
            case MEDIUM -> PRIORITY_MEDIUM;
            case LOW -> PRIORITY_LOW;
        };
    }

    /**
     * Builds the card style based on hover state.
     */
    private String buildCardStyle(boolean isHovered) {
        StyleBuilder builder = StyleBuilder.create()
                .backgroundColor(BG_PRIMARY)
                .borderColor(BORDER)
                .borderWidth(1)
                .radius(RADIUS_STANDARD)
                .cursorHand()
                .addProperty("-fx-border-left-width", "4")
                .addProperty("-fx-border-left-color", getPriorityColor(task.getPriority()));

        if (isHovered) {
            builder.effect(SHADOW_HOVER).scale(1.02);
        }

        return builder.build();
    }

    public Task getTask() {
        return task;
    }
}
