package com.roam.view;

import com.roam.service.SecurityContext;
import com.roam.util.StyleBuilder;
import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.util.Duration;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.ArrayList;
import java.util.List;

import static com.roam.util.UIConstants.*;

/**
 * Lock screen for PIN authentication.
 * <p>
 * This component provides a secure PIN entry interface with a clean numeric
 * keypad design
 * and animated PIN dots for visual feedback. It supports both mouse/touch input
 * via
 * the on-screen keypad and keyboard input for numbers 0-9 and backspace.
 * </p>
 * <p>
 * Features include:
 * </p>
 * <ul>
 * <li>Animated PIN dot indicators</li>
 * <li>Visual feedback for button presses</li>
 * <li>Error messaging for invalid PIN attempts</li>
 * <li>Keyboard and mouse input support</li>
 * </ul>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public class LockScreen extends StackPane {

    private final Runnable onUnlock;
    private final Label messageLabel;
    private final List<Circle> pinDots;
    private final StringBuilder currentPin;
    private final int PIN_LENGTH = 8;

    public LockScreen(Runnable onUnlock) {
        this.onUnlock = onUnlock;
        this.currentPin = new StringBuilder();
        this.pinDots = new ArrayList<>();

        setStyle("-fx-background-color: " + BG_PRIMARY + ";");

        VBox content = new VBox(SPACING_LG);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(320);
        content.setPadding(new Insets(SPACING_SECTION));

        // Icon container with circular background
        VBox iconBox = new VBox();
        iconBox.setAlignment(Pos.CENTER);
        iconBox.setStyle(StyleBuilder.create()
                .backgroundColor(BLUE_LIGHT)
                .backgroundRadiusCircle()
                .minWidth(60).minHeight(60)
                .maxWidth(60).maxHeight(60)
                .build());

        // Lock icon using SVG path
        SVGPath lockIcon = new SVGPath();
        // Lock icon SVG path (padlock shape)
        lockIcon.setContent(
                "M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z");
        lockIcon.setFill(Color.web("#3B82F6"));
        lockIcon.setScaleX(1.2);
        lockIcon.setScaleY(1.2);
        iconBox.getChildren().add(lockIcon);

        // Title
        Label title = new Label("App Locked");
        title.setFont(Font.font(FONT_BOLD, FONT_SIZE_TITLE));
        title.setStyle("-fx-text-fill: " + TEXT_PRIMARY + ";");

        // Subtitle
        Label subtitle = new Label("Enter your PIN to access Roam");
        subtitle.setFont(Font.font(FONT_REGULAR, FONT_SIZE_MD));
        subtitle.setStyle("-fx-text-fill: " + TEXT_SECONDARY + ";");

        // PIN Dots
        HBox dotsContainer = new HBox(SPACING_SM);
        dotsContainer.setAlignment(Pos.CENTER);
        dotsContainer.setPadding(new Insets(SPACING_STANDARD, 0, SPACING_STANDARD, 0));

        for (int i = 0; i < PIN_LENGTH; i++) {
            Circle dot = new Circle(4);
            dot.getStyleClass().add("pin-dot");
            pinDots.add(dot);
            dotsContainer.getChildren().add(dot);
        }

        // Keypad
        GridPane keypad = createKeypad();

        // Message Label
        messageLabel = new Label("");
        messageLabel.setFont(Font.font(FONT_REGULAR, FONT_SIZE_MD + 1));
        messageLabel.setStyle("-fx-text-fill: " + RED + ";");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(300);
        messageLabel.setAlignment(Pos.CENTER);

        // Keyboard hint
        Label keyboardHint = new Label("You can also use keyboard numbers");
        keyboardHint.setFont(Font.font(FONT_REGULAR, FONT_SIZE_SM));
        keyboardHint.setStyle("-fx-text-fill: " + TEXT_HINT + ";");

        // Footer
        Label footer = new Label("Roam Security");
        footer.setFont(Font.font(FONT_REGULAR, FONT_SIZE_MD));
        footer.setStyle("-fx-text-fill: " + TEXT_HINT + ";");
        VBox.setMargin(footer, new Insets(SPACING_STANDARD, 0, 0, 0));

        content.getChildren().addAll(iconBox, title, subtitle, dotsContainer, messageLabel, keypad, keyboardHint,
                footer);
        getChildren().add(content);

        // Add listeners for responsive scaling
        widthProperty().addListener((obs, oldVal, newVal) -> scaleContent(content));
        heightProperty().addListener((obs, oldVal, newVal) -> scaleContent(content));

        // Enable keyboard input
        setupKeyboardInput();

        // Request focus to enable keyboard input
        setFocusTraversable(true);
    }

    /**
     * Setup keyboard event handlers for numeric input
     */
    private void setupKeyboardInput() {
        // Handle key pressed events for digits, backspace, and enter
        addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            KeyCode code = event.getCode();

            // Handle number keys (both main keyboard and numpad)
            if (code.isDigitKey()) {
                String digit = code.getName().replace("Numpad ", "");
                // For regular digit keys, get the character
                if (code.getName().startsWith("Digit")) {
                    digit = code.getName().replace("Digit", "");
                }
                handleDigit(digit);
                event.consume();
            }
            // Handle backspace
            else if (code == KeyCode.BACK_SPACE || code == KeyCode.DELETE) {
                handleBackspace();
                event.consume();
            }
            // Handle enter to check PIN
            else if (code == KeyCode.ENTER && currentPin.length() == PIN_LENGTH) {
                checkPin();
                event.consume();
            }
        });
        // Note: Removed KEY_TYPED handler to prevent double input
    }

    /**
     * Request focus when the lock screen becomes visible
     */
    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        if (!isFocused() && getScene() != null) {
            requestFocus();
        }
    }

    private void scaleContent(VBox content) {
        double width = getWidth();
        double height = getHeight();

        // Use layout bounds to get the actual size of the content
        double contentWidth = content.getLayoutBounds().getWidth();
        double contentHeight = content.getLayoutBounds().getHeight();

        if (contentWidth == 0 || contentHeight == 0)
            return;

        // Calculate scale factors
        double scaleX = width < contentWidth + 40 ? (width - 40) / contentWidth : 1.0;
        double scaleY = height < contentHeight + 40 ? (height - 40) / contentHeight : 1.0;

        // Use the smaller scale to maintain aspect ratio and fit within bounds
        double scale = Math.min(scaleX, scaleY);

        content.setScaleX(scale);
        content.setScaleY(scale);
    }

    private GridPane createKeypad() {
        GridPane grid = new GridPane();
        grid.setHgap(SPACING_STANDARD);
        grid.setVgap(SPACING_STANDARD);
        grid.setAlignment(Pos.CENTER);

        int number = 1;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                Button btn = createKeypadButton(String.valueOf(number));
                grid.add(btn, col, row);
                number++;
            }
        }

        // Bottom row
        Button zeroBtn = createKeypadButton("0");
        grid.add(zeroBtn, 1, 3);

        Button backspaceBtn = new Button();
        FontIcon backIcon = new FontIcon(Feather.DELETE);
        backIcon.setIconSize(ICON_LG);
        backspaceBtn.setGraphic(backIcon);
        backspaceBtn.getStyleClass().add("keypad-button");
        backspaceBtn.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        backspaceBtn.setOnAction(e -> handleBackspace());
        grid.add(backspaceBtn, 2, 3);

        return grid;
    }

    private Button createKeypadButton(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().add("keypad-button");
        btn.setOnAction(e -> handleDigit(text));
        return btn;
    }

    private void handleDigit(String digit) {
        if (currentPin.length() < PIN_LENGTH) {
            currentPin.append(digit);
            updateDots();

            if (currentPin.length() == PIN_LENGTH) {
                // Small delay to show the last dot filled before checking
                Timeline timeline = new Timeline(
                        new KeyFrame(Duration.millis(100), event -> checkPin()));
                timeline.play();
            }
        }
    }

    private void handleBackspace() {
        if (currentPin.length() > 0) {
            currentPin.deleteCharAt(currentPin.length() - 1);
            updateDots();
            messageLabel.setText("");
        }
    }

    private void updateDots() {
        for (int i = 0; i < PIN_LENGTH; i++) {
            Circle dot = pinDots.get(i);
            if (i < currentPin.length()) {
                if (!dot.getStyleClass().contains("pin-dot-filled")) {
                    dot.getStyleClass().add("pin-dot-filled");

                    // Add pop animation effect
                    ScaleTransition scale = new ScaleTransition(Duration.millis(150), dot);
                    scale.setFromX(1.0);
                    scale.setFromY(1.0);
                    scale.setToX(1.5);
                    scale.setToY(1.5);
                    scale.setAutoReverse(true);
                    scale.setCycleCount(2);
                    scale.play();
                }
            } else {
                dot.getStyleClass().remove("pin-dot-filled");
            }
        }
    }

    private void checkPin() {
        String pin = currentPin.toString();

        try {
            if (SecurityContext.getInstance().authenticate(pin)) {
                // Success
                messageLabel.setStyle("-fx-text-fill: " + GREEN + ";");
                messageLabel.setText("Unlocked");

                // Small delay before unlocking for UX
                Timeline timeline = new Timeline(
                        new KeyFrame(Duration.millis(300), event -> onUnlock.run()));
                timeline.play();

            } else {
                // Failed authentication
                showError("Incorrect PIN");
                shakeAnimation();

                // Clear PIN after delay
                Timeline timeline = new Timeline(
                        new KeyFrame(Duration.millis(500), event -> {
                            currentPin.setLength(0);
                            updateDots();
                        }));
                timeline.play();
            }
        } catch (SecurityException e) {
            // Rate limit exceeded
            showError(e.getMessage());
            setDisable(true);

            // Re-enable after 60 seconds
            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.seconds(60), event -> {
                        setDisable(false);
                        messageLabel.setText("");
                        currentPin.setLength(0);
                        updateDots();
                    }));
            timeline.play();
        }
    }

    private void showError(String message) {
        messageLabel.setStyle("-fx-text-fill: " + RED + ";");
        messageLabel.setText(message);
    }

    private void shakeAnimation() {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(0), new javafx.animation.KeyValue(translateXProperty(), 0)),
                new KeyFrame(Duration.millis(50), new javafx.animation.KeyValue(translateXProperty(), -10)),
                new KeyFrame(Duration.millis(100), new javafx.animation.KeyValue(translateXProperty(), 10)),
                new KeyFrame(Duration.millis(150), new javafx.animation.KeyValue(translateXProperty(), -10)),
                new KeyFrame(Duration.millis(200), new javafx.animation.KeyValue(translateXProperty(), 10)),
                new KeyFrame(Duration.millis(250), new javafx.animation.KeyValue(translateXProperty(), 0)));
        timeline.play();
    }
}
