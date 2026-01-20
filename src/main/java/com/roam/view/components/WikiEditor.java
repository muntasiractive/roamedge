package com.roam.view.components;

import com.roam.model.Wiki;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A rich text editor component for creating and editing wiki pages.
 * <p>
 * This component provides a dual-mode editor with source (Markdown) and preview
 * views.
 * Features include auto-save functionality, markdown parsing and rendering,
 * and a wiki list for navigation. Supports creating, editing, and deleting wiki
 * pages.
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public class WikiEditor extends BorderPane {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    private final ListView<Wiki> wikisList;
    private final TextField titleField;
    private final TextArea sourceEditor;
    private final WebView previewView;
    private final ToggleGroup viewToggle;
    private final Button saveButton;
    private final Label statusLabel;
    private final StackPane editorContainer;

    private Wiki currentWiki;
    private Consumer<Wiki> onSave;
    private Consumer<Wiki> onDelete;
    private Runnable onNewWiki;
    private BiConsumer<Wiki, String> onTitleChanged;

    private PauseTransition autoSaveTimer;
    private boolean hasUnsavedChanges = false;

    private final Parser markdownParser;
    private final HtmlRenderer htmlRenderer;

    public WikiEditor() {
        // Initialize markdown parser
        markdownParser = Parser.builder().build();
        htmlRenderer = HtmlRenderer.builder().build();

        // Create components
        wikisList = createwikisList();
        titleField = createTitleField();
        sourceEditor = createSourceEditor();
        previewView = createPreviewView();
        viewToggle = new ToggleGroup();
        saveButton = createSaveButton();
        statusLabel = createStatusLabel();
        editorContainer = new StackPane();

        // Setup layout
        VBox leftSidebar = createSidebar();
        VBox centerEditor = createEditor();

        setLeft(leftSidebar);
        setCenter(centerEditor);

        // Setup auto-save
        setupAutoSave();

        // Show empty state initially
        showEmptyState();
    }

    private ListView<Wiki> createwikisList() {
        ListView<Wiki> list = new ListView<>();
        list.setCellFactory(lv -> new WikiListCell());
        list.getSelectionModel().selectedItemProperty().addListener((obs, old, newWiki) -> {
            if (newWiki != null && newWiki != currentWiki) {
                loadWiki(newWiki);
            }
        });
        return list;
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(15);
        sidebar.setPrefWidth(280);
        sidebar.setMinWidth(280);
        sidebar.setMaxWidth(280);
        sidebar.setPadding(new Insets(15));
        sidebar.setStyle(
                "-fx-background-color: -roam-gray-bg; -fx-border-color: -roam-border; -fx-border-width: 0 1 0 0;");

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Wiki");
        title.setFont(Font.font("Poppins Bold", 18));
        title.setStyle("-fx-text-fill: -roam-text-primary;");
        HBox.setHgrow(title, Priority.ALWAYS);

        Button newWikiBtn = new Button("+ New Wiki");
        newWikiBtn.setFont(Font.font("Poppins", 12));
        newWikiBtn.setStyle(
                "-fx-background-color: -roam-blue; " +
                        "-fx-text-fill: -roam-white; " +
                        "-fx-padding: 6 12 6 12; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand;");
        newWikiBtn.setOnAction(e -> {
            if (onNewWiki != null) {
                onNewWiki.run();
            }
        });

        header.getChildren().addAll(title, newWikiBtn);

        VBox.setVgrow(wikisList, Priority.ALWAYS);

        sidebar.getChildren().addAll(header, wikisList);
        return sidebar;
    }

    private TextField createTitleField() {
        TextField field = new TextField();
        field.setPromptText("Wiki title");
        field.setFont(Font.font("Poppins Medium", 16));
        field.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-border-width: 0; " +
                        "-fx-text-fill: -roam-text-primary;");
        field.textProperty().addListener((obs, old, newVal) -> {
            hasUnsavedChanges = true;
            statusLabel.setText("Unsaved changes");
            statusLabel.setStyle("-fx-text-fill: -roam-orange;");
            if (currentWiki != null && onTitleChanged != null) {
                onTitleChanged.accept(currentWiki, newVal);
            }
            restartAutoSave();
        });
        return field;
    }

    private TextArea createSourceEditor() {
        TextArea editor = new TextArea();
        editor.setPromptText("Write your wiki in Markdown...");
        editor.setWrapText(true);
        editor.setFont(Font.font("Consolas", 14));
        editor.setStyle("-fx-background-color: -roam-bg-primary; -fx-control-inner-background: -roam-bg-primary;");
        editor.textProperty().addListener((obs, old, newVal) -> {
            hasUnsavedChanges = true;
            statusLabel.setText("Unsaved changes");
            statusLabel.setStyle("-fx-text-fill: -roam-orange;");
            restartAutoSave();
        });
        return editor;
    }

    private WebView createPreviewView() {
        WebView view = new WebView();
        view.setContextMenuEnabled(false);
        return view;
    }

    private Button createSaveButton() {
        Button btn = new Button("💾 Save");
        btn.setFont(Font.font("Poppins", 14));
        btn.setStyle(
                "-fx-background-color: -roam-blue; " +
                        "-fx-text-fill: -roam-white; " +
                        "-fx-padding: 8 16 8 16; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand;");
        btn.setDisable(true);
        btn.setOnAction(e -> savecurrentWiki());
        return btn;
    }

    private Label createStatusLabel() {
        Label label = new Label("");
        label.setFont(Font.font("Poppins", 12));
        label.setStyle("-fx-text-fill: -roam-text-hint;");
        return label;
    }

    private VBox createEditor() {
        VBox editor = new VBox();
        editor.setStyle("-fx-background-color: -roam-bg-primary;");

        // Toolbar
        HBox toolbar = createToolbar();

        // Editor container (will hold either source or preview)
        editorContainer.setPadding(new Insets(20));
        VBox.setVgrow(editorContainer, Priority.ALWAYS);

        editor.getChildren().addAll(toolbar, editorContainer);
        return editor;
    }

    private HBox createToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setPrefHeight(50);
        toolbar.setPadding(new Insets(10));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle(
                "-fx-background-color: -roam-bg-primary; -fx-border-color: -roam-border; -fx-border-width: 0 0 1 0;");

        HBox.setHgrow(titleField, Priority.ALWAYS);

        // View toggle buttons
        ToggleButton sourceBtn = new ToggleButton("Source");
        ToggleButton previewBtn = new ToggleButton("Preview");

        sourceBtn.setToggleGroup(viewToggle);
        previewBtn.setToggleGroup(viewToggle);

        sourceBtn.setSelected(true);

        String inactiveStyle = "-fx-background-color: transparent; " +
                "-fx-text-fill: -roam-text-secondary; " +
                "-fx-border-color: -roam-border; " +
                "-fx-border-width: 1; " +
                "-fx-padding: 8 16 8 16; " +
                "-fx-font-family: 'Poppins'; " +
                "-fx-font-size: 14px; " +
                "-fx-cursor: hand;";

        String activeStyle = "-fx-background-color: -roam-bg-primary; " +
                "-fx-text-fill: -roam-blue; " +
                "-fx-border-color: -roam-blue; " +
                "-fx-border-width: 1; " +
                "-fx-padding: 8 16 8 16; " +
                "-fx-font-family: 'Poppins Medium'; " +
                "-fx-font-size: 14px; " +
                "-fx-cursor: hand;";

        sourceBtn.setStyle(activeStyle);
        previewBtn.setStyle(inactiveStyle);

        sourceBtn.setOnAction(e -> {
            sourceBtn.setStyle(activeStyle);
            previewBtn.setStyle(inactiveStyle);
            showSourceEditor();
        });

        previewBtn.setOnAction(e -> {
            previewBtn.setStyle(activeStyle);
            sourceBtn.setStyle(inactiveStyle);
            showPreview();
        });

        // Round corners
        sourceBtn.setStyle(
                sourceBtn.getStyle() + "-fx-background-radius: 6 0 0 6; -fx-border-radius: 6 0 0 6;");
        previewBtn.setStyle(
                previewBtn.getStyle() + "-fx-background-radius: 0 6 6 0; -fx-border-radius: 0 6 6 0;");

        HBox toggleContainer = new HBox(0, sourceBtn, previewBtn);

        FontIcon trashIcon = new FontIcon(Feather.TRASH_2);
        trashIcon.setIconSize(16);
        Button deleteBtn = new Button();
        deleteBtn.setGraphic(trashIcon);
        deleteBtn.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: -roam-text-secondary; " +
                        "-fx-padding: 8; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand;");
        deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle(
                "-fx-background-color: -roam-red-bg; " +
                        "-fx-text-fill: -roam-red; " +
                        "-fx-padding: 8; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand;"));
        deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: -roam-text-secondary; " +
                        "-fx-padding: 8; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand;"));
        deleteBtn.setOnAction(e -> deletecurrentWiki());

        toolbar.getChildren().addAll(titleField, toggleContainer, statusLabel, saveButton, deleteBtn);
        return toolbar;
    }

    private void showSourceEditor() {
        editorContainer.getChildren().clear();
        editorContainer.getChildren().add(sourceEditor);
    }

    private void showPreview() {
        editorContainer.getChildren().clear();
        updatePreview();
        editorContainer.getChildren().add(previewView);
    }

    private void updatePreview() {
        String markdown = sourceEditor.getText();
        String html = convertMarkdownToHtml(markdown);
        previewView.getEngine().loadContent(html);
    }

    private String convertMarkdownToHtml(String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            return "<html><body style='font-family: Poppins, sans-serif; padding: 20px; background-color: #1E1E1E; color: #E0E0E0;'><p style='color: #757575;'>No content</p></body></html>";
        }

        org.commonmark.node.Node document = markdownParser.parse(markdown);
        String htmlContent = htmlRenderer.render(document);

        return String.format(
                "<html><head><style>" +
                        "body { font-family: 'Poppins', sans-serif; font-size: 14px; line-height: 1.6; padding: 20px; background-color: #1E1E1E; color: #E0E0E0; }"
                        +
                        "h1 { font-size: 32px; font-weight: bold; color: #FFFFFF; }" +
                        "h2 { font-size: 24px; font-weight: bold; color: #FFFFFF; }" +
                        "h3 { font-size: 20px; font-weight: bold; color: #FFFFFF; }" +
                        "code { background-color: #333333; padding: 2px 6px; border-radius: 4px; font-family: Consolas, monospace; color: #E0E0E0; }"
                        +
                        "pre { background-color: #333333; padding: 15px; border-radius: 6px; overflow-x: auto; }" +
                        "pre code { background-color: transparent; padding: 0; }" +
                        "a { color: #64B5F6; text-decoration: underline; }" +
                        "blockquote { border-left: 4px solid #616161; padding-left: 15px; color: #BDBDBD; }" +
                        "</style></head><body>%s</body></html>",
                htmlContent);
    }

    private void showEmptyState() {
        VBox emptyState = new VBox(20);
        emptyState.setAlignment(Pos.CENTER);

        FontIcon icon = new FontIcon(Feather.FILE_TEXT);
        icon.setIconSize(72);
        icon.setIconColor(Color.web("#6b7280"));

        Label message = new Label("Select a Wiki or create a new one");
        message.setFont(Font.font("Poppins", 18));
        message.setStyle("-fx-text-fill: -roam-text-hint;");

        emptyState.getChildren().addAll(icon, message);

        editorContainer.getChildren().clear();
        editorContainer.getChildren().add(emptyState);
    }

    private void setupAutoSave() {
        autoSaveTimer = new PauseTransition(Duration.seconds(2));
        autoSaveTimer.setOnFinished(e -> savecurrentWiki());
    }

    private void restartAutoSave() {
        autoSaveTimer.stop();
        autoSaveTimer.playFromStart();
        saveButton.setDisable(false);
    }

    private void savecurrentWiki() {
        if (currentWiki != null && hasUnsavedChanges) {
            currentWiki.setTitle(titleField.getText().trim());
            currentWiki.setContent(sourceEditor.getText());

            if (onSave != null) {
                statusLabel.setText("Saving...");
                statusLabel.setStyle("-fx-text-fill: -roam-blue;");

                onSave.accept(currentWiki);

                hasUnsavedChanges = false;
                saveButton.setDisable(true);

                statusLabel.setText("Saved");
                statusLabel.setStyle("-fx-text-fill: -roam-green;");
                statusLabel.setGraphic(new FontIcon(Feather.CHECK));

                // Refresh wikis list
                wikisList.refresh();
            }
        }
    }

    private void deletecurrentWiki() {
        if (currentWiki != null && onDelete != null) {
            onDelete.accept(currentWiki);
            currentWiki = null;
            showEmptyState();
        }
    }

    private void loadWiki(Wiki Wiki) {
        // Save current Wiki if has changes
        if (hasUnsavedChanges && currentWiki != null) {
            savecurrentWiki();
        }

        currentWiki = Wiki;
        titleField.setText(Wiki.getTitle());
        sourceEditor.setText(Wiki.getContent() != null ? Wiki.getContent() : "");

        hasUnsavedChanges = false;
        saveButton.setDisable(true);
        statusLabel.setText("");

        showSourceEditor();
        ((ToggleButton) viewToggle.getToggles().get(0)).setSelected(true);
    }

    public void loadWikis(List<Wiki> wikis) {
        wikisList.getItems().setAll(wikis);

        if (wikis.isEmpty()) {
            showEmptyState();
            currentWiki = null;
        }
    }

    public void selectWiki(Wiki wiki) {
        wikisList.getSelectionModel().select(wiki);
    }

    public void setOnSave(Consumer<Wiki> handler) {
        this.onSave = handler;
    }

    public void setOnDelete(Consumer<Wiki> handler) {
        this.onDelete = handler;
    }

    public void setonNewWiki(Runnable handler) {
        this.onNewWiki = handler;
    }

    public void setOnTitleChanged(BiConsumer<Wiki, String> handler) {
        this.onTitleChanged = handler;
    }

    // Custom cell for wikis list
    private static class WikiListCell extends ListCell<Wiki> {
        @Override
        protected void updateItem(Wiki Wiki, boolean empty) {
            super.updateItem(Wiki, empty);

            if (empty || Wiki == null) {
                setGraphic(null);
            } else {
                VBox cell = new VBox(5);
                cell.setPadding(new Insets(10));

                Label title = new Label(Wiki.getTitle());
                title.setFont(Font.font("Poppins Medium", 14));
                title.setStyle("-fx-text-fill: -roam-text-primary;");
                title.setMaxWidth(Double.MAX_VALUE);

                String preview = Wiki.getContent() != null && !Wiki.getContent().isEmpty()
                        ? Wiki.getContent().substring(0, Math.min(60, Wiki.getContent().length()))
                        : "No content";
                Label content = new Label(preview);
                content.setFont(Font.font("Poppins", 12));
                content.setStyle("-fx-text-fill: -roam-text-secondary;");
                content.setWrapText(true);
                content.setMaxWidth(Double.MAX_VALUE);

                Label date = new Label("Updated: " + DATE_FORMATTER.format(Wiki.getUpdatedAt()));
                date.setFont(Font.font("Poppins", 11));
                date.setStyle("-fx-text-fill: -roam-text-hint;");

                cell.getChildren().addAll(title, content, date);
                setGraphic(cell);
            }
        }
    }
}
