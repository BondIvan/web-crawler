package com.crawler.web_crawler.parser;

import com.crawler.web_crawler.exception.JsoupException.JsoupClientException;
import com.crawler.web_crawler.exception.JsoupException.JsoupParseException;
import com.crawler.web_crawler.model.entity.NewsArticle;
import com.crawler.web_crawler.model.entity.Source;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class JsoupParser implements Parser {
    public static final String SELECTOR_TITLE = "title";
    public static final String SELECTOR_CONTENT = "content";
    public static final String SELECTOR_DATE = "date";

    private final DateParser dateParser;
    private final JsoupClient jsoupClient;

    public JsoupParser(DateParser dateParser, JsoupClient jsoupClient) {
        this.dateParser = dateParser;
        this.jsoupClient = jsoupClient;
    }

    @Override
    public List<NewsArticle> parse(Source source) {
        log.info("Start scanning {} for new articles", source.getUrl());
        List<NewsArticle> articles = new ArrayList<>();

        String url = source.getUrl();
        Map<String, String> selectors = source.getSelectors(); // title, content, date
        Document document;
        try {
            document = jsoupClient.getPageByUrl(url);
        } catch (JsoupClientException e) {
            throw new JsoupParseException(e.getMessage());
        }

        //TODO Add scan other pages for more news
        //TODO Add a separate page for testing css-selectors

        Elements titleElements = document.select(selectors.get(SELECTOR_TITLE));
        Elements contentElements = document.select(selectors.get(SELECTOR_CONTENT));
        Elements publishDateElements = document.select(selectors.get(SELECTOR_DATE));

        List<String> titles = fromElementsToList(titleElements);
        List<String> contents = fromElementsToList(contentElements);
        List<String> publishDates = fromElementsToList(publishDateElements);

        int size = titles.size() == publishDates.size() ? titles.size() : -1;
        if(size == -1) {
            log.warn("The number of titles does not match the number of dates");
            throw new JsoupParseException("The number of titles does not match the number of dates");
        }

        for(int i = 0; i < size; i++) {
            String title = titles.get(i);
            String content = contents.isEmpty() ? null : contents.get(i);
            LocalDate publishDate = dateParser.toLocalDateFromString(publishDates.get(i));
            if(title == null || publishDate == null) {
                log.warn("Skipping article due to missing fields {}", title == null ? "title" : "publish date");
                continue;
            }

            NewsArticle article = createArticle(source, title, content, publishDate);
            articles.add(article);
        }

        log.info("Stop scanning {}", source.getUrl());
        return articles;
    }

    private NewsArticle createArticle(Source source, String title, String content, LocalDate date) {
        NewsArticle article = new NewsArticle();
        article.setTitle(title);
        article.setContent(content);
        article.setPublishDate(date);
        article.setSource(source);
        article.setHash();

        return article;
    }

    private List<String> fromElementsToList(Elements elements) {
        if(elements == null) {
            log.warn("The elements list received from jsoup is null");
            throw new JsoupParseException("The elements list received from jsoup is null");
        }

        return elements.stream()
                .map(Element::text)
                .toList();
    }

}

