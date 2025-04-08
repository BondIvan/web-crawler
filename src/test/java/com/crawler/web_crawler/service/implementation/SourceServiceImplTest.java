package com.crawler.web_crawler.service.implementation;

import com.crawler.web_crawler.converter.SourceRequestDtoMapper;
import com.crawler.web_crawler.exception.SourceAlreadyExistsException;
import com.crawler.web_crawler.exception.SourceNotFoundException;
import com.crawler.web_crawler.model.dto.SourceRequestDTO;
import com.crawler.web_crawler.model.entity.Source;
import com.crawler.web_crawler.repository.SourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
    @InjectMocks
    private SourceServiceImpl underTest;

    private Source source1;
    private Source source2;
    private SourceRequestDTO sourceRequestDTO1;
    private SourceRequestDTO sourceRequestDTO2;

    @BeforeEach
    void setUp() {
        source1 = new Source(1L, "https://source1.com", "12345", Map.of("Key1Source1", "Value1Source1", "Key2Source1", "Value2Source1"));
        source2 = new Source(2L, "https://source2.com", "67890", Map.of("Key1Source2", "Value1Source2", "Key2Source2", "Value2Source2"));
        sourceRequestDTO1 = new SourceRequestDTO("https://source1.com", "12345", Map.of("Key1Source1", "Value1Source1", "Key2Source1", "Value2Source1"));
        sourceRequestDTO2 = new SourceRequestDTO("https://source2.com", "67890", Map.of("Key1Source2", "Value1Source2", "Key2Source2", "Value2Source2"));
    }

    @Test
    @Disabled
    void getSource() {
    }

    @Test
    void addSource_whenNoDuplicates_shouldSaveAndReturnNewSource() {
        // Given
        SourceRequestDTO sourceRequestDTO = new SourceRequestDTO("https://source.com", "11111", Map.of("key", "value"));
        Source newSource = new Source(null, "https://source.com", "11111", Map.of("key", "value"));
        Source savedSource = new Source(3L, "https://source.com", "11111", Map.of("key", "value"));

        when(repository.existsByUrl(sourceRequestDTO.url())).thenReturn(false);
        when(mapper.fromDto(sourceRequestDTO)).thenReturn(newSource);
        when(repository.save(newSource)).thenReturn(savedSource);

        // When
        Source result = underTest.addSource(sourceRequestDTO);

        // Then
        verify(repository).existsByUrl(sourceRequestDTO.url());
        verify(mapper).fromDto(sourceRequestDTO);
        verify(repository).save(newSource);
        assertEquals(savedSource, result);
    }

    @Test
    void addSource_whenDuplicates_shouldThrowException() {
        // Given
        SourceRequestDTO duplicateUrlSourceRequestDTO = new SourceRequestDTO("https://source1.com", "22222", Map.of("key", "value"));

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
    }

    @Test
    void addSource_WhenUrlNotExistsButDbConstraintViolated_ShouldThrowException() {
        // Given
        SourceRequestDTO sourceRequestDTO = new SourceRequestDTO("https://source.com", "22222", Map.of("key", "value"));
        Source source = new Source(null, "https://source.com", "22222", Map.of("key", "value"));

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
        assertIterableEquals(List.of(sourceRequestDTO1, sourceRequestDTO2), dtoList);
        assertEquals(2, dtoList.size());

        verify(repository).findAll();
        verify(mapper).toDto(source1);
        verify(mapper).toDto(source2);
        verifyNoMoreInteractions(mapper);
    }

    @Test
    void deleteSource_whenSourceExist_shouldReturnNothing() {
        // Given
        Long id = 1L;
        Optional<Source> optionalSource1 = Optional.of(source1);

        when(repository.findById(id)).thenReturn(optionalSource1);

        // When
        underTest.deleteSource(id);

        // Then
        assertEquals(id, source1.getId());

        verify(repository).findById(id);
        verify(repository).delete(source1);
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
    }
}