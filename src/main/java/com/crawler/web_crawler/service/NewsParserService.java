package com.crawler.web_crawler.service;

import com.crawler.web_crawler.model.entity.Source;

public interface NewsParserService {
    void parseAndSave(Source source);
}
