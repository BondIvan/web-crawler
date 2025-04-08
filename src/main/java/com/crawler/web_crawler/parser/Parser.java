package com.crawler.web_crawler.parser;

import com.crawler.web_crawler.model.entity.NewsArticle;
import com.crawler.web_crawler.model.entity.Source;

import java.util.List;

public interface Parser {

    List<NewsArticle> parse(Source source);

}
