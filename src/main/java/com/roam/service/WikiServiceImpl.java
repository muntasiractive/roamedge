package com.roam.service;

import com.roam.model.Wiki;
import com.roam.repository.WikiRepository;
import com.roam.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of the {@link WikiService} interface providing wiki page
 * management
 * functionality with full transaction support.
 * 
 * <p>
 * This service manages wiki pages, which serve as documentation and knowledge
 * base
 * entries within operations and tasks. Wikis support rich content, favorites,
 * regions,
 * and can be linked to operations or tasks for contextual organization.
 * </p>
 * 
 * <p>
 * Key features:
 * </p>
 * <ul>
 * <li>Transaction-managed persistence with automatic rollback on failure</li>
 * <li>Favorites management for quick access to important wikis</li>
 * <li>Operation and task-based wiki grouping</li>
 * <li>Recent wikis retrieval with configurable limits</li>
 * <li>Full-text search indexing integration</li>
 * </ul>
 * 
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 * @see WikiService
 * @see Wiki
 * @see WikiRepository
 */
public class WikiServiceImpl implements WikiService {

    private static final Logger logger = LoggerFactory.getLogger(WikiServiceImpl.class);
    private final WikiRepository repository;
    private final SearchService searchService;

    // ==================== Constructors ====================

    /**
     * Default constructor that initializes repository and services with default
     * implementations.
     */
    public WikiServiceImpl() {
        this.repository = new WikiRepository();
        this.searchService = SearchService.getInstance();
    }

    /**
     * Constructor for dependency injection, primarily used for testing.
     * 
     * @param repository    the wiki repository
     * @param searchService the search service for indexing wikis
     */
    public WikiServiceImpl(WikiRepository repository, SearchService searchService) {
        this.repository = repository;
        this.searchService = searchService;
    }

    // ==================== CRUD Operations ====================

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Creates a new wiki page within a managed transaction and indexes it for
     * search.
     * </p>
     * 
     * @throws IllegalArgumentException if the wiki is null
     * @throws RuntimeException         if the transaction fails
     */
    @Override
    public Wiki createWiki(Wiki wiki) {
        if (wiki == null) {
            throw new IllegalArgumentException("Wiki cannot be null");
        }

        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            Wiki created = repository.save(wiki);

            tx.commit();
            logger.info("✓ Wiki created: {}", created.getTitle());

            indexWiki(created);

            return created;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
                logger.warn("⚠️ Transaction rolled back for wiki creation");
            }
            logger.error("✗ Failed to create wiki: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create wiki", e);
        } finally {
            em.close();
        }
    }

    @Override
    public Wiki updateWiki(Wiki wiki) {
        if (wiki == null || wiki.getId() == null) {
            throw new IllegalArgumentException("Wiki and ID cannot be null");
        }

        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            Wiki updated = repository.save(wiki);

            tx.commit();
            logger.info("✓ Wiki updated: {}", updated.getTitle());

            indexWiki(updated);

            return updated;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
                logger.warn("⚠️ Transaction rolled back for wiki update");
            }
            logger.error("✗ Failed to update wiki: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update wiki", e);
        } finally {
            em.close();
        }
    }

    @Override
    public void deleteWiki(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Wiki ID cannot be null");
        }

        try {
            // Get wiki title for logging before deletion
            Optional<Wiki> wiki = repository.findById(id);
            String title = wiki.map(Wiki::getTitle).orElse("Unknown");

            // Repository handles its own transaction
            repository.delete(id);
            logger.info("✓ Wiki deleted: {}", title);

            // Remove from search index
            searchService.deleteDocument(id);

        } catch (Exception e) {
            logger.error("✗ Failed to delete wiki: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete wiki", e);
        }
    }

    @Override
    public Optional<Wiki> findById(Long id) {
        try {
            return repository.findById(id);
        } catch (Exception e) {
            logger.error("✗ Failed to find wiki by ID {}: {}", id, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public List<Wiki> findAll() {
        try {
            return repository.findAll();
        } catch (Exception e) {
            logger.error("✗ Failed to find all wikis: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve wikis", e);
        }
    }

    // ==================== Query Operations ====================

    @Override
    public List<Wiki> findByOperationId(Long operationId) {
        try {
            return repository.findByOperationId(operationId);
        } catch (Exception e) {
            logger.error("✗ Failed to find wikis by operation ID {}: {}", operationId, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve wikis by operation", e);
        }
    }

    @Override
    public List<Wiki> findByTaskId(Long taskId) {
        try {
            // WikiRepository doesn't have findByTaskId method
            // Need to query by taskId field
            return repository.findAll().stream()
                    .filter(w -> taskId.equals(w.getTaskId()))
                    .toList();
        } catch (Exception e) {
            logger.error("✗ Failed to find wikis by task ID: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve wikis", e);
        }
    }

    @Override
    public List<Wiki> findFavorites() {
        try {
            return repository.findFavorites();
        } catch (Exception e) {
            logger.error("✗ Failed to find favorite wikis: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve favorite wikis", e);
        }
    }

    @Override
    public List<Wiki> findRecent(int limit) {
        try {
            return repository.findRecent(limit);
        } catch (Exception e) {
            logger.error("✗ Failed to find recent wikis: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve recent wikis", e);
        }
    }

    // ==================== Favorites Management ====================

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Toggles the favorite status of a wiki page within a managed transaction.
     * If the wiki is currently a favorite, it will be unfavorited, and vice versa.
     * </p>
     * 
     * @throws IllegalArgumentException if id is null or wiki not found
     * @throws RuntimeException         if the transaction fails
     */
    @Override
    public Wiki toggleFavorite(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Wiki ID cannot be null");
        }

        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            Optional<Wiki> wikiOpt = repository.findById(id);
            if (wikiOpt.isEmpty()) {
                throw new IllegalArgumentException("Wiki not found: " + id);
            }

            Wiki wiki = wikiOpt.get();
            wiki.setIsFavorite(!wiki.getIsFavorite());
            Wiki updated = repository.save(wiki);

            tx.commit();
            logger.info("✓ Wiki favorite toggled: {} = {}", updated.getTitle(), updated.getIsFavorite());

            return updated;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
                logger.warn("⚠️ Transaction rolled back for wiki favorite toggle");
            }
            logger.error("✗ Failed to toggle wiki favorite: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to toggle wiki favorite", e);
        } finally {
            em.close();
        }
    }

    // ==================== Search Indexing ====================

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Indexes the wiki page for full-text search functionality.
     * </p>
     */
    @Override
    public void indexWiki(Wiki wiki) {
        try {
            searchService.indexWiki(
                    wiki.getId(),
                    wiki.getTitle(),
                    wiki.getContent(),
                    wiki.getRegion(),
                    wiki.getOperationId(),
                    wiki.getUpdatedAt());
            logger.debug("✓ Wiki indexed: {}", wiki.getTitle());
        } catch (Exception e) {
            logger.error("✗ Failed to index wiki: {}", e.getMessage(), e);
        }
    }
}
