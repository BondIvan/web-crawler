package com.crawler.web_crawler.service.implementation;

import com.crawler.web_crawler.model.entity.NewsArticle;
import com.crawler.web_crawler.model.entity.Source;
import com.crawler.web_crawler.parser.JsoupParser;
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
    private final JsoupParser parser;
    private final NewsArticleRepository repository;

    private NewsParserServiceImpl(JsoupParser parser, NewsArticleRepository repository) {
        this.parser = parser;
        this.repository = repository;
    }

    @Override
    public void parseAndSave(Source source) {
        List<NewsArticle> articleList = parser.parse(source);

        Set<String> hashes = getHashesFromDb();
        articleList.removeIf((article) -> hashes.contains(article.getHash()));

//TODO Consider adding a cache for article hashes, do not have to query the database for hashes every scan time

        if(articleList.isEmpty()) {
            log.info("No new articles were found.");
            return;
        }

        try {
            log.info("Found {} new articles", articleList.size());
            repository.saveAll(articleList);
        } catch (DataIntegrityViolationException e) {
            log.error("The list of articles was not saved.\nReason: {}", e.getMessage());
            throw new RuntimeException("Runtime exception: " + e.getMessage(), e);
        }
    }

    private Set<String> getHashesFromDb() {
        return repository.findAllByHash();
    }

}
