package com.roam.service;

import com.roam.model.CalendarEvent;
import com.roam.model.Task;
import com.roam.model.TaskStatus;
import com.roam.model.Priority;
import com.roam.repository.CalendarEventRepository;
import com.roam.repository.TaskRepository;
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
class CalendarServiceImplTest {

    @Mock
    private CalendarEventRepository repository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private SearchService searchService;

    private CalendarServiceImpl service;
    private CalendarEvent testEvent;
    private Task testTask;

    @BeforeEach
    void setUp() {
        service = new CalendarServiceImpl(repository, taskRepository, searchService);

        testEvent = new CalendarEvent();
        testEvent.setId(1L);
        testEvent.setTitle("Test Event");
        testEvent.setDescription("Test description");
        testEvent.setStartDateTime(LocalDateTime.now().plusDays(1));
        testEvent.setEndDateTime(LocalDateTime.now().plusDays(1).plusHours(2));
        testEvent.setLocation("Test Location");
        testEvent.setIsAllDay(false);
        testEvent.setOperationId(10L);
        testEvent.setCalendarSourceId(1L);

        testTask = new Task("Test Task", 10L);
        testTask.setId(100L);
        testTask.setDescription("Task description");
        testTask.setDueDate(LocalDateTime.now().plusDays(7));
        testTask.setStatus(TaskStatus.TODO);
        testTask.setPriority(Priority.HIGH);
    }

    @Test
    void createEvent_Success() throws Exception {
        // Arrange
        when(repository.save(any(CalendarEvent.class))).thenReturn(testEvent);
        doNothing().when(searchService).indexEvent(anyLong(), anyString(), anyString(), any(LocalDateTime.class),
                any(LocalDateTime.class), anyString());

        // Act
        CalendarEvent result = service.createEvent(testEvent);

        // Assert
        assertNotNull(result);
        assertEquals(testEvent.getId(), result.getId());
        assertEquals(testEvent.getTitle(), result.getTitle());
        verify(repository, times(1)).save(testEvent);
        verify(searchService, times(1)).indexEvent(
                eq(testEvent.getId()),
                eq(testEvent.getTitle()),
                eq(testEvent.getDescription()),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(testEvent.getLocation()));
    }

    @Test
    void createEvent_NullEvent_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.createEvent(null));
        verify(repository, never()).save(any());
    }

    @Test
    void updateEvent_Success() throws Exception {
        // Arrange
        CalendarEvent updated = new CalendarEvent();
        updated.setId(1L);
        updated.setTitle("Updated Event");
        when(repository.save(any(CalendarEvent.class))).thenReturn(updated);
        doNothing().when(searchService).indexEvent(anyLong(), anyString(), anyString(), any(LocalDateTime.class),
                any(LocalDateTime.class), anyString());

        // Act
        CalendarEvent result = service.updateEvent(updated);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Event", result.getTitle());
        verify(repository, times(1)).save(updated);
    }

    @Test
    void updateEvent_NullEvent_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.updateEvent(null));
        verify(repository, never()).save(any());
    }

    @Test
    void deleteEvent_Success() throws Exception {
        // Arrange
        Long eventId = 1L;
        when(repository.findById(eventId)).thenReturn(Optional.of(testEvent));
        doNothing().when(repository).delete(eventId);
        doNothing().when(searchService).deleteDocument(eventId);

        // Act
        service.deleteEvent(eventId);

        // Assert
        verify(repository, times(1)).findById(eventId);
        verify(repository, times(1)).delete(eventId);
        verify(searchService, times(1)).deleteDocument(eventId);
    }

    @Test
    void deleteEvent_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.deleteEvent(null));
        verify(repository, never()).delete(anyLong());
    }

    @Test
    void findById_Found() {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.of(testEvent));

        // Act
        Optional<CalendarEvent> result = service.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testEvent.getId(), result.get().getId());
        verify(repository, times(1)).findById(1L);
    }

    @Test
    void findById_NotFound() {
        // Arrange
        when(repository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<CalendarEvent> result = service.findById(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(repository, times(1)).findById(999L);
    }

    @Test
    void findAll_Success() {
        // Arrange
        CalendarEvent event2 = new CalendarEvent();
        event2.setId(2L);
        event2.setTitle("Second Event");
        List<CalendarEvent> events = Arrays.asList(testEvent, event2);
        when(repository.findAll()).thenReturn(events);

        // Act
        List<CalendarEvent> result = service.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(repository, times(1)).findAll();
    }

    @Test
    void findByDateRange_Success() {
        // Arrange
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().plusDays(7);
        List<CalendarEvent> events = Arrays.asList(testEvent);
        when(repository.findByDateRange(start, end)).thenReturn(events);

        // Act
        List<CalendarEvent> result = service.findByDateRange(start, end);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(repository, times(1)).findByDateRange(start, end);
    }

    @Test
    void findBySourceId_Success() {
        // Arrange
        Long sourceId = 1L;
        List<CalendarEvent> events = Arrays.asList(testEvent);
        when(repository.findByCalendarSourceId(sourceId)).thenReturn(events);

        // Act
        List<CalendarEvent> result = service.findBySourceId(sourceId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(repository, times(1)).findByCalendarSourceId(sourceId);
    }

    @Test
    void findByTaskId_Found() {
        // Arrange
        Long taskId = 100L;
        testEvent.setTaskId(taskId);
        when(repository.findByTaskId(taskId)).thenReturn(Optional.of(testEvent));

        // Act
        List<CalendarEvent> result = service.findByTaskId(taskId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(taskId, result.get(0).getTaskId());
        verify(repository, times(1)).findByTaskId(taskId);
    }

    @Test
    void findByTaskId_NotFound() {
        // Arrange
        Long taskId = 999L;
        when(repository.findByTaskId(taskId)).thenReturn(Optional.empty());

        // Act
        List<CalendarEvent> result = service.findByTaskId(taskId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repository, times(1)).findByTaskId(taskId);
    }

    @Test
    void findByOperationId_Success() {
        // Arrange
        Long operationId = 10L;
        List<CalendarEvent> events = Arrays.asList(testEvent);
        when(repository.findByOperationId(operationId)).thenReturn(events);

        // Act
        List<CalendarEvent> result = service.findByOperationId(operationId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(operationId, result.get(0).getOperationId());
        verify(repository, times(1)).findByOperationId(operationId);
    }

    @Test
    void syncTaskToCalendar_CreateNewEvent() throws Exception {
        // Arrange
        Long taskId = 100L;
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        when(repository.findByTaskId(taskId)).thenReturn(Optional.empty());
        when(repository.save(any(CalendarEvent.class))).thenAnswer(invocation -> {
            CalendarEvent event = invocation.getArgument(0);
            event.setId(50L);
            return event;
        });

        // Act
        service.syncTaskToCalendar(taskId);

        // Assert
        verify(taskRepository, times(1)).findById(taskId);
        verify(repository, times(1)).findByTaskId(taskId);
        verify(repository, times(1)).save(any(CalendarEvent.class));
    }

    @Test
    void syncTaskToCalendar_UpdateExistingEvent() throws Exception {
        // Arrange
        Long taskId = 100L;
        testEvent.setTaskId(taskId);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        when(repository.findByTaskId(taskId)).thenReturn(Optional.of(testEvent));
        when(repository.save(any(CalendarEvent.class))).thenReturn(testEvent);
        doNothing().when(searchService).indexEvent(anyLong(), anyString(), anyString(), any(LocalDateTime.class),
                any(LocalDateTime.class), anyString());

        // Act
        service.syncTaskToCalendar(taskId);

        // Assert
        verify(taskRepository, times(1)).findById(taskId);
        verify(repository, times(1)).findByTaskId(taskId);
        verify(repository, times(1)).save(testEvent);
        assertEquals(testTask.getTitle(), testEvent.getTitle());
        assertEquals(testTask.getDescription(), testEvent.getDescription());
    }

    @Test
    void syncTaskToCalendar_NullId_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.syncTaskToCalendar(null));
        verify(taskRepository, never()).findById(any());
    }

    @Test
    void syncTaskToCalendar_TaskNotFound_ThrowsException() {
        // Arrange
        Long taskId = 999L;
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.syncTaskToCalendar(taskId));
        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        verify(repository, never()).save(any());
    }

    @Test
    void syncTaskToCalendar_TaskWithoutDueDate_DoesNothing() {
        // Arrange
        Long taskId = 100L;
        testTask.setDueDate(null);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));

        // Act
        service.syncTaskToCalendar(taskId);

        // Assert
        verify(taskRepository, times(1)).findById(taskId);
        verify(repository, never()).save(any());
    }

    @Test
    void count_Success() {
        // Arrange
        when(repository.findAll()).thenReturn(Arrays.asList(testEvent, new CalendarEvent()));

        // Act
        long result = service.count();

        // Assert
        assertEquals(2L, result);
    }

    @Test
    void indexEvent_Success() throws Exception {
        // Arrange
        doNothing().when(searchService).indexEvent(anyLong(), anyString(), anyString(), any(LocalDateTime.class),
                any(LocalDateTime.class), anyString());

        // Act
        service.indexEvent(testEvent);

        // Assert
        verify(searchService, times(1)).indexEvent(
                eq(testEvent.getId()),
                eq(testEvent.getTitle()),
                eq(testEvent.getDescription()),
                eq(testEvent.getStartDateTime()),
                eq(testEvent.getEndDateTime()),
                eq(testEvent.getLocation()));
    }
}
