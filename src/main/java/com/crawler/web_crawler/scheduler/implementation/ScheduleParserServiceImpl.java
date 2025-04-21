package com.crawler.web_crawler.scheduler.implementation;

import com.crawler.web_crawler.model.entity.Source;
import com.crawler.web_crawler.scheduler.SchedulerParserService;
import com.crawler.web_crawler.service.newsArticle.NewsParserService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@RequiredArgsConstructor
@Service
public class ScheduleParserServiceImpl implements SchedulerParserService {
    private final TaskScheduler taskScheduler;
    private final NewsParserService newsParserService;

    @Getter
    private final Map<String, ScheduledFuture<?>> scheduledSources = new ConcurrentHashMap<>();

    @Override
    public void cancelScheduleSource(String sourceUrl) {
        ScheduledFuture<?> future = scheduledSources.remove(sourceUrl);
        if(future != null) {
            log.info("Interrupt execute parsing for this source {}", sourceUrl);
            future.cancel(true); // Passing true means interrupted the task if needed.
        }
    }

    @Override
    public void addNewScheduleSource(Source source) {
        cancelScheduleSource(source.getUrl());
        if(!source.getIsActive())
            return;

        CronTrigger cronTriggerSource = new CronTrigger(source.getSchedule());
        Runnable parseTask = () -> {
            try {
                newsParserService.parseAndSave(source);
            } catch (Exception e) {
                log.error("Error during task execution with source {}. Error message: {}", source.getUrl(), e.getMessage());
            }
        };

        ScheduledFuture<?> scheduledFuture = taskScheduler.schedule(
                parseTask,
                cronTriggerSource
        );

        scheduledSources.put(source.getUrl(), scheduledFuture);
    }

}
