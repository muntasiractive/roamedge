package com.roam.controller;

import com.roam.model.JournalEntry;
import com.roam.service.JournalService;
import com.roam.service.JournalServiceImpl;
import com.roam.model.JournalTemplate;
import com.roam.repository.JournalTemplateRepository;
import com.roam.util.DialogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Controller responsible for managing journal entries and templates.
 * <p>
 * This controller handles all journal-related functionality including:
 * <ul>
 * <li>Journal entry CRUD operations (create, read, update, delete)</li>
 * <li>Date-based journal entry retrieval</li>
 * <li>Journal template management and application</li>
 * </ul>
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public class JournalController {

    // ==================== Fields ====================

    private static final Logger logger = LoggerFactory.getLogger(JournalController.class);

    private final JournalService journalService;
    private final JournalTemplateRepository templateRepository;
    private Runnable onDataChanged;

    // ==================== Constructor ====================

    public JournalController() {
        this.journalService = new JournalServiceImpl();
        this.templateRepository = new JournalTemplateRepository();
    }

    // ==================== Entry Operations ====================

    public void setOnDataChanged(Runnable handler) {
        this.onDataChanged = handler;
    }

    public List<JournalEntry> loadAllEntries() {
        return journalService.findAll();
    }

    public Optional<JournalEntry> getEntryForDate(LocalDate date) {
        List<JournalEntry> entries = journalService.findByDate(date);
        return entries.isEmpty() ? Optional.empty() : Optional.of(entries.get(0));
    }

    public JournalEntry createEntry(LocalDate date) {
        // Check if exists
        List<JournalEntry> existing = journalService.findByDate(date);
        if (!existing.isEmpty()) {
            return existing.get(0);
        }

        JournalEntry entry = new JournalEntry("Journal - " + date.toString(), date);
        try {
            entry = journalService.createEntry(entry);
            if (onDataChanged != null)
                onDataChanged.run();
            return entry;
        } catch (Exception e) {
            DialogUtils.showError("Error", "Failed to create journal entry", e.getMessage());
            return null;
        }
    }

    public void saveEntry(JournalEntry entry) {
        try {
            journalService.updateEntry(entry);
            if (onDataChanged != null)
                onDataChanged.run();
        } catch (Exception e) {
            DialogUtils.showError("Error", "Failed to save journal entry", e.getMessage());
        }
    }

    public void deleteEntry(JournalEntry entry) {
        if (DialogUtils.showConfirmation("Delete Entry", "Delete this journal entry?", "This cannot be undone.")) {
            try {
                journalService.deleteEntry(entry.getId());
                if (onDataChanged != null)
                    onDataChanged.run();
            } catch (Exception e) {
                DialogUtils.showError("Error", "Failed to delete journal entry", e.getMessage());
            }
        }
    }

    // ==================== Template Operations ====================

    public List<JournalTemplate> loadTemplates() {
        return templateRepository.findAll();
    }

    public void applyTemplate(JournalEntry entry, JournalTemplate template) {
        if (entry == null || template == null)
            return;
        String currentContent = entry.getContent() == null ? "" : entry.getContent();
        entry.setContent(currentContent + "\n\n" + template.getContent());
        saveEntry(entry);
    }

    public JournalTemplate createTemplate(String name, String content) {
        JournalTemplate template = new JournalTemplate(name, content);
        return templateRepository.save(template);
    }

    public JournalTemplate updateTemplate(JournalTemplate template) {
        return templateRepository.save(template);
    }

    public void deleteTemplate(JournalTemplate template) {
        templateRepository.delete(template);
    }
}
