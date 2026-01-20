package com.roam.view.components;

import com.roam.model.CalendarEvent;
import com.roam.model.Wiki;
import com.roam.model.Operation;
import com.roam.model.Priority;
import com.roam.model.Region;
import com.roam.model.Task;
import com.roam.model.TaskStatus;
import com.roam.util.ThemeManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.StageStyle;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * A dialog component for creating and editing tasks.
 * <p>
 * This dialog provides a comprehensive form for task management, including
 * fields for
 * title, description, status, priority, due date, and associations with
 * operations,
 * regions, events, and wikis. Supports both create and edit modes with
 * validation.
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public class TaskDialog extends Dialog<Task> {

    private final TextField titleField;
    private final TextArea descriptionField;
    private final ComboBox<TaskStatus> statusCombo;
    private final ComboBox<Priority> priorityCombo;
    private final ComboBox<Operation> operationCombo;
    private final ComboBox<Region> regionCombo;
    private final ComboBox<CalendarEvent> eventCombo;
    private final ComboBox<Wiki> wikiCombo;
    private final ComboBox<String> recurrenceCombo;
    private final DatePicker recurrenceEndDatePicker;
    private final DatePicker dueDatePicker;
    private final Label errorLabel;

    private final Task task;
    private final boolean isEditMode;
    private final Runnable onDelete;
    private final List<Operation> operations;

    private Task resultTask = null;
    private Button createButton;
    private Button cancelButton;
    private Button deleteButton;

    // Constructor for operation-specific tasks (existing behavior)
    public TaskDialog(Task task, Runnable onDelete) {
        this(task, onDelete, null, null, null, null);
    }

    // Constructor for global task creation with operation selector
    public TaskDialog(Task task, Runnable onDelete, List<Operation> operations, List<Region> regions,
            List<CalendarEvent> events, List<Wiki> wikis) {
        this.task = task;
        this.isEditMode = task != null && task.getId() != null;
        this.onDelete = onDelete;
        this.operations = operations;

        // Set transparent stage style for drop shadow effect
        initStyle(StageStyle.TRANSPARENT);
        setTitle(isEditMode ? "Edit Task" : "Create New Task");

        // Create form fields
        titleField = createTextField("Enter task title", 255);
        descriptionField = createTextArea("Enter task description (optional)", 1000);
        statusCombo = createStatusComboBox();
        priorityCombo = createPriorityComboBox();
        operationCombo = operations != null ? createOperationComboBox(operations) : null;
        regionCombo = regions != null ? createRegionComboBox(regions) : null;
        eventCombo = events != null ? createEventComboBox(events) : null;
        wikiCombo = wikis != null ? createWikiComboBox(wikis) : null;
        recurrenceCombo = createRecurrenceCombo();
        recurrenceEndDatePicker = new DatePicker();
        dueDatePicker = createDatePicker();
        errorLabel = createErrorLabel();

        // Setup recurrence combo
        recurrenceEndDatePicker.setPromptText("End date (optional)");
        recurrenceEndDatePicker.setDisable(true);
        recurrenceCombo.setOnAction(e -> {
            boolean isRecurring = !"None".equals(recurrenceCombo.getValue());
            recurrenceEndDatePicker.setDisable(!isRecurring);
        });

        // Setup dialog with drop shadow
        setupDialogWithShadow();

        // Pre-fill data if editing
        if (isEditMode) {
            populateFields();
        }
    }

    private void setupDialogWithShadow() {
        boolean isDark = ThemeManager.getInstance().isDarkTheme();
        String bgColor = isDark ? "#2d2d2d" : "#ffffff";
        String textColor = isDark ? "#ffffff" : "#374151";
        String borderColor = isDark ? "#404040" : "#E5E7EB";
        String secondaryTextColor = isDark ? "#9ca3af" : "#6B7280";

        // Create form layout
        VBox formLayout = createFormLayout();
        formLayout.setStyle("-fx-background-color: " + bgColor + ";");

        // Wrap in ScrollPane
        ScrollPane scrollPane = new ScrollPane(formLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setMaxHeight(450);
        scrollPane.setPrefHeight(400);

        // Create dialog header with title bar
        HBox titleBar = new HBox(12);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(16, 20, 16, 20));
        titleBar.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 16 16 0 0;");

        FontIcon titleIcon = new FontIcon(isEditMode ? Feather.EDIT_3 : Feather.CHECK_SQUARE);
        titleIcon.setIconSize(18);
        titleIcon.setIconColor(Color.web("#4285f4"));

        Label titleLabel = new Label(isEditMode ? "Edit Task" : "Create New Task");
        titleLabel.setFont(Font.font("Poppins SemiBold", 14));
        titleLabel.setStyle("-fx-text-fill: " + textColor + ";");

        javafx.scene.layout.Region titleSpacer = new javafx.scene.layout.Region();
        HBox.setHgrow(titleSpacer, javafx.scene.layout.Priority.ALWAYS);

        Button closeBtn = new Button();
        FontIcon closeIcon = new FontIcon(Feather.X);
        closeIcon.setIconSize(16);
        closeIcon.setIconColor(Color.web(secondaryTextColor));
        closeBtn.setGraphic(closeIcon);
        closeBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> {
            resultTask = null;
            hide();
        });

        titleBar.getChildren().addAll(titleIcon, titleLabel, titleSpacer, closeBtn);

        // Create button bar
        HBox buttonBar = new HBox(12);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        buttonBar.setPadding(new Insets(16, 24, 20, 24));
        buttonBar.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 0 0 16 16;");

        // Add delete button if in edit mode
        if (isEditMode && onDelete != null) {
            deleteButton = new Button("Delete");
            deleteButton.setPrefWidth(100);
            deleteButton.setPrefHeight(40);
            deleteButton.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: #ef4444; " +
                            "-fx-font-family: 'Poppins'; -fx-font-size: 14px; -fx-font-weight: bold; " +
                            "-fx-background-radius: 8; -fx-cursor: hand; -fx-border-color: #ef4444; -fx-border-radius: 8;");
            deleteButton.setOnAction(e -> {
                onDelete.run();
                hide();
            });

            javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            buttonBar.getChildren().addAll(deleteButton, spacer);
        }

        cancelButton = new Button("Cancel");
        cancelButton.setPrefWidth(100);
        cancelButton.setPrefHeight(40);
        String cancelBtnBg = isDark ? "#3d3d3d" : "#f5f5f5";
        cancelButton.setStyle(
                "-fx-background-color: " + cancelBtnBg + "; -fx-text-fill: " + textColor + "; " +
                        "-fx-font-family: 'Poppins'; -fx-font-size: 14px; -fx-font-weight: bold; " +
                        "-fx-background-radius: 8; -fx-cursor: hand;");
        cancelButton.setOnAction(e -> {
            resultTask = null;
            hide();
        });

        createButton = new Button(isEditMode ? "Update" : "Create");
        createButton.setPrefWidth(100);
        createButton.setPrefHeight(40);
        createButton.setStyle(
                "-fx-background-color: #4285f4; -fx-text-fill: white; " +
                        "-fx-font-family: 'Poppins'; -fx-font-size: 14px; -fx-font-weight: bold; " +
                        "-fx-background-radius: 8; -fx-cursor: hand;");
        createButton.setDisable(!isEditMode && (titleField.getText() == null || titleField.getText().trim().isEmpty()));
        createButton.setOnAction(e -> {
            Task result = validateAndCreateTask();
            if (result != null) {
                resultTask = result;
                hide();
            }
        });

        // Enable button when title is not empty
        titleField.textProperty().addListener((obs, old, newVal) -> {
            boolean titleEmpty = newVal == null || newVal.trim().isEmpty();
            boolean operationEmpty = operationCombo != null && operationCombo.getValue() == null;
            createButton.setDisable(titleEmpty || operationEmpty);
            if (newVal != null && !newVal.trim().isEmpty()) {
                errorLabel.setVisible(false);
            }
        });

        if (operationCombo != null) {
            operationCombo.valueProperty().addListener((obs, old, newVal) -> {
                boolean titleEmpty = titleField.getText() == null || titleField.getText().trim().isEmpty();
                boolean operationEmpty = newVal == null;
                createButton.setDisable(titleEmpty || operationEmpty);
            });
        }

        // Enable submit button if editing with pre-filled title
        if (isEditMode && task != null && task.getTitle() != null && !task.getTitle().trim().isEmpty()) {
            createButton.setDisable(false);
        }

        buttonBar.getChildren().addAll(cancelButton, createButton);

        // Main dialog content
        VBox dialogContent = new VBox();
        dialogContent.setStyle("-fx-background-color: " + bgColor + ";");
        dialogContent.getChildren().add(scrollPane);

        // Main container with shadow
        VBox mainContainer = new VBox();
        mainContainer.setStyle(
                "-fx-background-color: " + bgColor + "; -fx-background-radius: 16; " +
                        "-fx-border-color: " + borderColor + "; -fx-border-width: 1; -fx-border-radius: 16;");
        mainContainer.setEffect(new DropShadow(30, 0, 8, Color.rgb(0, 0, 0, 0.15)));
        mainContainer.setPrefWidth(500);
        mainContainer.setMaxWidth(500);
        mainContainer.getChildren().addAll(titleBar, dialogContent, buttonBar);

        // Wrap in StackPane with padding to allow shadow to render
        StackPane shadowContainer = new StackPane(mainContainer);
        shadowContainer.setPadding(new Insets(40));
        shadowContainer.setStyle("-fx-background-color: transparent;");

        getDialogPane().setContent(shadowContainer);
        getDialogPane().setStyle("-fx-background-color: transparent; -fx-padding: 0;");

        // Make dialog pane background transparent
        getDialogPane().getScene().setFill(Color.TRANSPARENT);

        // Hide default buttons but add one to satisfy Dialog requirements
        getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        getDialogPane().lookupButton(ButtonType.CANCEL).setVisible(false);
        getDialogPane().lookupButton(ButtonType.CANCEL).setManaged(false);

        // Set result converter to return the result task
        setResultConverter(dialogButton -> resultTask);
    }

    private TextField createTextField(String prompt, int maxLength) {
        boolean isDark = ThemeManager.getInstance().isDarkTheme();
        String inputBg = isDark ? "#3d3d3d" : "#f8f9fa";
        String inputBorder = isDark ? "#4d4d4d" : "#e9ecef";
        String textColor = isDark ? "#ffffff" : "#212529";

        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setPrefHeight(42);
        field.setFont(Font.font("Poppins", 14));
        field.setStyle(
                "-fx-border-color: " + inputBorder + "; -fx-border-radius: 8; -fx-background-radius: 8; " +
                        "-fx-text-fill: " + textColor + "; -fx-background-color: " + inputBg + ";");

        field.textProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && newVal.length() > maxLength) {
                field.setText(old);
            }
        });

        return field;
    }

    private TextArea createTextArea(String prompt, int maxLength) {
        boolean isDark = ThemeManager.getInstance().isDarkTheme();
        String inputBg = isDark ? "#3d3d3d" : "#f8f9fa";
        String inputBorder = isDark ? "#4d4d4d" : "#e9ecef";
        String textColor = isDark ? "#ffffff" : "#212529";

        TextArea area = new TextArea();
        area.setPromptText(prompt);
        area.setPrefHeight(80);
        area.setWrapText(true);
        area.setFont(Font.font("Poppins", 14));
        area.setStyle(
                "-fx-border-color: " + inputBorder + "; -fx-border-radius: 8; -fx-background-radius: 8; " +
                        "-fx-text-fill: " + textColor + "; -fx-control-inner-background: " + inputBg + ";");

        area.textProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && newVal.length() > maxLength) {
                area.setText(old);
            }
        });

        return area;
    }

    private ComboBox<TaskStatus> createStatusComboBox() {
        boolean isDark = ThemeManager.getInstance().isDarkTheme();
        String inputBg = isDark ? "#3d3d3d" : "#f8f9fa";
        String inputBorder = isDark ? "#4d4d4d" : "#e9ecef";

        ComboBox<TaskStatus> combo = new ComboBox<>();
        combo.getItems().addAll(TaskStatus.values());
        combo.setValue(TaskStatus.TODO);
        combo.setPrefHeight(42);
        combo.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 14px; " +
                "-fx-background-color: " + inputBg + "; -fx-border-color: " + inputBorder + "; " +
                "-fx-border-radius: 8; -fx-background-radius: 8;");

        combo.setButtonCell(new TaskStatusListCell());
        combo.setCellFactory(lv -> new TaskStatusListCell());

        return combo;
    }

    private ComboBox<Priority> createPriorityComboBox() {
        boolean isDark = ThemeManager.getInstance().isDarkTheme();
        String inputBg = isDark ? "#3d3d3d" : "#f8f9fa";
        String inputBorder = isDark ? "#4d4d4d" : "#e9ecef";

        ComboBox<Priority> combo = new ComboBox<>();
        combo.getItems().addAll(Priority.values());
        combo.setValue(Priority.MEDIUM);
        combo.setPrefHeight(42);
        combo.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 14px; " +
                "-fx-background-color: " + inputBg + "; -fx-border-color: " + inputBorder + "; " +
                "-fx-border-radius: 8; -fx-background-radius: 8;");

        combo.setButtonCell(new PriorityListCell());
        combo.setCellFactory(lv -> new PriorityListCell());

        return combo;
    }

    private ComboBox<Operation> createOperationComboBox(List<Operation> ops) {
        boolean isDark = ThemeManager.getInstance().isDarkTheme();
        String inputBg = isDark ? "#3d3d3d" : "#f8f9fa";
        String inputBorder = isDark ? "#4d4d4d" : "#e9ecef";

        ComboBox<Operation> combo = new ComboBox<>();
        combo.getItems().addAll(ops);
        combo.setPromptText("Select operation");
        combo.setPrefHeight(42);
        combo.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 14px; " +
                "-fx-background-color: " + inputBg + "; -fx-border-color: " + inputBorder + "; " +
                "-fx-border-radius: 8; -fx-background-radius: 8;");

        combo.setButtonCell(new OperationListCell());
        combo.setCellFactory(lv -> new OperationListCell());

        return combo;
    }

    private ComboBox<Region> createRegionComboBox(List<Region> regions) {
        boolean isDark = ThemeManager.getInstance().isDarkTheme();
        String inputBg = isDark ? "#3d3d3d" : "#f8f9fa";
        String inputBorder = isDark ? "#4d4d4d" : "#e9ecef";

        ComboBox<Region> combo = new ComboBox<>();
        combo.getItems().addAll(regions);
        combo.setPromptText("Select Region");
        combo.setPrefHeight(42);
        combo.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 14px; " +
                "-fx-background-color: " + inputBg + "; -fx-border-color: " + inputBorder + "; " +
                "-fx-border-radius: 8; -fx-background-radius: 8;");
        combo.setButtonCell(new RegionListCell());
        combo.setCellFactory(lv -> new RegionListCell());
        return combo;
    }

    private ComboBox<CalendarEvent> createEventComboBox(List<CalendarEvent> events) {
        boolean isDark = ThemeManager.getInstance().isDarkTheme();
        String inputBg = isDark ? "#3d3d3d" : "#f8f9fa";
        String inputBorder = isDark ? "#4d4d4d" : "#e9ecef";

        ComboBox<CalendarEvent> combo = new ComboBox<>();
        combo.getItems().addAll(events);
        combo.setPromptText("Link to Event");
        combo.setPrefHeight(42);
        combo.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 14px; " +
                "-fx-background-color: " + inputBg + "; -fx-border-color: " + inputBorder + "; " +
                "-fx-border-radius: 8; -fx-background-radius: 8;");
        combo.setButtonCell(new CalendarEventListCell());
        combo.setCellFactory(lv -> new CalendarEventListCell());
        return combo;
    }

    private ComboBox<Wiki> createWikiComboBox(List<Wiki> wikis) {
        boolean isDark = ThemeManager.getInstance().isDarkTheme();
        String inputBg = isDark ? "#3d3d3d" : "#f8f9fa";
        String inputBorder = isDark ? "#4d4d4d" : "#e9ecef";

        ComboBox<Wiki> combo = new ComboBox<>();
        combo.getItems().addAll(wikis);
        combo.setPromptText("Link to Wiki");
        combo.setPrefHeight(42);
        combo.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 14px; " +
                "-fx-background-color: " + inputBg + "; -fx-border-color: " + inputBorder + "; " +
                "-fx-border-radius: 8; -fx-background-radius: 8;");
        combo.setButtonCell(new WikiListCell());
        combo.setCellFactory(lv -> new WikiListCell());
        return combo;
    }

    private ComboBox<String> createRecurrenceCombo() {
        boolean isDark = ThemeManager.getInstance().isDarkTheme();
        String inputBg = isDark ? "#3d3d3d" : "#f8f9fa";
        String inputBorder = isDark ? "#4d4d4d" : "#e9ecef";

        ComboBox<String> combo = new ComboBox<>();
        combo.getItems().addAll("None", "Daily", "Weekly", "Monthly", "Yearly");
        combo.setValue("None");
        combo.setPrefHeight(42);
        combo.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 14px; " +
                "-fx-background-color: " + inputBg + "; -fx-border-color: " + inputBorder + "; " +
                "-fx-border-radius: 8; -fx-background-radius: 8;");
        return combo;
    }

    private DatePicker createDatePicker() {
        boolean isDark = ThemeManager.getInstance().isDarkTheme();
        String inputBg = isDark ? "#3d3d3d" : "#f8f9fa";
        String inputBorder = isDark ? "#4d4d4d" : "#e9ecef";

        DatePicker picker = new DatePicker();
        picker.setPromptText("Select due date");
        picker.setPrefHeight(42);
        picker.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 14px; " +
                "-fx-background-color: " + inputBg + "; -fx-border-color: " + inputBorder + "; " +
                "-fx-border-radius: 8; -fx-background-radius: 8;");
        return picker;
    }

    private Label createErrorLabel() {
        Label label = new Label("Title is required");
        label.setFont(Font.font("Poppins", 12));
        label.setStyle("-fx-text-fill: #ef4444;");
        label.setVisible(false);
        return label;
    }

    private VBox createFormLayout() {
        boolean isDark = ThemeManager.getInstance().isDarkTheme();
        String bgColor = isDark ? "#2d2d2d" : "#ffffff";

        VBox layout = new VBox(16);
        layout.setPadding(new Insets(20, 24, 20, 24));
        layout.setStyle("-fx-background-color: " + bgColor + ";");

        layout.getChildren().addAll(
                createFieldGroup("Title *", titleField),
                errorLabel,
                createFieldGroup("Description", descriptionField));

        // Add operation selector if available
        if (operationCombo != null) {
            layout.getChildren().add(createFieldGroup("Operation *", operationCombo));
        }

        if (regionCombo != null) {
            layout.getChildren().add(createFieldGroup("Region", regionCombo));
        }

        if (eventCombo != null) {
            layout.getChildren().add(createFieldGroup("Event", eventCombo));
        }

        if (wikiCombo != null) {
            layout.getChildren().add(createFieldGroup("Wiki", wikiCombo));
        }

        layout.getChildren().addAll(
                createFieldGroup("Status *", statusCombo),
                createFieldGroup("Priority *", priorityCombo),
                createFieldGroup("Due Date", dueDatePicker),
                createRecurrenceSection());

        return layout;
    }

    private VBox createRecurrenceSection() {
        boolean isDark = ThemeManager.getInstance().isDarkTheme();
        String labelColor = isDark ? "#9ca3af" : "#6B7280";

        VBox section = new VBox(10);
        Label label = new Label("Recurrence");
        label.setFont(Font.font("Poppins", 13));
        label.setStyle("-fx-text-fill: " + labelColor + ";");

        HBox box = new HBox(10);
        recurrenceCombo.setPrefWidth(150);
        recurrenceEndDatePicker.setPrefWidth(200);
        recurrenceEndDatePicker.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 14px;");

        box.getChildren().addAll(recurrenceCombo, recurrenceEndDatePicker);
        section.getChildren().addAll(label, box);
        return section;
    }

    private VBox createFieldGroup(String labelText, Control field) {
        boolean isDark = ThemeManager.getInstance().isDarkTheme();
        String labelColor = isDark ? "#9ca3af" : "#6B7280";

        VBox group = new VBox(6);

        Label label = new Label(labelText);
        label.setFont(Font.font("Poppins", 13));
        label.setStyle("-fx-text-fill: " + labelColor + ";");

        group.getChildren().addAll(label, field);
        return group;
    }

    private void populateFields() {
        if (task != null) {
            titleField.setText(task.getTitle());
            descriptionField.setText(task.getDescription());
            statusCombo.setValue(task.getStatus());
            priorityCombo.setValue(task.getPriority());
            if (operationCombo != null && task.getOperationId() != null) {
                operationCombo.getItems().stream()
                        .filter(op -> op.getId().equals(task.getOperationId()))
                        .findFirst()
                        .ifPresent(operationCombo::setValue);
            }
            if (regionCombo != null && task.getRegion() != null) {
                regionCombo.getItems().stream()
                        .filter(r -> r.getName().equals(task.getRegion()))
                        .findFirst()
                        .ifPresent(regionCombo::setValue);
            }
            if (eventCombo != null && task.getCalendarEventId() != null) {
                eventCombo.getItems().stream()
                        .filter(e -> e.getId().equals(task.getCalendarEventId()))
                        .findFirst()
                        .ifPresent(eventCombo::setValue);
            }
            if (wikiCombo != null && task.getWikiId() != null) {
                wikiCombo.getItems().stream()
                        .filter(n -> n.getId().equals(task.getWikiId()))
                        .findFirst()
                        .ifPresent(wikiCombo::setValue);
            }
            if (task.getDueDate() != null) {
                dueDatePicker.setValue(task.getDueDate().toLocalDate());
            }
            if (task.getRecurrenceRule() != null) {
                recurrenceCombo.setValue(task.getRecurrenceRule());
                recurrenceEndDatePicker.setDisable(false);
            }
            if (task.getRecurrenceEndDate() != null) {
                recurrenceEndDatePicker.setValue(task.getRecurrenceEndDate().toLocalDate());
            }
        }
    }

    private Task validateAndCreateTask() {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            errorLabel.setVisible(true);
            return null;
        }

        Task t = isEditMode ? task : new Task();

        t.setTitle(title);
        t.setDescription(descriptionField.getText() != null ? descriptionField.getText().trim() : "");
        t.setStatus(statusCombo.getValue());
        t.setPriority(priorityCombo.getValue());

        // Set operation ID if operation selector is present
        if (operationCombo != null && operationCombo.getValue() != null) {
            t.setOperationId(operationCombo.getValue().getId());
        }

        if (regionCombo != null && regionCombo.getValue() != null) {
            t.setRegion(regionCombo.getValue().getName());
        } else {
            t.setRegion(null);
        }

        if (eventCombo != null && eventCombo.getValue() != null) {
            t.setCalendarEventId(eventCombo.getValue().getId());
        } else {
            t.setCalendarEventId(null);
        }

        if (wikiCombo != null && wikiCombo.getValue() != null) {
            t.setWikiId(wikiCombo.getValue().getId());
        } else {
            t.setWikiId(null);
        }

        if (dueDatePicker.getValue() != null) {
            // Combine date with end of day time
            t.setDueDate(LocalDateTime.of(dueDatePicker.getValue(), LocalTime.of(23, 59)));
        } else {
            t.setDueDate(null);
        }

        if (!"None".equals(recurrenceCombo.getValue())) {
            t.setRecurrenceRule(recurrenceCombo.getValue());
            if (recurrenceEndDatePicker.getValue() != null) {
                t.setRecurrenceEndDate(recurrenceEndDatePicker.getValue().atTime(23, 59, 59));
            } else {
                t.setRecurrenceEndDate(null);
            }
        } else {
            t.setRecurrenceRule(null);
            t.setRecurrenceEndDate(null);
        }

        return t;
    }

    private static class TaskStatusListCell extends ListCell<TaskStatus> {
        @Override
        protected void updateItem(TaskStatus item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                switch (item) {
                    case TODO -> setText("To Do");
                    case IN_PROGRESS -> setText("In Progress");
                    case DONE -> setText("Done");
                }
            }
        }
    }

    private static class PriorityListCell extends ListCell<Priority> {
        @Override
        protected void updateItem(Priority item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                switch (item) {
                    case HIGH -> setText("High");
                    case MEDIUM -> setText("Medium");
                    case LOW -> setText("Low");
                }
            }
        }
    }

    private static class OperationListCell extends ListCell<Operation> {
        @Override
        protected void updateItem(Operation item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                setText(item.getName());
            }
        }
    }

    private static class RegionListCell extends ListCell<Region> {
        @Override
        protected void updateItem(Region item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                setText(item.getName());
                setStyle("-fx-text-fill: " + item.getColor() + ";");
            }
        }
    }

    private static class CalendarEventListCell extends ListCell<CalendarEvent> {
        @Override
        protected void updateItem(CalendarEvent item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                setText(item.getTitle());
            }
        }
    }

    private static class WikiListCell extends ListCell<Wiki> {
        @Override
        protected void updateItem(Wiki item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                setText(item.getTitle());
            }
        }
    }
}
