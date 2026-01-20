package com.roam.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Interface for search functionality across all modules.
 * <p>
 * Provides abstraction for search indexing and querying, enabling full-text
 * search
 * capabilities across wikis, tasks, and operations within the Roam application.
 * </p>
 * 
 * <h2>Key Operations:</h2>
 * <ul>
 * <li>Index wiki documents with content and metadata</li>
 * <li>Index task documents with status and priority information</li>
 * <li>Index operation documents with regional and status data</li>
 * <li>Delete documents from the search index</li>
 * <li>Perform full-text search queries across all indexed content</li>
 * </ul>
 * 
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public interface SearchServiceInterface {

    /**
     * Indexes a wiki document for search.
     * <p>
     * Adds or updates a wiki entry in the search index with its content and
     * metadata.
     * </p>
     *
     * @param id          the unique identifier of the wiki
     * @param title       the title of the wiki
     * @param content     the full content of the wiki
     * @param region      the region/category associated with the wiki
     * @param operationId the ID of the associated operation, or {@code null} if
     *                    none
     * @param updatedAt   the timestamp of the last update
     * @throws IOException if an error occurs during indexing
     */
    void indexWiki(Long id, String title, String content, String region,
            Long operationId, LocalDateTime updatedAt) throws IOException;

    /**
     * Indexes a task document for search.
     * <p>
     * Adds or updates a task entry in the search index with its details and status.
     * </p>
     *
     * @param id          the unique identifier of the task
     * @param title       the title of the task
     * @param description the description of the task
     * @param priority    the priority level of the task
     * @param status      the current status of the task
     * @param operationId the ID of the associated operation, or {@code null} if
     *                    none
     * @param dueDate     the due date of the task, or {@code null} if none
     * @throws IOException if an error occurs during indexing
     */
    void indexTask(Long id, String title, String description, String priority,
            String status, Long operationId, LocalDateTime dueDate) throws IOException;

    /**
     * Indexes an operation document for search.
     * <p>
     * Adds or updates an operation entry in the search index with its metadata.
     * </p>
     *
     * @param id        the unique identifier of the operation
     * @param name      the name of the operation
     * @param purpose   the purpose or description of the operation
     * @param status    the current status of the operation
     * @param priority  the priority level of the operation
     * @param region    the region associated with the operation
     * @param updatedAt the timestamp of the last update
     * @throws IOException if an error occurs during indexing
     */
    void indexOperation(Long id, String name, String purpose, String status,
            String priority, String region, LocalDateTime updatedAt) throws IOException;

    /**
     * Deletes a document from the search index.
     * <p>
     * Removes the document with the specified ID from all search indices.
     * </p>
     *
     * @param id the unique identifier of the document to delete
     * @throws IOException if an error occurs during deletion
     */
    void deleteDocument(Long id) throws IOException;

    /**
     * Searches across all indexed documents.
     * <p>
     * Performs a full-text search query and returns matching results ranked by
     * relevance.
     * </p>
     *
     * @param query the search query string
     * @return a list of {@link SearchResult} objects matching the query, ordered by
     *         score
     * @throws IOException if an error occurs during the search operation
     */
    List<SearchResult> search(String query) throws IOException;

    /**
     * Represents a search result containing document metadata and relevance score.
     * <p>
     * This record encapsulates the essential information returned from a search
     * query,
     * including the document identifier, type, title, preview text, and relevance
     * score.
     * </p>
     *
     * @param id      the unique identifier of the matched document
     * @param type    the type of document (e.g., "wiki", "task", "operation")
     * @param title   the title of the matched document
     * @param preview a preview snippet of the matching content
     * @param score   the relevance score of the match (higher is more relevant)
     */
    record SearchResult(
            Long id,
            String type,
            String title,
            String preview,
            float score) {
    }

    /**
     * Shuts down the search service and releases resources.
     * <p>
     * This method should be called when the application is shutting down to ensure
     * proper cleanup of search index resources and pending operations.
     * </p>
     *
     * @throws IOException if an error occurs during shutdown
     */
    void shutdown() throws IOException;
}
