package com.roam.view;

import com.roam.controller.CalendarController;
import com.roam.model.CalendarEvent;
import com.roam.model.CalendarSource;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;

import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Calendar view that provides comprehensive event management and scheduling
 * capabilities.
 * <p>
 * This view supports multiple display modes including:
 * <ul>
 * <li>Agenda view - A list-based view of upcoming events</li>
 * <li>Month view - Traditional monthly calendar grid</li>
 * <li>Week view - Weekly schedule with time slots</li>
 * <li>Day view - Detailed daily schedule</li>
 * </ul>
 * The view integrates with {@link com.roam.controller.CalendarController} for
 * event
 * data management and supports filtering by calendar sources.
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public class CalendarView extends BorderPane {

    public enum ViewType {
        AGENDA, MONTH, WEEK, DAY
    }

    private final CalendarController controller;

    private Label dateLabel;
    private VBox filterPanel;
    private StackPane calendarContainer;
    private FlowPane legendContainer;

    private ViewType currentViewType = ViewType.DAY;
    private YearMonth currentYearMonth;
    private LocalDate currentDate;
    private ToggleGroup viewToggleGroup;

    private static final DateTimeFormatter MONTH_YEAR_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy");
    private static final DateTimeFormatter WEEK_FORMATTER = DateTimeFormatter.ofPattern("MMM d");

    public CalendarView(CalendarController controller) {
        this.controller = controller;
        this.currentYearMonth = YearMonth.now();
        this.currentDate = LocalDate.now();

        initialize();
    }

    private void initialize() {
        setStyle("-fx-background-color: -roam-bg-primary;");

        // Create toolbar
        HBox toolbar = createToolbar();
        setTop(toolbar);

        // Create filter panel
        filterPanel = createFilterPanel();

        // Create calendar container with bottom padding for floating bar
        calendarContainer = new StackPane();
        calendarContainer.setPadding(new Insets(20, 20, 60, 20));

        setCenter(calendarContainer);

        // Note: Legend container removed as it overlaps with floating navigation bar
        // Calendar source colors are visible on events themselves
        legendContainer = new FlowPane();
        legendContainer.setVisible(false);
        legendContainer.setManaged(false);

        // Load data
        controller.setOnDataChanged(this::refreshCalendar);
        refreshCalendar();
    }

    private HBox createToolbar() {
        HBox toolbar = new HBox(15);
        toolbar.setPrefHeight(60);
        toolbar.setPadding(new Insets(15, 20, 15, 20));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle(
                "-fx-background-color: -roam-bg-primary; -fx-border-color: -roam-border; -fx-border-width: 0 0 1 0;");

        // Today button
        Button todayBtn = createToolbarButton("Today");
        todayBtn.setGraphic(new FontIcon(Feather.CALENDAR));
        todayBtn.setOnAction(e -> {
            currentYearMonth = YearMonth.now();
            currentDate = LocalDate.now();
            refreshCalendar();
        });

        // Previous button
        // Previous button
        Button prevBtn = createToolbarButton("");
        prevBtn.setGraphic(new FontIcon(Feather.CHEVRON_LEFT));
        prevBtn.setPrefWidth(40);
        prevBtn.setOnAction(e -> navigatePrevious());

        // Next button
        // Next button
        Button nextBtn = createToolbarButton("");
        nextBtn.setGraphic(new FontIcon(Feather.CHEVRON_RIGHT));
        nextBtn.setPrefWidth(40);
        nextBtn.setOnAction(e -> navigateNext());

        // Date label
        dateLabel = new Label(getDateLabelText());
        dateLabel.setFont(Font.font("Poppins Bold", 18));
        dateLabel.setStyle("-fx-text-fill: -roam-text-primary;");
        dateLabel.setMinWidth(250);
        dateLabel.setAlignment(Pos.CENTER);

        // View selector
        HBox viewSelector = createViewSelector();

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // New Event button
        // New Event button
        Button newEventBtn = createToolbarButton("New Event");
        newEventBtn.setGraphic(new FontIcon(Feather.PLUS));
        newEventBtn.getStyleClass().add("pill-button");
        newEventBtn.setOnAction(e -> controller.createEvent(currentDate));

        // Filter toggle button
        // Filter toggle button
        Button filterBtn = createToolbarButton("");
        filterBtn.setGraphic(new FontIcon(Feather.FILTER));
        filterBtn.setPrefWidth(40);
        filterBtn.setOnAction(e -> toggleFilterPanel());

        toolbar.getChildren().addAll(
                todayBtn, prevBtn, nextBtn, dateLabel, viewSelector, spacer, newEventBtn, filterBtn);

        return toolbar;
    }

    private Button createToolbarButton(String text) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Poppins", 14));
        btn.setMinWidth(80);
        btn.setPrefHeight(36);
        btn.getStyleClass().add("pill-button");
        return btn;
    }

    private HBox createViewSelector() {
        HBox selector = new HBox(0);
        selector.setAlignment(Pos.CENTER);

        viewToggleGroup = new ToggleGroup();

        ToggleButton agendaBtn = createViewToggleButton("Agenda", ViewType.AGENDA, true);
        ToggleButton monthBtn = createViewToggleButton("Month", ViewType.MONTH, false);
        ToggleButton weekBtn = createViewToggleButton("Week", ViewType.WEEK, false);
        ToggleButton dayBtn = createViewToggleButton("Day", ViewType.DAY, false);

        agendaBtn.setToggleGroup(viewToggleGroup);
        monthBtn.setToggleGroup(viewToggleGroup);
        weekBtn.setToggleGroup(viewToggleGroup);
        dayBtn.setToggleGroup(viewToggleGroup);

        dayBtn.setSelected(true);

        selector.getChildren().addAll(agendaBtn, monthBtn, weekBtn, dayBtn);
        return selector;
    }

    private ToggleButton createViewToggleButton(String text, ViewType viewType, boolean isFirst) {
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
        if (isFirst) {
            radiusSuffix = " -fx-background-radius: 6 0 0 6; -fx-border-radius: 6 0 0 6;";
        } else if (viewType == ViewType.DAY) {
            radiusSuffix = " -fx-background-radius: 0 6 6 0; -fx-border-radius: 0 6 6 0;";
        } else {
            radiusSuffix = " -fx-background-radius: 0; -fx-border-radius: 0;";
        }

        final String baseStyle = baseStylePrefix + radiusSuffix;
        final String selectedStyle = selectedStylePrefix + radiusSuffix;

        btn.setStyle(baseStyle);

        btn.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                btn.setStyle(selectedStyle);
            } else {
                btn.setStyle(baseStyle);
            }
        });

        btn.setOnAction(e -> {
            if (btn.isSelected()) {
                currentViewType = viewType;
                refreshCalendar();
            }
        });

        return btn;
    }

    private void navigatePrevious() {
        switch (currentViewType) {
            case AGENDA:
                currentDate = currentDate.minusMonths(1);
                currentYearMonth = YearMonth.from(currentDate);
                break;
            case MONTH:
                currentYearMonth = currentYearMonth.minusMonths(1);
                currentDate = currentYearMonth.atDay(1);
                break;
            case WEEK:
                currentDate = currentDate.minusWeeks(1);
                currentYearMonth = YearMonth.from(currentDate);
                break;
            case DAY:
                currentDate = currentDate.minusDays(1);
                currentYearMonth = YearMonth.from(currentDate);
                break;
        }
        refreshCalendar();
    }

    private void navigateNext() {
        switch (currentViewType) {
            case AGENDA:
                currentDate = currentDate.plusMonths(1);
                currentYearMonth = YearMonth.from(currentDate);
                break;
            case MONTH:
                currentYearMonth = currentYearMonth.plusMonths(1);
                currentDate = currentYearMonth.atDay(1);
                break;
            case WEEK:
                currentDate = currentDate.plusWeeks(1);
                currentYearMonth = YearMonth.from(currentDate);
                break;
            case DAY:
                currentDate = currentDate.plusDays(1);
                currentYearMonth = YearMonth.from(currentDate);
                break;
        }
        refreshCalendar();
    }

    private String getDateLabelText() {
        switch (currentViewType) {
            case AGENDA:
                return currentYearMonth.format(MONTH_YEAR_FORMATTER);
            case MONTH:
                return currentYearMonth.format(MONTH_YEAR_FORMATTER);
            case WEEK:
                LocalDate weekStart = currentDate.minusDays(currentDate.getDayOfWeek().getValue() % 7);
                LocalDate weekEnd = weekStart.plusDays(6);
                if (weekStart.getMonth() == weekEnd.getMonth()) {
                    return weekStart.format(WEEK_FORMATTER) + " - " + weekEnd.getDayOfMonth() + ", "
                            + weekStart.getYear();
                } else {
                    return weekStart.format(WEEK_FORMATTER) + " - " + weekEnd.format(WEEK_FORMATTER) + ", "
                            + weekStart.getYear();
                }
            case DAY:
                return currentDate.format(DATE_FORMATTER);
            default:
                return "";
        }
    }

    private GridPane createMonthGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(0);
        grid.setVgap(0);
        grid.setStyle("-fx-background-color: -roam-bg-primary;");

        // Create day headers
        String[] dayNames = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };
        for (int col = 0; col < 7; col++) {
            Label dayHeader = new Label(dayNames[col]);
            dayHeader.setFont(Font.font("Poppins Medium", 13));
            dayHeader.setStyle(
                    "-fx-background-color: -roam-gray-bg; " +
                            "-fx-text-fill: -roam-text-secondary; " +
                            "-fx-alignment: center;");
            dayHeader.setMaxWidth(Double.MAX_VALUE);
            dayHeader.setPrefHeight(40);
            GridPane.setHgrow(dayHeader, Priority.ALWAYS);
            grid.add(dayHeader, col, 0);
        }

        // Calculate calendar grid
        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7; // Sunday = 0
        LocalDate startDate = firstOfMonth.minusDays(dayOfWeek);

        // Create day cells (6 weeks)
        int row = 1;
        LocalDate currentDate = startDate;

        for (int week = 0; week < 6; week++) {
            for (int col = 0; col < 7; col++) {
                VBox dayCell = createMonthDayCell(currentDate);
                GridPane.setHgrow(dayCell, Priority.ALWAYS);
                GridPane.setVgrow(dayCell, Priority.ALWAYS);
                grid.add(dayCell, col, row);
                currentDate = currentDate.plusDays(1);
            }
            row++;
        }

        return grid;
    }

    private GridPane createWeekGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(0);
        grid.setVgap(0);
        grid.setStyle("-fx-background-color: -roam-bg-primary;");

        // Set column constraints to prevent width changes on hover
        // First column (time) is fixed width
        javafx.scene.layout.ColumnConstraints timeColConstraint = new javafx.scene.layout.ColumnConstraints();
        timeColConstraint.setMinWidth(60);
        timeColConstraint.setMaxWidth(60);
        timeColConstraint.setPrefWidth(60);
        grid.getColumnConstraints().add(timeColConstraint);

        // Day columns get equal percentage of remaining space
        for (int i = 0; i < 7; i++) {
            javafx.scene.layout.ColumnConstraints dayColConstraint = new javafx.scene.layout.ColumnConstraints();
            dayColConstraint.setPercentWidth(100.0 / 7.0);
            dayColConstraint.setHgrow(Priority.ALWAYS);
            dayColConstraint.setFillWidth(true);
            grid.getColumnConstraints().add(dayColConstraint);
        }

        // Calculate week start (Sunday)
        LocalDate weekStart = currentDate.minusDays(currentDate.getDayOfWeek().getValue() % 7);

        // Time column
        VBox timeColumn = new VBox(0);
        timeColumn.setMinWidth(60);
        timeColumn.setMaxWidth(60);
        timeColumn.setStyle("-fx-background-color: -roam-gray-bg;");

        // Empty corner cell
        Label cornerCell = new Label("");
        cornerCell.setPrefHeight(40);
        cornerCell.setStyle("-fx-background-color: -roam-gray-bg;");
        timeColumn.getChildren().add(cornerCell);

        // Time labels (6 AM to 11 PM)
        for (int hour = 6; hour <= 23; hour++) {
            Label timeLabel = new Label(
                    String.format("%d:00", hour > 12 ? hour - 12 : hour) + (hour >= 12 ? " PM" : " AM"));
            timeLabel.setFont(Font.font("Poppins", 11));
            timeLabel.setPrefHeight(60);
            timeLabel.setAlignment(Pos.TOP_RIGHT);
            timeLabel.setPadding(new Insets(5, 5, 0, 0));
            timeLabel.setStyle("-fx-background-color: -roam-gray-bg; -fx-text-fill: -roam-text-secondary;");
            timeColumn.getChildren().add(timeLabel);
        }

        grid.add(timeColumn, 0, 0);
        GridPane.setRowSpan(timeColumn, 2);

        // Day headers
        String[] dayNames = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };
        for (int col = 0; col < 7; col++) {
            LocalDate date = weekStart.plusDays(col);
            VBox dayHeader = new VBox(2);
            dayHeader.setAlignment(Pos.CENTER);
            dayHeader.setPadding(new Insets(5));
            dayHeader.setStyle("-fx-background-color: -roam-gray-bg;");

            Label dayName = new Label(dayNames[col]);
            dayName.setFont(Font.font("Poppins Medium", 12));
            dayName.setStyle("-fx-text-fill: -roam-text-secondary;");

            Label dayNum = new Label(String.valueOf(date.getDayOfMonth()));
            dayNum.setFont(Font.font("Poppins Bold", 16));

            boolean isToday = date.equals(LocalDate.now());
            if (isToday) {
                dayNum.setStyle(
                        "-fx-text-fill: -roam-white; -fx-background-color: -roam-blue; -fx-background-radius: 50%; -fx-min-width: 30; -fx-min-height: 30; -fx-alignment: center;");
            } else {
                dayNum.setStyle("-fx-text-fill: -roam-text-primary;");
            }

            dayHeader.getChildren().addAll(dayName, dayNum);
            grid.add(dayHeader, col + 1, 0);
        }

        // Day columns with time slots
        for (int col = 0; col < 7; col++) {
            LocalDate date = weekStart.plusDays(col);
            VBox dayColumn = createWeekDayColumn(date);
            GridPane.setVgrow(dayColumn, Priority.ALWAYS);
            grid.add(dayColumn, col + 1, 1);
        }

        return grid;
    }

    private VBox createWeekDayColumn(LocalDate date) {
        final double HOUR_HEIGHT = 60;
        final int START_HOUR = 6;
        final int END_HOUR = 23;
        final int TOTAL_HOURS = END_HOUR - START_HOUR + 1;

        // Use Pane for absolute positioning
        javafx.scene.layout.Pane column = new javafx.scene.layout.Pane();
        column.setPrefHeight(HOUR_HEIGHT * TOTAL_HOURS);
        column.setMinHeight(HOUR_HEIGHT * TOTAL_HOURS);
        column.setMaxWidth(Double.MAX_VALUE);
        column.setStyle(
                "-fx-background-color: -roam-bg-primary; -fx-border-color: -roam-border; -fx-border-width: 0 0 0 0.5;");

        // Add hour grid lines
        for (int hour = START_HOUR; hour <= END_HOUR; hour++) {
            Region hourLine = new Region();
            hourLine.setLayoutY((hour - START_HOUR) * HOUR_HEIGHT);
            hourLine.setPrefHeight(HOUR_HEIGHT);
            hourLine.prefWidthProperty().bind(column.widthProperty());
            hourLine.setStyle("-fx-border-color: -roam-border; -fx-border-width: 0.5 0 0 0;");

            // Click handler for creating events
            final LocalDate clickDate = date;
            hourLine.setOnMouseClicked(e -> controller.createEvent(clickDate));
            hourLine.setOnMouseEntered(e -> {
                hourLine.setStyle(
                        "-fx-background-color: rgba(255,255,255,0.05); -fx-border-color: -roam-border; -fx-border-width: 0.5 0 0 0; -fx-cursor: hand;");
            });
            hourLine.setOnMouseExited(e -> {
                hourLine.setStyle("-fx-border-color: -roam-border; -fx-border-width: 0.5 0 0 0;");
            });

            column.getChildren().add(hourLine);
        }

        // Add events with proper height based on duration
        List<CalendarEvent> dayEvents = controller.getEventsForDate(date);
        List<CalendarEvent> timedEvents = dayEvents.stream()
                .filter(e -> !e.getIsAllDay())
                .filter(e -> e.getStartDateTime().getHour() >= START_HOUR && e.getStartDateTime().getHour() <= END_HOUR)
                .collect(Collectors.toList());

        for (CalendarEvent event : timedEvents) {
            // Calculate position and height
            double startHour = event.getStartDateTime().getHour() + event.getStartDateTime().getMinute() / 60.0;
            double endHour = event.getEndDateTime().getHour() + event.getEndDateTime().getMinute() / 60.0;

            // Clamp to visible range
            startHour = Math.max(startHour, START_HOUR);
            endHour = Math.min(endHour, END_HOUR + 1);

            // Handle events that end at midnight or next day
            if (endHour <= startHour) {
                endHour = END_HOUR + 1;
            }

            double topY = (startHour - START_HOUR) * HOUR_HEIGHT;
            double height = Math.max((endHour - startHour) * HOUR_HEIGHT, 20); // Minimum height of 20px

            Label eventLabel = createWeekEventLabel(event, height);
            eventLabel.setLayoutY(topY);
            eventLabel.setLayoutX(2);
            eventLabel.prefWidthProperty().bind(column.widthProperty().subtract(4));

            column.getChildren().add(eventLabel);
        }

        // Wrap in a VBox for proper grid layout
        VBox wrapper = new VBox(column);
        wrapper.setMaxWidth(Double.MAX_VALUE);
        wrapper.setFillWidth(true);
        HBox.setHgrow(wrapper, Priority.ALWAYS);
        VBox.setVgrow(column, Priority.ALWAYS);
        return wrapper;
    }

    private Label createWeekEventLabel(CalendarEvent event, double height) {
        CalendarSource source = controller.getCalendarSourceById(event.getCalendarSourceId());
        String color = source != null ? source.getColor() : "#4285f4";

        String timeStr = event.getStartDateTime().toLocalTime().toString();
        Label label = new Label(timeStr + " " + event.getTitle());
        label.setFont(Font.font("Poppins", 10));
        label.setMaxWidth(Double.MAX_VALUE);
        label.setPrefHeight(height);
        label.setMinHeight(height);
        label.setWrapText(true);
        label.setStyle(
                "-fx-background-color: " + color + "; " +
                        "-fx-text-fill: -roam-white; " +
                        "-fx-padding: 2 3 2 3; " +
                        "-fx-background-radius: 3; " +
                        "-fx-cursor: hand;");

        label.setOnMouseClicked(e -> {
            e.consume();
            controller.editEvent(event);
        });

        return label;
    }

    private VBox createDayView() {
        VBox dayView = new VBox(0);
        dayView.setStyle("-fx-background-color: -roam-bg-primary;");

        // All-day events section
        VBox allDaySection = new VBox(5);
        allDaySection.setPadding(new Insets(10));
        allDaySection.setStyle("-fx-background-color: -roam-gray-bg;");

        Label allDayLabel = new Label("All Day");
        allDayLabel.setFont(Font.font("Poppins Medium", 12));
        allDayLabel.setStyle("-fx-text-fill: -roam-text-secondary;");
        allDaySection.getChildren().add(allDayLabel);

        List<CalendarEvent> dayEvents = controller.getEventsForDate(currentDate);
        List<CalendarEvent> allDayEvents = dayEvents.stream()
                .filter(CalendarEvent::getIsAllDay)
                .collect(Collectors.toList());

        if (!allDayEvents.isEmpty()) {
            for (CalendarEvent event : allDayEvents) {
                Label eventLabel = createEventLabel(event);
                allDaySection.getChildren().add(eventLabel);
            }
        } else {
            Label noEvents = new Label("No all-day events");
            noEvents.setFont(Font.font("Poppins", 11));
            noEvents.setStyle("-fx-text-fill: -roam-text-hint;");
            allDaySection.getChildren().add(noEvents);
        }

        dayView.getChildren().add(allDaySection);

        // Time slots with absolute positioning for events
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        final double HOUR_HEIGHT = 80;
        final int TOTAL_HOURS = 24;

        HBox timeSlotContainer = new HBox(0);

        // Time column
        VBox timeColumn = new VBox(0);
        timeColumn.setMinWidth(80);
        timeColumn.setMaxWidth(80);
        timeColumn.setStyle("-fx-background-color: -roam-gray-bg;");

        // Create time labels
        for (int hour = 0; hour < TOTAL_HOURS; hour++) {
            Label timeLabel = new Label(String.format("%d:00", hour > 12 ? hour - 12 : (hour == 0 ? 12 : hour))
                    + (hour >= 12 ? " PM" : " AM"));
            timeLabel.setFont(Font.font("Poppins", 12));
            timeLabel.setPrefHeight(HOUR_HEIGHT);
            timeLabel.setAlignment(Pos.TOP_RIGHT);
            timeLabel.setPadding(new Insets(5, 10, 0, 0));
            timeLabel.setStyle("-fx-background-color: -roam-gray-bg; -fx-text-fill: -roam-text-secondary;");
            timeColumn.getChildren().add(timeLabel);
        }

        // Event area with absolute positioning using Pane
        javafx.scene.layout.Pane eventPane = new javafx.scene.layout.Pane();
        eventPane.setPrefHeight(HOUR_HEIGHT * TOTAL_HOURS);
        eventPane.setMinHeight(HOUR_HEIGHT * TOTAL_HOURS);
        eventPane.setStyle("-fx-background-color: -roam-bg-primary;");
        HBox.setHgrow(eventPane, Priority.ALWAYS);

        // Add hour grid lines
        for (int hour = 0; hour < TOTAL_HOURS; hour++) {
            Region hourLine = new Region();
            hourLine.setLayoutY(hour * HOUR_HEIGHT);
            hourLine.setPrefHeight(HOUR_HEIGHT);
            hourLine.prefWidthProperty().bind(eventPane.widthProperty());
            hourLine.setStyle("-fx-border-color: -roam-border; -fx-border-width: 0.5 0 0 0;");

            // Click handler for creating events
            hourLine.setOnMouseClicked(e -> controller.createEvent(currentDate));
            hourLine.setOnMouseEntered(e -> hourLine.setStyle(
                    "-fx-background-color: -roam-gray-light; -fx-border-color: -roam-border; -fx-border-width: 0.5 0 0 0; -fx-cursor: hand;"));
            hourLine.setOnMouseExited(e -> hourLine.setStyle(
                    "-fx-border-color: -roam-border; -fx-border-width: 0.5 0 0 0;"));

            eventPane.getChildren().add(hourLine);
        }

        // Add events with proper height based on duration
        List<CalendarEvent> timedEvents = dayEvents.stream()
                .filter(e -> !e.getIsAllDay())
                .collect(Collectors.toList());

        for (CalendarEvent event : timedEvents) {
            // Calculate position and height
            double startHour = event.getStartDateTime().getHour() + event.getStartDateTime().getMinute() / 60.0;
            double endHour = event.getEndDateTime().getHour() + event.getEndDateTime().getMinute() / 60.0;

            // Handle events that end at midnight or next day
            if (endHour <= startHour) {
                endHour = 24;
            }

            double topY = startHour * HOUR_HEIGHT;
            double height = Math.max((endHour - startHour) * HOUR_HEIGHT, 30); // Minimum height of 30px

            Label eventLabel = createDayEventLabel(event, height);
            eventLabel.setLayoutY(topY);
            eventLabel.setLayoutX(5);
            eventLabel.prefWidthProperty().bind(eventPane.widthProperty().subtract(10));

            eventPane.getChildren().add(eventLabel);
        }

        timeSlotContainer.getChildren().addAll(timeColumn, eventPane);
        scrollPane.setContent(timeSlotContainer);

        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        dayView.getChildren().add(scrollPane);

        return dayView;
    }

    private Label createDayEventLabel(CalendarEvent event, double height) {
        CalendarSource source = controller.getCalendarSourceById(event.getCalendarSourceId());
        String color = source != null ? source.getColor() : "#4285f4";

        String timeStr = event.getStartDateTime().toLocalTime().toString() + " - "
                + event.getEndDateTime().toLocalTime().toString();
        String text = event.getTitle() + "\n" + timeStr;
        if (event.getLocation() != null && !event.getLocation().isEmpty()) {
            text += "\n📍 " + event.getLocation();
        }

        Label label = new Label(text);
        label.setFont(Font.font("Poppins", 12));
        label.setMaxWidth(Double.MAX_VALUE);
        label.setPrefHeight(height);
        label.setMinHeight(height);
        label.setWrapText(true);
        label.setStyle(
                "-fx-background-color: " + color + "; " +
                        "-fx-text-fill: -roam-white; " +
                        "-fx-padding: 8 10 8 10; " +
                        "-fx-background-radius: 4; " +
                        "-fx-cursor: hand;");

        label.setOnMouseClicked(e -> {
            e.consume();
            controller.editEvent(event);
        });

        return label;
    }

    private VBox createMonthDayCell(LocalDate date) {
        VBox cell = new VBox(5);
        cell.setPadding(new Insets(5));
        cell.setMinHeight(100);
        cell.setStyle("-fx-background-color: -roam-bg-primary;");

        // Check if today
        boolean isToday = date.equals(LocalDate.now());
        // Check if current month
        boolean isCurrentMonth = YearMonth.from(date).equals(currentYearMonth);

        if (isToday) {
            cell.setStyle("-fx-background-color: -roam-blue-light;");
        }

        // Day number
        Label dayLabel = new Label(String.valueOf(date.getDayOfMonth()));
        dayLabel.setFont(Font.font("Poppins", 14));
        dayLabel.setStyle("-fx-text-fill: " + (isCurrentMonth ? "-roam-text-primary" : "-roam-text-hint"));
        dayLabel.setAlignment(Pos.TOP_RIGHT);
        dayLabel.setMaxWidth(Double.MAX_VALUE);

        cell.getChildren().add(dayLabel);

        // Get events for this date
        List<CalendarEvent> eventsOnDate = controller.getEventsForDate(date);

        // Show up to 3 events
        int count = 0;
        for (CalendarEvent event : eventsOnDate) {
            if (count >= 3) {
                int remaining = eventsOnDate.size() - 3;
                Label moreLabel = new Label("+" + remaining + " more");
                moreLabel.setFont(Font.font("Poppins", 11));
                moreLabel.setStyle(
                        "-fx-text-fill: -roam-blue; " +
                                "-fx-cursor: hand; " +
                                "-fx-underline: true;");
                cell.getChildren().add(moreLabel);
                break;
            }

            Label eventLabel = createEventLabel(event);
            cell.getChildren().add(eventLabel);
            count++;
        }

        // Click handler
        final LocalDate cellDate = date;
        cell.setOnMouseClicked(e -> controller.createEvent(cellDate));

        // Hover effect
        cell.setOnMouseEntered(e -> {
            if (!isToday) {
                cell.setStyle("-fx-background-color: -roam-gray-bg; -fx-cursor: hand;");
            }
        });
        cell.setOnMouseExited(e -> {
            if (!isToday) {
                cell.setStyle("-fx-background-color: -roam-bg-primary;");
            }
        });

        return cell;
    }

    private Label createEventLabel(CalendarEvent event) {
        CalendarSource source = controller.getCalendarSourceById(event.getCalendarSourceId());
        String color = source != null ? source.getColor() : "#4285f4";

        Label label = new Label(event.getTitle());
        label.setFont(Font.font("Poppins", 11));
        label.setMaxWidth(Double.MAX_VALUE);
        label.setStyle(
                "-fx-background-color: " + color + "; " +
                        "-fx-text-fill: #FFFFFF; " +
                        "-fx-padding: 2 5 2 5; " +
                        "-fx-background-radius: 3; " +
                        "-fx-cursor: hand;");

        // Truncate text
        label.setMaxHeight(18);

        // Click to edit
        label.setOnMouseClicked(e -> {
            e.consume();
            controller.editEvent(event);
        });

        return label;
    }

    private VBox createFilterPanel() {
        VBox panel = new VBox(20);
        panel.setPrefWidth(280);
        panel.setMinWidth(280);
        panel.setMaxWidth(280);
        panel.setPadding(new Insets(20));
        panel.setStyle(
                "-fx-background-color: -roam-gray-light; -fx-border-color: -roam-border; -fx-border-width: 0 0 0 1;");

        // Calendars section
        Label calendarsLabel = new Label("Calendars");
        calendarsLabel.setFont(Font.font("Poppins Bold", 16));

        VBox calendarsBox = new VBox(8);
        for (CalendarSource source : controller.getCalendarSources()) {
            CheckBox cb = new CheckBox(source.getName());
            cb.setFont(Font.font("Poppins", 14));
            cb.setSelected(source.getIsVisible());

            // Color indicator
            javafx.scene.shape.Circle colorDot = new javafx.scene.shape.Circle(5);
            colorDot.setFill(javafx.scene.paint.Color.web(source.getColor()));
            cb.setGraphic(colorDot);

            cb.setOnAction(e -> {
                controller.toggleCalendarVisibility(source.getId(), cb.isSelected());
                refreshCalendar();
            });

            calendarsBox.getChildren().add(cb);
        }

        panel.getChildren().addAll(calendarsLabel, calendarsBox);

        return panel;
    }

    private void toggleFilterPanel() {
        if (getRight() == null) {
            setRight(filterPanel);
        } else {
            setRight(null);
        }
    }

    private void refreshCalendar() {
        dateLabel.setText(getDateLabelText());

        // Recreate calendar view based on current view type
        calendarContainer.getChildren().clear();

        switch (currentViewType) {
            case AGENDA:
                ScrollPane agendaScroll = new ScrollPane(createAgendaView());
                agendaScroll.setFitToWidth(true);
                agendaScroll.setStyle("-fx-background-color: transparent;");
                calendarContainer.getChildren().add(agendaScroll);
                break;
            case MONTH:
                calendarContainer.getChildren().add(createMonthGrid());
                break;
            case WEEK:
                ScrollPane weekScroll = new ScrollPane(createWeekGrid());
                weekScroll.setFitToWidth(true);
                weekScroll.setStyle("-fx-background-color: transparent;");
                calendarContainer.getChildren().add(weekScroll);
                break;
            case DAY:
                calendarContainer.getChildren().add(createDayView());
                break;
        }

        refreshLegend();
    }

    private VBox createAgendaView() {
        VBox agendaView = new VBox(0);
        agendaView.setStyle("-fx-background-color: -roam-bg-primary;");

        // Get events for the month
        LocalDate startDate = currentYearMonth.atDay(1);
        LocalDate endDate = currentYearMonth.atEndOfMonth();

        List<CalendarEvent> monthEvents = controller.getAllEvents().stream()
                .filter(e -> {
                    LocalDate eventDate = e.getStartDateTime().toLocalDate();
                    return !eventDate.isBefore(startDate) && !eventDate.isAfter(endDate);
                })
                .sorted((e1, e2) -> e1.getStartDateTime().compareTo(e2.getStartDateTime()))
                .collect(Collectors.toList());

        if (monthEvents.isEmpty()) {
            VBox emptyBox = new VBox(20);
            emptyBox.setPadding(new Insets(50));
            emptyBox.setAlignment(Pos.CENTER);

            Label emptyLabel = new Label("No events scheduled for " + currentYearMonth.format(MONTH_YEAR_FORMATTER));
            emptyLabel.setFont(Font.font("Poppins", 16));
            emptyLabel.setStyle("-fx-text-fill: -roam-text-hint;");

            Button createBtn = new Button("+ Create Event");
            createBtn.setFont(Font.font("Poppins", 14));
            createBtn.getStyleClass().add("action-button");
            createBtn.setOnAction(e -> controller.createEvent(LocalDate.now()));

            emptyBox.getChildren().addAll(emptyLabel, createBtn);
            agendaView.getChildren().add(emptyBox);
            return agendaView;
        }

        // Group events by date
        LocalDate currentGroupDate = null;

        for (CalendarEvent event : monthEvents) {
            LocalDate eventDate = event.getStartDateTime().toLocalDate();

            // Add date header if new date
            if (currentGroupDate == null || !currentGroupDate.equals(eventDate)) {
                currentGroupDate = eventDate;

                HBox dateHeader = new HBox(15);
                dateHeader.setPadding(new Insets(20, 20, 10, 20));
                dateHeader.setAlignment(Pos.CENTER_LEFT);
                dateHeader.setStyle(
                        "-fx-background-color: -roam-gray-bg; " +
                                "-fx-border-color: -roam-border; " +
                                "-fx-border-width: 1 0 0 0;");

                // Day number
                Label dayNum = new Label(String.valueOf(eventDate.getDayOfMonth()));
                dayNum.setFont(Font.font("Poppins Bold", 24));

                boolean isToday = eventDate.equals(LocalDate.now());
                if (isToday) {
                    dayNum.setStyle(
                            "-fx-text-fill: #FFFFFF; " +
                                    "-fx-background-color: -roam-blue; " +
                                    "-fx-background-radius: 50%; " +
                                    "-fx-min-width: 45; " +
                                    "-fx-min-height: 45; " +
                                    "-fx-alignment: center;");
                } else {
                    dayNum.setStyle("-fx-text-fill: -roam-text-primary;");
                }

                // Date info
                VBox dateInfo = new VBox(2);
                Label dayName = new Label(eventDate.getDayOfWeek().toString().substring(0, 3));
                dayName.setFont(Font.font("Poppins Medium", 12));
                dayName.setStyle("-fx-text-fill: -roam-text-secondary;");

                Label monthYear = new Label(
                        eventDate.format(DateTimeFormatter.ofPattern("MMM yyyy")));
                monthYear.setFont(Font.font("Poppins", 12));
                monthYear.setStyle("-fx-text-fill: -roam-text-hint;");

                dateInfo.getChildren().addAll(dayName, monthYear);

                dateHeader.getChildren().addAll(dayNum, dateInfo);
                agendaView.getChildren().add(dateHeader);
            }

            // Add event item
            HBox eventItem = createAgendaEventItem(event);
            agendaView.getChildren().add(eventItem);
        }

        return agendaView;
    }

    private HBox createAgendaEventItem(CalendarEvent event) {
        HBox item = new HBox(15);
        item.setPadding(new Insets(15, 20, 15, 20));
        item.setAlignment(Pos.CENTER_LEFT);
        item.setStyle("-fx-background-color: -roam-bg-primary; -fx-cursor: hand;");

        // Calculate duration for visual sizing
        long durationMinutes = java.time.Duration.between(
                event.getStartDateTime(), event.getEndDateTime()).toMinutes();
        double durationHours = durationMinutes / 60.0;

        // Time
        VBox timeBox = new VBox(2);
        timeBox.setMinWidth(80);
        timeBox.setMaxWidth(80);

        if (event.getIsAllDay()) {
            Label allDayLabel = new Label("All Day");
            allDayLabel.setFont(Font.font("Poppins Medium", 12));
            allDayLabel.setStyle("-fx-text-fill: -roam-text-secondary;");
            timeBox.getChildren().add(allDayLabel);
        } else {
            Label startTime = new Label(
                    event.getStartDateTime().toLocalTime().format(DateTimeFormatter.ofPattern("h:mm a")));
            startTime.setFont(Font.font("Poppins Medium", 13));
            startTime.setStyle("-fx-text-fill: -roam-text-primary;");

            Label endTime = new Label(
                    event.getEndDateTime().toLocalTime().format(DateTimeFormatter.ofPattern("h:mm a")));
            endTime.setFont(Font.font("Poppins", 11));
            endTime.setStyle("-fx-text-fill: -roam-text-hint;");

            // Show duration
            String durationStr;
            if (durationMinutes < 60) {
                durationStr = durationMinutes + " min";
            } else if (durationMinutes % 60 == 0) {
                durationStr = (int) durationHours + " hr" + (durationHours > 1 ? "s" : "");
            } else {
                durationStr = String.format("%.1f hrs", durationHours);
            }
            Label durationLabel = new Label(durationStr);
            durationLabel.setFont(Font.font("Poppins", 10));
            durationLabel.setStyle("-fx-text-fill: -roam-text-hint;");

            timeBox.getChildren().addAll(startTime, endTime, durationLabel);
        }

        // Color bar - height scales with duration (min 40px, max 120px)
        CalendarSource source = controller.getCalendarSourceById(event.getCalendarSourceId());
        String color = source != null ? source.getColor() : "#4285f4";

        double barHeight = event.getIsAllDay() ? 50 : Math.max(40, Math.min(120, durationHours * 30));
        Region colorBar = new Region();
        colorBar.setMinWidth(4);
        colorBar.setMaxWidth(4);
        colorBar.setPrefHeight(barHeight);
        colorBar.setMinHeight(barHeight);
        colorBar.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 2;");

        // Event details
        VBox detailsBox = new VBox(4);
        HBox.setHgrow(detailsBox, Priority.ALWAYS);

        Label titleLabel = new Label(event.getTitle());
        titleLabel.setFont(Font.font("Poppins Medium", 14));
        titleLabel.setStyle("-fx-text-fill: -roam-text-primary;");

        detailsBox.getChildren().add(titleLabel);

        // Location
        if (event.getLocation() != null && !event.getLocation().isEmpty()) {
            Label locationLabel = new Label("📍 " + event.getLocation());
            locationLabel.setFont(Font.font("Poppins", 12));
            locationLabel.setStyle("-fx-text-fill: -roam-text-secondary;");
            detailsBox.getChildren().add(locationLabel);
        }

        // Calendar source
        if (source != null) {
            Label sourceLabel = new Label(source.getName());
            sourceLabel.setFont(Font.font("Poppins", 11));
            sourceLabel.setStyle("-fx-text-fill: -roam-text-hint;");
            detailsBox.getChildren().add(sourceLabel);
        }

        item.getChildren().addAll(timeBox, colorBar, detailsBox);

        // Hover effect
        item.setOnMouseEntered(e -> item.setStyle(
                "-fx-background-color: -roam-gray-bg; -fx-cursor: hand;"));
        item.setOnMouseExited(e -> item.setStyle(
                "-fx-background-color: -roam-bg-primary; -fx-cursor: hand;"));

        // Click to edit
        item.setOnMouseClicked(e -> controller.editEvent(event));

        return item;
    }

    private void refreshLegend() {
        legendContainer.getChildren().clear();

        List<CalendarSource> sources = controller.getCalendarSources();
        for (CalendarSource source : sources) {
            if (source.getIsVisible()) {
                HBox item = new HBox(8);
                item.setAlignment(Pos.CENTER_LEFT);

                Circle dot = new Circle(5);
                dot.setFill(Color.web(source.getColor()));

                Label name = new Label(source.getName());
                name.setFont(Font.font("Poppins", 12));
                name.setStyle("-fx-text-fill: -roam-text-secondary;");

                item.getChildren().addAll(dot, name);
                legendContainer.getChildren().add(item);
            }
        }
    }
}
