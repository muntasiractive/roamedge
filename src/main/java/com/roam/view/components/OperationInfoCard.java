package com.roam.view.components;

import com.roam.model.Operation;
import com.roam.model.OperationStatus;
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
 * A styled card component for displaying operation details.
 * <p>
 * This component renders an operation as a visually appealing card with status
 * badges,
 * priority indicators, and metadata such as due dates and timestamps. It
 * provides
 * edit functionality and supports consistent styling across the application.
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public class OperationInfoCard extends VBox {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    private final Operation operation;
    private final Consumer<Operation> onEdit;

    public OperationInfoCard(Operation operation, Consumer<Operation> onEdit) {
        this.operation = operation;
        this.onEdit = onEdit;

        setSpacing(SPACING_STANDARD);
        setPadding(new Insets(SPACING_XL));
        setStyle(StyleBuilder.sectionStyle());
        VBox.setMargin(this, new Insets(0, 0, SPACING_LG, 0));

        // Header row
        HBox header = createHeader();

        // Metadata row
        HBox metadata = createMetadata();

        // Add components
        getChildren().addAll(header, metadata);

        // Purpose section (if exists)
        if (operation.getPurpose() != null && !operation.getPurpose().isEmpty()) {
            VBox purposeSection = createTextSection("Purpose", operation.getPurpose());
            getChildren().add(purposeSection);
        }

        // Outcome section (if exists)
        if (operation.getOutcome() != null && !operation.getOutcome().isEmpty()) {
            VBox outcomeSection = createTextSection("Outcome", operation.getOutcome());
            getChildren().add(outcomeSection);
        }
    }

    private HBox createHeader() {
        HBox header = new HBox(SPACING_STANDARD);
        header.setAlignment(Pos.CENTER_LEFT);

        // Operation name
        Label nameLabel = new Label(operation.getName());
        nameLabel.setFont(Font.font(FONT_BOLD, FONT_SIZE_DISPLAY));
        nameLabel.setStyle("-fx-text-fill: " + TEXT_PRIMARY + ";");
        HBox.setHgrow(nameLabel, javafx.scene.layout.Priority.ALWAYS);

        // Edit button with consistent styling
        Button editBtn = new Button("✎ Edit Info");
        editBtn.setFont(Font.font(FONT_REGULAR, FONT_SIZE_STANDARD));
        String normalStyle = StyleBuilder.secondaryButtonStyle();
        String hoverStyle = StyleBuilder.create()
                .backgroundColor(BORDER)
                .textFill(TEXT_PRIMARY)
                .padding(SPACING_SM, SPACING_STANDARD)
                .radius(RADIUS_STANDARD)
                .cursorHand()
                .build();

        editBtn.setStyle(normalStyle);
        editBtn.setOnMouseEntered(e -> editBtn.setStyle(hoverStyle));
        editBtn.setOnMouseExited(e -> editBtn.setStyle(normalStyle));
        editBtn.setOnAction(e -> {
            if (onEdit != null) {
                onEdit.accept(operation);
            }
        });

        header.getChildren().addAll(nameLabel, editBtn);
        return header;
    }

    private HBox createMetadata() {
        HBox metadata = new HBox(SPACING_LG);
        metadata.setAlignment(Pos.CENTER_LEFT);

        // Status badge
        Label statusBadge = createStatusBadge(operation.getStatus());

        // Priority badge
        Label priorityBadge = createPriorityBadge(operation.getPriority());

        // Region badge (if exists)
        if (operation.getRegion() != null && !operation.getRegion().isEmpty()) {
            Label regionBadge = createRegionBadge(operation.getRegion());
            metadata.getChildren().add(statusBadge);
            metadata.getChildren().add(priorityBadge);
            metadata.getChildren().add(regionBadge);
        } else {
            metadata.getChildren().add(statusBadge);
            metadata.getChildren().add(priorityBadge);
        }

        // Due date
        Label dueDateLabel = new Label();
        dueDateLabel.setFont(Font.font(FONT_REGULAR, FONT_SIZE_MD + 1));
        dueDateLabel.setStyle("-fx-text-fill: " + TEXT_SECONDARY + ";");

        HBox dueDateBox = new HBox(SPACING_XS + 1);
        dueDateBox.setAlignment(Pos.CENTER_LEFT);
        FontIcon calIcon = new FontIcon(Feather.CALENDAR);
        calIcon.setIconSize(ICON_SM);

        if (operation.getDueDate() != null) {
            dueDateLabel.setText("Due: " + DATE_FORMATTER.format(operation.getDueDate()));
        } else {
            dueDateLabel.setText("No due date");
        }

        dueDateBox.getChildren().addAll(calIcon, dueDateLabel);
        metadata.getChildren().add(dueDateBox);
        return metadata;
    }

    private VBox createTextSection(String title, String content) {
        VBox section = new VBox(SPACING_XS + 1);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font(FONT_REGULAR, FONT_SIZE_MD));
        titleLabel.setStyle("-fx-text-fill: " + TEXT_HINT + ";");

        Label contentLabel = new Label(content);
        contentLabel.setFont(Font.font(FONT_REGULAR, FONT_SIZE_STANDARD));
        contentLabel.setStyle("-fx-text-fill: " + TEXT_PRIMARY + "; -fx-line-spacing: 0.6;");
        contentLabel.setWrapText(true);
        contentLabel.setMaxWidth(Double.MAX_VALUE);

        section.getChildren().addAll(titleLabel, contentLabel);
        return section;
    }

    private Label createStatusBadge(OperationStatus status) {
        Label badge = new Label();
        badge.setFont(Font.font(FONT_REGULAR, FONT_SIZE_MD + 1));
        String baseBadgeStyle = "-fx-padding: 4 12 4 12; -fx-background-radius: " + RADIUS_LARGE + ";";
        badge.setStyle(baseBadgeStyle);

        switch (status) {
            case ONGOING -> {
                badge.setText("Ongoing");
                badge.setStyle(badge.getStyle() +
                        "-fx-background-color: " + BLUE_LIGHT + "; -fx-text-fill: " + BLUE + ";");
            }
            case IN_PROGRESS -> {
                badge.setText("In Progress");
                badge.setStyle(badge.getStyle() +
                        "-fx-background-color: " + ORANGE_BG + "; -fx-text-fill: " + ORANGE + ";");
            }
            case END -> {
                badge.setText("Completed");
                badge.setStyle(badge.getStyle() +
                        "-fx-background-color: " + GREEN_BG + "; -fx-text-fill: " + GREEN + ";");
            }
        }

        return badge;
    }

    private Label createPriorityBadge(com.roam.model.Priority priority) {
        Label badge = new Label();
        badge.setFont(Font.font(FONT_REGULAR, FONT_SIZE_MD + 1));
        String baseBadgeStyle = "-fx-padding: 4 12 4 12; -fx-background-radius: " + RADIUS_LARGE + ";";
        badge.setStyle(baseBadgeStyle);

        switch (priority) {
            case HIGH -> {
                badge.setText("High");
                badge.setStyle(badge.getStyle() +
                        "-fx-background-color: " + RED_BG + "; -fx-text-fill: " + RED + ";");
            }
            case MEDIUM -> {
                badge.setText("Medium");
                badge.setStyle(badge.getStyle() +
                        "-fx-background-color: " + YELLOW_BG + "; -fx-text-fill: " + YELLOW + ";");
            }
            case LOW -> {
                badge.setText("Low");
                badge.setStyle(badge.getStyle() +
                        "-fx-background-color: " + BG_GRAY + "; -fx-text-fill: " + TEXT_SECONDARY + ";");
            }
        }

        return badge;
    }

    private Label createRegionBadge(String region) {
        Label badge = new Label("🌍 " + region);
        badge.setFont(Font.font(FONT_REGULAR, FONT_SIZE_MD + 1));
        badge.setStyle(StyleBuilder.badgeStyle("-roam-blue-tag-bg", "-roam-blue-tag"));
        return badge;
    }

    public void refresh(Operation updatedOperation) {
        // Re-create the card with updated data
        getChildren().clear();

        HBox header = createHeader();
        HBox metadata = createMetadata();

        getChildren().addAll(header, metadata);

        if (updatedOperation.getPurpose() != null && !updatedOperation.getPurpose().isEmpty()) {
            VBox purposeSection = createTextSection("Purpose", updatedOperation.getPurpose());
            getChildren().add(purposeSection);
        }

        if (updatedOperation.getOutcome() != null && !updatedOperation.getOutcome().isEmpty()) {
            VBox outcomeSection = createTextSection("Outcome", updatedOperation.getOutcome());
            getChildren().add(outcomeSection);
        }
    }
}
