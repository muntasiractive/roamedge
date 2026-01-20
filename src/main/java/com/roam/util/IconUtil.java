package com.roam.util;

import javafx.scene.Node;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Utility class for creating consistent icons throughout the application.
 * <p>
 * This class provides factory methods for creating {@link FontIcon} instances
 * using the Feather icon pack from Ikonli. All icons in the application should
 * be created through this utility to ensure consistent sizing and styling.
 * </p>
 * 
 * <h2>Usage:</h2>
 * 
 * <pre>{@code
 * // Create icon with default size (16px)
 * FontIcon icon = IconUtil.createIcon(Feather.STAR);
 * 
 * // Create icon with custom size
 * FontIcon largeIcon = IconUtil.createIcon(Feather.CALENDAR, 24);
 * 
 * // Use common shortcuts
 * Node starIcon = IconUtil.star();
 * }</pre>
 * 
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 * @see FontIcon
 * @see Feather
 */
public class IconUtil {

    // ==================== Icon Factory Methods ====================

    /**
     * Creates a FontIcon with the default size of 16 pixels.
     * 
     * @param icon the Feather icon to create
     * @return a FontIcon configured with the specified icon
     */
    public static FontIcon createIcon(Feather icon) {
        return createIcon(icon, 16);
    }

    /**
     * Creates a FontIcon with a custom size.
     * 
     * @param icon the Feather icon to create
     * @param size the icon size in pixels
     * @return a FontIcon configured with the specified icon and size
     */
    public static FontIcon createIcon(Feather icon, int size) {
        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconSize(size);
        return fontIcon;
    }

    /**
     * Creates a FontIcon with a custom size and inline CSS style.
     * 
     * @param icon  the Feather icon to create
     * @param size  the icon size in pixels
     * @param style the inline CSS style to apply (e.g., "-fx-fill: #FFD700;")
     * @return a FontIcon configured with the specified icon, size, and style
     */
    public static FontIcon createIcon(Feather icon, int size, String style) {
        FontIcon fontIcon = createIcon(icon, size);
        fontIcon.setStyle(style);
        return fontIcon;
    }

    // ==================== Common Icon Shortcuts ====================

    /**
     * Creates a star icon.
     * 
     * @return a star icon node
     */
    public static Node star() {
        return createIcon(Feather.STAR);
    }

    /**
     * Creates a filled star icon with gold color styling.
     * 
     * @return a gold-filled star icon node
     */
    public static Node starFilled() {
        FontIcon icon = createIcon(Feather.STAR);
        icon.setStyle("-fx-fill: #FFD700;");
        return icon;
    }

    /**
     * Creates a calendar icon.
     * 
     * @return a calendar icon node
     */
    public static Node calendar() {
        return createIcon(Feather.CALENDAR);
    }

    /**
     * Creates a clipboard icon.
     * 
     * @return a clipboard icon node
     */
    public static Node clipboard() {
        return createIcon(Feather.CLIPBOARD);
    }

    /**
     * Creates a check-square icon for task completion.
     * 
     * @return a check-square icon node
     */
    public static Node checkSquare() {
        return createIcon(Feather.CHECK_SQUARE);
    }

    /**
     * Creates a file-text icon for documents.
     * 
     * @return a file-text icon node
     */
    public static Node fileText() {
        return createIcon(Feather.FILE_TEXT);
    }

    /**
     * Creates an edit/pencil icon.
     * 
     * @return an edit icon node
     */
    public static Node edit() {
        return createIcon(Feather.EDIT);
    }

    /**
     * Creates a search/magnifying glass icon.
     * 
     * @return a search icon node
     */
    public static Node search() {
        return createIcon(Feather.SEARCH);
    }

    /**
     * Creates a settings/gear icon.
     * 
     * @return a settings icon node
     */
    public static Node settings() {
        return createIcon(Feather.SETTINGS);
    }

    /**
     * Creates a trash/delete icon.
     * 
     * @return a trash icon node
     */
    public static Node trash() {
        return createIcon(Feather.TRASH_2);
    }

    /**
     * Creates an image icon.
     * 
     * @return an image icon node
     */
    public static Node image() {
        return createIcon(Feather.IMAGE);
    }

    /**
     * Creates a generic file icon.
     * 
     * @return a file icon node
     */
    public static Node file() {
        return createIcon(Feather.FILE);
    }

    /**
     * Creates a book icon for wiki/documentation.
     * 
     * @return a book icon node
     */
    public static Node book() {
        return createIcon(Feather.BOOK);
    }

    /**
     * Creates a vertical more/menu icon.
     * 
     * @return a more-vertical icon node
     */
    public static Node moreVertical() {
        return createIcon(Feather.MORE_VERTICAL);
    }

    /**
     * Creates a refresh/reload icon with clockwise rotation.
     * 
     * @return a refresh icon node
     */
    public static Node refreshCw() {
        return createIcon(Feather.REFRESH_CW);
    }

    /**
     * Creates a copy icon.
     * 
     * @return a copy icon node
     */
    public static Node copy() {
        return createIcon(Feather.COPY);
    }

    /**
     * Creates a download icon.
     * 
     * @return a download icon node
     */
    public static Node download() {
        return createIcon(Feather.DOWNLOAD);
    }

    /**
     * Creates a clock/time icon.
     * 
     * @return a clock icon node
     */
    public static Node clock() {
        return createIcon(Feather.CLOCK);
    }

    /**
     * Creates an upload icon.
     * 
     * @return an upload icon node
     */
    public static Node upload() {
        return createIcon(Feather.UPLOAD);
    }

    /**
     * Creates a check/checkmark icon.
     * 
     * @return a check icon node
     */
    public static Node check() {
        return createIcon(Feather.CHECK);
    }
}
