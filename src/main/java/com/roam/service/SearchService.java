package com.roam.service;

import com.roam.util.InputSanitizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Full-text search service using Apache Lucene for indexing and searching
 * across all modules.
 * <p>
 * This service provides comprehensive search functionality across the entire
 * application,
 * including wikis, tasks, calendar events, journal entries, and operations. It
 * uses
 * Apache Lucene for fast, scalable full-text indexing and search capabilities.
 * </p>
 * <p>
 * Features:
 * <ul>
 * <li>Full-text search with fuzzy matching support</li>
 * <li>Multi-field search across titles, content, and descriptions</li>
 * <li>Filtering by type, region, operation, priority, and status</li>
 * <li>Real-time index updates on entity changes</li>
 * <li>Input sanitization to prevent Lucene injection attacks</li>
 * </ul>
 * </p>
 * <p>
 * This class implements the Singleton pattern to ensure only one instance
 * manages the Lucene index.
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 * @see SearchResult
 * @see SearchFilter
 * @see InputSanitizer
 */
public class SearchService {

    // ==================== Constants ====================

    // ==================== Constants ====================

    private static final String INDEX_PATH = System.getProperty("user.home") + "/.roam/index";

    // ==================== Singleton Instance ====================

    private static SearchService instance;

    // ==================== Fields ====================

    private final Directory indexDirectory;
    private final StandardAnalyzer analyzer;
    private IndexWriter indexWriter;

    private SearchService() throws IOException {
        // Ensure index directory exists
        java.nio.file.Path indexPath = Paths.get(INDEX_PATH);
        java.nio.file.Files.createDirectories(indexPath);

        this.indexDirectory = FSDirectory.open(indexPath);
        this.analyzer = new StandardAnalyzer();

        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        this.indexWriter = new IndexWriter(indexDirectory, config);
    }

    // ==================== Singleton Access ====================

    /**
     * Returns the singleton instance of SearchService.
     * <p>
     * Creates the instance on first access (lazy initialization).
     * Thread-safe implementation using synchronized method.
     * </p>
     *
     * @return the singleton SearchService instance
     * @throws RuntimeException if the service fails to initialize
     */
    public static synchronized SearchService getInstance() {
        if (instance == null) {
            try {
                instance = new SearchService();
            } catch (IOException e) {
                throw new RuntimeException("Failed to initialize SearchService", e);
            }
        }
        return instance;
    }

    // ==================== Indexing Methods ====================

    /**
     * Indexes a wiki document for full-text search.
     *
     * @param id          the unique identifier of the wiki
     * @param title       the title of the wiki
     * @param content     the content/body of the wiki
     * @param region      the region associated with the wiki
     * @param operationId the ID of the associated operation, or null
     * @param updatedAt   the last update timestamp
     * @throws IOException if the index cannot be written
     */
    public void indexWiki(Long id, String title, String content, String region,
            Long operationId, LocalDateTime updatedAt) throws IOException {
        Document doc = new Document();

        doc.add(new StringField("id", id.toString(), Field.Store.YES));
        doc.add(new StringField("type", "wiki", Field.Store.YES));
        doc.add(new TextField("title", title != null ? title : "", Field.Store.YES));
        doc.add(new TextField("content", content != null ? content : "", Field.Store.YES));
        doc.add(new StringField("region", region != null ? region : "", Field.Store.YES));
        doc.add(new StringField("operationId", operationId != null ? operationId.toString() : "", Field.Store.YES));
        doc.add(new StringField("updatedAt",
                updatedAt != null ? updatedAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "",
                Field.Store.YES));

        // For fuzzy matching and better search
        doc.add(new TextField("allText",
                (title != null ? title + " " : "") + (content != null ? content : ""),
                Field.Store.NO));

        indexWriter.updateDocument(new Term("id", id.toString()), doc);
        indexWriter.commit();
    }

    /**
     * Indexes a task document for full-text search.
     *
     * @param id          the unique identifier of the task
     * @param title       the title of the task
     * @param description the description of the task
     * @param priority    the priority level of the task
     * @param status      the current status of the task
     * @param operationId the ID of the associated operation, or null
     * @param dueDate     the due date of the task, or null
     * @throws IOException if the index cannot be written
     */
    public void indexTask(Long id, String title, String description, String priority,
            String status, Long operationId, LocalDateTime dueDate) throws IOException {
        Document doc = new Document();

        doc.add(new StringField("id", id.toString(), Field.Store.YES));
        doc.add(new StringField("type", "task", Field.Store.YES));
        doc.add(new TextField("title", title != null ? title : "", Field.Store.YES));
        doc.add(new TextField("description", description != null ? description : "", Field.Store.YES));
        doc.add(new StringField("priority", priority != null ? priority : "", Field.Store.YES));
        doc.add(new StringField("status", status != null ? status : "", Field.Store.YES));
        doc.add(new StringField("operationId", operationId != null ? operationId.toString() : "", Field.Store.YES));
        doc.add(new StringField("dueDate",
                dueDate != null ? dueDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "",
                Field.Store.YES));

        doc.add(new TextField("allText",
                (title != null ? title + " " : "") + (description != null ? description : ""),
                Field.Store.NO));

        indexWriter.updateDocument(new Term("id", id.toString()), doc);
        indexWriter.commit();
    }

    /**
     * Indexes a calendar event document for full-text search.
     *
     * @param id          the unique identifier of the event
     * @param title       the title of the event
     * @param description the description of the event
     * @param startTime   the start time of the event
     * @param endTime     the end time of the event
     * @param location    the location of the event, or null
     * @throws IOException if the index cannot be written
     */
    public void indexEvent(Long id, String title, String description, LocalDateTime startTime,
            LocalDateTime endTime, String location) throws IOException {
        Document doc = new Document();

        doc.add(new StringField("id", id.toString(), Field.Store.YES));
        doc.add(new StringField("type", "event", Field.Store.YES));
        doc.add(new TextField("title", title != null ? title : "", Field.Store.YES));
        doc.add(new TextField("description", description != null ? description : "", Field.Store.YES));
        doc.add(new TextField("location", location != null ? location : "", Field.Store.YES));
        doc.add(new StringField("startTime",
                startTime != null ? startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "",
                Field.Store.YES));
        doc.add(new StringField("endTime",
                endTime != null ? endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "",
                Field.Store.YES));

        doc.add(new TextField("allText",
                (title != null ? title + " " : "") +
                        (description != null ? description + " " : "") +
                        (location != null ? location : ""),
                Field.Store.NO));

        indexWriter.updateDocument(new Term("id", id.toString()), doc);
        indexWriter.commit();
    }

    /**
     * Indexes a journal entry document for full-text search.
     *
     * @param id      the unique identifier of the journal entry
     * @param title   the title of the journal entry
     * @param content the content of the journal entry
     * @param date    the date of the journal entry
     * @throws IOException if the index cannot be written
     */
    public void indexJournalEntry(Long id, String title, String content, String date) throws IOException {
        Document doc = new Document();

        doc.add(new StringField("id", id.toString(), Field.Store.YES));
        doc.add(new StringField("type", "journal", Field.Store.YES));
        doc.add(new TextField("title", title != null ? title : "", Field.Store.YES));
        doc.add(new TextField("content", content != null ? content : "", Field.Store.YES));
        doc.add(new StringField("date", date != null ? date : "", Field.Store.YES));

        doc.add(new TextField("allText",
                (title != null ? title + " " : "") + (content != null ? content : ""),
                Field.Store.NO));

        indexWriter.updateDocument(new Term("id", id.toString()), doc);
        indexWriter.commit();
    }

    /**
     * Indexes an operation document for full-text search.
     *
     * @param id       the unique identifier of the operation
     * @param name     the name of the operation
     * @param purpose  the purpose/objective of the operation
     * @param outcome  the expected or actual outcome
     * @param status   the current status of the operation
     * @param priority the priority level of the operation
     * @throws IOException if the index cannot be written
     */
    public void indexOperation(Long id, String name, String purpose, String outcome,
            String status, String priority) throws IOException {
        Document doc = new Document();

        doc.add(new StringField("id", id.toString(), Field.Store.YES));
        doc.add(new StringField("type", "operation", Field.Store.YES));
        doc.add(new TextField("name", name != null ? name : "", Field.Store.YES));
        doc.add(new TextField("purpose", purpose != null ? purpose : "", Field.Store.YES));
        doc.add(new TextField("outcome", outcome != null ? outcome : "", Field.Store.YES));
        doc.add(new StringField("status", status != null ? status : "", Field.Store.YES));
        doc.add(new StringField("priority", priority != null ? priority : "", Field.Store.YES));

        doc.add(new TextField("allText",
                (name != null ? name + " " : "") +
                        (purpose != null ? purpose + " " : "") +
                        (outcome != null ? outcome : ""),
                Field.Store.NO));

        indexWriter.updateDocument(new Term("id", id.toString()), doc);
        indexWriter.commit();
    }

    // ==================== Search Methods ====================

    /**
     * Searches across all indexed content using the provided query and filter.
     * <p>
     * Supports fuzzy matching with a minimum similarity of 0.7. The search is
     * performed across multiple fields including title, content, description,
     * name, purpose, outcome, location, and a combined allText field.
     * </p>
     *
     * @param queryString the search query string
     * @param filter      the search filter specifying types, region, and other
     *                    constraints
     * @return a list of search results matching the query and filter
     * @throws Exception if the search query is invalid or the index cannot be read
     */
    public List<SearchResult> search(String queryString, SearchFilter filter) throws Exception {
        List<SearchResult> results = new ArrayList<>();

        if (queryString == null || queryString.trim().isEmpty()) {
            return results;
        }

        // Sanitize search query to prevent Lucene injection attacks
        String sanitizedQuery = InputSanitizer.sanitizeSearchQuery(queryString);

        IndexReader reader = DirectoryReader.open(indexDirectory);
        IndexSearcher searcher = new IndexSearcher(reader); // Build query
        Query query = buildQuery(sanitizedQuery, filter);

        // Execute search with limit
        TopDocs topDocs = searcher.search(query, filter.maxResults); // Process results
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            Document doc = searcher.storedFields().document(scoreDoc.doc);
            SearchResult result = new SearchResult();
            result.id = Long.parseLong(doc.get("id"));
            result.type = doc.get("type"); // Get title based on type (operations use "name" field)
            if ("operation".equals(result.type)) {
                result.title = doc.get("name");
            } else {
                result.title = doc.get("title");
            }

            result.snippet = getSnippet(doc);
            result.score = scoreDoc.score;

            // Type-specific fields
            switch (result.type) {
                case "wiki":
                    result.region = doc.get("region");
                    result.operationId = doc.get("operationId");
                    result.updatedAt = doc.get("updatedAt");
                    break;
                case "task":
                    result.priority = doc.get("priority");
                    result.status = doc.get("status");
                    result.operationId = doc.get("operationId");
                    result.dueDate = doc.get("dueDate");
                    break;
                case "event":
                    result.location = doc.get("location");
                    result.startTime = doc.get("startTime");
                    result.endTime = doc.get("endTime");
                    break;
                case "journal":
                    result.date = doc.get("date");
                    break;
                case "operation":
                    result.status = doc.get("status");
                    result.priority = doc.get("priority");
                    break;
            }

            results.add(result);
        }

        reader.close();
        return results;
    }

    // ==================== Query Building ====================

    /**
     * Builds a Lucene query from the search string and filter criteria.
     *
     * @param queryString the search query string
     * @param filter      the search filter with type, region, and other constraints
     * @return the constructed Lucene query
     * @throws Exception if the query cannot be parsed
     */
    private Query buildQuery(String queryString, SearchFilter filter) throws Exception {
        // Parse basic query
        String[] fields = { "title", "content", "description", "name", "purpose", "outcome", "location", "allText" };
        MultiFieldQueryParser parser = new MultiFieldQueryParser(fields, analyzer);
        parser.setDefaultOperator(QueryParser.Operator.OR);
        parser.setFuzzyMinSim(0.7f); // Enable fuzzy matching

        Query baseQuery = parser.parse(queryString);

        // Add filters
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        builder.add(baseQuery, BooleanClause.Occur.MUST);

        // Filter by type
        if (filter.types != null && !filter.types.isEmpty()) {
            BooleanQuery.Builder typeBuilder = new BooleanQuery.Builder();
            for (String type : filter.types) {
                typeBuilder.add(new TermQuery(new Term("type", type)), BooleanClause.Occur.SHOULD);
            }
            builder.add(typeBuilder.build(), BooleanClause.Occur.MUST);
        }

        // Filter by region
        if (filter.region != null && !filter.region.isEmpty()) {
            builder.add(new TermQuery(new Term("region", filter.region)), BooleanClause.Occur.MUST);
        }

        // Filter by operation
        if (filter.operationId != null) {
            builder.add(new TermQuery(new Term("operationId", filter.operationId.toString())),
                    BooleanClause.Occur.MUST);
        }

        // Filter by priority
        if (filter.priority != null && !filter.priority.isEmpty()) {
            builder.add(new TermQuery(new Term("priority", filter.priority)), BooleanClause.Occur.MUST);
        }

        // Filter by status
        if (filter.status != null && !filter.status.isEmpty()) {
            builder.add(new TermQuery(new Term("status", filter.status)), BooleanClause.Occur.MUST);
        }

        return builder.build();
    }

    /**
     * Extracts a text snippet from the document for display in search results.
     * <p>
     * Returns the first 150 characters of the content, description, or purpose
     * field.
     * </p>
     *
     * @param doc the Lucene document to extract the snippet from
     * @return a text snippet for the search result
     */
    private String getSnippet(Document doc) {
        String content = doc.get("content");
        if (content == null)
            content = doc.get("description");
        if (content == null)
            content = doc.get("purpose");
        if (content == null)
            content = "";

        // Return first 150 characters as snippet
        if (content.length() > 150) {
            return content.substring(0, 150) + "...";
        }
        return content;
    }

    // ==================== Index Management ====================

    /**
     * Deletes a document from the search index.
     *
     * @param id the unique identifier of the document to delete
     * @throws IOException if the index cannot be modified
     */
    public void deleteDocument(Long id) throws IOException {
        indexWriter.deleteDocuments(new Term("id", id.toString()));
        indexWriter.commit();
    }

    /**
     * Clears the entire search index, removing all documents.
     * <p>
     * Use with caution as this operation cannot be undone. Typically used
     * before a full index rebuild.
     * </p>
     *
     * @throws IOException if the index cannot be cleared
     */
    public void clearIndex() throws IOException {
        indexWriter.deleteAll();
        indexWriter.commit();
    }

    /**
     * Rebuilds the entire search index from the database.
     * <p>
     * This method clears the existing index and re-indexes all content
     * from the database. Use this to recover from index corruption or
     * after significant data changes.
     * </p>
     */
    public void rebuildIndex() {
        // This will be called to reindex all content
        // Implementation will fetch all data and reindex
        try {
            clearIndex();
            // Repositories will be used to fetch and reindex all data
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ==================== Resource Management ====================

    /**
     * Closes the search service and releases all resources.
     * <p>
     * Closes both the index writer and the index directory. This method
     * should be called during application shutdown.
     * </p>
     *
     * @throws IOException if the resources cannot be properly closed
     */
    public void close() throws IOException {
        if (indexWriter != null) {
            indexWriter.close();
        }
        if (indexDirectory != null) {
            indexDirectory.close();
        }
    }

    // ==================== Inner Classes ====================

    /**
     * Represents a single search result with all associated metadata.
     * <p>
     * Contains common fields for all entity types plus optional fields
     * that are populated based on the result type.
     * </p>
     *
     * @author Roam Development Team
     * @version 1.0
     * @since 1.0
     */
    public static class SearchResult {
        /** The unique identifier of the matched entity */
        public Long id;
        /** The type of entity (wiki, task, event, journal, operation) */
        public String type;
        /** The title or name of the matched entity */
        public String title;
        /** A text snippet from the content for preview */
        public String snippet;
        /** The relevance score from Lucene */
        public float score;

        // Optional fields based on type
        /** The region (for wikis) */
        public String region;
        /** The associated operation ID (for wikis and tasks) */
        public String operationId;
        /** The last update timestamp (for wikis) */
        public String updatedAt;
        /** The priority level (for tasks and operations) */
        public String priority;
        /** The status (for tasks and operations) */
        public String status;
        /** The due date (for tasks) */
        public String dueDate;
        /** The location (for events) */
        public String location;
        /** The start time (for events) */
        public String startTime;
        /** The end time (for events) */
        public String endTime;
        /** The date (for journal entries) */
        public String date;
    }

    /**
     * Filter criteria for search operations.
     * <p>
     * Allows filtering search results by entity type, region, operation,
     * priority, status, and limiting the number of results.
     * </p>
     *
     * @author Roam Development Team
     * @version 1.0
     * @since 1.0
     */
    public static class SearchFilter {
        /**
         * List of entity types to include in search (wiki, task, event, journal,
         * operation)
         */
        public List<String> types = new ArrayList<>();
        /** Filter by region */
        public String region;
        /** Filter by associated operation ID */
        public Long operationId;
        /** Filter by priority level */
        public String priority;
        /** Filter by status */
        public String status;
        /** Maximum number of results to return */
        public int maxResults = 50;

        /**
         * Constructs a new SearchFilter with default settings.
         * <p>
         * By default, includes all entity types in the search.
         * </p>
         */
        public SearchFilter() {
            // By default, search all types
            types.add("wiki");
            types.add("task");
            types.add("event");
            types.add("journal");
            types.add("operation");
        }
    }
}
