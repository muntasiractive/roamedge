package com.roam.controller;

import com.roam.model.*;
import com.roam.repository.*;
import com.roam.service.WikiService;
import com.roam.service.WikiServiceImpl;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

/**
 * Controller responsible for managing wiki pages and documentation.
 * <p>
 * This controller provides comprehensive wiki management functionality
 * including:
 * <ul>
 * <li>Wiki CRUD operations (create, read, update, delete)</li>
 * <li>Auto-save functionality with configurable intervals</li>
 * <li>Wiki template management and application</li>
 * <li>Favorites and recent wikis tracking</li>
 * <li>Search and filtering capabilities</li>
 * <li>Operation and region association</li>
 * </ul>
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public class WikiController {

    // ==================== Fields ====================

    private static final Logger logger = LoggerFactory.getLogger(WikiController.class);

    private final WikiService wikiService;
    private final WikiTemplateRepository templateRepository;
    private final OperationRepository operationRepository;
    private final RegionRepository regionRepository;
    private final TaskRepository taskRepository;
    private final CalendarEventRepository eventRepository;

    private Wiki currentWiki;
    private final ObservableList<Wiki> allWikis;
    private final FilteredList<Wiki> filteredWikis;
    private final StringProperty searchQuery;
    private final ObjectProperty<Operation> selectedOperation;

    private Timeline autoSaveTimer;
    private final java.util.List<Consumer<Wiki>> wikiChangedListeners = new java.util.ArrayList<>();

    // ==================== Constructor ====================

    public WikiController() {
        this.wikiService = new WikiServiceImpl();
        this.templateRepository = new WikiTemplateRepository();
        this.operationRepository = new OperationRepository();
        this.regionRepository = new RegionRepository();
        this.taskRepository = new TaskRepository();
        this.eventRepository = new CalendarEventRepository();

        this.allWikis = FXCollections.observableArrayList();
        this.filteredWikis = new FilteredList<>(allWikis, wiki -> true);
        this.searchQuery = new SimpleStringProperty("");
        this.selectedOperation = new SimpleObjectProperty<>();

        setupAutoSave();
    }

    // ==================== Auto-Save Methods ====================

    private void setupAutoSave() {
        autoSaveTimer = new Timeline(new KeyFrame(Duration.seconds(2), event -> saveCurrentWiki()));
        autoSaveTimer.setCycleCount(1);
    }

    public void scheduleAutoSave() {
        if (autoSaveTimer.getStatus() == Timeline.Status.RUNNING) {
            autoSaveTimer.stop();
        }
        autoSaveTimer.playFromStart();
    }

    // ==================== Wiki CRUD Operations ====================

    public List<Wiki> loadAllWikis() {
        List<Wiki> wikis = wikiService.findAll();
        allWikis.setAll(wikis);
        return wikis;
    }

    public List<Wiki> loadRecentWikis(int limit) {
        return wikiService.findRecent(limit);
    }

    public List<Wiki> loadFavoriteWikis() {
        return wikiService.findFavorites();
    }

    public List<Wiki> loadWikisForOperation(Operation op) {
        if (op == null)
            return List.of();
        return wikiService.findByOperationId(op.getId());
    }

    public List<Wiki> searchWikis(String query) {
        if (query == null || query.trim().isEmpty()) {
            return loadAllWikis();
        }
        searchQuery.set(query);
        return wikiService.findAll(); // Service handles search via Lucene
    }

    public Wiki createNewWiki() {
        Wiki wiki = new Wiki();
        wiki.setTitle("Untitled");
        wiki.setContent("");
        wiki.setCreatedAt(LocalDateTime.now());
        wiki.setUpdatedAt(LocalDateTime.now());
        wiki.setIsFavorite(false);
        wiki.setWordCount(0);

        Wiki saved = wikiService.createWiki(wiki);
        allWikis.add(0, saved);
        setCurrentWiki(saved);

        return saved;
    }

    public Wiki createWikiFromTemplate(WikiTemplate template) {
        Wiki wiki = new Wiki();
        wiki.setTitle(template.getName());
        wiki.setContent(template.processTemplate(template.getName(), null));
        wiki.setTemplateId(template.getId());
        wiki.setCreatedAt(LocalDateTime.now());
        wiki.setUpdatedAt(LocalDateTime.now());
        wiki.setIsFavorite(false);
        wiki.calculateWordCount();

        Wiki saved = wikiService.createWiki(wiki);
        allWikis.add(0, saved);
        setCurrentWiki(saved);

        return saved;
    }

    public void saveCurrentWiki() {
        if (currentWiki == null)
            return;

        currentWiki.setUpdatedAt(LocalDateTime.now());
        currentWiki.calculateWordCount();
        wikiService.updateWiki(currentWiki);

        // Refresh in list
        int index = allWikis.indexOf(currentWiki);
        if (index >= 0) {
            allWikis.set(index, currentWiki);
        }
    }

    public void saveWiki(Wiki wiki) {
        if (wiki == null)
            return;

        wiki.setUpdatedAt(LocalDateTime.now());
        wiki.calculateWordCount();
        wikiService.updateWiki(wiki);

        // Refresh in list
        for (int i = 0; i < allWikis.size(); i++) {
            if (allWikis.get(i).getId().equals(wiki.getId())) {
                allWikis.set(i, wiki);
                break;
            }
        }
    }

    public void deleteWiki(Wiki wiki) {
        if (wiki == null) {
            return;
        }

        wikiService.deleteWiki(wiki.getId());
        allWikis.remove(wiki);

        if (currentWiki != null && currentWiki.getId().equals(wiki.getId())) {
            setCurrentWiki(null);
        }
    }

    public void toggleFavorite(Wiki wiki) {
        if (wiki == null)
            return;

        Wiki updated = wikiService.toggleFavorite(wiki.getId());

        // Refresh in list
        int index = allWikis.indexOf(wiki);
        if (index >= 0) {
            allWikis.set(index, updated);
        }

        // If this is the current wiki, update reference and trigger change notification
        if (currentWiki != null && currentWiki.getId().equals(wiki.getId())) {
            currentWiki = updated;
            notifyWikiChanged(updated);
        }
    }

    public Wiki duplicateWiki(Wiki original) {
        if (original == null)
            return null;

        Wiki duplicate = new Wiki();
        duplicate.setTitle(original.getTitle() + " (Copy)");
        duplicate.setContent(original.getContent());
        duplicate.setRegion(original.getRegion());
        duplicate.setOperationId(original.getOperationId());
        duplicate.setTaskId(original.getTaskId());
        duplicate.setCalendarEventId(original.getCalendarEventId());
        duplicate.setBannerUrl(original.getBannerUrl());
        duplicate.setTemplateId(original.getTemplateId());
        duplicate.setCreatedAt(LocalDateTime.now());
        duplicate.setUpdatedAt(LocalDateTime.now());
        duplicate.setIsFavorite(false);
        duplicate.calculateWordCount();

        Wiki saved = wikiService.createWiki(duplicate);
        allWikis.add(0, saved);
        setCurrentWiki(saved);

        return saved;
    }

    public List<Operation> loadAllOperations() {
        return operationRepository.findAll();
    }

    // ==================== Wiki Navigation ====================

    public void openWikiByTitle(String title) {
        // Find Wiki by title
        for (Wiki wiki : allWikis) {
            if (wiki.getTitle().equalsIgnoreCase(title)) {
                setCurrentWiki(wiki);
                return;
            }
        }

        // Wiki not found - could implement a feature to ask user to create it
    }

    // ==================== Template Operations ====================

    public List<WikiTemplate> loadAllTemplates() {
        return templateRepository.findAll();
    }

    public List<WikiTemplate> loadDefaultTemplates() {
        return templateRepository.findDefaults();
    }

    public List<WikiTemplate> loadCustomTemplates() {
        return templateRepository.findCustom();
    }

    public WikiTemplate createTemplate(String name, String description, String content, String icon) {
        WikiTemplate template = new WikiTemplate();
        template.setName(name);
        template.setDescription(description);
        template.setContent(content);
        template.setIcon(icon);
        template.setIsDefault(false);
        template.setCreatedAt(LocalDateTime.now());
        return templateRepository.save(template);
    }

    public WikiTemplate updateTemplate(WikiTemplate template) {
        template.setUpdatedAt(LocalDateTime.now());
        return templateRepository.save(template);
    }

    public void deleteTemplate(WikiTemplate template) {
        templateRepository.delete(template.getId());
    }

    // ==================== Statistics ====================

    public long getTotalWordCount() {
        // Calculate from all loaded wikis
        return allWikis.stream()
                .mapToLong(Wiki::getWordCount)
                .sum();
    }

    // ==================== Getters and Setters ====================

    public Wiki getCurrentWiki() {
        return currentWiki;
    }

    public void setCurrentWiki(Wiki wiki) {
        this.currentWiki = wiki;
        notifyWikiChanged(wiki);
    }

    public ObservableList<Wiki> getAllWikis() {
        return allWikis;
    }

    public FilteredList<Wiki> getFilteredWikis() {
        return filteredWikis;
    }

    public StringProperty searchQueryProperty() {
        return searchQuery;
    }

    public ObjectProperty<Operation> selectedOperationProperty() {
        return selectedOperation;
    }

    public void addOnWikiChangedListener(Consumer<Wiki> handler) {
        this.wikiChangedListeners.add(handler);
    }

    // ==================== Helper Methods ====================

    private void notifyWikiChanged(Wiki wiki) {
        for (Consumer<Wiki> listener : wikiChangedListeners) {
            listener.accept(wiki);
        }
    }

    // ==================== Data Loading Methods ====================

    public List<Region> loadAllRegions() {
        return regionRepository.findAll();
    }

    public List<Task> loadAllTasks() {
        return taskRepository.findAll();
    }

    public List<CalendarEvent> loadAllEvents() {
        return eventRepository.findAll();
    }
}
