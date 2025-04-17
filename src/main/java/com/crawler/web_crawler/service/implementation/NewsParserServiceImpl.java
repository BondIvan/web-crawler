package com.crawler.web_crawler.service.implementation;

import com.crawler.web_crawler.model.entity.NewsArticle;
import com.crawler.web_crawler.model.entity.Source;
import com.crawler.web_crawler.parser.JsoupParser;
import com.crawler.web_crawler.parser.Parser;
import com.crawler.web_crawler.repository.NewsArticleRepository;
import com.crawler.web_crawler.service.NewsParserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class NewsParserServiceImpl implements NewsParserService {
    private final Parser parser;
    private final NewsArticleRepository repository;

    //TODO Consider adding a cache for article hashes, do not have to query the database for hashes every scan time

    private NewsParserServiceImpl(Parser parser, NewsArticleRepository repository) {
        this.parser = parser;
        this.repository = repository;
    }

    @Override
    public void parseAndSave(Source source) {
        List<NewsArticle> articleList = parser.parse(source);
        Set<String> hashes = getHashesFromDb(source);

        articleList.removeIf((article) -> hashes.contains(article.getHash()));

        if(articleList.isEmpty()) {
            log.info("No new articles were found for source: {}", source.getUrl());
            return;
        }

        try {
            log.info("Found and save {} new articles for service {}", articleList.size(), source.getUrl());
            repository.saveAll(articleList);
        } catch (DataIntegrityViolationException e) {
            log.error("The list of articles was not saved.\nReason: {}", e.getMessage());
            throw new RuntimeException("Runtime exception while saving data: " + e.getMessage(), e);
        }
    }

    private Set<String> getHashesFromDb(Source source) {
        return repository.findHashesBySource(source);
    }

}
