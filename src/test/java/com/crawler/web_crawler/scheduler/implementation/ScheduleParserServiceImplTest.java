package com.crawler.web_crawler.scheduler.implementation;

import com.crawler.web_crawler.model.entity.Source;
import com.crawler.web_crawler.service.newsArticle.implementation.NewsParserServiceImpl;
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

import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleParserServiceImplTest {
    @Mock
    private TaskScheduler taskScheduler;
    @Mock
    private NewsParserServiceImpl newsParserService;
    @Captor
    private ArgumentCaptor<Runnable> runnableCaptor;
    @Captor
    private ArgumentCaptor<CronTrigger> triggerCaptor;
    @InjectMocks
    private ScheduleParserServiceImpl underTest;
    @Mock
    private ScheduledFuture<?> scheduledFuture;

    private Source activeSource;
    private Source inactiveSource;

    @BeforeEach
    void setUp() {
        activeSource = new Source(1L, "https://source1.com", "0 * * * * *", Map.of("Key1Source1", "Value1Source1", "Key2Source1", "Value2Source1"), true);
        inactiveSource = new Source(2L, "https://source2.com", "30 * * * * *", Map.of("Key1Source2", "Value1Source2", "Key2Source2", "Value2Source2"), false);
    }

    @Test
    void addNewScheduleSource_whenSourceIsActive_shouldAddFutureTaskToScheduler() {
        // Given
//        when(taskScheduler.schedule(
//                any(Runnable.class),
//                any(CronTrigger.class)
//                )
//        ).thenReturn((ScheduledFuture<?>)scheduledFuture);
//        Don't work and don't know why

        doReturn(scheduledFuture).when(taskScheduler).schedule(
                any(Runnable.class),
                any(CronTrigger.class)
        );

        // When
        underTest.addNewScheduleSource(activeSource);

        // Then
        Map<String, ScheduledFuture<?>> map = underTest.getScheduledSources();
        assertThat(map).hasSize(1);
        assertEquals(scheduledFuture, map.get(activeSource.getUrl()));

        verify(taskScheduler).schedule(runnableCaptor.capture(), triggerCaptor.capture());
        assertEquals(activeSource.getSchedule(), triggerCaptor.getValue().getExpression());

        runnableCaptor.getValue().run();
        verify(newsParserService).parseAndSave(activeSource);
    }

    @Test
    void addNewScheduleSource_whenSourceIsInactive_shouldNotAddToScheduler() {
        // Given

        // When
        underTest.addNewScheduleSource(inactiveSource);

        // Then
        Map<String, ScheduledFuture<?>> map = underTest.getScheduledSources();
        assertThat(map).isEmpty();

        verify(taskScheduler, never()).schedule(any(Runnable.class), any(CronTrigger.class));
    }

    @Test
    void addNewScheduleSource_whenSourceTaskAlreadyExist_shouldDeleteOldTaskAndAddNewTaskToScheduler() {
        // Given
        doReturn(scheduledFuture).when(taskScheduler).schedule(
                any(Runnable.class),
                any(CronTrigger.class)
        );

        // When
        underTest.addNewScheduleSource(activeSource);

        underTest.addNewScheduleSource(activeSource);

        // Then
        verify(scheduledFuture).cancel(true);
        verify(taskScheduler, times(2)).schedule(any(Runnable.class), any(CronTrigger.class));
    }

    @Test
    void errorWhileExecutingRunnableTask_shouldLogException() {
        // Given
        RuntimeException runtimeException = new RuntimeException("Some trouble while parsing");
        doThrow(runtimeException).when(newsParserService).parseAndSave(activeSource);

        doReturn(scheduledFuture).when(taskScheduler).schedule(
                any(Runnable.class),
                any(CronTrigger.class)
        );

        // When
        underTest.addNewScheduleSource(activeSource);

        verify(taskScheduler).schedule(runnableCaptor.capture(), triggerCaptor.capture());
        runnableCaptor.getValue().run();

        // Then
        verify(newsParserService).parseAndSave(activeSource);
//        assertThatThrownBy(() -> runnableCaptor.getValue().run())
//                .isInstanceOf(RuntimeException.class)
//                .hasMessage("Some trouble while parsing");
    }

    @Test
    void cancelScheduleSource_whenSourceExist_shouldDeleteTaskFromSchedulerWithInterrupted() {
        // Given
        doReturn(scheduledFuture).when(taskScheduler).schedule(
                any(Runnable.class),
                any(CronTrigger.class)
        );
        underTest.addNewScheduleSource(activeSource);

        // When
        underTest.cancelScheduleSource(activeSource.getUrl());

        // Then
        Map<String, ScheduledFuture<?>> map = underTest.getScheduledSources();
        assertThat(map).isEmpty();
        verify(scheduledFuture).cancel(true);
    }

    @Test
    void cancelScheduleSource_whenSourceDoesntExist_shouldNotDeleteTaskFromScheduler() {
        // Given

        // When
        underTest.cancelScheduleSource("No task with this url");

        // Then
        verifyNoInteractions(scheduledFuture);

    }

}