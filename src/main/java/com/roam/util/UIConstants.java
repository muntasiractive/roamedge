package com.roam.util;

/**
 * Centralized UI constants for consistent styling across the application.
 * 
 * <p>
 * This final utility class provides standard values for styling components,
 * ensuring visual consistency throughout the application. Use these constants
 * instead of hardcoded values to maintain a unified design system.
 * </p>
 * 
 * <h2>Categories</h2>
 * <ul>
 * <li><b>Border Radius</b> - Corner rounding values (standard: 8px)</li>
 * <li><b>Padding &amp; Spacing</b> - Consistent spacing values from 4px to
 * 30px</li>
 * <li><b>CSS Variables</b> - References to CSS custom properties for
 * colors</li>
 * <li><b>Shadow Effects</b> - Pre-defined drop shadow effects for
 * elevation</li>
 * <li><b>Font Families</b> - Poppins font family variants</li>
 * <li><b>Font Sizes</b> - Standardized text sizing from 10px to 32px</li>
 * <li><b>Component Sizes</b> - Standard dimensions for buttons, inputs,
 * icons</li>
 * <li><b>Animation Durations</b> - Timing values for transitions</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * 
 * <pre>{@code
 * import static com.roam.util.UIConstants.*;
 * 
 * // Use spacing constants
 * vbox.setSpacing(SPACING_STANDARD);
 * 
 * // Use radius constants
 * String style = "-fx-background-radius: " + RADIUS_STANDARD;
 * 
 * // Use with StyleBuilder
 * StyleBuilder.create()
 *     .backgroundColor(BG_PRIMARY)
 *     .padding(SPACING_MD)
 *     .build();
 * }</pre>
 * 
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 * @see StyleBuilder
 */
public final class UIConstants {

    // ========================================
    // CONSTRUCTOR
    // ========================================

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static constants.
     */
    private UIConstants() {
        // Prevent instantiation
    }

    // ========================================
    // BORDER RADIUS
    // ========================================

    /** Standard border radius for cards, containers, and most components */
    public static final int RADIUS_STANDARD = 8;

    /** Small border radius for badges, tags, small buttons */
    public static final int RADIUS_SMALL = 4;

    /** Large border radius for modals, dialogs, large containers */
    public static final int RADIUS_LARGE = 12;

    /** Circular radius (for icons, avatars, etc.) - use "50%" */
    public static final String RADIUS_CIRCLE = "50%";

    // ========================================
    // PADDING & SPACING
    // ========================================

    /** Extra small spacing (4px) */
    public static final int SPACING_XS = 4;

    /** Small spacing (8px) */
    public static final int SPACING_SM = 8;

    /** Medium spacing (12px) */
    public static final int SPACING_MD = 12;

    /** Standard spacing (16px) - default for most use cases */
    public static final int SPACING_STANDARD = 16;

    /** Large spacing (20px) */
    public static final int SPACING_LG = 20;

    /** Extra large spacing (24px) */
    public static final int SPACING_XL = 24;

    /** Section spacing (30px) - between major sections */
    public static final int SPACING_SECTION = 30;

    // ========================================
    // CSS VARIABLE REFERENCES
    // ========================================

    // Background Colors
    public static final String BG_PRIMARY = "-roam-bg-primary";
    public static final String BG_GRAY = "-roam-gray-bg";
    public static final String BG_GRAY_LIGHT = "-roam-gray-light";

    // Text Colors
    public static final String TEXT_PRIMARY = "-roam-text-primary";
    public static final String TEXT_SECONDARY = "-roam-text-secondary";
    public static final String TEXT_HINT = "-roam-text-hint";
    public static final String TEXT_WHITE = "-roam-white";

    // Border Colors
    public static final String BORDER = "-roam-border";

    // Brand Colors
    public static final String BLUE = "-roam-blue";
    public static final String BLUE_HOVER = "-roam-blue-hover";
    public static final String BLUE_LIGHT = "-roam-blue-light";

    // Status Colors
    public static final String RED = "-roam-red";
    public static final String RED_BG = "-roam-red-bg";
    public static final String ORANGE = "-roam-orange";
    public static final String ORANGE_BG = "-roam-orange-bg";
    public static final String YELLOW = "-roam-yellow";
    public static final String YELLOW_BG = "-roam-yellow-bg";
    public static final String GREEN = "-roam-green";
    public static final String GREEN_BG = "-roam-green-bg";
    public static final String PURPLE = "-roam-purple";
    public static final String PURPLE_BG = "-roam-purple-bg";

    // Priority Colors
    public static final String PRIORITY_HIGH = "-roam-priority-high";
    public static final String PRIORITY_MEDIUM = "-roam-priority-medium";
    public static final String PRIORITY_LOW = "-roam-priority-low";

    // Interactive States
    public static final String ROW_HOVER = "-roam-row-hover";
    public static final String ROW_SELECTED = "-roam-row-selected";

    // ========================================
    // CSS RADIUS VARIABLE REFERENCES
    // ========================================

    public static final String CSS_RADIUS = "-roam-radius";
    public static final String CSS_RADIUS_SMALL = "-roam-radius-small";
    public static final String CSS_RADIUS_TINY = "-roam-radius-tiny";

    // ========================================
    // SHADOW EFFECTS
    // ========================================

    /** Subtle shadow for cards */
    public static final String SHADOW_CARD = "dropshadow(gaussian, rgba(0,0,0,0.10), 12, 0, 0, 4)";

    /** Hover shadow for interactive elements */
    public static final String SHADOW_HOVER = "dropshadow(gaussian, rgba(0,0,0,0.15), 16, 0, 0, 6)";

    /** Light shadow for subtle elevation */
    public static final String SHADOW_LIGHT = "dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2)";

    // ========================================
    // FONT FAMILIES
    // ========================================

    /** Poppins Regular - for body text */
    public static final String FONT_REGULAR = "Poppins";
    /** Poppins Medium - for medium weight text */
    public static final String FONT_MEDIUM = "Poppins Medium";
    /** Poppins Bold - for headings and emphasis */
    public static final String FONT_BOLD = "Poppins Bold";
    /** Poppins SemiBold - for subheadings */
    public static final String FONT_SEMIBOLD = "Poppins SemiBold";

    // ========================================
    // FONT SIZES
    // ========================================

    public static final int FONT_SIZE_XS = 10;
    public static final int FONT_SIZE_SM = 11;
    public static final int FONT_SIZE_MD = 12;
    public static final int FONT_SIZE_STANDARD = 14;
    public static final int FONT_SIZE_LG = 16;
    public static final int FONT_SIZE_XL = 18;
    public static final int FONT_SIZE_TITLE = 20;
    public static final int FONT_SIZE_HEADER = 24;
    public static final int FONT_SIZE_DISPLAY = 32;

    // ========================================
    // COMPONENT SIZES
    // ========================================

    /** Standard button height */
    public static final int BUTTON_HEIGHT = 36;

    /** Standard input field height */
    public static final int INPUT_HEIGHT = 40;

    /** Minimum card height */
    public static final int CARD_MIN_HEIGHT = 100;

    /** Icon size - small */
    public static final int ICON_SM = 14;

    /** Icon size - medium */
    public static final int ICON_MD = 16;

    /** Icon size - large */
    public static final int ICON_LG = 20;

    /** Icon size - extra large */
    public static final int ICON_XL = 24;

    // ========================================
    // ANIMATION DURATIONS (milliseconds)
    // ========================================

    public static final int ANIMATION_FAST = 100;
    public static final int ANIMATION_NORMAL = 200;
    public static final int ANIMATION_SLOW = 300;
}
