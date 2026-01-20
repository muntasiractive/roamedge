package com.roam.service;

import com.roam.model.JournalEntry;
import com.roam.repository.JournalEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JournalServiceImplTest {

    @Mock
    private JournalEntryRepository repository;

    @Mock
    private SearchService searchService;

    private JournalServiceImpl service;
    private JournalEntry testEntry;

    @BeforeEach
    void setUp() {
        service = new JournalServiceImpl(repository, searchService);

        testEntry = new JournalEntry();
        testEntry.setId(1L);
        testEntry.setTitle("Test Journal Entry");
        testEntry.setContent("This is a test journal entry content.");
        testEntry.setDate(LocalDate.now());
    }

    @Test
    void createEntry_Success() throws Exception {
        // Arrange
        when(repository.save(any(JournalEntry.class))).thenReturn(testEntry);
        doNothing().when(searchService).indexJournalEntry(anyLong(), anyString(), anyString(), anyString());

        // Act
        JournalEntry result = service.createEntry(testEntry);

        // Assert
        assertNotNull(result);
        assertEquals(testEntry.getId(), result.getId());
        assertEquals(testEntry.getTitle(), result.getTitle());
        verify(repository, times(1)).save(testEntry);
        verify(searchService, times(1)).indexJournalEntry(
                eq(testEntry.getId()),
                eq(testEntry.getTitle()),
                eq(testEntry.getContent()),
                eq(testEntry.getDate().toString()));
    }

    @Test
    void createEntry_NullEntry_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.createEntry(null));
        verify(repository, never()).save(any());
    }

    @Test
    void updateEntry_Success() throws Exception {
        // Arrange
        JournalEntry updated = new JournalEntry();
        updated.setId(1L);
        updated.setTitle("Updated Entry");
        updated.setContent("Updated content");
        updated.setDate(LocalDate.now());
        when(repository.save(any(JournalEntry.class))).thenReturn(updated);
        doNothing().when(searchService).indexJournalEntry(anyLong(), anyString(), anyString(), anyString());

        // Act
        JournalEntry result = service.updateEntry(updated);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Entry", result.getTitle());
        verify(repository, times(1)).save(updated);
    }

    @Test
    void updateEntry_NullEntry_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.updateEntry(null));
        verify(repository, never()).save(any());
    }

    @Test
    void updateEntry_NullId_ThrowsException() {
        // Arrange
        JournalEntry entry = new JournalEntry();
        entry.setId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.updateEntry(entry));
        verify(repository, never()).save(any());
    }

    @Test
    void deleteEntry_Success() throws Exception {
        // Arrange
        Long entryId = 1L;
        when(repository.findById(entryId)).thenReturn(Optional.of(testEntry));
        doNothing().when(repository).delete(any(JournalEntry.class));
        doNothing().when(searchService).deleteDocument(entryId);

        // Act
        service.deleteEntry(entryId);

        // Assert
        verify(repository, times(1)).findById(entryId);
        verify(repository, times(1)).delete(testEntry);
        verify(searchService, times(1)).deleteDocument(entryId);
    }

    @Test
    void deleteEntry_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.deleteEntry(null));
        verify(repository, never()).delete(any(JournalEntry.class));
    }

    @Test
    void findById_Found() {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.of(testEntry));

        // Act
        Optional<JournalEntry> result = service.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testEntry.getId(), result.get().getId());
        verify(repository, times(1)).findById(1L);
    }

    @Test
    void findById_NotFound() {
        // Arrange
        when(repository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<JournalEntry> result = service.findById(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(repository, times(1)).findById(999L);
    }

    @Test
    void findAll_Success() {
        // Arrange
        JournalEntry entry2 = new JournalEntry();
        entry2.setId(2L);
        entry2.setTitle("Second Entry");
        List<JournalEntry> entries = Arrays.asList(testEntry, entry2);
        when(repository.findAll()).thenReturn(entries);

        // Act
        List<JournalEntry> result = service.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(repository, times(1)).findAll();
    }

    @Test
    void findByDate_Found() {
        // Arrange
        LocalDate date = LocalDate.now();
        when(repository.findByDate(date)).thenReturn(Optional.of(testEntry));

        // Act
        List<JournalEntry> result = service.findByDate(date);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testEntry.getDate(), result.get(0).getDate());
        verify(repository, times(1)).findByDate(date);
    }

    @Test
    void findByDate_NotFound() {
        // Arrange
        LocalDate date = LocalDate.now().minusDays(100);
        when(repository.findByDate(date)).thenReturn(Optional.empty());

        // Act
        List<JournalEntry> result = service.findByDate(date);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repository, times(1)).findByDate(date);
    }

    @Test
    void count_Success() {
        // Arrange
        when(repository.findAll()).thenReturn(Arrays.asList(testEntry, new JournalEntry()));

        // Act
        long result = service.count();

        // Assert
        assertEquals(2L, result);
    }

    @Test
    void indexEntry_Success() throws Exception {
        // Arrange
        doNothing().when(searchService).indexJournalEntry(anyLong(), anyString(), anyString(), anyString());

        // Act
        service.indexEntry(testEntry);

        // Assert
        verify(searchService, times(1)).indexJournalEntry(
                eq(testEntry.getId()),
                eq(testEntry.getTitle()),
                eq(testEntry.getContent()),
                eq(testEntry.getDate().toString()));
    }
}
