package com.crawler.web_crawler.scheduler;

import com.crawler.web_crawler.model.entity.Source;
import com.crawler.web_crawler.service.source.SourceService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SchedulerInitializer {
    private final SourceService sourceService;
    private final SchedulerParserService schedulerParserService;

    @PostConstruct
    public void init() {
        List<Source> activeSources = sourceService.getAllActiveSources();
        activeSources.forEach(
                schedulerParserService::addNewScheduleSource
        );

        log.info("Scheduled {} active sources", activeSources.size());
    }
}
