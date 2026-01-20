package com.roam.repository;

import com.roam.model.JournalEntry;
import com.roam.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository class for managing {@link JournalEntry} entity persistence
 * operations.
 * <p>
 * This repository provides CRUD operations and specialized query methods for
 * journal entries,
 * including support for date-based lookups. Journal entries represent daily
 * journal records
 * that users create for personal reflection and notes.
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 * @see JournalEntry
 * @see JournalTemplateRepository
 */
public class JournalEntryRepository {

    // ==================== CRUD Operations ====================

    /**
     * Saves a journal entry to the database (insert or update).
     * <p>
     * If the entry's ID is null, a new record is created. Otherwise, the existing
     * record is updated. The operation is wrapped in a transaction with automatic
     * rollback on failure.
     * </p>
     *
     * @param entry the journal entry to save (must not be null)
     * @return the saved journal entry with generated ID (for new entries)
     * @throws RuntimeException if the save operation fails
     */
    public JournalEntry save(JournalEntry entry) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;
        try {
            tx = em.getTransaction();
            tx.begin();
            if (entry.getId() == null) {
                em.persist(entry);
                em.flush(); // Ensure ID is generated
            } else {
                entry = em.merge(entry);
            }
            tx.commit();
            return entry;
        } catch (Exception e) {
            if (tx != null && tx.isActive())
                tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Finds a journal entry by its unique identifier.
     *
     * @param id the unique identifier of the journal entry
     * @return an Optional containing the entry if found, or empty if not found
     */
    public Optional<JournalEntry> findById(Long id) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(JournalEntry.class, id));
        } finally {
            em.close();
        }
    }

    // ==================== Query Methods ====================

    /**
     * Retrieves all journal entries ordered by date descending (newest first).
     *
     * @return a list of all journal entries (may be empty, never null)
     */
    public List<JournalEntry> findAll() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<JournalEntry> query = em.createQuery("SELECT j FROM JournalEntry j ORDER BY j.date DESC",
                    JournalEntry.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Finds a journal entry for a specific date.
     * <p>
     * Each date should have at most one journal entry, so this returns
     * the first matching entry if found.
     * </p>
     *
     * @param date the date to search for
     * @return an Optional containing the entry if found, or empty if not found
     */
    public Optional<JournalEntry> findByDate(LocalDate date) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            TypedQuery<JournalEntry> query = em.createQuery("SELECT j FROM JournalEntry j WHERE j.date = :date",
                    JournalEntry.class);
            query.setParameter("date", date);
            return query.getResultStream().findFirst();
        } finally {
            em.close();
        }
    }

    // ==================== Delete Operations ====================

    /**
     * Deletes a journal entry from the database.
     * <p>
     * The operation is wrapped in a transaction with automatic rollback on failure.
     * If the entry does not exist, the operation completes silently.
     * </p>
     *
     * @param entry the journal entry to delete
     * @throws RuntimeException if the delete operation fails
     */
    public void delete(JournalEntry entry) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = null;
        try {
            tx = em.getTransaction();
            tx.begin();
            JournalEntry managed = em.find(JournalEntry.class, entry.getId());
            if (managed != null) {
                em.remove(managed);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive())
                tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
