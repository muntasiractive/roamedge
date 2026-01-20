package com.roam.service;

import com.roam.model.JournalEntry;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for JournalEntry business logic.
 * <p>
 * Provides transaction management and business operations for journal entries,
 * enabling users to create and manage daily notes, reflections, and logs.
 * </p>
 * 
 * <h2>Key Operations:</h2>
 * <ul>
 * <li>Create, update, and delete journal entries</li>
 * <li>Find entries by ID, date, or date range</li>
 * <li>Retrieve recent entries for quick access</li>
 * <li>Index entries for search functionality</li>
 * </ul>
 * 
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public interface JournalService {

    /**
     * Creates a new journal entry.
     *
     * @param entry The entry to create
     * @return The created entry with ID
     * @throws IllegalArgumentException if entry is null
     * @throws RuntimeException         if creation fails
     */
    JournalEntry createEntry(JournalEntry entry);

    /**
     * Updates an existing journal entry.
     *
     * @param entry The entry to update
     * @return The updated entry
     * @throws IllegalArgumentException if entry or ID is null
     * @throws RuntimeException         if update fails
     */
    JournalEntry updateEntry(JournalEntry entry);

    /**
     * Deletes a journal entry by ID.
     *
     * @param id The entry ID
     * @throws IllegalArgumentException if id is null
     * @throws RuntimeException         if deletion fails
     */
    void deleteEntry(Long id);

    /**
     * Finds a journal entry by ID.
     *
     * @param id The entry ID
     * @return Optional containing the entry if found
     */
    Optional<JournalEntry> findById(Long id);

    /**
     * Retrieves all journal entries.
     *
     * @return List of all entries
     * @throws RuntimeException if retrieval fails
     */
    List<JournalEntry> findAll();

    /**
     * Finds journal entries for a specific date.
     *
     * @param date The entry date
     * @return List of entries for that date
     * @throws RuntimeException if retrieval fails
     */
    List<JournalEntry> findByDate(LocalDate date);

    /**
     * Finds journal entries within a date range.
     *
     * @param startDate Start date
     * @param endDate   End date
     * @return List of entries in range
     * @throws RuntimeException if retrieval fails
     */
    List<JournalEntry> findByDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * Finds recent journal entries.
     *
     * @param limit Maximum number of entries to return
     * @return List of recent entries
     * @throws RuntimeException if retrieval fails
     */
    List<JournalEntry> findRecent(int limit);

    /**
     * Counts all journal entries.
     *
     * @return Total count of entries
     */
    long count();

    /**
     * Indexes a journal entry for search.
     *
     * @param entry The entry to index
     */
    void indexEntry(JournalEntry entry);
}
