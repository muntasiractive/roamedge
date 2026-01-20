package com.roam.service;

import com.roam.model.Task;
import com.roam.model.TaskStatus;
import com.roam.model.Priority;
import com.roam.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository repository;

    @Mock
    private SearchService searchService;

    private TaskServiceImpl service;
    private Task testTask;

    @BeforeEach
    void setUp() {
        service = new TaskServiceImpl(repository, searchService);

        testTask = new Task("Test Task", 10L);
        testTask.setId(1L);
        testTask.setDescription("Test description");
        testTask.setStatus(TaskStatus.TODO);
        testTask.setPriority(Priority.HIGH);
        testTask.setDueDate(LocalDateTime.now().plusDays(7));
    }

    @Test
    void createTask_Success() throws Exception {
        // Arrange
        when(repository.save(any(Task.class))).thenReturn(testTask);
        doNothing().when(searchService).indexTask(anyLong(), anyString(), anyString(), anyString(), anyString(),
                anyLong(), any(LocalDateTime.class));

        // Act
        Task result = service.createTask(testTask);

        // Assert
        assertNotNull(result);
        assertEquals(testTask.getId(), result.getId());
        assertEquals(testTask.getTitle(), result.getTitle());
        verify(repository, times(1)).save(testTask);
        verify(searchService, times(1)).indexTask(
                eq(testTask.getId()),
                eq(testTask.getTitle()),
                eq(testTask.getDescription()),
                anyString(),
                anyString(),
                eq(testTask.getOperationId()),
                any(LocalDateTime.class));
    }

    @Test
    void createTask_NullTask_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.createTask(null));
        verify(repository, never()).save(any());
    }

    @Test
    void updateTask_Success() throws Exception {
        // Arrange
        Task updated = new Task("Updated Task", 10L);
        updated.setId(1L);
        updated.setDescription("Updated description");
        updated.setStatus(TaskStatus.IN_PROGRESS);
        when(repository.save(any(Task.class))).thenReturn(updated);

        // Act
        Task result = service.updateTask(updated);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Task", result.getTitle());
        assertEquals(TaskStatus.IN_PROGRESS, result.getStatus());
        verify(repository, times(1)).save(updated);
    }

    @Test
    void updateTask_NullTask_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.updateTask(null));
        verify(repository, never()).save(any());
    }

    @Test
    void deleteTask_Success() throws Exception {
        // Arrange
        Long taskId = 1L;
        when(repository.findById(taskId)).thenReturn(Optional.of(testTask));
        doNothing().when(repository).delete(any(Task.class));
        doNothing().when(searchService).deleteDocument(taskId);

        // Act
        service.deleteTask(taskId);

        // Assert
        verify(repository, times(1)).findById(taskId);
        verify(repository, times(1)).delete(testTask);
        verify(searchService, times(1)).deleteDocument(taskId);
    }

    @Test
    void deleteTask_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.deleteTask(null));
        verify(repository, never()).delete(any(Task.class));
    }

    @Test
    void findById_Found() {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.of(testTask));

        // Act
        Optional<Task> result = service.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testTask.getId(), result.get().getId());
        verify(repository, times(1)).findById(1L);
    }

    @Test
    void findById_NotFound() {
        // Arrange
        when(repository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Task> result = service.findById(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(repository, times(1)).findById(999L);
    }

    @Test
    void findAll_Success() {
        // Arrange
        Task task2 = new Task("Second Task", 10L);
        task2.setId(2L);
        List<Task> tasks = Arrays.asList(testTask, task2);
        when(repository.findAll()).thenReturn(tasks);

        // Act
        List<Task> result = service.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(repository, times(1)).findAll();
    }

    @Test
    void findByOperationId_Success() {
        // Arrange
        Long operationId = 10L;
        List<Task> tasks = Arrays.asList(testTask);
        when(repository.findByOperationId(operationId)).thenReturn(tasks);

        // Act
        List<Task> result = service.findByOperationId(operationId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(operationId, result.get(0).getOperationId());
        verify(repository, times(1)).findByOperationId(operationId);
    }

    @Test
    void findByStatus_Success() {
        // Arrange
        TaskStatus status = TaskStatus.IN_PROGRESS;
        testTask.setStatus(status);
        when(repository.findAll()).thenReturn(Arrays.asList(testTask));

        // Act
        List<Task> result = service.findByStatus(status);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(status, result.get(0).getStatus());
    }

    @Test
    void findByPriority_Success() {
        // Arrange
        Priority priority = Priority.HIGH;
        when(repository.findAll()).thenReturn(Arrays.asList(testTask));

        // Act
        List<Task> result = service.findByPriority(priority);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(priority, result.get(0).getPriority());
    }

    @Test
    void findDueBefore_Success() {
        // Arrange
        LocalDate futureDate = LocalDate.now().plusDays(10);
        when(repository.findAll()).thenReturn(Arrays.asList(testTask));

        // Act
        List<Task> result = service.findDueBefore(futureDate);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void findOverdue_Success() {
        // Arrange
        testTask.setDueDate(LocalDateTime.now().minusDays(1));
        testTask.setStatus(TaskStatus.TODO);
        when(repository.findAll()).thenReturn(Arrays.asList(testTask));

        // Act
        List<Task> result = service.findOverdue();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void count_Success() {
        // Arrange
        Task task2 = new Task("Task2", 10L);
        when(repository.findAll()).thenReturn(Arrays.asList(testTask, task2));

        // Act
        long result = service.count();

        // Assert
        assertEquals(2L, result);
    }

    @Test
    void countByStatus_Success() {
        // Arrange
        TaskStatus status = TaskStatus.DONE;
        when(repository.countByStatus(status)).thenReturn(5L);

        // Act
        long result = service.countByStatus(status);

        // Assert
        assertEquals(5L, result);
        verify(repository, times(1)).countByStatus(status);
    }

    @Test
    void updateStatus_Success() throws Exception {
        // Arrange
        Long taskId = 1L;
        TaskStatus newStatus = TaskStatus.DONE;
        when(repository.findById(taskId)).thenReturn(Optional.of(testTask));
        when(repository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(searchService).indexTask(anyLong(), anyString(), anyString(), anyString(), anyString(),
                anyLong(), any(LocalDateTime.class));

        // Act
        Task result = service.updateStatus(taskId, newStatus);

        // Assert
        assertNotNull(result);
        assertEquals(newStatus, result.getStatus());
        verify(repository, times(1)).findById(taskId);
        verify(repository, times(1)).save(any(Task.class));
    }

    @Test
    void updateStatus_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.updateStatus(null, TaskStatus.DONE));
        verify(repository, never()).save(any());
    }

    @Test
    void updateStatus_NotFound_ThrowsException() {
        // Arrange
        Long taskId = 999L;
        when(repository.findById(taskId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> service.updateStatus(taskId, TaskStatus.DONE));
        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        verify(repository, never()).save(any());
    }

    @Test
    void indexTask_Success() throws Exception {
        // Arrange
        doNothing().when(searchService).indexTask(anyLong(), anyString(), anyString(), anyString(), anyString(),
                anyLong(), any(LocalDateTime.class));

        // Act
        service.indexTask(testTask);

        // Assert
        verify(searchService, times(1)).indexTask(
                eq(testTask.getId()),
                eq(testTask.getTitle()),
                eq(testTask.getDescription()),
                anyString(),
                anyString(),
                eq(testTask.getOperationId()),
                any(LocalDateTime.class));
    }
}
