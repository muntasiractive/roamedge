package com.roam.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for processing Markdown content with wiki-style extensions.
 * 
 * <p>
 * This class provides methods to process and transform Markdown content,
 * with special handling for wiki-style internal links using the [[Title]]
 * syntax.
 * The processed links are converted to HTML anchors that can be intercepted
 * by the WebView for internal navigation.
 * </p>
 * 
 * <h2>Wiki Link Syntax</h2>
 * <p>
 * Wiki links use double brackets: {@code [[Wiki Title]]}
 * </p>
 * <p>
 * The link is converted to an HTML anchor with a special protocol that
 * the application's WebView can intercept for navigation.
 * </p>
 * 
 * <h2>Usage Example</h2>
 * 
 * <pre>{@code
 * String markdown = "See [[My Wiki Page]] for details.";
 * String html = MarkdownUtils.processWikiLinks(markdown);
 * // Result: "See <a href='Wiki://My%20Wiki%20Page' class=
 * 'wiki-link'>My Wiki Page</a> for details."
 * }</pre>
 * 
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public class MarkdownUtils {

    // ========================================
    // CONSTANTS
    // ========================================

    // Regex for [[Wiki Title]]
    private static final Pattern WIKI_LINK_PATTERN = Pattern.compile("\\[\\[(.*?)\\]\\]");

    public static String processWikiLinks(String markdown) {
        if (markdown == null)
            return "";

        Matcher matcher = WIKI_LINK_PATTERN.matcher(markdown);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String wikiTitle = matcher.group(1);
            // Replace [[Title]] with <a href="Wiki:Title">Title</a>
            // The WebView will intercept "Wiki:" protocol
            String replacement = String.format("<a href='Wiki://%s' class='wiki-link'>%s</a>",
                    wikiTitle.replace(" ", "%20"), wikiTitle);
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);

        return sb.toString();
    }
}
