package com.crawler.web_crawler.service.newsArticle;

import com.crawler.web_crawler.model.entity.Source;

public interface NewsParserService {
    void parseAndSave(Source source);
}
