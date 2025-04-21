package com.crawler.web_crawler.service.source.implementation;

import com.crawler.web_crawler.converter.SourceRequestDtoMapper;
import com.crawler.web_crawler.exception.SourceAlreadyExistsException;
import com.crawler.web_crawler.exception.SourceNotFoundException;
import com.crawler.web_crawler.model.dto.SourceRequestDTO;
import com.crawler.web_crawler.model.entity.Source;
import com.crawler.web_crawler.repository.SourceRepository;
import com.crawler.web_crawler.scheduler.SchedulerParserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SourceServiceImplTest {
    @Mock
    private SourceRepository repository;
    @Mock
    private SourceRequestDtoMapper mapper;
    @Mock
    private SchedulerParserService schedulerParserService;
    @InjectMocks
    private SourceServiceImpl underTest;

    private Source source1;
    private Source source2;
    private SourceRequestDTO sourceRequestDTO1;
    private SourceRequestDTO sourceRequestDTO2;

    @BeforeEach
    void setUp() {
        source1 = new Source(1L, "https://source1.com", "1 1 1 1 1 1", Map.of("Key1Source1", "Value1Source1", "Key2Source1", "Value2Source1"), true);
        source2 = new Source(2L, "https://source2.com", "2 2 2 2 2 2", Map.of("Key1Source2", "Value1Source2", "Key2Source2", "Value2Source2"), true);
        sourceRequestDTO1 = new SourceRequestDTO("https://source1.com", "1 1 1 1 1 1", Map.of("Key1Source1", "Value1Source1", "Key2Source1", "Value2Source1"), true);
        sourceRequestDTO2 = new SourceRequestDTO("https://source2.com", "2 2 2 2 2 2", Map.of("Key1Source2", "Value1Source2", "Key2Source2", "Value2Source2"), true);
    }

    @Test
    void updateSource_whenSourceExist_shouldReturnDto() {
        // Given
        Long id = 1L;
        String oldUrl = source1.getUrl();
        SourceRequestDTO newSourceRequestDto = new SourceRequestDTO(
                "https://new.com",
                "0 * * * * *",
                Map.of("key1", "value1"),
                false
        );

        when(repository.findById(id)).thenReturn(Optional.of(source1));
        when(repository.save(source1)).thenReturn(source1);
        when(mapper.toDto(source1)).thenReturn(newSourceRequestDto);

        // When
        SourceRequestDTO updatedDto = underTest.updateSource(id, newSourceRequestDto);

        // Then
        assertEquals("https://new.com", source1.getUrl());
        assertEquals("0 * * * * *", source1.getSchedule());
        assertEquals("value1", source1.getSelectors().get("key1"));
        assertFalse(source1.getIsActive());

        verify(repository).save(source1);
        verify(schedulerParserService).cancelScheduleSource(oldUrl);
        verify(schedulerParserService).addNewScheduleSource(source1);
        verify(mapper).toDto(source1);

        assertThat(newSourceRequestDto).isEqualTo(updatedDto);
    }

    @Test
    void updateSource_whenSourceDoesntExist_shouldThrowException() {
        // Given
        Long id = 1L;

        when(repository.findById(id)).thenReturn(Optional.empty());

        // When
        SourceNotFoundException exception = assertThrows(
                SourceNotFoundException.class,
                () -> underTest.updateSource(id, any(SourceRequestDTO.class)));

        // Then
        assertEquals("Source with such id not found", exception.getMessage());

        verify(repository).findById(id);
        verifyNoInteractions(schedulerParserService);
    }

    @Test
    void updateSource_whenSourceExistButUpdateError_shouldThrowException() {
        // Given
        Long id = 1L;
        String oldUrl = source1.getUrl();
        String messageError = "Error updating source with url: " + oldUrl + ". Message: Db error";
        SourceRequestDTO newSourceRequestDto = new SourceRequestDTO(
                "https://new.com",
                "0 * * * * *",
                Map.of("key1", "value1"),
                false
        );

        when(repository.findById(id)).thenReturn(Optional.of(source1));

        doThrow(new DataIntegrityViolationException("Db error")).when(repository).save(source1);

        // When
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> underTest.updateSource(id, newSourceRequestDto)
        );

        // Then
        assertEquals(messageError, exception.getMessage());
        assertThat(exception).hasCauseExactlyInstanceOf(DataIntegrityViolationException.class);
        verify(repository).save(source1);
        verifyNoInteractions(schedulerParserService);
    }

    @Test
    void getSource_whenSourceExist_shouldReturnSource() {
        // Given
        Long id = 1L;

        when(repository.findById(id)).thenReturn(Optional.of(source1));

        // When
        Source sourceFromRepo = underTest.getSource(id);

        // Then
        assertEquals(id, sourceFromRepo.getId());
        assertEquals(source1, sourceFromRepo);

        verify(repository).findById(id);
    }

    @Test
    void getSource_whenSourceDoesntExist_shouldReturnException() {
        // Given
        Long id = 1L;

        when(repository.findById(id)).thenReturn(Optional.empty());

        // When
        SourceNotFoundException exception = assertThrows(
                SourceNotFoundException.class,
                () -> underTest.getSource(id));

        // Then
        assertEquals("Source with such id not found", exception.getMessage());

        verify(repository).findById(id);
    }

    @Test
    void getAllActiveSources_whenActiveSourcesExist_shouldReturnSourceList() {
        // Given
        when(repository.findAllByIsActive()).thenReturn(List.of(source1, source2));

        // When
        List<Source> activeSources = underTest.getAllActiveSources();

        // Then
        assertThat(activeSources).containsExactlyInAnyOrder(source1, source2);
        assertThat(activeSources).hasSize(2);

        verify(repository).findAllByIsActive();
        verifyNoMoreInteractions(repository);
    }

    @Test
    void getAllActiveSources_whenNoActiveSources_shouldReturnEmptySourceList() {
        // Given
        when(repository.findAllByIsActive()).thenReturn(List.of());

        // When
        List<Source> emptyList = underTest.getAllActiveSources();

        // Then
        assertTrue(emptyList.isEmpty());
        verify(repository).findAllByIsActive();
    }

    @Test
    void addSource_whenNoDuplicates_shouldSaveAndReturnNewSource() {
        // Given
        SourceRequestDTO sourceRequestDTO = new SourceRequestDTO("https://source.com", "1 1 1 1 1 1", Map.of("key", "value"), true);
        Source newSource = new Source(null, "https://source.com", "1 1 1 1 1 1", Map.of("key", "value"), true);
        Source savedSource = new Source(3L, "https://source.com", "1 1 1 1 1 1", Map.of("key", "value"), true);

        when(repository.existsByUrl(sourceRequestDTO.url())).thenReturn(false);
        when(mapper.fromDto(sourceRequestDTO)).thenReturn(newSource);
        when(repository.save(newSource)).thenReturn(savedSource);

        // When
        Source result = underTest.addSource(sourceRequestDTO);

        // Then
        verify(repository).existsByUrl(sourceRequestDTO.url());
        verify(schedulerParserService).addNewScheduleSource(savedSource);
        verify(mapper).fromDto(sourceRequestDTO);
        verify(repository).save(newSource);
        assertEquals(savedSource, result);
    }

    @Test
    void addSource_whenDuplicates_shouldThrowException() {
        // Given
        SourceRequestDTO duplicateUrlSourceRequestDTO = new SourceRequestDTO("https://source1.com", "2 2 2 2 2 2", Map.of("key", "value"), true);

        when(repository.existsByUrl(duplicateUrlSourceRequestDTO.url())).thenReturn(true);

        // When
        SourceAlreadyExistsException exception = assertThrows(
                SourceAlreadyExistsException.class,
                () -> underTest.addSource(duplicateUrlSourceRequestDTO)
        );

        // Then
        assertEquals("Source with this url: " + duplicateUrlSourceRequestDTO.url() + " already exist", exception.getMessage());

        verify(repository).existsByUrl(duplicateUrlSourceRequestDTO.url());
        verifyNoInteractions(mapper);
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(schedulerParserService);
    }

    @Test
    void addSource_WhenUrlNotExistsButDbConstraintViolated_shouldThrowException() {
        // Given
        SourceRequestDTO sourceRequestDTO = new SourceRequestDTO("https://source.com", "2 2 2 2 2 2", Map.of("key", "value"), true);
        Source source = new Source(null, "https://source.com", "22222", Map.of("key", "value"), true);

        when(repository.existsByUrl(sourceRequestDTO.url())).thenReturn(false);
        when(mapper.fromDto(sourceRequestDTO)).thenReturn(source);
        when(repository.save(source)).thenThrow(DataIntegrityViolationException.class);

        // When
        SourceAlreadyExistsException exception = assertThrows(
                SourceAlreadyExistsException.class,
                () -> underTest.addSource(sourceRequestDTO)
        );

        // Then
        assertEquals("Source with this url: " + sourceRequestDTO.url() + " already exist", exception.getMessage());
        assertThat(exception).hasCauseExactlyInstanceOf(DataIntegrityViolationException.class);

        verify(repository).existsByUrl(sourceRequestDTO.url());
        verify(mapper).fromDto(sourceRequestDTO);
        verify(repository, times(1)).save(source);
        verifyNoMoreInteractions(repository);
        verifyNoMoreInteractions(schedulerParserService);
    }

    @Test
    void getAllSources_whenNoSources_shouldReturnEmptyList() {
        // Given
        when(repository.findAll()).thenReturn(List.of());

        // When
        List<SourceRequestDTO> emptyDtoList = underTest.getAllSources();

        // Then
        assertTrue(emptyDtoList.isEmpty());
        verify(repository).findAll();
        verifyNoInteractions(mapper);
    }

    @Test
    void getAllSources_WhenSourcesExist_shouldReturnDtoList() {
        // Given
        when(repository.findAll()).thenReturn(List.of(source1, source2));
        when(mapper.toDto(source1)).thenReturn(sourceRequestDTO1);
        when(mapper.toDto(source2)).thenReturn(sourceRequestDTO2);

        // When
        List<SourceRequestDTO> dtoList = underTest.getAllSources();

        // Then
        assertThat(dtoList).containsExactlyInAnyOrder(sourceRequestDTO1, sourceRequestDTO2);
        assertThat(dtoList).hasSize(2);

        verify(repository).findAll();
        verify(mapper).toDto(source1);
        verify(mapper).toDto(source2);
        verifyNoMoreInteractions(mapper);
    }

    @Test
    void deleteSource_whenSourceExist_shouldReturnNothing() {
        // Given
        Long id = 1L;

        when(repository.findById(id)).thenReturn(Optional.of(source1));

        // When
        underTest.deleteSource(id);

        // Then
        assertEquals(id, source1.getId());

        verify(repository).findById(id);
        verify(repository).delete(source1);
        verify(schedulerParserService).cancelScheduleSource(source1.getUrl());
        verifyNoMoreInteractions(repository);
    }

    @Test
    void deleteSource_whenSourceDoesntExist_shouldThrowException() {
        // Given
        Long id = 1L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        // When
        SourceNotFoundException exception = assertThrows(
                SourceNotFoundException.class,
                () -> underTest.deleteSource(id));

        // Then
        assertEquals("Source with such id not found", exception.getMessage());
        verify(repository).findById(id);
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(schedulerParserService);
    }

}