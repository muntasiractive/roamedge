package com.roam.service;

import com.roam.model.Operation;
import com.roam.model.OperationStatus;
import com.roam.model.Priority;
import com.roam.repository.OperationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OperationServiceImplTest {

    @Mock
    private OperationRepository repository;

    @Mock
    private SearchService searchService;

    private OperationServiceImpl service;
    private Operation testOperation;

    @BeforeEach
    void setUp() {
        service = new OperationServiceImpl(repository, searchService);

        testOperation = new Operation("Test Operation");
        testOperation.setId(1L);
        testOperation.setStatus(OperationStatus.IN_PROGRESS);
        testOperation.setPriority(Priority.HIGH);
        testOperation.setPurpose("Test purpose");
        testOperation.setOutcome("Test outcome");
    }

    @Test
    void createOperation_Success() throws Exception {
        // Arrange
        when(repository.save(any(Operation.class))).thenReturn(testOperation);
        doNothing().when(searchService).indexOperation(anyLong(), anyString(), anyString(), anyString(), anyString(),
                anyString());

        // Act
        Operation result = service.createOperation(testOperation);

        // Assert
        assertNotNull(result);
        assertEquals(testOperation.getId(), result.getId());
        verify(repository, times(1)).save(testOperation);
        verify(searchService, times(1)).indexOperation(
                eq(testOperation.getId()),
                eq(testOperation.getName()),
                anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void createOperation_NullOperation_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.createOperation(null));
        verify(repository, never()).save(any());
    }

    @Test
    void updateOperation_Success() throws Exception {
        // Arrange
        Operation updated = new Operation("Updated Operation");
        updated.setId(1L);
        when(repository.save(any(Operation.class))).thenReturn(updated);
        doNothing().when(searchService).indexOperation(anyLong(), anyString(), anyString(), anyString(), anyString(),
                anyString());

        // Act
        Operation result = service.updateOperation(updated);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Operation", result.getName());
        verify(repository, times(1)).save(updated);
    }

    @Test
    void updateOperation_NullOperation_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.updateOperation(null));
        verify(repository, never()).save(any());
    }

    @Test
    void updateOperation_NullId_ThrowsException() {
        // Arrange
        Operation operation = new Operation("Test");
        operation.setId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.updateOperation(operation));
        verify(repository, never()).save(any());
    }

    @Test
    void deleteOperation_Success() throws Exception {
        // Arrange
        Long operationId = 1L;
        when(repository.findById(operationId)).thenReturn(Optional.of(testOperation));
        doNothing().when(repository).delete(any(Operation.class));
        doNothing().when(searchService).deleteDocument(operationId);

        // Act
        service.deleteOperation(operationId);

        // Assert
        verify(repository, times(1)).findById(operationId);
        verify(repository, times(1)).delete(testOperation);
        verify(searchService, times(1)).deleteDocument(operationId);
    }

    @Test
    void deleteOperation_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.deleteOperation(null));
        verify(repository, never()).delete(any(Operation.class));
    }

    @Test
    void findById_Found() {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.of(testOperation));

        // Act
        Optional<Operation> result = service.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testOperation.getId(), result.get().getId());
        verify(repository, times(1)).findById(1L);
    }

    @Test
    void findById_NotFound() {
        // Arrange
        when(repository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Operation> result = service.findById(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(repository, times(1)).findById(999L);
    }

    @Test
    void findAll_Success() {
        // Arrange
        Operation op2 = new Operation("Second Operation");
        op2.setId(2L);
        List<Operation> operations = Arrays.asList(testOperation, op2);
        when(repository.findAll()).thenReturn(operations);

        // Act
        List<Operation> result = service.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(repository, times(1)).findAll();
    }

    @Test
    void findByStatus_Success() {
        // Arrange
        OperationStatus status = OperationStatus.IN_PROGRESS;
        List<Operation> operations = Arrays.asList(testOperation);
        when(repository.findByStatus(status)).thenReturn(operations);

        // Act
        List<Operation> result = service.findByStatus(status);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(status, result.get(0).getStatus());
        verify(repository, times(1)).findByStatus(status);
    }

    @Test
    void findByPriority_Success() {
        // Arrange
        Priority priority = Priority.HIGH;
        List<Operation> operations = Arrays.asList(testOperation);
        when(repository.findByPriority(priority)).thenReturn(operations);

        // Act
        List<Operation> result = service.findByPriority(priority);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(priority, result.get(0).getPriority());
        verify(repository, times(1)).findByPriority(priority);
    }

    @Test
    void findRecentlyUpdated_Success() {
        // Arrange
        Operation op1 = new Operation("Op1");
        op1.setId(1L);
        op1.setUpdatedAt(java.time.LocalDateTime.now().minusDays(5));

        Operation op2 = new Operation("Op2");
        op2.setId(2L);
        op2.setUpdatedAt(java.time.LocalDateTime.now().minusDays(1));

        Operation op3 = new Operation("Op3");
        op3.setId(3L);
        op3.setUpdatedAt(java.time.LocalDateTime.now());

        when(repository.findAll()).thenReturn(Arrays.asList(op1, op2, op3));

        // Act
        List<Operation> result = service.findRecentlyUpdated();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        // Should be sorted by updatedAt descending (most recent first)
        assertEquals(op3.getId(), result.get(0).getId());
        assertEquals(op2.getId(), result.get(1).getId());
        assertEquals(op1.getId(), result.get(2).getId());
        verify(repository, times(1)).findAll();
    }

    @Test
    void findRecentlyUpdated_LimitsToFive() {
        // Arrange
        List<Operation> manyOperations = new java.util.ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Operation op = new Operation("Op" + i);
            op.setId((long) i);
            op.setUpdatedAt(java.time.LocalDateTime.now().minusDays(i));
            manyOperations.add(op);
        }
        when(repository.findAll()).thenReturn(manyOperations);

        // Act
        List<Operation> result = service.findRecentlyUpdated();

        // Assert
        assertNotNull(result);
        assertEquals(5, result.size());
        // Should be the 5 most recent
        assertEquals(0L, result.get(0).getId());
        assertEquals(1L, result.get(1).getId());
        assertEquals(2L, result.get(2).getId());
        assertEquals(3L, result.get(3).getId());
        assertEquals(4L, result.get(4).getId());
        verify(repository, times(1)).findAll();
    }

    @Test
    void count_Success() {
        // Arrange
        when(repository.count()).thenReturn(15L);

        // Act
        long result = service.count();

        // Assert
        assertEquals(15L, result);
        verify(repository, times(1)).count();
    }

    @Test
    void indexOperation_Success() throws Exception {
        // Arrange
        doNothing().when(searchService).indexOperation(anyLong(), anyString(), anyString(), anyString(), anyString(),
                anyString());

        // Act
        service.indexOperation(testOperation);

        // Assert
        verify(searchService, times(1)).indexOperation(
                eq(testOperation.getId()),
                eq(testOperation.getName()),
                anyString(), anyString(), anyString(), anyString());
    }
}
