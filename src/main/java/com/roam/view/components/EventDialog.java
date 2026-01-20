package com.roam.view.components;

import com.roam.model.CalendarEvent;
import com.roam.model.CalendarSource;
import com.roam.model.Operation;
import com.roam.model.Region;
import com.roam.model.Task;
import com.roam.model.Wiki;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * A dialog component for creating and editing calendar events.
 * <p>
 * This dialog provides a comprehensive form for event management, including
 * fields for
 * title, description, location, start/end dates and times, all-day option,
 * recurrence settings,
 * and associations with calendar sources, operations, regions, tasks, and
 * wikis.
 * Supports both create and edit modes with validation.
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public class EventDialog extends Dialog<CalendarEvent> {

    private final TextField titleField;
    private final TextArea descriptionField;
    private final TextField locationField;
    private final DatePicker startDatePicker;
    private final DatePicker endDatePicker;
    private final Spinner<Integer> startHourSpinner;
    private final Spinner<Integer> startMinuteSpinner;
    private final Spinner<Integer> endHourSpinner;
    private final Spinner<Integer> endMinuteSpinner;
    private final CheckBox allDayCheckbox;
    private final ComboBox<CalendarSource> calendarCombo;
    private final ComboBox<Operation> operationCombo;
    private final ComboBox<Region> regionCombo;
    private final ComboBox<Task> taskCombo;
    private final ComboBox<Wiki> wikiCombo;
    private final ComboBox<String> recurrenceCombo;
    private final DatePicker recurrenceEndDatePicker;
    private final Label errorLabel;

    private final CalendarEvent event;
    private final boolean isEditMode;
    private final Runnable onDelete;

    private CalendarEvent resultEvent = null;
    private Button createButton;
    private Button cancelButton;
    private Button deleteButton;

    public EventDialog(CalendarEvent event, List<CalendarSource> calendars, List<Operation> operations,
            List<Region> regions, List<Task> tasks, List<Wiki> wikis, Runnable onDelete) {
        this.event = event;
        this.isEditMode = event != null && event.getId() != null;
        this.onDelete = onDelete;

        // Set transparent stage style for drop shadow effect
        initStyle(StageStyle.TRANSPARENT);
        setTitle(isEditMode ? "Edit Event" : "Create New Event");

        // Create form fields
        titleField = createTextField("Enter event title", 255);
        descriptionField = createTextArea("Enter event description (optional)", 1000);
        locationField = createTextField("Enter location (optional)", 255);
        startDatePicker = createDatePicker();
        endDatePicker = createDatePicker();
        startHourSpinner = createSpinner(0, 23, 9);
        startMinuteSpinner = createSpinner(0, 59, 0);
        endHourSpinner = createSpinner(0, 23, 10);
        endMinuteSpinner = createSpinner(0, 59, 0);
        allDayCheckbox = new CheckBox("All day event");
        calendarCombo = createCalendarComboBox(calendars);
        operationCombo = operations != null ? createOperationComboBox(operations) : null;
        regionCombo = regions != null ? createRegionComboBox(regions) : null;
        taskCombo = tasks != null ? createTaskComboBox(tasks) : null;
        wikiCombo = wikis != null ? createWikiComboBox(wikis) : null;
        recurrenceCombo = createRecurrenceCombo();
        recurrenceEndDatePicker = new DatePicker();
        errorLabel = createErrorLabel();

        // Setup all-day checkbox
        allDayCheckbox.setOnAction(e -> {
            boolean allDay = allDayCheckbox.isSelected();
            startHourSpinner.setDisable(allDay);
            startMinuteSpinner.setDisable(allDay);
            endHourSpinner.setDisable(allDay);
            endMinuteSpinner.setDisable(allDay);
        });

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
        scrollPane.setMaxHeight(500);
        scrollPane.setPrefHeight(450);

        // Create dialog header with title bar
        HBox titleBar = new HBox(12);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(16, 20, 16, 20));
        titleBar.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 16 16 0 0;");

        FontIcon titleIcon = new FontIcon(isEditMode ? Feather.EDIT_3 : Feather.CALENDAR);
        titleIcon.setIconSize(18);
        titleIcon.setIconColor(Color.web("#4285f4"));

        Label titleLabel = new Label(isEditMode ? "Edit Event" : "Create New Event");
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
            resultEvent = null;
            hide();
        });

        titleBar.getChildren().addAll(titleIcon, titleLabel, titleSpacer, closeBtn);

        // Create button bar
        HBox buttonBar = new HBox(12);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        buttonBar.setPadding(new Insets(16, 24, 20, 24));
        buttonBar.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 0 0 16 16;");

        // Delete button (only in edit mode)
        if (isEditMode && onDelete != null) {
            deleteButton = new Button("Delete");
            deleteButton.setPrefWidth(80);
            deleteButton.setPrefHeight(40);
            deleteButton.setStyle(
                    "-fx-background-color: #EF4444; -fx-text-fill: white; " +
                            "-fx-font-family: 'Poppins'; -fx-font-size: 14px; -fx-font-weight: bold; " +
                            "-fx-background-radius: 8; -fx-cursor: hand;");
            deleteButton.setOnAction(e -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Confirm Delete");
                confirm.setHeaderText("Delete Event");
                confirm.setContentText("Are you sure you want to delete this event?");
                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        onDelete.run();
                        resultEvent = null;
                        hide();
                    }
                });
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
            resultEvent = null;
            hide();
        });

        createButton = new Button(isEditMode ? "Update" : "Create");
        createButton.setPrefWidth(100);
        createButton.setPrefHeight(40);
        createButton.setStyle(
                "-fx-background-color: #4285f4; -fx-text-fill: white; " +
                        "-fx-font-family: 'Poppins'; -fx-font-size: 14px; -fx-font-weight: bold; " +
                        "-fx-background-radius: 8; -fx-cursor: hand;");
        createButton.setDisable(true);
        createButton.setOnAction(e -> {
            CalendarEvent result = validateAndCreateEvent();
            if (result != null) {
                resultEvent = result;
                hide();
            }
        });

        // Enable button when title is not empty
        titleField.textProperty().addListener((obs, old, newVal) -> {
            createButton.setDisable(newVal == null || newVal.trim().isEmpty());
            if (newVal != null && !newVal.trim().isEmpty()) {
                errorLabel.setVisible(false);
            }
        });

        // Enable submit button if editing with pre-filled title
        if (isEditMode && event.getTitle() != null && !event.getTitle().trim().isEmpty()) {
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
        mainContainer.setPrefWidth(520);
        mainContainer.setMaxWidth(520);
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

        // Set result converter to return the result event
        setResultConverter(dialogButton -> resultEvent);
    }

    private VBox createFormLayout() {
        boolean isDark = ThemeManager.getInstance().isDarkTheme();
        String textColor = isDark ? "#ffffff" : "#374151";
        String labelStyle = "-fx-text-fill: " + textColor + "; -fx-font-size: 14px; -fx-font-family: 'Poppins';";

        VBox form = new VBox(16);
        form.setPadding(new Insets(16, 24, 16, 24));

        // Title Field
        VBox titleBox = new VBox(6);
        Label titleLbl = new Label("Event Title *");
        titleLbl.setStyle(labelStyle + " -fx-font-weight: 600;");
        titleBox.getChildren().addAll(titleLbl, titleField);

        // Calendar Field
        VBox calendarBox = new VBox(6);
        Label calendarLbl = new Label("Calendar *");
        calendarLbl.setStyle(labelStyle + " -fx-font-weight: 600;");
        calendarBox.getChildren().addAll(calendarLbl, calendarCombo);

        // Date and Time
        HBox startDateTimeBox = new HBox(12);
        VBox startDateBox = new VBox(6);
        Label startDateLbl = new Label("Start Date *");
        startDateLbl.setStyle(labelStyle + " -fx-font-weight: 600;");
        startDateBox.getChildren().addAll(startDateLbl, startDatePicker);
        HBox.setHgrow(startDateBox, javafx.scene.layout.Priority.ALWAYS);

        VBox startTimeBox = new VBox(6);
        Label startTimeLbl = new Label("Start Time");
        startTimeLbl.setStyle(labelStyle + " -fx-font-weight: 600;");
        HBox startTimeSpinners = new HBox(4);
        startTimeSpinners.setAlignment(Pos.CENTER_LEFT);
        Label startColon = new Label(":");
        startColon.setStyle(labelStyle);
        startTimeSpinners.getChildren().addAll(startHourSpinner, startColon, startMinuteSpinner);
        startTimeBox.getChildren().addAll(startTimeLbl, startTimeSpinners);
        startDateTimeBox.getChildren().addAll(startDateBox, startTimeBox);

        HBox endDateTimeBox = new HBox(12);
        VBox endDateBox = new VBox(6);
        Label endDateLbl = new Label("End Date *");
        endDateLbl.setStyle(labelStyle + " -fx-font-weight: 600;");
        endDateBox.getChildren().addAll(endDateLbl, endDatePicker);
        HBox.setHgrow(endDateBox, javafx.scene.layout.Priority.ALWAYS);

        VBox endTimeBox = new VBox(6);
        Label endTimeLbl = new Label("End Time");
        endTimeLbl.setStyle(labelStyle + " -fx-font-weight: 600;");
        HBox endTimeSpinners = new HBox(4);
        endTimeSpinners.setAlignment(Pos.CENTER_LEFT);
        Label endColon = new Label(":");
        endColon.setStyle(labelStyle);
        endTimeSpinners.getChildren().addAll(endHourSpinner, endColon, endMinuteSpinner);
        endTimeBox.getChildren().addAll(endTimeLbl, endTimeSpinners);
        endDateTimeBox.getChildren().addAll(endDateBox, endTimeBox);

        // All day checkbox
        VBox allDayBox = new VBox(6);
        allDayCheckbox.setStyle("-fx-text-fill: " + textColor + "; -fx-font-family: 'Poppins'; -fx-font-size: 14px;");
        allDayBox.getChildren().add(allDayCheckbox);

        // Location Field
        VBox locationBox = new VBox(6);
        Label locationLbl = new Label("Location");
        locationLbl.setStyle(labelStyle + " -fx-font-weight: 600;");
        locationBox.getChildren().addAll(locationLbl, locationField);

        // Description Field
        VBox descBox = new VBox(6);
        Label descLbl = new Label("Description");
        descLbl.setStyle(labelStyle + " -fx-font-weight: 600;");
        descBox.getChildren().addAll(descLbl, descriptionField);

        // Recurrence Section
        VBox recurrenceBox = new VBox(6);
        Label recurrenceLbl = new Label("Repeat");
        recurrenceLbl.setStyle(labelStyle + " -fx-font-weight: 600;");
        HBox recurrenceRow = new HBox(12);
        recurrenceRow.setAlignment(Pos.CENTER_LEFT);
        recurrenceRow.getChildren().addAll(recurrenceCombo, recurrenceEndDatePicker);
        HBox.setHgrow(recurrenceCombo, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(recurrenceEndDatePicker, javafx.scene.layout.Priority.ALWAYS);
        recurrenceBox.getChildren().addAll(recurrenceLbl, recurrenceRow);

        // Links Section
        VBox linksBox = new VBox(12);
        Label linksLbl = new Label("Link to...");
        linksLbl.setStyle(labelStyle + " -fx-font-weight: 600;");
        linksBox.getChildren().add(linksLbl);

        // Operation combo
        if (operationCombo != null) {
            VBox opBox = new VBox(4);
            Label opLbl = new Label("Operation");
            opLbl.setStyle(labelStyle);
            opBox.getChildren().addAll(opLbl, operationCombo);
            linksBox.getChildren().add(opBox);
        }

        // Region combo
        if (regionCombo != null) {
            VBox regionBox = new VBox(4);
            Label regionLbl = new Label("Region");
            regionLbl.setStyle(labelStyle);
            regionBox.getChildren().addAll(regionLbl, regionCombo);
            linksBox.getChildren().add(regionBox);
        }

        // Task combo
        if (taskCombo != null) {
            VBox taskBox = new VBox(4);
            Label taskLbl = new Label("Task");
            taskLbl.setStyle(labelStyle);
            taskBox.getChildren().addAll(taskLbl, taskCombo);
            linksBox.getChildren().add(taskBox);
        }

        // Wiki combo
        if (wikiCombo != null) {
            VBox wikiBox = new VBox(4);
            Label wikiLbl = new Label("Wiki");
            wikiLbl.setStyle(labelStyle);
            wikiBox.getChildren().addAll(wikiLbl, wikiCombo);
            linksBox.getChildren().add(wikiBox);
        }

        form.getChildren().addAll(
                titleBox,
                calendarBox,
                startDateTimeBox,
                endDateTimeBox,
                allDayBox,
                locationBox,
                descBox,
                recurrenceBox,
                linksBox,
                errorLabel);

        return form;
    }

    private void populateFields() {
        if (event == null)
            return;

        if (event.getTitle() != null) {
            titleField.setText(event.getTitle());
        }
        if (event.getDescription() != null) {
            descriptionField.setText(event.getDescription());
        }
        if (event.getLocation() != null) {
            locationField.setText(event.getLocation());
        }
        if (event.getStartDateTime() != null) {
            startDatePicker.setValue(event.getStartDateTime().toLocalDate());
            startHourSpinner.getValueFactory().setValue(event.getStartDateTime().getHour());
            startMinuteSpinner.getValueFactory().setValue(event.getStartDateTime().getMinute());
        }
        if (event.getEndDateTime() != null) {
            endDatePicker.setValue(event.getEndDateTime().toLocalDate());
            endHourSpinner.getValueFactory().setValue(event.getEndDateTime().getHour());
            endMinuteSpinner.getValueFactory().setValue(event.getEndDateTime().getMinute());
        }
        if (event.getIsAllDay() != null) {
            allDayCheckbox.setSelected(event.getIsAllDay());
            boolean allDay = event.getIsAllDay();
            startHourSpinner.setDisable(allDay);
            startMinuteSpinner.setDisable(allDay);
            endHourSpinner.setDisable(allDay);
            endMinuteSpinner.setDisable(allDay);
        }

        // Set calendar source
        if (event.getCalendarSourceId() != null && calendarCombo.getItems() != null) {
            for (CalendarSource cs : calendarCombo.getItems()) {
                if (cs.getId().equals(event.getCalendarSourceId())) {
                    calendarCombo.setValue(cs);
                    break;
                }
            }
        }

        // Set linked operation
        if (event.getOperationId() != null && operationCombo != null) {
            for (Operation op : operationCombo.getItems()) {
                if (op != null && op.getId() != null && op.getId().equals(event.getOperationId())) {
                    operationCombo.setValue(op);
                    break;
                }
            }
        }

        // Set linked region
        if (event.getRegion() != null && regionCombo != null) {
            for (Region r : regionCombo.getItems()) {
                if (r != null && r.getName() != null && r.getName().equals(event.getRegion())) {
                    regionCombo.setValue(r);
                    break;
                }
            }
        }

        // Set linked task
        if (event.getTaskId() != null && taskCombo != null) {
            for (Task t : taskCombo.getItems()) {
                if (t != null && t.getId() != null && t.getId().equals(event.getTaskId())) {
                    taskCombo.setValue(t);
                    break;
                }
            }
        }

        // Set linked wiki
        if (event.getWikiId() != null && wikiCombo != null) {
            for (Wiki w : wikiCombo.getItems()) {
                if (w != null && w.getId() != null && w.getId().equals(event.getWikiId())) {
                    wikiCombo.setValue(w);
                    break;
                }
            }
        }

        // Set recurrence
        if (event.getRecurrenceRule() != null && !event.getRecurrenceRule().isEmpty()) {
            String rule = event.getRecurrenceRule().toUpperCase();
            if (rule.contains("DAILY")) {
                recurrenceCombo.setValue("Daily");
            } else if (rule.contains("WEEKLY")) {
                recurrenceCombo.setValue("Weekly");
            } else if (rule.contains("MONTHLY")) {
                recurrenceCombo.setValue("Monthly");
            } else if (rule.contains("YEARLY")) {
                recurrenceCombo.setValue("Yearly");
            }
            recurrenceEndDatePicker.setDisable(false);
        }
        if (event.getRecurrenceEndDate() != null) {
            recurrenceEndDatePicker.setValue(event.getRecurrenceEndDate().toLocalDate());
        }
    }

    private CalendarEvent validateAndCreateEvent() {
        String title = titleField.getText();
        if (title == null || title.trim().isEmpty()) {
            showError("Event title is required");
            return null;
        }

        CalendarSource selectedCalendar = calendarCombo.getValue();
        if (selectedCalendar == null) {
            showError("Please select a calendar");
            return null;
        }

        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        if (startDate == null) {
            showError("Start date is required");
            return null;
        }
        if (endDate == null) {
            endDate = startDate;
        }

        // Create date times
        LocalDateTime startDateTime;
        LocalDateTime endDateTime;

        if (allDayCheckbox.isSelected()) {
            startDateTime = startDate.atStartOfDay();
            endDateTime = endDate.atTime(23, 59, 59);
        } else {
            int startHour = startHourSpinner.getValue();
            int startMinute = startMinuteSpinner.getValue();
            int endHour = endHourSpinner.getValue();
            int endMinute = endMinuteSpinner.getValue();
            startDateTime = startDate.atTime(LocalTime.of(startHour, startMinute));
            endDateTime = endDate.atTime(LocalTime.of(endHour, endMinute));
        }

        if (endDateTime.isBefore(startDateTime)) {
            showError("End time must be after start time");
            return null;
        }

        CalendarEvent result = isEditMode ? event : new CalendarEvent();
        result.setTitle(title.trim());
        result.setDescription(descriptionField.getText() != null ? descriptionField.getText().trim() : null);
        result.setLocation(locationField.getText() != null ? locationField.getText().trim() : null);
        result.setCalendarSourceId(selectedCalendar.getId());
        result.setStartDateTime(startDateTime);
        result.setEndDateTime(endDateTime);
        result.setIsAllDay(allDayCheckbox.isSelected());

        // Set linked operation
        if (operationCombo != null && operationCombo.getValue() != null) {
            result.setOperationId(operationCombo.getValue().getId());
        } else {
            result.setOperationId(null);
        }

        // Set region
        if (regionCombo != null && regionCombo.getValue() != null) {
            result.setRegion(regionCombo.getValue().getName());
        } else {
            result.setRegion(null);
        }

        // Set linked task
        if (taskCombo != null && taskCombo.getValue() != null) {
            result.setTaskId(taskCombo.getValue().getId());
        } else {
            result.setTaskId(null);
        }

        // Set linked wiki
        if (wikiCombo != null && wikiCombo.getValue() != null) {
            result.setWikiId(wikiCombo.getValue().getId());
        } else {
            result.setWikiId(null);
        }

        // Set recurrence
        String recurrence = recurrenceCombo.getValue();
        if (recurrence != null && !"None".equals(recurrence)) {
            result.setRecurrenceRule("FREQ=" + recurrence.toUpperCase());
            if (recurrenceEndDatePicker.getValue() != null) {
                result.setRecurrenceEndDate(recurrenceEndDatePicker.getValue().atStartOfDay());
            }
        } else {
            result.setRecurrenceRule(null);
            result.setRecurrenceEndDate(null);
        }

        return result;
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private TextField createTextField(String prompt, int maxLength) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setPrefHeight(40);
        applyFieldStyle(field);
        field.textProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && newVal.length() > maxLength) {
                field.setText(old);
            }
        });
        return field;
    }

    private TextArea createTextArea(String prompt, int maxLength) {
        TextArea area = new TextArea();
        area.setPromptText(prompt);
        area.setPrefRowCount(3);
        area.setWrapText(true);
        applyTextAreaStyle(area);
        area.textProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && newVal.length() > maxLength) {
                area.setText(old);
            }
        });
        return area;
    }

    private DatePicker createDatePicker() {
        DatePicker picker = new DatePicker(LocalDate.now());
        picker.setPrefHeight(40);
        applyFieldStyle(picker);
        return picker;
    }

    private Spinner<Integer> createSpinner(int min, int max, int initial) {
        Spinner<Integer> spinner = new Spinner<>(min, max, initial);
        spinner.setPrefWidth(70);
        spinner.setPrefHeight(40);
        spinner.setEditable(true);
        applyFieldStyle(spinner);
        return spinner;
    }

    private ComboBox<CalendarSource> createCalendarComboBox(List<CalendarSource> calendars) {
        ComboBox<CalendarSource> combo = new ComboBox<>();
        combo.setPrefHeight(40);
        applyFieldStyle(combo);

        combo.setCellFactory(lv -> new CalendarSourceListCell());
        combo.setButtonCell(new CalendarSourceListCell());

        if (calendars != null && !calendars.isEmpty()) {
            combo.getItems().addAll(calendars);
            // Select default calendar or first one
            CalendarSource defaultCal = calendars.stream()
                    .filter(CalendarSource::getIsDefault)
                    .findFirst()
                    .orElse(calendars.get(0));
            combo.setValue(defaultCal);
        }
        return combo;
    }

    private ComboBox<Operation> createOperationComboBox(List<Operation> operations) {
        ComboBox<Operation> combo = new ComboBox<>();
        combo.setPrefHeight(40);
        applyFieldStyle(combo);
        combo.setPromptText("Select operation (optional)");

        combo.setCellFactory(lv -> new OperationListCell());
        combo.setButtonCell(new OperationListCell());

        if (operations != null) {
            combo.getItems().add(null); // Allow no selection
            combo.getItems().addAll(operations);
        }
        return combo;
    }

    private ComboBox<Region> createRegionComboBox(List<Region> regions) {
        ComboBox<Region> combo = new ComboBox<>();
        combo.setPrefHeight(40);
        applyFieldStyle(combo);
        combo.setPromptText("Select region (optional)");

        combo.setCellFactory(lv -> new RegionListCell());
        combo.setButtonCell(new RegionListCell());

        if (regions != null) {
            combo.getItems().add(null); // Allow no selection
            combo.getItems().addAll(regions);
        }
        return combo;
    }

    private ComboBox<Task> createTaskComboBox(List<Task> tasks) {
        ComboBox<Task> combo = new ComboBox<>();
        combo.setPrefHeight(40);
        applyFieldStyle(combo);
        combo.setPromptText("Select task (optional)");

        combo.setCellFactory(lv -> new TaskListCell());
        combo.setButtonCell(new TaskListCell());

        if (tasks != null) {
            combo.getItems().add(null); // Allow no selection
            combo.getItems().addAll(tasks);
        }
        return combo;
    }

    private ComboBox<Wiki> createWikiComboBox(List<Wiki> wikis) {
        ComboBox<Wiki> combo = new ComboBox<>();
        combo.setPrefHeight(40);
        applyFieldStyle(combo);
        combo.setPromptText("Select wiki (optional)");

        combo.setCellFactory(lv -> new WikiListCell());
        combo.setButtonCell(new WikiListCell());

        if (wikis != null) {
            combo.getItems().add(null); // Allow no selection
            combo.getItems().addAll(wikis);
        }
        return combo;
    }

    private ComboBox<String> createRecurrenceCombo() {
        ComboBox<String> combo = new ComboBox<>();
        combo.setPrefHeight(40);
        applyFieldStyle(combo);
        combo.getItems().addAll("None", "Daily", "Weekly", "Monthly", "Yearly");
        combo.setValue("None");
        return combo;
    }

    private Label createErrorLabel() {
        Label label = new Label();
        label.setStyle("-fx-text-fill: #EF4444; -fx-font-family: 'Poppins'; -fx-font-size: 12px;");
        label.setVisible(false);
        label.setWrapText(true);
        return label;
    }

    private void applyFieldStyle(Control control) {
        boolean isDark = ThemeManager.getInstance().isDarkTheme();
        String bgColor = isDark ? "#3d3d3d" : "#f9fafb";
        String borderColor = isDark ? "#4d4d4d" : "#E5E7EB";
        String textColor = isDark ? "#ffffff" : "#374151";
        control.setStyle(
                "-fx-background-color: " + bgColor + "; -fx-border-color: " + borderColor + "; " +
                        "-fx-border-radius: 8; -fx-background-radius: 8; " +
                        "-fx-font-family: 'Poppins'; -fx-font-size: 14px; -fx-text-fill: " + textColor + ";");
    }

    private void applyTextAreaStyle(TextArea area) {
        boolean isDark = ThemeManager.getInstance().isDarkTheme();
        String bgColor = isDark ? "#3d3d3d" : "#f9fafb";
        String borderColor = isDark ? "#4d4d4d" : "#E5E7EB";
        String textColor = isDark ? "#ffffff" : "#374151";
        area.setStyle(
                "-fx-control-inner-background: " + bgColor + "; -fx-border-color: " + borderColor + "; " +
                        "-fx-border-radius: 8; -fx-background-radius: 8; " +
                        "-fx-font-family: 'Poppins'; -fx-font-size: 14px; -fx-text-fill: " + textColor + ";");
    }

    // Custom cell classes
    private static class CalendarSourceListCell extends ListCell<CalendarSource> {
        @Override
        protected void updateItem(CalendarSource item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                HBox box = new HBox(8);
                box.setAlignment(Pos.CENTER_LEFT);
                javafx.scene.shape.Circle colorDot = new javafx.scene.shape.Circle(6);
                colorDot.setFill(Color.web(item.getColor() != null ? item.getColor() : "#4285f4"));
                Label label = new Label(item.getName());
                box.getChildren().addAll(colorDot, label);
                setGraphic(box);
                setText(null);
            }
        }
    }

    private static class OperationListCell extends ListCell<Operation> {
        @Override
        protected void updateItem(Operation item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                setText(item.getName());
                setGraphic(null);
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
                HBox box = new HBox(8);
                box.setAlignment(Pos.CENTER_LEFT);
                javafx.scene.shape.Circle colorDot = new javafx.scene.shape.Circle(6);
                colorDot.setFill(Color.web(item.getColor() != null ? item.getColor() : "#4285f4"));
                Label label = new Label(item.getName());
                box.getChildren().addAll(colorDot, label);
                setGraphic(box);
                setText(null);
            }
        }
    }

    private static class TaskListCell extends ListCell<Task> {
        @Override
        protected void updateItem(Task item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                setText(item.getTitle());
                setGraphic(null);
            }
        }
    }

    private static class WikiListCell extends ListCell<Wiki> {
        @Override
        protected void updateItem(Wiki item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                setText(item.getTitle());
                setGraphic(null);
            }
        }
    }
}
