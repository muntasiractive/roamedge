package com.roam.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.roam.model.*;
import com.roam.repository.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service responsible for exporting and importing all application data.
 * <p>
 * This service provides comprehensive data management capabilities, allowing
 * users
 * to backup their entire application state to JSON files and restore from
 * backups.
 * It supports both full data replacement and merge mode for importing.
 * </p>
 * <p>
 * Supported entity types for export/import:
 * <ul>
 * <li>Regions</li>
 * <li>Operations</li>
 * <li>Tasks</li>
 * <li>Calendar Sources and Events</li>
 * <li>Wikis and Wiki Templates</li>
 * <li>Journal Entries and Templates</li>
 * </ul>
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 * @see ExportResult
 * @see ImportResult
 */
public class DataService {

    // ==================== Fields ====================

    private final OperationRepository operationRepository;
    private final TaskRepository taskRepository;
    private final RegionRepository regionRepository;
    private final CalendarEventRepository eventRepository;
    private final CalendarSourceRepository sourceRepository;
    private final WikiRepository WikiRepository;
    private final WikiTemplateRepository WikiTemplateRepository;
    private final JournalEntryRepository journalRepository;
    private final JournalTemplateRepository journalTemplateRepository;

    private final ObjectMapper objectMapper;

    // ==================== Constructor ====================

    /**
     * Constructs a new DataService instance.
     * <p>
     * Initializes all repository instances and configures the Jackson ObjectMapper
     * for JSON serialization with proper date/time handling.
     * </p>
     */
    public DataService() {
        this.operationRepository = new OperationRepository();
        this.taskRepository = new TaskRepository();
        this.regionRepository = new RegionRepository();
        this.eventRepository = new CalendarEventRepository();
        this.sourceRepository = new CalendarSourceRepository();
        this.WikiRepository = new WikiRepository();
        this.WikiTemplateRepository = new WikiTemplateRepository();
        this.journalRepository = new JournalEntryRepository();
        this.journalTemplateRepository = new JournalTemplateRepository();

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // ==================== Export Operations ====================

    /**
     * Exports all application data to a JSON file.
     * <p>
     * Creates a comprehensive backup including metadata, regions, operations,
     * tasks, calendar data, wikis, and journal entries.
     * </p>
     *
     * @param exportFile the destination file for the exported data
     * @return an {@link ExportResult} containing the operation status and details
     */
    public ExportResult exportData(File exportFile) {
        try {
            Map<String, Object> exportData = new HashMap<>();

            // Add metadata
            Map<String, String> metadata = new HashMap<>();
            metadata.put("exportDate", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            metadata.put("applicationVersion", "1.0.0");
            metadata.put("dataFormat", "json");
            exportData.put("metadata", metadata);

            // Export all entities
            exportData.put("regions", regionRepository.findAll());
            exportData.put("operations", operationRepository.findAll());
            exportData.put("tasks", taskRepository.findAll());
            exportData.put("calendarSources", sourceRepository.findAll());
            exportData.put("calendarEvents", eventRepository.findAll());
            exportData.put("wikis", WikiRepository.findAll());
            exportData.put("wikiTemplates", WikiTemplateRepository.findCustom());
            exportData.put("journalEntries", journalRepository.findAll());
            exportData.put("journalTemplates", journalTemplateRepository.findAll());

            // Write to file
            objectMapper.writeValue(exportFile, exportData);

            // Calculate counts
            int totalRecords = calculateTotalRecords(exportData);

            return new ExportResult(true, "Data exported successfully", totalRecords, exportFile.getAbsolutePath());

        } catch (IOException e) {
            return new ExportResult(false, "Export failed: " + e.getMessage(), 0, null);
        }
    }

    // ==================== Import Operations ====================

    /**
     * Imports data from a JSON file into the application.
     * <p>
     * Supports two modes of operation:
     * <ul>
     * <li><strong>Merge mode:</strong> Skips existing entities based on name
     * matching</li>
     * <li><strong>Replace mode:</strong> Creates new entities regardless of
     * existing data</li>
     * </ul>
     * </p>
     * <p>
     * Import order respects entity dependencies (regions → operations → tasks,
     * etc.)
     * </p>
     *
     * @param importFile the source file containing the data to import
     * @param mergeMode  if {@code true}, existing entities are skipped; if
     *                   {@code false}, all entities are created as new
     * @return an {@link ImportResult} containing the operation status and counts
     */
    public ImportResult importData(File importFile, boolean mergeMode) {
        try {
            // Read JSON file
            @SuppressWarnings("unchecked")
            Map<String, Object> importData = objectMapper.readValue(importFile, Map.class);

            int importedCount = 0;
            int skippedCount = 0;
            StringBuilder errors = new StringBuilder();

            // Validate metadata
            if (!importData.containsKey("metadata")) {
                return new ImportResult(false, "Invalid import file: missing metadata", 0, 0);
            }

            // Import regions first (no dependencies)
            if (importData.containsKey("regions")) {
                try {
                    List<Region> regions = objectMapper.convertValue(importData.get("regions"),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, Region.class));
                    for (Region region : regions) {
                        if (mergeMode && regionExists(region)) {
                            skippedCount++;
                        } else {
                            region.setId(null); // Clear ID to create new
                            regionRepository.save(region);
                            importedCount++;
                        }
                    }
                } catch (Exception e) {
                    errors.append("Regions import error: ").append(e.getMessage()).append("\n");
                }
            }

            // Import operations (depends on regions)
            if (importData.containsKey("operations")) {
                try {
                    List<Operation> operations = objectMapper.convertValue(importData.get("operations"),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, Operation.class));
                    for (Operation operation : operations) {
                        if (mergeMode && operationExists(operation)) {
                            skippedCount++;
                        } else {
                            operation.setId(null);
                            operationRepository.save(operation);
                            importedCount++;
                        }
                    }
                } catch (Exception e) {
                    errors.append("Operations import error: ").append(e.getMessage()).append("\n");
                }
            }

            // Import tasks (depends on operations)
            if (importData.containsKey("tasks")) {
                try {
                    List<Task> tasks = objectMapper.convertValue(importData.get("tasks"),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, Task.class));
                    for (Task task : tasks) {
                        task.setId(null);
                        taskRepository.save(task);
                        importedCount++;
                    }
                } catch (Exception e) {
                    errors.append("Tasks import error: ").append(e.getMessage()).append("\n");
                }
            }

            // Import calendar sources
            if (importData.containsKey("calendarSources")) {
                try {
                    List<CalendarSource> sources = objectMapper.convertValue(importData.get("calendarSources"),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, CalendarSource.class));
                    for (CalendarSource source : sources) {
                        if (mergeMode && sourceExists(source)) {
                            skippedCount++;
                        } else {
                            source.setId(null);
                            sourceRepository.save(source);
                            importedCount++;
                        }
                    }
                } catch (Exception e) {
                    errors.append("Calendar sources import error: ").append(e.getMessage()).append("\n");
                }
            }

            // Import calendar events
            if (importData.containsKey("calendarEvents")) {
                try {
                    List<CalendarEvent> events = objectMapper.convertValue(importData.get("calendarEvents"),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, CalendarEvent.class));
                    for (CalendarEvent event : events) {
                        event.setId(null);
                        eventRepository.save(event);
                        importedCount++;
                    }
                } catch (Exception e) {
                    errors.append("Calendar events import error: ").append(e.getMessage()).append("\n");
                }
            }

            // Import wikis
            if (importData.containsKey("wikis")) {
                try {
                    List<Wiki> wikis = objectMapper.convertValue(importData.get("wikis"),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, Wiki.class));
                    for (Wiki wiki : wikis) {
                        wiki.setId(null);
                        WikiRepository.save(wiki);
                        importedCount++;
                    }
                } catch (Exception e) {
                    errors.append("Wikis import error: ").append(e.getMessage()).append("\n");
                }
            }

            // Import Wiki templates
            if (importData.containsKey("wikiTemplates")) {
                try {
                    List<WikiTemplate> templates = objectMapper.convertValue(importData.get("wikiTemplates"),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, WikiTemplate.class));
                    for (WikiTemplate template : templates) {
                        if (mergeMode && wikiTemplateExists(template)) {
                            skippedCount++;
                        } else {
                            template.setId(null);
                            WikiTemplateRepository.save(template);
                            importedCount++;
                        }
                    }
                } catch (Exception e) {
                    errors.append("Wiki templates import error: ").append(e.getMessage()).append("\n");
                }
            }

            // Import journal entries
            if (importData.containsKey("journalEntries")) {
                try {
                    List<JournalEntry> entries = objectMapper.convertValue(importData.get("journalEntries"),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, JournalEntry.class));
                    for (JournalEntry entry : entries) {
                        entry.setId(null);
                        journalRepository.save(entry);
                        importedCount++;
                    }
                } catch (Exception e) {
                    errors.append("Journal entries import error: ").append(e.getMessage()).append("\n");
                }
            }

            // Import journal templates
            if (importData.containsKey("journalTemplates")) {
                try {
                    List<JournalTemplate> templates = objectMapper.convertValue(importData.get("journalTemplates"),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, JournalTemplate.class));
                    for (JournalTemplate template : templates) {
                        template.setId(null);
                        journalTemplateRepository.save(template);
                        importedCount++;
                    }
                } catch (Exception e) {
                    errors.append("Journal templates import error: ").append(e.getMessage()).append("\n");
                }
            }

            String message = errors.length() > 0
                    ? "Import completed with errors:\n" + errors.toString()
                    : "Data imported successfully";

            return new ImportResult(true, message, importedCount, skippedCount);

        } catch (IOException e) {
            return new ImportResult(false, "Import failed: " + e.getMessage(), 0, 0);
        }
    }

    // ==================== Helper Methods ====================

    /**
     * Checks if a region with the same name already exists.
     *
     * @param region the region to check
     * @return {@code true} if a region with the same name exists, {@code false}
     *         otherwise
     */
    private boolean regionExists(Region region) {
        return regionRepository.findAll().stream()
                .anyMatch(r -> r.getName().equals(region.getName()));
    }

    /**
     * Checks if an operation with the same name already exists.
     *
     * @param operation the operation to check
     * @return {@code true} if an operation with the same name exists, {@code false}
     *         otherwise
     */
    private boolean operationExists(Operation operation) {
        return operationRepository.findAll().stream()
                .anyMatch(o -> o.getName().equals(operation.getName()));
    }

    /**
     * Checks if a calendar source with the same name already exists.
     *
     * @param source the calendar source to check
     * @return {@code true} if a source with the same name exists, {@code false}
     *         otherwise
     */
    private boolean sourceExists(CalendarSource source) {
        return sourceRepository.findAll().stream()
                .anyMatch(s -> s.getName().equals(source.getName()));
    }

    /**
     * Checks if a wiki template with the same name already exists.
     *
     * @param template the wiki template to check
     * @return {@code true} if a template with the same name exists, {@code false}
     *         otherwise
     */
    private boolean wikiTemplateExists(WikiTemplate template) {
        return WikiTemplateRepository.findAll().stream()
                .anyMatch(t -> t.getName().equals(template.getName()));
    }

    /**
     * Calculates the total number of records in the export data map.
     *
     * @param data the export data map containing all entity lists
     * @return the total count of all records across all entity types
     */
    private int calculateTotalRecords(Map<String, Object> data) {
        int total = 0;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (entry.getValue() instanceof List) {
                total += ((List<?>) entry.getValue()).size();
            }
        }
        return total;
    }

    // ==================== Result Classes ====================

    /**
     * Represents the result of a data export operation.
     * <p>
     * Contains information about whether the export succeeded, the number of
     * records exported, and the destination file path.
     * </p>
     *
     * @author Roam Development Team
     * @version 1.0
     * @since 1.0
     */
    public static class ExportResult {
        private final boolean success;
        private final String message;
        private final int recordCount;
        private final String filePath;

        /**
         * Constructs a new ExportResult.
         *
         * @param success     whether the export operation succeeded
         * @param message     descriptive message about the export result
         * @param recordCount the number of records exported
         * @param filePath    the path to the exported file, or null if export failed
         */
        public ExportResult(boolean success, String message, int recordCount, String filePath) {
            this.success = success;
            this.message = message;
            this.recordCount = recordCount;
            this.filePath = filePath;
        }

        /**
         * Returns whether the export operation was successful.
         *
         * @return {@code true} if the export succeeded, {@code false} otherwise
         */
        public boolean isSuccess() {
            return success;
        }

        /**
         * Returns the descriptive message about the export result.
         *
         * @return the result message
         */
        public String getMessage() {
            return message;
        }

        /**
         * Returns the number of records that were exported.
         *
         * @return the count of exported records
         */
        public int getRecordCount() {
            return recordCount;
        }

        /**
         * Returns the path to the exported file.
         *
         * @return the file path, or null if export failed
         */
        public String getFilePath() {
            return filePath;
        }
    }

    /**
     * Represents the result of a data import operation.
     * <p>
     * Contains information about whether the import succeeded, the number of
     * records imported, and how many were skipped (in merge mode).
     * </p>
     *
     * @author Roam Development Team
     * @version 1.0
     * @since 1.0
     */
    public static class ImportResult {
        private final boolean success;
        private final String message;
        private final int importedCount;
        private final int skippedCount;

        /**
         * Constructs a new ImportResult.
         *
         * @param success       whether the import operation succeeded
         * @param message       descriptive message about the import result
         * @param importedCount the number of records successfully imported
         * @param skippedCount  the number of records skipped (in merge mode)
         */
        public ImportResult(boolean success, String message, int importedCount, int skippedCount) {
            this.success = success;
            this.message = message;
            this.importedCount = importedCount;
            this.skippedCount = skippedCount;
        }

        /**
         * Returns whether the import operation was successful.
         *
         * @return {@code true} if the import succeeded, {@code false} otherwise
         */
        public boolean isSuccess() {
            return success;
        }

        /**
         * Returns the descriptive message about the import result.
         *
         * @return the result message
         */
        public String getMessage() {
            return message;
        }

        /**
         * Returns the number of records that were successfully imported.
         *
         * @return the count of imported records
         */
        public int getImportedCount() {
            return importedCount;
        }

        /**
         * Returns the number of records that were skipped during import.
         *
         * @return the count of skipped records
         */
        public int getSkippedCount() {
            return skippedCount;
        }
    }
}
