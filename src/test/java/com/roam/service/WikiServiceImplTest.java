package com.roam.service;

import com.roam.model.Wiki;
import com.roam.repository.WikiRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WikiServiceImplTest {

    @Mock
    private WikiRepository repository;

    @Mock
    private SearchService searchService;

    private WikiServiceImpl service;
    private Wiki testWiki;

    @BeforeEach
    void setUp() {
        service = new WikiServiceImpl(repository, searchService);

        testWiki = new Wiki("Test Wiki", 10L);
        testWiki.setId(1L);
        testWiki.setContent("# Test Content\n\nThis is test markdown content.");
        testWiki.setRegion("Knowledge");
        testWiki.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void createWiki_Success() throws Exception {
        // Arrange
        when(repository.save(any(Wiki.class))).thenReturn(testWiki);
        doNothing().when(searchService).indexWiki(anyLong(), anyString(), anyString(), anyString(), anyLong(),
                any(LocalDateTime.class));

        // Act
        Wiki result = service.createWiki(testWiki);

        // Assert
        assertNotNull(result);
        assertEquals(testWiki.getId(), result.getId());
        assertEquals(testWiki.getTitle(), result.getTitle());
        verify(repository, times(1)).save(testWiki);
        verify(searchService, times(1)).indexWiki(
                eq(testWiki.getId()),
                eq(testWiki.getTitle()),
                eq(testWiki.getContent()),
                anyString(),
                eq(testWiki.getOperationId()),
                any(LocalDateTime.class));
    }

    @Test
    void createWiki_NullWiki_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.createWiki(null));
        verify(repository, never()).save(any());
    }

    @Test
    void updateWiki_Success() throws Exception {
        // Arrange
        Wiki updated = new Wiki("Updated Wiki", 10L);
        updated.setId(1L);
        updated.setContent("Updated content");
        when(repository.save(any(Wiki.class))).thenReturn(updated);

        // Act
        Wiki result = service.updateWiki(updated);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Wiki", result.getTitle());
        verify(repository, times(1)).save(updated);
    }

    @Test
    void updateWiki_NullWiki_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.updateWiki(null));
        verify(repository, never()).save(any());
    }

    @Test
    void deleteWiki_Success() throws Exception {
        // Arrange
        Long wikiId = 1L;
        when(repository.findById(wikiId)).thenReturn(Optional.of(testWiki));
        doNothing().when(repository).delete(wikiId);
        doNothing().when(searchService).deleteDocument(wikiId);

        // Act
        service.deleteWiki(wikiId);

        // Assert
        verify(repository, times(1)).findById(wikiId);
        verify(repository, times(1)).delete(wikiId);
        verify(searchService, times(1)).deleteDocument(wikiId);
    }

    @Test
    void deleteWiki_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.deleteWiki(null));
        verify(repository, never()).delete(any(Long.class));
    }

    @Test
    void findById_Found() {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.of(testWiki));

        // Act
        Optional<Wiki> result = service.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testWiki.getId(), result.get().getId());
        verify(repository, times(1)).findById(1L);
    }

    @Test
    void findById_NotFound() {
        // Arrange
        when(repository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Wiki> result = service.findById(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(repository, times(1)).findById(999L);
    }

    @Test
    void findAll_Success() {
        // Arrange
        Wiki wiki2 = new Wiki("Second Wiki", 10L);
        wiki2.setId(2L);
        List<Wiki> wikis = Arrays.asList(testWiki, wiki2);
        when(repository.findAll()).thenReturn(wikis);

        // Act
        List<Wiki> result = service.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(repository, times(1)).findAll();
    }

    @Test
    void findByOperationId_Success() {
        // Arrange
        Long operationId = 10L;
        List<Wiki> wikis = Arrays.asList(testWiki);
        when(repository.findByOperationId(operationId)).thenReturn(wikis);

        // Act
        List<Wiki> result = service.findByOperationId(operationId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(operationId, result.get(0).getOperationId());
        verify(repository, times(1)).findByOperationId(operationId);
    }

    @Test
    void findByTaskId_Success() {
        // Arrange
        Long taskId = 100L;
        testWiki.setTaskId(taskId);
        when(repository.findAll()).thenReturn(Arrays.asList(testWiki));

        // Act
        List<Wiki> result = service.findByTaskId(taskId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(taskId, result.get(0).getTaskId());
    }

    @Test
    void findFavorites_Success() {
        // Arrange
        testWiki.setIsFavorite(true);
        List<Wiki> favorites = Arrays.asList(testWiki);
        when(repository.findFavorites()).thenReturn(favorites);

        // Act
        List<Wiki> result = service.findFavorites();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getIsFavorite());
        verify(repository, times(1)).findFavorites();
    }

    @Test
    void findRecent_Success() {
        // Arrange
        int limit = 5;
        List<Wiki> wikis = Arrays.asList(testWiki);
        when(repository.findRecent(limit)).thenReturn(wikis);

        // Act
        List<Wiki> result = service.findRecent(limit);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(repository, times(1)).findRecent(limit);
    }

    @Test
    void toggleFavorite_Success() {
        // Arrange
        Long wikiId = 1L;
        testWiki.setIsFavorite(false);
        when(repository.findById(wikiId)).thenReturn(Optional.of(testWiki));
        when(repository.save(any(Wiki.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Wiki result = service.toggleFavorite(wikiId);

        // Assert
        assertNotNull(result);
        assertTrue(result.getIsFavorite());
        verify(repository, times(1)).save(testWiki);
    }

    @Test
    void toggleFavorite_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.toggleFavorite(null));
        verify(repository, never()).save(any());
    }

    @Test
    void toggleFavorite_NotFound_ThrowsException() {
        // Arrange
        Long wikiId = 999L;
        when(repository.findById(wikiId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.toggleFavorite(wikiId));
        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        verify(repository, never()).save(any());
    }

    @Test
    void indexWiki_Success() throws Exception {
        // Arrange
        doNothing().when(searchService).indexWiki(anyLong(), anyString(), anyString(), anyString(), anyLong(),
                any(LocalDateTime.class));

        // Act
        service.indexWiki(testWiki);

        // Assert
        verify(searchService, times(1)).indexWiki(
                eq(testWiki.getId()),
                eq(testWiki.getTitle()),
                eq(testWiki.getContent()),
                anyString(),
                eq(testWiki.getOperationId()),
                any(LocalDateTime.class));
    }
}
