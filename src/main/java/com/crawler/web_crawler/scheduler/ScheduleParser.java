package com.crawler.web_crawler.scheduler;

import com.crawler.web_crawler.model.entity.Source;
import com.crawler.web_crawler.service.NewsParserService;
import com.crawler.web_crawler.service.SourceService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class ScheduleParser {
    private final TaskScheduler taskScheduler;
    private final SourceService sourceService;
    private final NewsParserService newsParserService;

    @PostConstruct
    public void init() {
        List<Source> sources = sourceService.getAllActiveSources();
        sources.forEach(this::scheduleParse);
    }

    public void scheduleParse(Source source) {
        taskScheduler.schedule(
                () -> newsParserService.parseAndSave(source),
                new CronTrigger(source.getSchedule())
        );
    }

}
