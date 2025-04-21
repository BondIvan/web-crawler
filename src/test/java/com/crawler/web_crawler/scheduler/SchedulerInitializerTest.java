package com.crawler.web_crawler.scheduler;

import com.crawler.web_crawler.model.entity.Source;
import com.crawler.web_crawler.service.source.SourceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchedulerInitializerTest {
    @Mock
    private SourceService sourceService;
    @Mock
    private SchedulerParserService scheduleParserService;
    @InjectMocks
    private SchedulerInitializer underTest;

    private Source source1;
    private Source source2;

    @BeforeEach
    void setUp() {
        source1 = new Source(1L, "https://source1.com", "0 * * * * *", Map.of("Key1Source1", "Value1Source1", "Key2Source1", "Value2Source1"), true);
        source2 = new Source(2L, "https://source2.com", "30 * * * * *", Map.of("Key1Source2", "Value1Source2", "Key2Source2", "Value2Source2"), true);
    }

    @Test
    void init_whenActiveSourcesExist_shouldAddSourcesToSchedule() {
        // Given
        List<Source> activeSources = List.of(source1, source2);

        when(sourceService.getAllActiveSources()).thenReturn(activeSources);

        // When
        underTest.init();

        // Then
        verify(scheduleParserService, times(2)).addNewScheduleSource(any(Source.class));
    }

    @Test
    void init_whenNoActiveSources_shouldNotScheduleAnyTask() {
        // Given
        when(sourceService.getAllActiveSources()).thenReturn(Collections.emptyList());

        // When
        underTest.init();

        // Then
        verifyNoInteractions(scheduleParserService);
    }

}