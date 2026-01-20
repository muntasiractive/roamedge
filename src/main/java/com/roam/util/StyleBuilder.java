package com.roam.util;

import static com.roam.util.UIConstants.*;

/**
 * Fluent builder for creating consistent inline CSS styles in JavaFX.
 * 
 * <p>
 * This utility class helps maintain consistent styling across the application
 * by providing a type-safe, fluent API for building CSS style strings. It
 * supports
 * all common JavaFX CSS properties including backgrounds, borders, text,
 * spacing,
 * effects, and transformations.
 * </p>
 * 
 * <h2>Features</h2>
 * <ul>
 * <li>Fluent API for chaining style properties</li>
 * <li>Integration with {@link UIConstants} for consistent values</li>
 * <li>Preset styles for common components (cards, buttons, badges)</li>
 * <li>Support for CSS variables and direct values</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * 
 * <pre>{@code
 * // Build a custom style
 * String style = StyleBuilder.create()
 *         .backgroundColor(BG_PRIMARY)
 *         .borderRadius(RADIUS_STANDARD)
 *         .padding(SPACING_STANDARD)
 *         .cursor("hand")
 *         .build();
 * 
 * // Use a preset style
 * String cardStyle = StyleBuilder.cardStyle();
 * }</pre>
 * 
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 * @see UIConstants
 */
public class StyleBuilder {

    // ========================================
    // INSTANCE VARIABLES
    // ========================================

    /** StringBuilder to accumulate CSS properties. */
    private final StringBuilder style;

    // ========================================
    // CONSTRUCTOR
    // ========================================

    /**
     * Private constructor for the builder pattern.
     * Use {@link #create()} or {@link #from(String)} to create instances.
     */
    private StyleBuilder() {
        this.style = new StringBuilder();
    }

    // ========================================
    // FACTORY METHODS
    // ========================================

    /**
     * Creates a new StyleBuilder instance.
     * 
     * @return A new StyleBuilder
     */
    public static StyleBuilder create() {
        return new StyleBuilder();
    }

    /**
     * Creates a StyleBuilder initialized with an existing style string.
     * 
     * @param existingStyle The existing style to start with
     * @return A new StyleBuilder with existing styles
     */
    public static StyleBuilder from(String existingStyle) {
        StyleBuilder builder = new StyleBuilder();
        if (existingStyle != null && !existingStyle.isEmpty()) {
            builder.style.append(existingStyle);
            if (!existingStyle.endsWith(";")) {
                builder.style.append("; ");
            } else if (!existingStyle.endsWith(" ")) {
                builder.style.append(" ");
            }
        }
        return builder;
    }

    // ========================================
    // BACKGROUND PROPERTIES
    // ========================================

    /**
     * Sets the background color using a CSS variable reference.
     */
    public StyleBuilder backgroundColor(String cssVariable) {
        return addProperty("-fx-background-color", cssVariable);
    }

    /**
     * Sets the background color using a hex color.
     */
    public StyleBuilder backgroundColorHex(String hexColor) {
        return addProperty("-fx-background-color", hexColor);
    }

    /**
     * Sets the background radius using the standard value.
     */
    public StyleBuilder backgroundRadius() {
        return backgroundRadius(RADIUS_STANDARD);
    }

    /**
     * Sets the background radius.
     */
    public StyleBuilder backgroundRadius(int radius) {
        return addProperty("-fx-background-radius", radius + "");
    }

    /**
     * Sets the background radius for specific corners.
     * Values are in order: top-left, top-right, bottom-right, bottom-left
     */
    public StyleBuilder backgroundRadius(int topLeft, int topRight, int bottomRight, int bottomLeft) {
        return addProperty("-fx-background-radius",
                topLeft + " " + topRight + " " + bottomRight + " " + bottomLeft);
    }

    /**
     * Sets the background radius to circular (50%).
     */
    public StyleBuilder backgroundRadiusCircle() {
        return addProperty("-fx-background-radius", RADIUS_CIRCLE);
    }

    // ========================================
    // BORDER PROPERTIES
    // ========================================

    /**
     * Sets the border color using a CSS variable reference.
     */
    public StyleBuilder borderColor(String cssVariable) {
        return addProperty("-fx-border-color", cssVariable);
    }

    /**
     * Sets the border width.
     */
    public StyleBuilder borderWidth(int width) {
        return addProperty("-fx-border-width", width + "");
    }

    /**
     * Sets different border widths for each side.
     */
    public StyleBuilder borderWidth(int top, int right, int bottom, int left) {
        return addProperty("-fx-border-width",
                top + " " + right + " " + bottom + " " + left);
    }

    /**
     * Sets the border radius using the standard value.
     */
    public StyleBuilder borderRadius() {
        return borderRadius(RADIUS_STANDARD);
    }

    /**
     * Sets the border radius.
     */
    public StyleBuilder borderRadius(int radius) {
        return addProperty("-fx-border-radius", radius + "");
    }

    /**
     * Sets the border radius for specific corners.
     */
    public StyleBuilder borderRadius(int topLeft, int topRight, int bottomRight, int bottomLeft) {
        return addProperty("-fx-border-radius",
                topLeft + " " + topRight + " " + bottomRight + " " + bottomLeft);
    }

    /**
     * Sets both background and border radius to the same value.
     */
    public StyleBuilder radius(int radius) {
        return backgroundRadius(radius).borderRadius(radius);
    }

    /**
     * Sets both background and border radius to standard value (8px).
     */
    public StyleBuilder radius() {
        return radius(RADIUS_STANDARD);
    }

    /**
     * Sets border to left side only with specified color and width.
     */
    public StyleBuilder borderLeft(String colorVar, int width) {
        return addProperty("-fx-border-color", colorVar)
                .addProperty("-fx-border-width", "0 0 0 " + width);
    }

    // ========================================
    // TEXT PROPERTIES
    // ========================================

    /**
     * Sets the text fill color using a CSS variable reference.
     */
    public StyleBuilder textFill(String cssVariable) {
        return addProperty("-fx-text-fill", cssVariable);
    }

    /**
     * Sets the font family.
     */
    public StyleBuilder fontFamily(String fontFamily) {
        return addProperty("-fx-font-family", fontFamily);
    }

    /**
     * Sets the font size.
     */
    public StyleBuilder fontSize(int size) {
        return addProperty("-fx-font-size", size + "px");
    }

    // ========================================
    // SPACING PROPERTIES
    // ========================================

    /**
     * Sets padding on all sides.
     */
    public StyleBuilder padding(int padding) {
        return addProperty("-fx-padding", padding + "");
    }

    /**
     * Sets padding with vertical and horizontal values.
     */
    public StyleBuilder padding(int vertical, int horizontal) {
        return addProperty("-fx-padding", vertical + " " + horizontal);
    }

    /**
     * Sets padding for each side individually.
     */
    public StyleBuilder padding(int top, int right, int bottom, int left) {
        return addProperty("-fx-padding",
                top + " " + right + " " + bottom + " " + left);
    }

    /**
     * Sets spacing (for VBox/HBox).
     */
    public StyleBuilder spacing(int spacing) {
        return addProperty("-fx-spacing", spacing + "");
    }

    // ========================================
    // EFFECTS
    // ========================================

    /**
     * Sets the effect (shadow, etc.).
     */
    public StyleBuilder effect(String effect) {
        return addProperty("-fx-effect", effect);
    }

    /**
     * Applies the standard card shadow.
     */
    public StyleBuilder cardShadow() {
        return effect(SHADOW_CARD);
    }

    /**
     * Applies the hover shadow.
     */
    public StyleBuilder hoverShadow() {
        return effect(SHADOW_HOVER);
    }

    /**
     * Applies the light shadow.
     */
    public StyleBuilder lightShadow() {
        return effect(SHADOW_LIGHT);
    }

    // ========================================
    // CURSOR & INTERACTION
    // ========================================

    /**
     * Sets the cursor type.
     */
    public StyleBuilder cursor(String cursorType) {
        return addProperty("-fx-cursor", cursorType);
    }

    /**
     * Sets cursor to hand (pointer).
     */
    public StyleBuilder cursorHand() {
        return cursor("hand");
    }

    // ========================================
    // DIMENSIONS
    // ========================================

    /**
     * Sets the preferred width.
     */
    public StyleBuilder prefWidth(int width) {
        return addProperty("-fx-pref-width", width + "");
    }

    /**
     * Sets the preferred height.
     */
    public StyleBuilder prefHeight(int height) {
        return addProperty("-fx-pref-height", height + "");
    }

    /**
     * Sets min width.
     */
    public StyleBuilder minWidth(int width) {
        return addProperty("-fx-min-width", width + "");
    }

    /**
     * Sets min height.
     */
    public StyleBuilder minHeight(int height) {
        return addProperty("-fx-min-height", height + "");
    }

    /**
     * Sets max width.
     */
    public StyleBuilder maxWidth(int width) {
        return addProperty("-fx-max-width", width + "");
    }

    /**
     * Sets max height.
     */
    public StyleBuilder maxHeight(int height) {
        return addProperty("-fx-max-height", height + "");
    }

    // ========================================
    // TRANSFORMATION
    // ========================================

    /**
     * Sets scale transformation.
     */
    public StyleBuilder scale(double scaleX, double scaleY) {
        return addProperty("-fx-scale-x", String.valueOf(scaleX))
                .addProperty("-fx-scale-y", String.valueOf(scaleY));
    }

    /**
     * Sets uniform scale.
     */
    public StyleBuilder scale(double scale) {
        return scale(scale, scale);
    }

    /**
     * Sets translate Y.
     */
    public StyleBuilder translateY(int y) {
        return addProperty("-fx-translate-y", y + "");
    }

    // ========================================
    // ALIGNMENT
    // ========================================

    /**
     * Sets alignment.
     */
    public StyleBuilder alignment(String alignment) {
        return addProperty("-fx-alignment", alignment);
    }

    // ========================================
    // ICON PROPERTIES
    // ========================================

    /**
     * Sets icon color using CSS variable.
     */
    public StyleBuilder iconColor(String cssVariable) {
        return addProperty("-fx-icon-color", cssVariable);
    }

    /**
     * Sets icon size.
     */
    public StyleBuilder iconSize(int size) {
        return addProperty("-fx-icon-size", size + "");
    }

    // ========================================
    // RAW PROPERTY
    // ========================================

    /**
     * Adds a raw CSS property and value.
     */
    public StyleBuilder addProperty(String property, String value) {
        style.append(property).append(": ").append(value).append("; ");
        return this;
    }

    /**
     * Adds raw CSS string (useful for conditionally adding styles).
     */
    public StyleBuilder addRaw(String rawCss) {
        if (rawCss != null && !rawCss.isEmpty()) {
            style.append(rawCss);
            if (!rawCss.endsWith(" ")) {
                style.append(" ");
            }
        }
        return this;
    }

    // ========================================
    // BUILD
    // ========================================

    /**
     * Builds the final CSS style string.
     * 
     * @return The complete CSS style string
     */
    public String build() {
        return style.toString().trim();
    }

    @Override
    public String toString() {
        return build();
    }

    // ========================================
    // PRESET STYLES
    // ========================================

    /**
     * Creates a standard card style.
     */
    public static String cardStyle() {
        return create()
                .backgroundColor(BG_PRIMARY)
                .borderColor(BORDER)
                .borderWidth(1)
                .radius()
                .cursorHand()
                .cardShadow()
                .build();
    }

    /**
     * Creates a card hover style.
     */
    public static String cardHoverStyle() {
        return create()
                .backgroundColor(BG_PRIMARY)
                .borderColor(BORDER)
                .borderWidth(1)
                .radius()
                .cursorHand()
                .hoverShadow()
                .scale(1.02)
                .build();
    }

    /**
     * Creates a section box style (used in settings, etc.).
     */
    public static String sectionStyle() {
        return create()
                .backgroundColor(BG_PRIMARY)
                .borderColor(BORDER)
                .borderWidth(1)
                .radius()
                .padding(SPACING_STANDARD)
                .build();
    }

    /**
     * Creates a badge style with background color.
     */
    public static String badgeStyle(String bgColor, String textColor) {
        return create()
                .backgroundColor(bgColor)
                .textFill(textColor)
                .padding(SPACING_XS, SPACING_SM)
                .backgroundRadius(RADIUS_LARGE)
                .build();
    }

    /**
     * Creates a primary button style.
     */
    public static String primaryButtonStyle() {
        return create()
                .backgroundColor(BLUE)
                .textFill(TEXT_WHITE)
                .padding(10, 20)
                .radius()
                .cursorHand()
                .build();
    }

    /**
     * Creates a secondary button style.
     */
    public static String secondaryButtonStyle() {
        return create()
                .backgroundColor(BG_GRAY)
                .textFill(TEXT_PRIMARY)
                .padding(SPACING_SM, SPACING_STANDARD)
                .radius()
                .cursorHand()
                .build();
    }

    /**
     * Creates a transparent button style.
     */
    public static String transparentButtonStyle() {
        return create()
                .backgroundColor("transparent")
                .cursorHand()
                .padding(2)
                .build();
    }
}
