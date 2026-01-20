package com.roam.model;

/**
 * Represents the types of sources that can provide calendar events within the
 * Roam application.
 * <p>
 * Calendar source types define the origin of calendar entries, allowing the
 * system to
 * categorize and manage events based on their source. Currently, all calendar
 * sources
 * are derived from user-defined regions, enabling geographic or contextual
 * organization
 * of calendar data.
 * </p>
 *
 * <h2>Enum Values:</h2>
 * <ul>
 * <li>{@link #REGION} - Calendar events sourced from user-defined regions</li>
 * </ul>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public enum CalendarSourceType {

    /**
     * Calendar source based on user-defined regions.
     * <p>
     * Events of this type are associated with specific geographic areas or
     * organizational regions defined by the user. This enables location-based
     * or context-aware calendar management, where events can be filtered
     * and displayed according to regional boundaries or categories.
     * </p>
     */
    REGION
}
