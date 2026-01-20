package com.roam.view;

import com.roam.controller.WikiController;
import com.roam.view.components.WikiFullEditor;
import com.roam.view.components.WikiSidebar;
import com.roam.view.components.WikiToolbar;
import javafx.geometry.Insets;
import javafx.scene.layout.BorderPane;

/**
 * Wiki view for creating and managing knowledge base articles.
 * <p>
 * This view provides a comprehensive wiki management interface with:
 * <ul>
 * <li>Sidebar navigation for browsing wiki pages</li>
 * <li>Full-featured rich text editor for content creation</li>
 * <li>Toolbar with formatting and organization options</li>
 * <li>Support for attachments and file linking</li>
 * </ul>
 * The view integrates with {@link com.roam.controller.WikiController} for
 * wiki data persistence and management operations.
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public class WikiView extends BorderPane {

    private final WikiController controller;
    private final WikiToolbar toolbar;
    private final WikiSidebar sidebar;
    private final WikiFullEditor wikiEditor;

    public WikiView(WikiController controller) {
        this.controller = controller;

        this.toolbar = new WikiToolbar(controller);
        this.sidebar = new WikiSidebar(controller);
        this.wikiEditor = new WikiFullEditor(controller);

        initializeLayout();
    }

    private void initializeLayout() {
        // Set components in BorderPane regions
        setTop(toolbar);
        setLeft(sidebar);
        setCenter(wikiEditor);

        // Add margin to sidebar so rounded corners are visible
        BorderPane.setMargin(sidebar, new Insets(16, 0, 16, 16));

        // Set background
        getStyleClass().add("wiki-view");

        // connect wiki editor's delete callback to refresh sidebar
        wikiEditor.setOnWikiDeleted(() -> sidebar.refreshAll());

        // Load initial data
        controller.loadAllWikis();
        sidebar.refreshAll();
    }

    public void refresh() {
        sidebar.refreshAll();
        controller.loadAllWikis();
    }
}
