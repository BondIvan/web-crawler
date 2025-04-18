package com.crawler.web_crawler.service.implementation;

import com.crawler.web_crawler.model.entity.NewsArticle;
import com.crawler.web_crawler.model.entity.Source;
import com.crawler.web_crawler.parser.Parser;
import com.crawler.web_crawler.repository.NewsArticleRepository;
import com.crawler.web_crawler.service.NewsParserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
public class NewsParserServiceImpl implements NewsParserService {
    private static final int PARSE_TIME_LIMIT = 30;

    private final Parser parser;
    private final NewsArticleRepository repository;

    //TODO Consider adding a cache for article hashes, do not have to query the database for hashes every scan time
    //TODO Update tests for this class

    private NewsParserServiceImpl(Parser parser, NewsArticleRepository repository) {
        this.parser = parser;
        this.repository = repository;
    }

    @Override
    public void parseAndSave(Source source) {
        try {
            List<NewsArticle> uniqueNewsArticles = parseSource(source);

            if(uniqueNewsArticles.isEmpty()) {
                log.info("No new articles were found for source: {}", source.getUrl());
                return;
            }

            log.info("Found and save {} new articles for service {}", uniqueNewsArticles.size(), source.getUrl());
            repository.saveAll(uniqueNewsArticles);
        } catch (DataIntegrityViolationException | TimeoutException e) {
            log.error("The list of articles was not saved.\nReason: {}", e.getMessage());
            throw new RuntimeException("Runtime exception while saving data: " + e.getMessage(), e);
        }
    }

    private List<NewsArticle> parseSource(Source source) throws TimeoutException {
        long startParseTime = System.currentTimeMillis();

        List<NewsArticle> articleList = parser.parse(source);

        long duration = (System.currentTimeMillis() - startParseTime) / 1000;
        log.info("Scanning {} took {} seconds", source.getUrl(), duration);

        if(duration > PARSE_TIME_LIMIT) {
            log.error("Parsing source {} exceeded time limit ({} sec)", source.getUrl(), PARSE_TIME_LIMIT);
            throw new TimeoutException("Parsing source: " + source.getUrl() + " exceeded time limit (" + PARSE_TIME_LIMIT + " sec)");
        }

        Set<String> hashes = getHashesFromDb(source);
        articleList.removeIf((article) -> hashes.contains(article.getHash()));

        return articleList;
    }

    private Set<String> getHashesFromDb(Source source) {
        return repository.findHashesBySource(source);
    }

}
