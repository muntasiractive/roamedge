package com.roam.service;

import com.roam.model.JournalEntry;
import com.roam.repository.JournalEntryRepository;
import com.roam.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of the {@link JournalService} interface providing journal
 * entry management
 * functionality with full transaction support.
 * 
 * <p>
 * This service manages daily journal entries, allowing users to create, update,
 * and
 * retrieve personal notes and reflections. All persistence operations are
 * executed within
 * managed transactions with automatic rollback on failure.
 * </p>
 * 
 * <p>
 * Key features:
 * </p>
 * <ul>
 * <li>Transaction-managed persistence operations</li>
 * <li>Date-based entry lookup and range queries</li>
 * <li>Full-text search indexing integration</li>
 * <li>Recent entries retrieval with configurable limits</li>
 * </ul>
 * 
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 * @see JournalService
 * @see JournalEntry
 * @see JournalEntryRepository
 */
public class JournalServiceImpl implements JournalService {

    private static final Logger logger = LoggerFactory.getLogger(JournalServiceImpl.class);
    private final JournalEntryRepository repository;
    private final SearchService searchService;

    // ==================== Constructors ====================

    /**
     * Default constructor that initializes repository and services with default
     * implementations.
     */
    public JournalServiceImpl() {
        this.repository = new JournalEntryRepository();
        this.searchService = SearchService.getInstance();
    }

    /**
     * Constructor for dependency injection, primarily used for testing.
     * 
     * @param repository    the journal entry repository
     * @param searchService the search service for indexing entries
     */
    public JournalServiceImpl(JournalEntryRepository repository, SearchService searchService) {
        this.repository = repository;
        this.searchService = searchService;
    }

    // ==================== CRUD Operations ====================

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Creates a new journal entry within a managed transaction and indexes it for
     * search.
     * </p>
     * 
     * @throws IllegalArgumentException if the entry is null
     * @throws RuntimeException         if the transaction fails
     */
    @Override
    public JournalEntry createEntry(JournalEntry entry) {
        if (entry == null) {
            throw new IllegalArgumentException("JournalEntry cannot be null");
        }

        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            JournalEntry created = repository.save(entry);

            tx.commit();
            logger.info("✓ Journal entry created for date: {}", created.getDate());

            indexEntry(created);

            return created;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("✗ Failed to create journal entry: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create journal entry", e);
        } finally {
            em.close();
        }
    }

    @Override
    public JournalEntry updateEntry(JournalEntry entry) {
        if (entry == null || entry.getId() == null) {
            throw new IllegalArgumentException("JournalEntry and ID cannot be null");
        }

        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            JournalEntry updated = repository.save(entry);

            tx.commit();
            logger.info("✓ Journal entry updated for date: {}", updated.getDate());

            indexEntry(updated);

            return updated;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("✗ Failed to update journal entry: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update journal entry", e);
        } finally {
            em.close();
        }
    }

    @Override
    public void deleteEntry(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Entry ID cannot be null");
        }

        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            Optional<JournalEntry> entry = repository.findById(id);
            if (entry.isPresent()) {
                repository.delete(entry.get());
                logger.info("✓ Journal entry deleted for date: {}", entry.get().getDate());
            }

            tx.commit();

            searchService.deleteDocument(id);

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("✗ Failed to delete journal entry: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete journal entry", e);
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<JournalEntry> findById(Long id) {
        try {
            return repository.findById(id);
        } catch (Exception e) {
            logger.error("✗ Failed to find journal entry by ID {}: {}", id, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public List<JournalEntry> findAll() {
        try {
            return repository.findAll();
        } catch (Exception e) {
            logger.error("✗ Failed to find all journal entries: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve journal entries", e);
        }
    }

    // ==================== Query Operations ====================

    @Override
    public List<JournalEntry> findByDate(LocalDate date) {
        try {
            Optional<JournalEntry> entry = repository.findByDate(date);
            return entry.map(List::of).orElse(List.of());
        } catch (Exception e) {
            logger.error("✗ Failed to find journal entry by date: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve journal entry", e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Retrieves journal entries within the specified date range, ordered by date
     * descending.
     * </p>
     * 
     * @param startDate the start date of the range (inclusive)
     * @param endDate   the end date of the range (inclusive)
     * @return list of entries within the date range
     */
    @Override
    public List<JournalEntry> findByDateRange(LocalDate startDate, LocalDate endDate) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<JournalEntry> query = em.createQuery(
                    "SELECT j FROM JournalEntry j WHERE j.date >= :startDate AND j.date <= :endDate ORDER BY j.date DESC",
                    JournalEntry.class);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.getResultList();
        } catch (Exception e) {
            logger.error("✗ Failed to find journal entries by date range: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve journal entries", e);
        } finally {
            em.close();
        }
    }

    @Override
    public List<JournalEntry> findRecent(int limit) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<JournalEntry> query = em.createQuery(
                    "SELECT j FROM JournalEntry j ORDER BY j.date DESC",
                    JournalEntry.class);
            query.setMaxResults(limit);
            return query.getResultList();
        } catch (Exception e) {
            logger.error("✗ Failed to find recent journal entries: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve recent journal entries", e);
        } finally {
            em.close();
        }
    }

    // ==================== Utility Methods ====================

    @Override
    public long count() {
        try {
            return repository.findAll().size();
        } catch (Exception e) {
            logger.error("✗ Failed to count journal entries: {}", e.getMessage(), e);
            return 0;
        }
    }

    // ==================== Search Indexing ====================

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Indexes the journal entry for full-text search functionality.
     * </p>
     */
    @Override
    public void indexEntry(JournalEntry entry) {
        try {
            searchService.indexJournalEntry(
                    entry.getId(),
                    entry.getTitle(),
                    entry.getContent(),
                    entry.getDate() != null ? entry.getDate().toString() : null);
            logger.debug("✓ Journal entry indexed for date: {}", entry.getDate());
        } catch (Exception e) {
            logger.error("✗ Failed to index journal entry: {}", e.getMessage(), e);
        }
    }
}
