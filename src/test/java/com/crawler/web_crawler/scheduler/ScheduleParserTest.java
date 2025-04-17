package com.crawler.web_crawler.scheduler;

import com.crawler.web_crawler.model.entity.Source;
import com.crawler.web_crawler.service.implementation.NewsParserServiceImpl;
import com.crawler.web_crawler.service.implementation.SourceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleParserTest {
    @Mock
    private TaskScheduler taskScheduler;
    @Mock
    private SourceServiceImpl sourceService;
    @Mock
    private NewsParserServiceImpl newsParserService;
    @InjectMocks
    private ScheduleParser underTest;
    @Captor
    private ArgumentCaptor<Runnable> runnableCaptor;
    @Captor
    private ArgumentCaptor<CronTrigger> cronTriggerCaptor;

    private Source source1;
    private Source source2;
    private Source source3;

    @BeforeEach
    void setUp() {
        source1 = new Source(1L, "https://source1.com", "0 * * * * *", Map.of("Key1Source1", "Value1Source1", "Key2Source1", "Value2Source1"), true);
        source2 = new Source(2L, "https://source2.com", "30 * * * * *", Map.of("Key1Source2", "Value1Source2", "Key2Source2", "Value2Source2"), true);
        source3 = new Source(3L, "https://source3.com", "invalid cron", Map.of("Key1Source3", "Value1Source3", "Key2Source3", "Value2Source3"), true);
    }

    @Test
    void init_whenExistActiveSources_shouldScheduleAllActiveSource() {
        // Given
        List<Source> activeSource = List.of(source1, source2);

        when(sourceService.getAllActiveSources()).thenReturn(activeSource);

        // When
        underTest.init();

        // Then
        verify(taskScheduler, times(2)).schedule(any(Runnable.class), any(CronTrigger.class));
    }

    @Test
    void init_whenNoActiveSources_shouldNotScheduleAnyTask() {
        // Given
        when(sourceService.getAllActiveSources()).thenReturn(Collections.emptyList());

        // When
        underTest.init();

        // Then
        verifyNoInteractions(taskScheduler);
    }

    @Test
    void scheduleParse_whenCronExpressionIsCorrect_shouldCheckCorrectnessOfTheExpression() {
        // Given

        // When
        underTest.scheduleParse(source1);

        // Then
        verify(taskScheduler).schedule(
                runnableCaptor.capture(),
                cronTriggerCaptor.capture()
        );

        CronTrigger cronTrigger = cronTriggerCaptor.getValue();
        assertEquals("0 * * * * *", cronTrigger.getExpression());
    }

    @Test
    void scheduleParse_whenCronExpressionIsCorrect_shouldExecuteTaskAndCallParseAndSave() {
        // Given

        // When
        underTest.scheduleParse(source1);

        // Then
        verify(taskScheduler).schedule(
                runnableCaptor.capture(),
                cronTriggerCaptor.capture()
        );

        Runnable task = runnableCaptor.getValue();
        task.run();

        verify(newsParserService).parseAndSave(source1);
    }

    @Test
    void scheduleParse_whenCronExpressionIncorrect_shouldLogError() {
        // Given

        // When
        assertThrows(
                IllegalArgumentException.class,
                () -> underTest.scheduleParse(source3)
        );

        // Then
        verify(taskScheduler, never()).schedule(any(Runnable.class), any(CronTrigger.class));
    }
}