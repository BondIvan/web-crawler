package com.crawler.web_crawler.service.implementation;

import com.crawler.web_crawler.model.entity.NewsArticle;
import com.crawler.web_crawler.model.entity.Source;
import com.crawler.web_crawler.parser.JsoupParser;
import com.crawler.web_crawler.repository.NewsArticleRepository;
import com.crawler.web_crawler.service.NewsParserService;
import lombok.extern.slf4j.Slf4j;
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

        for(NewsArticle article: articleList) {
            System.out.println(article.toString());
            System.out.println();
        }

        repository.saveAll(articleList);
    }

    private Set<String> getHashesFromDb() {
        return repository.findAllByHash();
    }

}
