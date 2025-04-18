package com.crawler.web_crawler.scheduler;

import com.crawler.web_crawler.model.entity.Source;

public interface SchedulerParserService {
    void cancelScheduleSource(String sourceUrl);
    void addNewScheduleSource(Source source);
}
